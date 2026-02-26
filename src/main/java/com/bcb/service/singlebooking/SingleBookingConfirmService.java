package com.bcb.service.singlebooking;

import com.bcb.dto.singlebooking.SingleBookingConfirmRequestDTO;
import com.bcb.dto.singlebooking.SingleBookingConfirmResponseDTO;

/**
 * Service interface for confirm-and-pay with transaction and hold.
 *
 * @author AnhTN
 */
public interface SingleBookingConfirmService {

    /**
     * Validates, locks slots, creates booking + invoice inside a transaction.
     */
    SingleBookingConfirmResponseDTO confirmAndPay(int accountId, SingleBookingConfirmRequestDTO request);
}
