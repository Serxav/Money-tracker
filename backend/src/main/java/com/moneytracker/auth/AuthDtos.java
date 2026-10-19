package com.moneytracker.auth;

import com.moneytracker.users.Locales;
import com.moneytracker.users.UserResponse;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Peticiones y respuestas de autenticación. */
public final class AuthDtos {

  private AuthDtos() {}

  public record RegisterRequest(
      @NotBlank @Email @Size(max = 254) String email,
      @NotBlank @Size(max = PasswordPolicy.MAX_LENGTH) String password,
      @Pattern(regexp = "^[A-Z]{3}$", message = "Código ISO 4217 de 3 letras") String baseCurrency,
      @Pattern(regexp = Locales.PATTERN, message = "Idioma no soportado") String locale) {}

  public record LoginRequest(
      @NotBlank @Size(max = 254) String email,
      @NotBlank @Size(max = PasswordPolicy.MAX_LENGTH) String password) {}

  public record RefreshRequest(@NotBlank @Size(max = 128) String refreshToken) {}

  public record AuthResponse(
      String accessToken,
      String tokenType,
      long expiresIn,
      String refreshToken,
      UserResponse user) {}
}
