package com.bcb.repository.staff;

import com.bcb.dto.staff.StaffRentalInventoryItemDTO;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.List;
import java.util.Map;

public interface StaffRentalRepository {

    List<StaffRentalInventoryItemDTO> findRentalItems(
            int facilityId, String keyword, int page, int pageSize) throws Exception;

    int countRentalItems(int facilityId, String keyword) throws Exception;

    boolean existsRacketRental(int bookingSlotId, int inventoryId) throws Exception;

    void insertRacketRental(
            int bookingSlotId,
            int inventoryId,
            int quantity,
            BigDecimal unitPrice,
            int addedBy) throws Exception;

    void updateRacketRental(
            int bookingSlotId,
            int inventoryId,
            int quantity) throws Exception;

    void deleteRacketRental(
            int bookingSlotId,
            int inventoryId) throws Exception;

    BigDecimal getBookingRentalTotal(int bookingId) throws Exception;

    List<Map<String, Object>> getBookingRentalRows(int bookingId) throws Exception;

    void insertRentalLogAndDecreaseStock(int bookingId, int facilityId, int staffId) throws Exception;

    void insertRentalLogAndDecreaseStock(Connection conn, int bookingId, int facilityId, int staffId) throws Exception;
}