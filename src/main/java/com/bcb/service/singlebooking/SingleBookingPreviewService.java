package com.bcb.service.singlebooking;

import com.bcb.dto.singlebooking.SingleBookingPreviewRequestDTO;
import com.bcb.dto.singlebooking.SingleBookingPreviewResponseDTO;

/**
 * Service interface for the single-booking preview (no hold).
 *
 * @author AnhTN
 */
public interface SingleBookingPreviewService {

    /**
     * Validates selections and computes pricing preview.
     */
    SingleBookingPreviewResponseDTO preview(SingleBookingPreviewRequestDTO request);
}
