package com.moneytracker.support;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

/** Base de los tests de integración: servidor real en un puerto aleatorio y PostgreSQL. */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestcontainersConfig.class)
@ActiveProfiles("test")
public abstract class IntegrationTest {

  @Value("${local.server.port}")
  private int port;

  protected ApiClient api;

  @BeforeEach
  void setUpClient() {
    api = new ApiClient(port);
  }
}
