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
                       i.is_active,
                       i.facility_id,
                       f.name AS facility_name
                FROM Inventory i
                LEFT JOIN Facility f
                ON i.facility_id = f.facility_id
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
                       i.is_active,
                       i.facility_id,
                       f.name AS facility_name
                FROM Inventory i
                LEFT JOIN Facility f
                ON i.facility_id = f.facility_id
                WHERE i.name LIKE ? OR i.brand LIKE ?
                """;

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, "%" + keyword + "%");
            ps.setString(2, "%" + keyword + "%");

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                list.add(mapRow(rs));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }
    
    @Override
    public Inventory findById(int id) {

        String sql = """
            SELECT i.inventory_id,
                   i.name,
                   i.brand,
                   i.description,
                   i.rental_price,
                   i.is_active,
                   i.facility_id,
                   f.name AS facility_name
            FROM Inventory i
            LEFT JOIN Facility f
            ON i.facility_id = f.facility_id
            WHERE i.inventory_id = ?
            """;

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {

                Inventory i = new Inventory();

                i.setInventoryId(rs.getInt("inventory_id"));
                i.setName(rs.getString("name"));
                i.setBrand(rs.getString("brand"));
                i.setDescription(rs.getString("description"));
                i.setRentalPrice(rs.getBigDecimal("rental_price"));
                i.setActive(rs.getBoolean("is_active"));
                i.setFacilityId((Integer) rs.getObject("facility_id"));
                i.setFacilityName(rs.getString("facility_name"));

                return i;
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
                (name, brand, description, rental_price, is_active, facility_id)
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, inventory.getName());
            ps.setString(2, inventory.getBrand());
            ps.setString(3, inventory.getDescription());
            ps.setBigDecimal(4, inventory.getRentalPrice());
            ps.setBoolean(5, inventory.isActive());
            ps.setObject(6, inventory.getFacilityId(), Types.INTEGER);

            ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void update(Inventory inventory) {

        String sql = """
                UPDATE Inventory
                SET name=?,
                    brand=?,
                    description=?,
                    rental_price=?,
                    is_active=?,
                    facility_id=?
                WHERE inventory_id=?
                """;

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, inventory.getName());
            ps.setString(2, inventory.getBrand());
            ps.setString(3, inventory.getDescription());
            ps.setBigDecimal(4, inventory.getRentalPrice());
            ps.setBoolean(5, inventory.isActive());
            ps.setObject(6, inventory.getFacilityId(), Types.INTEGER);
            ps.setInt(7, inventory.getInventoryId());

            ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void delete(int id) {

        String sql = "DELETE FROM Inventory WHERE inventory_id=?";

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<Inventory> findByFacility(int facilityId, int limit, int offset, String keyword) {

        List<Inventory> list = new ArrayList<>();

        String sql = """
                SELECT i.inventory_id,
                       i.name,
                       i.brand,
                       i.description,
                       i.rental_price,
                       i.is_active,
                       i.facility_id,
                       f.name AS facility_name
                FROM Inventory i
                JOIN Facility f
                ON i.facility_id = f.facility_id
                WHERE i.facility_id = ?
                """ +
                (keyword != null && !keyword.isBlank()
                        ? " AND (i.name LIKE ? OR i.brand LIKE ?) "
                        : "") +
                " ORDER BY i.inventory_id OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            int index = 1;
            ps.setInt(index++, facilityId);

            if (keyword != null && !keyword.isBlank()) {
                ps.setString(index++, "%" + keyword + "%");
                ps.setString(index++, "%" + keyword + "%");
            }
   
            ps.setInt(index++, offset);
            ps.setInt(index, limit);
            
            
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                list.add(mapRow(rs));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    @Override
    public int countByFacility(int facilityId, String keyword) {

        String sql = """
                SELECT COUNT(*)
                FROM Inventory
                WHERE facility_id = ?
                """ +
                (keyword != null && !keyword.isBlank()
                        ? " AND (name LIKE ? OR brand LIKE ?) "
                        : "");

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            int index = 1;

            ps.setInt(index++, facilityId);

            if (keyword != null && !keyword.isBlank()) {
                ps.setString(index++, "%" + keyword + "%");
                ps.setString(index, "%" + keyword + "%");
            }

            ResultSet rs = ps.executeQuery();

            if (rs.next()) return rs.getInt(1);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;
    }

    @Override
    public List<Inventory> findUnassigned(int limit, int offset, String keyword) {

        List<Inventory> list = new ArrayList<>();

        String sql = """
                SELECT i.inventory_id,
                       i.name,
                       i.brand,
                       i.description,
                       i.rental_price,
                       i.is_active,
                       i.facility_id
                FROM Inventory i
                WHERE i.facility_id IS NULL
                """ +
                (keyword != null && !keyword.isBlank()
                        ? " AND (i.name LIKE ? OR i.brand LIKE ?) "
                        : "") +
                " ORDER BY i.inventory_id OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            int index = 1;

            if (keyword != null && !keyword.isBlank()) {
                ps.setString(index++, "%" + keyword + "%");
                ps.setString(index++, "%" + keyword + "%");
            }

            ps.setInt(index++, offset);
            ps.setInt(index, limit);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                list.add(mapRow(rs));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    @Override
    public int countUnassigned(String keyword) {

        String sql = """
            SELECT COUNT(*)
            FROM Inventory
            WHERE facility_id IS NULL
            AND name LIKE ?
        """;

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, "%" + (keyword == null ? "" : keyword) + "%");

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;
    }
    private Inventory mapRow(ResultSet rs) throws SQLException {

        Inventory i = new Inventory();

        i.setInventoryId(rs.getInt("inventory_id"));
        i.setName(rs.getString("name"));
        i.setBrand(rs.getString("brand"));
        i.setDescription(rs.getString("description"));
        i.setRentalPrice(rs.getBigDecimal("rental_price"));
        i.setActive(rs.getBoolean("is_active"));

        i.setFacilityId((Integer) rs.getObject("facility_id"));

        try {
            i.setFacilityName(rs.getString("facility_name"));
        } catch (Exception ignored) {}

        return i;
    }

    @Override
    public void assignToCourt(int inventoryId, int facilityId) {

        String sql = """
            UPDATE Inventory
            SET facility_id = ?
            WHERE inventory_id = ?
        """;

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, facilityId);
            ps.setInt(2, inventoryId);

            ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void removeFromCourt(int inventoryId) {

        String sql = """
            UPDATE Inventory
            SET facility_id = NULL
            WHERE inventory_id = ?
        """;

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, inventoryId);

            ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    
    @Override
    public List<Inventory> findWithPagination(int limit, int offset, String keyword) {

        List<Inventory> list = new ArrayList<>();

        String sql = """
            SELECT i.*, f.name as facility_name
            FROM Inventory i
            LEFT JOIN Facility f ON i.facility_id = f.facility_id
            WHERE (? IS NULL OR i.name LIKE ?)
            ORDER BY i.inventory_id ASC
            OFFSET ? ROWS
            FETCH NEXT ? ROWS ONLY
        """;

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            if (keyword == null || keyword.trim().isEmpty()) {
                ps.setNull(1, Types.VARCHAR);
                ps.setNull(2, Types.VARCHAR);
            } else {
                ps.setString(1, keyword);
                ps.setString(2, "%" + keyword + "%");
            }

            ps.setInt(3, offset);
            ps.setInt(4, limit);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {

                Inventory i = new Inventory();

                i.setInventoryId(rs.getInt("inventory_id"));
                i.setName(rs.getString("name"));
                i.setBrand(rs.getString("brand"));
                i.setDescription(rs.getString("description"));
                i.setRentalPrice(rs.getBigDecimal("rental_price"));
                i.setActive(rs.getBoolean("is_active"));
                i.setFacilityId((Integer) rs.getObject("facility_id"));
                i.setFacilityName(rs.getString("facility_name"));

                list.add(i);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }
    
    @Override
    public int countInventory(String keyword) {

        String sql = """
            SELECT COUNT(*)
            FROM Inventory
            WHERE (? IS NULL OR name LIKE ?)
        """;

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            if (keyword == null || keyword.trim().isEmpty()) {
                ps.setNull(1, Types.VARCHAR);
                ps.setNull(2, Types.VARCHAR);
            } else {
                ps.setString(1, keyword);
                ps.setString(2, "%" + keyword + "%");
            }

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;
    }
    
}
       
    
    
    
   
    
    
