package com.bcb.service.staff;

import com.bcb.dto.staff.StaffBookingCreateOutcomeDTO;

public interface StaffBookingCreateService {
    StaffBookingCreateOutcomeDTO createBooking(String body, int facilityId, Integer staffId) throws Exception;
}

