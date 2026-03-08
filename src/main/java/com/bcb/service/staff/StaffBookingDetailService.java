package com.bcb.service.staff;

import com.bcb.dto.staff.StaffBookingDetailDataDto;

public interface StaffBookingDetailService {
    StaffBookingDetailDataDto getBookingDetail(int bookingId, int staffFacilityId) throws Exception;
}
