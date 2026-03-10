package com.bcb.repository.staff;

import com.bcb.dto.staff.StaffFacilityHoursDTO;
import com.bcb.dto.staff.StaffPriceRuleDTO;
import com.bcb.dto.staff.StaffSlotPriceCourtDTO;
import com.bcb.dto.staff.StaffTimeSlotDTO;

import java.util.List;

public interface StaffSlotPriceRepository {
    List<StaffSlotPriceCourtDTO> findActiveCourts(int facilityId) throws Exception;

    StaffFacilityHoursDTO findFacilityHours(int facilityId) throws Exception;

    List<StaffTimeSlotDTO> findTimeSlotsWithinHours(String openTime, String closeTime) throws Exception;

    List<StaffPriceRuleDTO> findPriceRules(int facilityId, String dayType) throws Exception;
}

