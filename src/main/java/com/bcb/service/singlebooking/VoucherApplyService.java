package com.bcb.service.singlebooking;

import com.bcb.dto.singlebooking.VoucherApplyRequestDTO;
import com.bcb.dto.singlebooking.VoucherApplyResponseDTO;

/**
 * Service interface for validating and applying a voucher code in single-booking flow.
 * Kiểm tra toàn bộ điều kiện hợp lệ của voucher trước khi cho phép áp dụng.
 *
 * @author AnhTN
 */
public interface VoucherApplyService {

    /**
     * Validates a voucher code against all rules and computes the discount amount.
     *
     * @param accountId Account of the customer applying the voucher
     * @param request   Contains voucherCode, facilityId, totalAmount
     * @return VoucherApplyResponseDTO with discount details
     * @throws com.bcb.exception.singlebooking.SingleBookingValidationException on any rule violation
     */
    VoucherApplyResponseDTO applyVoucher(int accountId, VoucherApplyRequestDTO request);
}
