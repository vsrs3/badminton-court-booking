package com.bcb.controller.staff;

import com.bcb.model.Account;
import com.bcb.utils.DBContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;

/**
 * REST API: GET /api/staff/booking/*
 * URL pattern: /api/staff/booking/{bookingId}
 * Returns full booking detail JSON for staff view.
 */
@WebServlet(name = "StaffBookingDetailApiServlet", urlPatterns = {"/api/staff/booking/*"})
public class StaffBookingDetailApiServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();

        // ─── Auth ───
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("account") == null) {
            response.setStatus(401);
            out.print("{\"success\":false,\"message\":\"Chưa đăng nhập\"}");
            return;
        }
        Account account = (Account) session.getAttribute("account");
        if (!"STAFF".equals(account.getRole())) {
            response.setStatus(403);
            out.print("{\"success\":false,\"message\":\"Không có quyền\"}");
            return;
        }
        Integer facilityId = (Integer) session.getAttribute("facilityId");
        if (facilityId == null) {
            response.setStatus(403);
            out.print("{\"success\":false,\"message\":\"Staff chưa được gán cơ sở\"}");
            return;
        }

        // ─── Parse booking ID from path ───
        String pathInfo = request.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            response.setStatus(400);
            out.print("{\"success\":false,\"message\":\"Thiếu booking ID\"}");
            return;
        }
        int bookingId;
        try {
            bookingId = Integer.parseInt(pathInfo.substring(1));
        } catch (NumberFormatException e) {
            response.setStatus(400);
            out.print("{\"success\":false,\"message\":\"Booking ID không hợp lệ\"}");
            return;
        }

        // ─── Build JSON ───
        try {
            String json = buildDetailJson(bookingId, facilityId);
            if (json == null) {
                response.setStatus(404);
                out.print("{\"success\":false,\"message\":\"Không tìm thấy booking\"}");
            } else {
                out.print(json);
            }
        } catch (Exception e) {
            System.out.println("❌ Booking Detail API error: " + e.getMessage());
            e.printStackTrace();
            response.setStatus(500);
            out.print("{\"success\":false,\"message\":\"Lỗi hệ thống\"}");
        }
    }

    private String buildDetailJson(int bookingId, int staffFacilityId) throws Exception {
        try (Connection conn = DBContext.getConnection()) {

            // ─── 1. Booking info ───
            String sqlBooking = """
                SELECT
                    b.booking_id, b.booking_date, b.booking_status,
                    b.created_at, b.checkin_time, b.checkout_time,
                    b.facility_id,
                    COALESCE(a.full_name, g.guest_name) AS customer_name,
                    COALESCE(a.phone, g.phone) AS customer_phone,
                    CASE WHEN b.account_id IS NOT NULL THEN 'ACCOUNT' ELSE 'GUEST' END AS customer_type
                FROM Booking b
                LEFT JOIN Account a ON b.account_id = a.account_id
                LEFT JOIN Guest g   ON b.guest_id = g.guest_id
                WHERE b.booking_id = ?
            """;

            StringBuilder json = new StringBuilder();
            int facilityIdFromBooking;
            String bookingDate, bookingStatus, createdAt, checkinTime, checkoutTime;
            String customerName, customerPhone, customerType;

            try (PreparedStatement ps = conn.prepareStatement(sqlBooking)) {
                ps.setInt(1, bookingId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) return null;

                    facilityIdFromBooking = rs.getInt("facility_id");
                    // Security: only allow staff to see bookings in their facility
                    if (facilityIdFromBooking != staffFacilityId) return null;

                    bookingDate = rs.getString("booking_date");
                    bookingStatus = rs.getString("booking_status");
                    createdAt = tsToStr(rs.getTimestamp("created_at"));
                    checkinTime = tsToStr(rs.getTimestamp("checkin_time"));
                    checkoutTime = tsToStr(rs.getTimestamp("checkout_time"));
                    customerName = rs.getString("customer_name");
                    customerPhone = rs.getString("customer_phone");
                    customerType = rs.getString("customer_type");
                }
            }

            json.append("{\"success\":true,\"data\":{");

            // Booking info
            json.append("\"bookingId\":").append(bookingId);
            json.append(",\"bookingDate\":\"").append(bookingDate).append("\"");
            json.append(",\"bookingStatus\":\"").append(bookingStatus).append("\"");
            json.append(",\"createdAt\":").append(escJson(createdAt));
            json.append(",\"checkinTime\":").append(escJson(checkinTime));
            json.append(",\"checkoutTime\":").append(escJson(checkoutTime));

            // Customer info
            json.append(",\"customerName\":").append(escJson(customerName));
            json.append(",\"customerPhone\":").append(escJson(customerPhone));
            json.append(",\"customerType\":\"").append(customerType).append("\"");

            // ─── 2. Slot list ───
            String sqlSlots = """
                SELECT
                    bs.booking_slot_id,
                    c.court_name,
                    ts.start_time,
                    ts.end_time,
                    bs.price,
                    bs.slot_status
                FROM BookingSlot bs
                JOIN Court c     ON bs.court_id = c.court_id
                JOIN TimeSlot ts ON bs.slot_id = ts.slot_id
                WHERE bs.booking_id = ?
                ORDER BY c.court_name, ts.start_time
            """;

            json.append(",\"slots\":[");
            boolean first = true;
            try (PreparedStatement ps = conn.prepareStatement(sqlSlots)) {
                ps.setInt(1, bookingId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        if (!first) json.append(",");
                        first = false;
                        json.append("{\"courtName\":").append(escJson(rs.getString("court_name")));
                        Time st = rs.getTime("start_time");
                        Time et = rs.getTime("end_time");
                        json.append(",\"startTime\":\"").append(st.toLocalTime().toString().substring(0, 5)).append("\"");
                        json.append(",\"endTime\":\"").append(et.toLocalTime().toString().substring(0, 5)).append("\"");
                        json.append(",\"price\":").append(rs.getBigDecimal("price"));
                        json.append(",\"slotStatus\":\"").append(rs.getString("slot_status")).append("\"");
                        json.append("}");
                    }
                }
            }
            json.append("]");

            // ─── 3. Invoice / Payment info ───
            String sqlInvoice = """
                SELECT total_amount, paid_amount, payment_status
                FROM Invoice
                WHERE booking_id = ?
            """;

            json.append(",\"invoice\":");
            try (PreparedStatement ps = conn.prepareStatement(sqlInvoice)) {
                ps.setInt(1, bookingId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        json.append("{\"totalAmount\":").append(rs.getBigDecimal("total_amount"));
                        json.append(",\"paidAmount\":").append(rs.getBigDecimal("paid_amount"));
                        json.append(",\"paymentStatus\":\"").append(rs.getString("payment_status")).append("\"}");
                    } else {
                        json.append("null");
                    }
                }
            }

            json.append("}}");
            return json.toString();
        }
    }

    private String tsToStr(Timestamp ts) {
        if (ts == null) return null;
        return ts.toLocalDateTime().toString().replace("T", " ").substring(0, 16);
    }

    private String escJson(String val) {
        if (val == null) return "null";
        return "\"" + val.replace("\\", "\\\\").replace("\"", "\\\"")
                         .replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t") + "\"";
    }
}