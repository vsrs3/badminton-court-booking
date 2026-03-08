package com.bcb.service.impl;

import com.bcb.dto.staff.StaffTimelineBookedCellDto;
import com.bcb.dto.staff.StaffTimelineDataDto;
import com.bcb.dto.staff.StaffTimelineDisabledCellDto;
import com.bcb.dto.staff.StaffTimelineFacilityDto;
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

    @Override
    public StaffTimelineDataDto getTimeline(int facilityId, LocalDate bookingDate) throws Exception {
        StaffTimelineFacilityDto facility = repository.findFacilityInfo(facilityId);

        StaffTimelineDataDto data = new StaffTimelineDataDto();
        data.setFacilityName(facility.getFacilityName() != null ? facility.getFacilityName() : "");
        data.setBookingDate(bookingDate);
        data.setCourts(repository.findActiveCourts(facilityId));
        data.setSlots(repository.findSlotsWithinHours(facility.getOpenTime(), facility.getCloseTime()));

        List<StaffTimelineBookedCellDto> bookedCells = repository.findBookedCells(facilityId, bookingDate);
        List<StaffTimelineDisabledCellDto> disabledCells = repository.findDisabledCells(
                facilityId, bookingDate, facility.getOpenTime(), facility.getCloseTime());

        Map<String, StaffTimelineBookedCellDto> bookedMap = new LinkedHashMap<>();
        for (StaffTimelineBookedCellDto bookedCell : bookedCells) {
            String key = bookedCell.getCourtId() + "-" + bookedCell.getSlotId();
            bookedMap.put(key, bookedCell);
        }

        Map<String, StaffTimelineDisabledCellDto> disabledMap = new LinkedHashMap<>();
        for (StaffTimelineDisabledCellDto disabledCell : disabledCells) {
            String key = disabledCell.getCourtId() + "-" + disabledCell.getSlotId();
            disabledMap.put(key, disabledCell);
        }

        List<StaffTimelineBookedCellDto> filteredBooked = new ArrayList<>();
        for (Map.Entry<String, StaffTimelineBookedCellDto> entry : bookedMap.entrySet()) {
            if (!disabledMap.containsKey(entry.getKey())) {
                filteredBooked.add(entry.getValue());
            }
        }

        List<StaffTimelineDisabledCellDto> mergedDisabled = new ArrayList<>(disabledMap.values());

        data.setBookedCells(filteredBooked);
        data.setDisabledCells(mergedDisabled);
        return data;
    }
}

