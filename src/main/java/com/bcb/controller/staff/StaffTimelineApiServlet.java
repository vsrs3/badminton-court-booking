package com.bcb.controller.staff;

import com.bcb.model.Account;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Time;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.bcb.utils.DBContext;

/**
 * REST API: GET /api/staff/timeline?date=yyyy-MM-dd
 * Returns JSON for the staff timeline grid.
 */
@WebServlet(name = "StaffTimelineApiServlet", urlPatterns = {"/api/staff/timeline"})
public class StaffTimelineApiServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();

        // ─── Auth check ───
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("account") == null) {
            response.setStatus(401);
            out.print("{\"success\":false,\"errorCode\":\"UNAUTHORIZED\",\"message\":\"Chưa đăng nhập\"}");
            return;
        }

        Account account = (Account) session.getAttribute("account");
        if (!"STAFF".equals(account.getRole())) {
            response.setStatus(403);
            out.print("{\"success\":false,\"errorCode\":\"FORBIDDEN\",\"message\":\"Không có quyền truy cập\"}");
            return;
        }

        Integer facilityId = (Integer) session.getAttribute("facilityId");
        if (facilityId == null) {
            response.setStatus(403);
            out.print("{\"success\":false,\"errorCode\":\"FORBIDDEN\",\"message\":\"Staff chưa được gán cơ sở\"}");
            return;
        }

        // ─── Parse date param ───
        String dateParam = request.getParameter("date");
        LocalDate bookingDate;
        try {
            bookingDate = (dateParam != null && !dateParam.isEmpty())
                    ? LocalDate.parse(dateParam)
                    : LocalDate.now();
        } catch (DateTimeParseException e) {
            response.setStatus(400);
            out.print("{\"success\":false,\"errorCode\":\"INVALID_DATE\",\"message\":\"Ngày không hợp lệ\"}");
            return;
        }

        // ─── Build JSON ───
        try {
            String json = buildTimelineJson(facilityId, bookingDate);
            out.print(json);
        } catch (Exception e) {
            System.out.println("❌ Timeline API error: " + e.getMessage());
            e.printStackTrace();
            response.setStatus(500);
            out.print("{\"success\":false,\"errorCode\":\"INTERNAL_ERROR\",\"message\":\"Lỗi hệ thống\"}");
        }
    }

    private String buildTimelineJson(int facilityId, LocalDate bookingDate) throws Exception {
        StringBuilder json = new StringBuilder();
        json.append("{\"success\":true,\"data\":{");

        try (Connection conn = DBContext.getConnection()) {

            // ─── 1. Facility info ───
            String facilityName = "";
            String openTimeStr = null;
            String closeTimeStr = null;

            String sqlFacility = "SELECT name, open_time, close_time FROM Facility WHERE facility_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(sqlFacility)) {
                ps.setInt(1, facilityId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        facilityName = rs.getString("name");
                        Time ot = rs.getTime("open_time");
                        Time ct = rs.getTime("close_time");
                        if (ot != null) openTimeStr = ot.toLocalTime().toString();   // "06:00"
                        if (ct != null) closeTimeStr = ct.toLocalTime().toString();  // "22:00"
                    }
                }
            }

            json.append("\"facilityName\":").append(escapeJson(facilityName)).append(",");
            json.append("\"bookingDate\":\"").append(bookingDate).append("\",");

            // ─── 2. Courts ───
            String sqlCourts = "SELECT court_id, court_name FROM Court "
                    + "WHERE facility_id = ? AND is_active = 1 ORDER BY court_name";

            List<int[]> courtList = new ArrayList<>();
            List<String> courtNames = new ArrayList<>();

            try (PreparedStatement ps = conn.prepareStatement(sqlCourts)) {
                ps.setInt(1, facilityId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        courtList.add(new int[]{rs.getInt("court_id")});
                        courtNames.add(rs.getString("court_name"));
                    }
                }
            }

            json.append("\"courts\":[");
            for (int i = 0; i < courtList.size(); i++) {
                if (i > 0) json.append(",");
                json.append("{\"courtId\":").append(courtList.get(i)[0]);
                json.append(",\"courtName\":").append(escapeJson(courtNames.get(i))).append("}");
            }
            json.append("],");

            // ─── 3. Time slots (compare TIME vs TIME using CAST) ───
            String sqlSlots = "SELECT slot_id, start_time, end_time FROM TimeSlot "
                    + "WHERE start_time >= CAST(? AS TIME) AND end_time <= CAST(? AS TIME) "
                    + "ORDER BY start_time";

            List<int[]> slotIds = new ArrayList<>();
            List<String[]> slotTimes = new ArrayList<>();

            try (PreparedStatement ps = conn.prepareStatement(sqlSlots)) {
                ps.setString(1, openTimeStr);   // e.g. "06:00"
                ps.setString(2, closeTimeStr);  // e.g. "22:00"
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        slotIds.add(new int[]{rs.getInt("slot_id")});
                        Time st = rs.getTime("start_time");
                        Time et = rs.getTime("end_time");
                        slotTimes.add(new String[]{
                                st.toLocalTime().toString().substring(0, 5),
                                et.toLocalTime().toString().substring(0, 5)
                        });
                    }
                }
            }

            json.append("\"slots\":[");
            for (int i = 0; i < slotIds.size(); i++) {
                if (i > 0) json.append(",");
                json.append("{\"slotId\":").append(slotIds.get(i)[0]);
                json.append(",\"startTime\":\"").append(slotTimes.get(i)[0]).append("\"");
                json.append(",\"endTime\":\"").append(slotTimes.get(i)[1]).append("\"}");
            }
            json.append("],");

            // ─── 4. Booked cells ───
            String sqlBooked = """
                SELECT
                    csb.court_id,
                    csb.slot_id,
                    b.booking_id,
                    b.booking_status,
                    COALESCE(a.full_name, g.guest_name) AS customer_name
                FROM CourtSlotBooking csb
                JOIN BookingSlot bs   ON csb.booking_slot_id = bs.booking_slot_id
                JOIN Booking b        ON bs.booking_id = b.booking_id
                LEFT JOIN Account a   ON b.account_id = a.account_id
                LEFT JOIN Guest g     ON b.guest_id = g.guest_id
                JOIN Court c          ON csb.court_id = c.court_id
                WHERE c.facility_id = ?
                  AND csb.booking_date = ?
                  AND b.booking_status != 'EXPIRED'
            """;

            Map<String, String[]> bookedMap = new LinkedHashMap<>();

            try (PreparedStatement ps = conn.prepareStatement(sqlBooked)) {
                ps.setInt(1, facilityId);
                ps.setDate(2, Date.valueOf(bookingDate));  // java.sql.Date for DATE column
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        String key = rs.getInt("court_id") + "-" + rs.getInt("slot_id");
                        bookedMap.put(key, new String[]{
                                String.valueOf(rs.getInt("booking_id")),
                                rs.getString("booking_status"),
                                rs.getString("customer_name")
                        });
                    }
                }
            }

            // ─── 5. Disabled slots (CourtScheduleException) ───
            //     Compare TIME columns to TIME values,
            //     and DATE columns to DATE values — no type mismatch.
            String sqlDisabled = """
                SELECT
                    cse.court_id,
                    ts.slot_id
                FROM CourtScheduleException cse
                CROSS JOIN TimeSlot ts
                WHERE cse.facility_id = ?
                  AND cse.is_active = 1
                  AND ? BETWEEN cse.start_date AND cse.end_date
                  AND ts.start_time >= CAST(? AS TIME)
                  AND ts.end_time   <= CAST(? AS TIME)
                  AND ts.start_time >= COALESCE(cse.start_time, CAST(? AS TIME))
                  AND ts.end_time   <= COALESCE(cse.end_time,   CAST(? AS TIME))
            """;

            Map<String, String> disabledMap = new LinkedHashMap<>();

            try (PreparedStatement ps = conn.prepareStatement(sqlDisabled)) {
                ps.setInt(1, facilityId);
                ps.setDate(2, Date.valueOf(bookingDate));  // DATE vs DATE
                ps.setString(3, openTimeStr);              // facility open
                ps.setString(4, closeTimeStr);             // facility close
                ps.setString(5, openTimeStr);              // COALESCE fallback open
                ps.setString(6, closeTimeStr);             // COALESCE fallback close
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        String key = rs.getInt("court_id") + "-" + rs.getInt("slot_id");
                        disabledMap.put(key, "Bảo trì");
                    }
                }
            }

            // Optionally load reasons (separate pass if needed)
            // For now, all exceptions labeled as "Bảo trì" — can enhance later.

            // ─── 6. Build cells array ───
            json.append("\"cells\":[");
            boolean first = true;
            for (int[] court : courtList) {
                int cId = court[0];
                for (int[] slot : slotIds) {
                    int sId = slot[0];
                    String key = cId + "-" + sId;

                    if (!first) json.append(",");
                    first = false;

                    json.append("{\"courtId\":").append(cId);
                    json.append(",\"slotId\":").append(sId);

                    if (disabledMap.containsKey(key)) {
                        json.append(",\"state\":\"DISABLED\"");
                        json.append(",\"disabledReason\":").append(escapeJson(disabledMap.get(key)));
                    } else if (bookedMap.containsKey(key)) {
                        String[] b = bookedMap.get(key);
                        json.append(",\"state\":\"BOOKED\"");
                        json.append(",\"bookingId\":").append(b[0]);
                        json.append(",\"bookingStatus\":\"").append(b[1]).append("\"");
                        json.append(",\"customerName\":").append(escapeJson(b[2]));
                    } else {
                        json.append(",\"state\":\"AVAILABLE\"");
                    }

                    json.append("}");
                }
            }
            json.append("]");

            json.append("}}");
        }

        return json.toString();
    }

    private String escapeJson(String value) {
        if (value == null) return "null";
        return "\"" + value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t")
                + "\"";
    }
}