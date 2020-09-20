package io.confluent.testcontainers.ksqldb;

import io.confluent.testcontainers.ksqldb.AbstractKsqlServerContainer;

import static io.restassured.RestAssured.get;
import static org.hamcrest.Matchers.equalTo;

public abstract class AbstractKsqlServerContainerTest {

  protected void getKsqlServerInfoVersion(final AbstractKsqlServerContainer ksqlServer, final String version) {
    get(ksqlServer.getTarget() + "/info")
        .then()
        .body("KsqlServerInfo.version", equalTo(version));
  }
}
