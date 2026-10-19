package com.moneytracker.accounts;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

/** Cuenta del usuario. Su divisa no cambia después de crearla. */
@Entity
@Table(name = "accounts")
public class Account {

  @Id private UUID id;

  @Column(name = "user_id", nullable = false, updatable = false)
  private UUID userId;

  @Column(nullable = false)
  private String name;

  @Column(nullable = false, updatable = false)
  private String currency;

  @Column(name = "initial_balance", nullable = false)
  private long initialBalance;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  protected Account() {}

  public Account(UUID userId, String name, String currency, long initialBalance) {
    this.id = UUID.randomUUID();
    this.userId = userId;
    this.name = name;
    this.currency = currency;
    this.initialBalance = initialBalance;
  }

  @PrePersist
  void onCreate() {
    createdAt = Instant.now();
    updatedAt = createdAt;
  }

  @PreUpdate
  void onUpdate() {
    updatedAt = Instant.now();
  }

  public UUID getId() {
    return id;
  }

  public UUID getUserId() {
    return userId;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getCurrency() {
    return currency;
  }

  public long getInitialBalance() {
    return initialBalance;
  }

  public void setInitialBalance(long initialBalance) {
    this.initialBalance = initialBalance;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }
}
