package com.bcb.dto.singlebooking;

import java.math.BigDecimal;

/**
 * Request DTO for POST /api/single-booking/apply-voucher.
 * Customer submits voucher code along with booking context to validate and get discount.
 *
 * @author AnhTN
 */
public class VoucherApplyRequestDTO {

    /** Mã voucher do customer nhập (case-sensitive). */
    private String voucherCode;

    /** Facility ID của booking (dùng để kiểm tra VoucherFacility). */
    private Integer facilityId;

    /** Tổng tiền sân trước giảm giá (dùng để kiểm tra min_order_amount). */
    private BigDecimal totalAmount;

    public VoucherApplyRequestDTO() {}

    public String getVoucherCode() { return voucherCode; }
    public void setVoucherCode(String voucherCode) { this.voucherCode = voucherCode; }

    public Integer getFacilityId() { return facilityId; }
    public void setFacilityId(Integer facilityId) { this.facilityId = facilityId; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
}
