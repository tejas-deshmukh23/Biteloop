package com.tiffin.common.exception;

import org.springframework.http.HttpStatus;

/**
 * Single exception class used across all services.
 * Services throw this; each service's GlobalExceptionHandler catches it.
 */
public class TiffinException extends RuntimeException {

    private final HttpStatus status;

    public TiffinException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public static TiffinException notFound(String message) {
        return new TiffinException(message, HttpStatus.NOT_FOUND);
    }

    public static TiffinException badRequest(String message) {
        return new TiffinException(message, HttpStatus.BAD_REQUEST);
    }

    public static TiffinException unauthorized(String message) {
        return new TiffinException(message, HttpStatus.UNAUTHORIZED);
    }

    public static TiffinException conflict(String message) {
        return new TiffinException(message, HttpStatus.CONFLICT);
    }

    public HttpStatus getStatus() {
        return status;
    }
}
