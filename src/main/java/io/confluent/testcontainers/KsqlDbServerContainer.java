package io.confluent.testcontainers;

import io.confluent.testcontainers.ksqldb.AbstractKsqlServerContainer;

/**
 * This container wraps standalone ksqlDB container
 * To learn more about KSQL visit https://ksqldb.io
 *
 * @since 0.1
 */
public class KsqlDbServerContainer extends AbstractKsqlServerContainer {

  // confluentinc/ksqldb-server:0.11.0
  public KsqlDbServerContainer(final String version) {
    super("confluentinc/ksqldb-server:" + version);
    withExposedPorts(KSQL_PORT);
  }
}
