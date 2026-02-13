package com.bcb.repository.impl;

import com.bcb.dto.TimeSlotPriceDTO;
import com.bcb.exception.DataAccessException;
import com.bcb.repository.FacilityPriceRuleRepository;
import com.bcb.utils.DBContext;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FacilityPriceRuleRepositoryImpl implements FacilityPriceRuleRepository {

    @Override
    public List<TimeSlotPriceDTO> findTimeSlotPrices(int facilityId, int courtTypeId, String dayType) {
        String sql = "SELECT ts.slot_id, ts.start_time, ts.end_time, fpr.price " +
                     "FROM TimeSlot ts " +
                     "LEFT JOIN FacilityPriceRule fpr ON ts.slot_id = fpr.slot_id " +
                     "AND fpr.facility_id = ? AND fpr.court_type_id = ? AND fpr.day_type = ? " +
                     "ORDER BY ts.start_time";

        List<TimeSlotPriceDTO> list = new ArrayList<>();
        try (Connection conn = DBContext.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, facilityId);
            pstmt.setInt(2, courtTypeId);
            pstmt.setString(3, dayType);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    TimeSlotPriceDTO dto = new TimeSlotPriceDTO();
                    dto.setSlotId(rs.getInt("slot_id"));
                    dto.setStartTime(rs.getTime("start_time").toLocalTime());
                    dto.setEndTime(rs.getTime("end_time").toLocalTime());
                    BigDecimal price = rs.getBigDecimal("price");
                    dto.setPrice(rs.wasNull() ? null : price);
                    list.add(dto);
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to fetch time slot prices", e);
        }
        return list;
    }

    @Override
    public void upsertPrice(int facilityId, int courtTypeId, String dayType, int slotId, BigDecimal price) {
        String sql = "MERGE INTO FacilityPriceRule AS target " +
                     "USING (SELECT ? AS facility_id, ? AS court_type_id, ? AS day_type, ? AS slot_id) AS source " +
                     "ON target.facility_id = source.facility_id " +
                     "AND target.court_type_id = source.court_type_id " +
                     "AND target.day_type = source.day_type " +
                     "AND target.slot_id = source.slot_id " +
                     "WHEN MATCHED THEN " +
                     "    UPDATE SET price = ? " +
                     "WHEN NOT MATCHED THEN " +
                     "    INSERT (facility_id, court_type_id, day_type, slot_id, price) " +
                     "    VALUES (source.facility_id, source.court_type_id, source.day_type, source.slot_id, ?);";

        try (Connection conn = DBContext.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, facilityId);
            pstmt.setInt(2, courtTypeId);
            pstmt.setString(3, dayType);
            pstmt.setInt(4, slotId);
            pstmt.setBigDecimal(5, price);
            pstmt.setBigDecimal(6, price);

            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to upsert price", e);
        }
    }

    @Override
    public void bulkUpsertPrices(int facilityId, int courtTypeId, String dayType, List<Integer> slotIds, BigDecimal price) {
        String sql = "MERGE INTO FacilityPriceRule AS target " +
                     "USING (SELECT ? AS facility_id, ? AS court_type_id, ? AS day_type, ? AS slot_id) AS source " +
                     "ON target.facility_id = source.facility_id " +
                     "AND target.court_type_id = source.court_type_id " +
                     "AND target.day_type = source.day_type " +
                     "AND target.slot_id = source.slot_id " +
                     "WHEN MATCHED THEN " +
                     "    UPDATE SET price = ? " +
                     "WHEN NOT MATCHED THEN " +
                     "    INSERT (facility_id, court_type_id, day_type, slot_id, price) " +
                     "    VALUES (source.facility_id, source.court_type_id, source.day_type, source.slot_id, ?);";

        try (Connection conn = DBContext.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                for (Integer slotId : slotIds) {
                    pstmt.setInt(1, facilityId);
                    pstmt.setInt(2, courtTypeId);
                    pstmt.setString(3, dayType);
                    pstmt.setInt(4, slotId);
                    pstmt.setBigDecimal(5, price);
                    pstmt.setBigDecimal(6, price);
                    pstmt.addBatch();
                }
                pstmt.executeBatch();
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to bulk upsert prices", e);
        }
    }
}
