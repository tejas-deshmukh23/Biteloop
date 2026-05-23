package com.tiffin.controller;

import com.tiffin.common.response.ApiResponse;
import com.tiffin.dto.AuthResponse;
import com.tiffin.dto.LoginRequest;
import com.tiffin.dto.RegisterRequest;
import com.tiffin.service.UserService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Handles all authentication related endpoints.
 * These are PUBLIC endpoints — no JWT token required.
 * Configured as permitAll() in SecurityConfig.
 *
 * Base URL: /api/users
 */
@RestController
@RequestMapping("/api/users")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    // Constructor injection — depends on interface not implementation
    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    // ── Register ───────────────────────────────────────────────────────────

    /**
     * POST /api/users/register
     *
     * Register a new user (customer or provider).
     * Returns JWT token immediately so user is logged in after register.
     *
     * @Valid triggers validation on RegisterRequest fields:
     * @NotBlank, @Email, @Size, @Pattern
     * If validation fails → GlobalExceptionHandler catches it → 400
     *
     * Request body:
     * {
     *   "name": "Raj Kumar",
     *   "email": "raj@gmail.com",
     *   "password": "secret123",
     *   "phone": "9876543210",
     *   "address": "Pune, Maharashtra",
     *   "role": "CUSTOMER"
     * }
     *
     * Response: 201 Created
     * {
     *   "success": true,
     *   "message": "User registered successfully",
     *   "data": {
     *     "token": "eyJhbGci...",
     *     "userId": "usr_550e8400...",
     *     "name": "Raj Kumar",
     *     "email": "raj@gmail.com",
     *     "role": "CUSTOMER"
     *   }
     * }
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @Valid @RequestBody RegisterRequest request) {

        log.info("Register request received for email: {}", request.getEmail());

        AuthResponse authResponse = userService.register(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)   // 201 — resource created
                .body(ApiResponse.success(
                        "User registered successfully",
                        authResponse));
    }

    // ── Login ──────────────────────────────────────────────────────────────

    /**
     * POST /api/users/login
     *
     * Authenticate existing user and return JWT token.
     *
     * Request body:
     * {
     *   "email": "raj@gmail.com",
     *   "password": "secret123"
     * }
     *
     * Response: 200 OK
     * {
     *   "success": true,
     *   "message": "Login successful",
     *   "data": {
     *     "token": "eyJhbGci...",
     *     "userId": "usr_550e8400...",
     *     "name": "Raj Kumar",
     *     "email": "raj@gmail.com",
     *     "role": "CUSTOMER"
     *   }
     * }
     *
     * Failure: 401 Unauthorized
     * {
     *   "success": false,
     *   "message": "Invalid email or password"
     * }
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request) {

        log.info("Login request received for email: {}", request.getEmail());

        AuthResponse authResponse = userService.login(request);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(
                        "Login successful",
                        authResponse));
    }
}