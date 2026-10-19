package com.moneytracker.core.web;

import com.moneytracker.core.error.ApiException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

/** Valida la paginación: página desde 0 y un límite máximo para evitar listados masivos. */
public final class PageParams {

  public static final int DEFAULT_LIMIT = 20;
  public static final int MAX_LIMIT = 100;

  private PageParams() {}

  public static PageRequest of(int page, int limit, Sort sort) {
    if (page < 0) {
      throw ApiException.badRequest("INVALID_PAGE", "La página debe ser 0 o mayor");
    }
    if (limit < 1 || limit > MAX_LIMIT) {
      throw ApiException.badRequest(
          "INVALID_LIMIT", "El límite debe estar entre 1 y " + MAX_LIMIT);
    }
    return PageRequest.of(page, limit, sort);
  }
}
