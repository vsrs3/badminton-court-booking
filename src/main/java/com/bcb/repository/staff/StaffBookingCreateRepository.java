package com.bcb.repository.staff;

import com.bcb.dto.staff.StaffBookingCreateSlotDTO;
import com.bcb.dto.staff.StaffCustomerAccountDTO;

import java.math.BigDecimal;
import java.sql.Connection;
import java.time.LocalTime;
import java.util.Map;

public interface StaffBookingCreateRepository {
    StaffCustomerAccountDTO findActiveCustomerByPhone(String phone) throws Exception;

    Integer insertGuest(Connection conn, String guestName, String guestPhone) throws Exception;

    int insertBookingForAccount(Connection conn, int facilityId, java.time.LocalDate bookingDate,
                                int accountId, int staffId) throws Exception;

    int insertBookingForGuest(Connection conn, int facilityId, java.time.LocalDate bookingDate,
                              int guestId, int staffId) throws Exception;

    Map<String, BigDecimal> loadPrices(Connection conn, int facilityId, String dayType) throws Exception;

    Map<Integer, Integer> loadCourtTypes(Connection conn, int facilityId) throws Exception;

    Map<Integer, LocalTime[]> loadSlotTimes(Connection conn) throws Exception;

    Map<Integer, Integer> loadSlotOrder(Connection conn) throws Exception;

    int insertBookingSlot(Connection conn, int bookingId, StaffBookingCreateSlotDTO slot, BigDecimal price) throws Exception;

    void insertCourtSlotBooking(Connection conn, StaffBookingCreateSlotDTO slot,
                                java.time.LocalDate bookingDate, int bookingSlotId) throws Exception;

    void insertInvoice(Connection conn, int bookingId, BigDecimal totalAmount) throws Exception;

    void insertBookingRental(Connection conn, int bookingSlotId,
                             int inventoryId, int quantity, BigDecimal unitPrice,
                             String addedBy) throws Exception;

    void updateInventoryRentalScheduleStatus(Connection conn, int facilityId, java.time.LocalDate bookingDate,
                                             int courtId, int slotId, int inventoryId, String status) throws Exception;

}

