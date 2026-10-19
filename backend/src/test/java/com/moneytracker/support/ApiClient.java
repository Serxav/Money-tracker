package com.moneytracker.support;

import com.jayway.jsonpath.JsonPath;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

/** Cliente HTTP mínimo para los tests de integración contra el servidor real. */
public class ApiClient {

  private final HttpClient http = HttpClient.newHttpClient();
  private final String baseUrl;

  public ApiClient(int port) {
    this.baseUrl = "http://localhost:" + port;
  }

  public Response get(String path, String token) {
    return send("GET", path, null, token, Map.of());
  }

  public Response post(String path, String body, String token) {
    return send("POST", path, body, token, Map.of());
  }

  public Response put(String path, String body, String token) {
    return send("PUT", path, body, token, Map.of());
  }

  public Response patch(String path, String body, String token) {
    return send("PATCH", path, body, token, Map.of());
  }

  public Response delete(String path, String body, String token) {
    return send("DELETE", path, body, token, Map.of());
  }

  public Response send(
      String method, String path, String body, String token, Map<String, String> headers) {
    HttpRequest.Builder builder =
        HttpRequest.newBuilder(URI.create(baseUrl + path))
            .method(
                method,
                body == null
                    ? HttpRequest.BodyPublishers.noBody()
                    : HttpRequest.BodyPublishers.ofString(body));
    if (body != null) {
      builder.header("Content-Type", "application/json");
    }
    if (token != null) {
      builder.header("Authorization", "Bearer " + token);
    }
    headers.forEach(builder::header);
    try {
      HttpResponse<String> response = http.send(builder.build(), HttpResponse.BodyHandlers.ofString());
      return new Response(response.statusCode(), response.body(), response);
    } catch (IOException | InterruptedException e) {
      throw new IllegalStateException(e);
    }
  }

  public record Response(int status, String body, HttpResponse<String> raw) {

    public <T> T json(String path) {
      return JsonPath.read(body, path);
    }

    public String header(String name) {
      return raw.headers().firstValue(name).orElse(null);
    }
  }
}
