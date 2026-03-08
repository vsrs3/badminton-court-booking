package com.bcb.service.staff;

import com.bcb.dto.staff.StaffSlotPriceDataDTO;

import java.time.LocalDate;

public interface StaffSlotPriceService {
    StaffSlotPriceDataDTO getSlotPrices(int facilityId, LocalDate bookingDate) throws Exception;
}

