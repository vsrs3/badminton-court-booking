package com.bcb.service.impl;

import com.bcb.dto.staff.StaffConfirmPaymentInvoiceDTO;
import com.bcb.dto.staff.StaffConfirmPaymentResultDTO;
import com.bcb.repository.impl.StaffConfirmPaymentRepositoryImpl;
import com.bcb.repository.impl.StaffRentalRepositoryImpl;
import com.bcb.repository.staff.StaffConfirmPaymentRepository;
import com.bcb.repository.staff.StaffRentalRepository;
import com.bcb.service.staff.StaffConfirmPaymentService;
import com.bcb.utils.DBContext;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class StaffConfirmPaymentServiceImpl implements StaffConfirmPaymentService {

    private final StaffConfirmPaymentRepository repository = new StaffConfirmPaymentRepositoryImpl();
    private final StaffRentalRepository rentalRepository = new StaffRentalRepositoryImpl();

    /**
     * Confirms full payment and updates invoice/payment records in a transaction.
     */
    @Override
    public StaffConfirmPaymentResultDTO confirmPayment(int bookingId, BigDecimal amount, String method,
                                                       int facilityId, int staffId) throws Exception {
        // Transaction flow: validate, insert payment, update invoice, and log rentals.
        Connection conn = null;
        try {
            conn = DBContext.getConnection();
            conn.setAutoCommit(false);

            // Validate booking ownership and payment method.
            Integer bookingFacilityId = repository.findFacilityIdByBookingId(conn, bookingId);
            if (bookingFacilityId == null) {
                conn.rollback();
                return fail("Không tìm thấy booking");
            }
            if (bookingFacilityId != facilityId) {
                conn.rollback();
                return fail("Booking không thuộc cơ sở của bạn");
            }

            // Lock invoice row before updating payment status.
            StaffConfirmPaymentInvoiceDTO invoice = repository.findInvoiceForUpdate(conn, bookingId);
            if (invoice == null) {
                conn.rollback();
                return fail("Không tìm thấy hóa đơn cho booking này");
            }

            if ("PAID".equals(invoice.getPaymentStatus())) {
                conn.rollback();
                return fail("Booking đã được thanh toán đầy đủ");
            }

            if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
                conn.rollback();
                return fail("Số tiền thanh toán không hợp lệ");
            }

            // Compute remaining amount and require exact full payment.
            BigDecimal actualTotalAmount = loadCurrentGrandTotal(conn, bookingId);
            BigDecimal currentPaid = invoice.getPaidAmount() != null ? invoice.getPaidAmount() : BigDecimal.ZERO;
            BigDecimal remaining = actualTotalAmount.subtract(currentPaid);

            BigDecimal newPaidAmount = currentPaid.add(amount);
            if (newPaidAmount.compareTo(actualTotalAmount) != 0) {
                conn.rollback();
                return fail("Số tiền không hợp lệ. Cần thu thêm đúng " + formatMoney(remaining) + " để đủ tổng tiền.");
            }

            String paymentType = currentPaid.compareTo(BigDecimal.ZERO) == 0 ? "FULL" : "REMAINING";
            // Insert payment record and mark invoice PAID in one transaction.
            repository.insertPayment(conn, invoice.getInvoiceId(), amount, paymentType, method, staffId);
            repository.updateInvoiceAsPaid(conn, bookingId, actualTotalAmount, actualTotalAmount);

            // Update rental logs after payment confirm.
            // Insert rental logs if needed, but do not change FacilityInventory.available_quantity.
            rentalRepository.insertRentalLogAndDecreaseStock(conn, bookingId, facilityId, staffId);

            conn.commit();

            StaffConfirmPaymentResultDTO result = new StaffConfirmPaymentResultDTO();
            result.setSuccess(true);
            result.setMessage("Xác nhận thanh toán thành công");
            result.setPaidAmount(actualTotalAmount);
            result.setTotalAmount(actualTotalAmount);
            result.setPaymentStatus("PAID");
            result.setMethod(method);
            return result;
        } catch (Exception e) {
            if (conn != null) {
                conn.rollback();
            }
            throw e;
        } finally {
            if (conn != null) {
                conn.setAutoCommit(true);
                conn.close();
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
        if (amount == null) {
            return "0d";
        }
        return String.format("%,.0f", amount) + "d";
    }

    private BigDecimal loadCurrentGrandTotal(Connection conn, int bookingId) throws Exception {
        String sql = """
                SELECT
                    ISNULL((SELECT SUM(bs.price)
                            FROM BookingSlot bs
                            WHERE bs.booking_id = ?
                              AND bs.slot_status <> 'CANCELLED'), 0) AS court_total,
                    ISNULL((SELECT SUM(rr.quantity * rr.unit_price)
                            FROM RacketRental rr
                            JOIN BookingSlot bs2 ON bs2.booking_slot_id = rr.booking_slot_id
                            WHERE bs2.booking_id = ?), 0) AS rental_total
                """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookingId);
            ps.setInt(2, bookingId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return BigDecimal.ZERO;
                }

                BigDecimal courtTotal = rs.getBigDecimal("court_total");
                BigDecimal rentalTotal = rs.getBigDecimal("rental_total");

                if (courtTotal == null) {
                    courtTotal = BigDecimal.ZERO;
                }
                if (rentalTotal == null) {
                    rentalTotal = BigDecimal.ZERO;
                }
                return courtTotal.add(rentalTotal);
            }
        }
    }
}
