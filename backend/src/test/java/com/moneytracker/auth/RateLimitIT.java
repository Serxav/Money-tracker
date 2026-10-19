package com.moneytracker.auth;

import static org.assertj.core.api.Assertions.assertThat;

import com.moneytracker.support.ApiClient.Response;
import com.moneytracker.support.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;

@TestPropertySource(properties = "app.rate-limit.auth-per-minute=3")
class RateLimitIT extends IntegrationTest {

  @Test
  void bruteForceOnLoginIsThrottled() {
    String body = "{\"email\":\"nadie@example.com\",\"password\":\"clave-incorrecta\"}";
    for (int i = 0; i < 3; i++) {
      assertThat(api.post("/v1/auth/login", body, null).status()).isEqualTo(401);
    }
    Response throttled = api.post("/v1/auth/login", body, null);
    assertThat(throttled.status()).isEqualTo(429);
    assertThat((String) throttled.json("$.code")).isEqualTo("RATE_LIMITED");
    assertThat(throttled.header("Retry-After")).isEqualTo("60");
  }
}
