package com.bcb.repository.staff;

import com.bcb.dto.staff.StaffBookingEditExistingSlotDto;
import com.bcb.dto.staff.StaffBookingEditSessionCellDto;
import com.bcb.dto.staff.StaffBookingEditSlotStateDto;
import com.bcb.dto.staff.StaffBookingEditStatusCountDto;

import java.math.BigDecimal;
import java.sql.Connection;
import java.time.LocalDate;
import java.util.List;

public interface StaffBookingEditRepository {
    List<Integer> findPendingSlotIds(Connection conn, int bookingId) throws Exception;

    List<StaffBookingEditSessionCellDto> findSessionCellsByBookingId(Connection conn, int bookingId) throws Exception;

    StaffBookingEditSessionCellDto findSessionCellBySlotId(Connection conn, int courtId, int slotId) throws Exception;

    int cancelPendingSlot(Connection conn, int bookingId, int bookingSlotId) throws Exception;

    void deleteCourtSlotBooking(Connection conn, int bookingSlotId) throws Exception;

    StaffBookingEditExistingSlotDto findExistingSlot(Connection conn, int bookingId, int courtId, int slotId) throws Exception;

    BigDecimal lookupCurrentPrice(Connection conn, int facilityId, LocalDate bookingDate, int courtId, int slotId) throws Exception;

    void reopenCancelledSlot(Connection conn, int bookingSlotId, BigDecimal price) throws Exception;

    int insertPendingSlot(Connection conn, int bookingId, int courtId, int slotId, BigDecimal price) throws Exception;

    void insertCourtSlotBooking(Connection conn, int courtId, LocalDate bookingDate, int slotId, int bookingSlotId) throws Exception;

    BigDecimal sumActiveAmount(Connection conn, int bookingId) throws Exception;

    BigDecimal findPaidAmount(Connection conn, int bookingId) throws Exception;

    void updateInvoiceAfterRecalc(Connection conn, int bookingId, BigDecimal totalAmount, BigDecimal refundDue,
                                  String refundStatus, String refundNote) throws Exception;

    List<StaffBookingEditStatusCountDto> findSlotStatusCounts(Connection conn, int bookingId) throws Exception;

    void updateBookingStatus(Connection conn, int bookingId, String status) throws Exception;

    boolean existsSlotStatus(Connection conn, int bookingId, String slotStatus) throws Exception;

    StaffBookingEditSlotStateDto findSlotState(Connection conn, int bookingId, int bookingSlotId) throws Exception;

    void markSlotReleased(Connection conn, int bookingSlotId) throws Exception;

    void insertAuditLog(Connection conn, int bookingId, int staffId,
                        String changeAction, String changeType,
                        String reason, String beforeEtag, String afterEtag,
                        String beforeJson, String afterJson,
                        BigDecimal refundDue) throws Exception;
}

