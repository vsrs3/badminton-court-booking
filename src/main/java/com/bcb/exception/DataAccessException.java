package com.bcb.exception;

/**
 * Custom exception for data access errors.
 * Thrown when database operations fail.
 */
public class DataAccessException extends RuntimeException {

    public DataAccessException(String message) {
        super(message);
    }

    public DataAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}
