package com.moneytracker.transactions;

import com.moneytracker.accounts.AccountBalanceProvider;
import com.moneytracker.categories.CategoryUsageProvider;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

/** Saldos y uso de categorías calculados a partir de los movimientos (una consulta por lista). */
@Component
class MovementStats implements AccountBalanceProvider, CategoryUsageProvider {

  private final MovementRepository movements;

  MovementStats(MovementRepository movements) {
    this.movements = movements;
  }

  @Override
  public Map<UUID, Long> netMovements(UUID userId, Collection<UUID> accountIds) {
    if (accountIds.isEmpty()) {
      return Map.of();
    }
    return movements.netByAccount(userId, accountIds).stream()
        .collect(
            Collectors.toMap(
                MovementRepository.AccountNet::getAccountId, MovementRepository.AccountNet::getNet));
  }

  @Override
  public boolean hasMovements(UUID userId, UUID accountId) {
    return movements.existsByUserIdAndAccountId(userId, accountId);
  }

  @Override
  public boolean isUsed(UUID userId, UUID categoryId) {
    return movements.existsByUserIdAndCategoryId(userId, categoryId);
  }
}
