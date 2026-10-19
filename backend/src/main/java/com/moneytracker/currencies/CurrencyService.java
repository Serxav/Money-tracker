package com.moneytracker.currencies;

import com.moneytracker.core.config.AppProperties;
import com.moneytracker.core.error.ApiException;
import java.util.Currency;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.stereotype.Service;

/**
 * Divisas soportadas (ISO 4217). Los decimales de cada divisa salen de la tabla ISO de Java, nunca
 * se asume 2.
 */
@Service
public class CurrencyService {

  private final Map<String, Currency> supported = new LinkedHashMap<>();

  public CurrencyService(AppProperties properties) {
    for (String code : properties.currencies().supported()) {
      Currency currency = Currency.getInstance(code.trim());
      if (currency.getDefaultFractionDigits() < 0) {
        throw new IllegalStateException("Divisa sin decimales definidos: " + code);
      }
      supported.put(currency.getCurrencyCode(), currency);
    }
  }

  public List<CurrencyResponse> list() {
    return supported.values().stream()
        .map(
            c ->
                new CurrencyResponse(
                    c.getCurrencyCode(),
                    c.getDefaultFractionDigits(),
                    c.getDisplayName(Locale.forLanguageTag("es"))))
        .toList();
  }

  /** Devuelve el código normalizado o lanza 422 si la divisa no está soportada. */
  public String requireSupported(String code) {
    if (code == null || !supported.containsKey(code)) {
      throw ApiException.unprocessable("UNSUPPORTED_CURRENCY", "Divisa no soportada");
    }
    return code;
  }

  public int fractionDigits(String code) {
    return supported.get(requireSupported(code)).getDefaultFractionDigits();
  }
}
