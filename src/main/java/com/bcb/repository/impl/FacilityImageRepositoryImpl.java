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
    public FacilityImage findThumbnail(int facilityId) {
        String sql = "SELECT image_id, facility_id, image_path, is_thumbnail, created_at " +
                "FROM FacilityImage WHERE facility_id = ? AND is_thumbnail = 1";

        try (Connection conn = DBContext.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, facilityId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToFacilityImage(rs);
                }
            }

        } catch (SQLException e) {
            throw new DataAccessException("Failed to find thumbnail for facility " + facilityId, e);
        }

        return null;
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
    public FacilityImage findById(int imageId) {
        String sql = "SELECT image_id, facility_id, image_path, is_thumbnail, created_at FROM FacilityImage WHERE image_id = ?";

        try (Connection conn = DBContext.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, imageId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToFacilityImage(rs);
                }
            }

        } catch (SQLException e) {
            throw new DataAccessException(
                    "Failed to find FacilityImage with id=" + imageId, e
            );
        }

        return null;
    }

    @Override
    public int insert(FacilityImage image) {
        String sql = "INSERT INTO FacilityImage (facility_id, image_path, is_thumbnail) VALUES (?, ?, ?)";

        try (Connection conn = DBContext.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, image.getFacilityId());
            pstmt.setString(2, image.getImagePath());
            pstmt.setBoolean(3, image.getIsThumbnail());

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
            pstmt.setBoolean(2, image.getIsThumbnail());
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


    /**
     * Maps ResultSet row to FacilityImage object.
     */
    private FacilityImage mapResultSetToFacilityImage(ResultSet rs) throws SQLException {
        FacilityImage image = new FacilityImage();

        image.setImageId(rs.getObject("image_id", Integer.class));
        image.setFacilityId(rs.getObject("facility_id", Integer.class));
        image.setImagePath(rs.getString("image_path"));
        image.setIsThumbnail(rs.getObject("is_thumbnail", Boolean.class));

        Timestamp createdAt = rs.getTimestamp("created_at");
        image.setCreatedAt(createdAt != null ? createdAt.toLocalDateTime() : null);

        return image;
    }
}
