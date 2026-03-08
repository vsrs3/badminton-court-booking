package com.bcb.service.staff;

import com.bcb.dto.staff.StaffSlotPriceDataDto;

import java.time.LocalDate;

public interface StaffSlotPriceService {
    StaffSlotPriceDataDto getSlotPrices(int facilityId, LocalDate bookingDate) throws Exception;
}
