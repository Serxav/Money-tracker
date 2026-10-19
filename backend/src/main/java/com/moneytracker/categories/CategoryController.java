package com.moneytracker.categories;

import com.moneytracker.categories.CategoryDtos.CategoryResponse;
import com.moneytracker.categories.CategoryDtos.CreateCategoryRequest;
import com.moneytracker.categories.CategoryDtos.UpdateCategoryRequest;
import com.moneytracker.core.security.CurrentUser;
import com.moneytracker.core.web.PageParams;
import com.moneytracker.core.web.PageResponse;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/categories")
public class CategoryController {

  private final CategoryService categories;

  public CategoryController(CategoryService categories) {
    this.categories = categories;
  }

  @GetMapping
  public PageResponse<CategoryResponse> list(
      @RequestParam(required = false) CategoryType type,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "" + PageParams.MAX_LIMIT) int limit) {
    return categories.list(
        CurrentUser.id(),
        type,
        PageParams.of(page, limit, Sort.by("type", "name").and(Sort.by("id"))));
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public CategoryResponse create(@Valid @RequestBody CreateCategoryRequest request) {
    return categories.create(CurrentUser.id(), request);
  }

  @PatchMapping("/{id}")
  public CategoryResponse update(
      @PathVariable UUID id, @Valid @RequestBody UpdateCategoryRequest request) {
    return categories.update(CurrentUser.id(), id, request);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable UUID id) {
    categories.delete(CurrentUser.id(), id);
  }
}
