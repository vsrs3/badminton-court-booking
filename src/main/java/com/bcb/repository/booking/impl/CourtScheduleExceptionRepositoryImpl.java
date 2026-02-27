package com.bcb.repository.booking.impl;

import com.bcb.exception.DataAccessException;
import com.bcb.model.CourtScheduleException;
import com.bcb.repository.booking.CourtScheduleExceptionRepository;
import com.bcb.utils.DBContext;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC implementation of {@link CourtScheduleExceptionRepository}.
 *
 * @author AnhTN
 */
public class CourtScheduleExceptionRepositoryImpl implements CourtScheduleExceptionRepository {

    /** {@inheritDoc} */
    @Override
    public List<CourtScheduleException> findActiveByFacilityAndDate(int facilityId, LocalDate date) {
        String sql = "SELECT exception_id, court_id, facility_id, start_date, end_date, "
                   + "start_time, end_time, exception_type, reason "
                   + "FROM CourtScheduleException "
                   + "WHERE facility_id = ? AND is_active = 1 "
                   + "AND start_date <= ? AND end_date >= ?";
        List<CourtScheduleException> list = new ArrayList<>();
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, facilityId);
            ps.setDate(2, Date.valueOf(date));
            ps.setDate(3, Date.valueOf(date));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    CourtScheduleException ex = new CourtScheduleException();
                    ex.setExceptionId(rs.getInt("exception_id"));
                    ex.setCourtId(rs.getInt("court_id"));
                    ex.setFacilityId(rs.getInt("facility_id"));
                    ex.setStartDate(rs.getDate("start_date").toLocalDate());
                    ex.setEndDate(rs.getDate("end_date").toLocalDate());
                    Time st = rs.getTime("start_time");
                    ex.setStartTime(st != null ? st.toLocalTime() : null);
                    Time et = rs.getTime("end_time");
                    ex.setEndTime(et != null ? et.toLocalTime() : null);
                    ex.setExceptionType(rs.getString("exception_type"));
                    ex.setReason(rs.getString("reason"));
                    list.add(ex);
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to find court schedule exceptions", e);
        }
        return list;
    }
}
