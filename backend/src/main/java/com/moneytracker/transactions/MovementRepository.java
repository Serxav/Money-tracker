package com.moneytracker.transactions;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MovementRepository
    extends JpaRepository<Movement, UUID>, JpaSpecificationExecutor<Movement> {

  Optional<Movement> findByIdAndUserId(UUID id, UUID userId);

  boolean existsByUserIdAndAccountId(UUID userId, UUID accountId);

  boolean existsByUserIdAndCategoryId(UUID userId, UUID categoryId);

  interface AccountNet {
    UUID getAccountId();

    Long getNet();
  }

  @Query(
      value =
          """
          SELECT account_id AS accountId,
                 CAST(COALESCE(SUM(CASE WHEN type = 'INCOME' THEN amount ELSE -amount END), 0) AS BIGINT) AS net
          FROM transactions
          WHERE user_id = :userId AND account_id IN (:accountIds)
          GROUP BY account_id
          """,
      nativeQuery = true)
  List<AccountNet> netByAccount(
      @Param("userId") UUID userId, @Param("accountIds") Collection<UUID> accountIds);
}
