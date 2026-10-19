package com.moneytracker.transactions;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

/** Recuerda qué movimiento creó cada {@code Idempotency-Key} para no duplicarlo al reintentar. */
@Entity
@Table(name = "idempotency_keys")
public class IdempotencyKey {

  @Embeddable
  public record Id(
      @Column(name = "user_id") UUID userId, @Column(name = "idem_key") String key)
      implements Serializable {}

  @EmbeddedId private Id id;

  @Column(name = "request_hash", nullable = false)
  private String requestHash;

  @Column(name = "transaction_id", nullable = false)
  private UUID transactionId;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  protected IdempotencyKey() {}

  public IdempotencyKey(UUID userId, String key, String requestHash, UUID transactionId) {
    this.id = new Id(userId, key);
    this.requestHash = requestHash;
    this.transactionId = transactionId;
    this.createdAt = Instant.now();
  }

  public String getRequestHash() {
    return requestHash;
  }

  public UUID getTransactionId() {
    return transactionId;
  }
}
