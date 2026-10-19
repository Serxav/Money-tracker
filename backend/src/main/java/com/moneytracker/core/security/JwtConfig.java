package com.moneytracker.core.security;

import com.moneytracker.core.config.AppProperties;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

/**
 * Clave HMAC para firmar los access tokens. Se lee de {@code JWT_SECRET} (mínimo 32 bytes). Solo
 * en el perfil {@code dev} se genera una clave efímera si no se proporciona.
 */
@Configuration
public class JwtConfig {

  private static final Logger log = LoggerFactory.getLogger(JwtConfig.class);
  private static final int MIN_BYTES = 32;

  private final SecretKey key;
  private final String issuer;

  public JwtConfig(AppProperties properties, Environment env) {
    this.issuer = properties.jwt().issuer();
    this.key = new SecretKeySpec(resolveSecret(properties.jwt().secret(), env), "HmacSHA256");
  }

  private static byte[] resolveSecret(String secret, Environment env) {
    if (secret == null || secret.isBlank()) {
      if (env.acceptsProfiles(Profiles.of("dev", "test"))) {
        log.warn("JWT_SECRET no definido: se usa una clave efímera (solo dev/test)");
        byte[] random = new byte[MIN_BYTES];
        new SecureRandom().nextBytes(random);
        return random;
      }
      throw new IllegalStateException("JWT_SECRET es obligatorio fuera del perfil dev");
    }
    byte[] bytes = decode(secret);
    if (bytes.length < MIN_BYTES) {
      throw new IllegalStateException("JWT_SECRET debe tener al menos " + MIN_BYTES + " bytes");
    }
    return bytes;
  }

  private static byte[] decode(String secret) {
    try {
      return Base64.getDecoder().decode(secret);
    } catch (IllegalArgumentException notBase64) {
      return secret.getBytes(StandardCharsets.UTF_8);
    }
  }

  @Bean
  JwtEncoder jwtEncoder() {
    return new NimbusJwtEncoder(new ImmutableSecret<>(Arrays.copyOf(key.getEncoded(), key.getEncoded().length)));
  }

  @Bean
  JwtDecoder jwtDecoder() {
    NimbusJwtDecoder decoder =
        NimbusJwtDecoder.withSecretKey(key).macAlgorithm(MacAlgorithm.HS256).build();
    decoder.setJwtValidator(
        new DelegatingOAuth2TokenValidator<>(JwtValidators.createDefaultWithIssuer(issuer)));
    return decoder;
  }

  public String issuer() {
    return issuer;
  }
}
