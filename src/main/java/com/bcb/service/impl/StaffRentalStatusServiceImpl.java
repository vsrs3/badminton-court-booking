package com.bcb.service.impl;

import com.bcb.dto.staff.StaffRentalStatusCellDTO;
import com.bcb.dto.staff.StaffRentalStatusCourtDTO;
import com.bcb.dto.staff.StaffRentalStatusDataDTO;
import com.bcb.dto.staff.StaffRentalStatusItemDTO;
import com.bcb.dto.staff.StaffRentalStatusRawRowDTO;
import com.bcb.dto.staff.StaffRentalStatusUpdateResultDTO;
import com.bcb.dto.staff.StaffTimelineFacilityDTO;
import com.bcb.repository.impl.StaffRentalStatusRepositoryImpl;
import com.bcb.repository.impl.StaffTimelineRepositoryImpl;
import com.bcb.service.staff.StaffRentalStatusService;
import com.bcb.utils.DBContext;

import java.sql.Connection;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class StaffRentalStatusServiceImpl implements StaffRentalStatusService {

    private static final String STATUS_RENTED = "RENTED";
    private static final String STATUS_RENTING = "RENTING";
    private static final String STATUS_RETURNED = "RETURNED";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE;

    private final StaffRentalStatusRepositoryImpl repository = new StaffRentalStatusRepositoryImpl();
    private final StaffTimelineRepositoryImpl timelineRepository = new StaffTimelineRepositoryImpl();

    @Override
    public StaffRentalStatusDataDTO getRentalStatusData(int facilityId, LocalDate bookingDate) throws Exception {
        List<StaffRentalStatusCourtDTO> courts;
        List<StaffRentalStatusRawRowDTO> rawRows;
        List<com.bcb.dto.staff.StaffTimelineSlotDTO> slots;
        List<com.bcb.dto.staff.StaffRentalInventoryStockDTO> inventoryItems;

        try (Connection conn = DBContext.getConnection()) {
            StaffTimelineFacilityDTO facility = timelineRepository.findFacilityInfo(conn, facilityId);
            String openTime = facility.getOpenTime() != null ? facility.getOpenTime() : "00:00";
            String closeTime = facility.getCloseTime() != null ? facility.getCloseTime() : "23:59";

            courts = repository.findCourtsByFacility(conn, facilityId);
            rawRows = repository.findRentalStatusRows(conn, facilityId, bookingDate);
            slots = timelineRepository.findSlotsWithinHours(conn, openTime, closeTime);
            inventoryItems = repository.findInventoryStocks(conn, facilityId);
        }

        Map<String, StaffRentalStatusCellDTO> cellMap = new LinkedHashMap<>();
        for (StaffRentalStatusRawRowDTO raw : rawRows) {
            String cellKey = buildCellKey(raw.getCourtId(), raw.getSlotId());
            StaffRentalStatusCellDTO cell = cellMap.get(cellKey);
            if (cell == null) {
                cell = new StaffRentalStatusCellDTO();
                cell.setCourtId(raw.getCourtId());
                cell.setSlotId(raw.getSlotId());
                cell.setBookingId(raw.getBookingId());
                cell.setCustomerName(defaultString(raw.getCustomerName()));
                cell.setCustomerKey(buildCustomerKey(raw));
                cell.setStatus(raw.getStatus());
                cellMap.put(cellKey, cell);
            }

            StaffRentalStatusItemDTO item = new StaffRentalStatusItemDTO();
            item.setScheduleId(raw.getScheduleId());
            item.setInventoryId(raw.getInventoryId());
            item.setInventoryName(defaultString(raw.getInventoryName()));
            item.setQuantity(raw.getQuantity());
            item.setStatus(raw.getStatus());
            cell.getItems().add(item);

            cell.setItemCount(cell.getItems().size());
            cell.setTotalQuantity(cell.getTotalQuantity() + raw.getQuantity());
            cell.setStatus(mergeStatus(cell.getStatus(), raw.getStatus()));
        }

        List<StaffRentalStatusCellDTO> cells = new ArrayList<>(cellMap.values());
        for (StaffRentalStatusCellDTO cell : cells) {
            cell.getItems().sort(Comparator.comparing(StaffRentalStatusItemDTO::getInventoryName));
        }

        cells.sort(Comparator
                .comparingInt(StaffRentalStatusCellDTO::getCourtId)
                .thenComparingInt(StaffRentalStatusCellDTO::getSlotId));

        StaffRentalStatusDataDTO data = new StaffRentalStatusDataDTO();
        data.setSelectedDate(bookingDate.format(DATE_FORMAT));
        data.setCourts(courts);
        data.setSlots(slots);
        data.setCells(cells);
        data.setInventoryItems(inventoryItems);
        return data;
    }

    @Override
    public StaffRentalStatusUpdateResultDTO updateRentalStatus(int facilityId, int scheduleId, String nextStatus)
            throws Exception {
        validateCurrentSlotForStatusUpdate(facilityId, scheduleId);

        int updatedCount = repository.updateScheduleStatus(facilityId, scheduleId, nextStatus);

        StaffRentalStatusUpdateResultDTO result = new StaffRentalStatusUpdateResultDTO();
        result.setReturned(STATUS_RETURNED.equals(nextStatus));
        result.setStatus(nextStatus);
        result.setUpdatedCount(updatedCount);
        return result;
    }

    private String buildCellKey(int courtId, int slotId) {
        return courtId + "_" + slotId;
    }

    private String buildCustomerKey(StaffRentalStatusRawRowDTO raw) {
        if (raw.getAccountId() != null) {
            return "ACCOUNT_" + raw.getAccountId();
        }
        if (raw.getGuestId() != null) {
            return "GUEST_" + raw.getGuestId();
        }
        return defaultString(raw.getCustomerName());
    }

    private String mergeStatus(String current, String next) {
        if (STATUS_RENTING.equals(current) || STATUS_RENTING.equals(next)) {
            return STATUS_RENTING;
        }
        if (STATUS_RENTED.equals(current) || STATUS_RENTED.equals(next)) {
            return STATUS_RENTED;
        }
        return STATUS_RETURNED;
    }

    private void validateCurrentSlotForStatusUpdate(int facilityId, int scheduleId) throws Exception {
        try (Connection conn = DBContext.getConnection()) {
            StaffRentalStatusRawRowDTO schedule = repository.findScheduleRowById(conn, facilityId, scheduleId);

            if (schedule == null) {
                throw new IllegalArgumentException("Không tìm thấy lịch thuê đồ cần cập nhật.");
            }

            if (!LocalDate.now().equals(schedule.getBookingDate())) {
                throw new IllegalArgumentException("Chỉ được cập nhật trạng thái cho lịch thuê đồ của ngày hôm nay.");
            }

            LocalTime startTime = schedule.getStartTime();
            LocalTime endTime = schedule.getEndTime();
            if (startTime == null || endTime == null) {
                throw new IllegalArgumentException("Không xác định được khung giờ của lịch thuê đồ.");
            }

            LocalTime now = LocalTime.now();
            boolean inCurrentSlot = !now.isBefore(startTime) && now.isBefore(endTime);
            if (!inCurrentSlot) {
                throw new IllegalArgumentException("Chỉ slot đang chứa thời gian hiện tại mới được cập nhật trạng thái.");
            }
        }
    }

    private String defaultString(String value) {
        return value == null ? "" : value;
    }
}
