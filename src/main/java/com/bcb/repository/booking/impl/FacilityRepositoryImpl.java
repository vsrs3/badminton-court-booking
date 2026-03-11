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
        String sql = "SELECT facility_id, name, province, district, ward, address, open_time, close_time "
                   + "FROM Facility WHERE facility_id = ? AND is_active = 1";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, facilityId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Facility f = new Facility();
                    f.setFacilityId(rs.getInt("facility_id"));
                    f.setName(rs.getString("name"));
                    f.setProvince(rs.getString("province"));
                    f.setDistrict(rs.getString("district"));
                    f.setWard(rs.getString("ward"));
                    f.setAddress(rs.getString("address"));
                    java.sql.Time openT  = rs.getTime("open_time");
                    java.sql.Time closeT = rs.getTime("close_time");
                    f.setOpenTime(openT  != null ? openT.toLocalTime()  : java.time.LocalTime.of(6, 0));
                    f.setCloseTime(closeT != null ? closeT.toLocalTime() : java.time.LocalTime.of(22, 0));
                    return Optional.of(f);
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to find active facility by ID", e);
        }
        return Optional.empty();
    }
}

