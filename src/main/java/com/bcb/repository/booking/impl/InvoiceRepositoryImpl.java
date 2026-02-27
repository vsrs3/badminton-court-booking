package com.bcb.repository.booking.impl;

import com.bcb.exception.DataAccessException;
import com.bcb.model.Invoice;
import com.bcb.repository.booking.InvoiceRepository;

import java.sql.*;

/**
 * JDBC implementation of {@link InvoiceRepository}.
 *
 * @author AnhTN
 */
public class InvoiceRepositoryImpl implements InvoiceRepository {

    /** {@inheritDoc} */
    @Override
    public int insertInvoice(Connection conn, Invoice invoice) {
        String sql = "INSERT INTO Invoice (booking_id, total_amount, paid_amount, deposit_percent, payment_status) "
                   + "VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, invoice.getBookingId());
            ps.setBigDecimal(2, invoice.getTotalAmount());
            ps.setBigDecimal(3, invoice.getPaidAmount());
            ps.setInt(4, invoice.getDepositPercent());
            ps.setString(5, invoice.getPaymentStatus());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
            throw new DataAccessException("Failed to insert invoice: No ID generated");
        } catch (SQLException e) {
            throw new DataAccessException("Failed to insert invoice", e);
        }
    }
}
