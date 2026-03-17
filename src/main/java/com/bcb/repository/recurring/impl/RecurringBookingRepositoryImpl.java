package com.bcb.repository.recurring.impl;

import com.bcb.exception.DataAccessException;
import com.bcb.model.RecurringBooking;
import com.bcb.repository.recurring.RecurringBookingRepository;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * JDBC implementation of {@link RecurringBookingRepository}.
 *
 * @author AnhTN
 */
public class RecurringBookingRepositoryImpl implements RecurringBookingRepository {

    @Override
    public int insert(Connection conn, RecurringBooking recurringBooking) {
        String sql = "INSERT INTO RecurringBooking (facility_id, start_date, end_date, status) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, recurringBooking.getFacilityId());
            ps.setDate(2, Date.valueOf(recurringBooking.getStartDate()));
            ps.setDate(3, Date.valueOf(recurringBooking.getEndDate()));
            ps.setString(4, recurringBooking.getStatus() == null ? "ACTIVE" : recurringBooking.getStatus());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
            throw new DataAccessException("Failed to insert recurring booking: No ID generated");
        } catch (SQLException e) {
            throw new DataAccessException("Failed to insert recurring booking", e);
        }
    }
}

