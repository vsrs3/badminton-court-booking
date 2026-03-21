package com.bcb.service.impl;

import com.bcb.dto.staff.StaffBookingDetailHeaderDTO;
import com.bcb.dto.staff.StaffBookingDetailRentalItemDTO;
import com.bcb.dto.staff.StaffBookingDetailRentalRowDTO;
import com.bcb.dto.staff.StaffBookingDetailSlotDTO;
import com.bcb.repository.impl.StaffBookingDetailRepositoryImpl;
import com.bcb.repository.impl.StaffRentalStatusRepositoryImpl;
import com.bcb.repository.staff.StaffBookingDetailRepository;
import com.bcb.service.staff.StaffBookingRentalService;
import com.bcb.utils.DBContext;

import java.sql.Connection;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class StaffBookingRentalServiceImpl implements StaffBookingRentalService {

    private static final String STATUS_RENTED = "RENTED";
    private static final String STATUS_RENTING = "RENTING";
    private static final String STATUS_RETURNED = "RETURNED";

    private final StaffBookingDetailRepository bookingDetailRepository = new StaffBookingDetailRepositoryImpl();
    private final StaffRentalStatusRepositoryImpl rentalStatusRepository = new StaffRentalStatusRepositoryImpl();

    @Override
    public String updateSessionRentalStatus(int bookingId, int sessionIndex, String nextStatus, int facilityId) throws Exception {
        if (!STATUS_RENTING.equals(nextStatus) && !STATUS_RETURNED.equals(nextStatus)) {
            return errorJson("Trạng thái thuê đồ không hợp lệ.");
        }

        try (Connection conn = DBContext.getConnection()) {
            conn.setAutoCommit(false);
            try {
                StaffBookingDetailHeaderDTO header = bookingDetailRepository.findBookingHeader(conn, bookingId);
                if (header == null || header.getFacilityId() != facilityId) {
                    conn.rollback();
                    return errorJson("Không tìm thấy booking hợp lệ.");
                }

                List<StaffBookingDetailSlotDTO> allSlots = bookingDetailRepository.findBookingSlots(conn, bookingId);
                List<List<StaffBookingDetailSlotDTO>> sessions = groupIntoSessions(allSlots);
                if (sessionIndex < 0 || sessionIndex >= sessions.size()) {
                    conn.rollback();
                    return errorJson("Session index không hợp lệ.");
                }

                List<StaffBookingDetailSlotDTO> targetSession = sessions.get(sessionIndex);
                String sessionDate = targetSession.get(0).getBookingDate();
                if (sessionDate == null || !LocalDate.now().toString().equals(sessionDate)) {
                    conn.rollback();
                    return errorJson("Chỉ có thể cập nhật đồ thuê cho phiên của hôm nay.");
                }

                String sessionStatus = deriveSessionStatus(targetSession);
                if ("NO_SHOW".equals(sessionStatus) || "CANCELLED".equals(sessionStatus) || "COMPLETED".equals(sessionStatus)) {
                    conn.rollback();
                    return errorJson("Phiên hiện tại không thể cập nhật trạng thái đồ thuê.");
                }

                List<StaffBookingDetailRentalRowDTO> rentalRows = bookingDetailRepository.findBookingRentalRows(conn, bookingId);
                List<StaffBookingDetailRentalItemDTO> sessionItems = collectSessionRentalItems(targetSession, rentalRows);
                if (sessionItems.isEmpty()) {
                    conn.rollback();
                    return errorJson("Phiên này không có đồ thuê.");
                }

                String currentStatus = deriveRentalStatus(sessionItems);
                if (STATUS_RENTING.equals(nextStatus)) {
                    if (STATUS_RENTING.equals(currentStatus)) {
                        conn.rollback();
                        return errorJson("Đồ thuê của phiên này đang ở trạng thái đang thuê.");
                    }
                    if (STATUS_RETURNED.equals(currentStatus)) {
                        conn.rollback();
                        return errorJson("Đồ thuê của phiên này đã được trả.");
                    }
                } else if (!STATUS_RENTING.equals(currentStatus)) {
                    conn.rollback();
                    return errorJson("Chỉ có thể xác nhận trả đồ khi phiên đang ở trạng thái đang thuê.");
                }

                Set<Integer> scheduleIds = new LinkedHashSet<>();
                for (StaffBookingDetailRentalItemDTO item : sessionItems) {
                    if (item.getScheduleId() != null && item.getScheduleId() > 0) {
                        scheduleIds.add(item.getScheduleId());
                    }
                }

                if (scheduleIds.isEmpty()) {
                    conn.rollback();
                    return errorJson("Không tìm thấy lịch thuê đồ để cập nhật.");
                }

                int updatedCount = 0;
                for (Integer scheduleId : scheduleIds) {
                    updatedCount += rentalStatusRepository.updateScheduleStatus(conn, facilityId, scheduleId, nextStatus);
                }

                conn.commit();
                return "{\"success\":true,\"message\":\"" + successMessage(nextStatus)
                        + "\",\"data\":{\"status\":\"" + nextStatus
                        + "\",\"updatedCount\":" + updatedCount + "}}";
            } catch (Exception e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    private String successMessage(String nextStatus) {
        if (STATUS_RENTING.equals(nextStatus)) {
            return "Đã chuyển đồ thuê sang trạng thái đang thuê.";
        }
        return "Đã cập nhật trạng thái đã trả đồ.";
    }

    private String errorJson(String message) {
        return "{\"success\":false,\"message\":\"" + escapeJson(message) + "\"}";
    }

    private String escapeJson(String value) {
        if (value == null) return "";
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private List<StaffBookingDetailRentalItemDTO> collectSessionRentalItems(List<StaffBookingDetailSlotDTO> sessionSlots,
                                                                            List<StaffBookingDetailRentalRowDTO> rentalRows) {
        Set<String> slotKeys = new LinkedHashSet<>();
        for (StaffBookingDetailSlotDTO slot : sessionSlots) {
            slotKeys.add(buildSlotKey(slot.getBookingDate(), slot.getCourtId(), slot.getSlotId()));
        }

        List<StaffBookingDetailRentalItemDTO> items = new ArrayList<>();
        for (StaffBookingDetailRentalRowDTO row : rentalRows) {
            if (!slotKeys.contains(buildSlotKey(row.getBookingDate(), row.getCourtId(), row.getSlotId()))) {
                continue;
            }
            items.addAll(row.getRentalItems());
        }
        return items;
    }

    private String deriveRentalStatus(List<StaffBookingDetailRentalItemDTO> items) {
        boolean hasTrackedStatus = false;
        boolean anyRenting = false;
        boolean anyRented = false;
        boolean allReturned = true;

        for (StaffBookingDetailRentalItemDTO item : items) {
            if (item.getScheduleId() == null || item.getScheduleId() <= 0) {
                continue;
            }
            hasTrackedStatus = true;
            String status = item.getStatus() == null ? STATUS_RENTED : item.getStatus().trim().toUpperCase();
            if (STATUS_RENTING.equals(status)) {
                anyRenting = true;
            }
            if (STATUS_RENTED.equals(status)) {
                anyRented = true;
            }
            if (!STATUS_RETURNED.equals(status)) {
                allReturned = false;
            }
        }

        if (anyRenting) {
            return STATUS_RENTING;
        }
        if (hasTrackedStatus && allReturned) {
            return STATUS_RETURNED;
        }
        if (anyRented || hasTrackedStatus) {
            return STATUS_RENTED;
        }
        return "";
    }

    private String buildSlotKey(String bookingDate, int courtId, int slotId) {
        return (bookingDate == null ? "" : bookingDate) + "_" + courtId + "_" + slotId;
    }

    private List<List<StaffBookingDetailSlotDTO>> groupIntoSessions(List<StaffBookingDetailSlotDTO> slots) {
        List<List<StaffBookingDetailSlotDTO>> sessions = new ArrayList<>();
        if (slots == null || slots.isEmpty()) {
            return sessions;
        }

        List<StaffBookingDetailSlotDTO> current = new ArrayList<>();
        current.add(slots.get(0));

        for (int i = 1; i < slots.size(); i++) {
            StaffBookingDetailSlotDTO prev = slots.get(i - 1);
            StaffBookingDetailSlotDTO next = slots.get(i);

            boolean sameCourt = prev.getCourtId() == next.getCourtId();
            boolean sameDate = safeValue(prev.getBookingDate()).equals(safeValue(next.getBookingDate()));
            boolean consecutive = safeValue(prev.getEndTime()).equals(safeValue(next.getStartTime()));

            if (sameCourt && sameDate && consecutive) {
                current.add(next);
            } else {
                sessions.add(current);
                current = new ArrayList<>();
                current.add(next);
            }
        }

        sessions.add(current);
        return sessions;
    }

    private String deriveSessionStatus(List<StaffBookingDetailSlotDTO> session) {
        boolean allCancelled = true;
        boolean allCheckout = true;
        boolean allNoShow = true;
        boolean anyCheckedIn = false;

        for (StaffBookingDetailSlotDTO slot : session) {
            String slotStatus = slot.getSlotStatus();
            if (!"CANCELLED".equals(slotStatus)) allCancelled = false;
            if (!"CHECK_OUT".equals(slotStatus)) allCheckout = false;
            if (!"NO_SHOW".equals(slotStatus)) allNoShow = false;
            if ("CHECKED_IN".equals(slotStatus)) anyCheckedIn = true;
        }

        if (allCancelled) return "CANCELLED";
        if (allNoShow) return "NO_SHOW";
        if (allCheckout) return "COMPLETED";
        if (anyCheckedIn) return "CHECKED_IN";
        return "PENDING";
    }

    private String safeValue(String value) {
        return value == null ? "" : value;
    }
}
