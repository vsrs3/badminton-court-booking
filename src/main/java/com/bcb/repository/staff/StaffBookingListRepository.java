package com.bcb.repository.staff;

import com.bcb.dto.staff.StaffBookingListItemDto;
import com.bcb.dto.staff.StaffBookingListSearchCriteriaDto;

import java.util.List;

public interface StaffBookingListRepository {
    int countBookings(StaffBookingListSearchCriteriaDto criteria) throws Exception;

    List<StaffBookingListItemDto> findBookings(StaffBookingListSearchCriteriaDto criteria, int offset, int size) throws Exception;
}
