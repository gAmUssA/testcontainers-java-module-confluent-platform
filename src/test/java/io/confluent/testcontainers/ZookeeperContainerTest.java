package io.confluent.testcontainers;

import org.junit.ClassRule;
import org.junit.Test;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.Wait;

import lombok.extern.slf4j.Slf4j;

import static org.junit.Assert.*;

@Slf4j
public class ZookeeperContainerTest {

  @ClassRule
  public static ZookeeperContainer
      zooCp =
      new ZookeeperContainer("confluentinc/cp-zookeeper:latest")
          .withNetworkAliases("zookeeper")
          .waitingFor(Wait.forListeningPort());

  @Test
  public void zookeeperUrl() {
    zooCp.followOutput(new Slf4jLogConsumer(log));
    System.out.println(zooCp.zookeeperUrl());
  }
}