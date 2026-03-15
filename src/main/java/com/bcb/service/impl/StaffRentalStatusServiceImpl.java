package com.bcb.service.impl;

import com.bcb.dto.staff.StaffRentalStatusCourtDTO;
import com.bcb.dto.staff.StaffRentalStatusDataDTO;
import com.bcb.dto.staff.StaffRentalStatusRawRowDTO;
import com.bcb.dto.staff.StaffRentalStatusRowDTO;
import com.bcb.dto.staff.StaffRentalStatusUpdateResultDTO;
import com.bcb.repository.impl.StaffRentalStatusRepositoryImpl;
import com.bcb.repository.staff.StaffRentalStatusRepository;
import com.bcb.service.staff.StaffRentalStatusService;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class StaffRentalStatusServiceImpl implements StaffRentalStatusService {

    private static final String STATUS_RENTED = "RENTED";
    private static final String STATUS_RETURNED = "RETURNED";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("H:mm");

    private final StaffRentalStatusRepository repository = new StaffRentalStatusRepositoryImpl();

    @Override
    public StaffRentalStatusDataDTO getRentalStatusData(int facilityId) throws Exception {
        List<StaffRentalStatusCourtDTO> courts = repository.findCourtsByFacility(facilityId);
        Map<Integer, StaffRentalStatusCourtDTO> courtMap = new LinkedHashMap<>();
        Map<Integer, Map<String, StaffRentalStatusRowDTO>> lastRowByCourt = new HashMap<>();
        for (StaffRentalStatusCourtDTO court : courts) {
            courtMap.put(court.getCourtId(), court);
        }

        for (StaffRentalStatusRawRowDTO raw : repository.findRentalStatusRows(facilityId)) {
            StaffRentalStatusCourtDTO court = courtMap.get(raw.getCourtId());
            if (court == null) {
                court = new StaffRentalStatusCourtDTO();
                court.setCourtId(raw.getCourtId());
                court.setCourtName(raw.getCourtName());
                courtMap.put(raw.getCourtId(), court);
            }

            Map<String, StaffRentalStatusRowDTO> rowGroupMap =
                    lastRowByCourt.computeIfAbsent(raw.getCourtId(), key -> new HashMap<>());
            List<StaffRentalStatusRowDTO> rows = court.getRows();
            String rowGroupKey = buildRowGroupKey(raw);
            StaffRentalStatusRowDTO current = rowGroupMap.get(rowGroupKey);
            if (canMerge(current, raw)) {
                current.getRentalIds().add(raw.getRentalId());
                current.setEndTime(formatTime(raw.getEndTime()));
                current.setSlotLabel(buildSlotLabel(current.getStartTime(), current.getEndTime()));
            } else {
                StaffRentalStatusRowDTO row = new StaffRentalStatusRowDTO();
                row.setCustomerName(defaultString(raw.getCustomerName()));
                row.setInventoryName(defaultString(raw.getInventoryName()));
                row.setQuantity(raw.getQuantity());
                row.setCourtName(defaultString(raw.getCourtName()));
                row.setBookingDate(raw.getBookingDate() != null ? raw.getBookingDate().format(DATE_FORMAT) : "");
                row.setStartTime(formatTime(raw.getStartTime()));
                row.setEndTime(formatTime(raw.getEndTime()));
                row.setSlotLabel(buildSlotLabel(row.getStartTime(), row.getEndTime()));
                row.setStatus(raw.getReturnedAt() == null ? STATUS_RENTED : STATUS_RETURNED);
                row.getRentalIds().add(raw.getRentalId());
                rows.add(row);
                rowGroupMap.put(rowGroupKey, row);
            }
        }

        StaffRentalStatusDataDTO data = new StaffRentalStatusDataDTO();
        data.setCourts(List.copyOf(courtMap.values()));
        return data;
    }

    @Override
    public StaffRentalStatusUpdateResultDTO updateRentalStatus(int facilityId, List<Integer> rentalIds, boolean returned)
            throws Exception {
        int updatedCount = repository.updateReturnedStatus(facilityId, rentalIds, returned);

        StaffRentalStatusUpdateResultDTO result = new StaffRentalStatusUpdateResultDTO();
        result.setReturned(returned);
        result.setStatus(returned ? STATUS_RETURNED : STATUS_RENTED);
        result.setUpdatedCount(updatedCount);
        return result;
    }

    private boolean canMerge(StaffRentalStatusRowDTO current, StaffRentalStatusRawRowDTO raw) {
        if (current == null) {
            return false;
        }
        return Objects.equals(current.getCustomerName(), defaultString(raw.getCustomerName()))
                && Objects.equals(current.getInventoryName(), defaultString(raw.getInventoryName()))
                && current.getQuantity() == raw.getQuantity()
                && Objects.equals(current.getCourtName(), defaultString(raw.getCourtName()))
                && Objects.equals(current.getBookingDate(), raw.getBookingDate() != null ? raw.getBookingDate().format(DATE_FORMAT) : "")
                && Objects.equals(current.getStatus(), raw.getReturnedAt() == null ? STATUS_RENTED : STATUS_RETURNED)
                && Objects.equals(current.getEndTime(), formatTime(raw.getStartTime()));
    }

    private String buildRowGroupKey(StaffRentalStatusRawRowDTO raw) {
        return raw.getCourtId() + "|"
                + defaultString(raw.getCustomerName()) + "|"
                + defaultString(raw.getInventoryName()) + "|"
                + raw.getQuantity() + "|"
                + (raw.getBookingDate() != null ? raw.getBookingDate().format(DATE_FORMAT) : "") + "|"
                + (raw.getReturnedAt() == null ? STATUS_RENTED : STATUS_RETURNED);
    }

    private String formatTime(LocalTime time) {
        return time == null ? "" : time.format(TIME_FORMAT);
    }

    private String buildSlotLabel(String startTime, String endTime) {
        return startTime + " - " + endTime;
    }

    private String defaultString(String value) {
        return value == null ? "" : value;
    }
}
