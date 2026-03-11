package com.bcb.repository.impl;

import com.bcb.repository.RevenueRepository;
import com.bcb.utils.DBContext;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class RevenueRepositoryImpl implements RevenueRepository {

    @Override
    public double getTotalRevenue() {

        double total = 0;

        String sql = "SELECT SUM(amount) AS total FROM Payment";

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                total = rs.getDouble("total");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return total;
    }

    @Override
    public List<Object[]> getRecentTransactions() {

        List<Object[]> list = new ArrayList<>();

        String sql = """
                SELECT TOP 5 
                p.payment_id,
                a.full_name,
                p.amount,
                p.created_at
                FROM Payment p
                JOIN Booking b ON p.booking_id = b.booking_id
                JOIN Account a ON b.account_id = a.account_id
                ORDER BY p.created_at DESC
                """;

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                Object[] row = new Object[]{
                        rs.getInt("payment_id"),
                        rs.getString("full_name"),
                        rs.getDouble("amount"),
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