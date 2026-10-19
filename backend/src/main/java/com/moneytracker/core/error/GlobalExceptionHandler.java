package com.moneytracker.core.error;

import com.moneytracker.core.web.RequestIdFilter;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * Traduce cualquier excepción a {@link ApiError}. Los mensajes son genéricos: el detalle técnico
 * solo va al log del servidor, junto al requestId.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

  private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  @ExceptionHandler(ApiException.class)
  ResponseEntity<ApiError> handleApi(ApiException ex) {
    return build(ex.status(), ex.code(), ex.getMessage(), List.of());
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex) {
    List<ApiError.FieldError> details =
        ex.getBindingResult().getFieldErrors().stream()
            .map(e -> new ApiError.FieldError(e.getField(), e.getDefaultMessage()))
            .toList();
    return build(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Datos no válidos", details);
  }

  @ExceptionHandler(HandlerMethodValidationException.class)
  ResponseEntity<ApiError> handleMethodValidation(HandlerMethodValidationException ex) {
    return build(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Parámetros no válidos", List.of());
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  ResponseEntity<ApiError> handleUnreadable(HttpMessageNotReadableException ex) {
    // Incluye JSON mal formado, tipos incorrectos y campos no permitidos.
    return build(
        HttpStatus.BAD_REQUEST,
        "MALFORMED_REQUEST",
        "Cuerpo de la petición no válido o con campos no permitidos",
        List.of());
  }

  @ExceptionHandler({
    MethodArgumentTypeMismatchException.class,
    MissingServletRequestParameterException.class,
    MissingRequestHeaderException.class
  })
  ResponseEntity<ApiError> handleBadParams(Exception ex) {
    return build(HttpStatus.BAD_REQUEST, "INVALID_PARAMETER", "Parámetro no válido", List.of());
  }

  @ExceptionHandler(NoResourceFoundException.class)
  ResponseEntity<ApiError> handleNoResource(NoResourceFoundException ex) {
    return build(HttpStatus.NOT_FOUND, "NOT_FOUND", "Recurso no encontrado", List.of());
  }

  @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
  ResponseEntity<ApiError> handleMethod(HttpRequestMethodNotSupportedException ex) {
    return build(
        HttpStatus.METHOD_NOT_ALLOWED, "METHOD_NOT_ALLOWED", "Método no permitido", List.of());
  }

  @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
  ResponseEntity<ApiError> handleMediaType(HttpMediaTypeNotSupportedException ex) {
    return build(
        HttpStatus.UNSUPPORTED_MEDIA_TYPE,
        "UNSUPPORTED_MEDIA_TYPE",
        "Tipo de contenido no soportado",
        List.of());
  }

  @ExceptionHandler(DataIntegrityViolationException.class)
  ResponseEntity<ApiError> handleIntegrity(DataIntegrityViolationException ex) {
    log.warn("Conflicto de integridad de datos", ex);
    return build(HttpStatus.CONFLICT, "CONFLICT", "La operación entra en conflicto", List.of());
  }

  @ExceptionHandler(Exception.class)
  ResponseEntity<ApiError> handleUnexpected(Exception ex) {
    log.error("Error no controlado", ex);
    return build(
        HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "Error interno del servidor", List.of());
  }

  private static ResponseEntity<ApiError> build(
      HttpStatus status, String code, String message, List<ApiError.FieldError> details) {
    return ResponseEntity.status(status)
        .body(new ApiError(code, message, MDC.get(RequestIdFilter.MDC_KEY), details));
  }
}
