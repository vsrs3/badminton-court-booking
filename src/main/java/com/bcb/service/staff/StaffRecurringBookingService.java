package com.bcb.service.staff;

import com.bcb.dto.staff.StaffRecurringBookingOutcomeDTO;

public interface StaffRecurringBookingService {
    StaffRecurringBookingOutcomeDTO preview(String body, int facilityId, Integer staffId) throws Exception;

    StaffRecurringBookingOutcomeDTO confirm(String body, int facilityId, Integer staffId) throws Exception;
}
