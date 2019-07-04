package io.confluent.testcontainers;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Test;
import org.testcontainers.containers.DockerComposeContainer;

import java.io.File;
import java.util.Collection;

import io.confluent.testcontainers.support.HelloConsumer;
import io.confluent.testcontainers.support.HelloProducer;

public class KafkaSingleNodeComposeTest {

  @ClassRule
  public static DockerComposeContainer environment =
      new DockerComposeContainer(new File("src/test/resources/kafka-single-node-compose.yml"))
          .withExposedService("kafka_1", 29092)
          .withExposedService("zookeeper_1", 32181);


  @Test
  public void testProducerConsumer() {
    String host = environment.getServiceHost("kafka_1",29092);
    Integer port = environment.getServicePort("kafka_1", 29092);

    HelloProducer helloProducer = new HelloProducer();
    helloProducer.createProducer(host+":"+port);

    HelloConsumer helloConsumer = new HelloConsumer(host+":"+port);
    helloConsumer.consume();
    Collection<ConsumerRecord> messages = helloConsumer.getReceivedRecords();

    Assert.assertEquals("message consumed", messages.size(), 5);
    messages.forEach(stringStringConsumerRecord -> {
      Assert.assertEquals(stringStringConsumerRecord.key(), "testContainers");
      Assert.assertEquals(stringStringConsumerRecord.value(), "AreAwesome");
    });
  }

}
