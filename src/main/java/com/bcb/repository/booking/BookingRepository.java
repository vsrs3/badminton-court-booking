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
}
