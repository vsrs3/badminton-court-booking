package com.bcb.dto.recurring;

import java.math.BigDecimal;

/**
 * Request DTO for recurring apply-voucher API.
 *
 * @author AnhTN
 */
public class RecurringVoucherApplyRequestDTO {

    private String voucherCode;
    private Integer facilityId;
    private BigDecimal totalAmount;

    public String getVoucherCode() {
        return voucherCode;
    }

    public void setVoucherCode(String voucherCode) {
        this.voucherCode = voucherCode;
    }

    public Integer getFacilityId() {
        return facilityId;
    }

    public void setFacilityId(Integer facilityId) {
        this.facilityId = facilityId;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }
}

