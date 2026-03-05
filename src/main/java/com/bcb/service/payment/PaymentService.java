package com.bcb.service.payment;

import com.bcb.dto.payment.PaymentCreateResult;
import com.bcb.dto.payment.PaymentStatusDTO;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Map;

/**
 * Orchestrator service for VNPay payments.
 * Creates Payment records, delegates URL generation to {@link VNPayService},
 * and processes callbacks / status checks.
 * Reusable: any module needing payment calls {@link #createVNPayPayment}.
 *
 * @author AnhTN
 */
public interface PaymentService {

    /**
     * Creates a PENDING payment record, generates VNPay redirect URL.
     *
     * @param invoiceId     linked invoice
     * @param payAmountVND  amount to charge in VND (already computed: total × depositPercent/100)
     * @param paymentType   FULL | DEPOSIT | REMAINING
     * @param description   VNPay order info text
     * @param httpReq       servlet request (for client IP)
     * @return result with paymentUrl, transactionCode, expireAt
     */
    PaymentCreateResult createVNPayPayment(int invoiceId, long payAmountVND,
                                           String paymentType, String description,
                                           HttpServletRequest httpReq);

    /**
     * Processes VNPay return / IPN callback:
     * verifies signature, updates Payment + Invoice + Booking status.
     *
     * @param params VNPay callback query parameters
     * @return payment status after processing
     */
    PaymentStatusDTO processVNPayCallback(Map<String, String> params);

    /**
     * Manual check: queries current payment status by transaction code.
     * Also handles late expiry detection.
     *
     * @param transactionCode our internal txn ref
     * @return current status
     */
    PaymentStatusDTO checkPaymentStatus(String transactionCode);

    /**
     * Expires all overdue PENDING payments and releases their bookings.
     * Called by the scheduler cron.
     */
    void expireOverduePayments();

    /**
     * Retries payment for an existing PENDING booking:
     * verifies ownership, extends hold, finds invoice, calculates amount, creates VNPay URL.
     *
     * @param bookingId the existing booking to retry payment for
     * @param accountId the authenticated user (ownership check)
     * @param httpReq   servlet request (for client IP)
     * @return result with paymentUrl, transactionCode
     * @author AnhTN
     */
    PaymentCreateResult retryPaymentForBooking(int bookingId, int accountId, HttpServletRequest httpReq);
}
