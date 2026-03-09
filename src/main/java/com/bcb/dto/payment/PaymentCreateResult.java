package com.bcb.dto.payment;

/**
 * Result returned after creating a VNPay payment URL.
 * Contains the redirect URL and metadata for the payment page.
 *
 * @author AnhTN
 */
public class PaymentCreateResult {

    private boolean success;
    private String paymentUrl;        // VNPay redirect URL
    private String transactionCode;   // our internal txn ref
    private String expireAt;          // ISO datetime string
    private Integer paymentId;
    private String message;           // error message if !success
    private String errorCode;         // error code for API response (e.g. NOT_FOUND, INVALID_STATUS)
    private Integer httpStatus;       // suggested HTTP status code for error responses
    private Integer bookingId;        // associated booking ID (used by retry flow)

    public PaymentCreateResult() {}

    /** @return true if payment record + URL created successfully */
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    /** @return full VNPay redirect URL with query params and signature */
    public String getPaymentUrl() { return paymentUrl; }
    public void setPaymentUrl(String paymentUrl) { this.paymentUrl = paymentUrl; }

    /** @return our internal transaction code */
    public String getTransactionCode() { return transactionCode; }
    public void setTransactionCode(String transactionCode) { this.transactionCode = transactionCode; }

    /** @return ISO datetime string of payment expiry */
    public String getExpireAt() { return expireAt; }
    public void setExpireAt(String expireAt) { this.expireAt = expireAt; }

    /** @return generated payment ID */
    public Integer getPaymentId() { return paymentId; }
    public void setPaymentId(Integer paymentId) { this.paymentId = paymentId; }

    /** @return error message when success=false */
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    /** @return error code for structured API error responses */
    public String getErrorCode() { return errorCode; }
    public void setErrorCode(String errorCode) { this.errorCode = errorCode; }

    /** @return suggested HTTP status code for error responses */
    public Integer getHttpStatus() { return httpStatus; }
    public void setHttpStatus(Integer httpStatus) { this.httpStatus = httpStatus; }

    /** @return associated booking ID */
    public Integer getBookingId() { return bookingId; }
    public void setBookingId(Integer bookingId) { this.bookingId = bookingId; }
}

