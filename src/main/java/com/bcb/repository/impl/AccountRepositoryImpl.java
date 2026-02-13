package com.bcb.repository.impl;

import com.bcb.model.Account;
import com.bcb.repository.AccountRepository;
import com.bcb.utils.DBContext;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Implementation of AccountRepository using JDBC
 */
public class AccountRepositoryImpl implements AccountRepository {

    @Override
    public Optional<Account> findByEmail(String email) {
        String sql = """
            SELECT 
                account_id,
                email,
                password_hash,
                google_id,
                full_name,
                phone,
                avatar_path,
                role,
                is_active,
                created_at
            FROM Account
            WHERE email = ? AND is_active = 1
        """;

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Account account = mapResultSetToAccount(rs);
                    return Optional.of(account);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error finding account by email: " + e.getMessage(), e);
        }

        return Optional.empty();
    }

    @Override
    public Optional<Account> findById(Integer accountId) {
        String sql = """
            SELECT 
                account_id,
                email,
                password_hash,
                google_id,
                full_name,
                phone,
                avatar_path,
                role,
                is_active,
                created_at
            FROM Account
            WHERE account_id = ? AND is_active = 1
        """;

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, accountId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Account account = mapResultSetToAccount(rs);
                    return Optional.of(account);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error finding account by ID: " + e.getMessage(), e);
        }

        return Optional.empty();
    }

    @Override
    public Account create(Account account) {
        String sql = """
            INSERT INTO Account (email, password_hash, full_name, phone, role, is_active, created_at)
            VALUES (?, ?, ?, ?, ?, 1, GETDATE())
        """;

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, account.getEmail());
            ps.setString(2, account.getPasswordHash());
            ps.setString(3, account.getFullName());
            ps.setString(4, account.getPhone());
            ps.setString(5, account.getRole());

            int affectedRows = ps.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Creating account failed, no rows affected.");
            }

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    account.setAccountId(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("Creating account failed, no ID obtained.");
                }
            }

            return account;

        } catch (SQLException e) {
            throw new RuntimeException("Error creating account: " + e.getMessage(), e);
        }
    }

    @Override
    public Account update(Account account) {
        // TODO: Implement update logic
        throw new UnsupportedOperationException("Update not implemented yet");
    }

    @Override
    public boolean existsByEmail(String email) {
        String sql = "SELECT COUNT(*) FROM Account WHERE email = ?";

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error checking email existence: " + e.getMessage(), e);
        }

        return false;
    }

    /**
     * Map ResultSet to Account entity
     */
    private Account mapResultSetToAccount(ResultSet rs) throws SQLException {
        Account account = new Account();

        account.setAccountId(rs.getInt("account_id"));
        account.setEmail(rs.getString("email"));
        account.setPasswordHash(rs.getString("password_hash"));
        account.setGoogleId(rs.getString("google_id"));
        account.setFullName(rs.getString("full_name"));
        account.setPhone(rs.getString("phone"));
        account.setAvatarPath(rs.getString("avatar_path"));
        account.setRole(rs.getString("role"));
        account.setIsActive(rs.getBoolean("is_active"));

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            account.setCreatedAt(createdAt.toLocalDateTime());
        }

        return account;
    }
}