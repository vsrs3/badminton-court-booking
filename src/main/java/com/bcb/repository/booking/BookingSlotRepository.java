package com.bcb.repository.booking;

import com.bcb.model.BookingSlot;

import java.sql.Connection;
import java.util.List;

/**
 * Repository interface for BookingSlot CRUD in single-booking context.
 *
 * @author AnhTN
 */
public interface BookingSlotRepository {

    /**
     * Inserts a booking slot (within a transaction). Returns generated bookingSlotId.
     */
    int insertBookingSlot(Connection conn, BookingSlot bookingSlot);

    /**
     * Finds all booking_slot_ids for a given bookingId. Used for cleanup.
     */
    List<Integer> findBookingSlotIdsByBookingId(Connection conn, int bookingId);
}

