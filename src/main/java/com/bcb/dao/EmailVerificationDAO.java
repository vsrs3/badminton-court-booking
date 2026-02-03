/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.bcb.dao;
import com.bcb.model.EmailVerification;
import com.bcb.utils.DBContext;
import java.sql.*;

public class EmailVerificationDAO {

    // ✅ 1. LƯU TOKEN
    public void savePendingRegister(
        String email,
        String passwordHash,
        String fullName,
        String phone,
        String role,
        String token,
        Timestamp expireAt
) throws Exception {

    String sql = """
        INSERT INTO EmailVerification
        (email, password_hash, full_name, phone, role, token, expire_at)
        VALUES (?, ?, ?, ?, ?, ?, ?)
    """;

    try (Connection con = DBContext.getConnection();
         PreparedStatement ps = con.prepareStatement(sql)) {

        ps.setString(1, email);
        ps.setString(2, passwordHash);
        ps.setString(3, fullName);
        ps.setString(4, phone);
        ps.setString(5, role);
        ps.setString(6, token);
        ps.setTimestamp(7, expireAt);
        ps.executeUpdate();
    }
}


    // ✅ 2. TÌM TOKEN
   public EmailVerification findByToken(String token) throws Exception {

    String sql = "SELECT * FROM EmailVerification WHERE token = ?";

    try (Connection con = DBContext.getConnection();
         PreparedStatement ps = con.prepareStatement(sql)) {

        ps.setString(1, token);
        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            EmailVerification ev = new EmailVerification();

            ev.setId(rs.getInt("id"));
            ev.setEmail(rs.getString("email"));
            ev.setPasswordHash(rs.getString("password_hash"));
            ev.setFullName(rs.getString("full_name"));
            ev.setPhone(rs.getString("phone"));
            ev.setRole(rs.getString("role"));

            ev.setToken(rs.getString("token"));
            ev.setExpireAt(rs.getTimestamp("expire_at"));

            return ev;
        }
    }
    return null;
}

   // 🧹 Dọn token hết hạn (dùng cho debug / manual)
public void deleteExpiredTokens() throws Exception {
    String sql = "DELETE FROM EmailVerification WHERE expire_at < GETDATE()";
    try (Connection con = DBContext.getConnection();
         PreparedStatement ps = con.prepareStatement(sql)) {
        ps.executeUpdate();
    }
}

    // ✅ 3. XOÁ TOKEN (SAU KHI VERIFY)
    public void deleteByToken(String token) throws Exception {

        String sql = "DELETE FROM EmailVerification WHERE token = ?";

        try (Connection con = DBContext.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, token);
            ps.executeUpdate();
        }
    }
}
