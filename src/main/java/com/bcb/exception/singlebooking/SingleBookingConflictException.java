package com.bcb.exception.singlebooking;

/**
 * Thrown when a slot conflict occurs (409), e.g. double-booking.
 *
 * @author AnhTN
 */
public class SingleBookingConflictException extends RuntimeException {

    private final String code;

    public SingleBookingConflictException(String code, String message) {
        super(message);
        this.code = code;
    }

    /** Returns the error code, e.g. SLOT_CONFLICT. */
    public String getCode() {
        return code;
    }
}
