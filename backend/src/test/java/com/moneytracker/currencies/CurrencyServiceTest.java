package com.moneytracker.currencies;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.moneytracker.core.config.AppProperties;
import com.moneytracker.core.error.ApiException;
import java.util.List;
import org.junit.jupiter.api.Test;

class CurrencyServiceTest {

  private final CurrencyService service =
      new CurrencyService(
          new AppProperties(
              null, null, new AppProperties.Currencies(List.of("EUR", "JPY", "KWD")), null));

  @Test
  void fractionDigitsComeFromIso4217() {
    assertThat(service.fractionDigits("EUR")).isEqualTo(2);
    assertThat(service.fractionDigits("JPY")).isZero();
    assertThat(service.fractionDigits("KWD")).isEqualTo(3);
  }

  @Test
  void unsupportedCurrenciesAreRejected() {
    assertThatThrownBy(() -> service.requireSupported("USD")).isInstanceOf(ApiException.class);
    assertThatThrownBy(() -> service.requireSupported("eur")).isInstanceOf(ApiException.class);
  }
}
