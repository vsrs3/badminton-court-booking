package com.bcb.repository.booking;

import com.bcb.model.Invoice;

import java.sql.Connection;

/**
 * Repository interface for Invoice CRUD in single-booking context.
 *
 * @author AnhTN
 */
public interface InvoiceRepository {

    /**
     * Inserts an invoice (within a transaction). Returns generated invoiceId.
     */
    int insertInvoice(Connection conn, Invoice invoice);
}
