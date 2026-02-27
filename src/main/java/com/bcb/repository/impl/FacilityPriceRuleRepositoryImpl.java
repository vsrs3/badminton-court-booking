package com.bcb.repository.impl;

import com.bcb.exception.DataAccessException;
import com.bcb.model.FacilityPriceRule;
import com.bcb.repository.FacilityPriceRuleRepository;
import com.bcb.utils.DBContext;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class FacilityPriceRuleRepositoryImpl implements FacilityPriceRuleRepository {

    @Override
    public List<FacilityPriceRule> findByFacilityAndCourtTypeAndDayType(int facilityId, int courtTypeId, String dayType) {
        String sql = "SELECT price_id, facility_id, court_type_id, day_type, start_time, end_time, price " +
                     "FROM FacilityPriceRule " +
                     "WHERE facility_id = ? AND court_type_id = ? AND day_type = ? " +
                     "ORDER BY start_time";

        List<FacilityPriceRule> list = new ArrayList<>();
        try (Connection conn = DBContext.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, facilityId);
            pstmt.setInt(2, courtTypeId);
            pstmt.setString(3, dayType);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    FacilityPriceRule rule = new FacilityPriceRule();
                    rule.setPriceId(rs.getInt("price_id"));
                    rule.setFacilityId(rs.getInt("facility_id"));
                    rule.setCourtTypeId(rs.getInt("court_type_id"));
                    rule.setDayType(rs.getString("day_type"));
                    rule.setStartTime(rs.getTime("start_time").toLocalTime());
                    rule.setEndTime(rs.getTime("end_time").toLocalTime());
                    rule.setPrice(rs.getBigDecimal("price"));
                    list.add(rule);
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to fetch price rules", e);
        }
        return list;
    }

    @Override
    public Optional<FacilityPriceRule> findById(int priceId) {
        String sql = "SELECT price_id, facility_id, court_type_id, day_type, start_time, end_time, price " +
                     "FROM FacilityPriceRule WHERE price_id = ?";

        try (Connection conn = DBContext.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, priceId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    FacilityPriceRule rule = new FacilityPriceRule();
                    rule.setPriceId(rs.getInt("price_id"));
                    rule.setFacilityId(rs.getInt("facility_id"));
                    rule.setCourtTypeId(rs.getInt("court_type_id"));
                    rule.setDayType(rs.getString("day_type"));
                    rule.setStartTime(rs.getTime("start_time").toLocalTime());
                    rule.setEndTime(rs.getTime("end_time").toLocalTime());
                    rule.setPrice(rs.getBigDecimal("price"));
                    return Optional.of(rule);
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to find price rule", e);
        }
        return Optional.empty();
    }

    @Override
    public boolean hasOverlap(int facilityId, int courtTypeId, String dayType,
                              LocalTime startTime, LocalTime endTime, Integer excludePriceId) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT COUNT(*) FROM FacilityPriceRule ");
        sql.append("WHERE facility_id = ? AND court_type_id = ? AND day_type = ? ");
        sql.append("AND start_time < ? AND end_time > ?");

        if (excludePriceId != null) {
            sql.append(" AND price_id != ?");
        }

        try (Connection conn = DBContext.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {

            pstmt.setInt(1, facilityId);
            pstmt.setInt(2, courtTypeId);
            pstmt.setString(3, dayType);
//            pstmt.setTime(4, Time.valueOf(endTime));
//            pstmt.setTime(5, Time.valueOf(startTime));
            pstmt.setString(4, endTime.toString());
            pstmt.setString(5, startTime.toString());

            if (excludePriceId != null) {
                pstmt.setInt(6, excludePriceId);
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to check overlap", e);
        }
        return false;
    }

    @Override
    public void insert(FacilityPriceRule priceRule) {
        String sql = "INSERT INTO FacilityPriceRule (facility_id, court_type_id, day_type, start_time, end_time, price) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBContext.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, priceRule.getFacilityId());
            pstmt.setInt(2, priceRule.getCourtTypeId());
            pstmt.setString(3, priceRule.getDayType());
            pstmt.setTime(4, Time.valueOf(priceRule.getStartTime()));
            pstmt.setTime(5, Time.valueOf(priceRule.getEndTime()));
            pstmt.setBigDecimal(6, priceRule.getPrice());

            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to insert price rule", e);
        }
    }

    @Override
    public void update(FacilityPriceRule priceRule) {
        String sql = "UPDATE FacilityPriceRule " +
                     "SET facility_id = ?, court_type_id = ?, day_type = ?, start_time = ?, end_time = ?, price = ? " +
                     "WHERE price_id = ?";

        try (Connection conn = DBContext.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, priceRule.getFacilityId());
            pstmt.setInt(2, priceRule.getCourtTypeId());
            pstmt.setString(3, priceRule.getDayType());
            pstmt.setTime(4, Time.valueOf(priceRule.getStartTime()));
            pstmt.setTime(5, Time.valueOf(priceRule.getEndTime()));
            pstmt.setBigDecimal(6, priceRule.getPrice());
            pstmt.setInt(7, priceRule.getPriceId());

            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to update price rule", e);
        }
    }

    @Override
    public void delete(int priceId) {
        String sql = "DELETE FROM FacilityPriceRule WHERE price_id = ?";

        try (Connection conn = DBContext.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, priceId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to delete price rule", e);
        }
    }
}
