package com.moneytracker.auth;

import com.moneytracker.core.error.ApiException;
import java.util.Locale;
import java.util.Set;

/**
 * Política mínima de contraseña: longitud y rechazo de contraseñas muy comunes. No obliga a usar
 * símbolos (ver SEGURIDAD_Y_ETICA.md §4).
 */
public final class PasswordPolicy {

  public static final int MIN_LENGTH = 10;
  public static final int MAX_LENGTH = 128;

  private static final Set<String> COMMON =
      Set.of(
          "1234567890", "12345678910", "0123456789", "qwertyuiop", "password123", "password1234",
          "contraseña", "contrasena", "contrasena1", "contraseña1", "123456789a", "abcdefghij",
          "iloveyou12", "aaaaaaaaaa", "1111111111", "0000000000", "qwerty1234", "1q2w3e4r5t",
          "passw0rd123", "administrator", "welcome123", "moneytracker", "money12345", "barcelona1",
          "madrid1234", "princesa12", "superman12", "football12", "letmein123", "changeme123");

  private PasswordPolicy() {}

  public static void check(String password, String email) {
    if (password == null
        || password.length() < MIN_LENGTH
        || password.length() > MAX_LENGTH) {
      throw ApiException.badRequest(
          "WEAK_PASSWORD",
          "La contraseña debe tener entre " + MIN_LENGTH + " y " + MAX_LENGTH + " caracteres");
    }
    String lower = password.toLowerCase(Locale.ROOT);
    String localPart = email == null ? "" : email.split("@")[0].toLowerCase(Locale.ROOT);
    if (COMMON.contains(lower)
        || lower.chars().distinct().count() < 4
        || (localPart.length() >= 4 && lower.contains(localPart))) {
      throw ApiException.badRequest(
          "WEAK_PASSWORD", "La contraseña es demasiado común o fácil de adivinar");
    }
  }
}
