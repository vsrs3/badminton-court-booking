package com.bcb.controller.staff;

import com.bcb.model.CourtScheduleException;
import com.bcb.repository.impl.StaffScheduleExceptionRepositoryImpl;
import com.bcb.repository.staff.StaffScheduleExceptionRepository;
import com.bcb.utils.DBContext;
import com.bcb.utils.api.JsonResponseUtil;
import com.bcb.utils.staff.StaffAuthUtil;
import com.bcb.utils.staff.StaffAuthUtil.AuthResult;
import com.bcb.utils.staff.StaffBookingSnapshotTokenUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.Connection;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;

@WebServlet(name = "StaffScheduleExceptionApiServlet", urlPatterns = {"/api/staff/schedule-exception/*"})
public class StaffScheduleExceptionApiServlet extends BaseStaffApiServlet {

    private final StaffScheduleExceptionRepository repository = new StaffScheduleExceptionRepositoryImpl();

    /**
     * Handles schedule exception create/delete (block/unblock) for staff.
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json;charset=UTF-8");

        AuthResult auth = StaffAuthUtil.validateStaff(request, response);
        if (!auth.valid) return;

        Integer staffId = (Integer) request.getSession().getAttribute("staffId");
        if (staffId == null) {
            writeError(response, 403, "Staff chưa được gán");
            return;
        }

        String path = request.getPathInfo();
        String body = readRequestBody(request);

        try {
            if ("/create".equals(path)) {
                handleCreate(response, auth.facilityId, staffId, body);
            } else if ("/delete".equals(path)) {
                handleDelete(response, auth.facilityId, staffId, body);
            } else {
                writeError(response, 404, "API không tồn tại");
            }
        } catch (ApiException e) {
            writeJson(response, e.status, JsonResponseUtil.error(e.message, e.data));
        } catch (Exception e) {
            e.printStackTrace();
            writeError(response, 500, "Lỗi hệ thống");
        }
    }

    private void handleCreate(HttpServletResponse response, int facilityId, int staffId, String body) throws Exception {
        String dateStr = StaffBookingSnapshotTokenUtil.extractString(body, "date");
        int courtId = parseInt(StaffBookingSnapshotTokenUtil.extractString(body, "courtId"));
        int slotId = parseInt(StaffBookingSnapshotTokenUtil.extractString(body, "slotId"));
        String reason = trimToNull(StaffBookingSnapshotTokenUtil.extractString(body, "reason"));
        if (reason == null) reason = "Bảo trì";

        if (dateStr == null || dateStr.isEmpty() || courtId <= 0 || slotId <= 0) {
            throw new ApiException(400, "Thiếu thông tin block lịch");
        }

        LocalDate date;
        try {
            date = LocalDate.parse(dateStr);
        } catch (DateTimeParseException e) {
            throw new ApiException(400, "Ngày không hợp lệ");
        }

        try (Connection conn = DBContext.getConnection()) {
            conn.setAutoCommit(false);
            try {
                /*
                 * Block transaction:
                 * - Validate slot time and ensure it is in the future.
                 * - Reject if slot already booked or blocked.
                 * - Insert CourtScheduleException for the slot time range.
                 */
                LocalTime[] slotTimes = repository.findSlotTime(conn, slotId);
                if (slotTimes == null) {
                    throw new ApiException(404, "Không tìm thấy slot");
                }

                LocalDate today = LocalDate.now();
                LocalTime now = LocalTime.now();
                // Block only future slots; reject past time.
                if (date.isBefore(today) || (date.equals(today) && now.isAfter(slotTimes[1]))) {
                    throw new ApiException(400, "Slot đã quá giờ, không thể block");
                }
                // Prevent block if slot already booked or already blocked.
                if (repository.hasBooking(conn, facilityId, courtId, date, slotId)) {
                    throw new ApiException(409, "Slot đang có booking, vui lòng xử lý booking trước");
                }

                if (repository.hasActiveExceptionOverlap(conn, facilityId, courtId, date, slotTimes[0], slotTimes[1])) {
                    throw new ApiException(409, "Slot đã bị block");
                }

                CourtScheduleException ex = new CourtScheduleException();
                ex.setFacilityId(facilityId);
                ex.setCourtId(courtId);
                ex.setStartDate(date);
                ex.setEndDate(date);
                ex.setStartTime(slotTimes[0]);
                ex.setEndTime(slotTimes[1]);
                ex.setExceptionType("OTHER");
                ex.setReason(reason);
                ex.setCreatedBy(staffId);

                int exceptionId = repository.insertException(conn, ex);
                conn.commit();

