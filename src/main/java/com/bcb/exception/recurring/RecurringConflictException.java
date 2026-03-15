package com.bcb.exception.recurring;

/**
 * Thrown when recurring confirm hits slot conflict (409).
 *
 * @author AnhTN
 */
public class RecurringConflictException extends RuntimeException {

    private final String code;

    public RecurringConflictException(String code, String message) {
        super(message);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}

