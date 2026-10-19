package com.moneytracker.accounts;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

/** Suma de movimientos por cuenta (ingresos − gastos), en unidades menores. */
public interface AccountBalanceProvider {

  Map<UUID, Long> netMovements(UUID userId, Collection<UUID> accountIds);

  boolean hasMovements(UUID userId, UUID accountId);
}
