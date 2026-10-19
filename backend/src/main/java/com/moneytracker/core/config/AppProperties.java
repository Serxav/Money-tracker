package com.moneytracker.core.config;

import java.time.Duration;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** Configuración propia de la aplicación (prefijo {@code app}). */
@ConfigurationProperties(prefix = "app")
public record AppProperties(Jwt jwt, RateLimit rateLimit, Currencies currencies, Http http) {

  public record Jwt(String secret, String issuer, Duration accessTokenTtl, Duration refreshTokenTtl) {}

  /** Peticiones por minuto y por IP. */
  public record RateLimit(int authPerMinute, int apiPerMinute) {}

  public record Currencies(List<String> supported) {}

  public record Http(long maxRequestBytes) {}
}
