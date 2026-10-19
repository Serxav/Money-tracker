package com.moneytracker.core.web;

import java.util.List;
import java.util.function.Function;
import org.springframework.data.domain.Page;

/** Respuesta paginada común a todos los listados. */
public record PageResponse<T>(List<T> items, int page, int limit, long total) {

  public static <E, T> PageResponse<T> of(Page<E> page, Function<E, T> mapper) {
    return new PageResponse<>(
        page.getContent().stream().map(mapper).toList(),
        page.getNumber(),
        page.getSize(),
        page.getTotalElements());
  }
}
