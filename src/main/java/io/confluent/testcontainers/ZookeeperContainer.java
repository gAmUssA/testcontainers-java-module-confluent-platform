package io.confluent.testcontainers;

import org.testcontainers.containers.GenericContainer;

public class ZookeeperContainer extends GenericContainer<ZookeeperContainer> {

  public static int ZOOKEEPER_PORT = 2181;

  public ZookeeperContainer(String dockerImageName) {
    super(dockerImageName);
  }

  @Override
  protected void configure() {
    super.configure();

    withExposedPorts(ZOOKEEPER_PORT);

    withEnv("ZOOKEEPER_TICK_TIME", String.valueOf(2000));
    withEnv("ZOOKEEPER_CLIENT_PORT", String.valueOf(ZOOKEEPER_PORT));

  }

  public String zookeeperUrl() {
    String containerIpAddress = this.getContainerIpAddress();
    Integer mappedPort = this.getMappedPort(ZOOKEEPER_PORT);
    return String.format("%s:%s", containerIpAddress, mappedPort);
  }
}
