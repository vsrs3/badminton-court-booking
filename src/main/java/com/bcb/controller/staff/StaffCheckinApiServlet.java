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
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * REST API for staff check-in / check-out / no-show per SESSION (phiên chơi).
 *
 * POST /api/staff/checkin
 *   Body: {"bookingId": 7, "sessionIndex": 0}
 *
 * POST /api/staff/checkout
 *   Body: {"bookingId": 7, "sessionIndex": 0}
 *
 * POST /api/staff/noshow
 *   Body: {"bookingId": 7, "sessionIndex": 0}
 *
 * Rules:
 * - Booking must be CONFIRMED, booking_date = today
 * - Invoice must be PAID before check-in/check-out
 * - No-show: session must be PENDING and past start_time + 15min buffer
 * - Check-in: auto-mark NO_SHOW for earlier PENDING sessions that are past due
 * - Check-in: skip NO_SHOW sessions (no strict order required)
 * - When all sessions are CHECK_OUT or NO_SHOW → Booking = COMPLETED
 */
@WebServlet(name = "StaffCheckinApiServlet", urlPatterns = {
        "/api/staff/checkin",
        "/api/staff/checkout",
        "/api/staff/noshow"
})
public class StaffCheckinApiServlet extends HttpServlet {

    /** Buffer in minutes: session is "past due" if now > start_time + buffer */
    private static final int NO_SHOW_BUFFER_MINUTES = 15;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json;charset=UTF-8");

        AuthResult auth = StaffAuthUtil.validateStaff(request, response);
        if (!auth.valid) return;

