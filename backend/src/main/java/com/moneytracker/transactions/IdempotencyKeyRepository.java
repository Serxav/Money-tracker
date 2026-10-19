package com.moneytracker.transactions;

import java.time.Instant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface IdempotencyKeyRepository
    extends JpaRepository<IdempotencyKey, IdempotencyKey.Id> {

  @Modifying
  @Transactional
  @Query("delete from IdempotencyKey k where k.createdAt < :before")
  int deleteOlderThan(@Param("before") Instant before);
}
