package com.moneytracker.auth;

import com.moneytracker.auth.AuthDtos.AuthResponse;
import com.moneytracker.auth.AuthDtos.RegisterRequest;
import com.moneytracker.core.error.ApiException;
import com.moneytracker.currencies.CurrencyService;
import com.moneytracker.users.Locales;
import com.moneytracker.users.User;
import com.moneytracker.users.UserRepository;
import com.moneytracker.users.UserResponse;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Registro, login, refresco y cierre de sesión. Los logs nunca incluyen emails ni contraseñas.
 */
@Service
public class AuthService {

  private static final Logger log = LoggerFactory.getLogger(AuthService.class);
  private static final String DEFAULT_CURRENCY = "EUR";

  private final UserRepository users;
  private final RefreshTokenRepository refreshTokens;
  private final PasswordEncoder passwordEncoder;
  private final TokenService tokens;
  private final CurrencyService currencies;
  private final List<UserRegistrationListener> registrationListeners;
  private final String dummyHash;

  public AuthService(
      UserRepository users,
      RefreshTokenRepository refreshTokens,
      PasswordEncoder passwordEncoder,
      TokenService tokens,
      CurrencyService currencies,
      List<UserRegistrationListener> registrationListeners) {
    this.users = users;
    this.refreshTokens = refreshTokens;
    this.passwordEncoder = passwordEncoder;
    this.tokens = tokens;
    this.currencies = currencies;
    this.registrationListeners = registrationListeners;
    // Hash de referencia para igualar el tiempo de respuesta cuando el usuario no existe.
    this.dummyHash = passwordEncoder.encode(UUID.randomUUID().toString());
  }

  @Transactional
  public AuthResponse register(RegisterRequest request) {
    String email = normalizeEmail(request.email());
    PasswordPolicy.check(request.password(), email);
    String baseCurrency =
        currencies.requireSupported(
            request.baseCurrency() == null ? DEFAULT_CURRENCY : request.baseCurrency());
    String locale = request.locale() == null ? Locales.DEFAULT : request.locale();
    if (users.existsByEmail(email)) {
      // Pendiente: verificación por email para no revelar cuentas existentes en el registro.
      throw ApiException.conflict("EMAIL_UNAVAILABLE", "No se puede registrar con ese email");
    }
    User user =
        users.save(new User(email, passwordEncoder.encode(request.password()), baseCurrency, locale));
    registrationListeners.forEach(l -> l.onUserRegistered(user));
    log.info("user_registered userId={}", user.getId());
    return issueTokens(user, Instant.now());
  }

  @Transactional
  public AuthResponse login(String rawEmail, String password) {
    Optional<User> user = users.findByEmail(normalizeEmail(rawEmail));
    String hash = user.map(User::getPasswordHash).orElse(dummyHash);
    boolean matches = passwordEncoder.matches(password, hash);
    if (user.isEmpty() || !matches) {
      log.warn("login_failed");
      throw invalidCredentials();
    }
    log.info("login_succeeded userId={}", user.get().getId());
    return issueTokens(user.get(), Instant.now());
  }

  /**
   * Rota el refresh token: el usado queda revocado. Si se presenta un token ya revocado (posible
   * robo), se revocan todas las sesiones del usuario.
   */
  @Transactional(noRollbackFor = ApiException.class)
  public AuthResponse refresh(String rawToken) {
    Instant now = Instant.now();
    RefreshToken stored =
        refreshTokens
            .findByTokenHash(TokenService.sha256(rawToken))
            .orElseThrow(AuthService::invalidSession);
    if (stored.isRevoked()) {
      refreshTokens.revokeAllForUser(stored.getUserId(), now);
      log.warn("refresh_token_reuse userId={}", stored.getUserId());
      throw invalidSession();
    }
    if (stored.isExpired(now)) {
      throw invalidSession();
    }
    stored.revoke(now);
    User user = users.findById(stored.getUserId()).orElseThrow(AuthService::invalidSession);
    return issueTokens(user, now);
  }

  @Transactional
  public void logout(String rawToken) {
    refreshTokens
        .findByTokenHash(TokenService.sha256(rawToken))
        .ifPresent(token -> token.revoke(Instant.now()));
  }

  private AuthResponse issueTokens(User user, Instant now) {
    String refresh = tokens.newRefreshToken();
    refreshTokens.save(
        new RefreshToken(user.getId(), TokenService.sha256(refresh), tokens.refreshTokenExpiry(now)));
    return new AuthResponse(
        tokens.accessToken(user.getId(), now),
        "Bearer",
        tokens.accessTokenTtlSeconds(),
        refresh,
        UserResponse.of(user));
  }

  private static String normalizeEmail(String email) {
    return email.trim().toLowerCase(Locale.ROOT);
  }

  private static ApiException invalidCredentials() {
    return new ApiException(
        HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS", "Email o contraseña incorrectos");
  }

  private static ApiException invalidSession() {
    return new ApiException(
        HttpStatus.UNAUTHORIZED, "INVALID_SESSION", "La sesión no es válida o ha caducado");
  }
}
