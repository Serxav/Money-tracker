package com.moneytracker.core.error;

import org.springframework.http.HttpStatus;

/** Error de negocio con código estable y mensaje seguro para mostrar al cliente. */
public class ApiException extends RuntimeException {

  private final HttpStatus status;
  private final String code;

  public ApiException(HttpStatus status, String code, String message) {
    super(message);
    this.status = status;
    this.code = code;
  }

  public HttpStatus status() {
    return status;
  }

  public String code() {
    return code;
  }

  public static ApiException notFound(String what) {
    return new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", what + " no encontrado");
  }

  public static ApiException badRequest(String code, String message) {
    return new ApiException(HttpStatus.BAD_REQUEST, code, message);
  }

  public static ApiException conflict(String code, String message) {
    return new ApiException(HttpStatus.CONFLICT, code, message);
  }

  public static ApiException unprocessable(String code, String message) {
    return new ApiException(HttpStatus.UNPROCESSABLE_CONTENT, code, message);
  }
}
