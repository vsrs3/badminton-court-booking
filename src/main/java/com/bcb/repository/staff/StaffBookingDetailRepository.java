package com.bcb.repository.staff;

import com.bcb.dto.staff.StaffBookingDetailHeaderDTO;
import com.bcb.dto.staff.StaffBookingDetailInvoiceDTO;
import com.bcb.dto.staff.StaffBookingDetailRentalRowDTO;
import com.bcb.dto.staff.StaffBookingDetailSlotDTO;

import java.sql.Connection;
import java.util.List;

public interface StaffBookingDetailRepository {
    StaffBookingDetailHeaderDTO findBookingHeader(Connection conn, int bookingId) throws Exception;

    List<StaffBookingDetailSlotDTO> findBookingSlots(Connection conn, int bookingId) throws Exception;

    StaffBookingDetailInvoiceDTO findInvoice(Connection conn, int bookingId) throws Exception;

    List<StaffBookingDetailRentalRowDTO> findBookingRentalRows(Connection conn, int bookingId) throws Exception;
}

