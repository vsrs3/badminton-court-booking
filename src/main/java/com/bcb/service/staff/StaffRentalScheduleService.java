package com.bcb.service.staff;

public interface StaffRentalScheduleService {
    String getSlotInventoryJson(int facilityId, String bookingDate, int courtId, int slotId, String keyword, int page, int pageSize)
            throws Exception;

    String saveSlotRentalSchedule(String body, int facilityId) throws Exception;
}
