package io.confluent.testcontainers;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Test;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.output.Slf4jLogConsumer;

import java.util.Collection;

import io.confluent.testcontainers.support.HelloConsumer;
import io.confluent.testcontainers.support.HelloProducer;
import lombok.extern.slf4j.Slf4j;

import static io.confluent.testcontainers.ConfluentKafkaContainer.CONFLUENTINC_CP_KAFKA_LATEST;

@Ignore("Doesn't work yet. Some network config issue. Ignoring for now")
@Slf4j
public class ProducerConsumerTest {

  @ClassRule
  public static Network network = Network.newNetwork();

  @ClassRule
  public static ZookeeperContainer
      zooCp =
      new ZookeeperContainer("confluentinc/cp-zookeeper:latest")
          .withNetwork(network)
          .withNetworkAliases("zookeeper")
          .withLogConsumer(new Slf4jLogConsumer(log));

  @ClassRule
  public static ConfluentKafkaContainer
      cp =
      new ConfluentKafkaContainer(CONFLUENTINC_CP_KAFKA_LATEST, "zookeeper")
          .withNetwork(network)
          .withNetworkAliases("kafka")
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
