package com.regifted.app.security;

import io.jsonwebtoken.MalformedJwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilsTest {

  private JwtUtils jwtUtils;
  private String secret;
  private long expirationMs;

  private final String USER_UUID = "user-123";
  private final String EMAIL = "john@example.com";

  @BeforeEach
  void setup() {
    // Secret must be at least 256 bits (32 bytes) for HS256
    secret = Base64.getEncoder().encodeToString("this_is_a_super_secret_jwt_key_123456".getBytes());
    expirationMs = 2000L; // 2 seconds
    jwtUtils = new JwtUtils(secret, expirationMs);
  }

  @Nested
  @DisplayName("Token Generation")
  class TokenGenerationTests {

    @Test
    @DisplayName("Generates a valid JWT with subject and email claim")
    void testGenerateToken() {
      String token = jwtUtils.generateToken(USER_UUID, EMAIL);
      assertNotNull(token);
      assertTrue(token.split("\\.").length == 3, "Token should have 3 JWT parts");

      assertTrue(jwtUtils.validateToken(token), "Generated token should be valid");
      assertEquals(USER_UUID, jwtUtils.getUserUuidFromToken(token));
    }
  }

  @Nested
  @DisplayName("Token Validation")
  class TokenValidationTests {

    @Test
    @DisplayName("Valid token passes validation")
    void testValidateValidToken() {
      String token = jwtUtils.generateToken(USER_UUID, EMAIL);
      assertTrue(jwtUtils.validateToken(token));
    }

    @Test
    @DisplayName("Invalid token fails validation")
    void testValidateInvalidToken() {
      String token = "invalid.token.value";
      assertFalse(jwtUtils.validateToken(token));
    }

    @Test
    @DisplayName("Expired token fails validation")
    void testExpiredToken() throws InterruptedException {
      JwtUtils shortLived = new JwtUtils(secret, 500); // 0.5 sec expiration
      String token = shortLived.generateToken(USER_UUID, EMAIL);

      Thread.sleep(600); // wait for token to expire
      assertFalse(shortLived.validateToken(token), "Expired token should be invalid");
    }

    @Test
    @DisplayName("Tampered token fails validation")
    void testTamperedToken() {
      String token = jwtUtils.generateToken(USER_UUID, EMAIL);
      String tampered = token.substring(0, token.length() - 2) + "aa";
      assertFalse(jwtUtils.validateToken(tampered));
    }
  }

  @Nested
  @DisplayName("Claims Extraction")
  class ClaimsExtractionTests {

    @Test
    @DisplayName("Extracts correct user UUID from valid token")
    void testExtractUserUuid() {
      String token = jwtUtils.generateToken(USER_UUID, EMAIL);
      assertEquals(USER_UUID, jwtUtils.getUserUuidFromToken(token));
    }

    @Test
    @DisplayName("Throws exception for malformed token")
    void testMalformedToken() {
      String token = "bad.token";
      assertThrows(MalformedJwtException.class, () -> jwtUtils.getUserUuidFromToken(token));
    }
  }
}
