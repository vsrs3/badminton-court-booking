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
            throw new RecurringValidationException("VOUCHER_EMPTY", "Vui long nhap ma voucher.");
        }

        Voucher v = voucherRepo.findByCode(code)
                .orElseThrow(() -> new RecurringValidationException("VOUCHER_NOT_FOUND",
                        "Ma voucher khong ton tai hoac khong hop le."));

        if (v.getIsActive() == null || !v.getIsActive()) {
            throw new RecurringValidationException("VOUCHER_INACTIVE", "Voucher da bi vo hieu hoa.");
        }

        LocalDateTime now = LocalDateTime.now();
        if (v.getValidFrom() != null && now.isBefore(v.getValidFrom())) {
            throw new RecurringValidationException("VOUCHER_NOT_YET", "Voucher chua den thoi gian ap dung.");
        }
        if (v.getValidTo() != null && now.isAfter(v.getValidTo())) {
            throw new RecurringValidationException("VOUCHER_EXPIRED", "Voucher da het han su dung.");
        }

        String bookingType = v.getApplicableBookingType();
        if (!"RECURRING".equals(bookingType) && !"BOTH".equals(bookingType)) {
            throw new RecurringValidationException("VOUCHER_WRONG_TYPE_RECURRING",
                    "Voucher nay khong ap dung cho dat lich co dinh.");
        }

        List<Integer> facilityIds = voucherRepo.findFacilityIdsByVoucherId(v.getVoucherId());
        if (!facilityIds.isEmpty() && req.getFacilityId() != null && !facilityIds.contains(req.getFacilityId())) {
            throw new RecurringValidationException("VOUCHER_WRONG_FACILITY",
                    "Voucher khong ap dung cho co so nay.");
        }

        if (v.getUsageLimit() != null) {
            int totalUsed = voucherRepo.countTotalUsageByVoucher(v.getVoucherId());
            if (totalUsed >= v.getUsageLimit()) {
                throw new RecurringValidationException("VOUCHER_LIMIT_REACHED",
                        "Voucher da dat gioi han luot su dung.");
            }
        }

        int perUser = v.getPerUserLimit() != null ? v.getPerUserLimit() : 1;
        int userUsed = voucherRepo.countUsageByVoucherAndAccount(v.getVoucherId(), accountId);
        if (userUsed >= perUser) {
            throw new RecurringValidationException("VOUCHER_USER_LIMIT",
                    "Ban da su dung voucher nay du so lan cho phep.");
        }

        BigDecimal totalAmount = req.getTotalAmount() != null ? req.getTotalAmount() : BigDecimal.ZERO;
        BigDecimal minOrder = v.getMinOrderAmount() != null ? v.getMinOrderAmount() : BigDecimal.ZERO;
        if (totalAmount.compareTo(minOrder) < 0) {
            throw new RecurringValidationException("VOUCHER_MIN_ORDER",
                    "Don hang chua dat gia tri toi thieu " + formatVnd(minOrder) + " de dung voucher nay.",
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


