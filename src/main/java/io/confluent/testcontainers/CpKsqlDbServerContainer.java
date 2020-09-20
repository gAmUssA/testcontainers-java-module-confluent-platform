package io.confluent.testcontainers;

import io.confluent.testcontainers.ksqldb.AbstractKsqlServerContainer;

/**
 * This container wraps ksqlDB container that is bundled with Confluent Platform
 * To learn more about KSQL visit https://www.confluent.io/product/ksql/
 *
 * @since 0.1
 */
public class CpKsqlDbServerContainer extends AbstractKsqlServerContainer {

  public CpKsqlDbServerContainer(String version) {
    super("confluentinc/cp-ksqldb-server:" + version);
    withExposedPorts(KSQL_PORT);
  }
}
