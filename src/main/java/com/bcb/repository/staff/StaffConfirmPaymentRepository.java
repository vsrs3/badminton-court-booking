package com.bcb.repository.staff;

import com.bcb.dto.staff.StaffConfirmPaymentInvoiceDto;

import java.math.BigDecimal;
import java.sql.Connection;

public interface StaffConfirmPaymentRepository {
    Integer findFacilityIdByBookingId(Connection conn, int bookingId) throws Exception;

    StaffConfirmPaymentInvoiceDto findInvoiceForUpdate(Connection conn, int bookingId) throws Exception;

    void insertPayment(Connection conn, int invoiceId, BigDecimal amount, String paymentType, String method, int staffId) throws Exception;

    void updateInvoiceAsPaid(Connection conn, int bookingId, BigDecimal paidAmount) throws Exception;
}
