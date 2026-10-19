package com.moneytracker.accounts;

import com.moneytracker.core.web.Amounts;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.UUID;

/** Peticiones y respuestas de cuentas. Importes en unidades menores de la divisa. */
public final class AccountDtos {

  private AccountDtos() {}

  public record CreateAccountRequest(
      @NotBlank @Size(max = 60) String name,
      @NotNull @Pattern(regexp = "^[A-Z]{3}$", message = "Código ISO 4217 de 3 letras") String currency,
      @Min(-Amounts.MAX) @Max(Amounts.MAX) Long initialBalance) {}

  /** La divisa no se puede cambiar; los campos nulos no se modifican. */
  public record UpdateAccountRequest(
      @Size(min = 1, max = 60) String name,
      @Min(-Amounts.MAX) @Max(Amounts.MAX) Long initialBalance) {}

  public record AccountResponse(
      UUID id,
      String name,
      String currency,
      int fractionDigits,
      long initialBalance,
      long balance,
      Instant createdAt) {}
}
