package io.confluent.testcontainers;

import org.testcontainers.containers.GenericContainer;

import java.util.HashMap;

class CPKafkaContainer extends GenericContainer<CPKafkaContainer> {

  // TODO: ise enum with all versions
  public static final String CONFLUENTINC_CP_KAFKA_3_3_0 = "confluentinc/cp-kafka:3.3.0";
  public static final int KAFKA_PORT = 9092;
  private final String zookeeperUrl;

  public CPKafkaContainer(String dockerImageName, String zookeeperHost) {
    super(dockerImageName);
    this.zookeeperUrl = zookeeperHost;
  }

  @Override
  protected void configure() {
    super.configure();

    HashMap<String, String> envs = new HashMap<>();
    envs.put("KAFKA_ZOOKEEPER_CONNECT", this.zookeeperUrl);
    envs.put("KAFKA_ADVERTISED_LISTENERS", "PLAINTEXT://kafka:" + KAFKA_PORT);
    envs.put("KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR", String.valueOf(1));
    withEnv(envs);
    withExposedPorts(KAFKA_PORT);
    ;
  }

  public String kafkaUrl() {
    String containerIpAddress = this.getContainerIpAddress();
    Integer mappedPort = this.getMappedPort(KAFKA_PORT);
    return String.format("%s:%s", containerIpAddress, mappedPort);
  }
}

