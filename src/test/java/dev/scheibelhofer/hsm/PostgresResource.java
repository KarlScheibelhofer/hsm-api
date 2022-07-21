package dev.scheibelhofer.hsm;

import java.util.Collections;
import java.util.Map;

import org.testcontainers.containers.PostgreSQLContainer;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;

public class PostgresResource implements
        QuarkusTestResourceLifecycleManager {

  static PostgreSQLContainer<?> db =
      new PostgreSQLContainer<>("postgres:14") 
        .withDatabaseName("hsm_db")
        .withUsername("hsm")
        .withPassword("hsm");

  @Override
  public Map<String, String> start() { 
    db.start();
    return Collections.singletonMap(
        "quarkus.datasource.jdbc.url", db.getJdbcUrl()
    );
  }

  @Override
  public void stop() { 
    db.stop();
  }
}