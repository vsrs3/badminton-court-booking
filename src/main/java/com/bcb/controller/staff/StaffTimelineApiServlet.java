package com.bcb.controller.staff;

import com.bcb.controller.staff.StaffAuthUtil.AuthResult;
import com.bcb.utils.DBContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * REST API: GET /api/staff/timeline?date=yyyy-MM-dd
 * Returns JSON for the staff timeline grid.
 *
 * Performance: Only returns BOOKED and DISABLED cells.
 * AVAILABLE cells are implied (frontend treats missing keys as available).
 */
@WebServlet(name = "StaffTimelineApiServlet", urlPatterns = {"/api/staff/timeline"})
public class StaffTimelineApiServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json;charset=UTF-8");

        // ─── Auth ───
        AuthResult auth = StaffAuthUtil.validateStaff(request, response);
        if (!auth.valid) return;

        // ─── Parse date ───
        String dateParam = request.getParameter("date");
        LocalDate bookingDate;
        try {
            bookingDate = (dateParam != null && !dateParam.isEmpty())
                    ? LocalDate.parse(dateParam)
                    : LocalDate.now();
        } catch (DateTimeParseException e) {
            response.setStatus(400);
            response.getWriter().print("{\"success\":false,\"message\":\"Ngày không hợp lệ\"}");
            return;
        }

        // ─── Build JSON ───
        try {
            String json = buildTimelineJson(auth.facilityId, bookingDate);
            response.getWriter().print(json);
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(500);
            response.getWriter().print("{\"success\":false,\"message\":\"Lỗi hệ thống\"}");
        }
    }

    private String buildTimelineJson(int facilityId, LocalDate bookingDate) throws Exception {
        StringBuilder json = new StringBuilder(2048);
        json.append("{\"success\":true,\"data\":{");

        try (Connection conn = DBContext.getConnection()) {

            // ─── 1. Facility info ───
            String facilityName = "";
            String openTimeStr = null;
            String closeTimeStr = null;

            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT name, open_time, close_time FROM Facility WHERE facility_id = ?")) {
                ps.setInt(1, facilityId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        facilityName = rs.getString("name");
                        Time ot = rs.getTime("open_time");
                        Time ct = rs.getTime("close_time");
                        if (ot != null) openTimeStr = ot.toLocalTime().toString();
                        if (ct != null) closeTimeStr = ct.toLocalTime().toString();
                    }
                }
            }

            json.append("\"facilityName\":").append(StaffAuthUtil.escapeJson(facilityName));
            json.append(",\"bookingDate\":\"").append(bookingDate).append("\"");

            // ─── 2. Courts ───
            List<int[]> courtList = new ArrayList<>();
            List<String> courtNames = new ArrayList<>();

            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT court_id, court_name FROM Court WHERE facility_id = ? AND is_active = 1 ORDER BY court_name")) {
                ps.setInt(1, facilityId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        courtList.add(new int[]{rs.getInt("court_id")});
                        courtNames.add(rs.getString("court_name"));
                    }
                }
            }

            json.append(",\"courts\":[");
            for (int i = 0; i < courtList.size(); i++) {
                if (i > 0) json.append(",");
                json.append("{\"courtId\":").append(courtList.get(i)[0]);
                json.append(",\"courtName\":").append(StaffAuthUtil.escapeJson(courtNames.get(i))).append("}");
            }
            json.append("]");

            // ─── 3. Time slots ───
            List<int[]> slotIds = new ArrayList<>();
            List<String[]> slotTimes = new ArrayList<>();

            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT slot_id, start_time, end_time FROM TimeSlot " +
                            "WHERE start_time >= CAST(? AS TIME) AND end_time <= CAST(? AS TIME) ORDER BY start_time")) {
                ps.setString(1, openTimeStr);
                ps.setString(2, closeTimeStr);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        slotIds.add(new int[]{rs.getInt("slot_id")});
                        slotTimes.add(new String[]{
                                rs.getTime("start_time").toLocalTime().toString().substring(0, 5),
                                rs.getTime("end_time").toLocalTime().toString().substring(0, 5)
                        });
                    }
                }
            }

            json.append(",\"slots\":[");
            for (int i = 0; i < slotIds.size(); i++) {
                if (i > 0) json.append(",");
                json.append("{\"slotId\":").append(slotIds.get(i)[0]);
                json.append(",\"startTime\":\"").append(slotTimes.get(i)[0]).append("\"");
                json.append(",\"endTime\":\"").append(slotTimes.get(i)[1]).append("\"}");
            }
            json.append("]");

            // ─── 4. Booked cells (ONLY non-empty) ───
            // Include slot_status to differentiate NO_SHOW vs other states on timeline
            String sqlBooked = """
                SELECT csb.court_id, csb.slot_id, b.booking_id, b.booking_status,
                       bs.slot_status,
                       COALESCE(a.full_name, g.guest_name) AS customer_name
                FROM CourtSlotBooking csb
                JOIN BookingSlot bs ON csb.booking_slot_id = bs.booking_slot_id
                JOIN Booking b      ON bs.booking_id = b.booking_id
                LEFT JOIN Account a ON b.account_id = a.account_id
                LEFT JOIN Guest g   ON b.guest_id = g.guest_id
                JOIN Court c        ON csb.court_id = c.court_id
                WHERE c.facility_id = ? AND csb.booking_date = ?
                  AND b.booking_status NOT IN ('EXPIRED', 'CANCELLED')
            """;

            Map<String, String[]> bookedMap = new LinkedHashMap<>();
            try (PreparedStatement ps = conn.prepareStatement(sqlBooked)) {
                ps.setInt(1, facilityId);
                ps.setDate(2, Date.valueOf(bookingDate));
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        String key = rs.getInt("court_id") + "-" + rs.getInt("slot_id");
                        bookedMap.put(key, new String[]{
                                String.valueOf(rs.getInt("booking_id")),
                                rs.getString("booking_status"),
                                rs.getString("customer_name"),
                                rs.getString("slot_status")   // NEW: slot-level status
                        });
                    }
                }
            }

            // ─── 5. Disabled cells ───
            String sqlDisabled = """
                SELECT cse.court_id, ts.slot_id, cse.reason
                FROM CourtScheduleException cse
                CROSS JOIN TimeSlot ts
                WHERE cse.facility_id = ? AND cse.is_active = 1
                  AND ? BETWEEN cse.start_date AND cse.end_date
                  AND ts.start_time >= CAST(? AS TIME) AND ts.end_time <= CAST(? AS TIME)
                  AND ts.start_time >= COALESCE(cse.start_time, CAST(? AS TIME))
                  AND ts.end_time   <= COALESCE(cse.end_time,   CAST(? AS TIME))
            """;

            Map<String, String> disabledMap = new LinkedHashMap<>();
            try (PreparedStatement ps = conn.prepareStatement(sqlDisabled)) {
                ps.setInt(1, facilityId);
                ps.setDate(2, Date.valueOf(bookingDate));
                ps.setString(3, openTimeStr);
                ps.setString(4, closeTimeStr);
                ps.setString(5, openTimeStr);
                ps.setString(6, closeTimeStr);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        String key = rs.getInt("court_id") + "-" + rs.getInt("slot_id");
                        String reason = rs.getString("reason");
                        disabledMap.put(key, reason != null ? reason : "Bảo trì");
                    }
                }
            }

            // ─── 6. Build cells — ONLY booked + disabled (skip available) ───
            json.append(",\"cells\":[");
            boolean first = true;

            for (Map.Entry<String, String[]> entry : bookedMap.entrySet()) {
                if (disabledMap.containsKey(entry.getKey())) continue;
                if (!first) json.append(",");
                first = false;
                String[] parts = entry.getKey().split("-");
                String[] b = entry.getValue();
                json.append("{\"courtId\":").append(parts[0]);
                json.append(",\"slotId\":").append(parts[1]);
                json.append(",\"state\":\"BOOKED\"");
                json.append(",\"bookingId\":").append(b[0]);
                json.append(",\"bookingStatus\":\"").append(b[1]).append("\"");
                json.append(",\"customerName\":").append(StaffAuthUtil.escapeJson(b[2]));
                json.append(",\"slotStatus\":\"").append(b[3]).append("\"");  // NEW
                json.append("}");
            }

            for (Map.Entry<String, String> entry : disabledMap.entrySet()) {
                if (!first) json.append(",");
                first = false;
                String[] parts = entry.getKey().split("-");
                json.append("{\"courtId\":").append(parts[0]);
                json.append(",\"slotId\":").append(parts[1]);
                json.append(",\"state\":\"DISABLED\"");
                json.append(",\"disabledReason\":").append(StaffAuthUtil.escapeJson(entry.getValue()));
                json.append("}");
            }

            json.append("]}}");
        }

        return json.toString();
    }
}