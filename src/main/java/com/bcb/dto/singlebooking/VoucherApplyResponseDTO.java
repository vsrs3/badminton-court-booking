package com.bcb.dto.singlebooking;

import java.math.BigDecimal;

/**
 * Response DTO for POST /api/single-booking/apply-voucher.
 * Trả về thông tin giảm giá nếu voucher hợp lệ.
 *
 * @author AnhTN
 */
public class VoucherApplyResponseDTO {

    /** ID voucher đã áp dụng. */
    private Integer voucherId;

    /** Mã voucher. */
    private String voucherCode;

    /** Tên voucher để hiển thị cho user. */
    private String voucherName;

    /** Loại giảm giá: PERCENTAGE | FIXED_AMOUNT. */
    private String discountType;

    /** Giá trị giảm (% hoặc số tiền). */
    private BigDecimal discountValue;

    /** Số tiền thực tế được giảm (đã tính toán dựa trên totalAmount). */
    private BigDecimal discountAmount;

    /** Tổng tiền sau khi trừ giảm giá. */
    private BigDecimal finalAmount;

    public VoucherApplyResponseDTO() {}

    public Integer getVoucherId() { return voucherId; }
    public void setVoucherId(Integer voucherId) { this.voucherId = voucherId; }

    public String getVoucherCode() { return voucherCode; }
    public void setVoucherCode(String voucherCode) { this.voucherCode = voucherCode; }

    public String getVoucherName() { return voucherName; }
    public void setVoucherName(String voucherName) { this.voucherName = voucherName; }

    public String getDiscountType() { return discountType; }
    public void setDiscountType(String discountType) { this.discountType = discountType; }

    public BigDecimal getDiscountValue() { return discountValue; }
    public void setDiscountValue(BigDecimal discountValue) { this.discountValue = discountValue; }

    public BigDecimal getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(BigDecimal discountAmount) { this.discountAmount = discountAmount; }

    public BigDecimal getFinalAmount() { return finalAmount; }
    public void setFinalAmount(BigDecimal finalAmount) { this.finalAmount = finalAmount; }
}
