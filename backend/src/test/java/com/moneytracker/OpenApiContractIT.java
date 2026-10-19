package com.moneytracker;

import static org.assertj.core.api.Assertions.assertThat;

import com.moneytracker.support.ApiClient.Response;
import com.moneytracker.support.IntegrationTest;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

/**
 * Garantiza que {@code backend/openapi.yaml} (contrato con la app móvil) coincide con la API real.
 * Para regenerarlo tras un cambio de API: {@code ./mvnw verify -Dopenapi.update=true}.
 */
class OpenApiContractIT extends IntegrationTest {

  @Test
  void committedContractMatchesTheApi() throws IOException {
    Response res = api.get("/v3/api-docs.yaml", null);
    assertThat(res.status()).isEqualTo(200);
    String actual = res.body().strip() + "\n";
    Path spec = Path.of(System.getProperty("openapi.spec", "openapi.yaml"));

    if (Boolean.getBoolean("openapi.update")) {
      Files.writeString(spec, actual);
      return;
    }
    assertThat(spec)
        .as("Falta openapi.yaml: ejecuta ./mvnw verify -Dopenapi.update=true")
        .exists();
    assertThat(Files.readString(spec))
        .as("openapi.yaml no está al día: ejecuta ./mvnw verify -Dopenapi.update=true y revisa el diff")
        .isEqualTo(actual);
  }
}
