package com.bcb.service.singlebooking.impl;

import com.bcb.dto.singlebooking.VoucherApplyRequestDTO;
import com.bcb.dto.singlebooking.VoucherApplyResponseDTO;
import com.bcb.exception.singlebooking.SingleBookingValidationException;
import com.bcb.model.Voucher;
import com.bcb.repository.voucher.VoucherRepository;
import com.bcb.repository.voucher.impl.VoucherRepositoryImpl;
import com.bcb.service.singlebooking.VoucherApplyService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Validates and computes discount for a voucher code in single-booking flow.
 *
 * Validation order:
 * 1. Code tồn tại
 * 2. is_active = 1
 * 3. Trong khoảng valid_from → valid_to
 * 4. applicable_booking_type = SINGLE hoặc BOTH
 * 5. Áp dụng đúng facility (VoucherFacility rỗng = áp dụng tất cả)
 * 6. Chưa vượt global usage_limit
 * 7. User chưa vượt per_user_limit
 * 8. totalAmount >= min_order_amount
 *
 * @author AnhTN
 */
public class VoucherApplyServiceImpl implements VoucherApplyService {

    private final VoucherRepository voucherRepo;

    public VoucherApplyServiceImpl() {
        this.voucherRepo = new VoucherRepositoryImpl();
    }

    /** {@inheritDoc} */
    @Override
    public VoucherApplyResponseDTO applyVoucher(int accountId, VoucherApplyRequestDTO req) {

        // ── 1. Mã tồn tại ──
        String code = req.getVoucherCode() == null ? "" : req.getVoucherCode().trim();
        if (code.isEmpty()) {
            throw new SingleBookingValidationException("VOUCHER_EMPTY",
                    "Vui lòng nhập mã voucher.", List.of());
        }

        Voucher v = voucherRepo.findByCode(code)
                .orElseThrow(() -> new SingleBookingValidationException("VOUCHER_NOT_FOUND",
                        "Mã voucher không tồn tại hoặc không hợp lệ.", List.of()));

        // ── 2. is_active ──
        if (v.getIsActive() == null || !v.getIsActive()) {
            throw new SingleBookingValidationException("VOUCHER_INACTIVE",
                    "Voucher đã bị vô hiệu hóa.", List.of());
        }

        // ── 3. Thời hạn ──
        LocalDateTime now = LocalDateTime.now();
        if (v.getValidFrom() != null && now.isBefore(v.getValidFrom())) {
            throw new SingleBookingValidationException("VOUCHER_NOT_YET",
                    "Voucher chưa đến thời gian áp dụng.", List.of());
        }
        if (v.getValidTo() != null && now.isAfter(v.getValidTo())) {
            throw new SingleBookingValidationException("VOUCHER_EXPIRED",
                    "Voucher đã hết hạn sử dụng.", List.of());
        }

        // ── 4. Loại booking (SINGLE hoặc BOTH) ──
        String bookingType = v.getApplicableBookingType();
        if (!"SINGLE".equals(bookingType) && !"BOTH".equals(bookingType)) {
            throw new SingleBookingValidationException("VOUCHER_WRONG_TYPE",
                    "Voucher này không áp dụng cho đặt sân đơn.", List.of());
        }

        // ── 5. Facility ──
        // Nếu VoucherFacility có record thì kiểm tra, không có = áp dụng tất cả
        List<Integer> facilityIds = voucherRepo.findFacilityIdsByVoucherId(v.getVoucherId());
        if (!facilityIds.isEmpty() && req.getFacilityId() != null
                && !facilityIds.contains(req.getFacilityId())) {
            throw new SingleBookingValidationException("VOUCHER_WRONG_FACILITY",
                    "Voucher không áp dụng cho cơ sở này.", List.of());
        }

        // ── 6. Global usage_limit ──
        if (v.getUsageLimit() != null) {
            int totalUsed = voucherRepo.countTotalUsageByVoucher(v.getVoucherId());
            if (totalUsed >= v.getUsageLimit()) {
                throw new SingleBookingValidationException("VOUCHER_LIMIT_REACHED",
                        "Voucher đã đạt giới hạn lượt sử dụng.", List.of());
            }
        }

        // ── 7. Per-user limit ──
        int perUser = v.getPerUserLimit() != null ? v.getPerUserLimit() : 1;
        int userUsed = voucherRepo.countUsageByVoucherAndAccount(v.getVoucherId(), accountId);
        if (userUsed >= perUser) {
            throw new SingleBookingValidationException("VOUCHER_USER_LIMIT",
                    "Bạn đã sử dụng voucher này đủ số lần cho phép.", List.of());
        }

        // ── 8. min_order_amount ──
        BigDecimal totalAmount = req.getTotalAmount() != null ? req.getTotalAmount() : BigDecimal.ZERO;
        BigDecimal minOrder = v.getMinOrderAmount() != null ? v.getMinOrderAmount() : BigDecimal.ZERO;
        if (totalAmount.compareTo(minOrder) < 0) {
            throw new SingleBookingValidationException("VOUCHER_MIN_ORDER",
                    "Đơn hàng chưa đạt giá trị tối thiểu " + formatVnd(minOrder) + " để dùng voucher này.",
                    List.of(Map.of("minOrderAmount", minOrder)));
        }

        // ── Tính discount ──
        BigDecimal discountAmount;
        if ("PERCENTAGE".equals(v.getDiscountType())) {
            discountAmount = totalAmount
                    .multiply(v.getDiscountValue())
                    .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
            // Áp max_discount_amount nếu có
            if (v.getMaxDiscountAmount() != null
                    && discountAmount.compareTo(v.getMaxDiscountAmount()) > 0) {
                discountAmount = v.getMaxDiscountAmount();
            }
        } else {
            // FIXED_AMOUNT: không vượt quá totalAmount
            discountAmount = v.getDiscountValue().min(totalAmount);
        }

        BigDecimal finalAmount = totalAmount.subtract(discountAmount).max(BigDecimal.ZERO);

        // ── Build response ──
        VoucherApplyResponseDTO resp = new VoucherApplyResponseDTO();
        resp.setVoucherId(v.getVoucherId());
        resp.setVoucherCode(v.getCode());
        resp.setVoucherName(v.getName());
        resp.setDiscountType(v.getDiscountType());
        resp.setDiscountValue(v.getDiscountValue());
        resp.setDiscountAmount(discountAmount);
        resp.setFinalAmount(finalAmount);
        return resp;
    }

    /** Format số tiền theo định dạng VNĐ để hiển thị trong message lỗi. */
    private String formatVnd(BigDecimal amount) {
        if (amount == null) return "0 ₫";
        return String.format("%,.0f ₫", amount);
    }
}
