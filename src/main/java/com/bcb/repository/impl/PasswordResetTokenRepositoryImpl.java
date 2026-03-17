package com.bcb.repository.impl;

import com.bcb.model.PasswordResetToken;
import com.bcb.repository.PasswordResetTokenRepository;
import com.bcb.utils.DBContext;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;

public class PasswordResetTokenRepositoryImpl implements PasswordResetTokenRepository {

    @Override
    public void save(String email, String token, Timestamp expireAt) {
        String sql = """
            INSERT INTO PasswordResetToken (email, token, expire_at)
            VALUES (?, ?, ?)
        """;

        try (Connection con = DBContext.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, email);
            ps.setString(2, token);
            ps.setTimestamp(3, expireAt);
            ps.executeUpdate();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public PasswordResetToken findByToken(String token) {
        String sql = "SELECT * FROM PasswordResetToken WHERE token = ?";

        try (Connection con = DBContext.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, token);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }

                PasswordResetToken passwordResetToken = new PasswordResetToken();
                passwordResetToken.setId(rs.getInt("id"));
                passwordResetToken.setEmail(rs.getString("email"));
                passwordResetToken.setToken(rs.getString("token"));
                passwordResetToken.setExpireAt(rs.getTimestamp("expire_at"));
                return passwordResetToken;
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deleteByToken(String token) {
        String sql = "DELETE FROM PasswordResetToken WHERE token = ?";

        try (Connection con = DBContext.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, token);
            ps.executeUpdate();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deleteByEmail(String email) {
        String sql = "DELETE FROM PasswordResetToken WHERE email = ?";

        try (Connection con = DBContext.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, email);
            ps.executeUpdate();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deleteExpiredTokens() {
        String sql = "DELETE FROM PasswordResetToken WHERE expire_at < GETDATE()";

        try (Connection con = DBContext.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.executeUpdate();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
