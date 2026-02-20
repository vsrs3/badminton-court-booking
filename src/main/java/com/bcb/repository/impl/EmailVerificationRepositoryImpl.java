package com.bcb.repository.impl;

import com.bcb.model.EmailVerification;
import com.bcb.repository.EmailVerificationRepository;
import com.bcb.utils.DBContext;

import java.sql.*;

public class EmailVerificationRepositoryImpl
        implements EmailVerificationRepository {

    @Override
    public void savePendingRegister(
            String email,
            String passwordHash,
            String fullName,
            String phone,
            String role,
            String token,
            Timestamp expireAt
    ) {

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

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public EmailVerification findByToken(String token) {
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

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return null;
    }

    @Override
    public void deleteByToken(String token) {
        String sql = "DELETE FROM EmailVerification WHERE token = ?";

        try (Connection con = DBContext.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, token);
            ps.executeUpdate();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deleteExpiredTokens() {
        String sql = "DELETE FROM EmailVerification WHERE expire_at < GETDATE()";

        try (Connection con = DBContext.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.executeUpdate();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
