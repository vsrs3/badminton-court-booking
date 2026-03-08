package com.bcb.exception.singlebooking;

/**
 * Thrown when user is not authenticated (401).
 *
 * @author AnhTN
 */
public class SingleBookingUnauthorizedException extends RuntimeException {

    private final String code;

    public SingleBookingUnauthorizedException(String code, String message) {
        super(message);
        this.code = code;
    }

    /** Returns the error code, e.g. UNAUTHORIZED. */
    public String getCode() {
        return code;
    }
}

