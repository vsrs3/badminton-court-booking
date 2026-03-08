package com.bcb.exception.singlebooking;

/**
 * Thrown when a required entity is not found (404).
 *
 * @author AnhTN
 */
public class SingleBookingNotFoundException extends RuntimeException {

    private final String code;

    public SingleBookingNotFoundException(String code, String message) {
        super(message);
        this.code = code;
    }

    /** Returns the error code, e.g. NOT_FOUND. */
    public String getCode() {
        return code;
    }
}

