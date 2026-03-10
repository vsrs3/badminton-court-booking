package com.bcb.repository.staff;

import com.bcb.dto.staff.StaffTimelineBookedCellDTO;
import com.bcb.dto.staff.StaffTimelineCourtDTO;
import com.bcb.dto.staff.StaffTimelineDisabledCellDTO;
import com.bcb.dto.staff.StaffTimelineFacilityDTO;
import com.bcb.dto.staff.StaffTimelineSlotDTO;

import java.time.LocalDate;
import java.util.List;

public interface StaffTimelineRepository {
    StaffTimelineFacilityDTO findFacilityInfo(int facilityId) throws Exception;

    List<StaffTimelineCourtDTO> findActiveCourts(int facilityId) throws Exception;

    List<StaffTimelineSlotDTO> findSlotsWithinHours(String openTime, String closeTime) throws Exception;

    List<StaffTimelineBookedCellDTO> findBookedCells(int facilityId, LocalDate bookingDate) throws Exception;

    List<StaffTimelineDisabledCellDTO> findDisabledCells(int facilityId, LocalDate bookingDate,
                                                         String openTime, String closeTime) throws Exception;
}

