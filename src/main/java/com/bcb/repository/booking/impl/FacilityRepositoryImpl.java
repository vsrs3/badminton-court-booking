package com.bcb.repository.booking.impl;

import com.bcb.exception.DataAccessException;
import com.bcb.model.Facility;
import com.bcb.repository.booking.FacilityRepository;
import com.bcb.utils.DBContext;

import java.sql.*;
import java.util.Optional;

/**
 * JDBC implementation of {@link FacilityRepository}.
 *
 * @author AnhTN
 */
public class FacilityRepositoryImpl implements FacilityRepository {

    /** {@inheritDoc} */
    @Override
    public Optional<Facility> findActiveById(int facilityId) {
        String sql = "SELECT facility_id, name, open_time, close_time "
                   + "FROM Facility WHERE facility_id = ? AND is_active = 1";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, facilityId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Facility f = new Facility();
                    f.setFacilityId(rs.getInt("facility_id"));
                    f.setName(rs.getString("name"));
                    f.setOpenTime(rs.getTime("open_time").toLocalTime());
                    f.setCloseTime(rs.getTime("close_time").toLocalTime());
                    return Optional.of(f);
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to find active facility by ID", e);
        }
        return Optional.empty();
    }
}
