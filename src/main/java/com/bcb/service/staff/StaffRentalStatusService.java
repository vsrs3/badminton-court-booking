package com.bcb.service.staff;

import com.bcb.dto.staff.StaffRentalStatusDataDTO;
import com.bcb.dto.staff.StaffRentalStatusUpdateResultDTO;

import java.time.LocalDate;

public interface StaffRentalStatusService {
    StaffRentalStatusDataDTO getRentalStatusData(int facilityId, LocalDate bookingDate) throws Exception;

    StaffRentalStatusUpdateResultDTO updateRentalStatus(int facilityId, int scheduleId, String nextStatus)
            throws Exception;
}
