package io.confluent.testcontainers;

import static io.confluent.testcontainers.Constants.KAFKA_IMAGE_TAG;
import static io.confluent.testcontainers.Constants.KAFKA_TEST_IMAGE;

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
import org.testcontainers.utility.DockerImageName;

@Slf4j
public class AvroProducerConsumerTest {

  public static KafkaContainer kafka = new KafkaContainer(DockerImageName.parse(KAFKA_TEST_IMAGE))
      .withLogConsumer(new Slf4jLogConsumer(log))
      .withNetwork(Network.newNetwork());

  public static SchemaRegistryContainer schemaRegistry = new SchemaRegistryContainer(KAFKA_IMAGE_TAG)
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
