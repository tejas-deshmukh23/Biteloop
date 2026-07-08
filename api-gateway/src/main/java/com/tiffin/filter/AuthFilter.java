package com.tiffin.filter;

import com.tiffin.config.PublicEndpointsProperties;
import com.tiffin.util.JwtUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * AuthFilter — applied to every route in application.yml
 *
 * Flow:
 * 1. Check if request path is public — if yes, skip auth and forward
 * 2. Extract Authorization header
 * 3. Validate JWT token
 * 4. Extract userId and role from token
 * 5. Inject X-User-Id and X-User-Role headers
 * 6. Remove original Authorization header (services don't need it)
 * 7. Forward request to downstream service
 *
 * If token is missing or invalid — return 401, request never reaches service.
 */
@Component
public class AuthFilter extends AbstractGatewayFilterFactory<AuthFilter.Config> {

    private final JwtUtil jwtUtil;
    private final List<String> publicEndpoints;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    public AuthFilter(JwtUtil jwtUtil,
    		PublicEndpointsProperties publicEndpointsProperties) {
        super(Config.class);
        this.jwtUtil = jwtUtil;
        this.publicEndpoints = publicEndpointsProperties.getPublicEndpoints();
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String path = request.getPath().value();
            
         // Block internal endpoints from being accessed through gateway
            if (path.contains("/internal/")) {
                return unauthorized(exchange, "Access denied");
            }

            // Step 1 — Check if this is a public endpoint
            if (isPublicEndpoint(path)) {
                return chain.filter(exchange); // skip auth entirely
            }

            // Step 2 — Extract Authorization header
            String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return unauthorized(exchange, "Missing or invalid Authorization header");
            }

            String token = authHeader.substring(7); // remove "Bearer " prefix

            // Step 3 — Validate token
            if (!jwtUtil.isValid(token)) {
                return unauthorized(exchange, "Invalid or expired token");
            }

            // Step 4 — Extract user info
            String userId = jwtUtil.getUserId(token);
            String role = jwtUtil.getRole(token);
            String providerId = jwtUtil.getProviderId(token);
            String email = jwtUtil.getEmail(token); //we will need this email for notification so adding it here on date 06/07/2026

//            // Step 5 — Inject headers + remove Authorization
//            ServerHttpRequest mutatedRequest = request.mutate()
//                    .header("X-User-Id", userId)
//                    .header("X-User-Role", role)
//                    .header(HttpHeaders.AUTHORIZATION, "") // remove JWT from downstream
//                    .build();
            
         // Build mutated request
            ServerHttpRequest.Builder requestBuilder = request.mutate()
                    .header("X-User-Id", userId)
                    .header("X-User-Role", role)
                    .header("X-User-Email", email)
                    .header(HttpHeaders.AUTHORIZATION, "");

            // Only inject X-Provider-Id if present (PROVIDER role)
            if (providerId != null) {
                requestBuilder.header("X-Provider-Id", providerId);
            }

            ServerHttpRequest mutatedRequest = requestBuilder.build();

            // Step 6 — Forward modified request
            return chain.filter(exchange.mutate().request(mutatedRequest).build());
        };
    }

    // ── Helpers ────────────────────────────────────────────────

    private boolean isPublicEndpoint(String path) {
        return publicEndpoints.stream()
                .anyMatch(pattern -> pathMatcher.match(pattern, path));
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().add("Content-Type", "application/json");

        // Return consistent JSON error matching our ApiResponse shape
        String body = """
                {
                    "success": false,
                    "message": "%s",
                    "timestamp": "%s"
                }
                """.formatted(message, java.time.LocalDateTime.now());

        org.springframework.core.io.buffer.DataBuffer buffer =
                response.bufferFactory().wrap(body.getBytes());

        return response.writeWith(Mono.just(buffer));
    }

    // Config class required by AbstractGatewayFilterFactory
    // Empty because our filter needs no per-route configuration
    public static class Config {}
}