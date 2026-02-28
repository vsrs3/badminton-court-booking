package com.bcb.repository.booking.impl;

import com.bcb.dto.singlebooking.SingleBookingMatrixTimeSlotDTO;
import com.bcb.exception.DataAccessException;
import com.bcb.repository.booking.TimeSlotRepository;
import com.bcb.utils.DBContext;

import java.sql.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC implementation of {@link TimeSlotRepository}.
 *
 * @author AnhTN
 */
public class TimeSlotRepositoryImpl implements TimeSlotRepository {

    private static final DateTimeFormatter TF = DateTimeFormatter.ofPattern("HH:mm");

    /** {@inheritDoc} */
    @Override
    public List<SingleBookingMatrixTimeSlotDTO> findByTimeRange(LocalTime openTime, LocalTime closeTime) {
        String sql = "SELECT slot_id, start_time, end_time FROM TimeSlot "
                + "WHERE start_time >= CAST(? AS TIME) AND end_time <= CAST(? AS TIME) ORDER BY start_time";
        List<SingleBookingMatrixTimeSlotDTO> list = new ArrayList<>();
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            // Dùng setString với format HH:mm:ss để tránh type mismatch
            ps.setString(1, openTime.format(DateTimeFormatter.ofPattern("HH:mm:ss")));
            ps.setString(2, closeTime.format(DateTimeFormatter.ofPattern("HH:mm:ss")));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    SingleBookingMatrixTimeSlotDTO dto = new SingleBookingMatrixTimeSlotDTO();
                    dto.setSlotId(rs.getInt("slot_id"));
                    String st = rs.getString("start_time");
                    String et = rs.getString("end_time");
                    if (st == null || et == null) continue;
                    // Cắt lấy HH:mm từ "HH:mm:ss.0000000"
                    dto.setStartTime(st.substring(0, 5));
                    dto.setEndTime(et.substring(0, 5));
                    list.add(dto);
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to find time slots by range | Cause: " + e.getMessage(), e);
        }
        return list;
    }

}
