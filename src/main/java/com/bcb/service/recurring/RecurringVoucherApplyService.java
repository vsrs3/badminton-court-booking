package com.bcb.service.recurring;

import com.bcb.dto.recurring.RecurringVoucherApplyRequestDTO;
import com.bcb.dto.recurring.RecurringVoucherApplyResponseDTO;

/**
 * Service interface for validating recurring vouchers.
 *
 * @author AnhTN
 */
public interface RecurringVoucherApplyService {

    /**
     * Validates voucher for recurring booking type and calculates discount.
     */
    RecurringVoucherApplyResponseDTO applyVoucher(int accountId, RecurringVoucherApplyRequestDTO request);
}

