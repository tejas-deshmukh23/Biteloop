package com.tiffin.controller;

import com.tiffin.common.response.ApiResponse;
import com.tiffin.dto.UpdateProfileRequest;
import com.tiffin.dto.UserProfileResponse;
import com.tiffin.service.UserService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Handles all user profile related endpoints.
 * These are PROTECTED endpoints — valid JWT token required.
 *
 * Important: We get the userId from the request header "X-User-Id"
 * NOT from the JWT token directly.
 *
 * Why? Because API Gateway validates the JWT token and
 * extracts userId from it, then forwards it as "X-User-Id" header.
 * This way user-service never needs to parse JWT itself on
 * protected endpoints — gateway already did the work.
 *
 * Base URL: /api/users
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // ── Get My Profile ─────────────────────────────────────────────────────

    /**
     * GET /api/users/me
     *
     * Returns profile of the currently logged in user.
     * JWT token required in Authorization header.
     *
     * X-User-Id header is injected by API Gateway after
     * validating the JWT token. Controllers just read it.
     *
     * Response: 200 OK
     * {
     *   "success": true,
     *   "message": "Success",
     *   "data": {
     *     "id": "usr_550e8400...",
     *     "name": "Raj Kumar",
     *     "email": "raj@gmail.com",
     *     "phone": "9876543210",
     *     "address": "Pune, Maharashtra",
     *     "role": "CUSTOMER",
     *     "isActive": true,
     *     "createdAt": "2024-01-15T10:23:45"
     *   }
     * }
     */
    @GetMapping("/me")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'PROVIDER', 'ADMIN')")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getMyProfile(
            @RequestHeader("X-User-Id") String userId) {

        log.info("Get profile request for userId: {}", userId);

        UserProfileResponse profile = userService.getUserById(userId);

        return ResponseEntity.ok(ApiResponse.success(profile));
    }

    // ── Update My Profile ──────────────────────────────────────────────────

    /**
     * PUT /api/users/me
     *
     * Updates name, phone, and/or address of logged in user.
     * Email and role are NOT updatable after registration.
     * Only send fields you want to update — others are ignored.
     *
     * Request body (all fields optional):
     * {
     *   "name": "Raj Kumar Updated",
     *   "phone": "9123456789",
     *   "address": "Mumbai, Maharashtra"
     * }
     *
     * Response: 200 OK
     * {
     *   "success": true,
     *   "message": "Profile updated successfully",
     *   "data": { ...updated profile... }
     * }
     */
    @PutMapping("/me")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'PROVIDER', 'ADMIN')")
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateMyProfile(
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody UpdateProfileRequest request) {

        log.info("Update profile request for userId: {}", userId);

        UserProfileResponse updatedProfile = userService.updateProfile(userId, request);

        return ResponseEntity.ok(ApiResponse.success(
                "Profile updated successfully",
                updatedProfile));
    }
    
    /**
     * Internal endpoint — called by provider-service after provider registration.
     * NOT exposed publicly — only accessible within Docker network.
     * TODO: Replace with Kafka event in Milestone 8 (PROVIDER_REGISTERED event)
     */
    @PutMapping("/internal/update-provider-id")
    public ResponseEntity<ApiResponse<Void>> updateProviderId(
            @RequestParam String userId,
            @RequestParam String providerId) {

        userService.updateProviderId(userId, providerId);
        return ResponseEntity.ok(ApiResponse.success("Provider ID updated", null));
    }
}