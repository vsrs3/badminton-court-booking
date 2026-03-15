package com.bcb.service.staff;

import com.bcb.dto.staff.StaffBookingListDataDTO;

public interface StaffBookingListService {
    StaffBookingListDataDTO getBookingList(int facilityId, String search, String status, boolean todayOnly, int page, int size) throws Exception;
}

