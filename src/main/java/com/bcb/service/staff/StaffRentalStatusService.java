package com.bcb.service.staff;

import com.bcb.dto.staff.StaffRentalStatusDataDTO;
import com.bcb.dto.staff.StaffRentalStatusUpdateResultDTO;

import java.util.List;

public interface StaffRentalStatusService {
    StaffRentalStatusDataDTO getRentalStatusData(int facilityId) throws Exception;

    StaffRentalStatusUpdateResultDTO updateRentalStatus(int facilityId, List<Integer> rentalIds, boolean returned)
            throws Exception;
}
