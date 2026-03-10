package com.bcb.service.singlebooking;

import com.bcb.dto.singlebooking.SingleBookingConfirmRequestDTO;
import com.bcb.dto.singlebooking.SingleBookingConfirmResponseDTO;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Service interface for confirm-and-pay with transaction and hold.
 *
 * @author AnhTN
 */
public interface SingleBookingConfirmService {

    /**
     * Validates, locks slots, creates booking + invoice + payment inside a transaction.
     *
     * @param accountId logged-in user's account ID
     * @param request   booking selections and deposit preference
     * @param httpReq   servlet request (needed for VNPay client IP)
     * @return response with bookingId, invoiceId, paymentUrl, etc.
     */
    SingleBookingConfirmResponseDTO confirmAndPay(int accountId,
                                                   SingleBookingConfirmRequestDTO request,
                                                   HttpServletRequest httpReq);
}

