package com.tiffin.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.nio.charset.StandardCharsets;

/**
 * Handles JWT token parsing and validation.
 * Same secret as user-service — tokens issued there, validated here.
 * Gateway never issues tokens — only validates them.
 */
@Component
public class JwtUtil {

    private final Key signingKey;

    public JwtUtil(@Value("${jwt.secret}") String secret) {
        // Build the signing key from the same secret user-service uses
        this.signingKey = Keys.hmacShaKeyFor(
                secret.getBytes(StandardCharsets.UTF_8)
        );
    }

    /**
     * Validate token signature and expiry.
     * Returns true if valid, false if expired or tampered.
     */
    public boolean isValid(String token) {
        try {
            getClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Extract userId (subject) from token.
     * user-service stores userId as the JWT subject.
     */
    public String getUserId(String token) {
        return getClaims(token).getSubject();
    }

    /**
     * Extract role from token claims.
     * user-service stores role as "role" claim.
     */
    public String getRole(String token) {
        return getClaims(token).get("role", String.class);
    }

    // ── Private ────────────────────────────────────────────────

    private Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}