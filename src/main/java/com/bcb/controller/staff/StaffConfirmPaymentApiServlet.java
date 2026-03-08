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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * REST API for staff to confirm full payment before check-in/check-out.
 *
 * POST /api/staff/payment/confirm
 * Body: {"bookingId": 9, "amount": 110000, "method":"CASH"}
 */
@WebServlet(name = "StaffConfirmPaymentApiServlet", urlPatterns = {"/api/staff/payment/confirm"})
public class StaffConfirmPaymentApiServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json;charset=UTF-8");

        AuthResult auth = StaffAuthUtil.validateStaff(request, response);
        if (!auth.valid) return;

        String body = readBody(request);
        int bookingId = extractInt(body, "bookingId");
        BigDecimal amount = extractDecimal(body, "amount");
        String method = extractString(body, "method");

        if (bookingId <= 0) {
            response.setStatus(400);
            response.getWriter().print("{\"success\":false,\"message\":\"Thieu bookingId\"}");
            return;
        }

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            response.setStatus(400);
            response.getWriter().print("{\"success\":false,\"message\":\"So tien phai lon hon 0\"}");
            return;
        }

        if (method == null || method.trim().isEmpty()) method = "CASH";
        method = method.trim().toUpperCase();
        if (!"CASH".equals(method) && !"BANK_TRANSFER".equals(method) && !"VNPAY".equals(method)) {
            response.setStatus(400);
            response.getWriter().print("{\"success\":false,\"message\":\"Phuong thuc thanh toan khong hop le\"}");
            return;
        }

        Integer staffId = (Integer) request.getSession().getAttribute("staffId");
        if (staffId == null || staffId <= 0) {
            response.setStatus(403);
            response.getWriter().print("{\"success\":false,\"message\":\"Khong xac dinh duoc staff\"}");
            return;
        }

        try {
            String result = doConfirmPayment(bookingId, amount, method, auth.facilityId, staffId);
            response.getWriter().print(result);
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(500);
            response.getWriter().print("{\"success\":false,\"message\":\"Loi he thong\"}");
        }
    }

    private String doConfirmPayment(int bookingId, BigDecimal amount, String method,
                                    int facilityId, int staffId) throws Exception {
        try (Connection conn = DBContext.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // 1) Validate booking belongs to staff facility
                try (PreparedStatement ps = conn.prepareStatement(
                        "SELECT facility_id FROM Booking WHERE booking_id = ?")) {
                    ps.setInt(1, bookingId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            conn.rollback();
                            return "{\"success\":false,\"message\":\"Khong tim thay booking\"}";
                        }
                        if (rs.getInt("facility_id") != facilityId) {
                            conn.rollback();
                            return "{\"success\":false,\"message\":\"Booking khong thuoc co so cua ban\"}";
                        }
                    }
                }

                // 2) Lock and read invoice
                int invoiceId;
                BigDecimal totalAmount;
                BigDecimal paidAmount;
                String paymentStatus;
                try (PreparedStatement ps = conn.prepareStatement(
                        "SELECT invoice_id, total_amount, paid_amount, payment_status " +
                                "FROM Invoice WITH (UPDLOCK, ROWLOCK) WHERE booking_id = ?")) {
                    ps.setInt(1, bookingId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            conn.rollback();
                            return "{\"success\":false,\"message\":\"Khong tim thay hoa don cho booking nay\"}";
                        }
                        invoiceId = rs.getInt("invoice_id");
                        totalAmount = rs.getBigDecimal("total_amount");
                        paidAmount = rs.getBigDecimal("paid_amount");
                        paymentStatus = rs.getString("payment_status");
                    }
                }

                // 3) Already paid => reject (no insert)
                if ("PAID".equals(paymentStatus)) {
                    conn.rollback();
                    return "{\"success\":false,\"message\":\"Booking da duoc thanh toan day du\"}";
                }

                // 4) Validate amount must be exactly remaining
                BigDecimal newPaidAmount = paidAmount.add(amount);
                if (newPaidAmount.compareTo(totalAmount) != 0) {
                    BigDecimal remaining = totalAmount.subtract(paidAmount);
                    conn.rollback();
                    return "{\"success\":false,\"message\":\"So tien khong hop le. Can thu them dung "
                            + formatMoney(remaining) + " de du tong tien.\"}";
                }

                // 5) Insert manual payment log
                String paymentType = amount.compareTo(totalAmount) == 0 ? "FULL" : "REMAINING";
                try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO Payment (invoice_id, paid_amount, payment_time, payment_type, method, payment_status, staff_confirm_id, confirm_time) " +
                                "VALUES (?, ?, GETDATE(), ?, ?, 'SUCCESS', ?, GETDATE())")) {
                    ps.setInt(1, invoiceId);
                    ps.setBigDecimal(2, amount);
                    ps.setString(3, paymentType);
                    ps.setString(4, method);
                    ps.setInt(5, staffId);
                    ps.executeUpdate();
                }

                // 6) Update invoice
                try (PreparedStatement ps = conn.prepareStatement(
                        "UPDATE Invoice SET paid_amount = ?, payment_status = 'PAID' WHERE booking_id = ?")) {
                    ps.setBigDecimal(1, totalAmount);
                    ps.setInt(2, bookingId);
                    ps.executeUpdate();
                }

                conn.commit();
                return "{\"success\":true,\"message\":\"Xac nhan thanh toan thanh cong\"," +
                        "\"data\":{\"paidAmount\":" + totalAmount + ",\"paymentStatus\":\"PAID\",\"method\":\"" + method + "\"}}";
            } catch (Exception e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    private String formatMoney(BigDecimal amount) {
        if (amount == null) return "0d";
        return String.format("%,.0f", amount) + "d";
    }

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

    private String extractString(String json, String key) {
        try {
            String search = "\"" + key + "\"";
            int idx = json.indexOf(search);
            if (idx < 0) return null;
            int colonIdx = json.indexOf(":", idx + search.length());
            if (colonIdx < 0) return null;

            int firstQuote = json.indexOf("\"", colonIdx + 1);
            if (firstQuote < 0) return null;
            int endQuote = json.indexOf("\"", firstQuote + 1);
            if (endQuote < 0) return null;
            return json.substring(firstQuote + 1, endQuote);
        } catch (Exception e) {
            return null;
        }
    }
}

