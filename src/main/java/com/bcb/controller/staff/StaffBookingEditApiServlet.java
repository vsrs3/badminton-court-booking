
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
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Booking edit/cancel/release APIs with snapshot-token optimistic lock.
 */
@WebServlet(name = "StaffBookingEditApiServlet", urlPatterns = {
        "/api/staff/booking/edit/preview",
        "/api/staff/booking/edit/save",
        "/api/staff/booking/release-slot",
        "/api/staff/booking/cancel"
})
public class StaffBookingEditApiServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json;charset=UTF-8");

        AuthResult auth = StaffAuthUtil.validateStaff(request, response);
        if (!auth.valid) return;

        Integer staffId = (Integer) request.getSession().getAttribute("staffId");
        if (staffId == null) {
            response.setStatus(403);
            response.getWriter().print("{\"success\":false,\"message\":\"Staff chưa được gán\"}");
            return;
        }

        String body = readBody(request);
        String path = request.getServletPath();

        try (Connection conn = DBContext.getConnection()) {
            if (path.endsWith("/edit/preview")) {
                handlePreview(response, conn, auth.facilityId, body);
            } else if (path.endsWith("/edit/save")) {
                handleSave(response, conn, auth.facilityId, staffId, body);
            } else if (path.endsWith("/release-slot")) {
                handleReleaseSlot(response, conn, auth.facilityId, staffId, body);
            } else if (path.endsWith("/cancel")) {
                handleCancel(response, conn, auth.facilityId, staffId, body);
            } else {
                response.setStatus(404);
                response.getWriter().print("{\"success\":false,\"message\":\"API không tồn tại\"}");
            }
        } catch (ApiException e) {
            response.setStatus(e.status);
            response.getWriter().print(e.toJson());
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(500);
            response.getWriter().print("{\"success\":false,\"message\":\"Lỗi hệ thống\"}");
        }
    }

    private void handlePreview(HttpServletResponse response, Connection conn, int facilityId, String body)
            throws Exception {
        int bookingId = parseInt(StaffBookingSnapshotTokenUtil.extractString(body, "bookingId"));
        if (bookingId <= 0) {
            throw new ApiException(400, "Thiếu bookingId");
        }

        StaffBookingSnapshotTokenUtil.Snapshot snapshot =
                StaffBookingSnapshotTokenUtil.loadSnapshot(conn, bookingId, facilityId);
        if (snapshot == null) {
            throw new ApiException(404, "Không tìm thấy booking");
        }

        String etag = StaffBookingSnapshotTokenUtil.computeEtag(snapshot);
        String totalAmount = snapshot.invoice != null ? snapshot.invoice.totalAmount : "0";
        String paidAmount = snapshot.invoice != null ? snapshot.invoice.paidAmount : "0";
        String paymentStatus = snapshot.invoice != null ? snapshot.invoice.paymentStatus : "UNPAID";
        String refundDue = snapshot.invoice != null ? snapshot.invoice.refundDue : "0";
        String refundStatus = snapshot.invoice != null ? snapshot.invoice.refundStatus : "NONE";

        StringBuilder json = new StringBuilder(320);
        json.append("{\"success\":true,\"data\":{");
        json.append("\"bookingId\":").append(snapshot.bookingId);
        json.append(",\"bookingStatus\":\"").append(snapshot.bookingStatus).append("\"");
        json.append(",\"slotCount\":").append(snapshot.slots.size());
        json.append(",\"totalAmount\":").append(totalAmount);
        json.append(",\"paidAmount\":").append(paidAmount);
        json.append(",\"paymentStatus\":\"").append(paymentStatus).append("\"");
        json.append(",\"refundDue\":").append(refundDue);
        json.append(",\"refundStatus\":\"").append(refundStatus).append("\"");
        json.append(",\"etag\":").append(StaffAuthUtil.escapeJson(etag));
        json.append("}}");
        response.getWriter().print(json.toString());
    }

    private void handleSave(HttpServletResponse response, Connection conn, int facilityId, int staffId, String body)
            throws Exception {
        int bookingId = parseInt(StaffBookingSnapshotTokenUtil.extractString(body, "bookingId"));
        String etag = StaffBookingSnapshotTokenUtil.extractString(body, "etag");
        String reason = trimToNull(StaffBookingSnapshotTokenUtil.extractString(body, "reason"));

        List<SlotPair> addSlots = parseSlotPairs(body, "addSlots");
        Set<Integer> removeSlotIds = parseIntSet(body, "removeBookingSlotIds");

        if (bookingId <= 0 || etag == null || etag.isEmpty()) {
            throw new ApiException(400, "Thiếu bookingId hoặc etag");
        }
        if (addSlots.isEmpty() && removeSlotIds.isEmpty()) {
            throw new ApiException(400, "Không có thay đổi để lưu");
        }

        conn.setAutoCommit(false);
        conn.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
        try {
            StaffBookingSnapshotTokenUtil.Snapshot before = assertConfirmedSnapshot(conn, bookingId, facilityId, etag);
            String beforeEtag = StaffBookingSnapshotTokenUtil.computeEtag(before);

            ensureEditableRemovals(conn, bookingId, removeSlotIds);
            ensureNoDuplicateAddPairs(addSlots);
            validateSessionRuleAfterEdit(conn, bookingId, removeSlotIds, addSlots);

            for (Integer bookingSlotId : removeSlotIds) {
                cancelPendingSlot(conn, bookingId, bookingSlotId);
            }

            LocalDate bookingDate = LocalDate.parse(before.bookingDate);
            ensureAddSlotsNotExpired(conn, bookingDate, addSlots);
            for (SlotPair slot : addSlots) {
                upsertPendingSlot(conn, bookingId, facilityId, bookingDate, slot);
            }

            InvoiceUpdateResult invoice = recalcInvoice(conn, bookingId, reason);
            String nextBookingStatus = recomputeBookingStatus(conn, bookingId);
            updateBookingStatus(conn, bookingId, nextBookingStatus);

            StaffBookingSnapshotTokenUtil.Snapshot after =
                    StaffBookingSnapshotTokenUtil.loadSnapshot(conn, bookingId, facilityId);
            String afterEtag = StaffBookingSnapshotTokenUtil.computeEtag(after);

            insertAuditLog(conn, bookingId, staffId, "EDIT_SAVE", "CHANGE_ALL",
                    reason, beforeEtag, afterEtag, before, after, invoice.refundDue);

            conn.commit();
            response.getWriter().print("{\"success\":true,\"message\":\"Lưu thay đổi thành công\",\"data\":{"
                    + "\"etag\":" + StaffAuthUtil.escapeJson(afterEtag)
                    + ",\"bookingStatus\":\"" + nextBookingStatus + "\""
                    + ",\"totalAmount\":" + invoice.totalAmount.toPlainString()
                    + ",\"paidAmount\":" + invoice.paidAmount.toPlainString()
                    + ",\"refundDue\":" + invoice.refundDue.toPlainString()
                    + ",\"refundStatus\":\"" + invoice.refundStatus + "\""
                    + "}}");

        } catch (Exception e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(true);
        }
    }
    private void handleReleaseSlot(HttpServletResponse response, Connection conn, int facilityId, int staffId, String body)
            throws Exception {
        int bookingId = parseInt(StaffBookingSnapshotTokenUtil.extractString(body, "bookingId"));
        String etag = StaffBookingSnapshotTokenUtil.extractString(body, "etag");
        int bookingSlotId = parseInt(StaffBookingSnapshotTokenUtil.extractString(body, "bookingSlotId"));
        String reason = trimToNull(StaffBookingSnapshotTokenUtil.extractString(body, "reason"));

        if (bookingId <= 0 || bookingSlotId <= 0 || etag == null || etag.isEmpty()) {
            throw new ApiException(400, "Thiếu bookingId, bookingSlotId hoặc etag");
        }

        conn.setAutoCommit(false);
        conn.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
        try {
            StaffBookingSnapshotTokenUtil.Snapshot before = assertReleaseSnapshot(conn, bookingId, facilityId, etag);
            String beforeEtag = StaffBookingSnapshotTokenUtil.computeEtag(before);

            SlotState slot = loadSlotState(conn, bookingId, bookingSlotId);
            if (slot == null) {
                throw new ApiException(404, "Không tìm thấy slot trong booking");
            }
            if (!"NO_SHOW".equals(slot.slotStatus)) {
                throw new ApiException(400, "Chỉ cho phép giải phóng slot NO_SHOW");
            }

            boolean changed = false;
            if (!slot.released) {
                try (PreparedStatement ps = conn.prepareStatement(
                        "DELETE FROM CourtSlotBooking WHERE booking_slot_id = ?")) {
                    ps.setInt(1, bookingSlotId);
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = conn.prepareStatement(
                        "UPDATE BookingSlot SET is_released = 1 WHERE booking_slot_id = ?")) {
                    ps.setInt(1, bookingSlotId);
                    ps.executeUpdate();
                }
                changed = true;
            }

            StaffBookingSnapshotTokenUtil.Snapshot after =
                    StaffBookingSnapshotTokenUtil.loadSnapshot(conn, bookingId, facilityId);
            String afterEtag = StaffBookingSnapshotTokenUtil.computeEtag(after);

            insertAuditLog(conn, bookingId, staffId, "RELEASE_SLOT", "CHANGE_SLOT",
                    reason, beforeEtag, afterEtag, before, after,
                    after.invoice != null ? parseMoney(after.invoice.refundDue) : BigDecimal.ZERO);

            conn.commit();

            response.getWriter().print("{\"success\":true,\"message\":"
                    + StaffAuthUtil.escapeJson(changed ? "Giải phóng slot thành công" : "Slot đã được giải phóng trước đó")
                    + ",\"data\":{\"etag\":" + StaffAuthUtil.escapeJson(afterEtag)
                    + ",\"changed\":" + changed + "}}");

        } catch (Exception e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(true);
        }
    }

    private void handleCancel(HttpServletResponse response, Connection conn, int facilityId, int staffId, String body)
            throws Exception {
        int bookingId = parseInt(StaffBookingSnapshotTokenUtil.extractString(body, "bookingId"));
        String etag = StaffBookingSnapshotTokenUtil.extractString(body, "etag");
        String reason = trimToNull(StaffBookingSnapshotTokenUtil.extractString(body, "reason"));
        boolean confirmAllRemaining = "true".equalsIgnoreCase(
                String.valueOf(StaffBookingSnapshotTokenUtil.extractString(body, "confirmAllRemaining")));

        if (bookingId <= 0 || etag == null || etag.isEmpty()) {
            throw new ApiException(400, "Thiếu bookingId hoặc etag");
        }
        if (!confirmAllRemaining) {
            throw new ApiException(400, "Bạn cần xác nhận hủy toàn bộ slot còn lại");
        }
        if (reason == null || reason.isEmpty()) {
            throw new ApiException(400, "Vui lòng nhập lý do hủy");
        }

        conn.setAutoCommit(false);
        conn.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
        try {
            StaffBookingSnapshotTokenUtil.Snapshot before = assertConfirmedSnapshot(conn, bookingId, facilityId, etag);
            String beforeEtag = StaffBookingSnapshotTokenUtil.computeEtag(before);

            if (hasSlotStatus(conn, bookingId, "CHECKED_IN")) {
                throw new ApiException(400, "Không thể hủy khi có phiên đang CHECKED_IN");
            }

            List<Integer> pendingSlotIds = loadPendingSlotIds(conn, bookingId);
            if (pendingSlotIds.isEmpty()) {
                throw new ApiException(400, "Không còn slot PENDING để hủy");
            }
            for (Integer bookingSlotId : pendingSlotIds) {
                cancelPendingSlot(conn, bookingId, bookingSlotId);
            }

            InvoiceUpdateResult invoice = recalcInvoice(conn, bookingId, reason);
            String nextBookingStatus = recomputeBookingStatus(conn, bookingId);
            updateBookingStatus(conn, bookingId, nextBookingStatus);

            StaffBookingSnapshotTokenUtil.Snapshot after =
                    StaffBookingSnapshotTokenUtil.loadSnapshot(conn, bookingId, facilityId);
            String afterEtag = StaffBookingSnapshotTokenUtil.computeEtag(after);

            insertAuditLog(conn, bookingId, staffId, "CANCEL_BOOKING", "CHANGE_ALL",
                    reason, beforeEtag, afterEtag, before, after, invoice.refundDue);

            conn.commit();
            response.getWriter().print("{\"success\":true,\"message\":\"Hủy các slot còn lại thành công\",\"data\":{"
                    + "\"etag\":" + StaffAuthUtil.escapeJson(afterEtag)
                    + ",\"bookingStatus\":\"" + nextBookingStatus + "\""
                    + ",\"totalAmount\":" + invoice.totalAmount.toPlainString()
                    + ",\"paidAmount\":" + invoice.paidAmount.toPlainString()
                    + ",\"refundDue\":" + invoice.refundDue.toPlainString()
                    + ",\"refundStatus\":\"" + invoice.refundStatus + "\""
                    + "}}");

        } catch (Exception e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(true);
        }
    }

    private StaffBookingSnapshotTokenUtil.Snapshot assertConfirmedSnapshot(
            Connection conn, int bookingId, int facilityId, String requestEtag) throws Exception {
        StaffBookingSnapshotTokenUtil.Snapshot snapshot =
                StaffBookingSnapshotTokenUtil.loadSnapshot(conn, bookingId, facilityId);
        if (snapshot == null) {
            throw new ApiException(404, "Không tìm thấy booking");
        }

        String currentEtag = StaffBookingSnapshotTokenUtil.computeEtag(snapshot);
        if (!currentEtag.equals(requestEtag)) {
            throw ApiException.conflict(currentEtag);
        }

        if (!"CONFIRMED".equals(snapshot.bookingStatus)) {
            throw new ApiException(400, "Chỉ cho phép sửa booking ở trạng thái CONFIRMED");
        }

        return snapshot;
    }

    private StaffBookingSnapshotTokenUtil.Snapshot assertReleaseSnapshot(
            Connection conn, int bookingId, int facilityId, String requestEtag) throws Exception {
        StaffBookingSnapshotTokenUtil.Snapshot snapshot =
                StaffBookingSnapshotTokenUtil.loadSnapshot(conn, bookingId, facilityId);
        if (snapshot == null) {
            throw new ApiException(404, "Không tìm thấy booking");
        }

        String currentEtag = StaffBookingSnapshotTokenUtil.computeEtag(snapshot);
        if (!currentEtag.equals(requestEtag)) {
            throw ApiException.conflict(currentEtag);
        }

        if (!"CONFIRMED".equals(snapshot.bookingStatus) && !"COMPLETED".equals(snapshot.bookingStatus)) {
            throw new ApiException(400, "Chỉ cho phép giải phóng slot khi booking ở trạng thái CONFIRMED hoặc COMPLETED");
        }

        return snapshot;
    }


    private void ensureEditableRemovals(Connection conn, int bookingId, Set<Integer> removeSlotIds) throws Exception {
        if (removeSlotIds.isEmpty()) return;

        Set<Integer> pending = new HashSet<>(loadPendingSlotIds(conn, bookingId));
        for (Integer slotId : removeSlotIds) {
            if (!pending.contains(slotId)) {
                throw new ApiException(400, "Chỉ được bỏ các slot PENDING");
            }
        }
    }

    private void ensureNoDuplicateAddPairs(List<SlotPair> addSlots) throws Exception {
        Set<String> keys = new HashSet<>();
        for (SlotPair slot : addSlots) {
            if (slot.courtId <= 0 || slot.slotId <= 0) {
                throw new ApiException(400, "addSlots không hợp lệ");
            }
            String key = slot.courtId + "-" + slot.slotId;
            if (!keys.add(key)) {
                throw new ApiException(400, "addSlots bị trùng courtId-slotId");
            }
        }
    }
    private void validateSessionRuleAfterEdit(Connection conn, int bookingId,
                                              Set<Integer> removeSlotIds, List<SlotPair> addSlots) throws Exception {
        List<SessionCell> cells = new ArrayList<>();

        String sql = "SELECT bs.booking_slot_id, bs.court_id, bs.slot_id, bs.slot_status, ts.start_time, ts.end_time " +
                "FROM BookingSlot bs JOIN TimeSlot ts ON bs.slot_id = ts.slot_id WHERE bs.booking_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookingId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String status = rs.getString("slot_status");
                    int bookingSlotId = rs.getInt("booking_slot_id");
                    if ("CANCELLED".equals(status)) continue;
                    if ("PENDING".equals(status) && removeSlotIds.contains(bookingSlotId)) continue;

                    SessionCell c = new SessionCell();
                    c.courtId = rs.getInt("court_id");
                    c.slotId = rs.getInt("slot_id");
                    c.start = rs.getTime("start_time").toLocalTime();
                    c.end = rs.getTime("end_time").toLocalTime();
                    cells.add(c);
                }
            }
        }

        for (SlotPair add : addSlots) {
            SessionCell c = loadSessionCell(conn, add.courtId, add.slotId);
            if (c == null) {
                throw new ApiException(400, "Slot thêm mới không hợp lệ");
            }
            cells.add(c);
        }

        Map<Integer, List<SessionCell>> byCourt = new HashMap<>();
        for (SessionCell c : cells) {
            byCourt.computeIfAbsent(c.courtId, k -> new ArrayList<>()).add(c);
        }

        for (Map.Entry<Integer, List<SessionCell>> entry : byCourt.entrySet()) {
            List<SessionCell> list = entry.getValue();
            list.sort(Comparator.comparing(a -> a.start));
            int len = 1;
            for (int i = 1; i < list.size(); i++) {
                SessionCell prev = list.get(i - 1);
                SessionCell cur = list.get(i);
                if (prev.end.equals(cur.start)) {
                    len++;
                } else {
                    if (len < 2) {
                        throw new ApiException(400, "Mỗi phiên phải có ít nhất 2 slot liên tiếp trên cùng 1 sân");
                    }
                    len = 1;
                }
            }
            if (!list.isEmpty() && len < 2) {
                throw new ApiException(400, "Mỗi phiên phải có ít nhất 2 slot liên tiếp trên cùng 1 sân");
            }
        }
    }

    private SessionCell loadSessionCell(Connection conn, int courtId, int slotId) throws SQLException {
        String sql = "SELECT ts.start_time, ts.end_time FROM TimeSlot ts WHERE ts.slot_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, slotId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                SessionCell c = new SessionCell();
                c.courtId = courtId;
                c.slotId = slotId;
                c.start = rs.getTime("start_time").toLocalTime();
                c.end = rs.getTime("end_time").toLocalTime();
                return c;
            }
        }
    }

    private void ensureAddSlotsNotExpired(Connection conn, LocalDate bookingDate, List<SlotPair> addSlots)
            throws ApiException, SQLException {
        if (!LocalDate.now().equals(bookingDate) || addSlots == null || addSlots.isEmpty()) {
            return;
        }
        LocalTime now = LocalTime.now();
        for (SlotPair slot : addSlots) {
            SessionCell cell = loadSessionCell(conn, slot.courtId, slot.slotId);
            if (cell == null) {
                throw new ApiException(400, "Slot khong ton tai");
            }
            if (now.compareTo(cell.end) >= 0) {
                throw new ApiException(400, "Da qua gio ket thuc cua mot hoac nhieu slot. Vui long chon slot khac.");
            }
        }
    }
    private void cancelPendingSlot(Connection conn, int bookingId, int bookingSlotId) throws Exception {
        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE BookingSlot SET slot_status = 'CANCELLED', is_released = 1 " +
                        "WHERE booking_id = ? AND booking_slot_id = ? AND slot_status = 'PENDING'")) {
            ps.setInt(1, bookingId);
            ps.setInt(2, bookingSlotId);
            int affected = ps.executeUpdate();
            if (affected == 0) {
                throw new ApiException(400, "Không thể hủy slot không ở trạng thái PENDING");
            }
        }

        try (PreparedStatement ps = conn.prepareStatement(
                "DELETE FROM CourtSlotBooking WHERE booking_slot_id = ?")) {
            ps.setInt(1, bookingSlotId);
            ps.executeUpdate();
        }
    }

    private void upsertPendingSlot(Connection conn, int bookingId, int facilityId,
                                   LocalDate bookingDate, SlotPair slot) throws Exception {
        String existingSql = "SELECT booking_slot_id, slot_status FROM BookingSlot " +
                "WHERE booking_id = ? AND court_id = ? AND slot_id = ?";
        Integer existingBookingSlotId = null;
        String existingStatus = null;

        try (PreparedStatement ps = conn.prepareStatement(existingSql)) {
            ps.setInt(1, bookingId);
            ps.setInt(2, slot.courtId);
            ps.setInt(3, slot.slotId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    existingBookingSlotId = rs.getInt("booking_slot_id");
                    existingStatus = rs.getString("slot_status");
                }
            }
        }

        BigDecimal price = lookupCurrentPrice(conn, facilityId, bookingDate, slot.courtId, slot.slotId);
        if (price == null) {
            throw new ApiException(400, "Không tìm thấy giá cho slot thêm mới");
        }

        int bookingSlotId;
        if (existingBookingSlotId != null) {
            if (!"CANCELLED".equals(existingStatus)) {
                throw new ApiException(409, "Slot thêm mới đã tồn tại trong booking");
            }

            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE BookingSlot SET slot_status = 'PENDING', is_released = 0, price = ?, " +
                            "checkin_time = NULL, checkout_time = NULL WHERE booking_slot_id = ?")) {
                ps.setBigDecimal(1, price);
                ps.setInt(2, existingBookingSlotId);
                ps.executeUpdate();
            }
            bookingSlotId = existingBookingSlotId;
        } else {
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO BookingSlot (booking_id, court_id, slot_id, price, is_released, slot_status) " +
                            "VALUES (?, ?, ?, ?, 0, 'PENDING')", Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, bookingId);
                ps.setInt(2, slot.courtId);
                ps.setInt(3, slot.slotId);
                ps.setBigDecimal(4, price);
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (!keys.next()) {
                        throw new ApiException(500, "Không thể tạo booking slot mới");
                    }
                    bookingSlotId = keys.getInt(1);
                }
            }
        }

        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO CourtSlotBooking (court_id, booking_date, slot_id, booking_slot_id) VALUES (?, ?, ?, ?)")) {
            ps.setInt(1, slot.courtId);
            ps.setDate(2, Date.valueOf(bookingDate));
            ps.setInt(3, slot.slotId);
            ps.setInt(4, bookingSlotId);
            try {
                ps.executeUpdate();
            } catch (SQLException e) {
                throw new ApiException(409, "Slot thêm mới đã bị người khác đặt");
            }
        }
    }
    private BigDecimal lookupCurrentPrice(Connection conn, int facilityId, LocalDate bookingDate,
                                          int courtId, int slotId) throws SQLException {
        String dayType = (bookingDate.getDayOfWeek() == DayOfWeek.SATURDAY
                || bookingDate.getDayOfWeek() == DayOfWeek.SUNDAY) ? "WEEKEND" : "WEEKDAY";

        String sql = "SELECT TOP 1 fpr.price " +
                "FROM Court c " +
                "JOIN TimeSlot ts ON ts.slot_id = ? " +
                "JOIN FacilityPriceRule " +
                "fpr ON fpr.facility_id = c.facility_id AND fpr.court_type_id = c.court_type_id " +
                "WHERE c.court_id = ? AND c.facility_id = ? AND c.is_active = 1 " +
                "AND fpr.day_type = ? " +
                "AND ts.start_time >= fpr.start_time AND ts.end_time <= fpr.end_time " +
                "ORDER BY fpr.start_time";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, slotId);
            ps.setInt(2, courtId);
            ps.setInt(3, facilityId);
            ps.setString(4, dayType);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getBigDecimal("price");
                return null;
            }
        }
    }

    private InvoiceUpdateResult recalcInvoice(Connection conn, int bookingId, String reason) throws SQLException {
        BigDecimal totalAmount = BigDecimal.ZERO;
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT COALESCE(SUM(price),0) AS total_amount FROM BookingSlot WHERE booking_id = ? AND slot_status <> 'CANCELLED'")) {
            ps.setInt(1, bookingId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) totalAmount = rs.getBigDecimal("total_amount");
            }
        }

        BigDecimal paidAmount = BigDecimal.ZERO;
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT paid_amount FROM Invoice WHERE booking_id = ?")) {
            ps.setInt(1, bookingId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) paidAmount = rs.getBigDecimal("paid_amount");
                else throw new SQLException("Invoice not found");
            }
        }

        BigDecimal refundDue = paidAmount.subtract(totalAmount);
        if (refundDue.compareTo(BigDecimal.ZERO) < 0) {
            refundDue = BigDecimal.ZERO;
        }
        String refundStatus = refundDue.compareTo(BigDecimal.ZERO) > 0 ? "PENDING_MANUAL" : "NONE";

        String refundNote = null;
        if (refundDue.compareTo(BigDecimal.ZERO) > 0) {
            refundNote = "Manual refund pending" + (reason != null ? (": " + reason) : "");
        }

        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE Invoice SET total_amount = ?, refund_due = ?, refund_status = ?, refund_note = ? WHERE booking_id = ?")) {
            ps.setBigDecimal(1, totalAmount);
            ps.setBigDecimal(2, refundDue);
            ps.setString(3, refundStatus);
            if (refundNote == null) ps.setNull(4, java.sql.Types.NVARCHAR);
            else ps.setString(4, refundNote);
            ps.setInt(5, bookingId);
            ps.executeUpdate();
        }

        InvoiceUpdateResult r = new InvoiceUpdateResult();
        r.totalAmount = totalAmount;
        r.paidAmount = paidAmount;
        r.refundDue = refundDue;
        r.refundStatus = refundStatus;
        return r;
    }

    private String recomputeBookingStatus(Connection conn, int bookingId) throws SQLException {
        int activeCount = 0;
        int playedCount = 0;

        String sql = "SELECT slot_status, COUNT(*) AS cnt FROM BookingSlot WHERE booking_id = ? GROUP BY slot_status";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookingId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String status = rs.getString("slot_status");
                    int cnt = rs.getInt("cnt");
                    if (!"CANCELLED".equals(status)) {
                        activeCount += cnt;
                    }
                    if ("CHECK_OUT".equals(status) || "NO_SHOW".equals(status)) {
                        playedCount += cnt;
                    }
                }
            }
        }

        if (activeCount == 0) {
            return playedCount > 0 ? "COMPLETED" : "CANCELLED";
        }
        return "CONFIRMED";
    }

    private void updateBookingStatus(Connection conn, int bookingId, String status) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE Booking SET booking_status = ? WHERE booking_id = ?")) {
            ps.setString(1, status);
            ps.setInt(2, bookingId);
            ps.executeUpdate();
        }
    }

    private void insertAuditLog(Connection conn, int bookingId, int staffId,
                                String changeAction, String changeType,
                                String reason, String beforeEtag, String afterEtag,
                                StaffBookingSnapshotTokenUtil.Snapshot before,
                                StaffBookingSnapshotTokenUtil.Snapshot after,
                                BigDecimal refundDue) throws SQLException {

        String beforeJson = buildSnapshotJson(before);
        String afterJson = buildSnapshotJson(after);

        String sql = "INSERT INTO BookingChangeLog (booking_id, change_type, note, actor_staff_id, change_action, " +
                "before_data, after_data, reason, etag_before, etag_after, refund_due) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookingId);
            ps.setString(2, changeType);
            ps.setString(3, changeAction);
            ps.setInt(4, staffId);
            ps.setString(5, changeAction);
            ps.setNString(6, beforeJson);
            ps.setNString(7, afterJson);
            if (reason == null) ps.setNull(8, java.sql.Types.NVARCHAR);
            else ps.setNString(8, reason);
            ps.setString(9, beforeEtag);
            ps.setString(10, afterEtag);
            ps.setBigDecimal(11, refundDue);
            ps.executeUpdate();
        }
    }
    private String buildSnapshotJson(StaffBookingSnapshotTokenUtil.Snapshot s) {
        StringBuilder json = new StringBuilder(1024);
        json.append('{');
        json.append("\"bookingStatus\":\"").append(s.bookingStatus).append("\"");

        json.append(",\"slots\":[");
        for (int i = 0; i < s.slots.size(); i++) {
            StaffBookingSnapshotTokenUtil.SlotSnapshot slot = s.slots.get(i);
            if (i > 0) json.append(',');
            json.append('{')
                    .append("\"bookingSlotId\":").append(slot.bookingSlotId)
                    .append(",\"courtId\":").append(slot.courtId)
                    .append(",\"slotId\":").append(slot.slotId)
                    .append(",\"slotStatus\":\"").append(slot.slotStatus).append("\"")
                    .append(",\"price\":").append(slot.price)
                    .append(",\"released\":").append(slot.released)
                    .append('}');
        }
        json.append(']');

        if (s.invoice != null) {
            json.append(",\"invoice\":{")
                    .append("\"totalAmount\":").append(s.invoice.totalAmount)
                    .append(",\"paidAmount\":").append(s.invoice.paidAmount)
                    .append(",\"paymentStatus\":\"").append(s.invoice.paymentStatus).append("\"")
                    .append(",\"refundDue\":").append(s.invoice.refundDue)
                    .append(",\"refundStatus\":\"").append(s.invoice.refundStatus).append("\"")
                    .append('}');
        } else {
            json.append(",\"invoice\":null");
        }

        json.append('}');
        return json.toString();
    }

    private List<Integer> loadPendingSlotIds(Connection conn, int bookingId) throws SQLException {
        List<Integer> ids = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT booking_slot_id FROM BookingSlot WHERE booking_id = ? AND slot_status = 'PENDING'")) {
            ps.setInt(1, bookingId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) ids.add(rs.getInt("booking_slot_id"));
            }
        }
        return ids;
    }

    private boolean hasSlotStatus(Connection conn, int bookingId, String status) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT TOP 1 1 FROM BookingSlot WHERE booking_id = ? AND slot_status = ?")) {
            ps.setInt(1, bookingId);
            ps.setString(2, status);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private SlotState loadSlotState(Connection conn, int bookingId, int bookingSlotId) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT slot_status, is_released FROM BookingSlot WHERE booking_id = ? AND booking_slot_id = ?")) {
            ps.setInt(1, bookingId);
            ps.setInt(2, bookingSlotId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                SlotState s = new SlotState();
                s.slotStatus = rs.getString("slot_status");
                s.released = rs.getBoolean("is_released");
                return s;
            }
        }
    }

    private List<SlotPair> parseSlotPairs(String json, String key) {
        String arr = extractArray(json, key);
        List<SlotPair> out = new ArrayList<>();
        if (arr == null || arr.length() < 2) return out;

        int pos = 0;
        while (pos < arr.length()) {
            int objStart = arr.indexOf('{', pos);
            if (objStart < 0) break;
            int objEnd = arr.indexOf('}', objStart);
            if (objEnd < 0) break;
            String obj = arr.substring(objStart, objEnd + 1);

            SlotPair p = new SlotPair();
            p.courtId = parseInt(StaffBookingSnapshotTokenUtil.extractString(obj, "courtId"));
            p.slotId = parseInt(StaffBookingSnapshotTokenUtil.extractString(obj, "slotId"));
            if (p.courtId > 0 && p.slotId > 0) {
                out.add(p);
            }
            pos = objEnd + 1;
        }

        return out;
    }

    private Set<Integer> parseIntSet(String json, String key) {
        String arr = extractArray(json, key);
        Set<Integer> out = new LinkedHashSet<>();
        if (arr == null || arr.length() < 2) return out;

        String body = arr.substring(1, arr.length() - 1).trim();
        if (body.isEmpty()) return out;

        String[] parts = body.split(",");
        for (String part : parts) {
            int val = parseInt(part.trim());
            if (val > 0) out.add(val);
        }
        return out;
    }

    private String extractArray(String json, String key) {
        if (json == null) return null;
        String search = '"' + key + '"';
        int idx = json.indexOf(search);
        if (idx < 0) return null;

        int start = json.indexOf('[', idx + search.length());
        if (start < 0) return null;

        int depth = 0;
        for (int i = start; i < json.length(); i++) {
            char c = json.charAt(i);
            if (c == '[') depth++;
            else if (c == ']') {
                depth--;
                if (depth == 0) {
                    return json.substring(start, i + 1);
                }
            }
        }
        return null;
    }

    private String readBody(HttpServletRequest request) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }
        return sb.toString();
    }

    private int parseInt(String value) {
        if (value == null) return -1;
        try {
            return Integer.parseInt(value.trim());
        } catch (Exception e) {
            return -1;
        }
    }

    private BigDecimal parseMoney(String value) {
        try {
            return new BigDecimal(value);
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    private String trimToNull(String s) {
        if (s == null) return null;
        String v = s.trim();
        return v.isEmpty() ? null : v;
    }

    private static class SlotPair {
        int courtId;
        int slotId;
    }

    private static class SlotState {
        String slotStatus;
        boolean released;
    }

    private static class SessionCell {
        int courtId;
        int slotId;
        LocalTime start;
        LocalTime end;
    }

    private static class InvoiceUpdateResult {
        BigDecimal totalAmount;
        BigDecimal paidAmount;
        BigDecimal refundDue;
        String refundStatus;
    }

    private static class ApiException extends Exception {
        final int status;
        final String message;
        final String currentEtag;

        ApiException(int status, String message) {
            this(status, message, null);
        }

        ApiException(int status, String message, String currentEtag) {
            super(message);
            this.status = status;
            this.message = message;
            this.currentEtag = currentEtag;
        }

        static ApiException conflict(String currentEtag) {
            return new ApiException(409, "Booking data changed, please reload.", currentEtag);
        }

        String toJson() {
            StringBuilder json = new StringBuilder(160);
            json.append("{\"success\":false,\"message\":")
                    .append(StaffAuthUtil.escapeJson(message));
            if (currentEtag != null) {
                json.append(",\"data\":{\"currentEtag\":")
                        .append(StaffAuthUtil.escapeJson(currentEtag))
                        .append("}");
            }
            json.append("}");
            return json.toString();
        }
    }
}

