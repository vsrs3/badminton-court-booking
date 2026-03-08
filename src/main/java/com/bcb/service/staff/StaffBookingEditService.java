package com.bcb.service.staff;

import com.bcb.dto.staff.StaffBookingEditOutcomeDto;

public interface StaffBookingEditService {
    StaffBookingEditOutcomeDto process(String servletPath, int facilityId, int staffId, String body) throws Exception;
}
