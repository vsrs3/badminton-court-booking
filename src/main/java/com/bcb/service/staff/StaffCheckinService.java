package com.bcb.service.staff;

public interface StaffCheckinService {
    String doCheckin(int bookingId, int sessionIndex, int facilityId) throws Exception;

    String doCheckout(int bookingId, int sessionIndex, int facilityId) throws Exception;

    String doNoShow(int bookingId, int sessionIndex, int facilityId) throws Exception;
}
