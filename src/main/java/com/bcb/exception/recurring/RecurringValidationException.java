package com.bcb.exception.recurring;

import java.util.List;
import java.util.Map;

/**
 * Thrown when recurring-booking request validation fails (400).
 *
 * @author AnhTN
 */
public class RecurringValidationException extends RuntimeException {

    private final String code;
    private final List<Map<String, Object>> details;

    public RecurringValidationException(String code, String message) {
        super(message);
        this.code = code;
        this.details = null;
    }

    public RecurringValidationException(String code, String message, List<Map<String, Object>> details) {
        super(message);
        this.code = code;
        this.details = details;
    }

    public String getCode() {
        return code;
    }

    public List<Map<String, Object>> getDetails() {
        return details;
    }
}

