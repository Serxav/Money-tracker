package com.moneytracker.categories;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public final class CategoryDtos {

  static final String ICON = "^[a-z0-9-]{1,40}$";
  static final String COLOR = "^#[0-9A-Fa-f]{6}$";

  private CategoryDtos() {}

  public record CreateCategoryRequest(
      @NotBlank @Size(max = 40) String name,
      @NotNull CategoryType type,
      @Pattern(regexp = ICON, message = "Icono no válido") String icon,
      @Pattern(regexp = COLOR, message = "Color en formato #RRGGBB") String color) {}

  /** El tipo no se puede cambiar; los campos nulos no se modifican. */
  public record UpdateCategoryRequest(
      @Size(min = 1, max = 40) String name,
      @Pattern(regexp = ICON, message = "Icono no válido") String icon,
      @Pattern(regexp = COLOR, message = "Color en formato #RRGGBB") String color) {}

  public record CategoryResponse(UUID id, String name, CategoryType type, String icon, String color) {

    public static CategoryResponse of(Category c) {
      return new CategoryResponse(c.getId(), c.getName(), c.getType(), c.getIcon(), c.getColor());
    }
  }
}
