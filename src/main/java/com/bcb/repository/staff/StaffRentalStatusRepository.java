package com.bcb.repository.staff;

import com.bcb.dto.staff.StaffRentalStatusCourtDTO;
import com.bcb.dto.staff.StaffRentalStatusRawRowDTO;

import java.util.List;

public interface StaffRentalStatusRepository {
    List<StaffRentalStatusCourtDTO> findCourtsByFacility(int facilityId) throws Exception;

    List<StaffRentalStatusRawRowDTO> findRentalStatusRows(int facilityId) throws Exception;

    int updateReturnedStatus(int facilityId, List<Integer> rentalIds, boolean returned) throws Exception;
}
