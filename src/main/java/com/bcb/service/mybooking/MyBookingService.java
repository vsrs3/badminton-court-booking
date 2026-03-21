package com.bcb.service.mybooking;

import com.bcb.dto.mybooking.MyBookingDetailDTO;
import com.bcb.dto.mybooking.MyBookingListDTO;
import com.bcb.exception.BusinessException;

import java.time.LocalDate;
import java.util.List;

/**
 * Service interface for customer "My Bookings" feature.
 */
public interface MyBookingService {

    /**
     * Gets list of customer's bookings with paging support.
     */
    List<MyBookingListDTO> getMyBookings(int accountId, String status,
                                         String bookingType,
                                         LocalDate dateFrom, LocalDate dateTo,
                                         int offset, int limit);

    /**
     * Gets optimized detail data for recurring bookings using day-window loading.
     */
    MyBookingDetailDTO getBookingDetail(int bookingId, int accountId,
                                        int futureDayLimit,
                                        boolean includePastDays,
                                        int pastDayLimit,
                                        LocalDate pivotDate) throws BusinessException;

    /**
     * Cancels a booking. Only if UNPAID and belongs to customer.
     * Also sends in-app notification.
     *
     * @param bookingId  the booking ID
     * @param accountId  the customer's account ID
     * @throws BusinessException if booking cannot be cancelled
     */
    void cancelBooking(int bookingId, int accountId) throws BusinessException;
}

