package com.bcb.repository.payment;

import com.bcb.model.Payment;

import java.sql.Connection;
import java.util.List;
import java.util.Optional;

/**
 * Data-access interface for the Payment table.
 * Reusable across all payment flows (booking, membership, etc.).
 *
 * @author AnhTN
 */
public interface PaymentRepository {

    /**
     * Inserts a payment record within a transaction. Returns generated paymentId.
     */
    int insertPayment(Connection conn, Payment payment);

    /**
     * Finds a payment by its internal transaction code.
     */
    Optional<Payment> findByTransactionCode(String transactionCode);

    /**
     * Finds a PENDING payment by invoiceId.
     */
    Optional<Payment> findPendingByInvoiceId(int invoiceId);

    /**
     * Updates payment status + VNPay callback fields.
     */
    boolean updateVNPayResult(String transactionCode, String status,
                              String vnpayTxnNo, String vnpayResponseCode);

    /**
     * Finds all PENDING payments whose expire_at has passed.
     */
    List<Payment> findExpiredPendingPayments();

    /**
     * Bulk-expire: set status=FAILED for expired PENDING payments.
     * Returns count of rows updated.
     */
    int expireOverduePayments();
}
