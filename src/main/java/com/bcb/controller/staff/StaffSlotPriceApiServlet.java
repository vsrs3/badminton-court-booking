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
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * REST API: GET /api/staff/booking/slot-prices?date=yyyy-MM-dd
 *
 * Returns price for every (court, slot) combination on the given date.
 * Price is looked up from FacilityPriceRule based on court_type_id + day_type.
 * Price returned is per 30-minute slot (as stored in DB).
 */
@WebServlet(name = "StaffSlotPriceApiServlet", urlPatterns = {"/api/staff/booking/slot-prices"})
public class StaffSlotPriceApiServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json;charset=UTF-8");

        AuthResult auth = StaffAuthUtil.validateStaff(request, response);
        if (!auth.valid) return;

        String dateParam = request.getParameter("date");
        LocalDate bookingDate;
        try {
            bookingDate = (dateParam != null && !dateParam.isEmpty())
                    ? LocalDate.parse(dateParam) : LocalDate.now();
        } catch (DateTimeParseException e) {
            response.setStatus(400);
            response.getWriter().print("{\"success\":false,\"message\":\"Ngày không hợp lệ\"}");
            return;
        }

        try {
            String json = buildPricesJson(auth.facilityId, bookingDate);
            response.getWriter().print(json);
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(500);
            response.getWriter().print("{\"success\":false,\"message\":\"Lỗi hệ thống\"}");
        }
    }

    private String buildPricesJson(int facilityId, LocalDate bookingDate) throws Exception {
        // Determine dayType
        DayOfWeek dow = bookingDate.getDayOfWeek();
        String dayType = (dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY) ? "WEEKEND" : "WEEKDAY";

        StringBuilder json = new StringBuilder(2048);
        json.append("{\"success\":true,\"data\":{");
        json.append("\"dayType\":\"").append(dayType).append("\"");

        try (Connection conn = DBContext.getConnection()) {

            // 1. Get all courts with their court_type_id
            String sqlCourts = "SELECT court_id, court_type_id FROM Court WHERE facility_id = ? AND is_active = 1";
            List<int[]> courts = new ArrayList<>(); // [court_id, court_type_id]
            try (PreparedStatement ps = conn.prepareStatement(sqlCourts)) {
                ps.setInt(1, facilityId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        courts.add(new int[]{rs.getInt("court_id"), rs.getInt("court_type_id")});
                    }
                }
            }

            // 2. Get facility open/close time
            String openTimeStr = null, closeTimeStr = null;
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT open_time, close_time FROM Facility WHERE facility_id = ?")) {
                ps.setInt(1, facilityId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        Time ot = rs.getTime("open_time");
                        Time ct = rs.getTime("close_time");
                        if (ot != null) openTimeStr = ot.toLocalTime().toString();
                        if (ct != null) closeTimeStr = ct.toLocalTime().toString();
                    }
                }
            }

            // 3. Get all time slots within facility hours
            String sqlSlots = "SELECT slot_id, start_time, end_time FROM TimeSlot " +
                    "WHERE start_time >= CAST(? AS TIME) AND end_time <= CAST(? AS TIME) ORDER BY start_time";
            List<int[]> slots = new ArrayList<>(); // [slot_id]
            List<LocalTime[]> slotTimesList = new ArrayList<>();
            try (PreparedStatement ps = conn.prepareStatement(sqlSlots)) {
                ps.setString(1, openTimeStr);
                ps.setString(2, closeTimeStr);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        slots.add(new int[]{rs.getInt("slot_id")});
                        slotTimesList.add(new LocalTime[]{
                                rs.getTime("start_time").toLocalTime(),
                                rs.getTime("end_time").toLocalTime()
                        });
                    }
                }
            }

            // 4. Get all price rules for this facility + dayType
            String sqlRules = "SELECT court_type_id, start_time, end_time, price FROM FacilityPriceRule " +
                    "WHERE facility_id = ? AND day_type = ? ORDER BY court_type_id, start_time";
            List<Object[]> rules = new ArrayList<>(); // [courtTypeId, startTime, endTime, price]
            try (PreparedStatement ps = conn.prepareStatement(sqlRules)) {
                ps.setInt(1, facilityId);
                ps.setString(2, dayType);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        rules.add(new Object[]{
                                rs.getInt("court_type_id"),
                                rs.getTime("start_time").toLocalTime(),
                                rs.getTime("end_time").toLocalTime(),
                                rs.getBigDecimal("price") // per 30 min
                        });
                    }
                }
            }

            // 5. Build prices array
            json.append(",\"prices\":[");
            boolean first = true;

            for (int[] court : courts) {
                int courtId = court[0];
                int courtTypeId = court[1];

                for (int s = 0; s < slots.size(); s++) {
                    int slotId = slots.get(s)[0];
                    LocalTime slotStart = slotTimesList.get(s)[0];
                    LocalTime slotEnd = slotTimesList.get(s)[1];

                    // Find matching price rule
                    java.math.BigDecimal price = null;
                    for (Object[] rule : rules) {
                        int ruleTypeId = (int) rule[0];
                        LocalTime ruleStart = (LocalTime) rule[1];
                        LocalTime ruleEnd = (LocalTime) rule[2];

                        if (ruleTypeId == courtTypeId &&
                                !slotStart.isBefore(ruleStart) && !slotEnd.isAfter(ruleEnd)) {
                            price = (java.math.BigDecimal) rule[3];
                            break;
                        }
                    }

                    if (price != null) {
                        if (!first) json.append(",");
                        first = false;
                        json.append("{\"courtId\":").append(courtId);
                        json.append(",\"slotId\":").append(slotId);
                        json.append(",\"price\":").append(price);
                        json.append("}");
                    }
                }
            }

            json.append("]}}");
        }

        return json.toString();
    }
}