package io.confluent.testcontainers;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.output.Slf4jLogConsumer;

import java.util.Collection;

import io.confluent.developer.Movie;
import io.confluent.testcontainers.support.TestAvroConsumer;
import io.confluent.testcontainers.support.TestAvroProducer;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AvroProducerConsumerTest {

  public static KafkaContainer kafka = new KafkaContainer("5.5.1")
      .withLogConsumer(new Slf4jLogConsumer(log))
      .withNetwork(Network.newNetwork());

  public static SchemaRegistryContainer schemaRegistry = new SchemaRegistryContainer("5.5.1")
      .withLogConsumer(new Slf4jLogConsumer(log));

  @BeforeClass
  public static void prep() {
    kafka.start();
    schemaRegistry.withKafka(kafka).start();
  }

  @Test
  public void testProducerConsumer() {
    TestAvroProducer helloProducer = new TestAvroProducer(schemaRegistry.getSchemaRegistryUrl());
    helloProducer.createProducer(kafka.getBootstrapServers());

    TestAvroConsumer
        helloConsumer =
        new TestAvroConsumer(kafka.getBootstrapServers(), schemaRegistry.getSchemaRegistryUrl());
    helloConsumer.consume();

    Collection<ConsumerRecord<String, Movie>> messages = helloConsumer.getReceivedRecords();

    Assert.assertEquals("message consumed", messages.size(), 5);
    messages.forEach(stringStringConsumerRecord -> {
      Assert.assertEquals(stringStringConsumerRecord.key(), "Lethal Weapon");
      Assert.assertEquals(stringStringConsumerRecord.value().getTitle(), "Lethal Weapon");
    });
  }

}
