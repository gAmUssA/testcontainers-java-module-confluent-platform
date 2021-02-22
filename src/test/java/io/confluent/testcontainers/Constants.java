package io.confluent.testcontainers;

public final class Constants {

  private Constants() {
  }

  public static final String KAFKA_IMAGE_TAG = "5.5.3";
  public static final String KAFKA_TEST_IMAGE = "confluentinc/cp-kafka:" + KAFKA_IMAGE_TAG;
  public static final String SCHEMA_REGISTRY_TEST_IMAGE = "confluentinc/cp-schema-registry:" + KAFKA_IMAGE_TAG;

}
