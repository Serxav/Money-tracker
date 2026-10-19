package com.moneytracker.currencies;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/currencies")
public class CurrencyController {

  private final CurrencyService currencies;

  public CurrencyController(CurrencyService currencies) {
    this.currencies = currencies;
  }

  @GetMapping
  public List<CurrencyResponse> list() {
    return currencies.list();
  }
}
