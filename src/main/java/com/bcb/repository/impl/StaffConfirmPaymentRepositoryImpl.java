package com.bcb.repository.impl;

import com.bcb.dto.staff.StaffConfirmPaymentInvoiceDTO;
import com.bcb.repository.staff.StaffConfirmPaymentRepository;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class StaffConfirmPaymentRepositoryImpl implements StaffConfirmPaymentRepository {

    @Override
    public Integer findFacilityIdByBookingId(Connection conn, int bookingId) throws Exception {
        String sql = "SELECT facility_id FROM Booking WHERE booking_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookingId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                return rs.getInt("facility_id");
            }
        }
    }

    @Override
    public StaffConfirmPaymentInvoiceDTO findInvoiceForUpdate(Connection conn, int bookingId) throws Exception {
        String sql = "SELECT invoice_id, total_amount, paid_amount, payment_status " +
                "FROM Invoice WITH (UPDLOCK, ROWLOCK) WHERE booking_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookingId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }

                StaffConfirmPaymentInvoiceDTO invoice = new StaffConfirmPaymentInvoiceDTO();
                invoice.setInvoiceId(rs.getInt("invoice_id"));
                invoice.setTotalAmount(rs.getBigDecimal("total_amount"));
                invoice.setPaidAmount(rs.getBigDecimal("paid_amount"));
                invoice.setPaymentStatus(rs.getString("payment_status"));
                return invoice;
            }
        }
    }

    @Override
    public void insertPayment(Connection conn, int invoiceId, BigDecimal amount, String paymentType, String method, int staffId)
            throws Exception {
        String sql = "INSERT INTO Payment (invoice_id, paid_amount, payment_time, payment_type, method, payment_status, staff_confirm_id, confirm_time) " +
                "VALUES (?, ?, GETDATE(), ?, ?, 'SUCCESS', ?, GETDATE())";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, invoiceId);
            ps.setBigDecimal(2, amount);
            ps.setString(3, paymentType);
            ps.setString(4, method);
            ps.setInt(5, staffId);
            ps.executeUpdate();
        }
    }

    @Override
    public void updateInvoiceAsPaid(Connection conn, int bookingId, BigDecimal paidAmount) throws Exception {
        String sql = "UPDATE Invoice SET paid_amount = ?, payment_status = 'PAID' WHERE booking_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBigDecimal(1, paidAmount);
            ps.setInt(2, bookingId);
            ps.executeUpdate();
        }
    }
}

