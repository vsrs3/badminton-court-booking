package com.bcb.repository.impl;

import com.bcb.exception.DataAccessException;
import com.bcb.model.CourtType;
import com.bcb.repository.CourtTypeRepository;
import com.bcb.utils.DBContext;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CourtTypeRepositoryImpl implements CourtTypeRepository {

    @Override
    public List<CourtType> findAll() {
        String sql = "SELECT * FROM CourtType";
        List<CourtType> types = new ArrayList<>();
        try (Connection conn = DBContext.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                types.add(mapResultSetToCourtType(rs));
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to find all court types", e);
        }
        return types;
    }

    @Override
    public Optional<CourtType> findById(int courtTypeId) {
        String sql = "SELECT * FROM CourtType WHERE court_type_id = ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, courtTypeId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToCourtType(rs));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to find court type by ID", e);
        }
        return Optional.empty();
    }

    private CourtType mapResultSetToCourtType(ResultSet rs) throws SQLException {
        CourtType courtType = new CourtType();

        courtType.setCourtTypeId(rs.getObject("court_type_id", Integer.class));
        courtType.setTypeCode(rs.getString("type_code"));
        courtType.setDescription(rs.getString("description"));

        return courtType;
    }
}
