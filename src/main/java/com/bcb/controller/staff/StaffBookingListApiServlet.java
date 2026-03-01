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
 * REST API: GET /api/staff/booking/list?search=...&page=...&size=...
 * Search by: customer_name, phone, booking_id
 * Data scope: Booking.facility_id = staff's facility
 */
@WebServlet(name = "StaffBookingListApiServlet", urlPatterns = {"/api/staff/booking/list"})
public class StaffBookingListApiServlet extends HttpServlet {

    private static final int DEFAULT_PAGE_SIZE = 10;

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
            if (s != null) size = Math.min(50, Math.max(1, Integer.parseInt(s)));
        } catch (NumberFormatException ignored) {}

        try {
            String json = buildListJson(facilityId, search, page, size);
            out.print(json);
        } catch (Exception e) {
            System.out.println("❌ Booking List API error: " + e.getMessage());
            e.printStackTrace();
            response.setStatus(500);
            out.print("{\"success\":false,\"message\":\"Lỗi hệ thống\"}");
        }
    }

    private String buildListJson(int facilityId, String search, int page, int size) throws Exception {
        StringBuilder json = new StringBuilder();

        // ─── WHERE clause ───
        String whereBase = "WHERE b.facility_id = ? AND b.booking_status != 'EXPIRED'";
        String whereSearch = "";
        boolean hasSearch = (search != null);

        if (hasSearch) {
            // Check if search is a number (booking_id)
            boolean isNumeric = search.matches("\\d+");
            if (isNumeric) {
                whereSearch = " AND (b.booking_id = ? OR a.full_name LIKE ? OR g.guest_name LIKE ? OR a.phone LIKE ? OR g.phone LIKE ?)";
            } else {
                whereSearch = " AND (a.full_name LIKE ? OR g.guest_name LIKE ? OR a.phone LIKE ? OR g.phone LIKE ?)";
            }
        }

        String fromJoin = """
            FROM Booking b
            LEFT JOIN Account a ON b.account_id = a.account_id
            LEFT JOIN Guest g   ON b.guest_id = g.guest_id
        """;

        try (Connection conn = DBContext.getConnection()) {

            // ─── 1. Count total ───
            String sqlCount = "SELECT COUNT(*) " + fromJoin + whereBase + whereSearch;
            int totalRows;
            try (PreparedStatement ps = conn.prepareStatement(sqlCount)) {
                int idx = 1;
                ps.setInt(idx++, facilityId);
                if (hasSearch) {
                    boolean isNumeric = search.matches("\\d+");
                    String like = "%" + search + "%";
                    if (isNumeric) {
                        ps.setInt(idx++, Integer.parseInt(search));
                        ps.setString(idx++, like);
                        ps.setString(idx++, like);
                        ps.setString(idx++, like);
                        ps.setString(idx++, like);
                    } else {
                        ps.setString(idx++, like);
                        ps.setString(idx++, like);
                        ps.setString(idx++, like);
                        ps.setString(idx++, like);
                    }
                }
                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    totalRows = rs.getInt(1);
                }
            }

            int totalPages = (int) Math.ceil((double) totalRows / size);
            if (page > totalPages && totalPages > 0) page = totalPages;
            int offset = (page - 1) * size;

            // ─── 2. Fetch rows ───
            // For court display: sub-select to get court names
            String sqlData = """
                SELECT
                    b.booking_id,
                    COALESCE(a.full_name, g.guest_name) AS customer_name,
                    COALESCE(a.phone, g.phone) AS phone,
                    b.booking_date,
                    b.booking_status,
                    i.payment_status,
                    (SELECT COUNT(DISTINCT bs2.court_id) FROM BookingSlot bs2 WHERE bs2.booking_id = b.booking_id) AS court_count,
                    (SELECT TOP 1 c2.court_name FROM BookingSlot bs3 JOIN Court c2 ON bs3.court_id = c2.court_id WHERE bs3.booking_id = b.booking_id ORDER BY c2.court_name) AS first_court_name
            """ + fromJoin + """
                LEFT JOIN Invoice i ON b.booking_id = i.booking_id
            """ + whereBase + whereSearch + """
                ORDER BY b.created_at DESC
                OFFSET ? ROWS FETCH NEXT ? ROWS ONLY
            """;

            json.append("{\"success\":true,\"data\":{");
            json.append("\"page\":").append(page);
            json.append(",\"size\":").append(size);
            json.append(",\"totalRows\":").append(totalRows);
            json.append(",\"totalPages\":").append(totalPages);
            json.append(",\"bookings\":[");

            boolean first = true;
            try (PreparedStatement ps = conn.prepareStatement(sqlData)) {
                int idx = 1;
                ps.setInt(idx++, facilityId);
                if (hasSearch) {
                    boolean isNumeric = search.matches("\\d+");
                    String like = "%" + search + "%";
                    if (isNumeric) {
                        ps.setInt(idx++, Integer.parseInt(search));
                        ps.setString(idx++, like);
                        ps.setString(idx++, like);
                        ps.setString(idx++, like);
                        ps.setString(idx++, like);
                    } else {
                        ps.setString(idx++, like);
                        ps.setString(idx++, like);
                        ps.setString(idx++, like);
                        ps.setString(idx++, like);
                    }
                }
                ps.setInt(idx++, offset);
                ps.setInt(idx++, size);

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        if (!first) json.append(",");
                        first = false;

                        int courtCount = rs.getInt("court_count");
                        String courtDisplay = courtCount > 1
                                ? "Nhiều sân (" + courtCount + ")"
                                : rs.getString("first_court_name");

                        json.append("{\"bookingId\":").append(rs.getInt("booking_id"));
                        json.append(",\"customerName\":").append(esc(rs.getString("customer_name")));
                        json.append(",\"phone\":").append(esc(rs.getString("phone")));
                        json.append(",\"bookingDate\":\"").append(rs.getString("booking_date")).append("\"");
                        json.append(",\"bookingStatus\":\"").append(rs.getString("booking_status")).append("\"");
                        json.append(",\"paymentStatus\":").append(esc(rs.getString("payment_status")));
                        json.append(",\"courtDisplay\":").append(esc(courtDisplay));
                        json.append("}");
                    }
                }
            }

            json.append("]}}");
        }

        return json.toString();
    }

    private String esc(String val) {
        if (val == null) return "null";
        return "\"" + val.replace("\\", "\\\\").replace("\"", "\\\"")
                         .replace("\n", "\\n").replace("\r", "\\r") + "\"";
    }
}