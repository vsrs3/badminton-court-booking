package com.bcb.repository.booking;

import com.bcb.model.Invoice;

import java.sql.Connection;
import java.util.Optional;

/**
 * Repository interface for Invoice CRUD.
 * Used by single-booking and payment modules.
 *
 * @author AnhTN
 */
public interface InvoiceRepository {

    /**
     * Inserts an invoice (within a transaction). Returns generated invoiceId.
     */
    int insertInvoice(Connection conn, Invoice invoice);

    /**
     * Finds an invoice by its invoiceId.
     */
    Optional<Invoice> findById(int invoiceId);

    /**
     * Finds the invoice linked to a given booking.
     */
    Optional<Invoice> findByBookingId(int bookingId);

    /**
     * Updates payment_status and paid_amount of an invoice.
     */
    void updatePaymentStatus(int invoiceId, String paymentStatus, java.math.BigDecimal paidAmount);
}

