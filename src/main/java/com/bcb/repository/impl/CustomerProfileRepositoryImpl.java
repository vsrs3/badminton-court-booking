package com.bcb.repository.impl;

import com.bcb.model.Account;
import com.bcb.repository.CustomerProfileRepository;
import com.bcb.utils.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class CustomerProfileRepositoryImpl implements CustomerProfileRepository {

    private final String UPDATE_PROFILE = "Update Account Set "
                                            + "avatar_path = ?, "
                                            + "full_name = ?, "
                                            + "email = ?, "
                                            + "phone = ? "
                                            + "Where account_id = ?";

    private final String UPDATE_PASSWORD = "Update Account Set " +
                                                "password_hash = ? " +
                                                "Where account_id = ?";
            //"And password_hash = ?"

    private final String GET_CUSTOMER_BY_ID = "Select * From Account Where account_id = ?";
    private final String GET_LIST_EMAIL = "SELECT email, [role] "
                                        + "FROM Account "
                                        + "WHERE email IS NOT NULL "
                                        + "AND email <> ''AND email <> ? AND [role] = 'USER'";

    @Override
    public boolean updateAccountInfo(String avatarPath, String fullName, String email, String phone, Integer accountId) {

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
        }
        return false;
    }

    @Override
    public boolean updatePassword(String newPass, Integer accountId) {

        try (Connection connect = DBContext.getConnection();PreparedStatement ps = connect.prepareStatement(UPDATE_PASSWORD)){
            ps.setString(1, newPass);
            ps.setInt(2, accountId);

            int result = ps.executeUpdate();
            return result > 0;

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
        }
        return false;
    }

    @Override
    public Account getCustomerById(Integer accountId) {

        Connection connect = DBContext.getConnection();
        try (PreparedStatement ps = connect.prepareStatement(GET_CUSTOMER_BY_ID)) {
            ps.setInt(1, accountId);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new Account(
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
            System.out.println(e.getMessage());
        }
        return null;
    }

    @Override
    public List<String> emailList(String email) {

        List<String> listEmail = new ArrayList<>();
        try(Connection connect = DBContext.getConnection()) {
            PreparedStatement ps = connect.prepareStatement(GET_LIST_EMAIL);
            ps.setString(1, email);

            ResultSet result = ps.executeQuery();
             while (result.next()) {
                listEmail.add(result.getString("email"));
            }
            return listEmail;

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return listEmail;
    }
}
