package com.bcb.repository.payment.impl;

import com.bcb.exception.DataAccessException;
import com.bcb.model.Payment;
import com.bcb.repository.payment.PaymentRepository;
import com.bcb.utils.DBContext;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JDBC implementation of {@link PaymentRepository}.
 * Uses DBContext.getConnection() for standalone reads;
 * transactional writes receive a Connection parameter.
 *
 * @author AnhTN
 */
public class PaymentRepositoryImpl implements PaymentRepository {

    /** {@inheritDoc} */
    @Override
    public int insertPayment(Connection conn, Payment p) {
        String sql = "INSERT INTO Payment "
                + "(invoice_id, gateway, transaction_code, paid_amount, payment_time, "
                + " payment_type, method, payment_status, "
                + " vnpay_txn_no, vnpay_response_code, expire_at) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, p.getInvoiceId());
            ps.setString(2, p.getGateway() != null ? p.getGateway() : "VNPAY");
            ps.setString(3, p.getTransactionCode());
            ps.setBigDecimal(4, p.getPaidAmount());
            if (p.getPaymentTime() != null) {
                ps.setTimestamp(5, Timestamp.valueOf(p.getPaymentTime()));
            } else {
                ps.setNull(5, Types.TIMESTAMP);
            }
            ps.setString(6, p.getPaymentType());
            ps.setString(7, p.getMethod() != null ? p.getMethod() : "VNPAY");
            ps.setString(8, p.getPaymentStatus() != null ? p.getPaymentStatus() : "PENDING");
            ps.setString(9, p.getVnpayTxnNo());
            ps.setString(10, p.getVnpayResponseCode());
            if (p.getExpireAt() != null) {
                ps.setTimestamp(11, Timestamp.valueOf(p.getExpireAt()));
            } else {
                ps.setNull(11, Types.TIMESTAMP);
            }
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
            throw new DataAccessException("Failed to insert payment: No ID generated");
        } catch (SQLException e) {
            throw new DataAccessException("Failed to insert payment", e);
        }
    }

    /** {@inheritDoc} */
    @Override
    public Optional<Payment> findByTransactionCode(String transactionCode) {
        String sql = "SELECT * FROM Payment WHERE transaction_code = ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, transactionCode);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to find payment by txn code", e);
        }
        return Optional.empty();
    }

    /** {@inheritDoc} */
    @Override
    public Optional<Payment> findPendingByInvoiceId(int invoiceId) {
        String sql = "SELECT * FROM Payment WHERE invoice_id = ? AND payment_status = 'PENDING' "
                + "ORDER BY created_at DESC";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, invoiceId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to find pending payment by invoice", e);
        }
        return Optional.empty();
    }

    /** {@inheritDoc} */
    @Override
    public boolean updateVNPayResult(String transactionCode, String status,
                                     String vnpayTxnNo, String vnpayResponseCode) {
        String sql = "UPDATE Payment SET payment_status = ?, vnpay_txn_no = ?, "
                + "vnpay_response_code = ?, payment_time = GETDATE() "
                + "WHERE transaction_code = ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setString(2, vnpayTxnNo);
            ps.setString(3, vnpayResponseCode);
            ps.setString(4, transactionCode);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to update payment VNPay result", e);
        }
    }

    /** {@inheritDoc} */
    @Override
    public List<Payment> findExpiredPendingPayments() {
        String sql = "SELECT * FROM Payment "
                + "WHERE payment_status = 'PENDING' AND expire_at IS NOT NULL AND expire_at <= GETDATE()";
        List<Payment> list = new ArrayList<>();
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to find expired pending payments", e);
        }
        return list;
    }

    /** {@inheritDoc} */
    @Override
    public int expireOverduePayments() {
        String sql = "UPDATE Payment SET payment_status = 'FAILED' "
                + "WHERE payment_status = 'PENDING' AND expire_at IS NOT NULL AND expire_at <= GETDATE()";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to expire overdue payments", e);
        }
    }

    /** Maps a ResultSet row to a Payment entity. */
    private Payment mapRow(ResultSet rs) throws SQLException {
        Payment p = new Payment();
        p.setPaymentId(rs.getInt("payment_id"));
        p.setInvoiceId(rs.getInt("invoice_id"));
        p.setGateway(rs.getString("gateway"));
        p.setTransactionCode(rs.getString("transaction_code"));
        p.setPaidAmount(rs.getBigDecimal("paid_amount"));

        Timestamp pt = rs.getTimestamp("payment_time");
        if (pt != null) p.setPaymentTime(pt.toLocalDateTime());

        p.setPaymentType(rs.getString("payment_type"));
        p.setMethod(rs.getString("method"));
        p.setPaymentStatus(rs.getString("payment_status"));

        p.setVnpayTxnNo(rs.getString("vnpay_txn_no"));
        p.setVnpayResponseCode(rs.getString("vnpay_response_code"));

        Timestamp ea = rs.getTimestamp("expire_at");
        if (ea != null) p.setExpireAt(ea.toLocalDateTime());

        Timestamp ca = rs.getTimestamp("created_at");
        if (ca != null) p.setCreatedAt(ca.toLocalDateTime());

        int staffId = rs.getInt("staff_confirm_id");
        if (!rs.wasNull()) p.setStaffConfirmId(staffId);

        Timestamp ct = rs.getTimestamp("confirm_time");
        if (ct != null) p.setConfirmTime(ct.toLocalDateTime());

        return p;
    }
}
