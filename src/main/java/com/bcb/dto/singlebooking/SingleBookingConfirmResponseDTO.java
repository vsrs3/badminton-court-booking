package com.bcb.dto.singlebooking;

import java.math.BigDecimal;

/**
 * Response DTO for the confirm-and-pay endpoint.
 *
 * @author AnhTN
 */
public class SingleBookingConfirmResponseDTO {

    private Integer bookingId;
    private Integer invoiceId;
    private String holdExpiredAt;
    private BigDecimal totalAmount;
    private Integer depositPercent;
    private String paymentUrlStub;

    public SingleBookingConfirmResponseDTO() {}

    public Integer getBookingId() { return bookingId; }
    public void setBookingId(Integer bookingId) { this.bookingId = bookingId; }

    public Integer getInvoiceId() { return invoiceId; }
    public void setInvoiceId(Integer invoiceId) { this.invoiceId = invoiceId; }

    public String getHoldExpiredAt() { return holdExpiredAt; }
    public void setHoldExpiredAt(String holdExpiredAt) { this.holdExpiredAt = holdExpiredAt; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public Integer getDepositPercent() { return depositPercent; }
    public void setDepositPercent(Integer depositPercent) { this.depositPercent = depositPercent; }

    public String getPaymentUrlStub() { return paymentUrlStub; }
    public void setPaymentUrlStub(String paymentUrlStub) { this.paymentUrlStub = paymentUrlStub; }
}
