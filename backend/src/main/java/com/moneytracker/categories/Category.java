package com.moneytracker.categories;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

/** Categoría de ingreso o gasto. Su tipo no cambia después de crearla. */
@Entity
@Table(name = "categories")
public class Category {

  @Id private UUID id;

  @Column(name = "user_id", nullable = false, updatable = false)
  private UUID userId;

  @Column(nullable = false)
  private String name;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, updatable = false)
  private CategoryType type;

  private String icon;

  private String color;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  protected Category() {}

  public Category(UUID userId, String name, CategoryType type, String icon, String color) {
    this.id = UUID.randomUUID();
    this.userId = userId;
    this.name = name;
    this.type = type;
    this.icon = icon;
    this.color = color;
  }

  @PrePersist
  void onCreate() {
    createdAt = Instant.now();
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

  public CategoryType getType() {
    return type;
  }

  public String getIcon() {
    return icon;
  }

  public void setIcon(String icon) {
    this.icon = icon;
  }

  public String getColor() {
    return color;
  }

  public void setColor(String color) {
    this.color = color;
  }
}
