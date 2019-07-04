package io.confluent.testcontainers;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Test;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;

import java.util.Collection;

import io.confluent.testcontainers.support.HelloConsumer;
import io.confluent.testcontainers.support.HelloProducer;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ProducerConsumerTest {

  @ClassRule
  public static KafkaContainer kafka = new KafkaContainer("5.2.1")
      .withLogConsumer(new Slf4jLogConsumer(log));
  
  @Test
  public void testProducerConsumer() {
    HelloProducer helloProducer = new HelloProducer();
    helloProducer.createProducer(kafka.getBootstrapServers());

    HelloConsumer helloConsumer = new HelloConsumer(kafka.getBootstrapServers());
    helloConsumer.consume();
    Collection<ConsumerRecord> messages = helloConsumer.getReceivedRecords();

    Assert.assertEquals("message consumed", messages.size(), 5);
    messages.forEach(stringStringConsumerRecord -> {
      Assert.assertEquals(stringStringConsumerRecord.key(), "testContainers");
      Assert.assertEquals(stringStringConsumerRecord.value(), "AreAwesome");
    });
  }

}
