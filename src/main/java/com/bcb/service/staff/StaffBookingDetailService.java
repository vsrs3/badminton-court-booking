package com.bcb.service.staff;

import com.bcb.dto.staff.StaffBookingDetailDataDTO;

public interface StaffBookingDetailService {
    StaffBookingDetailDataDTO getBookingDetail(int bookingId, int staffFacilityId) throws Exception;
}

