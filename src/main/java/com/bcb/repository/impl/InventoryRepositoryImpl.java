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
        String sql = "SELECT * FROM Inventory";

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
        String sql = "SELECT * FROM Inventory WHERE name LIKE ? OR brand LIKE ?";

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
        String sql = "SELECT * FROM Inventory WHERE inventory_id = ?";

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
        String sql = "INSERT INTO Inventory(name, brand, description, rental_price, is_active) VALUES (?, ?, ?, ?, ?)";

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
        String sql = "UPDATE Inventory SET name=?, brand=?, description=?, rental_price=?, is_active=? WHERE inventory_id=?";

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
        return i;
    }
}