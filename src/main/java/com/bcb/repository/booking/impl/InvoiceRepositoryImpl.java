package com.bcb.repository.booking.impl;

import com.bcb.exception.DataAccessException;
import com.bcb.model.Invoice;
import com.bcb.repository.booking.InvoiceRepository;
import com.bcb.utils.DBContext;

import java.math.BigDecimal;
import java.sql.*;
import java.util.Optional;

/**
 * JDBC implementation of {@link InvoiceRepository}.
 *
 * @author AnhTN
 */
public class InvoiceRepositoryImpl implements InvoiceRepository {

    /** {@inheritDoc} */
    @Override
    public int insertInvoice(Connection conn, Invoice invoice) {
        String sql = "INSERT INTO Invoice (booking_id, total_amount, paid_amount, deposit_percent, payment_status, voucher_id, discount_amount) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, invoice.getBookingId());
            ps.setBigDecimal(2, invoice.getTotalAmount());
            ps.setBigDecimal(3, invoice.getPaidAmount());
            ps.setInt(4, invoice.getDepositPercent());
            ps.setString(5, invoice.getPaymentStatus());
            if (invoice.getVoucherId() != null) {
                ps.setInt(6, invoice.getVoucherId());
            } else {
                ps.setNull(6, java.sql.Types.INTEGER);
            }
            ps.setBigDecimal(7, invoice.getDiscountAmount() != null
                    ? invoice.getDiscountAmount() : BigDecimal.ZERO);
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

    /** {@inheritDoc} */
    @Override
    public Optional<Invoice> findById(int invoiceId) {
        String sql = "SELECT * FROM Invoice WHERE invoice_id = ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, invoiceId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to find invoice by id", e);
        }
        return Optional.empty();
    }

    /** {@inheritDoc} */
    @Override
    public Optional<Invoice> findByBookingId(int bookingId) {
        String sql = "SELECT * FROM Invoice WHERE booking_id = ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookingId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to find invoice by booking id", e);
        }
        return Optional.empty();
    }

    /** {@inheritDoc} */
    @Override
    public void updatePaymentStatus(int invoiceId, String paymentStatus, BigDecimal paidAmount) {
        String sql = "UPDATE Invoice SET payment_status = ?, paid_amount = ? WHERE invoice_id = ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, paymentStatus);
            ps.setBigDecimal(2, paidAmount);
            ps.setInt(3, invoiceId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to update invoice payment status", e);
        }
    }

    /** Maps a ResultSet row to an Invoice entity. */
    private Invoice mapRow(ResultSet rs) throws SQLException {
        Invoice inv = new Invoice();
        inv.setInvoiceId(rs.getInt("invoice_id"));
        inv.setBookingId(rs.getInt("booking_id"));
        inv.setTotalAmount(rs.getBigDecimal("total_amount"));
        inv.setPaidAmount(rs.getBigDecimal("paid_amount"));
        inv.setDepositPercent(rs.getInt("deposit_percent"));
        inv.setPaymentStatus(rs.getString("payment_status"));
        Timestamp ca = rs.getTimestamp("created_at");
        if (ca != null) inv.setCreatedAt(ca.toLocalDateTime());
        return inv;
    }
}
