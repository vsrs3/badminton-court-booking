package com.bcb.repository.staff;

import com.bcb.dto.staff.StaffTimelineBookedCellDto;
import com.bcb.dto.staff.StaffTimelineCourtDto;
import com.bcb.dto.staff.StaffTimelineDisabledCellDto;
import com.bcb.dto.staff.StaffTimelineFacilityDto;
import com.bcb.dto.staff.StaffTimelineSlotDto;

import java.time.LocalDate;
import java.util.List;

public interface StaffTimelineRepository {
    StaffTimelineFacilityDto findFacilityInfo(int facilityId) throws Exception;

    List<StaffTimelineCourtDto> findActiveCourts(int facilityId) throws Exception;

    List<StaffTimelineSlotDto> findSlotsWithinHours(String openTime, String closeTime) throws Exception;

    List<StaffTimelineBookedCellDto> findBookedCells(int facilityId, LocalDate bookingDate) throws Exception;

    List<StaffTimelineDisabledCellDto> findDisabledCells(int facilityId, LocalDate bookingDate,
                                                         String openTime, String closeTime) throws Exception;
}

