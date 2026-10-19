package com.moneytracker.users;

import com.moneytracker.core.error.ApiException;
import com.moneytracker.currencies.CurrencyService;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

  private static final Logger log = LoggerFactory.getLogger(UserService.class);

  private final UserRepository users;
  private final CurrencyService currencies;
  private final PasswordEncoder passwordEncoder;

  public UserService(
      UserRepository users, CurrencyService currencies, PasswordEncoder passwordEncoder) {
    this.users = users;
    this.currencies = currencies;
    this.passwordEncoder = passwordEncoder;
  }

  @Transactional(readOnly = true)
  public User get(UUID userId) {
    return users.findById(userId).orElseThrow(() -> ApiException.notFound("Usuario"));
  }

  @Transactional
  public User update(UUID userId, UpdateMeRequest request) {
    User user = get(userId);
    if (request.baseCurrency() != null) {
      user.setBaseCurrency(currencies.requireSupported(request.baseCurrency()));
    }
    if (request.locale() != null) {
      user.setLocale(request.locale());
    }
    return user;
  }

  /** Derecho de supresión: borra el usuario y, en cascada, todos sus datos y sesiones. */
  @Transactional
  public void delete(UUID userId, String password) {
    User user = get(userId);
    if (!passwordEncoder.matches(password, user.getPasswordHash())) {
      throw new ApiException(HttpStatus.FORBIDDEN, "INVALID_PASSWORD", "Contraseña incorrecta");
    }
    users.delete(user);
    log.info("account_deleted userId={}", userId);
  }
}
