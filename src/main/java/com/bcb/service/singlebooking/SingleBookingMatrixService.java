package com.bcb.service.singlebooking;

import com.bcb.dto.singlebooking.SingleBookingMatrixResponseDTO;

import java.time.LocalDate;

/**
 * Service interface for building the single-booking matrix data.
 *
 * @author AnhTN
 */
public interface SingleBookingMatrixService {

    /**
     * Builds the full matrix-data response for a facility on a date.
     */
    SingleBookingMatrixResponseDTO getMatrixData(int facilityId, LocalDate bookingDate);
}
