package io.confluent.testcontainers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;

import org.junit.BeforeClass;
import org.junit.Test;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.output.Slf4jLogConsumer;

import java.util.List;
import java.util.Map;

import io.confluent.ksql.rest.entity.KsqlRequest;
import io.confluent.testcontainers.ksqldb.AbstractKsqlServerContainerTest;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import static io.confluent.testcontainers.Constants.KAFKA_TEST_IMAGE;
import static io.confluent.testcontainers.CpKsqlDbServerContainer.KSQL_REQUEST_CONTENT_TYPE;
import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.testcontainers.utility.DockerImageName.parse;

@Slf4j
public class CpKsqlDbServerContainerTest extends AbstractKsqlServerContainerTest {

  private static final KafkaContainer kafka = new KafkaContainer(parse(KAFKA_TEST_IMAGE)).withNetwork(Network.newNetwork());
  private static final SchemaRegistryContainer schemaRegistry = new SchemaRegistryContainer("5.5.1");

  @BeforeClass
  public static void setUpClass() {
    // for ksql command topic
    kafka.addEnv("KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR", "1");
    kafka.addEnv("KAFKA_TRANSACTION_STATE_LOG_MIN_ISR", "1");
    kafka.addEnv("KAFKA_TRANSACTION_STATE_LOG_REPLICATION_FACTOR", "1");
    kafka.start();

    schemaRegistry.withKafka(kafka).start();
  }

  @Test
  public void shouldStartWithKafka() {
    try (CpKsqlDbServerContainer ksqlServer = new CpKsqlDbServerContainer("5.5.1")) {
      ksqlServer.withKafka(kafka)
          .withLogConsumer(new Slf4jLogConsumer(log))
          .start();

      // https://github.com/confluentinc/ksql/blob/master/docs/developer-guide/api.rst#get-the-status-of-a-ksql-server
      getKsqlServerInfoVersion(ksqlServer, "5.5.1");
    }
  }

  @Test
  public void shouldStartWithSchemaRegistry() {
    try (CpKsqlDbServerContainer ksqlServer = new CpKsqlDbServerContainer("5.5.1")) {
      ksqlServer
          .withKafka(kafka)
          .withSchemaRegistry(schemaRegistry)
          .withLogConsumer(new Slf4jLogConsumer(log))
          .start();

      final String
          ksqlRequest = createKsqlRequestJSON("show properties;", null);

      final JsonPath jsonPath = given()
          .body(ksqlRequest)
          .contentType(KSQL_REQUEST_CONTENT_TYPE)
          .when()
          .post(ksqlServer.getTarget() + "/ksql")
          .then().contentType(ContentType.JSON).extract().response().jsonPath();

      //  jsonPath.get("[0].properties.find {'ksql.schema.registry.url' == it['name']}")
      final List<Map<String, String>> properties = jsonPath.get("[0].properties");
      properties
          .stream()
          .filter(property -> "ksql.schema.registry.url".equals(property.get("name")))
          .forEach(property -> assertThat(property.get("value"), equalTo(schemaRegistry.getSchemaRegistryUrl())));
    }
  }

  @SneakyThrows
  private String createKsqlRequestJSON(final String ksqlStatement, final Map<String, ?> params) {
    return new ObjectMapper()
        // to properly handle `Optional` serialization
        .registerModule(new Jdk8Module())
        .writeValueAsString(new KsqlRequest(ksqlStatement, params, null));
  }

  @Test
  public void shouldCreateStream() {

    try (CpKsqlDbServerContainer cpKsqlServerContainer = new CpKsqlDbServerContainer("5.5.1")) {
      cpKsqlServerContainer
          .withKafka(kafka)
          .withSchemaRegistry(schemaRegistry)
          .withLogConsumer(new Slf4jLogConsumer(log))
          .start();

      String
          statement =
          "CREATE STREAM movies_avro (ROWKEY BIGINT KEY, title VARCHAR, release_year INT) WITH (KAFKA_TOPIC='avro-movies',           PARTITIONS=1, VALUE_FORMAT='avro');";

      /*
        [
              {
                "@type": "currentStatus",
                "statementText": "CREATE STREAM movies_avro (ROWKEY BIGINT KEY, title VARCHAR, release_year INT)     WITH (KAFKA_TOPIC='avro-movies',           PARTITIONS=1,           VALUE_FORMAT='avro');",
                "commandId": "stream/`MOVIES_AVRO`/create",
                "commandStatus": {
                  "status": "SUCCESS",
                  "message": "Stream created"
                },
                "commandSequenceNumber": 0,
                "warnings": []
              }
            ]

       */
      given()
          .body(createKsqlRequestJSON(statement, null))
          .contentType(KSQL_REQUEST_CONTENT_TYPE)
          .when()
          .post(cpKsqlServerContainer.getTarget() + "/ksql")
          .then()
          .contentType(ContentType.JSON).
          body("[0].commandStatus.status", equalTo("SUCCESS"));

      /*
        a response from ksqlDB server looks like

        [
          {
            "@type": "streams",
            "statementText": "LIST STREAMS;",
            "streams": [
              {
                "type": "STREAM",
                "name": "MOVIES_AVRO",
                "topic": "avro-movies",
                "format": "AVRO"
              }
            ],
            "warnings": []
          }
        ]
       */

      final JsonPath jsonPath = given()
          .body(createKsqlRequestJSON("LIST STREAMS;", null))
          .contentType(KSQL_REQUEST_CONTENT_TYPE)
          .when()
          .post(cpKsqlServerContainer.getTarget() + "/ksql").
              then().contentType(ContentType.JSON).extract().response().jsonPath();

      final Map<String, String> o = jsonPath.get("[0].streams[0]");
      assertThat("STREAM", equalTo(o.get("type")));
      assertThat("MOVIES_AVRO", equalTo(o.get("name")));
      assertThat("avro-movies", equalTo(o.get("topic")));
      assertThat("AVRO", equalTo(o.get("format")));

    }
  }
}
