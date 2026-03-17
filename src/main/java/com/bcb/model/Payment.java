package com.bcb.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity representing the Payment table.
 * Supports VNPay online payment and offline (CASH/BANK_TRANSFER) confirmation.
 *
 * @author AnhTN
 */
public class Payment {

    private Integer paymentId;
    private Integer invoiceId;

    // VNPay-specific fields
    private String vnpayTxnNo;
    private String vnpayResponseCode;
    private LocalDateTime expireAt;
    private LocalDateTime createdAt;

    private String transactionCode;   // our internal txn ref (used as vnp_TxnRef)
    private BigDecimal paidAmount;
    private LocalDateTime paymentTime;

    private String paymentType;       // DEPOSIT | REMAINING | FULL
    private String method;            // VNPAY | CASH | BANK_TRANSFER
    private String paymentStatus;     // SUCCESS | FAILED | PENDING

    private Integer staffConfirmId;
    private LocalDateTime confirmTime;

    public Payment() {}

    /** @return payment primary key */
    public Integer getPaymentId() { return paymentId; }
    public void setPaymentId(Integer paymentId) { this.paymentId = paymentId; }

    /** @return linked invoice id */
    public Integer getInvoiceId() { return invoiceId; }
    public void setInvoiceId(Integer invoiceId) { this.invoiceId = invoiceId; }

    /** @return VNPay transaction number returned by gateway */
    public String getVnpayTxnNo() { return vnpayTxnNo; }
    public void setVnpayTxnNo(String vnpayTxnNo) { this.vnpayTxnNo = vnpayTxnNo; }

    /** @return VNPay response code */
    public String getVnpayResponseCode() { return vnpayResponseCode; }
    public void setVnpayResponseCode(String vnpayResponseCode) { this.vnpayResponseCode = vnpayResponseCode; }

    /** @return payment expiration datetime */
    public LocalDateTime getExpireAt() { return expireAt; }
    public void setExpireAt(LocalDateTime expireAt) { this.expireAt = expireAt; }

    /** @return creation datetime */
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }


    /** @return internal transaction code (used as vnp_TxnRef) */
    public String getTransactionCode() { return transactionCode; }
    public void setTransactionCode(String transactionCode) { this.transactionCode = transactionCode; }

    /** @return the amount that was paid */
    public BigDecimal getPaidAmount() { return paidAmount; }
    public void setPaidAmount(BigDecimal paidAmount) { this.paidAmount = paidAmount; }

    /** @return datetime when payment was completed */
    public LocalDateTime getPaymentTime() { return paymentTime; }
    public void setPaymentTime(LocalDateTime paymentTime) { this.paymentTime = paymentTime; }

    /** @return DEPOSIT, REMAINING, or FULL */
    public String getPaymentType() { return paymentType; }
    public void setPaymentType(String paymentType) { this.paymentType = paymentType; }

    /** @return VNPAY, CASH, or BANK_TRANSFER */
    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }

    /** @return SUCCESS, FAILED, or PENDING */
    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }

    /** @return staff who confirmed offline payment (nullable) */
    public Integer getStaffConfirmId() { return staffConfirmId; }
    public void setStaffConfirmId(Integer staffConfirmId) { this.staffConfirmId = staffConfirmId; }

    /** @return datetime when staff confirmed offline payment */
    public LocalDateTime getConfirmTime() { return confirmTime; }
    public void setConfirmTime(LocalDateTime confirmTime) { this.confirmTime = confirmTime; }
}

