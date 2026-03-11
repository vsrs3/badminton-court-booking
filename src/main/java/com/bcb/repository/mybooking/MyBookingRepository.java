package com.bcb.repository.mybooking;

import com.bcb.dto.mybooking.BookingSlotDetailDTO;
import com.bcb.dto.mybooking.MyBookingDetailDTO;
import com.bcb.dto.mybooking.MyBookingListDTO;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for customer's "My Bookings" feature.
 * All methods use their own connections (read-only queries).
 */
public interface MyBookingRepository {

    /**
     * Finds all bookings for a customer with optional filters.
     *
     * @param accountId  the customer's account ID
     * @param status     booking status filter (null = all)
     * @param dateFrom   date range start (null = no lower bound)
     * @param dateTo     date range end (null = no upper bound)
     * @return list of booking summary DTOs
     */
    List<MyBookingListDTO> findMyBookings(int accountId, String status,
                                          LocalDate dateFrom, LocalDate dateTo);

    /**
     * Finds detailed booking info including facility, slots, invoice.
     *
     * @param bookingId  the booking ID
     * @param accountId  the customer's account ID (for ownership validation)
     * @return Optional of booking detail DTO
     */
    Optional<MyBookingDetailDTO> findBookingDetail(int bookingId, int accountId);

    /**
     * Finds all slot details for a booking.
     *
     * @param bookingId the booking ID
     * @return list of slot detail DTOs
     */
    List<BookingSlotDetailDTO> findSlotsByBookingId(int bookingId);

    /**
     * Checks if booking is UNPAID and belongs to the account.
     *
     * @param bookingId the booking ID
     * @param accountId the customer's account ID
     * @return true if booking can be cancelled
     */
    boolean isCancellable(int bookingId, int accountId);

    /**
     * Updates booking status to CANCELLED and all its slot statuses.
     * Also deletes CourtSlotBooking locks.
     *
     * @param bookingId the booking ID
     */
    void cancelBooking(int bookingId);
}

