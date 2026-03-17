package com.bcb.repository.impl;

import com.bcb.model.FacilityInventory;
import com.bcb.repository.FacilityInventoryRepository;
import com.bcb.utils.DBContext;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class FacilityInventoryRepositoryImpl implements FacilityInventoryRepository {

    @Override
    public List<FacilityInventory> findByFacilityId(int facilityId, int limit, int offset, String keyword) {
        List<FacilityInventory> list = new ArrayList<>();

        String sql = """
                SELECT
                    fi.facility_inventory_id,
                    fi.facility_id,
                    fi.inventory_id,
                    fi.total_quantity,
                    fi.available_quantity,
                    f.name AS facility_name,
                    i.name AS inventory_name
                FROM FacilityInventory fi
                JOIN Facility f ON fi.facility_id = f.facility_id
                JOIN Inventory i ON fi.inventory_id = i.inventory_id
                WHERE fi.facility_id = ?
                  AND (? IS NULL OR i.name LIKE ?)
                ORDER BY fi.facility_inventory_id ASC
                OFFSET ? ROWS FETCH NEXT ? ROWS ONLY
                """;

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, facilityId);
            bindKeyword(ps, keyword, 2, 3);
            ps.setInt(4, offset);
            ps.setInt(5, limit);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed to load assigned inventory", e);
        }

        return list;
    }

    @Override
    public int countByFacilityId(int facilityId, String keyword) {
        String sql = """
                SELECT COUNT(*)
                FROM FacilityInventory fi
                JOIN Inventory i ON fi.inventory_id = i.inventory_id
                WHERE fi.facility_id = ?
                  AND (? IS NULL OR i.name LIKE ?)
                """;

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, facilityId);
            bindKeyword(ps, keyword, 2, 3);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed to count assigned inventory", e);
        }

        return 0;
    }

    @Override
    public void assignToFacility(int facilityId, int inventoryId, int totalQuantity, int availableQuantity) {
        String sql = """
                INSERT INTO FacilityInventory (facility_id, inventory_id, total_quantity, available_quantity)
                VALUES (?, ?, ?, ?)
                """;

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, facilityId);
            ps.setInt(2, inventoryId);
            ps.setInt(3, totalQuantity);
            ps.setInt(4, availableQuantity);
            ps.executeUpdate();

        } catch (Exception e) {
            throw new RuntimeException("Failed to assign inventory to facility", e);
        }
    }

    @Override
    public int assignAllToFacility(int facilityId, int totalQuantity, int availableQuantity, String keyword) {
        String sql = """
                INSERT INTO FacilityInventory (facility_id, inventory_id, total_quantity, available_quantity)
                SELECT ?, i.inventory_id, ?, ?
                FROM Inventory i
                WHERE i.is_active = 1
                  AND NOT EXISTS (
                      SELECT 1
                      FROM FacilityInventory fi
                      WHERE fi.facility_id = ?
                        AND fi.inventory_id = i.inventory_id
                  )
                  AND (
                      ? IS NULL
                      OR i.name LIKE ?
                      OR i.brand LIKE ?
                  )
                """;

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, facilityId);
            ps.setInt(2, totalQuantity);
            ps.setInt(3, availableQuantity);
            ps.setInt(4, facilityId);
            bindKeywordForInventory(ps, keyword, 5, 6, 7);
            return ps.executeUpdate();

        } catch (Exception e) {
            throw new RuntimeException("Failed to assign all inventory to facility", e);
        }
    }

    @Override
    public void updateQuantity(int facilityInventoryId, int totalQuantity, int availableQuantity) {
        String sql = """
                UPDATE FacilityInventory
                SET total_quantity = ?, available_quantity = ?
                WHERE facility_inventory_id = ?
                """;

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, totalQuantity);
            ps.setInt(2, availableQuantity);
            ps.setInt(3, facilityInventoryId);
            ps.executeUpdate();

        } catch (Exception e) {
            throw new RuntimeException("Failed to update assigned inventory quantity", e);
        }
    }

    @Override
    public void updateAllQuantitiesByFacility(int facilityId, int totalQuantity, int availableQuantity) {
        String sql = """
                UPDATE FacilityInventory
                SET total_quantity = ?, available_quantity = ?
                WHERE facility_id = ?
                """;

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, totalQuantity);
            ps.setInt(2, availableQuantity);
            ps.setInt(3, facilityId);
            ps.executeUpdate();

        } catch (Exception e) {
            throw new RuntimeException("Failed to update all assigned inventory quantities", e);
        }
    }

    @Override
    public void removeById(int facilityInventoryId) {
        String deleteRentalLog = """
                DELETE FROM RacketRentalLog
                WHERE facility_inventory_id = ?
                """;

        String deleteFacilityInventory = """
                DELETE FROM FacilityInventory
                WHERE facility_inventory_id = ?
                """;

        try (Connection conn = DBContext.getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement ps1 = conn.prepareStatement(deleteRentalLog);
                 PreparedStatement ps2 = conn.prepareStatement(deleteFacilityInventory)) {

                ps1.setInt(1, facilityInventoryId);
                ps1.executeUpdate();

                ps2.setInt(1, facilityInventoryId);
                ps2.executeUpdate();

                conn.commit();
            } catch (Exception e) {
                conn.rollback();
                throw e;
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed to remove assigned inventory", e);
        }
    }

    @Override
    public int removeAllByFacility(int facilityId, String keyword) {
        String deleteRentalLog = """
                DELETE rrl
                FROM RacketRentalLog rrl
                JOIN FacilityInventory fi ON rrl.facility_inventory_id = fi.facility_inventory_id
                JOIN Inventory i ON fi.inventory_id = i.inventory_id
                WHERE fi.facility_id = ?
                  AND (? IS NULL OR i.name LIKE ?)
                """;

        String deleteFacilityInventory = """
                DELETE fi
                FROM FacilityInventory fi
                JOIN Inventory i ON fi.inventory_id = i.inventory_id
                WHERE fi.facility_id = ?
                  AND (? IS NULL OR i.name LIKE ?)
                """;

        try (Connection conn = DBContext.getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement ps1 = conn.prepareStatement(deleteRentalLog);
                 PreparedStatement ps2 = conn.prepareStatement(deleteFacilityInventory)) {

                ps1.setInt(1, facilityId);
                bindKeyword(ps1, keyword, 2, 3);
                ps1.executeUpdate();

                ps2.setInt(1, facilityId);
                bindKeyword(ps2, keyword, 2, 3);
                int removedCount = ps2.executeUpdate();

                conn.commit();
                return removedCount;
            } catch (Exception e) {
                conn.rollback();
                throw e;
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed to remove all assigned inventory", e);
        }
    }

    @Override
    public FacilityInventory findById(int facilityInventoryId) {
        String sql = """
                SELECT
                    fi.facility_inventory_id,
                    fi.facility_id,
                    fi.inventory_id,
                    fi.total_quantity,
                    fi.available_quantity,
                    f.name AS facility_name,
                    i.name AS inventory_name
                FROM FacilityInventory fi
                JOIN Facility f ON fi.facility_id = f.facility_id
                JOIN Inventory i ON fi.inventory_id = i.inventory_id
                WHERE fi.facility_inventory_id = ?
                """;

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, facilityInventoryId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed to find assigned inventory", e);
        }

        return null;
    }

    @Override
    public boolean existsByFacilityAndInventory(int facilityId, int inventoryId) {
        String sql = """
                SELECT 1
                FROM FacilityInventory
                WHERE facility_id = ? AND inventory_id = ?
                """;

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, facilityId);
            ps.setInt(2, inventoryId);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed to check assigned inventory existence", e);
        }
    }

    private void bindKeyword(PreparedStatement ps, String keyword, int exactIndex, int likeIndex) throws SQLException {
        if (keyword == null || keyword.trim().isEmpty()) {
            ps.setNull(exactIndex, Types.VARCHAR);
            ps.setNull(likeIndex, Types.VARCHAR);
            return;
        }

        ps.setString(exactIndex, keyword.trim());
        ps.setString(likeIndex, "%" + keyword.trim() + "%");
    }

    private void bindKeywordForInventory(PreparedStatement ps, String keyword, int exactIndex, int likeNameIndex, int likeBrandIndex) throws SQLException {
        if (keyword == null || keyword.trim().isEmpty()) {
            ps.setNull(exactIndex, Types.VARCHAR);
            ps.setNull(likeNameIndex, Types.VARCHAR);
            ps.setNull(likeBrandIndex, Types.VARCHAR);
            return;
        }

        String trimmedKeyword = keyword.trim();
        String searchValue = "%" + trimmedKeyword + "%";
        ps.setString(exactIndex, trimmedKeyword);
        ps.setString(likeNameIndex, searchValue);
        ps.setString(likeBrandIndex, searchValue);
    }

    private FacilityInventory mapRow(ResultSet rs) throws SQLException {
        FacilityInventory facilityInventory = new FacilityInventory();
        facilityInventory.setFacilityInventoryId(rs.getInt("facility_inventory_id"));
        facilityInventory.setFacilityId(rs.getInt("facility_id"));
        facilityInventory.setInventoryId(rs.getInt("inventory_id"));
        facilityInventory.setTotalQuantity(rs.getInt("total_quantity"));
        facilityInventory.setAvailableQuantity(rs.getInt("available_quantity"));
        facilityInventory.setFacilityName(rs.getString("facility_name"));
        facilityInventory.setInventoryName(rs.getString("inventory_name"));
        return facilityInventory;
    }
}
