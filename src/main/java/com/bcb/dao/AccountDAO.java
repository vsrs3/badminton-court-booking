/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.bcb.dao;
import com.bcb.utils.DBContext;
import java.sql.*;
import com.bcb.model.Account;
import com.bcb.utils.DBContext;
import org.mindrot.jbcrypt.BCrypt;

public class AccountDAO {
    public boolean isPhoneExists(String phone) throws Exception {
        String sql = "SELECT 1 FROM Account WHERE phone = ?";
        try (Connection con = DBContext.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, phone);
            return ps.executeQuery().next();
        }
    }

    public boolean isEmailExists(String email) throws Exception {
        String sql = "SELECT 1 FROM Account WHERE email = ?";
        try (Connection con = DBContext.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, email);
            return ps.executeQuery().next();
        }
    }

    // Register bằng form thường
   public void register(Account acc) throws Exception {
    String sql = """
        INSERT INTO Account
        (email, password_hash, full_name, phone, role, is_active)
        VALUES (?, ?, ?, ?, 'USER', 1)
    """;

    try (Connection con = DBContext.getConnection();
         PreparedStatement ps = con.prepareStatement(sql)) {

        ps.setString(1, acc.getEmail());
        ps.setString(2, acc.getPasswordHash());
        ps.setString(3, acc.getFullName());
        ps.setString(4, acc.getPhone());
        ps.executeUpdate();
    }
}


    // Register bằng Google
    public void registerByGoogle(Account acc) throws Exception {
        String sql = """
            INSERT INTO Account
            (email, google_id, full_name, avatar_path, role, is_active)
            VALUES (?, ?, ?, ?, 'USER', 1)
        """;
        try (Connection con = DBContext.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, acc.getEmail());
            ps.setString(2, acc.getGoogleId());
            ps.setString(3, acc.getFullName());
            ps.setString(4, acc.getAvatarPath());
            ps.executeUpdate();
        }
    }
    
    
    public Account findByEmail(String email) throws Exception {
    String sql = "SELECT * FROM Account WHERE email = ?";
    try (Connection con = DBContext.getConnection();
         PreparedStatement ps = con.prepareStatement(sql)) {

        ps.setString(1, email);
        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            Account acc = new Account();
            acc.setAccountId(rs.getInt("account_id"));
            acc.setEmail(rs.getString("email"));
            acc.setGoogleId(rs.getString("google_id"));
            acc.setFullName(rs.getString("full_name"));
            acc.setAvatarPath(rs.getString("avatar_path"));
            acc.setRole(rs.getString("role"));
            acc.setIsActive(rs.getBoolean("is_active"));
            return acc;
        }
    }
    
    
    return null;}
    
    
    public void updateGoogleId(int accountId, String googleId) throws Exception {
    String sql = "UPDATE Account SET google_id = ? WHERE account_id = ?";
    try (Connection con = DBContext.getConnection();
         PreparedStatement ps = con.prepareStatement(sql)) {
        ps.setString(1, googleId);
        ps.setInt(2, accountId);
        ps.executeUpdate();
    }
}
    public Account loginByEmailPassword(String email, String rawPassword) throws Exception {
    String sql = "SELECT * FROM Account WHERE email = ? AND is_active = 1";

    try (Connection con = DBContext.getConnection();
         PreparedStatement ps = con.prepareStatement(sql)) {

        ps.setString(1, email);
        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            String hash = rs.getString("password_hash");
            if (hash != null && BCrypt.checkpw(rawPassword, hash)) {
                Account acc = new Account();
                acc.setAccountId(rs.getInt("account_id"));
                acc.setEmail(rs.getString("email"));
                acc.setGoogleId(rs.getString("google_id"));
                acc.setFullName(rs.getString("full_name"));
                acc.setRole(rs.getString("role"));
                acc.setIsActive(rs.getBoolean("is_active"));
                return acc;
            }
        }
    }
    return null;
}


}
