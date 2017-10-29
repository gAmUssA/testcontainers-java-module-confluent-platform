package io.confluent.testcontainers;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Test;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.Wait;

import java.util.Collection;

import io.confluent.testcontainers.support.HelloConsumer;
import io.confluent.testcontainers.support.HelloProducer;
import lombok.extern.slf4j.Slf4j;

import static io.confluent.testcontainers.CPKafkaContainer.CONFLUENTINC_CP_KAFKA_3_3_0;
import static io.confluent.testcontainers.ZookeeperContainer.ZOOKEEPER_PORT;

@Slf4j
public class ProducerConsumerTest {

  @ClassRule
  public static Network network = Network.newNetwork();

  @ClassRule
  public static ZookeeperContainer
      zooCp =
      new ZookeeperContainer("confluentinc/cp-zookeeper:3.3.0")
          .withNetwork(network)
          .withNetworkAliases("zookeeper")
          .waitingFor(Wait.forListeningPort())
          .withLogConsumer(new Slf4jLogConsumer(log));

  @ClassRule
  public static CPKafkaContainer
      cp =
      new CPKafkaContainer(CONFLUENTINC_CP_KAFKA_3_3_0, "zookeeper:" + ZOOKEEPER_PORT)
          .withNetworkAliases("kafka")
          .waitingFor(Wait.forListeningPort())
          .withNetwork(network)
          .withLogConsumer(new Slf4jLogConsumer(log));


  @Test
  public void testProducerConsumer() {
    String containerIpAddress = cp.getContainerIpAddress();
    System.out.println("containerIpAddress = " + containerIpAddress);
    Integer zookeeperPort = zooCp.getMappedPort(2181);
    System.out.println("zookeeperPort = " + zookeeperPort);
    Integer kafkaPort = cp.getMappedPort(9092);
    System.out.println("kafkaPort = " + kafkaPort);

    HelloProducer helloProducer = new HelloProducer();
    helloProducer.createProducer(cp.kafkaUrl());

    HelloConsumer helloConsumer = new HelloConsumer(cp.kafkaUrl());
    helloConsumer.consume();
    Collection<ConsumerRecord> messages = helloConsumer.getReceivedRecords();

    Assert.assertEquals("message consumed", messages.size(), 5);
    messages.forEach(stringStringConsumerRecord -> {
      Assert.assertEquals(stringStringConsumerRecord.key(), "testContainers");
      Assert.assertEquals(stringStringConsumerRecord.value(), "AreAwesome");
    });
  }

}
