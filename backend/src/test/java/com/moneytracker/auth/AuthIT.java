package com.moneytracker.auth;

import static org.assertj.core.api.Assertions.assertThat;

import com.moneytracker.support.ApiClient.Response;
import com.moneytracker.support.IntegrationTest;
import org.junit.jupiter.api.Test;

class AuthIT extends IntegrationTest {

  @Test
  void registerReturnsTokensAndNeverThePasswordHash() {
    String email = uniqueEmail();
    Response res =
        api.post(
            "/v1/auth/register",
            "{\"email\":\"" + email.toUpperCase() + "\",\"password\":\"" + PASSWORD + "\",\"baseCurrency\":\"USD\",\"locale\":\"ca\"}",
            null);
    assertThat(res.status()).isEqualTo(201);
    assertThat((String) res.json("$.accessToken")).isNotBlank();
    assertThat((String) res.json("$.refreshToken")).isNotBlank();
    assertThat((String) res.json("$.tokenType")).isEqualTo("Bearer");
    assertThat((String) res.json("$.user.email")).isEqualTo(email);
    assertThat((String) res.json("$.user.baseCurrency")).isEqualTo("USD");
    assertThat((String) res.json("$.user.locale")).isEqualTo("ca");
    assertThat(res.body()).doesNotContainIgnoringCase("password").doesNotContain("argon");
  }

  @Test
  void duplicateEmailIsRejected() {
    Session s = register();
    Response res =
        api.post(
            "/v1/auth/register",
            "{\"email\":\"" + s.email() + "\",\"password\":\"" + PASSWORD + "\"}",
            null);
    assertThat(res.status()).isEqualTo(409);
    assertThat((String) res.json("$.code")).isEqualTo("EMAIL_UNAVAILABLE");
  }

  @Test
  void weakPasswordsAreRejected() {
    for (String weak : new String[] {"corta", "1234567890", "aaaaaaaaaaaa"}) {
      Response res =
          api.post(
              "/v1/auth/register",
              "{\"email\":\"" + uniqueEmail() + "\",\"password\":\"" + weak + "\"}",
              null);
      assertThat(res.status()).as(weak).isEqualTo(400);
      assertThat((String) res.json("$.code")).isEqualTo("WEAK_PASSWORD");
    }
  }

  @Test
  void unsupportedCurrencyIsRejected() {
    Response res =
        api.post(
            "/v1/auth/register",
            "{\"email\":\"" + uniqueEmail() + "\",\"password\":\"" + PASSWORD + "\",\"baseCurrency\":\"XXX\"}",
            null);
    assertThat(res.status()).isEqualTo(422);
    assertThat((String) res.json("$.code")).isEqualTo("UNSUPPORTED_CURRENCY");
  }

  @Test
  void unknownFieldsAreRejected() {
    Response res =
        api.post(
            "/v1/auth/register",
            "{\"email\":\"" + uniqueEmail() + "\",\"password\":\"" + PASSWORD + "\",\"role\":\"ADMIN\"}",
            null);
    assertThat(res.status()).isEqualTo(400);
    assertThat((String) res.json("$.code")).isEqualTo("MALFORMED_REQUEST");
  }

  @Test
  void invalidEmailGivesFieldDetails() {
    Response res =
        api.post("/v1/auth/register", "{\"email\":\"no-es-email\",\"password\":\"" + PASSWORD + "\"}", null);
    assertThat(res.status()).isEqualTo(400);
    assertThat((String) res.json("$.code")).isEqualTo("VALIDATION_ERROR");
    assertThat((String) res.json("$.details[0].field")).isEqualTo("email");
  }

  @Test
  void loginWithWrongPasswordOrUnknownEmailGivesTheSameError() {
    Session s = register();
    Response wrongPassword =
        api.post("/v1/auth/login", "{\"email\":\"" + s.email() + "\",\"password\":\"otra-clave-mala\"}", null);
    Response unknownEmail =
        api.post("/v1/auth/login", "{\"email\":\"" + uniqueEmail() + "\",\"password\":\"otra-clave-mala\"}", null);
    assertThat(wrongPassword.status()).isEqualTo(401);
    assertThat(unknownEmail.status()).isEqualTo(401);
    assertThat((String) wrongPassword.json("$.code")).isEqualTo("INVALID_CREDENTIALS");
    assertThat((String) unknownEmail.json("$.message"))
        .isEqualTo(wrongPassword.json("$.message"));
  }

