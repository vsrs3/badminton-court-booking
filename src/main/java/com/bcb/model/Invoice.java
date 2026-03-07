package com.bcb.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity representing an invoice for a booking.
 * Updated to include voucher discount fields.
 *
 * @author AnhTN
 */
public class Invoice {
    private Integer invoiceId;
    private Integer bookingId;
    private BigDecimal totalAmount;
    private BigDecimal paidAmount;
    private Integer depositPercent;
    private String paymentStatus;
    private LocalDateTime createdAt;

    /** Voucher applied to this invoice (nullable) */
    private Integer voucherId;

    /** Actual discount amount applied */
    private BigDecimal discountAmount;

    public Invoice() {}

    public Integer getInvoiceId() { return invoiceId; }
    public void setInvoiceId(Integer invoiceId) { this.invoiceId = invoiceId; }
    public Integer getBookingId() { return bookingId; }
    public void setBookingId(Integer bookingId) { this.bookingId = bookingId; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public BigDecimal getPaidAmount() { return paidAmount; }
    public void setPaidAmount(BigDecimal paidAmount) { this.paidAmount = paidAmount; }
    public Integer getDepositPercent() { return depositPercent; }
    public void setDepositPercent(Integer depositPercent) { this.depositPercent = depositPercent; }
    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public Integer getVoucherId() { return voucherId; }
    public void setVoucherId(Integer voucherId) { this.voucherId = voucherId; }
    public BigDecimal getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(BigDecimal discountAmount) { this.discountAmount = discountAmount; }
}
