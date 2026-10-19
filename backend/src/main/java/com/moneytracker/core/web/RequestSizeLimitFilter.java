package com.moneytracker.core.web;

import com.moneytracker.core.config.AppProperties;
import com.moneytracker.core.error.ErrorResponseWriter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/** Rechaza cuerpos de petición mayores que {@code app.http.max-request-bytes}. */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class RequestSizeLimitFilter extends OncePerRequestFilter {

  private final long maxBytes;

  public RequestSizeLimitFilter(AppProperties properties) {
    this.maxBytes = properties.http().maxRequestBytes();
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain chain)
      throws ServletException, IOException {
    boolean chunked = "chunked".equalsIgnoreCase(request.getHeader("Transfer-Encoding"));
    if (request.getContentLengthLong() > maxBytes || chunked) {
      ErrorResponseWriter.write(
          response, 413, "PAYLOAD_TOO_LARGE", "La petición supera el tamaño permitido");
      return;
    }
    chain.doFilter(request, response);
  }
}
