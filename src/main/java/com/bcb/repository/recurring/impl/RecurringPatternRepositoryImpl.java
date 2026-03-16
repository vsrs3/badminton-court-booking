package com.bcb.repository.recurring.impl;

import com.bcb.exception.DataAccessException;
import com.bcb.repository.recurring.RecurringPatternRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * JDBC implementation of {@link RecurringPatternRepository}.
 *
 * @author AnhTN
 */
public class RecurringPatternRepositoryImpl implements RecurringPatternRepository {

    @Override
    public void insert(Connection conn, int recurringId, int courtId, int dayOfWeek, int slotId) {
        String sql = "INSERT INTO RecurringPattern (recurring_id, court_id, day_of_week, slot_id) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, recurringId);
            ps.setInt(2, courtId);
            ps.setInt(3, dayOfWeek);
            ps.setInt(4, slotId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to insert recurring pattern", e);
        }
    }
}

