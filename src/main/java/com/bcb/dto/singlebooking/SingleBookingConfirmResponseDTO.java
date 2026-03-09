package com.bcb.dto.singlebooking;

import java.math.BigDecimal;

/**
 * Response DTO for the confirm-and-pay endpoint.
 * Now includes real VNPay payment URL and transaction code.
 *
 * @author AnhTN
 */
public class SingleBookingConfirmResponseDTO {

    private Integer bookingId;
    private Integer invoiceId;
    private String holdExpiredAt;
    private BigDecimal totalAmount;
    private BigDecimal payAmount;
    private Integer depositPercent;
    private String paymentUrlStub;
    private String paymentUrl;
    private String transactionCode;
    /** Số tiền giảm giá từ voucher (0 nếu không có voucher). */
    private BigDecimal discountAmount;
    /** Tổng tiền sau khi trừ voucher. */
    private BigDecimal finalAmount;

    public SingleBookingConfirmResponseDTO() {}

    public Integer getBookingId() { return bookingId; }
    public void setBookingId(Integer bookingId) { this.bookingId = bookingId; }

    public Integer getInvoiceId() { return invoiceId; }
    public void setInvoiceId(Integer invoiceId) { this.invoiceId = invoiceId; }

    public String getHoldExpiredAt() { return holdExpiredAt; }
    public void setHoldExpiredAt(String holdExpiredAt) { this.holdExpiredAt = holdExpiredAt; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public BigDecimal getPayAmount() { return payAmount; }
    public void setPayAmount(BigDecimal payAmount) { this.payAmount = payAmount; }

    public Integer getDepositPercent() { return depositPercent; }
    public void setDepositPercent(Integer depositPercent) { this.depositPercent = depositPercent; }

    public String getPaymentUrlStub() { return paymentUrlStub; }
    public void setPaymentUrlStub(String paymentUrlStub) { this.paymentUrlStub = paymentUrlStub; }

    /** @return real VNPay redirect URL */
    public String getPaymentUrl() { return paymentUrl; }
    public void setPaymentUrl(String paymentUrl) { this.paymentUrl = paymentUrl; }

    /** @return internal transaction code for payment status checks */
    public String getTransactionCode() { return transactionCode; }
    public void setTransactionCode(String transactionCode) { this.transactionCode = transactionCode; }

    public BigDecimal getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(BigDecimal discountAmount) { this.discountAmount = discountAmount; }

    public BigDecimal getFinalAmount() { return finalAmount; }
    public void setFinalAmount(BigDecimal finalAmount) { this.finalAmount = finalAmount; }
}

