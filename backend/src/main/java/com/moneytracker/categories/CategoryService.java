package com.moneytracker.categories;

import com.moneytracker.categories.CategoryDtos.CategoryResponse;
import com.moneytracker.categories.CategoryDtos.CreateCategoryRequest;
import com.moneytracker.categories.CategoryDtos.UpdateCategoryRequest;
import com.moneytracker.core.error.ApiException;
import com.moneytracker.core.web.PageResponse;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CategoryService {

  private final CategoryRepository categories;
  private final CategoryUsageProvider usage;

  public CategoryService(CategoryRepository categories, CategoryUsageProvider usage) {
    this.categories = categories;
    this.usage = usage;
  }

  @Transactional(readOnly = true)
  public PageResponse<CategoryResponse> list(UUID userId, CategoryType type, Pageable pageable) {
    return PageResponse.of(
        type == null
            ? categories.findByUserId(userId, pageable)
            : categories.findByUserIdAndType(userId, type, pageable),
        CategoryResponse::of);
  }

  /** Para otros módulos: la categoría solo existe si pertenece al usuario. */
  @Transactional(readOnly = true)
  public Category require(UUID userId, UUID id) {
    return categories
        .findByIdAndUserId(id, userId)
        .orElseThrow(() -> ApiException.notFound("Categoría"));
  }

  @Transactional
  public CategoryResponse create(UUID userId, CreateCategoryRequest request) {
    String name = request.name().trim();
    ensureNameAvailable(userId, request.type(), name);
    return CategoryResponse.of(
        categories.save(new Category(userId, name, request.type(), request.icon(), request.color())));
  }

  @Transactional
  public CategoryResponse update(UUID userId, UUID id, UpdateCategoryRequest request) {
    Category category = require(userId, id);
    if (request.name() != null) {
      String name = request.name().trim();
      if (!name.equalsIgnoreCase(category.getName())) {
        ensureNameAvailable(userId, category.getType(), name);
      }
      if (name.isEmpty()) {
        throw ApiException.badRequest("VALIDATION_ERROR", "El nombre no puede estar vacío");
      }
      category.setName(name);
    }
    if (request.icon() != null) {
      category.setIcon(request.icon());
    }
    if (request.color() != null) {
      category.setColor(request.color());
    }
    return CategoryResponse.of(category);
  }

  @Transactional
  public void delete(UUID userId, UUID id) {
    Category category = require(userId, id);
    if (usage.isUsed(userId, id)) {
      throw ApiException.conflict(
          "CATEGORY_IN_USE", "La categoría tiene movimientos: cámbialos de categoría antes");
    }
    categories.delete(category);
  }

  private void ensureNameAvailable(UUID userId, CategoryType type, String name) {
    if (name.isEmpty()) {
      throw ApiException.badRequest("VALIDATION_ERROR", "El nombre no puede estar vacío");
    }
    if (categories.existsByUserIdAndTypeAndNameIgnoreCase(userId, type, name)) {
      throw ApiException.conflict("CATEGORY_NAME_TAKEN", "Ya tienes una categoría con ese nombre");
    }
  }
}
