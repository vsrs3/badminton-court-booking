package com.bcb.service.singlebooking;

/**
 * Service interface for cleaning up expired PENDING booking holds.
 *
 * @author AnhTN
 */
public interface SingleBookingCleanupService {

    /**
     * Expires PENDING bookings whose hold_expired_at &lt;= now,
     * sets status to EXPIRED and releases CourtSlotBooking locks.
     */
    void cleanupExpiredHolds();
}
