package com.bcb.repository.notification.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.bcb.dto.notilication.NotificationDTO;
import com.bcb.exception.DataAccessException;
import com.bcb.model.Notification;
import com.bcb.repository.notification.NotificationRepository;
import com.bcb.utils.DBContext;

public class NotificationRepositoryImpl implements NotificationRepository{
	
	 @Override
	    public void insertNotification(Integer acountId, String title, String content) {
	        String sql = "INSERT INTO Notification (account_id, title, content, type, is_sent) "
	                   + "VALUES (?, ?, ?, 'SYSTEM', 1)";

	        try (Connection conn = DBContext.getConnection();
	             PreparedStatement ps = conn.prepareStatement(sql)) {

	            ps.setInt(1, acountId);
	            ps.setNString(2, title);
	            ps.setNString(3, content);
	            ps.executeUpdate();

	        } catch (SQLException e) {
	            throw new DataAccessException("Thất bại khi thêm thông báo", e);
	        }
	    }
}
