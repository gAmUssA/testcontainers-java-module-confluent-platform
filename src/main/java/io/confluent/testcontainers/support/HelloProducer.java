package io.confluent.testcontainers.support;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;

public class HelloProducer {

  public void createProducer(String bootstrapServer) {
    long numberOfEvents = 5;

    Properties props = new Properties();
    props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServer);
    props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
    props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);

    KafkaProducer<String, String> producer = new KafkaProducer<>(
        props);

    for (int i = 0; i < numberOfEvents; i++) {
      String key = "testContainers";
      String value = "AreAwesome";
      ProducerRecord<String, String> record = new ProducerRecord<>(
          "hello_world_topic", key, value);
      producer.send(record);
      System.out.printf("key = %s, value = %s\n", key, value);
    }

    producer.close();
  }

  /*
    Test me :)
   */
  public static void main(String[] args) {
    HelloProducer helloProducer = new HelloProducer();
    helloProducer.createProducer("localhost:9092");
  }
}
