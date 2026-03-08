package com.bcb.service.staff;

import com.bcb.dto.staff.StaffBookingCreateOutcomeDto;

public interface StaffBookingCreateService {
    StaffBookingCreateOutcomeDto createBooking(String body, int facilityId, Integer staffId) throws Exception;
}

