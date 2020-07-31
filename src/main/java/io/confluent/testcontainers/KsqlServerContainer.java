package io.confluent.testcontainers;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.Network;

import static org.testcontainers.containers.wait.strategy.Wait.forLogMessage;

/**
 * This container wraps ksqlDB container
 * To learn more about KSQL visit https://www.confluent.io/product/ksql/
 *
 * @since 0.1
 */
public class KsqlServerContainer extends GenericContainer<KsqlServerContainer> {

  private static final int KSQL_PORT = 8088;
  public static String KSQL_REQUEST_CONTENT_TYPE = "application/vnd.ksql.v1+json";

  public KsqlServerContainer(String version) {
    super("confluentinc/cp-ksqldb-server:" + version);
    withExposedPorts(KSQL_PORT);
  }

  public KsqlServerContainer withKafka(KafkaContainer kafka) {
    
    return withKafka(kafka.getNetwork(), kafka.getNetworkAliases().get(0) + ":9092");
  }

  public KsqlServerContainer withKafka(Network network, String bootstrapServers) {
    withNetwork(network);
    withEnv("KSQL_HOST_NAME", "ksql-server");
    withEnv("KSQL_LISTENERS", "http://0.0.0.0:" + KSQL_PORT);
    withEnv("KSQL_BOOTSTRAP_SERVERS", bootstrapServers);
    waitingFor(
        forLogMessage(".*INFO Server up and running.*\\n", 1));
    return self();
  }

  public KsqlServerContainer withSchemaRegistry(SchemaRegistryContainer schemaRegistry) {
    withEnv("KSQL_KSQL_SCHEMA_REGISTRY_URL", schemaRegistry.getSchemaRegistryUrl());
    return self();
  }

  public String getTarget() {
    return "http://" + getContainerIpAddress() + ":" + getMappedPort(KSQL_PORT);
  }

}
