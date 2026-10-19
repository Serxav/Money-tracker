package com.moneytracker.support;

import java.util.UUID;
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

  protected static final String PASSWORD = "Una-clave-larga-2026";

  protected static String uniqueEmail() {
    return "user-" + UUID.randomUUID() + "@example.com";
  }

  /** Registra un usuario nuevo y devuelve su sesión. */
  protected Session register() {
    String email = uniqueEmail();
    ApiClient.Response res =
        api.post(
            "/v1/auth/register",
            "{\"email\":\"" + email + "\",\"password\":\"" + PASSWORD + "\"}",
            null);
    if (res.status() != 201) {
      throw new IllegalStateException("Registro fallido: " + res.status() + " " + res.body());
    }
    return new Session(
        email, res.json("$.accessToken"), res.json("$.refreshToken"), res.json("$.user.id"));
  }

  protected record Session(String email, String accessToken, String refreshToken, String userId) {}
}
