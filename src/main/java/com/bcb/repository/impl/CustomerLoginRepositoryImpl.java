package com.bcb.repository.impl;

import com.bcb.model.Customer;
import com.bcb.utils.DBContext;

import java.sql.*;

public class CustomerLoginRepositoryImpl implements com.bcb.repository.CustomerLoginRepository {
   @Override
    public Customer getCustomerByEmailAndPass(String email) {
        String sql = "Select * From Account where email = ?";

        try (Connection connect = DBContext.getConnection();PreparedStatement ps = connect.prepareStatement(sql)) {
            ps.setString(1, email.trim());

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new Customer(
                        rs.getInt("account_id"),
                        rs.getString("email"),
                        rs.getString("password_hash"),
                        rs.getString("google_id"),
                        rs.getString("full_name"),
                        rs.getString("phone"),
                        rs.getString("avatar_path"),
                        rs.getString("role"),
                        rs.getBoolean("is_active"),
                        rs.getTimestamp("created_at").toLocalDateTime()
                );
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
        }
        return null;
    }
}
