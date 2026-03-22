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
     * Finds bookings for a customer with paging support.
     */
    List<MyBookingListDTO> findMyBookings(int accountId, String status,
                                          String bookingType,
                                          LocalDate dateFrom, LocalDate dateTo,
                                          int offset, int limit);

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
     * Finds booking dates for recurring session lazy loading.
     */
    List<LocalDate> findBookingDates(int bookingId, LocalDate pivotDate,
                                     boolean pastDates, int limit);

    /**
     * Finds slots for provided booking dates.
     */
    List<BookingSlotDetailDTO> findSlotsByBookingIdAndDates(int bookingId,
                                                            List<LocalDate> bookingDates,
                                                            boolean ascendingDate);

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

