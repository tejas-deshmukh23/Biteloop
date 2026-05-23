package com.tiffin.exception;

import com.tiffin.common.exception.TiffinException;
import com.tiffin.common.response.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * Centralized exception handling for ALL exceptions thrown
 * anywhere in user-service.
 *
 * @RestControllerAdvice = @ControllerAdvice + @ResponseBody
 * Meaning: intercept exceptions + return JSON responses
 *
 * Without this class:
 * → Spring returns its own ugly error format
 * → Inconsistent with our ApiResponse<T> format
 * → Frontend gets confused
 *
 * With this class:
 * → Every error returns ApiResponse.error() format
 * → Consistent, clean, frontend friendly
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // ── Our Custom Exception ───────────────────────────────────────────────

    /**
     * Handles TiffinException — our custom exception thrown
     * intentionally from service layer.
     *
     * Examples:
     * TiffinException.conflict("Email already registered")    → 409
     * TiffinException.notFound("User not found")              → 404
     * TiffinException.unauthorized("Invalid credentials")     → 401
     * TiffinException.badRequest("Invalid input")             → 400
     */
    @ExceptionHandler(TiffinException.class)
    public ResponseEntity<ApiResponse<Object>> handleTiffinException(
            TiffinException ex) {

        log.warn("TiffinException: {}", ex.getMessage());

        return ResponseEntity
                .status(ex.getStatus())
                .body(ApiResponse.error(ex.getMessage()));
    }

    // ── Validation Exception ───────────────────────────────────────────────

    /**
     * Handles @Valid validation failures.
     *
     * Triggered when request body fails validation like:
     * @NotBlank, @Email, @Size, @Pattern
     *
     * Example:
     * POST /api/users/register with email = "notanemail"
     * → MethodArgumentNotValidException
     * → returns map of field → error message
     *
     * Response:
     * {
     *   "success": false,
     *   "message": "Validation failed",
     *   "data": {
     *     "email": "Invalid email format",
     *     "phone": "Invalid Indian phone number"
     *   }
     * }
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>>
    handleValidationException(MethodArgumentNotValidException ex) {

        // Collect all field errors into a map
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult()
                .getAllErrors()
                .forEach(error -> {
                    String fieldName = ((FieldError) error).getField();
                    String errorMessage = error.getDefaultMessage();
                    errors.put(fieldName, errorMessage);
                });

        log.warn("Validation failed: {}", errors);

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.<Map<String, String>>builder()
                        .success(false)
                        .message("Validation failed")
                        .data(errors)
                        .build());
    }

    // ── Spring Security Exceptions ─────────────────────────────────────────

    /**
     * Handles AccessDeniedException.
     * Thrown when authenticated user tries to access
     * an endpoint they don't have permission for.
     *
     * Example:
     * CUSTOMER tries to hit /api/users/admin/**
     * → 403 Forbidden
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Object>> handleAccessDeniedException(
            AccessDeniedException ex) {

        log.warn("Access denied: {}", ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error(
                        "Access denied. You don't have permission to perform this action."));
    }

    /**
     * Handles BadCredentialsException.
     * Thrown by Spring Security when password is wrong
     * during authentication.
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<Object>> handleBadCredentialsException(
            BadCredentialsException ex) {

        log.warn("Bad credentials attempt");

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("Invalid email or password"));
    }

    // ── Fallback Exception ─────────────────────────────────────────────────

    /**
     * Catches ANY exception not handled by the above methods.
     * This is the safety net.
     *
     * IMPORTANT: We log the full stack trace here
     * but return a GENERIC message to the client.
     *
     * Never expose internal error details to the client —
     * stack traces reveal your code structure to attackers.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleGenericException(
            Exception ex) {

        // Log full stack trace internally for debugging
        log.error("Unexpected error occurred: {}", ex.getMessage(), ex);

        // Return generic message to client — never expose internals
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(
                        "Something went wrong. Please try again later."));
    }
}