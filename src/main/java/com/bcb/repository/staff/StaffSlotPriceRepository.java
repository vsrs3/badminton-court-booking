package com.bcb.repository.staff;

import com.bcb.dto.staff.StaffFacilityHoursDto;
import com.bcb.dto.staff.StaffPriceRuleDto;
import com.bcb.dto.staff.StaffSlotPriceCourtDto;
import com.bcb.dto.staff.StaffTimeSlotDto;

import java.util.List;

public interface StaffSlotPriceRepository {
    List<StaffSlotPriceCourtDto> findActiveCourts(int facilityId) throws Exception;

    StaffFacilityHoursDto findFacilityHours(int facilityId) throws Exception;

    List<StaffTimeSlotDto> findTimeSlotsWithinHours(String openTime, String closeTime) throws Exception;

    List<StaffPriceRuleDto> findPriceRules(int facilityId, String dayType) throws Exception;
}

