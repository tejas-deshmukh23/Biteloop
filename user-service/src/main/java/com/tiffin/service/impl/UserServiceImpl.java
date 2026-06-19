package com.tiffin.service.impl;

import com.tiffin.common.exception.TiffinException;
import com.tiffin.dto.AuthResponse;
import com.tiffin.dto.LoginRequest;
import com.tiffin.dto.RegisterRequest;
import com.tiffin.dto.UpdateProfileRequest;
import com.tiffin.dto.UserProfileResponse;
import com.tiffin.entity.User;
import com.tiffin.repository.UserRepository;
import com.tiffin.security.JwtUtils;
import com.tiffin.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of UserService.
 * Contains all business logic for user operations.
 *
 * @Transactional on class level means every method runs inside
 * a DB transaction automatically. If anything throws an exception,
 * the transaction rolls back — no partial saves.
 *
 * We override with @Transactional(readOnly = true) on read methods
 * for better performance — Postgres can optimize read-only transactions.
 */
@Service
@Transactional
public class UserServiceImpl implements UserService {

    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    // Constructor injection — never use @Autowired on fields
    // Constructor injection makes dependencies explicit and easier to test
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    public UserServiceImpl(UserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           JwtUtils jwtUtils) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
    }

    // ── Register ───────────────────────────────────────────────────────────

    @Override
    public AuthResponse register(RegisterRequest request) {

        log.info("Registering new user with email: {}", request.getEmail());

        // Step 1 — Check email uniqueness
        if (userRepository.existsByEmail(request.getEmail())) {
            throw TiffinException.conflict("Email already registered: " + request.getEmail());
        }

        // Step 2 — Check phone uniqueness
        if (userRepository.existsByPhone(request.getPhone())) {
            throw TiffinException.conflict("Phone number already registered: " + request.getPhone());
        }

        // Step 3 — Build User entity
        // NEVER save raw password — always hash first
        User user = new User(
                request.getName(),
                request.getEmail(),
                passwordEncoder.encode(request.getPassword()),  // BCrypt hash
                request.getPhone(),
                request.getRole()
        );
        user.setAddress(request.getAddress());

        // Step 4 — Save to DB
        // @PrePersist in BaseEntity sets id and createdAt automatically
        User savedUser = userRepository.save(user);
        log.info("User registered successfully with id: {}", savedUser.getId());

        // Step 5 — Generate JWT token
        // User is logged in immediately after registration — no need to login again
        String token = jwtUtils.generateToken(savedUser);

        // Step 6 — Return response
        return new AuthResponse(
                token,
                savedUser.getId(),
                savedUser.getName(),
                savedUser.getEmail(),
                savedUser.getRole()
        );
    }

    // ── Login ──────────────────────────────────────────────────────────────

    @Override
    public AuthResponse login(LoginRequest request) {

        log.info("Login attempt for email: {}", request.getEmail());

        // Step 1 — Find user by email
        // We use a vague error message intentionally —
        // never tell an attacker whether the email exists or the password is wrong
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> TiffinException.unauthorized("Invalid email or password"));

        // Step 2 — Check if account is active
        if (!user.isActive()) {
            throw TiffinException.unauthorized("Account is deactivated. Please contact support.");
        }

        // Step 3 — Verify password
        // BCrypt compares raw password against stored hash
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw TiffinException.unauthorized("Invalid email or password");
        }

        log.info("User logged in successfully: {}", user.getId());

        // Step 4 — Generate and return JWT token
        String token = jwtUtils.generateToken(user);

        return new AuthResponse(
                token,
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole()
        );
    }

    // ── Get Profile ────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)  // read-only for performance
    public UserProfileResponse getUserById(String userId) {

        log.info("Fetching profile for userId: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> TiffinException.notFound("User not found: " + userId));

        return mapToProfileResponse(user);
    }

    // ── Update Profile ─────────────────────────────────────────────────────

    @Override
    public UserProfileResponse updateProfile(String userId, UpdateProfileRequest request) {

        log.info("Updating profile for userId: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> TiffinException.notFound("User not found: " + userId));

        // Only update fields that are provided — null means "don't change"
        if (request.getName() != null && !request.getName().isBlank()) {
            user.setName(request.getName());
        }

        if (request.getPhone() != null && !request.getPhone().isBlank()) {
            // Check new phone isn't already taken by another user
            if (userRepository.existsByPhone(request.getPhone())) {
                throw TiffinException.conflict("Phone number already in use");
            }
            user.setPhone(request.getPhone());
        }

        if (request.getAddress() != null && !request.getAddress().isBlank()) {
            user.setAddress(request.getAddress());
        }

        // @PreUpdate in BaseEntity sets updatedAt automatically
        User updatedUser = userRepository.save(user);
        log.info("Profile updated successfully for userId: {}", userId);

        return mapToProfileResponse(updatedUser);
    }

    // ── Private helpers ────────────────────────────────────────────────────

    /**
     * Converts User entity to UserProfileResponse DTO.
     * Kept private — only used inside this class.
     * Never expose the entity directly from the service layer.
     */
    private UserProfileResponse mapToProfileResponse(User user) {
        return new UserProfileResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getPhone(),
                user.getAddress(),
                user.getRole(),
                user.isActive(),
                user.getCreatedAt()
        );
    }

	@Override
	public void updateProviderId(String userId, String providerId) {
		User user = userRepository.findById(userId)
	            .orElseThrow(() -> new RuntimeException(
	                    "User not found: " + userId));

	    user.setProviderId(providerId);
	    userRepository.save(user);

	    log.info("Updated providerId={} for userId={}", providerId, userId);
	}
}