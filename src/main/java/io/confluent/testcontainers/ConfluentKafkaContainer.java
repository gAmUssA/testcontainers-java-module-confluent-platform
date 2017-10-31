package io.confluent.testcontainers;

import org.testcontainers.containers.GenericContainer;

import java.util.HashMap;

class ConfluentKafkaContainer extends GenericContainer<ConfluentKafkaContainer> {

  // TODO: use enum with all versions
  public static final String CONFLUENTINC_CP_KAFKA_3_3_0 = "confluentinc/cp-kafka:3.3.0";
  public static final String CONFLUENTINC_CP_KAFKA_LATEST = "confluentinc/cp-kafka:latest";
  public static final int KAFKA_PORT = 9092;
  private final String zookeeperUrl;

  public ConfluentKafkaContainer(String dockerImageName, String zookeeperUrl) {
    super(dockerImageName);
    this.zookeeperUrl = zookeeperUrl;
  }

  @Override
  protected void configure() {
    super.configure();

    HashMap<String, String> envs = new HashMap<>();
    envs.put("KAFKA_ZOOKEEPER_CONNECT", this.zookeeperUrl);
    envs.put("KAFKA_ADVERTISED_LISTENERS", "PLAINTEXT://:" + KAFKA_PORT);
    envs.put("KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR", String.valueOf(1));
    envs.put("KAFKA_BROKER_ID", String.valueOf(1));
    withEnv(envs);
    withExposedPorts(KAFKA_PORT);

  }

  public String kafkaUrl() {
    String containerIpAddress = this.getContainerIpAddress();
    Integer mappedPort = this.getMappedPort(KAFKA_PORT);
    return String.format("%s:%s", containerIpAddress, mappedPort);
  }
}

