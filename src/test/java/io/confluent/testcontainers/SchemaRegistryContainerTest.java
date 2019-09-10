package io.confluent.testcontainers;

import org.junit.BeforeClass;
import org.junit.Test;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.Wait;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SchemaRegistryContainerTest {

  private static KafkaContainer kafka = new KafkaContainer("5.2.1");

  @BeforeClass
  public static void setUp() {
    kafka.start();
  }

  @Test
  public void shouldStartWithKafka() {
    try (SchemaRegistryContainer schemaRegistryContainer = new SchemaRegistryContainer("5.2.1")) {
      schemaRegistryContainer.withKafka(kafka)
          .withLogConsumer(new Slf4jLogConsumer(log))
          .waitingFor(Wait.forHttp("/subjects")
                          .forStatusCode(200))
          .start();
    }
  }
}