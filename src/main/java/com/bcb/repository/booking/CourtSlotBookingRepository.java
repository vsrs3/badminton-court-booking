package com.bcb.repository.booking;

import java.sql.Connection;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Repository interface for querying booked (locked) slots in single-booking context.
 * Uses CourtSlotBooking table as the lock table.
 *
 * @author AnhTN
 */
public interface CourtSlotBookingRepository {

    /**
     * Finds all booked slot IDs grouped by courtId for a facility on a date.
     * Uses standalone connection.
     */
    Map<Integer, List<Integer>> findBookedSlots(int facilityId, LocalDate bookingDate);

    /**
     * Inserts a court-slot lock row (within a transaction).
     *
     * @param conn          shared transaction connection
     * @param courtId       court ID
     * @param bookingDate   booking date
     * @param slotId        slot ID
     * @param bookingSlotId booking_slot_id FK
     */
    void insertLock(Connection conn, int courtId, LocalDate bookingDate, int slotId, int bookingSlotId);

    /**
     * Deletes lock rows by bookingSlotIds (within a transaction, used for cleanup).
     */
    void deleteByBookingSlotIds(Connection conn, List<Integer> bookingSlotIds);
}
