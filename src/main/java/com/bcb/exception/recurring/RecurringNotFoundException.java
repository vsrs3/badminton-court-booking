package com.bcb.exception.recurring;

/**
 * Thrown when recurring data/entity is not found (404).
 *
 * @author AnhTN
 */
public class RecurringNotFoundException extends RuntimeException {

    private final String code;

    public RecurringNotFoundException(String code, String message) {
        super(message);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}

