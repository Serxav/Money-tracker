package com.moneytracker.core.error;

import com.moneytracker.core.web.RequestIdFilter;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.slf4j.MDC;
import org.springframework.http.MediaType;

/** Escribe un {@link ApiError} desde filtros, donde no llega el manejador global. */
public final class ErrorResponseWriter {

  private ErrorResponseWriter() {}

  public static void write(HttpServletResponse response, int status, String code, String message)
      throws IOException {
    response.setStatus(status);
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.setCharacterEncoding("UTF-8");
    String requestId = MDC.get(RequestIdFilter.MDC_KEY);
    response
        .getWriter()
        .write(
            "{\"code\":\""
                + escape(code)
                + "\",\"message\":\""
                + escape(message)
                + "\""
                + (requestId == null ? "" : ",\"requestId\":\"" + escape(requestId) + "\"")
                + "}");
  }

  private static String escape(String value) {
    StringBuilder sb = new StringBuilder();
    for (char c : value.toCharArray()) {
      switch (c) {
        case '"' -> sb.append("\\\"");
        case '\\' -> sb.append("\\\\");
        default -> {
          if (c < 0x20) {
            sb.append(String.format("\\u%04x", (int) c));
          } else {
            sb.append(c);
          }
        }
      }
    }
    return sb.toString();
  }
}
