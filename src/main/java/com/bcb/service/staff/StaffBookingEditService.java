package com.bcb.service.staff;

import com.bcb.dto.staff.StaffBookingEditOutcomeDTO;

public interface StaffBookingEditService {
    StaffBookingEditOutcomeDTO process(String servletPath, int facilityId, int staffId, String body) throws Exception;
}

