package com.moneytracker.core.security;

import java.util.UUID;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

/**
 * Identidad del usuario autenticado. El propietario de un recurso se obtiene siempre del token,
 * nunca de un campo de la petición.
 */
public final class CurrentUser {

  private CurrentUser() {}

  public static UUID id() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null || !(auth.getPrincipal() instanceof Jwt jwt)) {
      throw new IllegalStateException("No hay usuario autenticado");
    }
    return UUID.fromString(jwt.getSubject());
  }
}
