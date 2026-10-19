package com.moneytracker;

import static org.assertj.core.api.Assertions.assertThat;

import com.moneytracker.support.ApiClient.Response;
import com.moneytracker.support.IntegrationTest;
import java.util.Map;
import org.junit.jupiter.api.Test;

class PlatformIT extends IntegrationTest {

  @Test
  void healthIsPublicAndHidesDetails() {
    Response res = api.get("/actuator/health", null);
    assertThat(res.status()).isEqualTo(200);
    assertThat(res.body()).doesNotContain("components");
  }

  @Test
  void protectedEndpointsRequireTokenWithStandardError() {
    Response res = api.get("/v1/me", null);
    assertThat(res.status()).isEqualTo(401);
    assertThat((String) res.json("$.code")).isEqualTo("UNAUTHORIZED");
    assertThat((String) res.json("$.requestId")).isNotBlank();
  }

  @Test
  void invalidTokenIsRejected() {
    Response res = api.get("/v1/me", "not-a-jwt");
    assertThat(res.status()).isEqualTo(401);
  }

  @Test
  void requestIdIsPropagatedWhenValid() {
    Response res =
        api.send("GET", "/v1/me", null, null, Map.of("X-Request-Id", "test-request-1234"));
    assertThat(res.header("X-Request-Id")).isEqualTo("test-request-1234");
    assertThat((String) res.json("$.requestId")).isEqualTo("test-request-1234");
  }

  @Test
  void invalidRequestIdIsReplaced() {
    Response res = api.send("GET", "/v1/me", null, null, Map.of("X-Request-Id", "bad id!"));
    assertThat(res.header("X-Request-Id")).isNotEqualTo("bad id!").isNotBlank();
  }

  @Test
  void securityHeadersArePresent() {
    Response res = api.get("/actuator/health", null);
    assertThat(res.header("X-Content-Type-Options")).isEqualTo("nosniff");
    assertThat(res.header("X-Frame-Options")).isEqualTo("DENY");
    assertThat(res.header("Content-Security-Policy")).contains("default-src 'none'");
    assertThat(res.header("Referrer-Policy")).isEqualTo("no-referrer");
  }

  @Test
  void oversizedBodiesAreRejected() {
    String big = "{\"email\":\"" + "a".repeat(70_000) + "\"}";
    Response res = api.post("/v1/auth/login", big, null);
    assertThat(res.status()).isEqualTo(413);
    assertThat((String) res.json("$.code")).isEqualTo("PAYLOAD_TOO_LARGE");
  }
}
