package com.bcb.service.staff;

public interface StaffBookingRentalService {
    String updateSessionRentalStatus(int bookingId, int sessionIndex, String nextStatus, int facilityId) throws Exception;
}
