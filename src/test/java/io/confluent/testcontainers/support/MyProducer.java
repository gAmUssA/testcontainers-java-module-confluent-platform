package io.confluent.testcontainers.support;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public interface MyProducer {

  @NotNull Properties createProducerProperties(String bootstrapServer);

  void createProducer(String bootstrapServer);

  default void createTopics(Properties envProps) {

    Map<String, Object> config = new HashMap<>();
    config.put("bootstrap.servers", envProps.getProperty("bootstrap.servers"));
    AdminClient client = AdminClient.create(config);

    List<NewTopic> topics = new ArrayList<>();
    topics.add(new NewTopic(
        envProps.getProperty("input.topic.name"),
        Integer.parseInt(envProps.getProperty("input.topic.partitions")),
        Short.parseShort(envProps.getProperty("input.topic.replication.factor"))));
    
    client.createTopics(topics);
    client.close();
  }
}
