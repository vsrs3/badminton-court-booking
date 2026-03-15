package com.bcb.repository.staff;

import com.bcb.dto.staff.StaffCustomerAccountDTO;

import java.math.BigDecimal;
import java.sql.Connection;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

public interface StaffRecurringBookingRepository {
    StaffCustomerAccountDTO findActiveCustomerByPhone(String phone) throws Exception;

    Integer insertGuest(Connection conn, String guestName, String guestPhone, String guestEmail) throws Exception;

    int insertBookingRoot(Connection conn, int facilityId, Integer accountId, Integer guestId, int staffId) throws Exception;

    int insertRecurringBooking(Connection conn, int facilityId, LocalDate startDate, LocalDate endDate) throws Exception;

    void updateBookingRecurringId(Connection conn, int bookingId, int recurringId) throws Exception;

    void updateBookingStatus(Connection conn, int bookingId, String status) throws Exception;

    void insertRecurringPattern(Connection conn, int recurringId, int courtId, int dayOfWeek, int slotId) throws Exception;

    int insertBookingSlot(Connection conn, int bookingId, int courtId, LocalDate bookingDate, int slotId, BigDecimal price) throws Exception;

    void insertCourtSlotBooking(Connection conn, int courtId, LocalDate bookingDate, int slotId, int bookingSlotId) throws Exception;

    void insertBookingSkip(Connection conn, int recurringId, LocalDate skipDate, String reason) throws Exception;

    int insertInvoice(Connection conn, int bookingId, BigDecimal totalAmount) throws Exception;

    Map<String, BigDecimal> loadPrices(Connection conn, int facilityId, String dayType) throws Exception;

    Map<Integer, Integer> loadCourtTypes(Connection conn, int facilityId) throws Exception;

    Map<Integer, LocalTime[]> loadSlotTimes(Connection conn) throws Exception;

    Map<Integer, Integer> loadSlotOrder(Connection conn) throws Exception;

    List<Integer> loadCourtsByType(Connection conn, int facilityId, int courtTypeId) throws Exception;

    Map<Integer, List<Integer>> findBookedSlots(Connection conn, int facilityId, LocalDate bookingDate) throws Exception;
}
