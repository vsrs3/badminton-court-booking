package com.bcb.repository.recurring.impl;

import com.bcb.exception.DataAccessException;
import com.bcb.repository.recurring.BookingSkipRepository;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;

/**
 * JDBC implementation of {@link BookingSkipRepository}.
 *
 * @author AnhTN
 */
public class BookingSkipRepositoryImpl implements BookingSkipRepository {

    @Override
    public void insert(Connection conn, int recurringId, LocalDate skipDate, String reason) {
        String sql = "INSERT INTO BookingSkip (recurring_id, skip_date, reason) VALUES (?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, recurringId);
            ps.setDate(2, Date.valueOf(skipDate));
            ps.setString(3, reason);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to insert booking skip", e);
        }
    }
}

