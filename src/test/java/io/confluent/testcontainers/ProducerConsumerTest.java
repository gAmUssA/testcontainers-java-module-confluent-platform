package io.confluent.testcontainers;

import static io.confluent.testcontainers.Constants.KAFKA_TEST_IMAGE;
import static org.testcontainers.utility.DockerImageName.parse;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Test;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;

import java.util.Collection;

import io.confluent.testcontainers.support.TestConsumer;
import io.confluent.testcontainers.support.TestProducer;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ProducerConsumerTest {

  @ClassRule
  public static KafkaContainer kafka = new KafkaContainer(parse(KAFKA_TEST_IMAGE))
      .withLogConsumer(new Slf4jLogConsumer(log));

  @Test
  public void testProducerConsumer() {
    TestProducer helloProducer = new TestProducer();
    helloProducer.createProducer(kafka.getBootstrapServers());

    TestConsumer helloConsumer = new TestConsumer(kafka.getBootstrapServers());
    helloConsumer.consume();
    Collection<ConsumerRecord<String, String>> messages = helloConsumer.getReceivedRecords();

    Assert.assertEquals("message consumed", messages.size(), 5);
    messages.forEach(stringStringConsumerRecord -> {
      Assert.assertEquals(stringStringConsumerRecord.key(), "testContainers");
      Assert.assertEquals(stringStringConsumerRecord.value(), "AreAwesome");
    });
  }

}
