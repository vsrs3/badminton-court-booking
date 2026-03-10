package com.bcb.repository.booking;

import com.bcb.dto.singlebooking.SingleBookingMatrixTimeSlotDTO;

import java.time.LocalTime;
import java.util.List;

/**
 * Repository interface for TimeSlot lookup in single-booking context.
 *
 * @author AnhTN
 */
public interface TimeSlotRepository {

    /**
     * Finds time slots within the given open/close time range.
     */
    List<SingleBookingMatrixTimeSlotDTO> findByTimeRange(LocalTime openTime, LocalTime closeTime);
}

