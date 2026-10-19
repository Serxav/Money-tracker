package com.moneytracker.transactions;

import com.moneytracker.categories.CategoryType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Movimiento (ingreso o gasto). El importe es positivo, en unidades menores, y la divisa es
 * siempre la de su cuenta.
 */
@Entity
@Table(name = "transactions")
public class Movement {

  @Id private UUID id;

  @Column(name = "user_id", nullable = false, updatable = false)
  private UUID userId;

  @Column(name = "account_id", nullable = false)
  private UUID accountId;

  @Column(name = "category_id", nullable = false)
  private UUID categoryId;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private CategoryType type;

  @Column(nullable = false)
  private long amount;

  @Column(nullable = false)
  private String currency;

  @Column(nullable = false)
  private LocalDate date;

  private String description;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  protected Movement() {}

  public Movement(UUID userId) {
    this.id = UUID.randomUUID();
    this.userId = userId;
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

  public void apply(
      UUID accountId,
      UUID categoryId,
      CategoryType type,
      long amount,
      String currency,
      LocalDate date,
      String description) {
    this.accountId = accountId;
    this.categoryId = categoryId;
    this.type = type;
    this.amount = amount;
    this.currency = currency;
    this.date = date;
    this.description = description;
  }

  public UUID getId() {
    return id;
  }

  public UUID getUserId() {
    return userId;
  }

  public UUID getAccountId() {
    return accountId;
  }

  public UUID getCategoryId() {
    return categoryId;
  }

  public CategoryType getType() {
    return type;
  }

  public long getAmount() {
    return amount;
  }

  public String getCurrency() {
    return currency;
  }

  public LocalDate getDate() {
    return date;
  }

  public String getDescription() {
    return description;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }
}
