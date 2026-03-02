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
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * REST API for staff check-in / check-out per SESSION (phiên chơi).
 *
 * POST /api/staff/checkin
 *   Body: {"bookingId": 7, "sessionIndex": 0}
 *
 * POST /api/staff/checkout
 *   Body: {"bookingId": 7, "sessionIndex": 0}
 *
 * Rules:
 * - Booking must be CONFIRMED, booking_date = today
 * - Invoice must be PAID (payment confirmed) before check-in/check-out
 * - Check-in must follow session time order (earlier sessions first)
 * - Check-out only for sessions already checked in
 * - When all sessions are checked out → Booking.booking_status = COMPLETED
 */
@WebServlet(name = "StaffCheckinApiServlet", urlPatterns = {
        "/api/staff/checkin",
        "/api/staff/checkout"
})
public class StaffCheckinApiServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json;charset=UTF-8");

        AuthResult auth = StaffAuthUtil.validateStaff(request, response);
        if (!auth.valid) return;

        boolean isCheckin = request.getServletPath().contains("/checkin");

        // ─── Parse JSON body ───
        String body = readBody(request);
        int bookingId = extractInt(body, "bookingId");
        int sessionIndex = extractInt(body, "sessionIndex");

        if (bookingId <= 0 || sessionIndex < 0) {
            response.setStatus(400);
            response.getWriter().print("{\"success\":false,\"message\":\"Thiếu bookingId hoặc sessionIndex\"}");
            return;
        }

        try {
            String result;
            if (isCheckin) {
                result = doCheckin(bookingId, sessionIndex, auth.facilityId);
            } else {
                result = doCheckout(bookingId, sessionIndex, auth.facilityId);
            }
            response.getWriter().print(result);
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(500);
            response.getWriter().print("{\"success\":false,\"message\":\"Lỗi hệ thống\"}");
        }
    }

    private String doCheckin(int bookingId, int sessionIndex, int facilityId) throws Exception {
        try (Connection conn = DBContext.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // 1. Validate booking
                String valResult = validateBooking(conn, bookingId, facilityId);
                if (valResult != null) { conn.rollback(); return valResult; }

                // 2. Build sessions
                List<List<Integer>> sessions = buildSessions(conn, bookingId);
                if (sessionIndex >= sessions.size()) {
                    conn.rollback();
                    return "{\"success\":false,\"message\":\"Session index không hợp lệ\"}";
                }

                // 3. Check session status
                List<Integer> targetSlotIds = sessions.get(sessionIndex);
                String targetStatus = getSessionStatus(conn, targetSlotIds);

                if ("CHECKED_IN".equals(targetStatus) || "COMPLETED".equals(targetStatus)) {
                    conn.rollback();
                    return "{\"success\":false,\"message\":\"Phiên này đã được check-in\"}";
                }

                // 4. Validate order: all previous sessions must be checked in or completed
                for (int i = 0; i < sessionIndex; i++) {
                    String prevStatus = getSessionStatus(conn, sessions.get(i));
                    if ("PENDING".equals(prevStatus)) {
                        conn.rollback();
                        return "{\"success\":false,\"message\":\"Phải check-in phiên " + (i + 1) + " trước\"}";
                    }
                }

                // 5. Update all slots in this session
                Timestamp now = new Timestamp(System.currentTimeMillis());
                for (int slotId : targetSlotIds) {
                    try (PreparedStatement ps = conn.prepareStatement(
                            "UPDATE BookingSlot SET slot_status = 'CHECKED_IN', checkin_time = ? WHERE booking_slot_id = ?")) {
                        ps.setTimestamp(1, now);
                        ps.setInt(2, slotId);
                        ps.executeUpdate();
                    }
                }

                conn.commit();
                String timeStr = now.toLocalDateTime().toString().replace("T", " ").substring(0, 16);
                return "{\"success\":true,\"message\":\"Check-in phiên " + (sessionIndex + 1) + " thành công\",\"data\":{\"checkinTime\":\"" + timeStr + "\"}}";

            } catch (Exception e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    private String doCheckout(int bookingId, int sessionIndex, int facilityId) throws Exception {
        try (Connection conn = DBContext.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // 1. Validate booking
                String valResult = validateBooking(conn, bookingId, facilityId);
                if (valResult != null) { conn.rollback(); return valResult; }

                // 2. Build sessions
                List<List<Integer>> sessions = buildSessions(conn, bookingId);
                if (sessionIndex >= sessions.size()) {
                    conn.rollback();
                    return "{\"success\":false,\"message\":\"Session index không hợp lệ\"}";
                }

                // 3. Check session is checked in
                List<Integer> targetSlotIds = sessions.get(sessionIndex);
                String targetStatus = getSessionStatus(conn, targetSlotIds);

                if ("PENDING".equals(targetStatus)) {
                    conn.rollback();
                    return "{\"success\":false,\"message\":\"Phiên chưa được check-in\"}";
                }
                if ("COMPLETED".equals(targetStatus)) {
                    conn.rollback();
                    return "{\"success\":false,\"message\":\"Phiên đã được check-out\"}";
                }

                // 4. Update all slots in this session
                Timestamp now = new Timestamp(System.currentTimeMillis());
                for (int slotId : targetSlotIds) {
                    try (PreparedStatement ps = conn.prepareStatement(
                            "UPDATE BookingSlot SET slot_status = 'CHECK_OUT', checkout_time = ? WHERE booking_slot_id = ?")) {
                        ps.setTimestamp(1, now);
                        ps.setInt(2, slotId);
                        ps.executeUpdate();
                    }
                }

                // 5. Check if ALL sessions are completed → mark booking COMPLETED
                boolean allCompleted = true;
                for (int i = 0; i < sessions.size(); i++) {
                    String status;
                    if (i == sessionIndex) {
                        status = "COMPLETED"; // just checked out
                    } else {
                        status = getSessionStatus(conn, sessions.get(i));
                    }
                    if (!"COMPLETED".equals(status)) {
                        allCompleted = false;
                        break;
                    }
                }

                if (allCompleted) {
                    try (PreparedStatement ps = conn.prepareStatement(
                            "UPDATE Booking SET booking_status = 'COMPLETED' WHERE booking_id = ?")) {
                        ps.setInt(1, bookingId);
                        ps.executeUpdate();
                    }
                }

                conn.commit();
                String timeStr = now.toLocalDateTime().toString().replace("T", " ").substring(0, 16);
                return "{\"success\":true,\"message\":\"Check-out phiên " + (sessionIndex + 1) + " thành công\"" +
                        ",\"data\":{\"checkoutTime\":\"" + timeStr + "\",\"bookingCompleted\":" + allCompleted + "}}";

            } catch (Exception e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    // ─── Validate booking: CONFIRMED, today, same facility, PAID ───
    private String validateBooking(Connection conn, int bookingId, int facilityId) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT booking_status, booking_date, facility_id FROM Booking WHERE booking_id = ?")) {
            ps.setInt(1, bookingId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return "{\"success\":false,\"message\":\"Không tìm thấy booking\"}";

                if (rs.getInt("facility_id") != facilityId)
                    return "{\"success\":false,\"message\":\"Booking không thuộc cơ sở của bạn\"}";

                String status = rs.getString("booking_status");
                if (!"CONFIRMED".equals(status))
                    return "{\"success\":false,\"message\":\"Chỉ xử lý booking đã xác nhận. Trạng thái: " + status + "\"}";

                Date bDate = rs.getDate("booking_date");
                if (!bDate.toLocalDate().equals(LocalDate.now()))
                    return "{\"success\":false,\"message\":\"Chỉ check-in/out booking ngày hôm nay\"}";
            }
        }

        // ─── Validate payment status: must be PAID ───
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT payment_status FROM Invoice WHERE booking_id = ?")) {
            ps.setInt(1, bookingId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return "{\"success\":false,\"message\":\"Không tìm thấy hóa đơn cho booking này\"}";
                }
                String paymentStatus = rs.getString("payment_status");
                if (!"PAID".equals(paymentStatus)) {
                    return "{\"success\":false,\"message\":\"Booking chưa thanh toán đủ. Vui lòng xác nhận thanh toán trước khi check-in/check-out.\"}";
                }
            }
        }

        return null; // OK
    }

    /**
     * Build sessions from BookingSlot.
     * Returns list of sessions, each session = list of booking_slot_id.
     * Sessions sorted by start_time.
     */
    private List<List<Integer>> buildSessions(Connection conn, int bookingId) throws SQLException {
        String sql = """
            SELECT bs.booking_slot_id, bs.court_id, ts.start_time, ts.end_time
            FROM BookingSlot bs
            JOIN TimeSlot ts ON bs.slot_id = ts.slot_id
            WHERE bs.booking_id = ?
            ORDER BY bs.court_id, ts.start_time
        """;

        List<int[]> slotData = new ArrayList<>(); // [booking_slot_id, court_id]
        List<Time[]> slotTimes = new ArrayList<>(); // [start, end]

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookingId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    slotData.add(new int[]{rs.getInt("booking_slot_id"), rs.getInt("court_id")});
                    slotTimes.add(new Time[]{rs.getTime("start_time"), rs.getTime("end_time")});
                }
            }
        }

        // Group into sessions
        List<List<Integer>> sessions = new ArrayList<>();
        if (slotData.isEmpty()) return sessions;

        List<Integer> current = new ArrayList<>();
        current.add(slotData.get(0)[0]);
        Time[] currentStartEnd = new Time[]{slotTimes.get(0)[0], slotTimes.get(0)[1]};

        for (int i = 1; i < slotData.size(); i++) {
            int prevCourt = slotData.get(i - 1)[1];
            int currCourt = slotData.get(i)[1];
            Time prevEnd = slotTimes.get(i - 1)[1];
            Time currStart = slotTimes.get(i)[0];

            if (prevCourt == currCourt && prevEnd.equals(currStart)) {
                current.add(slotData.get(i)[0]);
            } else {
                sessions.add(current);
                current = new ArrayList<>();
                current.add(slotData.get(i)[0]);
            }
        }
        sessions.add(current);

        // Sort by first slot's start_time
        sessions.sort((a, b) -> {
            int idxA = findSlotIndex(slotData, a.get(0));
            int idxB = findSlotIndex(slotData, b.get(0));
            return slotTimes.get(idxA)[0].compareTo(slotTimes.get(idxB)[0]);
        });

        return sessions;
    }

    private int findSlotIndex(List<int[]> slotData, int bookingSlotId) {
        for (int i = 0; i < slotData.size(); i++) {
            if (slotData.get(i)[0] == bookingSlotId) return i;
        }
        return 0;
    }

    /**
     * Get session status from slot statuses.
     */
    private String getSessionStatus(Connection conn, List<Integer> slotIds) throws SQLException {
        if (slotIds.isEmpty()) return "PENDING";

        StringBuilder inClause = new StringBuilder();
        for (int i = 0; i < slotIds.size(); i++) {
            if (i > 0) inClause.append(",");
            inClause.append("?");
        }

        boolean allCheckout = true;
        boolean anyCheckedIn = false;

        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT slot_status FROM BookingSlot WHERE booking_slot_id IN (" + inClause + ")")) {
            for (int i = 0; i < slotIds.size(); i++) {
                ps.setInt(i + 1, slotIds.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String s = rs.getString("slot_status");
                    if (!"CHECK_OUT".equals(s)) allCheckout = false;
                    if ("CHECKED_IN".equals(s)) anyCheckedIn = true;
                }
            }
        }

        if (allCheckout) return "COMPLETED";
        if (anyCheckedIn) return "CHECKED_IN";
        return "PENDING";
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
}