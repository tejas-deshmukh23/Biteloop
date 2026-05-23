package com.tiffin.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Runs once per request (OncePerRequestFilter guarantees this).
 *
 * What it does on every request:
 * 1. Reads the Authorization header
 * 2. Extracts the JWT token
 * 3. Validates the token
 * 4. If valid — loads user and sets authentication in SecurityContext
 * 5. If invalid — does nothing (request will be rejected by SecurityConfig)
 *
 * Flow:
 * Request → JwtAuthFilter → SecurityConfig rules → Controller
 */
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthFilter.class);

    private final JwtUtils jwtUtils;
    private final CustomUserDetailsService userDetailsService;

    public JwtAuthFilter(JwtUtils jwtUtils,
                         CustomUserDetailsService userDetailsService) {
        this.jwtUtils = jwtUtils;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        try {
            // Step 1 — Extract JWT from Authorization header
            String jwt = extractJwtFromRequest(request);

            // Step 2 — Validate token
            if (StringUtils.hasText(jwt) && jwtUtils.validateToken(jwt)) {

                // Step 3 — Extract email from token
                String email = jwtUtils.getEmailFromToken(jwt);

                // Step 4 — Load user details from DB
                UserDetails userDetails = userDetailsService.loadUserByUsername(email);

                // Step 5 — Create authentication object
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,  // credentials null — already authenticated via JWT
                                userDetails.getAuthorities()
                        );

                authentication.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );

                // Step 6 — Set authentication in SecurityContext
                // This tells Spring Security "this request is authenticated"
                // Controllers can now access the current user via SecurityContextHolder
                SecurityContextHolder.getContext().setAuthentication(authentication);

                log.debug("Authenticated user: {}", email);
            }

        } catch (Exception e) {
            // Don't throw — just log and let the request continue
            // SecurityConfig will reject it if authentication wasn't set
            log.error("Cannot set user authentication: {}", e.getMessage());
        }

        // Always continue the filter chain — never block here
        // SecurityConfig decides whether to allow or reject
        filterChain.doFilter(request, response);
    }

    /**
     * Extracts JWT token from the Authorization header.
     *
     * Expected header format:
     * Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c3...
     *
     * We strip "Bearer " prefix and return just the token string.
     */
    private String extractJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");

        // Check header exists and starts with "Bearer "
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);  // remove "Bearer " (7 characters)
        }

        return null;
    }
}