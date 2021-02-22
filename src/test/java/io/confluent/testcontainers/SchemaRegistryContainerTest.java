package io.confluent.testcontainers;

import static io.confluent.testcontainers.Constants.KAFKA_IMAGE_TAG;
import static io.confluent.testcontainers.Constants.KAFKA_TEST_IMAGE;
import static org.testcontainers.utility.DockerImageName.parse;

import org.junit.BeforeClass;
import org.junit.Test;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.Wait;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SchemaRegistryContainerTest {

  private static final KafkaContainer kafka = new KafkaContainer(parse(KAFKA_TEST_IMAGE))
      .withNetwork(Network.newNetwork());



  @BeforeClass
  public static void setUp() {
    kafka.start();
  }

  @Test
  public void shouldStartWithKafka() {
    try (SchemaRegistryContainer schemaRegistryContainer = new SchemaRegistryContainer(KAFKA_IMAGE_TAG)) {
      schemaRegistryContainer.withKafka(kafka)
          .withLogConsumer(new Slf4jLogConsumer(log))
          .waitingFor(Wait.forHttp("/subjects")
                          .forStatusCode(200))
          .start();
    }
  }
}
