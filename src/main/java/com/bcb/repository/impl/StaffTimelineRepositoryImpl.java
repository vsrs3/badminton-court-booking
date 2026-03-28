package com.bcb.repository.impl;

import com.bcb.dto.staff.StaffTimelineBookedCellDTO;
import com.bcb.dto.staff.StaffTimelineCourtDTO;
import com.bcb.dto.staff.StaffTimelineDisabledCellDTO;
import com.bcb.dto.staff.StaffTimelineFacilityDTO;
import com.bcb.dto.staff.StaffTimelineSlotDTO;
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
    public StaffTimelineFacilityDTO findFacilityInfo(int facilityId) throws Exception {
        try (Connection conn = DBContext.getConnection()) {
            return findFacilityInfo(conn, facilityId);
        }
    }

    public StaffTimelineFacilityDTO findFacilityInfo(Connection conn, int facilityId) throws Exception {
        StaffTimelineFacilityDTO facility = new StaffTimelineFacilityDTO();
        String sql = "SELECT name, open_time, close_time FROM Facility WHERE facility_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
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
    public List<StaffTimelineCourtDTO> findActiveCourts(int facilityId) throws Exception {
        String sql = "SELECT court_id, court_name FROM Court WHERE facility_id = ? AND is_active = 1 ORDER BY court_name";
        List<StaffTimelineCourtDTO> courts = new ArrayList<>();

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, facilityId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    StaffTimelineCourtDTO court = new StaffTimelineCourtDTO();
                    court.setCourtId(rs.getInt("court_id"));
                    court.setCourtName(rs.getString("court_name"));
                    courts.add(court);
                }
            }
        }

        return courts;
    }

    @Override
    public List<StaffTimelineSlotDTO> findSlotsWithinHours(String openTime, String closeTime) throws Exception {
        try (Connection conn = DBContext.getConnection()) {
            return findSlotsWithinHours(conn, openTime, closeTime);
        }
    }

    public List<StaffTimelineSlotDTO> findSlotsWithinHours(Connection conn, String openTime, String closeTime)
            throws Exception {
        String sql = "SELECT slot_id, start_time, end_time FROM TimeSlot " +
                "WHERE start_time >= CAST(? AS TIME) AND end_time <= CAST(? AS TIME) ORDER BY start_time";
        List<StaffTimelineSlotDTO> slots = new ArrayList<>();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, openTime);
            ps.setString(2, closeTime);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    StaffTimelineSlotDTO slot = new StaffTimelineSlotDTO();
                    slot.setSlotId(rs.getInt("slot_id"));
                    slot.setStartTime(rs.getTime("start_time").toLocalTime().toString().substring(0, 5));
                    slot.setEndTime(rs.getTime("end_time").toLocalTime().toString().substring(0, 5));
                    slots.add(slot);
                }
            }
        }

        return slots;
    }

    /**
     * Fetches booked cells for a given facility and date to render in the daily timeline.
     */
    @Override
    public List<StaffTimelineBookedCellDTO> findBookedCells(int facilityId, LocalDate bookingDate) throws Exception {
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

        List<StaffTimelineBookedCellDTO> cells = new ArrayList<>();

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, facilityId);
            ps.setDate(2, Date.valueOf(bookingDate));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    StaffTimelineBookedCellDTO cell = new StaffTimelineBookedCellDTO();
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

    /**
     * Fetches blocked/exception cells that should appear as DISABLED in the daily timeline.
     */
    @Override
    public List<StaffTimelineDisabledCellDTO> findDisabledCells(int facilityId, LocalDate bookingDate,
                                                                String openTime, String closeTime) throws Exception {
        String sql = """
                SELECT cse.exception_id, cse.court_id, ts.slot_id, cse.reason
                FROM CourtScheduleException cse
                CROSS JOIN TimeSlot ts
                WHERE cse.facility_id = ? AND cse.is_active = 1
                  AND ? BETWEEN cse.start_date AND cse.end_date
                  AND ts.start_time >= CAST(? AS TIME) AND ts.end_time <= CAST(? AS TIME)
                  AND ts.start_time >= COALESCE(cse.start_time, CAST(? AS TIME))
                  AND ts.end_time   <= COALESCE(cse.end_time,   CAST(? AS TIME))
                """;

        List<StaffTimelineDisabledCellDTO> cells = new ArrayList<>();

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
                    StaffTimelineDisabledCellDTO cell = new StaffTimelineDisabledCellDTO();
                    cell.setExceptionId(rs.getInt("exception_id"));
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

