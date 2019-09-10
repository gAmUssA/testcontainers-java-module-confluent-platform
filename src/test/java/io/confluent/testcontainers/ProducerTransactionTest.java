package io.confluent.testcontainers;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.BeforeClass;
import org.junit.Test;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;

import java.util.Properties;

import lombok.extern.slf4j.Slf4j;

import static org.apache.kafka.clients.consumer.ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG;
import static org.apache.kafka.clients.producer.ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG;
import static org.apache.kafka.clients.producer.ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG;
import static org.apache.kafka.clients.producer.ProducerConfig.TRANSACTIONAL_ID_CONFIG;
import static org.apache.kafka.clients.producer.ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG;

@Slf4j
public class ProducerTransactionTest {

  public static KafkaContainer kafka = new KafkaContainer("5.3.0")
      .withLogConsumer(new Slf4jLogConsumer(log));

  @BeforeClass
  public static void prep() {
    // see https://cwiki.apache.org/confluence/display/KAFKA/KIP-98+-+Exactly+Once+Delivery+and+Transactional+Messaging#KIP-98-ExactlyOnceDeliveryandTransactionalMessaging-NewConfigurations
    kafka.addEnv("KAFKA_TRANSACTION_STATE_LOG_REPLICATION_FACTOR", "1");
    kafka.addEnv("KAFKA_TRANSACTION_STATE_LOG_MIN_ISR", "1");
    kafka.start();
  }

  @Test
  public void testIt() {
    Properties props = new Properties();
    props.put(BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers());
    props.put(KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
    props.put(VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
    props.put(ENABLE_IDEMPOTENCE_CONFIG, "true");
    props.put(TRANSACTIONAL_ID_CONFIG, "prod-0");
    KafkaProducer<String, String> producer = new KafkaProducer<>(props);
    ProducerRecord<String, String> record = new ProducerRecord<>("something", "A message");
    producer.initTransactions();
    producer.beginTransaction();
    producer.send(record);
    producer.commitTransaction();
  }
}
