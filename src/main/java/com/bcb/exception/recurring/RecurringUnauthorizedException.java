package com.bcb.exception.recurring;

/**
 * Thrown when recurring confirm requires login (401).
 *
 * @author AnhTN
 */
public class RecurringUnauthorizedException extends RuntimeException {

    private final String code;

    public RecurringUnauthorizedException(String code, String message) {
        super(message);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}

