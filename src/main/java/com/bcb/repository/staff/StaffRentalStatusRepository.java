package com.bcb.repository.staff;

import com.bcb.dto.staff.StaffRentalInventoryStockDTO;
import com.bcb.dto.staff.StaffRentalStatusCourtDTO;
import com.bcb.dto.staff.StaffRentalStatusRawRowDTO;

import java.time.LocalDate;
import java.util.List;

public interface StaffRentalStatusRepository {
    List<StaffRentalStatusCourtDTO> findCourtsByFacility(int facilityId) throws Exception;

    List<StaffRentalStatusRawRowDTO> findRentalStatusRows(int facilityId, LocalDate bookingDate) throws Exception;

    List<StaffRentalInventoryStockDTO> findInventoryStocks(int facilityId) throws Exception;

    int updateScheduleStatus(int facilityId, int scheduleId, String nextStatus) throws Exception;
}
