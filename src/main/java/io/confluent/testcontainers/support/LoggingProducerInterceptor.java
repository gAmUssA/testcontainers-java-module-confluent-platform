package io.confluent.testcontainers.support;

import org.apache.kafka.clients.producer.ProducerInterceptor;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

import java.util.Map;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LoggingProducerInterceptor implements ProducerInterceptor {

  @Override
  public ProducerRecord onSend(ProducerRecord record) {
    log.info("Intercepted record: {}", record.toString());
    return record;
  }

  @Override
  public void onAcknowledgement(RecordMetadata metadata, Exception exception) {
    log.info("Broker ACKed with metadata {}", metadata);
  }

  @Override
  public void close() {

  }

  @Override
  public void configure(Map<String, ?> configs) {

  }
}
