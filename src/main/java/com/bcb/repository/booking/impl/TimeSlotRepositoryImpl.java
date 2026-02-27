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
                   + "WHERE start_time >= ? AND end_time <= ? ORDER BY start_time";
        List<SingleBookingMatrixTimeSlotDTO> list = new ArrayList<>();
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setTime(1, Time.valueOf(openTime));
            ps.setTime(2, Time.valueOf(closeTime));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    SingleBookingMatrixTimeSlotDTO dto = new SingleBookingMatrixTimeSlotDTO();
                    dto.setSlotId(rs.getInt("slot_id"));
                    dto.setStartTime(rs.getTime("start_time").toLocalTime().format(TF));
                    dto.setEndTime(rs.getTime("end_time").toLocalTime().format(TF));
                    list.add(dto);
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to find time slots by range", e);
        }
        return list;
    }
}
