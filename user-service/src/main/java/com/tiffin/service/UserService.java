package com.tiffin.service;

import com.tiffin.dto.AuthResponse;
import com.tiffin.dto.LoginRequest;
import com.tiffin.dto.RegisterRequest;
import com.tiffin.dto.UpdateProfileRequest;
import com.tiffin.dto.UserProfileResponse;

/**
 * Contract for all user-related operations.
 * Controller depends on this interface, not the implementation.
 * This makes the controller easy to test by mocking this interface.
 */
public interface UserService {

    /**
     * Registers a new user.
     * - Validates email and phone uniqueness
     * - Hashes the password
     * - Saves user to DB
     * - Publishes user.registered Kafka event
     * - Returns JWT token so user is logged in immediately after register
     */
    AuthResponse register(RegisterRequest request);

    /**
     * Authenticates an existing user.
     * - Finds user by email
     * - Verifies BCrypt password
     * - Returns JWT token on success
     */
    AuthResponse login(LoginRequest request);

    /**
     * Fetches the profile of the currently logged-in user.
     * userId comes from the JWT token extracted by the gateway
     * and forwarded as X-User-Id header.
     */
    UserProfileResponse getUserById(String userId);

    /**
     * Updates name, phone, address of the logged-in user.
     * Email and role are never updatable after registration.
     */
    UserProfileResponse updateProfile(String userId, UpdateProfileRequest request);
}