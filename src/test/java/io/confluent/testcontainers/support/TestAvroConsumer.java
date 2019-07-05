package io.confluent.testcontainers.support;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Properties;
import java.util.Random;

import io.confluent.developer.Movie;
import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig;
import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import static java.util.Collections.singletonList;

@Slf4j
public class TestAvroConsumer {

  private final String schemaRegistryUrl;
  private final KafkaConsumer<String, Movie> consumer;

  @Getter
  private Collection<ConsumerRecord<String, Movie>> receivedRecords;

  public TestAvroConsumer(final String bootstrapServer, final String schemaRegistryUrl) {
    this.schemaRegistryUrl = schemaRegistryUrl;
    this.consumer = new KafkaConsumer<>(createConsumerProperties(bootstrapServer));
    receivedRecords = new ArrayList<>();
  }

  public void consume() {
    consumer.subscribe(singletonList("hello_avro_topic"));
    ConsumerRecords<String, Movie> records = consumer.poll(Duration.ofMillis(10000));
    for (ConsumerRecord<String, Movie> record : records) {
      log.info("offset = {}, key = {}, value = {}", record.offset(), record.key(), record.value());
      this.receivedRecords.add(record);
    }
  }
  
  @NotNull
  public Properties createConsumerProperties(String bootstrapServer) {
    Properties props = new Properties();
    props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServer);
    props.put(ConsumerConfig.GROUP_ID_CONFIG, "testgroup" + new Random().nextInt());
    props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");
    props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "1000");
    props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
    props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, "30000");
    props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
    props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, KafkaAvroDeserializer.class);
    props.put(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, this.schemaRegistryUrl);
    props.put(KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG, true);

    return props;
  }
}
