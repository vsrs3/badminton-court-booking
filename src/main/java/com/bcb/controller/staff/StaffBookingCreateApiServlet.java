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
import java.sql.*;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST API: POST /api/staff/booking/create
 *
 * Staff creates a booking on behalf of a customer (ACCOUNT or GUEST).
 * Booking is created with status = CONFIRMED immediately.
 * Invoice created with payment_status = UNPAID.
 *
 * Transaction: Booking → BookingSlot → CourtSlotBooking → Invoice
 */
@WebServlet(name = "StaffBookingCreateApiServlet", urlPatterns = {"/api/staff/booking/create"})
public class StaffBookingCreateApiServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json;charset=UTF-8");

        AuthResult auth = StaffAuthUtil.validateStaff(request, response);
        if (!auth.valid) return;

        // Read JSON body
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) sb.append(line);
        }
        String body = sb.toString();

        try {
            // Parse request
            String dateStr = extractString(body, "date");
            String customerType = extractString(body, "customerType");
            String accountIdStr = extractString(body, "accountId");
            String guestName = extractString(body, "guestName");
            String guestPhone = extractString(body, "guestPhone");
            guestPhone = normalizePhone(guestPhone);

            // Parse slots array
            List<int[]> slots = parseSlots(body); // [courtId, slotId]

            // Validate
            if (dateStr == null || dateStr.isEmpty()) {
                sendError(response, 400, "Thiếu ngày đặt sân");
                return;
            }

            LocalDate bookingDate;
            try {
                bookingDate = LocalDate.parse(dateStr);
            } catch (DateTimeParseException e) {
                sendError(response, 400, "Ngày không hợp lệ");
                return;
            }

            if (bookingDate.isBefore(LocalDate.now())) {
                sendError(response, 400, "Không thể đặt cho ngày trong quá khứ");
                return;
            }

            if (slots.isEmpty()) {
                sendError(response, 400, "Chưa chọn slot nào");
                return;
            }

            if (!"ACCOUNT".equals(customerType) && !"GUEST".equals(customerType)) {
                sendError(response, 400, "Loại khách không hợp lệ");
                return;
            }

            Integer accountId = null;
            if ("ACCOUNT".equals(customerType)) {
                if (accountIdStr == null || accountIdStr.isEmpty()) {
                    sendError(response, 400, "Chưa chọn khách hàng");
                    return;
                }
                try {
                    accountId = Integer.parseInt(accountIdStr);
                } catch (NumberFormatException e) {
                    sendError(response, 400, "Account ID không hợp lệ");
                    return;
                }
            } else {
                if (guestName == null || guestName.trim().isEmpty()) {
                    sendError(response, 400, "Vui lòng nhập họ tên khách");
                    return;
                }
                if (guestPhone == null || guestPhone.trim().isEmpty()) {
                    sendError(response, 400, "Vui lòng nhập số điện thoại");
                    return;
                }
            }

            // GUEST phone already belongs to a CUSTOMER account:
            // return structured response so frontend can switch to ACCOUNT flow.
            if ("GUEST".equals(customerType)) {
                CustomerAccount matchedAccount = findActiveCustomerByPhone(guestPhone);
                if (matchedAccount != null) {
                    sendGuestPhoneMatched(response, matchedAccount);
                    return;
                }
            }
            // Validate slot groups (>= 2 consecutive per court)
            if (!validateSlotGroups(slots)) {
                sendError(response, 400, "Mỗi phiên chơi phải có ít nhất 2 slot liên tiếp trên cùng 1 sân");
                return;
            }

            // Get staffId from session
            Integer staffId = (Integer) request.getSession().getAttribute("staffId");
            if (staffId == null) {
                sendError(response, 403, "Staff chưa được gán");
                return;
            }

            // Create booking in transaction
            int bookingId = createBooking(auth.facilityId, bookingDate, customerType,
                    accountId, guestName, guestPhone, staffId, slots);

            response.getWriter().print("{\"success\":true,\"message\":\"Đặt sân thành công\"," +
                    "\"data\":{\"bookingId\":" + bookingId + "}}");

        } catch (SlotConflictException e) {
            sendError(response, 409, "Slot đã được đặt bởi người khác. Vui lòng chọn lại.");
        } catch (Exception e) {
            e.printStackTrace();
            sendError(response, 500, "Lỗi hệ thống: " + e.getMessage());
        }
    }

    private int createBooking(int facilityId, LocalDate bookingDate, String customerType,
                              Integer accountId, String guestName, String guestPhone,
                              int staffId, List<int[]> slots) throws Exception {

        DayOfWeek dow = bookingDate.getDayOfWeek();
        String dayType = (dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY) ? "WEEKEND" : "WEEKDAY";

        try (Connection conn = DBContext.getConnection()) {
            conn.setAutoCommit(false);

            try {
                // 1. If GUEST → insert Guest
                Integer guestId = null;
                if ("GUEST".equals(customerType)) {
                    String sqlGuest = "INSERT INTO Guest (guest_name, phone) VALUES (?, ?)";
                    try (PreparedStatement ps = conn.prepareStatement(sqlGuest, Statement.RETURN_GENERATED_KEYS)) {
                        ps.setNString(1, guestName.trim());
                        ps.setString(2, guestPhone.trim());
                        ps.executeUpdate();
                        try (ResultSet keys = ps.getGeneratedKeys()) {
                            if (keys.next()) guestId = keys.getInt(1);
                        }
                    }
                }

                // 2. Insert Booking
                String sqlBooking;
                if ("ACCOUNT".equals(customerType)) {
                    sqlBooking = "INSERT INTO Booking (facility_id, booking_date, account_id, staff_id, booking_status) " +
                            "VALUES (?, ?, ?, ?, 'CONFIRMED')";
                } else {
                    sqlBooking = "INSERT INTO Booking (facility_id, booking_date, guest_id, staff_id, booking_status) " +
                            "VALUES (?, ?, ?, ?, 'CONFIRMED')";
                }

                int bookingId;
                try (PreparedStatement ps = conn.prepareStatement(sqlBooking, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setInt(1, facilityId);
                    ps.setDate(2, Date.valueOf(bookingDate));
                    if ("ACCOUNT".equals(customerType)) {
                        ps.setInt(3, accountId);
                    } else {
                        ps.setInt(3, guestId);
                    }
                    ps.setInt(4, staffId);
                    ps.executeUpdate();
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        if (keys.next()) bookingId = keys.getInt(1);
                        else throw new Exception("Failed to create booking");
                    }
                }

                // 3. Lookup prices server-side
                Map<String, BigDecimal> priceCache = loadPrices(conn, facilityId, dayType);

                // 4. Get court_type_id for each court
                Map<Integer, Integer> courtTypeMap = loadCourtTypes(conn, facilityId);

                // 5. Get slot times for validation
                Map<Integer, LocalTime[]> slotTimeMap = loadSlotTimes(conn);

                // 6. Insert BookingSlot + CourtSlotBooking
                BigDecimal totalAmount = BigDecimal.ZERO;

                for (int[] slot : slots) {
                    int courtId = slot[0];
                    int slotId = slot[1];

                    // Lookup price
                    Integer courtTypeId = courtTypeMap.get(courtId);
                    LocalTime[] times = slotTimeMap.get(slotId);
                    BigDecimal price = BigDecimal.ZERO;

                    if (courtTypeId != null && times != null) {
                        String priceKey = courtTypeId + "-" + times[0] + "-" + times[1];
                        // Find matching rule
                        for (Map.Entry<String, BigDecimal> entry : priceCache.entrySet()) {
                            String[] parts = entry.getKey().split("\\|");
                            int ruleTypeId = Integer.parseInt(parts[0]);
                            LocalTime ruleStart = LocalTime.parse(parts[1]);
                            LocalTime ruleEnd = LocalTime.parse(parts[2]);

                            if (ruleTypeId == courtTypeId &&
                                    !times[0].isBefore(ruleStart) && !times[1].isAfter(ruleEnd)) {
                                price = entry.getValue();
                                break;
                            }
                        }
                    }

                    totalAmount = totalAmount.add(price);

                    // Insert BookingSlot
                    int bookingSlotId;
                    String sqlBS = "INSERT INTO BookingSlot (booking_id, court_id, slot_id, price) VALUES (?, ?, ?, ?)";
                    try (PreparedStatement ps = conn.prepareStatement(sqlBS, Statement.RETURN_GENERATED_KEYS)) {
                        ps.setInt(1, bookingId);
                        ps.setInt(2, courtId);
                        ps.setInt(3, slotId);
                        ps.setBigDecimal(4, price);
                        ps.executeUpdate();
                        try (ResultSet keys = ps.getGeneratedKeys()) {
                            if (keys.next()) bookingSlotId = keys.getInt(1);
                            else throw new Exception("Failed to insert booking slot");
                        }
                    }

                    // Insert CourtSlotBooking (lock)
                    String sqlLock = "INSERT INTO CourtSlotBooking (court_id, booking_date, slot_id, booking_slot_id) " +
                            "VALUES (?, ?, ?, ?)";
                    try (PreparedStatement ps = conn.prepareStatement(sqlLock)) {
                        ps.setInt(1, courtId);
                        ps.setDate(2, Date.valueOf(bookingDate));
                        ps.setInt(3, slotId);
                        ps.setInt(4, bookingSlotId);
                        try {
                            ps.executeUpdate();
                        } catch (SQLException e) {
                            // PK violation = slot conflict
                            conn.rollback();
                            throw new SlotConflictException();
                        }
                    }
                }

                // 7. Insert Invoice
                String sqlInvoice = "INSERT INTO Invoice (booking_id, total_amount, paid_amount, deposit_percent, payment_status) " +
                        "VALUES (?, ?, 0, 100, 'UNPAID')";
                try (PreparedStatement ps = conn.prepareStatement(sqlInvoice)) {
                    ps.setInt(1, bookingId);
                    ps.setBigDecimal(2, totalAmount);
                    ps.executeUpdate();
                }

                conn.commit();
                return bookingId;

            } catch (SlotConflictException e) {
                throw e;
            } catch (Exception e) {
                conn.rollback();
                throw e;
            }
        }
    }

    // ─── Helper: load price rules into cache ───
    private Map<String, BigDecimal> loadPrices(Connection conn, int facilityId, String dayType) throws SQLException {
        String sql = "SELECT court_type_id, start_time, end_time, price FROM FacilityPriceRule " +
                "WHERE facility_id = ? AND day_type = ?";
        Map<String, BigDecimal> map = new HashMap<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, facilityId);
            ps.setString(2, dayType);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String key = rs.getInt("court_type_id") + "|" +
                            rs.getTime("start_time").toLocalTime() + "|" +
                            rs.getTime("end_time").toLocalTime();
                    map.put(key, rs.getBigDecimal("price"));
                }
            }
        }
        return map;
    }

    // ─── Helper: court_id → court_type_id ───
    private Map<Integer, Integer> loadCourtTypes(Connection conn, int facilityId) throws SQLException {
        String sql = "SELECT court_id, court_type_id FROM Court WHERE facility_id = ? AND is_active = 1";
        Map<Integer, Integer> map = new HashMap<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, facilityId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    map.put(rs.getInt("court_id"), rs.getInt("court_type_id"));
                }
            }
        }
        return map;
    }

    // ─── Helper: slot_id → [start_time, end_time] ───
    private Map<Integer, LocalTime[]> loadSlotTimes(Connection conn) throws SQLException {
        String sql = "SELECT slot_id, start_time, end_time FROM TimeSlot ORDER BY start_time";
        Map<Integer, LocalTime[]> map = new HashMap<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    map.put(rs.getInt("slot_id"), new LocalTime[]{
                            rs.getTime("start_time").toLocalTime(),
                            rs.getTime("end_time").toLocalTime()
                    });
                }
            }
        }
        return map;
    }

    // ─── Validate: each court group ≥ 2 consecutive ───
    private boolean validateSlotGroups(List<int[]> slots) throws Exception {
        // Group by courtId
        Map<Integer, List<Integer>> groups = new HashMap<>();
        for (int[] s : slots) {
            groups.computeIfAbsent(s[0], k -> new ArrayList<>()).add(s[1]);
        }

        // Load slot order
        Map<Integer, Integer> slotOrder = new HashMap<>();
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT slot_id, start_time FROM TimeSlot ORDER BY start_time");
             ResultSet rs = ps.executeQuery()) {
            int idx = 0;
            while (rs.next()) {
                slotOrder.put(rs.getInt("slot_id"), idx++);
            }
        }

        for (Map.Entry<Integer, List<Integer>> entry : groups.entrySet()) {
            List<Integer> slotIds = entry.getValue();
            // Sort by slot order
            slotIds.sort((a, b) -> {
                int oa = slotOrder.getOrDefault(a, 0);
                int ob = slotOrder.getOrDefault(b, 0);
                return Integer.compare(oa, ob);
            });

            // Find consecutive groups
            List<Integer> currentSession = new ArrayList<>();
            currentSession.add(slotIds.get(0));

            for (int i = 1; i < slotIds.size(); i++) {
                int prevOrder = slotOrder.getOrDefault(slotIds.get(i - 1), -1);
                int curOrder = slotOrder.getOrDefault(slotIds.get(i), -1);

                if (curOrder == prevOrder + 1) {
                    currentSession.add(slotIds.get(i));
                } else {
                    // End of session — check size
                    if (currentSession.size() < 2) return false;
                    currentSession = new ArrayList<>();
                    currentSession.add(slotIds.get(i));
                }
            }

            // Check last session
            if (currentSession.size() < 2) return false;
        }

        return true;
    }

    // ─── Simple JSON parser helpers (no library) ───
    private String extractString(String json, String key) {
        String search = "\"" + key + "\"";
        int idx = json.indexOf(search);
        if (idx < 0) return null;

        idx = json.indexOf(":", idx + search.length());
        if (idx < 0) return null;

        // Skip whitespace
        idx++;
        while (idx < json.length() && json.charAt(idx) == ' ') idx++;

        if (idx >= json.length()) return null;

        if (json.charAt(idx) == 'n') return null; // null

        if (json.charAt(idx) == '"') {
            int end = json.indexOf('"', idx + 1);
            if (end < 0) return null;
            return json.substring(idx + 1, end);
        }

        // Number
        int end = idx;
        while (end < json.length() && json.charAt(end) != ',' && json.charAt(end) != '}' && json.charAt(end) != ' ') end++;
        return json.substring(idx, end);
    }

    private List<int[]> parseSlots(String json) {
        List<int[]> result = new ArrayList<>();
        String slotsKey = "\"slots\"";
        int idx = json.indexOf(slotsKey);
        if (idx < 0) return result;

        int arrStart = json.indexOf("[", idx);
        int arrEnd = json.indexOf("]", arrStart);
        if (arrStart < 0 || arrEnd < 0) return result;

        String arrStr = json.substring(arrStart, arrEnd + 1);

        // Find each {...} object
        int pos = 0;
        while (pos < arrStr.length()) {
            int objStart = arrStr.indexOf("{", pos);
            if (objStart < 0) break;
            int objEnd = arrStr.indexOf("}", objStart);
            if (objEnd < 0) break;

            String obj = arrStr.substring(objStart, objEnd + 1);
            String courtIdStr = extractString(obj, "courtId");
            String slotIdStr = extractString(obj, "slotId");

            if (courtIdStr != null && slotIdStr != null) {
                try {
                    result.add(new int[]{Integer.parseInt(courtIdStr), Integer.parseInt(slotIdStr)});
                } catch (NumberFormatException ignored) {}
            }

            pos = objEnd + 1;
        }

        return result;
    }

    private String normalizePhone(String phone) {
        if (phone == null) return null;
        return phone.replaceAll("\\s+", "").trim();
    }

    private CustomerAccount findActiveCustomerByPhone(String phone) throws Exception {
        if (phone == null || phone.isEmpty()) return null;

        String sql = "SELECT TOP 1 account_id, full_name, phone, email " +
                "FROM Account WHERE role = 'CUSTOMER' AND is_active = 1 AND phone = ?";

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, phone);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;

                CustomerAccount out = new CustomerAccount();
                out.accountId = rs.getInt("account_id");
                out.fullName = rs.getString("full_name");
                out.phone = rs.getString("phone");
                out.email = rs.getString("email");
                return out;
            }
        }
    }

    private void sendGuestPhoneMatched(HttpServletResponse response, CustomerAccount account) throws IOException {
        response.setStatus(409);
        response.getWriter().print(
                "{\"success\":false," +
                        "\"code\":\"GUEST_PHONE_MATCHED_ACCOUNT\"," +
                        "\"message\":\"So dien thoai da ton tai tai khoan khach hang\"," +
                        "\"data\":{" +
                        "\"accountId\":" + account.accountId + "," +
                        "\"fullName\":" + StaffAuthUtil.escapeJson(account.fullName) + "," +
                        "\"phone\":" + StaffAuthUtil.escapeJson(account.phone) + "," +
                        "\"email\":" + StaffAuthUtil.escapeJson(account.email) +
                        "}}"
        );
    }

    private void sendError(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.getWriter().print("{\"success\":false,\"message\":" + StaffAuthUtil.escapeJson(message) + "}");
    }

    private static class CustomerAccount {
        int accountId;
        String fullName;
        String phone;
        String email;
    }

    // Custom exception for slot conflicts
    private static class SlotConflictException extends Exception {
        public SlotConflictException() { super("Slot conflict"); }
    }
}

