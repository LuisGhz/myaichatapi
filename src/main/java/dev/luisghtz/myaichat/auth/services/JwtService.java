package dev.luisghtz.myaichat.auth.services;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import dev.luisghtz.myaichat.auth.entities.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

@Service
@Slf4j
public class JwtService {

  @Value("${app.jwt.secret}")
  private String jwtSecret;

  @Value("${app.jwt.expiration:86400000}") // 24 hours
  private long jwtExpiration;

  public String generateToken(User user) {
    long now = System.currentTimeMillis();
    // Add a random nonce to ensure uniqueness even for same user at same
    // millisecond
    String nonce = UUID.randomUUID().toString().substring(0, 8);

    return JWT.create()
        .withSubject(user.getId().toString())
        .withClaim("username", user.getUsername())
        .withClaim("email", user.getEmail())
        .withClaim("role", user.getRole().getName().name())
        .withClaim("nonce", nonce)
        .withIssuedAt(new Date(now))
        .withExpiresAt(new Date(now + jwtExpiration))
        .sign(getAlgorithm());
  }

  public boolean validateToken(String token) {
    try {
      Algorithm algorithm = Algorithm.HMAC256(jwtSecret);
      JWTVerifier verifier = JWT.require(algorithm).build();
      verifier.verify(token);
      return true;
    } catch (JWTVerificationException e) {
      log.error("Invalid JWT token: {}", e.getMessage());
      return false;
    }
  }

  public String getUserIdFromToken(String token) {
    try {
      Algorithm algorithm = getAlgorithm();
      JWTVerifier verifier = JWT.require(algorithm).build();
      DecodedJWT decodedJWT = verifier.verify(token);
      return decodedJWT.getSubject();
    } catch (JWTVerificationException e) {
      log.error("Error extracting user ID from token: {}", e.getMessage());
      return null;
    }
  }

  public String getUsernameFromToken(String token) {
    return getClaim(token, "username");
  }

  public String getEmailFromToken(String token) {
    return getClaim(token, "email");
  }

  private Algorithm getAlgorithm() {
    return Algorithm.HMAC256(jwtSecret.getBytes(StandardCharsets.UTF_8));
  }

  private String getClaim(String token, String claim) {
    try {
      Algorithm algorithm = getAlgorithm();
      JWTVerifier verifier = JWT.require(algorithm).build();
      DecodedJWT decodedJWT = verifier.verify(token);
      return decodedJWT.getClaim(claim).asString();
    } catch (JWTVerificationException e) {
      log.error("Error extracting claim {} from token: {}", claim, e.getMessage());
      return null;
    }
  }
}
