package io.confluent.testcontainers.support;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.jetbrains.annotations.NotNull;

import java.util.Properties;
import java.util.concurrent.ExecutionException;

import io.confluent.developer.Movie;
import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig;
import io.confluent.kafka.serializers.KafkaAvroSerializer;

public class TestAvroProducer implements MyProducer {
  
  private final String schemaRegistryUrl;

  public TestAvroProducer(final String schemaRegistryUrl) {
    this.schemaRegistryUrl = schemaRegistryUrl;
  }
  
  @Override
  @NotNull
  public Properties createProducerProperties(final String bootstrapServer) {
    Properties props = new Properties();
    props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServer);
    props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
    props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class);

    // TODO: make named param and null check ¯\_(ツ)_/¯
    props.put(AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, schemaRegistryUrl);
    props.put(ProducerConfig.INTERCEPTOR_CLASSES_CONFIG, LoggingProducerInterceptor.class.getName());

    props.put("input.topic.name", "hello_avro_topic");
    props.put("input.topic.partitions", "1");
    props.put("input.topic.replication.factor", "1");

    return props;
  }

  @Override
  public void createProducer(final String bootstrapServer) {
    Properties props = createProducerProperties(bootstrapServer);
    this.createTopics(props);

    KafkaProducer<String, Movie> producer = new KafkaProducer<>(props);

    long numberOfEvents = 5;
    for (int i = 0; i < numberOfEvents; i++) {
      final Movie movie = Parser.parseMovie(LETHAL_WEAPON);
      String key = movie.getTitle();
      ProducerRecord<String, Movie> record = new ProducerRecord<>(
          (String) props.get("input.topic.name"), key, movie);
      try {
        producer.send(record).get();

      } catch (InterruptedException | ExecutionException e) {
        e.printStackTrace();
      }
      System.out.printf("key = %s, value = %s\n", key, movie);
    }

    producer.close();

  }

  private static String
      LETHAL_WEAPON =
      "362::Lethal Weapon::1987::105::United States::6.7::76999::Action|Comedy::Mel Gibson|Danny Glover|Gary Busey|Mitchell Ryan|Tom Atkins|Traci Wolfe|Darlene Love|Grand L. Bush|Lycia Naff|Bill Kalmenson|Jackie Swanson::Richard Donner::Michael Kamen|Eric Clapton::Shane Black::Stephen Goldblatt::Warner Bros.";
}


