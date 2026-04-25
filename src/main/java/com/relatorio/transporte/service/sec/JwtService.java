package com.relatorio.transporte.service.sec;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

@Service
public class JwtService {

    @Value("${app.jwt.secret}")
    private String secret;

    @Value("${app.jwt.expiration-ms}")
    private long expirationMs;

    @Value("${app.jwt.refresh-expiration-ms}")
    private long refreshExpirationMs;

    // ─────────────────────────────
    // GENERATE TOKEN
    // ─────────────────────────────

    public String generateAccessToken(UserPrincipal user) {
        return buildToken(
                Map.of(
                        "role", user.getRole(),
                        "name", user.getName()
                ),
                user.getId().toString(),
                expirationMs
        );
    }

    public String generateRefreshToken(UserPrincipal user) {
        return buildToken(
                Map.of("type", "refresh"),
                user.getId().toString(),
                refreshExpirationMs
        );
    }

    private String buildToken(Map<String, Object> claims, String subject, long expMs) {
        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expMs))
                .signWith(getSignKey(), Jwts.SIG.HS256)
                .compact();
    }

    // ─────────────────────────────
    // VALIDATION
    // ─────────────────────────────

    public boolean isValid(String token, String userId) {
        try {
            String subject = extractSubject(token);
            return subject.equals(userId) && !isExpired(token);
        } catch (JwtException e) {
            return false;
        }
    }

    public boolean isExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }

    // ─────────────────────────────
    // EXTRACT
    // ─────────────────────────────

    public String extractSubject(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public UUID extractUserId(String token) {
        return UUID.fromString(extractSubject(token));
    }

    public String extractRole(String token) {
        return extractClaim(token, claims -> claims.get("role", String.class));
    }

    public <T> T extractClaim(String token, Function<Claims, T> resolver) {
        return resolver.apply(extractAllClaims(token));
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSignKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSignKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }
}