package com.bcb.controller.staff;

import com.bcb.controller.staff.StaffAuthUtil.AuthResult;
import com.bcb.utils.DBContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.*;

/**
 * REST API for staff to confirm full payment before check-in/check-out.
 *
 * POST /api/staff/payment/confirm
 *   Body: {"bookingId": 9, "amount": 110000}
 *
 * Rules:
 * - Booking must belong to staff's facility
 * - Invoice must exist and not already PAID
 * - paidAmount + amount must equal totalAmount exactly
 * - Updates Invoice: paidAmount = totalAmount, paymentStatus = 'PAID'
 */
@WebServlet(name = "StaffConfirmPaymentApiServlet", urlPatterns = {"/api/staff/payment/confirm"})
public class StaffConfirmPaymentApiServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json;charset=UTF-8");

        AuthResult auth = StaffAuthUtil.validateStaff(request, response);
        if (!auth.valid) return;

        // Parse JSON body
        String body = readBody(request);
        int bookingId = extractInt(body, "bookingId");
        BigDecimal amount = extractDecimal(body, "amount");

        if (bookingId <= 0) {
            response.setStatus(400);
            response.getWriter().print("{\"success\":false,\"message\":\"Thiếu bookingId\"}");
            return;
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            response.setStatus(400);
            response.getWriter().print("{\"success\":false,\"message\":\"Số tiền phải lớn hơn 0\"}");
            return;
        }

        try {
            String result = doConfirmPayment(bookingId, amount, auth.facilityId);
            response.getWriter().print(result);
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(500);
            response.getWriter().print("{\"success\":false,\"message\":\"Lỗi hệ thống\"}");
        }
    }

    private String doConfirmPayment(int bookingId, BigDecimal amount, int facilityId) throws Exception {
        try (Connection conn = DBContext.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // 1. Validate booking belongs to facility
                try (PreparedStatement ps = conn.prepareStatement(
                        "SELECT facility_id FROM Booking WHERE booking_id = ?")) {
                    ps.setInt(1, bookingId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            conn.rollback();
                            return "{\"success\":false,\"message\":\"Không tìm thấy booking\"}";
                        }
                        if (rs.getInt("facility_id") != facilityId) {
                            conn.rollback();
                            return "{\"success\":false,\"message\":\"Booking không thuộc cơ sở của bạn\"}";
                        }
                    }
                }

                // 2. Get Invoice
                BigDecimal totalAmount;
                BigDecimal paidAmount;
                String paymentStatus;

                try (PreparedStatement ps = conn.prepareStatement(
                        "SELECT total_amount, paid_amount, payment_status FROM Invoice WHERE booking_id = ?")) {
                    ps.setInt(1, bookingId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            conn.rollback();
                            return "{\"success\":false,\"message\":\"Không tìm thấy hóa đơn cho booking này\"}";
                        }
                        totalAmount = rs.getBigDecimal("total_amount");
                        paidAmount = rs.getBigDecimal("paid_amount");
                        paymentStatus = rs.getString("payment_status");
                    }
                }

                // 3. Check not already PAID
                if ("PAID".equals(paymentStatus)) {
                    conn.rollback();
                    return "{\"success\":false,\"message\":\"Booking đã được thanh toán đầy đủ\"}";
                }

                // 4. Validate amount: paidAmount + amount must equal totalAmount
                BigDecimal newPaidAmount = paidAmount.add(amount);
                if (newPaidAmount.compareTo(totalAmount) != 0) {
                    BigDecimal remaining = totalAmount.subtract(paidAmount);
                    conn.rollback();
                    return "{\"success\":false,\"message\":\"Số tiền không hợp lệ. Cần thu thêm đúng " +
                            formatMoney(remaining) + " để đủ tổng tiền.\"}";
                }

                // 5. Update Invoice
                try (PreparedStatement ps = conn.prepareStatement(
                        "UPDATE Invoice SET paid_amount = ?, payment_status = 'PAID' WHERE booking_id = ?")) {
                    ps.setBigDecimal(1, totalAmount);
                    ps.setInt(2, bookingId);
                    ps.executeUpdate();
                }

                conn.commit();
                return "{\"success\":true,\"message\":\"Xác nhận thanh toán thành công\"," +
                        "\"data\":{\"paidAmount\":" + totalAmount + ",\"paymentStatus\":\"PAID\"}}";

            } catch (Exception e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    private String formatMoney(BigDecimal amount) {
        if (amount == null) return "0đ";
        return String.format("%,.0f", amount) + "đ";
    }

    // ─── Parse simple JSON (no library) ───
    private String readBody(HttpServletRequest request) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) sb.append(line);
        }
        return sb.toString();
    }

    private int extractInt(String json, String key) {
        try {
            String search = "\"" + key + "\"";
            int idx = json.indexOf(search);
            if (idx < 0) return -1;
            int colonIdx = json.indexOf(":", idx + search.length());
            if (colonIdx < 0) return -1;

            StringBuilder num = new StringBuilder();
            for (int i = colonIdx + 1; i < json.length(); i++) {
                char c = json.charAt(i);
                if (c >= '0' && c <= '9') num.append(c);
                else if (num.length() > 0) break;
            }
            return num.length() > 0 ? Integer.parseInt(num.toString()) : -1;
        } catch (Exception e) {
            return -1;
        }
    }

    private BigDecimal extractDecimal(String json, String key) {
        try {
            String search = "\"" + key + "\"";
            int idx = json.indexOf(search);
            if (idx < 0) return null;
            int colonIdx = json.indexOf(":", idx + search.length());
            if (colonIdx < 0) return null;

            StringBuilder num = new StringBuilder();
            boolean hasDot = false;
            for (int i = colonIdx + 1; i < json.length(); i++) {
                char c = json.charAt(i);
                if (c >= '0' && c <= '9') {
                    num.append(c);
                } else if (c == '.' && !hasDot) {
                    num.append(c);
                    hasDot = true;
                } else if (num.length() > 0) {
                    break;
                }
            }
            return num.length() > 0 ? new BigDecimal(num.toString()) : null;
        } catch (Exception e) {
            return null;
        }
    }
}