package com.moneytracker.transactions;

import com.moneytracker.accounts.AccountBalanceProvider;
import com.moneytracker.categories.CategoryUsageProvider;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Component;

/** Provisional hasta que exista el módulo de movimientos. */
@Component
class NoMovementsYet implements AccountBalanceProvider, CategoryUsageProvider {

  @Override
  public Map<UUID, Long> netMovements(UUID userId, Collection<UUID> accountIds) {
    return Map.of();
  }

  @Override
  public boolean hasMovements(UUID userId, UUID accountId) {
    return false;
  }

  @Override
  public boolean isUsed(UUID userId, UUID categoryId) {
    return false;
  }
}
