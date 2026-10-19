package com.moneytracker.users;

import java.time.Instant;
import java.util.UUID;

/** Datos del usuario expuestos por la API (nunca el hash de la contraseña). */
public record UserResponse(
    UUID id, String email, String baseCurrency, String locale, Instant createdAt) {

  public static UserResponse of(User user) {
    return new UserResponse(
        user.getId(),
        user.getEmail(),
        user.getBaseCurrency(),
        user.getLocale(),
        user.getCreatedAt());
  }
}
