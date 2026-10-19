package com.moneytracker.auth;

import com.moneytracker.core.config.AppProperties;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.UUID;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

/** Emite access tokens JWT de vida corta y refresh tokens opacos aleatorios. */
@Service
public class TokenService {

  private static final SecureRandom RANDOM = new SecureRandom();

  private final JwtEncoder encoder;
  private final AppProperties.Jwt config;

  public TokenService(JwtEncoder encoder, AppProperties properties) {
    this.encoder = encoder;
    this.config = properties.jwt();
  }

  public String accessToken(UUID userId, Instant now) {
    JwtClaimsSet claims =
        JwtClaimsSet.builder()
            .issuer(config.issuer())
            .subject(userId.toString())
            .issuedAt(now)
            .expiresAt(now.plus(config.accessTokenTtl()))
            .id(UUID.randomUUID().toString())
            .build();
    JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
    return encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
  }

  public long accessTokenTtlSeconds() {
    return config.accessTokenTtl().toSeconds();
  }

  public Instant refreshTokenExpiry(Instant now) {
    return now.plus(config.refreshTokenTtl());
  }

  public String newRefreshToken() {
    byte[] bytes = new byte[32];
    RANDOM.nextBytes(bytes);
    return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
  }

  public static String sha256(String value) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      return HexFormat.of().formatHex(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException(e);
    }
  }
}
