package com.moneytracker.categories;

import java.util.UUID;

/** Indica si una categoría tiene movimientos asociados. */
public interface CategoryUsageProvider {

  boolean isUsed(UUID userId, UUID categoryId);
}
