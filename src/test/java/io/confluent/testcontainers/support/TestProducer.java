package io.confluent.testcontainers.support;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.jetbrains.annotations.NotNull;

import java.util.Properties;
import java.util.concurrent.ExecutionException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TestProducer implements MyProducer {

  @Override
  public void createProducer(String bootstrapServer) {

    Properties props = createProducerProperties(bootstrapServer);
    this.createTopics(props);

    KafkaProducer<String, String> producer = new KafkaProducer<>(props);

    long numberOfEvents = 5;
    for (int i = 0; i < numberOfEvents; i++) {
      String key = "testContainers";
      String value = "AreAwesome";
      ProducerRecord<String, String> record = new ProducerRecord<>(
          (String) props.get("input.topic.name"), key, value);
      try {
        producer.send(record).get();

      } catch (InterruptedException | ExecutionException e) {
        e.printStackTrace();
      }
      log.info("key = {}, value = {}", key, value);
    }

    producer.close();
  }

  @Override
  @NotNull
  public Properties createProducerProperties(final String bootstrapServer) {
    Properties props = new Properties();
    props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServer);
    props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
    props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
    props.put(ProducerConfig.INTERCEPTOR_CLASSES_CONFIG, LoggingProducerInterceptor.class.getName());

    props.put("input.topic.name", "hello_world_topic");
    
    // simulate the properties retrieved from *.properties file (hence String values)
    props.put("input.topic.partitions", "1");
    props.put("input.topic.replication.factor", "1");

    return props;
  }

  /*
    Test me :)
   */
  public static void main(String[] args) {
    TestProducer helloProducer = new TestProducer();
    helloProducer.createProducer("localhost:29092");
  }
}
