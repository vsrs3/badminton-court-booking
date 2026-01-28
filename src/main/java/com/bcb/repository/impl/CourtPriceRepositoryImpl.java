package com.bcb.repository.impl;

import com.bcb.exception.DataAccessException;
import com.bcb.model.CourtPrice;
import com.bcb.repository.CourtPriceRepository;
import com.bcb.utils.DBContext;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JDBC implementation of CourtPriceRepository.
 * Handles all database operations for CourtPrice entity.
 */
public class CourtPriceRepositoryImpl implements CourtPriceRepository {

    @Override
    public List<CourtPrice> findByCourtId(int courtId) {
        String sql = "SELECT price_id, court_id, start_time, end_time, price_per_hour " +
                     "FROM CourtPrice WHERE court_id = ? ORDER BY start_time";

        List<CourtPrice> prices = new ArrayList<>();

        try (Connection conn = DBContext.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, courtId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    prices.add(mapResultSetToCourtPrice(rs));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to find prices by court", e);
        } catch (Exception e) {
            throw new DataAccessException("Failed to find prices by court", e);
        }

        return prices;
    }

    @Override
    public Optional<CourtPrice> findById(int priceId) {
        String sql = "SELECT price_id, court_id, start_time, end_time, price_per_hour " +
                     "FROM CourtPrice WHERE price_id = ?";

        try (Connection conn = DBContext.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, priceId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToCourtPrice(rs));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to find price by ID", e);
        }
    }

    @Override
    public Optional<CourtPrice> findByIdAndCourt(int priceId, int courtId) {
        String sql = "SELECT price_id, court_id, start_time, end_time, price_per_hour " +
                     "FROM CourtPrice WHERE price_id = ? AND court_id = ?";

        try (Connection conn = DBContext.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, priceId);
            pstmt.setInt(2, courtId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToCourtPrice(rs));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to find price by ID and court", e);
        }
    }

    @Override
    public int insert(CourtPrice courtPrice) {
        String sql = "INSERT INTO CourtPrice (court_id, start_time, end_time, price_per_hour) " +
                     "VALUES (?, ?, ?, ?)";

        try (Connection conn = DBContext.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, courtPrice.getCourtId());
            pstmt.setString(2, courtPrice.getStartTime().toString());
            pstmt.setString(3, courtPrice.getEndTime().toString());
            pstmt.setBigDecimal(4, courtPrice.getPricePerHour());

            pstmt.executeUpdate();

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                }
            }
            throw new DataAccessException("Failed to insert court price: No ID generated");
        } catch (DataAccessException e) {
            throw e;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to insert court price", e);
        }
    }

    @Override
    public int update(CourtPrice courtPrice) {
        String sql = "UPDATE CourtPrice SET court_id = ?, start_time = ?, end_time = ?, " +
                     "price_per_hour = ? WHERE price_id = ?";

        try (Connection conn = DBContext.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, courtPrice.getCourtId());
            pstmt.setString(2, courtPrice.getStartTime().toString());
            pstmt.setString(3, courtPrice.getEndTime().toString());
            pstmt.setBigDecimal(4, courtPrice.getPricePerHour());
            pstmt.setInt(5, courtPrice.getPriceId());

            return pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to update court price", e);
        }
    }

    @Override
    public int delete(int priceId) {
        String sql = "DELETE FROM CourtPrice WHERE price_id = ?";

        try (Connection conn = DBContext.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, priceId);
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to delete court price", e);
        }
    }

    @Override
    public int deleteByCourtId(int courtId) {
        String sql = "DELETE FROM CourtPrice WHERE court_id = ?";

        try (Connection conn = DBContext.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, courtId);
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to delete prices by court", e);
        }
    }

    @Override
    public boolean hasOverlappingTime(int courtId, int priceId, String startTime, String endTime) {
        String sql = "SELECT COUNT(*) FROM CourtPrice " +
                     "WHERE court_id = ? AND price_id != ? " +
                     "AND ((start_time < ? AND end_time > ?) " +
                     "OR (start_time < ? AND end_time > ?))";

        try (Connection conn = DBContext.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, courtId);
            pstmt.setInt(2, priceId);
            pstmt.setString(3, endTime);
            pstmt.setString(4, startTime);
            pstmt.setString(5, endTime);
            pstmt.setString(6, startTime);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to check overlapping time", e);
        }

        return false;
    }

    @Override
    public int countByCourtId(int courtId) {
        String sql = "SELECT COUNT(*) FROM CourtPrice WHERE court_id = ?";

        try (Connection conn = DBContext.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, courtId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to count prices by court", e);
        }

        return 0;
    }

    /**
     * Maps ResultSet row to CourtPrice object.
     */
    private CourtPrice mapResultSetToCourtPrice(ResultSet rs) throws SQLException {
        return new CourtPrice(
            rs.getInt("price_id"),
            rs.getInt("court_id"),
            LocalTime.parse(rs.getTime("start_time").toString()),
            LocalTime.parse(rs.getTime("end_time").toString()),
            rs.getBigDecimal("price_per_hour")
        );
    }
}
