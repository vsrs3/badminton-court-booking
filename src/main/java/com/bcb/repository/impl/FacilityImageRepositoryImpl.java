package com.bcb.repository.impl;

import com.bcb.exception.DataAccessException;
import com.bcb.model.FacilityImage;
import com.bcb.repository.FacilityImageRepository;
import com.bcb.utils.DBContext;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JDBC implementation of FacilityImageRepository.
 * Handles all database operations for FacilityImage entity.
 */
public class FacilityImageRepositoryImpl implements FacilityImageRepository {

    @Override
    public List<FacilityImage> findByFacility(int facilityId) {
        String sql = "SELECT image_id, facility_id, image_path, is_thumbnail, created_at " +
                     "FROM FacilityImage WHERE facility_id = ? ORDER BY is_thumbnail DESC, created_at DESC";

        List<FacilityImage> images = new ArrayList<>();

        try (Connection conn = DBContext.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, facilityId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    images.add(mapResultSetToFacilityImage(rs));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to find images by facility", e);
        }

        return images;
    }

    @Override
    public Optional<FacilityImage> findThumbnail(int facilityId) {
        String sql = "SELECT image_id, facility_id, image_path, is_thumbnail, created_at " +
                     "FROM FacilityImage WHERE facility_id = ? AND is_thumbnail = 1";

        try (Connection conn = DBContext.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, facilityId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToFacilityImage(rs));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to find thumbnail", e);
        }

        return Optional.empty();
    }

    @Override
    public List<FacilityImage> findGallery(int facilityId) {
        String sql = "SELECT image_id, facility_id, image_path, is_thumbnail, created_at " +
                     "FROM FacilityImage WHERE facility_id = ? AND is_thumbnail = 0 " +
                     "ORDER BY created_at DESC";

        List<FacilityImage> images = new ArrayList<>();

        try (Connection conn = DBContext.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, facilityId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    images.add(mapResultSetToFacilityImage(rs));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to find gallery images", e);
        }

        return images;
    }

    @Override
    public Optional<FacilityImage> findById(int imageId) {
        String sql = "SELECT image_id, facility_id, image_path, is_thumbnail, created_at " +
                     "FROM FacilityImage WHERE image_id = ?";

        try (Connection conn = DBContext.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, imageId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToFacilityImage(rs));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to find image by ID", e);
        }

        return Optional.empty();
    }

    @Override
    public int insert(FacilityImage image) {
        String sql = "INSERT INTO FacilityImage (facility_id, image_path, is_thumbnail) VALUES (?, ?, ?)";

        try (Connection conn = DBContext.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, image.getFacilityId());
            pstmt.setString(2, image.getImagePath());
            pstmt.setBoolean(3, image.isThumbnail());

            pstmt.executeUpdate();

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to insert image", e);
        }

        throw new DataAccessException("Failed to insert image: No ID generated");
    }

    @Override
    public int update(FacilityImage image) {
        String sql = "UPDATE FacilityImage SET image_path = ?, is_thumbnail = ? WHERE image_id = ?";

        try (Connection conn = DBContext.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, image.getImagePath());
            pstmt.setBoolean(2, image.isThumbnail());
            pstmt.setInt(3, image.getImageId());

            return pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to update image", e);
        }
    }

    @Override
    public int delete(int imageId) {
        String sql = "DELETE FROM FacilityImage WHERE image_id = ?";

        try (Connection conn = DBContext.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, imageId);
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to delete image", e);
        }
    }

    @Override
    public int deleteByFacility(int facilityId) {
        String sql = "DELETE FROM FacilityImage WHERE facility_id = ?";

        try (Connection conn = DBContext.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, facilityId);
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to delete images by facility", e);
        }
    }

    @Override
    public int setThumbnail(int imageId, boolean isThumbnail) {
        String sql = "UPDATE FacilityImage SET is_thumbnail = ? WHERE image_id = ?";

        try (Connection conn = DBContext.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setBoolean(1, isThumbnail);
            pstmt.setInt(2, imageId);

            return pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to set thumbnail", e);
        }
    }

    @Override
    public int clearThumbnails(int facilityId) {
        String sql = "UPDATE FacilityImage SET is_thumbnail = 0 WHERE facility_id = ?";

        try (Connection conn = DBContext.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, facilityId);
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to clear thumbnails", e);
        }
    }

    /**
     * Maps ResultSet row to FacilityImage object.
     */
    private FacilityImage mapResultSetToFacilityImage(ResultSet rs) throws SQLException {
        return new FacilityImage(
            rs.getInt("image_id"),
            rs.getInt("facility_id"),
            rs.getString("image_path"),
            rs.getBoolean("is_thumbnail"),
            rs.getTimestamp("created_at").toLocalDateTime()
        );
    }
}
