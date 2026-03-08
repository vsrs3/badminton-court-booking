package com.bcb.dto.staff;

import java.math.BigDecimal;

public class StaffBookingDetailInvoiceDto {
    private BigDecimal totalAmount;
    private BigDecimal paidAmount;
    private String paymentStatus;
    private BigDecimal refundDue;
    private String refundStatus;

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public BigDecimal getPaidAmount() {
        return paidAmount;
    }

    public void setPaidAmount(BigDecimal paidAmount) {
        this.paidAmount = paidAmount;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public BigDecimal getRefundDue() {
        return refundDue;
    }

    public void setRefundDue(BigDecimal refundDue) {
        this.refundDue = refundDue;
    }

    public String getRefundStatus() {
        return refundStatus;
    }

    public void setRefundStatus(String refundStatus) {
        this.refundStatus = refundStatus;
    }
}
