package com.bcb.repository.staff;

import com.bcb.dto.staff.InventoryRentalScheduleSaveItemDTO;
import com.bcb.dto.staff.StaffRentalInventoryItemDTO;

import java.sql.Connection;
import java.time.LocalDate;
import java.util.List;

public interface StaffRentalScheduleRepository {
    List<StaffRentalInventoryItemDTO> findRentalItemsForSlot(
            int facilityId, LocalDate bookingDate, int courtId, int slotId, String keyword, String priceSort, int page, int pageSize)
            throws Exception;

    int countRentalItems(int facilityId, String keyword) throws Exception;

    List<StaffRentalInventoryItemDTO> findSelectedItemsForSlot(
            int facilityId, LocalDate bookingDate, int courtId, int slotId) throws Exception;

    void replaceRentalSchedule(
            Connection conn,
            int facilityId,
            LocalDate bookingDate,
            int courtId,
            int slotId,
            List<InventoryRentalScheduleSaveItemDTO> items) throws Exception;
}
