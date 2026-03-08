package com.bcb.service.staff;

import com.bcb.dto.staff.StaffBookingListDataDto;

public interface StaffBookingListService {
    StaffBookingListDataDto getBookingList(int facilityId, String search, int page, int size) throws Exception;
}
