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

import java.util.Date;
import java.util.UUID;

@Service
@Slf4j
public class JwtService {
    
    @Value("${app.jwt.secret:myaichat-secret-key-placeholder}")
    private String jwtSecret;
    
    @Value("${app.jwt.expiration:86400000}") // 24 hours
    private long jwtExpiration;
    
    public String generateToken(User user) {
        Algorithm algorithm = Algorithm.HMAC256(jwtSecret);
        
        long now = System.currentTimeMillis();
        // Add a random nonce to ensure uniqueness even for same user at same millisecond
        String nonce = UUID.randomUUID().toString().substring(0, 8);
        
        return JWT.create()
                .withSubject(user.getId().toString())
                .withClaim("username", user.getUsername())
                .withClaim("email", user.getEmail())
                .withClaim("role", user.getRole().getName().name())
                .withClaim("nonce", nonce)
                .withIssuedAt(new Date(now))
                .withExpiresAt(new Date(now + jwtExpiration))
                .sign(algorithm);
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
    
    public UUID getUserIdFromToken(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(jwtSecret);
            JWTVerifier verifier = JWT.require(algorithm).build();
            DecodedJWT decodedJWT = verifier.verify(token);
            return UUID.fromString(decodedJWT.getSubject());
        } catch (JWTVerificationException e) {
            log.error("Error extracting user ID from token: {}", e.getMessage());
            return null;
        }
    }
    
    public String getUsernameFromToken(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(jwtSecret);
            JWTVerifier verifier = JWT.require(algorithm).build();
            DecodedJWT decodedJWT = verifier.verify(token);
            return decodedJWT.getClaim("username").asString();
        } catch (JWTVerificationException e) {
            log.error("Error extracting username from token: {}", e.getMessage());
            return null;
        }
    }
}