                Map<String, Object> data = new HashMap<>();
                data.put("exceptionId", exceptionId);
                writeJson(response, JsonResponseUtil.success("Block lịch thành công", data));
            } catch (Exception e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    private void handleDelete(HttpServletResponse response, int facilityId, int staffId, String body) throws Exception {
        int exceptionId = parseInt(StaffBookingSnapshotTokenUtil.extractString(body, "exceptionId"));
        int courtId = parseInt(StaffBookingSnapshotTokenUtil.extractString(body, "courtId"));
        int slotId = parseInt(StaffBookingSnapshotTokenUtil.extractString(body, "slotId"));
        String dateStr = StaffBookingSnapshotTokenUtil.extractString(body, "date");

        if (exceptionId <= 0 || courtId <= 0 || slotId <= 0 || dateStr == null || dateStr.isEmpty()) {
            throw new ApiException(400, "Thiếu thông tin gỡ block");
        }

        LocalDate date;
        try {
            date = LocalDate.parse(dateStr);
        } catch (DateTimeParseException e) {
            throw new ApiException(400, "Ngày không hợp lệ");
        }

        try (Connection conn = DBContext.getConnection()) {
            conn.setAutoCommit(false);
            try {
                /*
                 * Unblock transaction:
                 * - Load active exception and validate same-day delete.
                 * - If exact match, deactivate exception.
                 * - Otherwise split into before/after ranges and deactivate original.
                 */
                CourtScheduleException ex = repository.findActiveExceptionById(conn, facilityId, exceptionId);
                if (ex == null) {
                    throw new ApiException(404, "Không tìm thấy block");
                }
                if (ex.getCourtId() == null || ex.getCourtId() != courtId) {
                    throw new ApiException(400, "Thông tin sân không khớp");
                }
                if (date.isBefore(ex.getStartDate()) || date.isAfter(ex.getEndDate())) {
                    throw new ApiException(400, "Ngày không thuộc khoảng block");
                }
                // Unblock supports same-day only; split exception into before/after.
                if (!date.equals(ex.getStartDate()) || !date.equals(ex.getEndDate())) {
                    throw new ApiException(400, "Chỉ hỗ trợ gỡ block trong ngày");
                }

                LocalTime[] slotTimes = repository.findSlotTime(conn, slotId);
                if (slotTimes == null) {
                    throw new ApiException(404, "Không tìm thấy slot");
                }
                LocalTime slotStart = slotTimes[0];
                LocalTime slotEnd = slotTimes[1];

                LocalTime exStart = ex.getStartTime();
                LocalTime exEnd = ex.getEndTime();
                if (exStart == null || exEnd == null) {
                    LocalTime[] bounds = repository.findTimeSlotBounds(conn);
                    if (bounds == null) {
                        throw new ApiException(500, "Không tải được mốc thời gian");
                    }
                    exStart = bounds[0];
                    exEnd = bounds[1];
                }

                if (!slotStart.isBefore(exEnd) || !slotEnd.isAfter(exStart)) {
                    throw new ApiException(400, "Slot không nằm trong block");
                }

                boolean exactMatch = slotStart.equals(exStart) && slotEnd.equals(exEnd);
                if (exactMatch) {
                    // Deactivate exception when exact match.
                    repository.deactivateException(conn, exceptionId);
                } else {
                    if (exStart.isBefore(slotStart)) {
                        CourtScheduleException before = new CourtScheduleException();
                        before.setFacilityId(facilityId);
                        before.setCourtId(courtId);
                        before.setStartDate(date);
                        before.setEndDate(date);
                        before.setStartTime(exStart);
                        before.setEndTime(slotStart);
                        before.setExceptionType(ex.getExceptionType() != null ? ex.getExceptionType() : "OTHER");
                        before.setReason(ex.getReason());
                        before.setCreatedBy(staffId);
                        repository.insertException(conn, before);
                    }
                    if (exEnd.isAfter(slotEnd)) {
                        CourtScheduleException after = new CourtScheduleException();
                        after.setFacilityId(facilityId);
                        after.setCourtId(courtId);
                        after.setStartDate(date);
                        after.setEndDate(date);
                        after.setStartTime(slotEnd);
                        after.setEndTime(exEnd);
                        after.setExceptionType(ex.getExceptionType() != null ? ex.getExceptionType() : "OTHER");
                        after.setReason(ex.getReason());
                        after.setCreatedBy(staffId);
                        repository.insertException(conn, after);
                    }
                    repository.deactivateException(conn, exceptionId);
                }

                conn.commit();
                writeJson(response, JsonResponseUtil.success("Gỡ block thành công", null));
            } catch (Exception e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    private int parseInt(String value) {
        if (value == null) return -1;
        try {
            return Integer.parseInt(value.trim());
        } catch (Exception e) {
            return -1;
        }
    }

    private String trimToNull(String s) {
        if (s == null) return null;
        String v = s.trim();
        return v.isEmpty() ? null : v;
    }

    private static class ApiException extends Exception {
        final int status;
        final String message;
        final Map<String, Object> data;

        ApiException(int status, String message) {
            this(status, message, null);
        }

        ApiException(int status, String message, Map<String, Object> data) {
            super(message);
            this.status = status;
            this.message = message;
            this.data = data;
        }
    }
}


