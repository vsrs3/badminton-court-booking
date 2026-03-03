package com.bcb.controller.staff;

import com.bcb.controller.staff.StaffAuthUtil.AuthResult;
import com.bcb.utils.DBContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * REST API: GET /api/staff/booking/detail/{bookingId}
 *
 * Returns booking detail with slots grouped into "sessions" (phiên chơi).
 * A session = 2+ consecutive slots on the same court (no time gap).
 */
@WebServlet(name = "StaffBookingDetailApiServlet", urlPatterns = {"/api/staff/booking/detail/*"})
public class StaffBookingDetailApiServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json;charset=UTF-8");

        AuthResult auth = StaffAuthUtil.validateStaff(request, response);
        if (!auth.valid) return;

        String pathInfo = request.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            response.setStatus(400);
            response.getWriter().print("{\"success\":false,\"message\":\"Thiếu booking ID\"}");
            return;
        }

        int bookingId;
        try {
            bookingId = Integer.parseInt(pathInfo.substring(1));
        } catch (NumberFormatException e) {
            response.setStatus(400);
            response.getWriter().print("{\"success\":false,\"message\":\"Booking ID không hợp lệ\"}");
            return;
        }

        try {
            String json = buildDetailJson(bookingId, auth.facilityId);
            if (json == null) {
                response.setStatus(404);
                response.getWriter().print("{\"success\":false,\"message\":\"Không tìm thấy booking\"}");
            } else {
                response.getWriter().print(json);
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(500);
            response.getWriter().print("{\"success\":false,\"message\":\"Lỗi hệ thống\"}");
        }
    }

    private String buildDetailJson(int bookingId, int staffFacilityId) throws Exception {
        try (Connection conn = DBContext.getConnection()) {

            // ─── 1. Booking info ───
            String sqlBooking = """
                SELECT b.booking_id, b.booking_date, b.booking_status, b.created_at, b.facility_id,
                       COALESCE(a.full_name, g.guest_name) AS customer_name,
                       COALESCE(a.phone, g.phone) AS customer_phone,
                       CASE WHEN b.account_id IS NOT NULL THEN 'ACCOUNT' ELSE 'GUEST' END AS customer_type
                FROM Booking b
                LEFT JOIN Account a ON b.account_id = a.account_id
                LEFT JOIN Guest g   ON b.guest_id = g.guest_id
                WHERE b.booking_id = ?
            """;

            StringBuilder json = new StringBuilder(2048);
            String bookingDate, bookingStatus, createdAt;
            String customerName, customerPhone, customerType;

            try (PreparedStatement ps = conn.prepareStatement(sqlBooking)) {
                ps.setInt(1, bookingId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) return null;
                    if (rs.getInt("facility_id") != staffFacilityId) return null;

                    bookingDate = rs.getString("booking_date");
                    bookingStatus = rs.getString("booking_status");
                    createdAt = tsToStr(rs.getTimestamp("created_at"));
                    customerName = rs.getString("customer_name");
                    customerPhone = rs.getString("customer_phone");
                    customerType = rs.getString("customer_type");
                }
            }

            json.append("{\"success\":true,\"data\":{");
            json.append("\"bookingId\":").append(bookingId);
            json.append(",\"bookingDate\":\"").append(bookingDate).append("\"");
            json.append(",\"bookingStatus\":\"").append(bookingStatus).append("\"");
            json.append(",\"createdAt\":").append(esc(createdAt));
            json.append(",\"customerName\":").append(esc(customerName));
            json.append(",\"customerPhone\":").append(esc(customerPhone));
            json.append(",\"customerType\":\"").append(customerType).append("\"");

            // ─── 2. Fetch all slots ordered for session grouping ───
            String sqlSlots = """
                SELECT bs.booking_slot_id, bs.court_id, c.court_name,
                       bs.slot_id, ts.start_time, ts.end_time,
                       bs.price, bs.slot_status, bs.checkin_time, bs.checkout_time
                FROM BookingSlot bs
                JOIN Court c     ON bs.court_id = c.court_id
                JOIN TimeSlot ts ON bs.slot_id = ts.slot_id
                WHERE bs.booking_id = ?
                ORDER BY c.court_name, ts.start_time
            """;

            List<SlotRow> allSlots = new ArrayList<>();
            try (PreparedStatement ps = conn.prepareStatement(sqlSlots)) {
                ps.setInt(1, bookingId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        SlotRow s = new SlotRow();
                        s.bookingSlotId = rs.getInt("booking_slot_id");
                        s.courtId = rs.getInt("court_id");
                        s.courtName = rs.getString("court_name");
                        s.slotId = rs.getInt("slot_id");
                        s.startTime = rs.getTime("start_time");
                        s.endTime = rs.getTime("end_time");
                        s.price = rs.getBigDecimal("price");
                        s.slotStatus = rs.getString("slot_status");
                        s.checkinTime = rs.getTimestamp("checkin_time");
                        s.checkoutTime = rs.getTimestamp("checkout_time");
                        allSlots.add(s);
                    }
                }
            }

            // ─── 3. Group into sessions ───
            List<List<SlotRow>> sessions = groupIntoSessions(allSlots);

            json.append(",\"sessions\":[");
            for (int i = 0; i < sessions.size(); i++) {
                if (i > 0) json.append(",");
                List<SlotRow> session = sessions.get(i);
                SlotRow first = session.get(0);
                SlotRow last = session.get(session.size() - 1);

                // Session status: derive from slot statuses
                String sessionStatus = deriveSessionStatus(session);

                // Checkin/checkout times from slots
                String checkinTimeStr = null;
                String checkoutTimeStr = null;
                if (first.checkinTime != null) checkinTimeStr = tsToStr(first.checkinTime);
                if (last.checkoutTime != null) checkoutTimeStr = tsToStr(last.checkoutTime);

                // Total price
                java.math.BigDecimal totalPrice = java.math.BigDecimal.ZERO;
                for (SlotRow s : session) {
                    if (s.price != null) totalPrice = totalPrice.add(s.price);
                }

                json.append("{\"sessionIndex\":").append(i);
                json.append(",\"courtId\":").append(first.courtId);
                json.append(",\"courtName\":").append(esc(first.courtName));
                json.append(",\"startTime\":\"").append(fmtTime(first.startTime)).append("\"");
                json.append(",\"endTime\":\"").append(fmtTime(last.endTime)).append("\"");
                json.append(",\"slotCount\":").append(session.size());
                json.append(",\"totalPrice\":").append(totalPrice);
                json.append(",\"sessionStatus\":\"").append(sessionStatus).append("\"");
                json.append(",\"checkinTime\":").append(esc(checkinTimeStr));
                json.append(",\"checkoutTime\":").append(esc(checkoutTimeStr));

                // Include slot IDs for reference
                json.append(",\"bookingSlotIds\":[");
                for (int j = 0; j < session.size(); j++) {
                    if (j > 0) json.append(",");
                    json.append(session.get(j).bookingSlotId);
                }
                json.append("]");

                json.append("}");
            }
            json.append("]");

            // ─── 4. Invoice ───
            json.append(",\"invoice\":");
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT total_amount, paid_amount, payment_status FROM Invoice WHERE booking_id = ?")) {
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

    /**
     * Group slots into sessions.
     * Session = consecutive slots (no time gap) on the same court.
     * Slots must be pre-sorted by court_name, start_time.
     */
    private List<List<SlotRow>> groupIntoSessions(List<SlotRow> slots) {
        List<List<SlotRow>> sessions = new ArrayList<>();
        if (slots.isEmpty()) return sessions;

        List<SlotRow> current = new ArrayList<>();
        current.add(slots.get(0));

        for (int i = 1; i < slots.size(); i++) {
            SlotRow prev = slots.get(i - 1);
            SlotRow curr = slots.get(i);

            // Same court AND start_time == prev.end_time → consecutive
            boolean sameCourt = (prev.courtId == curr.courtId);
            boolean consecutive = prev.endTime.equals(curr.startTime);

            if (sameCourt && consecutive) {
                current.add(curr);
            } else {
                sessions.add(current);
                current = new ArrayList<>();
                current.add(curr);
            }
        }
        sessions.add(current);

        // Sort sessions by start_time
        sessions.sort((a, b) -> a.get(0).startTime.compareTo(b.get(0).startTime));

        return sessions;
    }

    /**
     * Derive session status from slot statuses:
     * - All NO_SHOW → "NO_SHOW"
     * - All CHECK_OUT → "COMPLETED"
     * - Any CHECKED_IN → "CHECKED_IN"
     * - Otherwise → "PENDING" (waiting for check-in)
     */
    private String deriveSessionStatus(List<SlotRow> session) {
        boolean allCheckout = true;
        boolean allNoShow = true;
        boolean anyCheckedIn = false;

        for (SlotRow s : session) {
            if (!"CHECK_OUT".equals(s.slotStatus)) allCheckout = false;
            if (!"NO_SHOW".equals(s.slotStatus)) allNoShow = false;
            if ("CHECKED_IN".equals(s.slotStatus)) anyCheckedIn = true;
        }

        if (allNoShow) return "NO_SHOW";
        if (allCheckout) return "COMPLETED";
        if (anyCheckedIn) return "CHECKED_IN";
        return "PENDING";
    }

    private String fmtTime(Time t) {
        return t.toLocalTime().toString().substring(0, 5);
    }

    private String tsToStr(Timestamp ts) {
        if (ts == null) return null;
        return ts.toLocalDateTime().toString().replace("T", " ").substring(0, 16);
    }

    private String esc(String val) {
        return StaffAuthUtil.escapeJson(val);
    }

    // ─── Inner class for slot data ───
    private static class SlotRow {
        int bookingSlotId;
        int courtId;
        String courtName;
        int slotId;
        Time startTime;
        Time endTime;
        java.math.BigDecimal price;
        String slotStatus;
        Timestamp checkinTime;
        Timestamp checkoutTime;
    }
}