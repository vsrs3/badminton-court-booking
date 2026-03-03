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
        	           i.court_id,
        	           c.court_name
        	    FROM Inventory i
        	    LEFT JOIN Court c ON i.court_id = c.court_id
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
        	           i.court_id,
        	           c.court_name
        	    FROM Inventory i
        	    LEFT JOIN Court c ON i.court_id = c.court_id
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
    		           i.court_id,
    		           c.court_name
    		    FROM Inventory i
    		    LEFT JOIN Court c ON i.court_id = c.court_id
    		    WHERE i.inventory_id = ?
    		""";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return mapRow(rs);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public void save(Inventory inventory) {
    	String sql = "INSERT INTO Inventory(name, brand, description, rental_price, is_active, court_id) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, inventory.getName());
            ps.setString(2, inventory.getBrand());
            ps.setString(3, inventory.getDescription());
            ps.setBigDecimal(4, inventory.getRentalPrice());
            ps.setBoolean(5, inventory.isActive());
            ps.setObject(6, inventory.getCourtId(), Types.INTEGER);
            
            ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void update(Inventory inventory) {
    	String sql = "UPDATE Inventory SET name=?, brand=?, description=?, rental_price=?, is_active=?, court_id=? WHERE inventory_id=?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

        	ps.setString(1, inventory.getName());
        	ps.setString(2, inventory.getBrand());
        	ps.setString(3, inventory.getDescription());
        	ps.setBigDecimal(4, inventory.getRentalPrice());
        	ps.setBoolean(5, inventory.isActive());
        	ps.setObject(6, inventory.getCourtId(), Types.INTEGER);
        	ps.setInt(7, inventory.getInventoryId());

            ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void delete(int id) {
        String sql = "DELETE FROM Inventory WHERE inventory_id = ?";

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();

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
        i.setCourtId((Integer) rs.getObject("court_id"));
        i.setCourtName(rs.getString("court_name"));
        return i;
    }
    
    
    @Override
    public List<Inventory> findByFacility(int facilityId, int limit, int offset, String keyword) {
        List<Inventory> list = new ArrayList<>();

        String sql = """
            SELECT i.*, c.court_name
            FROM Inventory i
            JOIN Court c ON i.court_id = c.court_id
            WHERE c.facility_id = ?
            """ + (keyword != null && !keyword.isBlank()
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
                Inventory i = mapRow(rs);
                i.setCourtName(rs.getString("court_name"));
                list.add(i);
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
            FROM Inventory i
            JOIN Court c ON i.court_id = c.court_id
            WHERE c.facility_id = ?
            """ + (keyword != null && !keyword.isBlank()
                ? " AND (i.name LIKE ? OR i.brand LIKE ?) "
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
               i.court_id,
               c.court_name
        FROM Inventory i
        LEFT JOIN Court c ON i.court_id = c.court_id
        WHERE i.court_id IS NULL
        """ + (keyword != null && !keyword.isBlank()
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
            WHERE court_id IS NULL
            """ + (keyword != null && !keyword.isBlank()
                ? " AND (name LIKE ? OR brand LIKE ?) "
                : "");

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            if (keyword != null && !keyword.isBlank()) {
                ps.setString(1, "%" + keyword + "%");
                ps.setString(2, "%" + keyword + "%");
            }

            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;
    }

    @Override
    public void assignToCourt(int inventoryId, int courtId) {
        String sql = "UPDATE Inventory SET court_id=? WHERE inventory_id=?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, courtId);
            ps.setInt(2, inventoryId);
            ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void removeFromCourt(int inventoryId) {
        String sql = "UPDATE Inventory SET court_id=NULL WHERE inventory_id=?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, inventoryId);
            ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}