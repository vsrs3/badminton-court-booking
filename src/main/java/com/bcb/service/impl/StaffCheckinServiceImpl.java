package com.bcb.service.impl;

import com.bcb.dto.staff.StaffCheckinBookingDTO;
import com.bcb.dto.staff.StaffCheckinSessionDTO;
import com.bcb.repository.impl.StaffCheckinRepositoryImpl;
import com.bcb.repository.staff.StaffCheckinRepository;
import com.bcb.service.staff.StaffCheckinService;
import com.bcb.utils.DBContext;

import java.sql.Connection;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class StaffCheckinServiceImpl implements StaffCheckinService {

    private static final int NO_SHOW_BUFFER_MINUTES = 15;
    private static final int AUTO_CHECKOUT_GRACE_MINUTES = 15;
    private final StaffCheckinRepository repository = new StaffCheckinRepositoryImpl();

    @Override
    public String doCheckin(int bookingId, int sessionIndex, int facilityId) throws Exception {
        try (Connection conn = DBContext.getConnection()) {
            conn.setAutoCommit(false);
            try {
                String valResult = validateBooking(conn, bookingId, facilityId);
                if (valResult != null) {
                    conn.rollback();
                    return valResult;
                }

                List<StaffCheckinSessionDTO> sessions =
                        StaffCheckinSessionBuilder.buildSessionsWithTime(repository, conn, bookingId);
                if (sessionIndex >= sessions.size()) {
                    conn.rollback();
                    return "{\"success\":false,\"message\":\"Session index không hợp lệ\"}";
                }

                StaffCheckinSessionDTO target = sessions.get(sessionIndex);
                String dateCheck = validateSessionDate(target);
                if (dateCheck != null) {
                    conn.rollback();
                    return dateCheck;
                }
                String targetStatus = getSessionStatus(conn, target.getSlotIds());

                if ("CHECKED_IN".equals(targetStatus) || "COMPLETED".equals(targetStatus)) {
                    conn.rollback();
                    return "{\"success\":false,\"message\":\"Phiên này đã được check-in\"}";
                }
                if ("NO_SHOW".equals(targetStatus)) {
                    conn.rollback();
                    return "{\"success\":false,\"message\":\"Phiên này đã được đánh dấu vắng mặt\"}";
                }
                if ("CANCELLED".equals(targetStatus)) {
                    conn.rollback();
                    return "{\"success\":false,\"message\":\"Phiên này đã bị hủy\"}";
                }

                LocalTime now = LocalTime.now();
                int autoNoShowCount = 0;

                for (int i = 0; i < sessionIndex; i++) {
                    StaffCheckinSessionDTO prev = sessions.get(i);
                    String prevStatus = getSessionStatus(conn, prev.getSlotIds());

                    if ("PENDING".equals(prevStatus)) {
                        LocalTime deadline = prev.getStartTime().plusMinutes(NO_SHOW_BUFFER_MINUTES);
                        if (now.isAfter(deadline)) {
                            repository.updateSlotsNoShow(conn, prev.getSlotIds());
                            autoNoShowCount++;
                        } else {
                            conn.rollback();
                            return "{\"success\":false,\"message\":\"Phiên " + (i + 1)
                                    + " chưa quá giờ. Vui lòng check-in phiên đó trước hoặc đợi hết giờ.\"}";
                        }
                    }
                }

                Timestamp nowTs = new Timestamp(System.currentTimeMillis());
                repository.updateSlotsCheckedIn(conn, target.getSlotIds(), nowTs);

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

    @Override
    public String doCheckout(int bookingId, int sessionIndex, int facilityId) throws Exception {
        try (Connection conn = DBContext.getConnection()) {
            conn.setAutoCommit(false);
            try {
                String valResult = validateBooking(conn, bookingId, facilityId);
                if (valResult != null) {
                    conn.rollback();
                    return valResult;
                }

                List<StaffCheckinSessionDTO> sessions =
                        StaffCheckinSessionBuilder.buildSessionsWithTime(repository, conn, bookingId);
                if (sessionIndex >= sessions.size()) {
                    conn.rollback();
                    return "{\"success\":false,\"message\":\"Session index không hợp lệ\"}";
                }

                StaffCheckinSessionDTO target = sessions.get(sessionIndex);
                String dateCheck = validateSessionDate(target);
                if (dateCheck != null) {
                    conn.rollback();
                    return dateCheck;
                }
                String targetStatus = getSessionStatus(conn, target.getSlotIds());

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
                if ("CANCELLED".equals(targetStatus)) {
                    conn.rollback();
                    return "{\"success\":false,\"message\":\"Phiên đã bị hủy\"}";
                }

                Timestamp nowTs = new Timestamp(System.currentTimeMillis());
                repository.updateSlotsCheckedOut(conn, target.getSlotIds(), nowTs);

                boolean allFinished = checkAllSessionsFinished(conn, sessions, sessionIndex, "COMPLETED");
                if (allFinished) {
                    repository.updateBookingStatus(conn, bookingId, "COMPLETED");
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

    @Override
    public String doNoShow(int bookingId, int sessionIndex, int facilityId) throws Exception {
        try (Connection conn = DBContext.getConnection()) {
            conn.setAutoCommit(false);
            try {
                String valResult = validateBookingForNoShow(conn, bookingId, facilityId);
                if (valResult != null) {
                    conn.rollback();
                    return valResult;
                }

                List<StaffCheckinSessionDTO> sessions =
                        StaffCheckinSessionBuilder.buildSessionsWithTime(repository, conn, bookingId);
                if (sessionIndex >= sessions.size()) {
                    conn.rollback();
                    return "{\"success\":false,\"message\":\"Session index không hợp lệ\"}";
                }

                StaffCheckinSessionDTO target = sessions.get(sessionIndex);
                String dateCheck = validateSessionDate(target);
                if (dateCheck != null) {
                    conn.rollback();
                    return dateCheck;
                }
                String targetStatus = getSessionStatus(conn, target.getSlotIds());

                if (!"PENDING".equals(targetStatus)) {
                    conn.rollback();
                    String label = statusLabel(targetStatus);
                    return "{\"success\":false,\"message\":\"Không thể đánh dấu vắng. Phiên đang ở trạng thái: " + label + "\"}";
                }

                LocalTime now = LocalTime.now();
                LocalTime deadline = target.getStartTime().plusMinutes(NO_SHOW_BUFFER_MINUTES);
                if (!now.isAfter(deadline)) {
                    conn.rollback();
                    String deadlineStr = deadline.toString().substring(0, 5);
                    return "{\"success\":false,\"message\":\"Chưa quá giờ. Chỉ có thể đánh dấu vắng sau " + deadlineStr + "\"}";
                }

                repository.updateSlotsNoShow(conn, target.getSlotIds());

                boolean allFinished = checkAllSessionsFinished(conn, sessions, sessionIndex, "NO_SHOW");
                if (allFinished) {
                    repository.updateBookingStatus(conn, bookingId, "COMPLETED");
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

    public void runAutoCheckoutOverdueSessions() throws Exception {
        try (Connection conn = DBContext.getConnection()) {
            conn.setAutoCommit(false);
            try {
                LocalDate today = LocalDate.now();
                LocalTime now = LocalTime.now();
                Timestamp nowTs = new Timestamp(System.currentTimeMillis());

                List<Integer> bookingIds = repository.findBookingIdsWithCheckedInSlots(conn, today);
                for (Integer bookingId : bookingIds) {
                    List<StaffCheckinSessionDTO> sessions =
                            StaffCheckinSessionBuilder.buildSessionsWithTime(repository, conn, bookingId);
                    boolean updatedAny = false;

                    for (StaffCheckinSessionDTO session : sessions) {
                        if (!"CHECKED_IN".equals(getSessionStatus(conn, session.getSlotIds()))) {
                            continue;
                        }
                        if (isOverdueForAutoCheckout(session, today, now)) {
                            repository.updateSlotsCheckedOut(conn, session.getSlotIds(), nowTs);
                            updatedAny = true;
                        }
                    }

                    if (updatedAny && isBookingFinished(conn, sessions)) {
                        repository.updateBookingStatus(conn, bookingId, "COMPLETED");
                    }
                }

                conn.commit();
            } catch (Exception e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    private boolean checkAllSessionsFinished(Connection conn, List<StaffCheckinSessionDTO> sessions,
                                             int justFinishedIndex, String justFinishedAs) throws Exception {
        for (int i = 0; i < sessions.size(); i++) {
            String status;
            if (i == justFinishedIndex) {
                status = justFinishedAs;
            } else {
                status = getSessionStatus(conn, sessions.get(i).getSlotIds());
            }
            if (!"COMPLETED".equals(status) && !"NO_SHOW".equals(status) && !"CANCELLED".equals(status)) {
                return false;
            }
        }
        return true;
    }

    private String validateBooking(Connection conn, int bookingId, int facilityId) throws Exception {
        String base = validateBookingBase(conn, bookingId, facilityId);
        if (base != null) return base;

        String paymentStatus = repository.findInvoicePaymentStatus(conn, bookingId);
        if (paymentStatus == null) {
            return "{\"success\":false,\"message\":\"Không tìm thấy hóa đơn cho booking này\"}";
        }
        if (!"PAID".equals(paymentStatus)) {
            return "{\"success\":false,\"message\":\"Booking chưa thanh toán đủ. Vui lòng xác nhận thanh toán trước khi check-in/check-out.\"}";
        }
        return null;
    }

    private String validateBookingForNoShow(Connection conn, int bookingId, int facilityId) throws Exception {
        return validateBookingBase(conn, bookingId, facilityId);
    }

    private String validateSessionDate(StaffCheckinSessionDTO session) {
        if (session == null || session.getSessionDate() == null) {
            return "{\"success\":false,\"message\":\"Không tìm thấy ngày của phiên chơi\"}";
        }
        if (!LocalDate.now().equals(session.getSessionDate())) {
            return "{\"success\":false,\"message\":\"Chỉ check-in/out booking ngày hôm nay\"}";
        }
        return null;
    }

    private String validateBookingBase(Connection conn, int bookingId, int facilityId) throws Exception {
        StaffCheckinBookingDTO booking = repository.findBooking(conn, bookingId);
        if (booking == null) return "{\"success\":false,\"message\":\"Không tìm thấy booking\"}";

        if (booking.getFacilityId() != facilityId)
            return "{\"success\":false,\"message\":\"Booking không thuộc cơ sở của bạn\"}";

        if (!"CONFIRMED".equals(booking.getBookingStatus()))
            return "{\"success\":false,\"message\":\"Chỉ xử lý booking đã xác nhận. Trạng thái: \" + booking.getBookingStatus() + \"\"}";

        return null;
    }

    private String getSessionStatus(Connection conn, List<Integer> slotIds) throws Exception {
        if (slotIds.isEmpty()) return "PENDING";

        List<String> statuses = repository.findSlotStatuses(conn, slotIds);

        boolean allCheckout = true;
        boolean allNoShow = true;
        boolean allCancelled = true;
        boolean anyCheckedIn = false;

        for (String s : statuses) {
            if (!"CHECK_OUT".equals(s)) allCheckout = false;
            if (!"NO_SHOW".equals(s)) allNoShow = false;
            if (!"CANCELLED".equals(s)) allCancelled = false;
            if ("CHECKED_IN".equals(s)) anyCheckedIn = true;
        }

        if (allCancelled) return "CANCELLED";
        if (allNoShow) return "NO_SHOW";
        if (allCheckout) return "COMPLETED";
        if (anyCheckedIn) return "CHECKED_IN";
        return "PENDING";
    }

    private boolean isOverdueForAutoCheckout(StaffCheckinSessionDTO session, LocalDate today, LocalTime now) {
        if (session == null || session.getSessionDate() == null || session.getEndTime() == null) return false;
        LocalDate date = session.getSessionDate();
        if (date.isBefore(today)) return true;
        if (!date.isEqual(today)) return false;
        LocalTime deadline = session.getEndTime().plusMinutes(AUTO_CHECKOUT_GRACE_MINUTES);
        return !now.isBefore(deadline);
    }

    private boolean isBookingFinished(Connection conn, List<StaffCheckinSessionDTO> sessions) throws Exception {
        for (StaffCheckinSessionDTO session : sessions) {
            String status = getSessionStatus(conn, session.getSlotIds());
            if (!"COMPLETED".equals(status) && !"NO_SHOW".equals(status) && !"CANCELLED".equals(status)) {
                return false;
            }
        }
        return true;
    }

    private String statusLabel(String status) {
        switch (status) {
            case "CHECKED_IN":
                return "Đang chơi";
            case "COMPLETED":
                return "Hoàn thành";
            case "NO_SHOW":
                return "Vắng mặt";
            case "CANCELLED":
                return "Đã hủy";
            default:
                return status;
        }
    }
}


