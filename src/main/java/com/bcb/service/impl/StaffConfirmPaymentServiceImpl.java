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

public class StaffConfirmPaymentServiceImpl implements StaffConfirmPaymentService {

    private final StaffConfirmPaymentRepository repository = new StaffConfirmPaymentRepositoryImpl();
    private final StaffRentalRepository rentalRepository = new StaffRentalRepositoryImpl() {
        @Override
        public void insertRentalLogAndDecreaseStock(Connection conn, int bookingId, int facilityId, int staffId) throws Exception {

            String insertLogSql = """
            INSERT INTO RacketRentalLog (
                booking_slot_id,
                facility_inventory_id,
                quantity,
                staff_id,
                rented_at,
                returned_at
            )
            SELECT
                rr.booking_slot_id,
                fi.facility_inventory_id,
                rr.quantity,
                ?,
                GETDATE(),
                NULL
            FROM RacketRental rr
            JOIN BookingSlot bs ON bs.booking_slot_id = rr.booking_slot_id
            JOIN Court c ON c.court_id = bs.court_id
            JOIN FacilityInventory fi
                 ON fi.facility_id = c.facility_id
                AND fi.inventory_id = rr.inventory_id
            WHERE bs.booking_id = ?
              AND c.facility_id = ?
              AND NOT EXISTS (
                  SELECT 1
                  FROM RacketRentalLog rrl
                  WHERE rrl.booking_slot_id = rr.booking_slot_id
                    AND rrl.facility_inventory_id = fi.facility_inventory_id
              )
            """;

            try (PreparedStatement ps = conn.prepareStatement(insertLogSql)) {
                ps.setInt(1, staffId);
                ps.setInt(2, bookingId);
                ps.setInt(3, facilityId);
                ps.executeUpdate();
            }

            String updateStockSql = """
            UPDATE fi
            SET fi.available_quantity = fi.available_quantity - x.total_qty
            FROM FacilityInventory fi
            JOIN (
                SELECT
                    c.facility_id,
                    rr.inventory_id,
                    SUM(rr.quantity) AS total_qty
                FROM RacketRental rr
                JOIN BookingSlot bs ON bs.booking_slot_id = rr.booking_slot_id
                JOIN Court c ON c.court_id = bs.court_id
                WHERE bs.booking_id = ?
                  AND c.facility_id = ?
                GROUP BY c.facility_id, rr.inventory_id
            ) x
            ON fi.facility_id = x.facility_id
            AND fi.inventory_id = x.inventory_id
            """;

            try (PreparedStatement ps = conn.prepareStatement(updateStockSql)) {
                ps.setInt(1, bookingId);
                ps.setInt(2, facilityId);
                ps.executeUpdate();
            }
        }
    };

    @Override
    public StaffConfirmPaymentResultDTO confirmPayment(int bookingId, BigDecimal amount, String method,
                                                       int facilityId, int staffId) throws Exception {
        Connection conn = null;
        try {
            conn = DBContext.getConnection();
            conn.setAutoCommit(false);

            // 1. Validate booking thuộc facility của staff
            Integer bookingFacilityId = repository.findFacilityIdByBookingId(conn, bookingId);
            if (bookingFacilityId == null) {
                conn.rollback();
                return fail("Khong tim thay booking");
            }
            if (bookingFacilityId != facilityId) {
                conn.rollback();
                return fail("Booking khong thuoc co so cua ban");
            }

            // 2. Validate invoice
            StaffConfirmPaymentInvoiceDTO invoice = repository.findInvoiceForUpdate(conn, bookingId);
            if (invoice == null) {
                conn.rollback();
                return fail("Khong tim thay hoa don cho booking nay");
            }

            if ("PAID".equals(invoice.getPaymentStatus())) {
                conn.rollback();
                return fail("Booking da duoc thanh toan day du");
            }

            if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
                conn.rollback();
                return fail("So tien thanh toan khong hop le");
            }

            // 3. Kiểm tra số tiền phải thu đúng phần còn thiếu
            BigDecimal currentPaid = invoice.getPaidAmount() != null ? invoice.getPaidAmount() : BigDecimal.ZERO;
            BigDecimal totalAmount = invoice.getTotalAmount() != null ? invoice.getTotalAmount() : BigDecimal.ZERO;
            BigDecimal remaining = totalAmount.subtract(currentPaid);

            BigDecimal newPaidAmount = currentPaid.add(amount);
            if (newPaidAmount.compareTo(totalAmount) != 0) {
                conn.rollback();
                return fail("So tien khong hop le. Can thu them dung " + formatMoney(remaining) + " de du tong tien.");
            }

            // 4. Insert payment
            String paymentType = currentPaid.compareTo(BigDecimal.ZERO) == 0 ? "FULL" : "REMAINING";
            repository.insertPayment(conn, invoice.getInvoiceId(), amount, paymentType, method, staffId);

            // 5. Update invoice -> PAID
            repository.updateInvoiceAsPaid(conn, bookingId, totalAmount);

            // 6. Insert RacketRentalLog + decrease stock
            // QUAN TRỌNG: method này phải dùng CHUNG conn hiện tại
            rentalRepository.insertRentalLogAndDecreaseStock(conn, bookingId, facilityId, staffId);

            conn.commit();

            StaffConfirmPaymentResultDTO result = new StaffConfirmPaymentResultDTO();
            result.setSuccess(true);
            result.setMessage("Xac nhan thanh toan thanh cong");
            result.setPaidAmount(totalAmount);
            result.setPaymentStatus("PAID");
            result.setMethod(method);
            return result;

        } catch (Exception e) {
            if (conn != null) conn.rollback();
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
        if (amount == null) return "0d";
        return String.format("%,.0f", amount) + "d";
    }
}