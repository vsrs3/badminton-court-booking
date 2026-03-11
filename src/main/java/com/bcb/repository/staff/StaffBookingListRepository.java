package com.bcb.repository.staff;

import com.bcb.dto.staff.StaffBookingListItemDTO;
import com.bcb.dto.staff.StaffBookingListSearchCriteriaDTO;

import java.util.List;

public interface StaffBookingListRepository {
    int countBookings(StaffBookingListSearchCriteriaDTO criteria) throws Exception;

    List<StaffBookingListItemDTO> findBookings(StaffBookingListSearchCriteriaDTO criteria, int offset, int size) throws Exception;
}

