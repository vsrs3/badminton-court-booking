package com.bcb.repository.impl;

import com.bcb.model.Customer;
import com.bcb.repository.CustomerProfileRepository;
import com.bcb.utils.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class CustomerProfileRepositoryImpl implements CustomerProfileRepository {

    private final String UPDATE_PROFILE = "Update Account Set "
                                            + "avatar_path = ?, "
                                            + "full_name = ?, "
                                            + "email = ?, "
                                            + "phone = ? "
                                            + "Where account_id = ?";

    private final String UPDATE_PASSWORD = "Update Account Set " +
                                                "password_hash = ? " +
                                                "Where account_id = ? " +
                                                "And password_hash = ?";

    private final String GET_CUSTOMER_BY_ID = "Select * From Account Where account_id = ?";

    @Override
    public boolean updateAccountInfo(String avatarPath, String fullName, String email, String phone, int accountId) {

        DBContext db = new DBContext();
        Connection connect = db.getConnection();
        try (PreparedStatement ps = connect.prepareStatement(UPDATE_PROFILE)) {
            ps.setString(1, avatarPath);
            ps.setString(2, fullName);
            ps.setString(3, email);
            ps.setString(4, phone);
            ps.setInt(5, accountId);

            int result = ps.executeUpdate();
            return result > 0;

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
            return false;
        }
    }

    @Override
    public boolean updatePassword(String oldPass, String newPass, int accountId) {

        DBContext db = new DBContext();
        Connection connect = db.getConnection();
        try (PreparedStatement ps = connect.prepareStatement(UPDATE_PASSWORD)){
            ps.setString(1, newPass);
            ps.setInt(2, accountId);
            ps.setString(3, oldPass);

            int result = ps.executeUpdate();
            return result > 0;

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
            return false;
        }
    }

    @Override
    public Customer getCustomerById(int cusId) {

        DBContext db = new DBContext();
        Connection connect = db.getConnection();
        try (PreparedStatement ps = connect.prepareStatement(GET_CUSTOMER_BY_ID)) {
            ps.setInt(1, cusId);

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
                        rs.getTimestamp("created_at").toLocalDateTime());
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
        }
        return null;
    }
}
