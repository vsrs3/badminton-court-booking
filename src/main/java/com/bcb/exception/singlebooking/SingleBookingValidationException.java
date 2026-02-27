package com.bcb.exception.singlebooking;

import java.util.List;
import java.util.Map;

/**
 * Thrown when single-booking request validation fails (400).
 *
 * @author AnhTN
 */
public class SingleBookingValidationException extends RuntimeException {

    private final String code;
    private final List<Map<String, Object>> details;

    public SingleBookingValidationException(String code, String message) {
        super(message);
        this.code = code;
        this.details = null;
    }

    public SingleBookingValidationException(String code, String message, List<Map<String, Object>> details) {
        super(message);
        this.code = code;
        this.details = details;
    }

    /** Returns the error code, e.g. PAST_DATE, MIN_DURATION_60M. */
    public String getCode() {
        return code;
    }

    /** Returns detail list (may be null). */
    public List<Map<String, Object>> getDetails() {
        return details;
    }
}