  @Test
  void loginIsCaseInsensitiveOnEmail() {
    Session s = register();
    Response res =
        api.post("/v1/auth/login", "{\"email\":\"" + s.email().toUpperCase() + "\",\"password\":\"" + PASSWORD + "\"}", null);
    assertThat(res.status()).isEqualTo(200);
    assertThat(api.get("/v1/me", (String) res.json("$.accessToken")).status()).isEqualTo(200);
  }

  @Test
  void refreshRotatesAndReuseRevokesAllSessions() {
    Session s = register();
    Response first = api.post("/v1/auth/refresh", "{\"refreshToken\":\"" + s.refreshToken() + "\"}", null);
    assertThat(first.status()).isEqualTo(200);
    String rotated = first.json("$.refreshToken");
    assertThat(rotated).isNotEqualTo(s.refreshToken());

    // Reutilizar el token ya usado: se considera robo y se revocan todas las sesiones.
    Response reuse = api.post("/v1/auth/refresh", "{\"refreshToken\":\"" + s.refreshToken() + "\"}", null);
    assertThat(reuse.status()).isEqualTo(401);
    assertThat((String) reuse.json("$.code")).isEqualTo("INVALID_SESSION");
    Response afterReuse = api.post("/v1/auth/refresh", "{\"refreshToken\":\"" + rotated + "\"}", null);
    assertThat(afterReuse.status()).isEqualTo(401);
  }

  @Test
  void logoutRevokesTheRefreshToken() {
    Session s = register();
    assertThat(api.post("/v1/auth/logout", "{\"refreshToken\":\"" + s.refreshToken() + "\"}", null).status())
        .isEqualTo(204);
    assertThat(api.post("/v1/auth/refresh", "{\"refreshToken\":\"" + s.refreshToken() + "\"}", null).status())
        .isEqualTo(401);
  }

  @Test
  void meCanBeReadAndUpdated() {
    Session s = register();
    Response me = api.get("/v1/me", s.accessToken());
    assertThat(me.status()).isEqualTo(200);
    assertThat((String) me.json("$.email")).isEqualTo(s.email());
    assertThat((String) me.json("$.baseCurrency")).isEqualTo("EUR");

    Response updated = api.patch("/v1/me", "{\"baseCurrency\":\"JPY\",\"locale\":\"en\"}", s.accessToken());
    assertThat(updated.status()).isEqualTo(200);
    assertThat((String) updated.json("$.baseCurrency")).isEqualTo("JPY");
    assertThat((String) updated.json("$.locale")).isEqualTo("en");
  }

  @Test
  void emailCannotBeChangedThroughMe() {
    Session s = register();
    Response res = api.patch("/v1/me", "{\"email\":\"otro@example.com\"}", s.accessToken());
    assertThat(res.status()).isEqualTo(400);
  }

  @Test
  void deleteAccountRequiresPasswordAndRemovesSessions() {
    Session s = register();
    assertThat(api.delete("/v1/me", "{\"password\":\"incorrecta-123\"}", s.accessToken()).status())
        .isEqualTo(403);
    assertThat(api.delete("/v1/me", "{\"password\":\"" + PASSWORD + "\"}", s.accessToken()).status())
        .isEqualTo(204);
    assertThat(api.post("/v1/auth/refresh", "{\"refreshToken\":\"" + s.refreshToken() + "\"}", null).status())
        .isEqualTo(401);
    assertThat(
            api.post("/v1/auth/login", "{\"email\":\"" + s.email() + "\",\"password\":\"" + PASSWORD + "\"}", null)
                .status())
        .isEqualTo(401);
  }

  @Test
  void currenciesListIncludesFractionDigits() {
    Session s = register();
    Response res = api.get("/v1/currencies", s.accessToken());
    assertThat(res.status()).isEqualTo(200);
    assertThat(digits(res, "JPY")).isZero();
    assertThat(digits(res, "EUR")).isEqualTo(2);
    assertThat(digits(res, "KWD")).isEqualTo(3);
  }

  private static int digits(Response res, String code) {
    java.util.List<Integer> values = res.json("$[?(@.code=='" + code + "')].fractionDigits");
    return values.get(0);
  }
}
