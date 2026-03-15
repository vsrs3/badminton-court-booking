package com.bcb.repository.impl;

import com.bcb.model.CourtScheduleException;
import com.bcb.repository.staff.StaffScheduleExceptionRepository;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalTime;

public class StaffScheduleExceptionRepositoryImpl implements StaffScheduleExceptionRepository {

    @Override
    public LocalTime[] findSlotTime(Connection conn, int slotId) throws Exception {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT start_time, end_time FROM TimeSlot WHERE slot_id = ?")) {
            ps.setInt(1, slotId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                LocalTime start = rs.getTime("start_time").toLocalTime();
                LocalTime end = rs.getTime("end_time").toLocalTime();
                return new LocalTime[]{start, end};
            }
        }
    }

    @Override
    public LocalTime[] findTimeSlotBounds(Connection conn) throws Exception {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT MIN(start_time) AS min_start, MAX(end_time) AS max_end FROM TimeSlot")) {
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                LocalTime start = rs.getTime("min_start").toLocalTime();
                LocalTime end = rs.getTime("max_end").toLocalTime();
                return new LocalTime[]{start, end};
            }
        }
    }

    @Override
    public boolean hasBooking(Connection conn, int facilityId, int courtId, LocalDate date, int slotId) throws Exception {
        String sql = """
                SELECT TOP 1 1
                FROM CourtSlotBooking csb
                JOIN Court c ON csb.court_id = c.court_id
                WHERE c.facility_id = ? AND csb.court_id = ? AND csb.slot_id = ? AND csb.booking_date = ?
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, facilityId);
            ps.setInt(2, courtId);
            ps.setInt(3, slotId);
            ps.setDate(4, Date.valueOf(date));
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    @Override
    public boolean hasActiveExceptionOverlap(Connection conn, int facilityId, int courtId, LocalDate date,
                                             LocalTime start, LocalTime end) throws Exception {
        String sql = """
                SELECT TOP 1 1
                FROM CourtScheduleException
                WHERE facility_id = ? AND court_id = ? AND is_active = 1
                  AND ? BETWEEN start_date AND end_date
                  AND (
                        (start_time IS NULL AND end_time IS NULL)
                        OR (start_time IS NOT NULL AND end_time IS NOT NULL
                            AND CAST(? AS TIME) < CAST(end_time AS TIME)
                            AND CAST(? AS TIME) > CAST(start_time AS TIME))
                      )
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, facilityId);
            ps.setInt(2, courtId);
            ps.setDate(3, Date.valueOf(date));
            ps.setTime(4, java.sql.Time.valueOf(start));
            ps.setTime(5, java.sql.Time.valueOf(end));
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    @Override
    public CourtScheduleException findActiveExceptionById(Connection conn, int facilityId, int exceptionId) throws Exception {
        String sql = """
                SELECT exception_id, court_id, facility_id, start_date, end_date, start_time, end_time,
                       exception_type, reason, created_by, created_at, updated_at, is_active
                FROM CourtScheduleException
                WHERE exception_id = ? AND facility_id = ? AND is_active = 1
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, exceptionId);
            ps.setInt(2, facilityId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                CourtScheduleException ex = new CourtScheduleException();
                ex.setExceptionId(rs.getInt("exception_id"));
                ex.setCourtId(rs.getInt("court_id"));
                ex.setFacilityId(rs.getInt("facility_id"));
                ex.setStartDate(rs.getDate("start_date").toLocalDate());
                ex.setEndDate(rs.getDate("end_date").toLocalDate());
                java.sql.Time st = rs.getTime("start_time");
                java.sql.Time et = rs.getTime("end_time");
                ex.setStartTime(st != null ? st.toLocalTime() : null);
                ex.setEndTime(et != null ? et.toLocalTime() : null);
                ex.setExceptionType(rs.getString("exception_type"));
                ex.setReason(rs.getString("reason"));
                ex.setCreatedBy((Integer) rs.getObject("created_by"));
                ex.setCreatedAt(rs.getTimestamp("created_at") != null
                        ? rs.getTimestamp("created_at").toLocalDateTime() : null);
                ex.setUpdatedAt(rs.getTimestamp("updated_at") != null
                        ? rs.getTimestamp("updated_at").toLocalDateTime() : null);
                ex.setIsActive(rs.getBoolean("is_active"));
                return ex;
            }
        }
    }

    @Override
    public int insertException(Connection conn, CourtScheduleException ex) throws Exception {
        String sql = """
                INSERT INTO CourtScheduleException
                (court_id, facility_id, start_date, end_date, start_time, end_time,
                 exception_type, reason, created_by, is_active)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, 1)
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, ex.getCourtId());
            ps.setInt(2, ex.getFacilityId());
            ps.setDate(3, Date.valueOf(ex.getStartDate()));
            ps.setDate(4, Date.valueOf(ex.getEndDate()));
            if (ex.getStartTime() == null) ps.setNull(5, java.sql.Types.TIME);
            else ps.setTime(5, java.sql.Time.valueOf(ex.getStartTime()));
            if (ex.getEndTime() == null) ps.setNull(6, java.sql.Types.TIME);
            else ps.setTime(6, java.sql.Time.valueOf(ex.getEndTime()));
            ps.setString(7, ex.getExceptionType());
            if (ex.getReason() == null) ps.setNull(8, java.sql.Types.NVARCHAR);
            else ps.setNString(8, ex.getReason());
            if (ex.getCreatedBy() == null) ps.setNull(9, java.sql.Types.INTEGER);
            else ps.setInt(9, ex.getCreatedBy());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
                return 0;
            }
        }
    }

    @Override
    public void deactivateException(Connection conn, int exceptionId) throws Exception {
        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE CourtScheduleException SET is_active = 0, updated_at = GETDATE() WHERE exception_id = ?")) {
            ps.setInt(1, exceptionId);
            ps.executeUpdate();
        }
    }
}
