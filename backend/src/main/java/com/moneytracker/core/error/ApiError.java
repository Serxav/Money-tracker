package com.moneytracker.core.error;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

/** Formato estándar de error: nunca incluye detalles internos del servidor. */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record ApiError(String code, String message, String requestId, List<FieldError> details) {

  public record FieldError(String field, String message) {}

  public static ApiError of(String code, String message, String requestId) {
    return new ApiError(code, message, requestId, List.of());
  }
}
