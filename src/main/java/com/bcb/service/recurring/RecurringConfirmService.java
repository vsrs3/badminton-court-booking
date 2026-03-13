package com.bcb.service.recurring;

import com.bcb.dto.recurring.RecurringConfirmRequestDTO;
import com.bcb.dto.recurring.RecurringConfirmResponseDTO;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Service interface for recurring confirm-and-pay flow.
 *
 * @author AnhTN
 */
public interface RecurringConfirmService {

    RecurringConfirmResponseDTO confirmAndPay(int accountId,
                                              RecurringConfirmRequestDTO request,
                                              HttpServletRequest httpReq);
}

