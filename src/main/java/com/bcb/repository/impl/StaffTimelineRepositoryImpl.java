package com.bcb.repository.impl;

import com.bcb.dto.staff.StaffTimelineBookedCellDto;
import com.bcb.dto.staff.StaffTimelineCourtDto;
import com.bcb.dto.staff.StaffTimelineDisabledCellDto;
import com.bcb.dto.staff.StaffTimelineFacilityDto;
import com.bcb.dto.staff.StaffTimelineSlotDto;
import com.bcb.repository.staff.StaffTimelineRepository;
import com.bcb.utils.DBContext;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Time;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class StaffTimelineRepositoryImpl implements StaffTimelineRepository {

    @Override
    public StaffTimelineFacilityDto findFacilityInfo(int facilityId) throws Exception {
        StaffTimelineFacilityDto facility = new StaffTimelineFacilityDto();
        String sql = "SELECT name, open_time, close_time FROM Facility WHERE facility_id = ?";

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, facilityId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    facility.setFacilityName(rs.getString("name"));
                    Time openTime = rs.getTime("open_time");
                    Time closeTime = rs.getTime("close_time");
                    facility.setOpenTime(openTime != null ? openTime.toLocalTime().toString() : null);
                    facility.setCloseTime(closeTime != null ? closeTime.toLocalTime().toString() : null);
                }
            }
        }

        return facility;
    }

    @Override
    public List<StaffTimelineCourtDto> findActiveCourts(int facilityId) throws Exception {
        String sql = "SELECT court_id, court_name FROM Court WHERE facility_id = ? AND is_active = 1 ORDER BY court_name";
        List<StaffTimelineCourtDto> courts = new ArrayList<>();

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, facilityId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    StaffTimelineCourtDto court = new StaffTimelineCourtDto();
                    court.setCourtId(rs.getInt("court_id"));
                    court.setCourtName(rs.getString("court_name"));
                    courts.add(court);
                }
            }
        }

        return courts;
    }

    @Override
    public List<StaffTimelineSlotDto> findSlotsWithinHours(String openTime, String closeTime) throws Exception {
        String sql = "SELECT slot_id, start_time, end_time FROM TimeSlot " +
                "WHERE start_time >= CAST(? AS TIME) AND end_time <= CAST(? AS TIME) ORDER BY start_time";
        List<StaffTimelineSlotDto> slots = new ArrayList<>();

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, openTime);
            ps.setString(2, closeTime);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    StaffTimelineSlotDto slot = new StaffTimelineSlotDto();
                    slot.setSlotId(rs.getInt("slot_id"));
                    slot.setStartTime(rs.getTime("start_time").toLocalTime().toString().substring(0, 5));
                    slot.setEndTime(rs.getTime("end_time").toLocalTime().toString().substring(0, 5));
                    slots.add(slot);
                }
            }
        }

        return slots;
    }

    @Override
    public List<StaffTimelineBookedCellDto> findBookedCells(int facilityId, LocalDate bookingDate) throws Exception {
        String sql = """
                SELECT csb.court_id, csb.slot_id, b.booking_id, b.booking_status,
                       bs.slot_status,
                       COALESCE(a.full_name, g.guest_name) AS customer_name
                FROM CourtSlotBooking csb
                JOIN BookingSlot bs ON csb.booking_slot_id = bs.booking_slot_id
                JOIN Booking b      ON bs.booking_id = b.booking_id
                LEFT JOIN Account a ON b.account_id = a.account_id
                LEFT JOIN Guest g   ON b.guest_id = g.guest_id
                JOIN Court c        ON csb.court_id = c.court_id
                WHERE c.facility_id = ? AND csb.booking_date = ?
                  AND b.booking_status NOT IN ('EXPIRED', 'CANCELLED')
                """;

        List<StaffTimelineBookedCellDto> cells = new ArrayList<>();

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, facilityId);
            ps.setDate(2, Date.valueOf(bookingDate));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    StaffTimelineBookedCellDto cell = new StaffTimelineBookedCellDto();
                    cell.setCourtId(rs.getInt("court_id"));
                    cell.setSlotId(rs.getInt("slot_id"));
                    cell.setBookingId(rs.getInt("booking_id"));
                    cell.setBookingStatus(rs.getString("booking_status"));
                    cell.setCustomerName(rs.getString("customer_name"));
                    cell.setSlotStatus(rs.getString("slot_status"));
                    cells.add(cell);
                }
            }
        }

        return cells;
    }

    @Override
    public List<StaffTimelineDisabledCellDto> findDisabledCells(int facilityId, LocalDate bookingDate,
                                                                 String openTime, String closeTime) throws Exception {
        String sql = """
                SELECT cse.court_id, ts.slot_id, cse.reason
                FROM CourtScheduleException cse
                CROSS JOIN TimeSlot ts
                WHERE cse.facility_id = ? AND cse.is_active = 1
                  AND ? BETWEEN cse.start_date AND cse.end_date
                  AND ts.start_time >= CAST(? AS TIME) AND ts.end_time <= CAST(? AS TIME)
                  AND ts.start_time >= COALESCE(cse.start_time, CAST(? AS TIME))
                  AND ts.end_time   <= COALESCE(cse.end_time,   CAST(? AS TIME))
                """;

        List<StaffTimelineDisabledCellDto> cells = new ArrayList<>();

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, facilityId);
            ps.setDate(2, Date.valueOf(bookingDate));
            ps.setString(3, openTime);
            ps.setString(4, closeTime);
            ps.setString(5, openTime);
            ps.setString(6, closeTime);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    StaffTimelineDisabledCellDto cell = new StaffTimelineDisabledCellDto();
                    cell.setCourtId(rs.getInt("court_id"));
                    cell.setSlotId(rs.getInt("slot_id"));
                    String reason = rs.getString("reason");
                    cell.setReason(reason != null ? reason : "Bảo trì");
                    cells.add(cell);
                }
            }
        }

        return cells;
    }
}

