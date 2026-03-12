package com.bcb.service.impl;

import com.bcb.dto.staff.StaffConfirmPaymentInvoiceDTO;
import com.bcb.dto.staff.StaffConfirmPaymentResultDTO;
import com.bcb.repository.impl.StaffConfirmPaymentRepositoryImpl;
import com.bcb.repository.staff.StaffConfirmPaymentRepository;
import com.bcb.service.staff.StaffConfirmPaymentService;
import com.bcb.utils.DBContext;

import java.math.BigDecimal;
import java.sql.Connection;

public class StaffConfirmPaymentServiceImpl implements StaffConfirmPaymentService {

    private final StaffConfirmPaymentRepository repository = new StaffConfirmPaymentRepositoryImpl();

    @Override
    public StaffConfirmPaymentResultDTO confirmPayment(int bookingId, BigDecimal amount, String method,
                                                       int facilityId, int staffId) throws Exception {
        try (Connection conn = DBContext.getConnection()) {
            conn.setAutoCommit(false);
            try {
                Integer bookingFacilityId = repository.findFacilityIdByBookingId(conn, bookingId);
                if (bookingFacilityId == null) {
                    conn.rollback();
                    return fail("Khong tim thay booking");
                }
                if (bookingFacilityId != facilityId) {
                    conn.rollback();
                    return fail("Booking khong thuoc co so cua ban");
                }

                StaffConfirmPaymentInvoiceDTO invoice = repository.findInvoiceForUpdate(conn, bookingId);
                if (invoice == null) {
                    conn.rollback();
                    return fail("Khong tim thay hoa don cho booking nay");
                }

                if ("PAID".equals(invoice.getPaymentStatus())) {
                    conn.rollback();
                    return fail("Booking da duoc thanh toan day du");
                }

                BigDecimal newPaidAmount = invoice.getPaidAmount().add(amount);
                if (newPaidAmount.compareTo(invoice.getTotalAmount()) != 0) {
                    BigDecimal remaining = invoice.getTotalAmount().subtract(invoice.getPaidAmount());
                    conn.rollback();
                    return fail("So tien khong hop le. Can thu them dung " + formatMoney(remaining) + " de du tong tien.");
                }

                String paymentType = amount.compareTo(invoice.getTotalAmount()) == 0 ? "FULL" : "REMAINING";
                repository.insertPayment(conn, invoice.getInvoiceId(), amount, paymentType, method, staffId);
                repository.updateInvoiceAsPaid(conn, bookingId, invoice.getTotalAmount());

                conn.commit();
                StaffConfirmPaymentResultDTO result = new StaffConfirmPaymentResultDTO();
                result.setSuccess(true);
                result.setMessage("Xac nhan thanh toan thanh cong");
                result.setPaidAmount(invoice.getTotalAmount());
                result.setPaymentStatus("PAID");
                result.setMethod(method);
                return result;
            } catch (Exception e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    private StaffConfirmPaymentResultDTO fail(String message) {
        StaffConfirmPaymentResultDTO result = new StaffConfirmPaymentResultDTO();
        result.setSuccess(false);
        result.setMessage(message);
        return result;
    }

    private String formatMoney(BigDecimal amount) {
        if (amount == null) return "0d";
        return String.format("%,.0f", amount) + "d";
    }
}

