package com.bcb.service.staff;

public interface StaffRentalService {
    String getInventoryJson(int facilityId, String keyword, int page, int pageSize) throws Exception;
    String saveRental(String body, int facilityId, int staffId) throws Exception;
    String updateRental(String body, int facilityId, int staffId) throws Exception;
    String deleteRental(String body, int facilityId, int staffId) throws Exception;
    String getRentalSummaryJson(int bookingId, int facilityId) throws Exception;
}