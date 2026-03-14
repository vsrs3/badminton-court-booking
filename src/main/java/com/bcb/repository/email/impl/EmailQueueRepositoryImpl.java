package com.bcb.repository.email.impl;

import com.bcb.dto.email.EmailQueueItemDTO;
import com.bcb.repository.email.EmailQueueRepository;
import com.bcb.utils.DBContext;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class EmailQueueRepositoryImpl implements EmailQueueRepository {

    @Override
    public void enqueue(String emailType, int bookingId, String toEmail, String payloadJson) throws Exception {
        String sql = "INSERT INTO EmailQueue (email_type, booking_id, to_email, payload_json) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, emailType);
            ps.setInt(2, bookingId);
            ps.setString(3, toEmail);
            if (payloadJson == null) {
                ps.setNull(4, java.sql.Types.NVARCHAR);
            } else {
                ps.setString(4, payloadJson);
            }
            ps.executeUpdate();
        }
    }

    @Override
    public List<EmailQueueItemDTO> findAndMarkPending(int limit) throws Exception {
        String selectSql = "SELECT TOP (?) email_id, email_type, booking_id, to_email, payload_json, retry_count " +
                "FROM EmailQueue " +
                "WHERE status = 'PENDING' AND next_attempt_at <= GETDATE() " +
                "ORDER BY created_at";
        String markSql = "UPDATE EmailQueue SET status = 'SENDING' WHERE email_id = ? AND status = 'PENDING'";

        List<EmailQueueItemDTO> results = new ArrayList<>();
        try (Connection conn = DBContext.getConnection();
             PreparedStatement select = conn.prepareStatement(selectSql);
             PreparedStatement mark = conn.prepareStatement(markSql)) {

            select.setInt(1, limit);
            try (ResultSet rs = select.executeQuery()) {
                while (rs.next()) {
                    int emailId = rs.getInt("email_id");
                    mark.setInt(1, emailId);
                    int updated = mark.executeUpdate();
                    if (updated == 0) {
                        continue;
                    }

                    EmailQueueItemDTO item = new EmailQueueItemDTO();
                    item.setEmailId(emailId);
                    item.setEmailType(rs.getString("email_type"));
                    item.setBookingId(rs.getInt("booking_id"));
                    item.setToEmail(rs.getString("to_email"));
                    item.setPayloadJson(rs.getString("payload_json"));
                    item.setRetryCount(rs.getInt("retry_count"));
                    results.add(item);
                }
            }
        }
        return results;
    }

    @Override
    public void markSent(int emailId) throws Exception {
        String sql = "UPDATE EmailQueue SET status = 'SENT', sent_at = GETDATE() WHERE email_id = ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, emailId);
            ps.executeUpdate();
        }
    }

    @Override
    public void markFailed(int emailId, int retryCount, LocalDateTime nextAttemptAt, String lastError, String status)
            throws Exception {
        String sql = "UPDATE EmailQueue SET status = ?, retry_count = ?, next_attempt_at = ?, last_error = ? " +
                "WHERE email_id = ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, retryCount);
            ps.setTimestamp(3, Timestamp.valueOf(nextAttemptAt));
            if (lastError == null) {
                ps.setNull(4, java.sql.Types.NVARCHAR);
            } else {
                ps.setString(4, lastError);
            }
            ps.setInt(5, emailId);
            ps.executeUpdate();
        }
    }
}
