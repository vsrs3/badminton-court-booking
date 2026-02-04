package com.bcb.repository.impl;

import com.bcb.model.Account;
import com.bcb.repository.AccountAuthRepository;
import com.bcb.utils.DBContext;

import java.sql.*;

public class AccountAuthRepositoryImpl implements AccountAuthRepository {

    private final String GET_CUSTOMER = "Select * From Account where email = ?";

    private final String DELETE_CUSTOMER = "Delete From Account Where account_id = ?";

    private final String CREATE_USER = "Insert Into Account "
                                        + "(email, password_hash, full_name, phone, role, created_at) "
                                        + "Values(?, ?, ?, ?,  'USER', GETDATE())";

   @Override
    public Account getAccountByEmailAndPass(String email) {

        try (Connection connect = DBContext.getConnection()) {
            PreparedStatement ps = connect.prepareStatement(GET_CUSTOMER);
            ps.setString(1, email.trim());

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
    public boolean registerUser(String username, String email, String password, String phone) {

        try (Connection connect = DBContext.getConnection()) {
            PreparedStatement st = connect.prepareStatement(CREATE_USER);
            st.setString(1, email);
            st.setString(2, password);
            st.setString(3, username);
            st.setString(4, phone);

            int result = st.executeUpdate();
            return result > 0;

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
        }
        return false;
    }

    @Override
    public boolean deleteAccountById(Integer accountId) {

        try(Connection connect = DBContext.getConnection();) {
            PreparedStatement ps = connect.prepareStatement(DELETE_CUSTOMER);
            ps.setInt(1,accountId);
            int result = ps.executeUpdate();
            return result > 0;

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
        }
        return false;
    }
}
