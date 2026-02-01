package com.bcb.repository.impl;

import com.bcb.model.Customer;
import com.bcb.repository.CustomerAuthRepository;
import com.bcb.utils.DBContext;

import java.sql.*;

public class CustomerAuthRepositoryImpl implements CustomerAuthRepository {

    private final String GET_CUSTOMER = "Select * From Account where email = ?";

    private final String DELETE_CUSTOMER = "Delete From Account Where account_id = ?";

   @Override
    public Customer getCustomerByEmailAndPass(String email) {

        try (Connection connect = DBContext.getConnection()) {
            PreparedStatement ps = connect.prepareStatement(GET_CUSTOMER);
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

    @Override
    public boolean deleteCustomerById(int customerId) {

        try(Connection connect = DBContext.getConnection();) {
            PreparedStatement ps = connect.prepareStatement(DELETE_CUSTOMER);
            ps.setInt(1,customerId);
            int result = ps.executeUpdate();
            return result > 0;

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
        }
        return false;
    }
}
