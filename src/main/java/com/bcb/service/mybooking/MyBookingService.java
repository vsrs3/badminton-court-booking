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
     * Gets list of customer's bookings with optional filters.
     *
     * @param accountId  the customer's account ID
     * @param status     booking status filter (null or "all" = no filter)
     * @param dateFrom   start date filter (nullable)
     * @param dateTo     end date filter (nullable)
     * @return list of booking list DTOs
     */
    List<MyBookingListDTO> getMyBookings(int accountId, String status,
                                          LocalDate dateFrom, LocalDate dateTo);

    /**
     * Gets detailed booking info.
     *
     * @param bookingId  the booking ID
     * @param accountId  the customer's account ID (ownership check)
     * @return booking detail DTO
     * @throws BusinessException if booking not found or not owned
     */
    MyBookingDetailDTO getBookingDetail(int bookingId, int accountId) throws BusinessException;

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

