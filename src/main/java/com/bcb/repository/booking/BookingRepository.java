package com.bcb.repository.booking;

import com.bcb.model.Booking;

import java.sql.Connection;
import java.util.List;

/**
 * Repository interface for Booking CRUD in single-booking context.
 *
 * @author AnhTN
 */
public interface BookingRepository {

    /**
     * Inserts a booking (within a transaction). Returns generated bookingId.
     */
    int insertBooking(Connection conn, Booking booking);

    /**
     * Updates booking status by bookingId (within a transaction).
     */
    void updateStatus(Connection conn, int bookingId, String newStatus);

    /**
     * Finds PENDING bookings whose hold has expired.
     */
    List<Booking> findExpiredPendingBookings();

    /**
     * Verifies booking belongs to a given account and returns [bookingStatus, facilityName].
     * Returns null if not found or not owned by accountId.
     *
     * @author AnhTN
     */
    String[] findBookingOwnershipInfo(int bookingId, int accountId);

    /**
     * Extends hold_expired_at for a PENDING booking.
     *
     * @param bookingId       the booking to extend hold for
     * @param newHoldExpireAt the new hold expiry timestamp
     * @author AnhTN
     */
    void extendHold(int bookingId, java.time.LocalDateTime newHoldExpireAt);
}
