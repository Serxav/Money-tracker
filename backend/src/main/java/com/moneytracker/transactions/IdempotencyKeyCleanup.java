package com.moneytracker.transactions;

import java.time.Duration;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** Las claves de idempotencia solo sirven para reintentos cercanos: se borran a los 7 días. */
@Component
class IdempotencyKeyCleanup {

  private static final Logger log = LoggerFactory.getLogger(IdempotencyKeyCleanup.class);
  static final Duration RETENTION = Duration.ofDays(7);

  private final IdempotencyKeyRepository keys;

  IdempotencyKeyCleanup(IdempotencyKeyRepository keys) {
    this.keys = keys;
  }

  @Scheduled(cron = "0 30 3 * * *")
  void purge() {
    int deleted = keys.deleteOlderThan(Instant.now().minus(RETENTION));
    log.info("idempotency_keys_purged count={}", deleted);
  }
}
