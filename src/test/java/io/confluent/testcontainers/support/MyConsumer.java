package io.confluent.testcontainers.support;

import org.jetbrains.annotations.NotNull;

import java.util.Properties;

public interface MyConsumer {

  @NotNull Properties createConsumerProperties(String bootstrapServer);

  void consume();

}
