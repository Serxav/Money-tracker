package com.moneytracker.auth;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.moneytracker.core.error.ApiException;
import org.junit.jupiter.api.Test;

class PasswordPolicyTest {

  @Test
  void acceptsLongUncommonPasswords() {
    assertThatCode(() -> PasswordPolicy.check("caballo-bateria-grapa", "ana@example.com"))
        .doesNotThrowAnyException();
  }

  @Test
  void rejectsShortCommonRepetitiveOrEmailBasedPasswords() {
    for (String weak :
        new String[] {"corta", "1234567890", "contraseña", "abababababab", "anagarcia2026"}) {
      assertThatThrownBy(() -> PasswordPolicy.check(weak, "anagarcia@example.com"))
          .as(weak)
          .isInstanceOf(ApiException.class);
    }
  }

  @Test
  void rejectsTooLongPasswords() {
    assertThatThrownBy(() -> PasswordPolicy.check("x".repeat(129) + "abc", "a@b.c"))
        .isInstanceOf(ApiException.class);
  }
}
