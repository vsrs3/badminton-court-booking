package com.bcb.repository.staff;

import com.bcb.dto.staff.StaffCheckinBookingDto;
import com.bcb.dto.staff.StaffCheckinSessionSlotRowDto;

import java.sql.Connection;
import java.sql.Timestamp;
import java.util.List;

public interface StaffCheckinRepository {
    StaffCheckinBookingDto findBooking(Connection conn, int bookingId) throws Exception;

    String findInvoicePaymentStatus(Connection conn, int bookingId) throws Exception;

    List<StaffCheckinSessionSlotRowDto> findSessionSlotRows(Connection conn, int bookingId) throws Exception;

    List<String> findSlotStatuses(Connection conn, List<Integer> slotIds) throws Exception;

    void updateSlotsCheckedIn(Connection conn, List<Integer> slotIds, Timestamp checkinTime) throws Exception;

    void updateSlotsCheckedOut(Connection conn, List<Integer> slotIds, Timestamp checkoutTime) throws Exception;

    void updateSlotsNoShow(Connection conn, List<Integer> slotIds) throws Exception;

    void updateBookingStatus(Connection conn, int bookingId, String status) throws Exception;
}
