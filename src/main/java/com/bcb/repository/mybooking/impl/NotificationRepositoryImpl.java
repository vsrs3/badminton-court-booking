package com.bcb.repository.mybooking.impl;

import com.bcb.exception.DataAccessException;
import com.bcb.model.Notification;
import com.bcb.repository.mybooking.NotificationRepository;
import com.bcb.utils.DBContext;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * JDBC implementation of {@link NotificationRepository}.
 */
public class NotificationRepositoryImpl implements NotificationRepository {

    /** {@inheritDoc} */
    @Override
    public void insertNotification(Notification notification) {
        String sql = "INSERT INTO Notification (account_id, title, content, type, is_sent) "
                   + "VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, notification.getAccountId());
            ps.setNString(2, notification.getTitle());
            ps.setNString(3, notification.getContent());
            ps.setString(4, notification.getType());
            ps.setBoolean(5, notification.getIsSent() != null ? notification.getIsSent() : false);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new DataAccessException("Failed to insert notification", e);
        }
    }
}