        String path = request.getServletPath();
        boolean isCheckin  = path.contains("/checkin");
        boolean isCheckout = path.contains("/checkout");
        boolean isNoshow   = path.contains("/noshow");

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
            } else if (isCheckout) {
                result = doCheckout(bookingId, sessionIndex, auth.facilityId);
            } else {
                result = doNoShow(bookingId, sessionIndex, auth.facilityId);
            }
            response.getWriter().print(result);
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(500);
            response.getWriter().print("{\"success\":false,\"message\":\"Lỗi hệ thống\"}");
        }
    }

    // ═══════════════════════════════════════════════════════════════
    //  CHECK-IN — with auto NO_SHOW for past-due earlier sessions
    // ═══════════════════════════════════════════════════════════════

    private String doCheckin(int bookingId, int sessionIndex, int facilityId) throws Exception {
        try (Connection conn = DBContext.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // 1. Validate booking
                String valResult = validateBooking(conn, bookingId, facilityId);
                if (valResult != null) { conn.rollback(); return valResult; }

                // 2. Build sessions with time info
                List<SessionInfo> sessions = buildSessionsWithTime(conn, bookingId);
                if (sessionIndex >= sessions.size()) {
                    conn.rollback();
                    return "{\"success\":false,\"message\":\"Session index không hợp lệ\"}";
                }

                // 3. Check target session status
                SessionInfo target = sessions.get(sessionIndex);
                String targetStatus = getSessionStatus(conn, target.slotIds);

                if ("CHECKED_IN".equals(targetStatus) || "COMPLETED".equals(targetStatus)) {
                    conn.rollback();
                    return "{\"success\":false,\"message\":\"Phiên này đã được check-in\"}";
                }
                if ("NO_SHOW".equals(targetStatus)) {
                    conn.rollback();
                    return "{\"success\":false,\"message\":\"Phiên này đã được đánh dấu vắng mặt\"}";
                }

                // 4. Auto-mark NO_SHOW for earlier PENDING sessions that are past due
                LocalTime now = LocalTime.now();
                int autoNoShowCount = 0;

                for (int i = 0; i < sessionIndex; i++) {
                    SessionInfo prev = sessions.get(i);
                    String prevStatus = getSessionStatus(conn, prev.slotIds);

                    if ("PENDING".equals(prevStatus)) {
                        // Check if past due: now > session start_time + buffer
                        LocalTime deadline = prev.startTime.plusMinutes(NO_SHOW_BUFFER_MINUTES);
                        if (now.isAfter(deadline)) {
                            // Auto mark NO_SHOW
                            markSlotsNoShow(conn, prev.slotIds);
                            autoNoShowCount++;
                        } else {
                            // Still within buffer, can't skip
                            conn.rollback();
                            return "{\"success\":false,\"message\":\"Phiên " + (i + 1)
                                    + " chưa quá giờ. Vui lòng check-in phiên đó trước hoặc đợi hết giờ.\"}";
                        }
                    }
                    // If prev is CHECKED_IN, COMPLETED, or NO_SHOW → OK, skip
                }

                // 5. Update all slots in target session to CHECKED_IN
                Timestamp nowTs = new Timestamp(System.currentTimeMillis());
                for (int slotId : target.slotIds) {
                    try (PreparedStatement ps = conn.prepareStatement(
                            "UPDATE BookingSlot SET slot_status = 'CHECKED_IN', checkin_time = ? WHERE booking_slot_id = ?")) {
                        ps.setTimestamp(1, nowTs);
                        ps.setInt(2, slotId);
                        ps.executeUpdate();
                    }
                }

                conn.commit();
                String timeStr = nowTs.toLocalDateTime().toString().replace("T", " ").substring(0, 16);

                String msg = "Check-in phiên " + (sessionIndex + 1) + " thành công";
                if (autoNoShowCount > 0) {
                    msg += " (" + autoNoShowCount + " phiên trước đã tự động đánh dấu vắng mặt)";
                }

                return "{\"success\":true,\"message\":\"" + msg
                        + "\",\"data\":{\"checkinTime\":\"" + timeStr
                        + "\",\"autoNoShowCount\":" + autoNoShowCount + "}}";

            } catch (Exception e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════
    //  CHECK-OUT — also considers NO_SHOW for booking completion
    // ═══════════════════════════════════════════════════════════════

    private String doCheckout(int bookingId, int sessionIndex, int facilityId) throws Exception {
        try (Connection conn = DBContext.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // 1. Validate booking
                String valResult = validateBooking(conn, bookingId, facilityId);
                if (valResult != null) { conn.rollback(); return valResult; }

                // 2. Build sessions
                List<SessionInfo> sessions = buildSessionsWithTime(conn, bookingId);
                if (sessionIndex >= sessions.size()) {
                    conn.rollback();
                    return "{\"success\":false,\"message\":\"Session index không hợp lệ\"}";
                }

                // 3. Check session is checked in
                SessionInfo target = sessions.get(sessionIndex);
                String targetStatus = getSessionStatus(conn, target.slotIds);

                if ("PENDING".equals(targetStatus)) {
                    conn.rollback();
                    return "{\"success\":false,\"message\":\"Phiên chưa được check-in\"}";
                }
                if ("COMPLETED".equals(targetStatus)) {
                    conn.rollback();
                    return "{\"success\":false,\"message\":\"Phiên đã được check-out\"}";
                }
                if ("NO_SHOW".equals(targetStatus)) {
                    conn.rollback();
                    return "{\"success\":false,\"message\":\"Phiên đã được đánh dấu vắng mặt\"}";
                }

                // 4. Update all slots in this session to CHECK_OUT
                Timestamp nowTs = new Timestamp(System.currentTimeMillis());
                for (int slotId : target.slotIds) {
                    try (PreparedStatement ps = conn.prepareStatement(
                            "UPDATE BookingSlot SET slot_status = 'CHECK_OUT', checkout_time = ? WHERE booking_slot_id = ?")) {
                        ps.setTimestamp(1, nowTs);
                        ps.setInt(2, slotId);
                        ps.executeUpdate();
                    }
                }

                // 5. Check if ALL sessions are finished (CHECK_OUT or NO_SHOW)
                boolean allFinished = checkAllSessionsFinished(conn, sessions, sessionIndex, "COMPLETED");

                if (allFinished) {
                    try (PreparedStatement ps = conn.prepareStatement(
                            "UPDATE Booking SET booking_status = 'COMPLETED' WHERE booking_id = ?")) {
                        ps.setInt(1, bookingId);
                        ps.executeUpdate();
                    }
                }

                conn.commit();
                String timeStr = nowTs.toLocalDateTime().toString().replace("T", " ").substring(0, 16);
                return "{\"success\":true,\"message\":\"Check-out phiên " + (sessionIndex + 1) + " thành công\""
                        + ",\"data\":{\"checkoutTime\":\"" + timeStr
                        + "\",\"bookingCompleted\":" + allFinished + "}}";

            } catch (Exception e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    // ═══════════════════��═══════════════════════════════════════════
    //  NO-SHOW — staff manually marks a session as no-show
    // ═══════════════════════════════════════════════════════════════

    private String doNoShow(int bookingId, int sessionIndex, int facilityId) throws Exception {
        try (Connection conn = DBContext.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // 1. Validate booking (same rules: CONFIRMED, today, same facility)
                String valResult = validateBookingForNoShow(conn, bookingId, facilityId);
                if (valResult != null) { conn.rollback(); return valResult; }

                // 2. Build sessions
                List<SessionInfo> sessions = buildSessionsWithTime(conn, bookingId);
                if (sessionIndex >= sessions.size()) {
                    conn.rollback();
                    return "{\"success\":false,\"message\":\"Session index không hợp lệ\"}";
                }

                // 3. Check session status: must be PENDING
                SessionInfo target = sessions.get(sessionIndex);
                String targetStatus = getSessionStatus(conn, target.slotIds);

                if (!"PENDING".equals(targetStatus)) {
                    conn.rollback();
                    String label = statusLabel(targetStatus);
                    return "{\"success\":false,\"message\":\"Không thể đánh dấu vắng. Phiên đang ở trạng thái: " + label + "\"}";
                }

                // 4. Check if session is past due (start_time + buffer)
                LocalTime now = LocalTime.now();
                LocalTime deadline = target.startTime.plusMinutes(NO_SHOW_BUFFER_MINUTES);
                if (!now.isAfter(deadline)) {
                    conn.rollback();
                    String deadlineStr = deadline.toString().substring(0, 5);
                    return "{\"success\":false,\"message\":\"Chưa quá giờ. Chỉ có thể đánh dấu vắng sau " + deadlineStr + "\"}";
                }

                // 5. Mark all slots in session as NO_SHOW
                markSlotsNoShow(conn, target.slotIds);

                // 6. Check if ALL sessions are finished
                boolean allFinished = checkAllSessionsFinished(conn, sessions, sessionIndex, "NO_SHOW");

                if (allFinished) {
                    try (PreparedStatement ps = conn.prepareStatement(
                            "UPDATE Booking SET booking_status = 'COMPLETED' WHERE booking_id = ?")) {
                        ps.setInt(1, bookingId);
                        ps.executeUpdate();
                    }
                }

                conn.commit();
                return "{\"success\":true,\"message\":\"Đã đánh dấu vắng mặt phiên " + (sessionIndex + 1) + "\""
                        + ",\"data\":{\"bookingCompleted\":" + allFinished + "}}";

            } catch (Exception e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════
    //  HELPER: Mark slots as NO_SHOW
    // ═══════════════════════════════════════════════════════════════

    private void markSlotsNoShow(Connection conn, List<Integer> slotIds) throws SQLException {
        for (int slotId : slotIds) {
            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE BookingSlot SET slot_status = 'NO_SHOW' WHERE booking_slot_id = ?")) {
                ps.setInt(1, slotId);
                ps.executeUpdate();
            }
        }
    }

    /**
     * Check if ALL sessions are finished (CHECK_OUT or NO_SHOW).
     * @param justFinishedIndex the session we just processed
     * @param justFinishedAs what that session just became ("COMPLETED" or "NO_SHOW")
     */
    private boolean checkAllSessionsFinished(Connection conn, List<SessionInfo> sessions,
                                             int justFinishedIndex, String justFinishedAs) throws SQLException {
        for (int i = 0; i < sessions.size(); i++) {
            String status;
            if (i == justFinishedIndex) {
                status = justFinishedAs;
            } else {
                status = getSessionStatus(conn, sessions.get(i).slotIds);
            }
            if (!"COMPLETED".equals(status) && !"NO_SHOW".equals(status)) {
                return false;
            }
        }
        return true;
    }

    // ═══════════════════════════════════════════════════════════════
    //  VALIDATE BOOKING
    // ═══════════════════════════════════════════════════════════════

    /** Validate for check-in / check-out: CONFIRMED, today, same facility, PAID */
    private String validateBooking(Connection conn, int bookingId, int facilityId) throws SQLException {
        String base = validateBookingBase(conn, bookingId, facilityId);
        if (base != null) return base;

        // Payment must be PAID
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
        return null;
    }

    /** Validate for no-show: CONFIRMED, today, same facility (no payment check) */
    private String validateBookingForNoShow(Connection conn, int bookingId, int facilityId) throws SQLException {
        return validateBookingBase(conn, bookingId, facilityId);
    }

    /** Base validation: booking exists, CONFIRMED, today, same facility */
    private String validateBookingBase(Connection conn, int bookingId, int facilityId) throws SQLException {
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
        return null;
    }

    // ═══════════════════════════════════════════════════════════════
    //  BUILD SESSIONS — now includes start_time for NO_SHOW logic
    // ═══════════════════════════════════════════════════════════════

    /**
     * Session info: slot IDs + start/end time of the session.
     */
    private static class SessionInfo {
        List<Integer> slotIds;
        LocalTime startTime;
        LocalTime endTime;
    }

    private List<SessionInfo> buildSessionsWithTime(Connection conn, int bookingId) throws SQLException {
        String sql = """
            SELECT bs.booking_slot_id, bs.court_id, ts.start_time, ts.end_time
            FROM BookingSlot bs
            JOIN TimeSlot ts ON bs.slot_id = ts.slot_id
            WHERE bs.booking_id = ?
            ORDER BY bs.court_id, ts.start_time
        """;

        List<int[]> slotData = new ArrayList<>();     // [booking_slot_id, court_id]
        List<Time[]> slotTimes = new ArrayList<>();    // [start, end]

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookingId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    slotData.add(new int[]{rs.getInt("booking_slot_id"), rs.getInt("court_id")});
                    slotTimes.add(new Time[]{rs.getTime("start_time"), rs.getTime("end_time")});
                }
            }
        }

        // Group into sessions (same court, consecutive time)
        List<List<Integer>> rawSessions = new ArrayList<>();
        List<int[]> sessionRanges = new ArrayList<>(); // [firstIndex, lastIndex] in slotData

        if (slotData.isEmpty()) return new ArrayList<>();

        List<Integer> current = new ArrayList<>();
        current.add(slotData.get(0)[0]);
        int firstIdx = 0;

        for (int i = 1; i < slotData.size(); i++) {
            int prevCourt = slotData.get(i - 1)[1];
            int currCourt = slotData.get(i)[1];
            Time prevEnd = slotTimes.get(i - 1)[1];
            Time currStart = slotTimes.get(i)[0];

            if (prevCourt == currCourt && prevEnd.equals(currStart)) {
                current.add(slotData.get(i)[0]);
            } else {
                rawSessions.add(current);
                sessionRanges.add(new int[]{firstIdx, i - 1});
                current = new ArrayList<>();
                current.add(slotData.get(i)[0]);
                firstIdx = i;
            }
        }
        rawSessions.add(current);
        sessionRanges.add(new int[]{firstIdx, slotData.size() - 1});

        // Build SessionInfo with sort by start_time
        List<SessionInfo> sessions = new ArrayList<>();
        for (int i = 0; i < rawSessions.size(); i++) {
            SessionInfo si = new SessionInfo();
            si.slotIds = rawSessions.get(i);
            int[] range = sessionRanges.get(i);
            si.startTime = slotTimes.get(range[0])[0].toLocalTime();
            si.endTime = slotTimes.get(range[1])[1].toLocalTime();
            sessions.add(si);
        }

        sessions.sort((a, b) -> a.startTime.compareTo(b.startTime));
        return sessions;
    }

    // ═══════════════════════════════════════════════════════════════
    //  GET SESSION STATUS — now recognizes NO_SHOW
    // ═══════════════════════════════════════════════════════════════

    private String getSessionStatus(Connection conn, List<Integer> slotIds) throws SQLException {
        if (slotIds.isEmpty()) return "PENDING";

        StringBuilder inClause = new StringBuilder();
        for (int i = 0; i < slotIds.size(); i++) {
            if (i > 0) inClause.append(",");
            inClause.append("?");
        }

        boolean allCheckout = true;
        boolean allNoShow = true;
        boolean anyCheckedIn = false;
        boolean anyNoShow = false;

        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT slot_status FROM BookingSlot WHERE booking_slot_id IN (" + inClause + ")")) {
            for (int i = 0; i < slotIds.size(); i++) {
                ps.setInt(i + 1, slotIds.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String s = rs.getString("slot_status");
                    if (!"CHECK_OUT".equals(s)) allCheckout = false;
                    if (!"NO_SHOW".equals(s)) allNoShow = false;
                    if ("CHECKED_IN".equals(s)) anyCheckedIn = true;
                    if ("NO_SHOW".equals(s)) anyNoShow = true;
                }
            }
        }

        if (allNoShow) return "NO_SHOW";
        if (allCheckout) return "COMPLETED";
        if (anyCheckedIn) return "CHECKED_IN";
        return "PENDING";
    }

    private String statusLabel(String status) {
        switch (status) {
            case "CHECKED_IN": return "Đang chơi";
            case "COMPLETED":  return "Hoàn thành";
            case "NO_SHOW":    return "Vắng mặt";
            default:           return status;
        }
    }

    // ═══════════════════════════════════════════════════════════════
    //  JSON PARSING HELPERS
    // ═══════════════════════════════════════════════════════════════

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