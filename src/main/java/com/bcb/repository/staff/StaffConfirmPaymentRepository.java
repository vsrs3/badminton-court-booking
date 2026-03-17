package com.bcb.repository.staff;

import com.bcb.dto.staff.StaffConfirmPaymentInvoiceDTO;

import java.math.BigDecimal;
import java.sql.Connection;

public interface StaffConfirmPaymentRepository {
    Integer findFacilityIdByBookingId(Connection conn, int bookingId) throws Exception;

    StaffConfirmPaymentInvoiceDTO findInvoiceForUpdate(Connection conn, int bookingId) throws Exception;

    void insertPayment(Connection conn, int invoiceId, BigDecimal amount, String paymentType, String method, int staffId) throws Exception;

    void updateInvoiceAsPaid(Connection conn, int bookingId, BigDecimal totalAmount, BigDecimal paidAmount) throws Exception;
}

