package com.moneytracker.core.web;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.moneytracker.core.config.AppProperties;
import com.moneytracker.core.error.ErrorResponseWriter;
import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Rate limiting por IP: un límite estricto para los endpoints de autenticación y otro general
 * para el resto de la API. En memoria: válido para una sola instancia (ver README del backend).
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 2)
public class RateLimitFilter extends OncePerRequestFilter {

  private static final Logger log = LoggerFactory.getLogger(RateLimitFilter.class);

  private final int authPerMinute;
  private final int apiPerMinute;
  private final Cache<String, Bucket> buckets =
      Caffeine.newBuilder().expireAfterAccess(Duration.ofMinutes(10)).maximumSize(100_000).build();

  public RateLimitFilter(AppProperties properties) {
    this.authPerMinute = properties.rateLimit().authPerMinute();
    this.apiPerMinute = properties.rateLimit().apiPerMinute();
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain chain)
      throws ServletException, IOException {
    String path = request.getRequestURI();
    if (!path.startsWith("/v1/")) {
      chain.doFilter(request, response);
      return;
    }
    boolean auth = path.startsWith("/v1/auth/");
    String key = (auth ? "auth:" : "api:") + request.getRemoteAddr();
    int perMinute = auth ? authPerMinute : apiPerMinute;
    Bucket bucket = buckets.get(key, k -> newBucket(perMinute));
    if (bucket.tryConsume(1)) {
      chain.doFilter(request, response);
      return;
    }
    log.warn("rate_limit_exceeded scope={}", auth ? "auth" : "api");
    response.setHeader("Retry-After", "60");
    ErrorResponseWriter.write(
        response, 429, "RATE_LIMITED", "Demasiadas peticiones. Inténtalo más tarde");
  }

  private static Bucket newBucket(int perMinute) {
    return Bucket.builder()
        .addLimit(limit -> limit.capacity(perMinute).refillGreedy(perMinute, Duration.ofMinutes(1)))
        .build();
  }
}
