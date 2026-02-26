package com.bcb.repository.booking.impl;

import com.bcb.exception.DataAccessException;
import com.bcb.model.Court;
import com.bcb.repository.booking.CourtRepository;
import com.bcb.utils.DBContext;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC implementation of {@link CourtRepository}.
 *
 * @author AnhTN
 */
public class CourtRepositoryImpl implements CourtRepository {

    /** {@inheritDoc} */
    @Override
    public List<Court> findActiveByFacilityId(int facilityId) {
        String sql = "SELECT court_id, facility_id, court_type_id, court_name "
                   + "FROM Court WHERE facility_id = ? AND is_active = 1 ORDER BY court_id";
        List<Court> list = new ArrayList<>();
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, facilityId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Court c = new Court();
                    c.setCourtId(rs.getInt("court_id"));
                    c.setFacilityId(rs.getInt("facility_id"));
                    c.setCourtTypeId(rs.getInt("court_type_id"));
                    c.setCourtName(rs.getString("court_name"));
                    list.add(c);
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to find active courts by facility", e);
        }
        return list;
    }
}
