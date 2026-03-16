package com.bcb.repository.impl;

import com.bcb.repository.CustomerRepository;
import com.bcb.utils.DBContext;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class CustomerRepositoryImpl implements CustomerRepository {

    @Override
    public int countCustomers() {

        int total = 0;

        String sql = "SELECT COUNT(*) FROM Account";

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                total = rs.getInt(1);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return total;
    }

    @Override
    public List<Object[]> getLatestCustomers() {

        List<Object[]> list = new ArrayList<>();

        String sql = """
                SELECT TOP 5
                account_id,
                full_name,
                email,
                created_at
                FROM Account
                ORDER BY created_at DESC
                """;

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                Object[] row = new Object[]{
                        rs.getInt("account_id"),
                        rs.getString("full_name"),
                        rs.getString("email"),
                        rs.getTimestamp("created_at")
                };

                list.add(row);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }
}