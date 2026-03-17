package com.bcb.repository.impl;

import com.bcb.model.Inventory;
import com.bcb.repository.InventoryRepository;
import com.bcb.utils.DBContext;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class InventoryRepositoryImpl implements InventoryRepository {

    @Override
    public List<Inventory> findAll() {

        List<Inventory> list = new ArrayList<>();

        String sql = """
                SELECT i.inventory_id,
                       i.name,
                       i.brand,
                       i.description,
                       i.rental_price,
                       i.is_active
                FROM Inventory i
                """;

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapRow(rs));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    @Override
    public List<Inventory> search(String keyword) {

        List<Inventory> list = new ArrayList<>();

        String sql = """
                SELECT i.inventory_id,
                       i.name,
                       i.brand,
                       i.description,
                       i.rental_price,
                       i.is_active
                FROM Inventory i
                WHERE i.name LIKE ? OR i.brand LIKE ?
                """;

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, "%" + keyword + "%");
            ps.setString(2, "%" + keyword + "%");

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    @Override
    public Inventory findById(int id) {

        String sql = """
                SELECT inventory_id,
                       name,
                       brand,
                       description,
                       rental_price,
                       is_active
                FROM Inventory
                WHERE inventory_id = ?
                """;

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public void save(Inventory inventory) {

        String sql = """
                INSERT INTO Inventory
                (name, brand, description, rental_price, is_active)
                VALUES (?, ?, ?, ?, ?)
                """;

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, inventory.getName());
            ps.setString(2, inventory.getBrand());
            ps.setString(3, inventory.getDescription());
            ps.setBigDecimal(4, inventory.getRentalPrice());
            ps.setBoolean(5, inventory.isActive());

            ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void update(Inventory inventory) {

        String sql = """
                UPDATE Inventory
                SET name = ?,
                    brand = ?,
                    description = ?,
                    rental_price = ?,
                    is_active = ?
                WHERE inventory_id = ?
                """;

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, inventory.getName());
            ps.setString(2, inventory.getBrand());
            ps.setString(3, inventory.getDescription());
            ps.setBigDecimal(4, inventory.getRentalPrice());
            ps.setBoolean(5, inventory.isActive());
            ps.setInt(6, inventory.getInventoryId());

            ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void delete(int id) {
        String deleteRacketRentalLog = """
                DELETE rrl
                FROM RacketRentalLog rrl
                INNER JOIN FacilityInventory fi
                    ON rrl.facility_inventory_id = fi.facility_inventory_id
                WHERE fi.inventory_id = ?
                """;

        String deleteRacketRental = """
                DELETE FROM RacketRental
                WHERE inventory_id = ?
                """;

        String deleteFacilityInventory = """
                DELETE FROM FacilityInventory
                WHERE inventory_id = ?
                """;

        String deleteInventory = """
                DELETE FROM Inventory
                WHERE inventory_id = ?
                """;

        try (Connection conn = DBContext.getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement ps1 = conn.prepareStatement(deleteRacketRentalLog);
                 PreparedStatement ps2 = conn.prepareStatement(deleteRacketRental);
                 PreparedStatement ps3 = conn.prepareStatement(deleteFacilityInventory);
                 PreparedStatement ps4 = conn.prepareStatement(deleteInventory)) {

                ps1.setInt(1, id);
                ps1.executeUpdate();

                ps2.setInt(1, id);
                ps2.executeUpdate();

                ps3.setInt(1, id);
                ps3.executeUpdate();

                ps4.setInt(1, id);
                ps4.executeUpdate();

                conn.commit();
            } catch (Exception e) {
                conn.rollback();
                throw e;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Inventory mapRow(ResultSet rs) throws SQLException {

        Inventory i = new Inventory();

        i.setInventoryId(rs.getInt("inventory_id"));
        i.setName(rs.getString("name"));
        i.setBrand(rs.getString("brand"));
        i.setDescription(rs.getString("description"));
        i.setRentalPrice(rs.getBigDecimal("rental_price"));
        i.setActive(rs.getBoolean("is_active"));

        return i;
    }

    @Override
    public List<Inventory> findWithPagination(int limit, int offset, String keyword, Boolean activeStatus, String priceSort) {

        List<Inventory> list = new ArrayList<>();

        StringBuilder sql = new StringBuilder("""
                SELECT inventory_id,
                       name,
                       brand,
                       description,
                       rental_price,
                       is_active
                FROM Inventory
                WHERE 1 = 1
                """);

        List<Object> parameters = new ArrayList<>();
        String normalizedKeyword = normalizeKeyword(keyword);

        if (normalizedKeyword != null) {
            sql.append(" AND name LIKE ? ");
            parameters.add("%" + normalizedKeyword + "%");
        }

        if (activeStatus != null) {
            sql.append(" AND is_active = ? ");
            parameters.add(activeStatus);
        }

        sql.append(resolveInventoryOrderBy(priceSort));
        sql.append(" OFFSET ? ROWS FETCH NEXT ? ROWS ONLY ");
        parameters.add(offset);
        parameters.add(limit);

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            bindParameters(ps, parameters);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    @Override
    public int countInventory(String keyword, Boolean activeStatus) {

        StringBuilder sql = new StringBuilder("""
                SELECT COUNT(*)
                FROM Inventory
                WHERE 1 = 1
                """);

        List<Object> parameters = new ArrayList<>();
        String normalizedKeyword = normalizeKeyword(keyword);

        if (normalizedKeyword != null) {
            sql.append(" AND name LIKE ? ");
            parameters.add("%" + normalizedKeyword + "%");
        }

        if (activeStatus != null) {
            sql.append(" AND is_active = ? ");
            parameters.add(activeStatus);
        }

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            bindParameters(ps, parameters);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;
    }

    @Override
    public List<Inventory> findActiveNotAssignedToFacility(int facilityId, String keyword) {

        List<Inventory> list = new ArrayList<>();

        String sql = """
                SELECT i.inventory_id,
                       i.name,
                       i.brand,
                       i.description,
                       i.rental_price,
                       i.is_active
                FROM Inventory i
                WHERE i.is_active = 1
                  AND NOT EXISTS (
                      SELECT 1
                      FROM FacilityInventory fi
                      WHERE fi.inventory_id = i.inventory_id
                        AND fi.facility_id = ?
                  )
                  AND (
                      ? IS NULL
                      OR i.name LIKE ?
                      OR i.brand LIKE ?
                  )
                ORDER BY i.inventory_id ASC
                """;

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, facilityId);

            if (keyword == null || keyword.trim().isEmpty()) {
                ps.setNull(2, Types.VARCHAR);
                ps.setNull(3, Types.VARCHAR);
                ps.setNull(4, Types.VARCHAR);
            } else {
                String searchValue = "%" + keyword.trim() + "%";
                ps.setString(2, keyword.trim());
                ps.setString(3, searchValue);
                ps.setString(4, searchValue);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    @Override
    public List<Inventory> findActiveNotAssignedToFacilityWithPagination(int facilityId, int limit, int offset, String keyword) {

        List<Inventory> list = new ArrayList<>();

        String sql = """
            SELECT i.inventory_id,
                   i.name,
                   i.brand,
                   i.description,
                   i.rental_price,
                   i.is_active
            FROM Inventory i
            WHERE i.is_active = 1
              AND NOT EXISTS (
                  SELECT 1
                  FROM FacilityInventory fi
                  WHERE fi.inventory_id = i.inventory_id
                    AND fi.facility_id = ?
              )
              AND (
                  ? IS NULL
                  OR i.name LIKE ?
                  OR i.brand LIKE ?
              )
            ORDER BY i.inventory_id ASC
            OFFSET ? ROWS FETCH NEXT ? ROWS ONLY
            """;

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, facilityId);

            if (keyword == null || keyword.trim().isEmpty()) {
                ps.setNull(2, Types.VARCHAR);
                ps.setNull(3, Types.VARCHAR);
                ps.setNull(4, Types.VARCHAR);
            } else {
                String searchValue = "%" + keyword.trim() + "%";
                ps.setString(2, keyword.trim());
                ps.setString(3, searchValue);
                ps.setString(4, searchValue);
            }

            ps.setInt(5, offset);
            ps.setInt(6, limit);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    @Override
    public int countActiveNotAssignedToFacility(int facilityId, String keyword) {

        String sql = """
            SELECT COUNT(*)
            FROM Inventory i
            WHERE i.is_active = 1
              AND NOT EXISTS (
                  SELECT 1
                  FROM FacilityInventory fi
                  WHERE fi.inventory_id = i.inventory_id
                    AND fi.facility_id = ?
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

            if (keyword == null || keyword.trim().isEmpty()) {
                ps.setNull(2, Types.VARCHAR);
                ps.setNull(3, Types.VARCHAR);
                ps.setNull(4, Types.VARCHAR);
            } else {
                String searchValue = "%" + keyword.trim() + "%";
                ps.setString(2, keyword.trim());
                ps.setString(3, searchValue);
                ps.setString(4, searchValue);
            }

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;
    }

    private String normalizeKeyword(String keyword) {
        if (keyword == null) {
            return null;
        }

        String trimmed = keyword.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String resolveInventoryOrderBy(String priceSort) {
        if ("price_desc".equals(priceSort)) {
            return " ORDER BY rental_price DESC, inventory_id ASC ";
        }

        if ("price_asc".equals(priceSort)) {
            return " ORDER BY rental_price ASC, inventory_id ASC ";
        }

        return " ORDER BY inventory_id ASC ";
    }

    private void bindParameters(PreparedStatement ps, List<Object> parameters) throws SQLException {
        for (int index = 0; index < parameters.size(); index++) {
            Object value = parameters.get(index);
            int parameterIndex = index + 1;

            if (value instanceof String stringValue) {
                ps.setString(parameterIndex, stringValue);
                continue;
            }

            if (value instanceof Boolean booleanValue) {
                ps.setBoolean(parameterIndex, booleanValue);
                continue;
            }

            if (value instanceof Integer integerValue) {
                ps.setInt(parameterIndex, integerValue);
            }
        }
    }
}
