package com.tiffin.security;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.tiffin.entity.User;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;

/**
 * Handles everything related to JWT tokens:
 *  - Generating a token after login/register
 *  - Validating a token received in a request
 *  - Extracting claims (userId, role, email) from a token
 *
 * @Component — makes it a Spring bean so it can be injected
 * anywhere via constructor injection.
 *
 * This class is used by:
 *  - UserServiceImpl  (generates token after login/register)
 *  - SecurityConfig   (validates token on every protected request)
 *  - API Gateway      (same logic mirrored there for validation)
 */

@Component
public class JwtUtils {
	
	private static final Logger log = LoggerFactory.getLogger(JwtUtils.class);
	
	/**
     * Secret key read from application.yml
     * jwt:
     *   secret: your-256-bit-secret
     *   expiration: 86400000
     *
     * @Value injects the value directly from config.
     * Never hardcode secrets in source code.
     */
	
	@Value("${jwt.secret}")
	private String jwtSecret;
	
	@Value("${jwt.expiration}")
	private long jwtExpirationMs; // 86400000 = 24 hours in milliseconds
	
	// -- generate token------------------------------------------------
	
	/**
     * Generates a JWT token for the given user.
     *
     * Token structure (decoded):
     * Header:  { "alg": "HS256" }
     * Payload: {
     *   "sub": "usr_550e8400...",     ← userId (subject)
     *   "email": "raj@gmail.com",
     *   "role": "CUSTOMER",
     *   "iat": 1234567890,            ← issued at (auto)
     *   "exp": 1234654290             ← expiry (auto)
     * }
     * Signature: HMACSHA256(base64(header) + base64(payload), secret)
     */
	
	public String generateToken(User user) {
		
		Map<String, Object> claims = new HashMap<>();
		// We embed email and role in the token so the gateway
        // and other services can read them without hitting the DB
		
		claims.put("email", user.getEmail());
		claims.put("role", user.getRole().name());
		
		return Jwts.builder()
				.setClaims(claims)
				.setSubject(user.getId())
				.setIssuedAt(new Date())
				.setExpiration(new Date(System.currentTimeMillis()+jwtExpirationMs))
				.signWith(getSigningKey(), SignatureAlgorithm.HS256)
				.compact();
		
	}
	
	//--------------------Validate Token-----------------------------
	
	/**
     * Validates the token.
     * Returns true if valid, false if expired/tampered/malformed.
     *
     * We catch each exception separately so we can log
     * exactly WHY the token failed — useful for debugging.
     */
	
	public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
 
        } catch (ExpiredJwtException e) {
            log.warn("JWT token is expired: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.warn("JWT token is malformed: {}", e.getMessage());
        } catch (SecurityException e) {
            log.warn("JWT signature is invalid: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.warn("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.warn("JWT claims string is empty: {}", e.getMessage());
        }
        return false;
    }
	
	 // ── Extract Claims ─────────────────────────────────────────────────────
	 
    /**
     * Extracts userId from token.
     * userId is stored as the "subject" claim.
     * This is what the gateway forwards as X-User-Id header.
     */
    public String getUserIdFromToken(String token) {
        return extractAllClaims(token).getSubject();
    }
 
    /**
     * Extracts email from token claims.
     */
    public String getEmailFromToken(String token) {
        return (String) extractAllClaims(token).get("email");
    }
 
    /**
     * Extracts role from token claims.
     */
    public String getRoleFromToken(String token) {
        return (String) extractAllClaims(token).get("role");
    }
    
 // ── Private Helpers ────────────────────────────────────────────────────
    
    /**
     * Parses and returns all claims from the token.
     * Called internally by the extract methods above.
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
	
	/**
     * Converts the plain string secret from application.yml
     * into a cryptographic Key object that JJWT requires.
     *
     * Keys.hmacShaKeyFor() ensures the key is valid for HS256.
     */
    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

}
