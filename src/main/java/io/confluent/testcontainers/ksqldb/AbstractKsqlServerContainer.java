package io.confluent.testcontainers.ksqldb;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.Network;

import io.confluent.testcontainers.CpKsqlDbServerContainer;
import io.confluent.testcontainers.SchemaRegistryContainer;
import lombok.NonNull;

import static org.testcontainers.containers.wait.strategy.Wait.forLogMessage;

public abstract class AbstractKsqlServerContainer extends GenericContainer<AbstractKsqlServerContainer> {

  protected static final int KSQL_PORT = 8088;
  public static String KSQL_REQUEST_CONTENT_TYPE = "application/vnd.ksql.v1+json";

  public AbstractKsqlServerContainer(final @NonNull String dockerImageName) {
    super(dockerImageName);
  }

  public AbstractKsqlServerContainer withKafka(KafkaContainer kafka) {

    return withKafka(kafka.getNetwork(), kafka.getNetworkAliases().get(0) + ":9092");
  }

  public AbstractKsqlServerContainer withKafka(Network network, String bootstrapServers) {
    withNetwork(network);
    withEnv("KSQL_HOST_NAME", "ksql-server");
    withEnv("KSQL_LISTENERS", "http://0.0.0.0:" + KSQL_PORT);
    withEnv("KSQL_BOOTSTRAP_SERVERS", bootstrapServers);
    waitingFor(
        forLogMessage(".*INFO Server up and running.*\\n", 1));
    return self();
  }

  public AbstractKsqlServerContainer withSchemaRegistry(SchemaRegistryContainer schemaRegistry) {
    withEnv("KSQL_KSQL_SCHEMA_REGISTRY_URL", schemaRegistry.getSchemaRegistryUrl());
    return self();
  }

  public String getTarget() {
    return "http://" + getContainerIpAddress() + ":" + getMappedPort(KSQL_PORT);
  }
}
