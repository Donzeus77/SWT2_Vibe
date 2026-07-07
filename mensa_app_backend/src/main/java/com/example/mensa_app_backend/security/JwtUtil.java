package com.example.mensa_app_backend.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

@Component
public class JwtUtil {

    private final SecretKey key;
    private final Duration expiration;

    public JwtUtil(@Value("${jwt.secret}") String secret,
                   @Value("${jwt.expiration-days:30}") long expirationDays) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expiration = Duration.ofDays(expirationDays);
    }

    public String generateToken(Long userId, String email, String type) {
        return Jwts.builder()
                .subject(userId.toString())
                .claim("email", email)
                .claim("type", type)
                .issuedAt(Date.from(Instant.now()))
                .expiration(Date.from(Instant.now().plus(expiration)))
                .signWith(key)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public Long extractUserId(String token) {
        return Long.parseLong(Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject());
    }
}
