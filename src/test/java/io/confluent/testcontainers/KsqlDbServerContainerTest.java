package io.confluent.testcontainers;

import static io.confluent.testcontainers.Constants.KAFKA_TEST_IMAGE;
import static org.testcontainers.utility.DockerImageName.parse;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.output.Slf4jLogConsumer;

import io.confluent.testcontainers.ksqldb.AbstractKsqlServerContainerTest;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class KsqlDbServerContainerTest extends AbstractKsqlServerContainerTest {

  private static final KafkaContainer kafka = new KafkaContainer(parse(KAFKA_TEST_IMAGE))
      .withNetwork(Network.newNetwork());

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
    final String version = "0.11.0";

    try (KsqlDbServerContainer ksqlServer = new KsqlDbServerContainer(version)) {
      ksqlServer.withKafka(kafka)
          .withLogConsumer(new Slf4jLogConsumer(log))
          .start();

      // https://github.com/confluentinc/ksql/blob/master/docs/developer-guide/api.rst#get-the-status-of-a-ksql-server
      getKsqlServerInfoVersion(ksqlServer, version);
    }
  }

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }
}
