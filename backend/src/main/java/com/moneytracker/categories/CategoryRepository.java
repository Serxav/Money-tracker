package com.moneytracker.categories;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, UUID> {

  Page<Category> findByUserId(UUID userId, Pageable pageable);

  Page<Category> findByUserIdAndType(UUID userId, CategoryType type, Pageable pageable);

  Optional<Category> findByIdAndUserId(UUID id, UUID userId);

  boolean existsByUserIdAndTypeAndNameIgnoreCase(UUID userId, CategoryType type, String name);
}
