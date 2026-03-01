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

/**
 * REST API: GET /api/staff/booking/list?search=...&page=...&size=...
 * Search by: customer_name, phone, booking_id
 * Data scope: Booking.facility_id = staff's facility
 */
@WebServlet(name = "StaffBookingListApiServlet", urlPatterns = {"/api/staff/booking/list"})
public class StaffBookingListApiServlet extends HttpServlet {

    private static final int DEFAULT_PAGE_SIZE = 10;
    private static final int MAX_PAGE_SIZE = 50;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json;charset=UTF-8");

        // ─── Auth ───
        AuthResult auth = StaffAuthUtil.validateStaff(request, response);
        if (!auth.valid) return;

        // ─── Params ───
        String search = request.getParameter("search");
        if (search != null) search = search.trim();
        if (search != null && search.isEmpty()) search = null;

        int page = 1;
        int size = DEFAULT_PAGE_SIZE;
        try {
            String p = request.getParameter("page");
            if (p != null) page = Math.max(1, Integer.parseInt(p));
            String s = request.getParameter("size");
            if (s != null) size = Math.min(MAX_PAGE_SIZE, Math.max(1, Integer.parseInt(s)));
        } catch (NumberFormatException ignored) {}

        try {
            String json = buildListJson(auth.facilityId, search, page, size);
            response.getWriter().print(json);
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(500);
            response.getWriter().print("{\"success\":false,\"message\":\"Lỗi hệ thống\"}");
        }
    }

    private String buildListJson(int facilityId, String search, int page, int size) throws Exception {
        // ─── Prepare search context (computed once, used twice) ───
        boolean hasSearch = (search != null);
        boolean isNumeric = hasSearch && search.matches("\\d+");
        String like = hasSearch ? "%" + search + "%" : null;

        String whereBase = "WHERE b.facility_id = ? AND b.booking_status != 'EXPIRED'";
        String whereSearch = "";
        if (hasSearch) {
            whereSearch = isNumeric
                    ? " AND (b.booking_id = ? OR a.full_name LIKE ? OR g.guest_name LIKE ? OR a.phone LIKE ? OR g.phone LIKE ?)"
                    : " AND (a.full_name LIKE ? OR g.guest_name LIKE ? OR a.phone LIKE ? OR g.phone LIKE ?)";
        }

        String fromJoin = " FROM Booking b LEFT JOIN Account a ON b.account_id = a.account_id LEFT JOIN Guest g ON b.guest_id = g.guest_id ";

        try (Connection conn = DBContext.getConnection()) {

            // ─── 1. Count ───
            int totalRows;
            try (PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*)" + fromJoin + whereBase + whereSearch)) {
                int idx = bindSearchParams(ps, 1, facilityId, hasSearch, isNumeric, search, like);
                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    totalRows = rs.getInt(1);
                }
            }

            int totalPages = Math.max(1, (int) Math.ceil((double) totalRows / size));
            if (page > totalPages) page = totalPages;
            int offset = (page - 1) * size;

            // ─── 2. Fetch ───
            String sqlData = """
                SELECT b.booking_id,
                       COALESCE(a.full_name, g.guest_name) AS customer_name,
                       COALESCE(a.phone, g.phone) AS phone,
                       b.booking_date, b.booking_status, i.payment_status,
                       (SELECT COUNT(DISTINCT bs2.court_id) FROM BookingSlot bs2 WHERE bs2.booking_id = b.booking_id) AS court_count,
                       (SELECT TOP 1 c2.court_name FROM BookingSlot bs3 JOIN Court c2 ON bs3.court_id = c2.court_id WHERE bs3.booking_id = b.booking_id ORDER BY c2.court_name) AS first_court_name
            """ + fromJoin + " LEFT JOIN Invoice i ON b.booking_id = i.booking_id " + whereBase + whereSearch +
                    " ORDER BY b.created_at DESC OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

            StringBuilder json = new StringBuilder(1024);
            json.append("{\"success\":true,\"data\":{");
            json.append("\"page\":").append(page);
            json.append(",\"size\":").append(size);
            json.append(",\"totalRows\":").append(totalRows);
            json.append(",\"totalPages\":").append(totalPages);
            json.append(",\"bookings\":[");

            boolean first = true;
            try (PreparedStatement ps = conn.prepareStatement(sqlData)) {
                int idx = bindSearchParams(ps, 1, facilityId, hasSearch, isNumeric, search, like);
                ps.setInt(idx++, offset);
                ps.setInt(idx, size);

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        if (!first) json.append(",");
                        first = false;

                        int courtCount = rs.getInt("court_count");
                        String courtDisplay = courtCount > 1
                                ? "Nhiều sân (" + courtCount + ")"
                                : rs.getString("first_court_name");

                        json.append("{\"bookingId\":").append(rs.getInt("booking_id"));
                        json.append(",\"customerName\":").append(StaffAuthUtil.escapeJson(rs.getString("customer_name")));
                        json.append(",\"phone\":").append(StaffAuthUtil.escapeJson(rs.getString("phone")));
                        json.append(",\"bookingDate\":\"").append(rs.getString("booking_date")).append("\"");
                        json.append(",\"bookingStatus\":\"").append(rs.getString("booking_status")).append("\"");
                        json.append(",\"paymentStatus\":").append(StaffAuthUtil.escapeJson(rs.getString("payment_status")));
                        json.append(",\"courtDisplay\":").append(StaffAuthUtil.escapeJson(courtDisplay));
                        json.append("}");
                    }
                }
            }

            json.append("]}}");
            return json.toString();
        }
    }

    /**
     * Bind facility + search params to PreparedStatement.
     * Returns next parameter index.
     */
    private int bindSearchParams(PreparedStatement ps, int startIdx,
                                 int facilityId, boolean hasSearch, boolean isNumeric,
                                 String search, String like) throws SQLException {
        int idx = startIdx;
        ps.setInt(idx++, facilityId);
        if (hasSearch) {
            if (isNumeric) {
                ps.setInt(idx++, Integer.parseInt(search));
            }
            ps.setString(idx++, like);
            ps.setString(idx++, like);
            ps.setString(idx++, like);
            ps.setString(idx++, like);
        }
        return idx;
    }
}