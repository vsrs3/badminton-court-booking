package com.bcb.dto.payment;

/**
 * Payment status check result — returned by the "check payment" endpoint
 * and used by frontend polling/manual-check.
 *
 * @author AnhTN
 */
public class PaymentStatusDTO {

    private String transactionCode;
    private String status;            // PENDING | SUCCESS | FAILED | EXPIRED | NOT_FOUND
    private String vnpayTxnNo;
    private Long amount;
    private String message;
    private Integer bookingId;

    public PaymentStatusDTO() {}

    /** @return our internal transaction code */
    public String getTransactionCode() { return transactionCode; }
    public void setTransactionCode(String transactionCode) { this.transactionCode = transactionCode; }

    /** @return current payment status */
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    /** @return VNPay transaction number (if available) */
    public String getVnpayTxnNo() { return vnpayTxnNo; }
    public void setVnpayTxnNo(String vnpayTxnNo) { this.vnpayTxnNo = vnpayTxnNo; }

    /** @return payment amount in VND */
    public Long getAmount() { return amount; }
    public void setAmount(Long amount) { this.amount = amount; }

    /** @return human-readable status message */
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    /** @return associated booking ID */
    public Integer getBookingId() { return bookingId; }
    public void setBookingId(Integer bookingId) { this.bookingId = bookingId; }
}

