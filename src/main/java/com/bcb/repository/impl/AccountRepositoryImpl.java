package com.bcb.repository.impl;

import com.bcb.model.Account;
import com.bcb.repository.AccountRepository;
import com.bcb.utils.DBContext;
import org.mindrot.jbcrypt.BCrypt;

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
    public Account findByEmailAnyStatus(String email) throws SQLException {
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




    @Override
    public boolean isPhoneExists(String phone) {
        String sql = "SELECT 1 FROM Account WHERE phone = ?";
        try (Connection con = DBContext.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, phone);
            return ps.executeQuery().next();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean isEmailExists(String email) {
        String sql = "SELECT 1 FROM Account WHERE email = ?";
        try (Connection con = DBContext.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, email);
            return ps.executeQuery().next();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void register(Account acc) {
        String sql = """
        INSERT INTO Account
        (email, password_hash, full_name, phone, role, is_active)
        VALUES (?, ?, ?, ?, ?, 1)
        """;

        try (Connection con = DBContext.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, acc.getEmail());
            ps.setString(2, acc.getPasswordHash());
            ps.setString(3, acc.getFullName());
            ps.setString(4, acc.getPhone());
            ps.setString(5, acc.getRole());
            ps.executeUpdate();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void registerByGoogle(Account acc) {
        String sql = """
        INSERT INTO Account
        (email, google_id, full_name, avatar_path, role, is_active)
        VALUES (?, ?, ?, ?, 'CUSTOMER', 1)
        """;

        try (Connection con = DBContext.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, acc.getEmail());
            ps.setString(2, acc.getGoogleId());
            ps.setString(3, acc.getFullName());
            ps.setString(4, acc.getAvatarPath());
            ps.executeUpdate();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }



    @Override
    public Account findByGoogleId(String googleId) {
        String sql = "SELECT * FROM Account WHERE google_id = ?";

        try (Connection con = DBContext.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, googleId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return mapResultSetToAccount(rs);
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return null;
    }

    @Override
    public void updateGoogleId(int accountId, String googleId) {
        String sql = "UPDATE Account SET google_id = ? WHERE account_id = ?";

        try (Connection con = DBContext.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, googleId);
            ps.setInt(2, accountId);
            ps.executeUpdate();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Account loginByEmailPassword(String email, String rawPassword) {
        String sql = "SELECT * FROM Account WHERE email = ? AND is_active = 1";

        try (Connection con = DBContext.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String hash = rs.getString("password_hash");

                if ((hash != null) && BCrypt.checkpw(rawPassword, hash)) {
                    return mapResultSetToAccount(rs);
                }
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return null;
    }

    @Override
    public void updatePassword(String email, String newHashedPassword) {
        String sql = "UPDATE Account SET password_hash = ? WHERE email = ?";

        try (Connection con = DBContext.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, newHashedPassword);
            ps.setString(2, email);
            ps.executeUpdate();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }




}