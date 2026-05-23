package com.tiffin.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Central Spring Security configuration for user-service.
 *
 * Key decisions made here:
 *  1. Which endpoints are public vs protected
 *  2. Session management — STATELESS (JWT, no server-side sessions)
 *  3. Password encoding — BCrypt
 *  4. Where our JwtAuthFilter fits in the filter chain
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity   // enables @PreAuthorize on controller methods
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final JwtAuthFilter jwtAuthFilter;

    public SecurityConfig(CustomUserDetailsService userDetailsService,
                          JwtAuthFilter jwtAuthFilter) {
        this.userDetailsService = userDetailsService;
        this.jwtAuthFilter = jwtAuthFilter;
    }

    /**
     * Main security filter chain — defines all security rules.
     *
     * Request flow:
     * Incoming Request
     *   → JwtAuthFilter          (validates JWT, sets authentication)
     *   → UsernamePasswordFilter (skipped — we use JWT not form login)
     *   → SecurityFilterChain    (checks if endpoint is allowed)
     *   → Controller
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
            // Disable CSRF — not needed for REST APIs using JWT
            // CSRF protects browser-based form submissions with sessions
            // We use stateless JWT so CSRF is irrelevant
            .csrf(AbstractHttpConfigurer::disable)

            // Define which endpoints are public and which require auth
            .authorizeHttpRequests(auth -> auth

                // Public endpoints — no token required
                .requestMatchers(
                        "/api/users/register",
                        "/api/users/login"
                ).permitAll()

                // Admin only endpoints
                .requestMatchers("/api/users/admin/**")
                        .hasRole("ADMIN")

                // All other endpoints require a valid JWT token
                .anyRequest().authenticated()
            )

            // STATELESS — no HTTP sessions, no cookies
            // Every request must carry its own JWT token
            // Server never stores session state
            .sessionManagement(session -> session
                    .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            // Register our authentication provider
            // This tells Spring Security to use our UserDetailsService + BCrypt
            .authenticationProvider(authenticationProvider())

            // Add our JwtAuthFilter BEFORE Spring's default
            // UsernamePasswordAuthenticationFilter in the chain
            // This ensures JWT is validated before any other security check
            .addFilterBefore(jwtAuthFilter,
                    UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * BCrypt password encoder.
     * BCrypt automatically handles salting — no need to manage salts manually.
     * Strength 12 = 2^12 hashing rounds (good balance of security vs speed).
     * Default strength is 10 — 12 is slightly more secure, still fast enough.
     *
     * This bean is injected into UserServiceImpl for hashing passwords.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    /**
     * Wires together our UserDetailsService and PasswordEncoder.
     * Spring Security uses this to authenticate users —
     * it loads the user via UserDetailsService then
     * verifies the password via PasswordEncoder.
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    /**
     * AuthenticationManager bean — needed if you ever want to
     * programmatically trigger authentication (e.g. in login flow).
     */
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}