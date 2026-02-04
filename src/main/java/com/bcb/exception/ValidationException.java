package com.bcb.exception;

/**
 * Custom exception for validation errors.
 * Thrown when input validation fails.
 */
public class ValidationException extends BusinessException {

    public ValidationException(String message) {
        super("VALIDATION_ERROR", message);
    }

}

