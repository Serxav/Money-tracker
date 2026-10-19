package com.moneytracker.transactions;

import com.moneytracker.categories.CategoryType;
import com.moneytracker.core.web.Amounts;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/** Importes positivos en unidades menores; el tipo indica si suma (ingreso) o resta (gasto). */
public final class MovementDtos {

  private MovementDtos() {}

  public record MovementRequest(
      @NotNull UUID accountId,
      @NotNull UUID categoryId,
      @NotNull CategoryType type,
      @NotNull @Min(1) @Max(Amounts.MAX) Long amount,
      @NotNull LocalDate date,
      @Size(max = 255) String description) {}

  public record MovementResponse(
      UUID id,
      UUID accountId,
      UUID categoryId,
      CategoryType type,
      long amount,
      String currency,
      LocalDate date,
      String description,
      Instant createdAt,
      Instant updatedAt) {

    public static MovementResponse of(Movement m) {
      return new MovementResponse(
          m.getId(),
          m.getAccountId(),
          m.getCategoryId(),
          m.getType(),
          m.getAmount(),
          m.getCurrency(),
          m.getDate(),
          m.getDescription(),
          m.getCreatedAt(),
          m.getUpdatedAt());
    }
  }

  public record MovementFilter(
      UUID accountId, UUID categoryId, CategoryType type, LocalDate from, LocalDate to) {}
}
