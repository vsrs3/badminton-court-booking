package com.bcb.repository.staff;

import com.bcb.dto.staff.StaffBookingDetailHeaderDto;
import com.bcb.dto.staff.StaffBookingDetailInvoiceDto;
import com.bcb.dto.staff.StaffBookingDetailSlotDto;

import java.sql.Connection;
import java.util.List;

public interface StaffBookingDetailRepository {
    StaffBookingDetailHeaderDto findBookingHeader(Connection conn, int bookingId) throws Exception;

    List<StaffBookingDetailSlotDto> findBookingSlots(Connection conn, int bookingId) throws Exception;

    StaffBookingDetailInvoiceDto findInvoice(Connection conn, int bookingId) throws Exception;
}
