package io.confluent.testcontainers;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.Wait;

import java.io.IOException;

import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static org.junit.Assert.assertEquals;

@Slf4j
public class KsqlServerContainerTest {

  private static KafkaContainer kafka = new KafkaContainer("5.2.1");
  private OkHttpClient client;

  @BeforeClass
  public static void setUpClass() {
    kafka.start();
  }

  @Before
  public void setUp() {
    client = new OkHttpClient();
  }

  @Test
  public void shouldStartWithKafka() throws IOException {
    try (KsqlServerContainer ksqlServer = new KsqlServerContainer("5.2.1")) {
      ksqlServer.withKafka(kafka)
          .withLogConsumer(new Slf4jLogConsumer(log))
          .waitingFor(Wait.forHttp("/info").forStatusCode(200))
          .start();

      Request request = new Request.Builder().url(ksqlServer.getTarget() + "/info")
          .get()
          .build();
      final Response response = client.newCall(request).execute();
      final String string = response.body().string();
      ObjectMapper mapper = new ObjectMapper();
      
      // https://github.com/confluentinc/ksql/blob/master/docs/developer-guide/api.rst#get-the-status-of-a-ksql-server
      final String expected = mapper
          .readTree(string)
          .get("KsqlServerInfo")
          .get("version")
          .asText();
      assertEquals(expected, "5.2.1");
    }
  }
}