package com.bcb.repository.email.impl;

import com.bcb.dto.email.EmailReminderCandidateDTO;
import com.bcb.repository.email.EmailReminderRepository;
import com.bcb.utils.DBContext;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class EmailReminderRepositoryImpl implements EmailReminderRepository {

    @Override
    public List<EmailReminderCandidateDTO> findUpcomingCandidates(LocalDateTime from, LocalDateTime to) throws Exception {
        String sql = "SELECT b.booking_id, COALESCE(a.email, g.email) AS email, " +
                "DATEADD(SECOND, DATEDIFF(SECOND, '00:00:00', MIN(ts.start_time)), CAST(COALESCE(bs.booking_date, b.booking_date) AS DATETIME)) AS start_at " +
                "FROM Booking b " +
                "JOIN BookingSlot bs ON bs.booking_id = b.booking_id AND bs.slot_status <> 'CANCELLED' " +
                "JOIN TimeSlot ts ON ts.slot_id = bs.slot_id " +
                "LEFT JOIN Account a ON b.account_id = a.account_id " +
                "LEFT JOIN Guest g ON b.guest_id = g.guest_id " +
                "WHERE b.staff_id IS NOT NULL " +
                "AND b.booking_status = 'CONFIRMED' " +
                "AND COALESCE(a.email, g.email) IS NOT NULL " +
                "GROUP BY b.booking_id, COALESCE(bs.booking_date, b.booking_date), a.email, g.email " +
                "HAVING DATEADD(SECOND, DATEDIFF(SECOND, '00:00:00', MIN(ts.start_time)), CAST(COALESCE(bs.booking_date, b.booking_date) AS DATETIME)) BETWEEN ? AND ?";

        return queryCandidates(sql, from, to);
    }

    @Override
    public List<EmailReminderCandidateDTO> findUpcomingCustomerCandidates(LocalDateTime from, LocalDateTime to) throws Exception {
        String sql = "SELECT b.booking_id, COALESCE(a.email, g.email) AS email, " +
                "DATEADD(SECOND, DATEDIFF(SECOND, '00:00:00', MIN(ts.start_time)), CAST(COALESCE(bs.booking_date, b.booking_date) AS DATETIME)) AS start_at " +
                "FROM Booking b " +
                "JOIN BookingSlot bs ON bs.booking_id = b.booking_id AND bs.slot_status <> 'CANCELLED' " +
                "JOIN TimeSlot ts ON ts.slot_id = bs.slot_id " +
                "LEFT JOIN Account a ON b.account_id = a.account_id " +
                "LEFT JOIN Guest g ON b.guest_id = g.guest_id " +
                "WHERE b.staff_id IS NULL " +
                "AND b.booking_status = 'CONFIRMED' " +
                "AND COALESCE(a.email, g.email) IS NOT NULL " +
                "GROUP BY b.booking_id, COALESCE(bs.booking_date, b.booking_date), a.email, g.email " +
                "HAVING DATEADD(SECOND, DATEDIFF(SECOND, '00:00:00', MIN(ts.start_time)), CAST(COALESCE(bs.booking_date, b.booking_date) AS DATETIME)) BETWEEN ? AND ?";

        return queryCandidates(sql, from, to);
    }

    @Override
    public List<EmailReminderCandidateDTO> findPaymentCandidates(LocalDateTime from, LocalDateTime to) throws Exception {
        String sql = "SELECT b.booking_id, COALESCE(a.email, g.email) AS email, " +
                "DATEADD(SECOND, DATEDIFF(SECOND, '00:00:00', MIN(ts.start_time)), CAST(COALESCE(bs.booking_date, b.booking_date) AS DATETIME)) AS start_at " +
                "FROM Booking b " +
                "JOIN BookingSlot bs ON bs.booking_id = b.booking_id AND bs.slot_status <> 'CANCELLED' " +
                "JOIN TimeSlot ts ON ts.slot_id = bs.slot_id " +
                "JOIN Invoice i ON i.booking_id = b.booking_id " +
                "LEFT JOIN Account a ON b.account_id = a.account_id " +
                "LEFT JOIN Guest g ON b.guest_id = g.guest_id " +
                "WHERE b.staff_id IS NOT NULL " +
                "AND b.booking_status = 'CONFIRMED' " +
                "AND i.payment_status IN ('UNPAID','PARTIAL') " +
                "AND COALESCE(a.email, g.email) IS NOT NULL " +
                "GROUP BY b.booking_id, COALESCE(bs.booking_date, b.booking_date), a.email, g.email " +
                "HAVING DATEADD(SECOND, DATEDIFF(SECOND, '00:00:00', MIN(ts.start_time)), CAST(COALESCE(bs.booking_date, b.booking_date) AS DATETIME)) BETWEEN ? AND ?";

        return queryCandidates(sql, from, to);
    }

    private List<EmailReminderCandidateDTO> queryCandidates(String sql, LocalDateTime from, LocalDateTime to) throws Exception {
        List<EmailReminderCandidateDTO> results = new ArrayList<>();
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(from));
            ps.setTimestamp(2, Timestamp.valueOf(to));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    EmailReminderCandidateDTO dto = new EmailReminderCandidateDTO();
                    dto.setBookingId(rs.getInt("booking_id"));
                    dto.setToEmail(rs.getString("email"));
                    Timestamp ts = rs.getTimestamp("start_at");
                    dto.setStartAt(ts != null ? ts.toLocalDateTime() : null);
                    results.add(dto);
                }
            }
        }
        return results;
    }
}


