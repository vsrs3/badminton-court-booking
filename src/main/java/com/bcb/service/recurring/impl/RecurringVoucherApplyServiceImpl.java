package com.bcb.service.recurring.impl;

import com.bcb.dto.recurring.RecurringVoucherApplyRequestDTO;
import com.bcb.dto.recurring.RecurringVoucherApplyResponseDTO;
import com.bcb.exception.recurring.RecurringValidationException;
import com.bcb.model.Voucher;
import com.bcb.repository.voucher.VoucherRepository;
import com.bcb.repository.voucher.impl.VoucherRepositoryImpl;
import com.bcb.service.recurring.RecurringVoucherApplyService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Validates and computes voucher discount for recurring booking.
 * Accepts only applicable_booking_type = RECURRING or BOTH.
 *
 * @author AnhTN
 */
public class RecurringVoucherApplyServiceImpl implements RecurringVoucherApplyService {

    private final VoucherRepository voucherRepo;

    public RecurringVoucherApplyServiceImpl() {
        this.voucherRepo = new VoucherRepositoryImpl();
    }

    /**
     * Applies voucher checks and returns discount result for recurring flow.
     */
    @Override
    public RecurringVoucherApplyResponseDTO applyVoucher(int accountId, RecurringVoucherApplyRequestDTO req) {
        String code = req.getVoucherCode() == null ? "" : req.getVoucherCode().trim();
        if (code.isEmpty()) {
            throw new RecurringValidationException("VOUCHER_EMPTY", "Vui lòng nhập mã voucher.");
        }

        Voucher v = voucherRepo.findByCode(code)
                .orElseThrow(() -> new RecurringValidationException("VOUCHER_NOT_FOUND",
                        "Mã voucher không tồn tại hoặc không hợp lệ."));

        if (v.getIsActive() == null || !v.getIsActive()) {
            throw new RecurringValidationException("VOUCHER_INACTIVE", "Voucher đã bị vô hiệu hóa.");
        }

        LocalDateTime now = LocalDateTime.now();
        if (v.getValidFrom() != null && now.isBefore(v.getValidFrom())) {
            throw new RecurringValidationException("VOUCHER_NOT_YET", "Voucher chưa đến thời gian áp dụng.");
        }
        if (v.getValidTo() != null && now.isAfter(v.getValidTo())) {
            throw new RecurringValidationException("VOUCHER_EXPIRED", "Voucher đã hết hạn sử dụng.");
        }

        String bookingType = v.getApplicableBookingType();
        if (!"RECURRING".equals(bookingType) && !"BOTH".equals(bookingType)) {
            throw new RecurringValidationException("VOUCHER_WRONG_TYPE_RECURRING",
                    "Voucher này không áp dụng cho đặt lịch cố định.");
        }

        List<Integer> facilityIds = voucherRepo.findFacilityIdsByVoucherId(v.getVoucherId());
        if (!facilityIds.isEmpty() && req.getFacilityId() != null && !facilityIds.contains(req.getFacilityId())) {
            throw new RecurringValidationException("VOUCHER_WRONG_FACILITY",
                    "Voucher không áp dụng cho cơ sở này.");
        }

        if (v.getUsageLimit() != null) {
            int totalUsed = voucherRepo.countTotalUsageByVoucher(v.getVoucherId());
            if (totalUsed >= v.getUsageLimit()) {
                throw new RecurringValidationException("VOUCHER_LIMIT_REACHED",
                        "Voucher đã đạt giới hạn lượt sử dụng.");
            }
        }

        int perUser = v.getPerUserLimit() != null ? v.getPerUserLimit() : 1;
        int userUsed = voucherRepo.countUsageByVoucherAndAccount(v.getVoucherId(), accountId);
        if (userUsed >= perUser) {
            throw new RecurringValidationException("VOUCHER_USER_LIMIT",
                    "Bạn đã sử dụng voucher này đủ số lần cho phép.");
        }

        BigDecimal totalAmount = req.getTotalAmount() != null ? req.getTotalAmount() : BigDecimal.ZERO;
        BigDecimal minOrder = v.getMinOrderAmount() != null ? v.getMinOrderAmount() : BigDecimal.ZERO;
        if (totalAmount.compareTo(minOrder) < 0) {
            throw new RecurringValidationException("VOUCHER_MIN_ORDER",
                    "Đơn hàng chưa đạt giá trị tối thiểu " + formatVnd(minOrder) + " để dùng voucher này.",
                    List.of(Map.of("minOrderAmount", minOrder)));
        }

        BigDecimal discountAmount;
        if ("PERCENTAGE".equals(v.getDiscountType())) {
            discountAmount = totalAmount.multiply(v.getDiscountValue())
                    .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
            if (v.getMaxDiscountAmount() != null && discountAmount.compareTo(v.getMaxDiscountAmount()) > 0) {
                discountAmount = v.getMaxDiscountAmount();
            }
        } else {
            discountAmount = v.getDiscountValue().min(totalAmount);
        }

        BigDecimal finalAmount = totalAmount.subtract(discountAmount).max(BigDecimal.ZERO);

        RecurringVoucherApplyResponseDTO response = new RecurringVoucherApplyResponseDTO();
        response.setVoucherId(v.getVoucherId());
        response.setVoucherCode(v.getCode());
        response.setVoucherName(v.getName());
        response.setDiscountType(v.getDiscountType());
        response.setDiscountValue(v.getDiscountValue());
        response.setDiscountAmount(discountAmount);
        response.setFinalAmount(finalAmount);
        return response;
    }

    /**
     * Formats money for validation messages.
     */
    private String formatVnd(BigDecimal amount) {
        if (amount == null) {
            return "0 VND";
        }
        return String.format("%,.0f VND", amount);
    }

}


