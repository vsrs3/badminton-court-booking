package com.bcb.service.impl;

import com.bcb.dto.staff.StaffTimelineBookedCellDTO;
import com.bcb.dto.staff.StaffTimelineDataDTO;
import com.bcb.dto.staff.StaffTimelineDisabledCellDTO;
import com.bcb.dto.staff.StaffTimelineFacilityDTO;
import com.bcb.repository.impl.StaffTimelineRepositoryImpl;
import com.bcb.repository.staff.StaffTimelineRepository;
import com.bcb.service.staff.StaffTimelineService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class StaffTimelineServiceImpl implements StaffTimelineService {

    private final StaffTimelineRepository repository = new StaffTimelineRepositoryImpl();

    /**
     * Builds a daily timeline view with courts, slots, bookings, and blocked cells for one facility.
     */
    @Override
    public StaffTimelineDataDTO getTimeline(int facilityId, LocalDate bookingDate) throws Exception {
        StaffTimelineFacilityDTO facility = repository.findFacilityInfo(facilityId);

        StaffTimelineDataDTO data = new StaffTimelineDataDTO();
        data.setFacilityName(facility.getFacilityName() != null ? facility.getFacilityName() : "");
        data.setBookingDate(bookingDate);
        data.setCourts(repository.findActiveCourts(facilityId));
        data.setSlots(repository.findSlotsWithinHours(facility.getOpenTime(), facility.getCloseTime()));

        List<StaffTimelineBookedCellDTO> bookedCells = repository.findBookedCells(facilityId, bookingDate);
        List<StaffTimelineDisabledCellDTO> disabledCells = repository.findDisabledCells(
                facilityId, bookingDate, facility.getOpenTime(), facility.getCloseTime());

        Map<String, StaffTimelineBookedCellDTO> bookedMap = new LinkedHashMap<>();
        for (StaffTimelineBookedCellDTO bookedCell : bookedCells) {
            String key = bookedCell.getCourtId() + "-" + bookedCell.getSlotId();
            bookedMap.put(key, bookedCell);
        }

        Map<String, StaffTimelineDisabledCellDTO> disabledMap = new LinkedHashMap<>();
        for (StaffTimelineDisabledCellDTO disabledCell : disabledCells) {
            String key = disabledCell.getCourtId() + "-" + disabledCell.getSlotId();
            disabledMap.put(key, disabledCell);
        }

        // Blocked/exception slots override booked cells in the grid.
        List<StaffTimelineBookedCellDTO> filteredBooked = new ArrayList<>();
        for (Map.Entry<String, StaffTimelineBookedCellDTO> entry : bookedMap.entrySet()) {
            if (!disabledMap.containsKey(entry.getKey())) {
                filteredBooked.add(entry.getValue());
            }
        }

        List<StaffTimelineDisabledCellDTO> mergedDisabled = new ArrayList<>(disabledMap.values());

        data.setBookedCells(filteredBooked);
        data.setDisabledCells(mergedDisabled);
        return data;
    }
}

