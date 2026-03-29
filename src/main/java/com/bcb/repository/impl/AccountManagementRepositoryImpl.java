package com.bcb.repository.impl;

import com.bcb.exception.DataAccessException;
import com.bcb.model.Account;
import com.bcb.repository.AccountManagementRepository;
import com.bcb.utils.DBContext;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of AccountManagementRepository using JDBC
 */
public class AccountManagementRepositoryImpl implements AccountManagementRepository {

    @Override
    public List<Account> findAll(int limit, int offset) {
        String sql = "SELECT * FROM Account WHERE is_deleted = 0 ORDER BY account_id DESC OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

        List<Account> accounts = new ArrayList<>();

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, offset);
            ps.setInt(2, limit);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    accounts.add(mapResultSetToAccount(rs));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to find all accounts", e);
        }
        return accounts;
    }

    @Override
    public int count() {
        String sql = "SELECT COUNT(*) FROM Account WHERE is_deleted = 0";

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to count accounts", e);
        }
        return 0;
    }

    @Override
    public List<Account> findByKeyword(String keyword, int limit, int offset) {
        String sql = "SELECT * FROM Account "
                + "WHERE is_deleted = 0 AND (full_name LIKE ? OR email LIKE ? OR phone LIKE ?) "
                + "ORDER BY account_id DESC "
                + "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";


        List<Account> accounts = new ArrayList<>();

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            String likeParam = "%" + keyword + "%";
            ps.setString(1, likeParam);
            ps.setString(2, likeParam);
            ps.setString(3, likeParam);
            ps.setInt(4, offset);
            ps.setInt(5, limit);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    accounts.add(mapResultSetToAccount(rs));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to find accounts by keyword", e);
        }
        return accounts;
    }

    @Override
    public int countByKeyword(String keyword) {
        String sql = "SELECT COUNT(*) FROM Account "
                + "WHERE is_deleted = 0 AND (full_name LIKE ? OR email LIKE ? OR phone LIKE ?)";


        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            String likeParam = "%" + keyword + "%";
            ps.setString(1, likeParam);
            ps.setString(2, likeParam);
            ps.setString(3, likeParam);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to count accounts by keyword", e);
        }
        return 0;
    }

    @Override
    public List<Account> findByKeywordAndRole(String keyword, String role, int limit, int offset) {
        String sql = "SELECT * FROM Account "
                + "WHERE is_deleted = 0 AND [role] = ? AND (full_name LIKE ? OR email LIKE ? OR phone LIKE ?) "
                + "ORDER BY account_id DESC "
                + "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

        List<Account> accounts = new ArrayList<>();

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            String likeParam = "%" + keyword + "%";
            ps.setString(1, role);
            ps.setString(2, likeParam);
            ps.setString(3, likeParam);
            ps.setString(4, likeParam);
            ps.setInt(5, offset);
            ps.setInt(6, limit);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    accounts.add(mapResultSetToAccount(rs));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to find accounts by keyword and role", e);
        }
        return accounts;
    }

    @Override
    public int countByKeywordAndRole(String keyword, String role) {
        String sql = "SELECT COUNT(*) FROM Account "
                + "WHERE is_deleted = 0 AND [role] = ? AND (full_name LIKE ? OR email LIKE ? OR phone LIKE ?)";

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            String likeParam = "%" + keyword + "%";
            ps.setString(1, role);
            ps.setString(2, likeParam);
            ps.setString(3, likeParam);
            ps.setString(4, likeParam);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to count accounts by keyword and role", e);
        }
        return 0;
    }

    @Override
    public List<Account> findByRole(String role, int limit, int offset) {
        String sql = "SELECT * FROM Account WHERE is_deleted = 0 AND [role] = ? "
                + "ORDER BY account_id DESC "
                + "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";


        List<Account> accounts = new ArrayList<>();

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, role);
            ps.setInt(2, offset);
            ps.setInt(3, limit);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    accounts.add(mapResultSetToAccount(rs));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to find accounts by role", e);
        }
        return accounts;
    }

    @Override
    public int countByRole(String role) {
        String sql = "SELECT COUNT(*) FROM Account WHERE is_deleted = 0 AND [role] = ?";


        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, role);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to count accounts by role", e);
        }
        return 0;
    }

    @Override
    public Optional<Account> findById(Integer accountId) {
        String sql = """
    SELECT
        account_id, email, password_hash, google_id,
        full_name, phone, avatar_path, role, is_active, is_deleted, created_at
    FROM Account
    WHERE account_id = ? AND is_deleted = 0
""";


        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, accountId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToAccount(rs));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to find account by ID", e);
        }
        return Optional.empty();
    }

    @Override
    public boolean updateStatus(Integer accountId, boolean isActive) {
        String sql = "UPDATE Account SET is_active = ? WHERE account_id = ?";

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setBoolean(1, isActive);
            ps.setInt(2, accountId);

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to update account status", e);
        }
    }

    @Override
    public boolean updateAccountInfo(Integer accountId, String fullName, String email, String phone, String role) {
        String sql = "UPDATE Account SET full_name = ?, email = ?, phone = ?, [role] = ? WHERE account_id = ?";

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, fullName);
            ps.setString(2, email);
            ps.setString(3, phone);
            ps.setString(4, role);
            ps.setInt(5, accountId);

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to update account info", e);
        }
    }

    @Override
    public boolean deleteAccount(Integer accountId) {
        // Soft delete
        String sql = "UPDATE Account SET is_deleted = 1, is_active = 0 WHERE account_id = ?";


        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, accountId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to soft delete account", e);
        }
    }

    @Override
    public boolean isEmailExistsForOther(String email, Integer excludeAccountId) {
        String sql = "SELECT COUNT(*) FROM Account WHERE email = ? AND account_id != ?";

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email);
            ps.setInt(2, excludeAccountId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to check email existence", e);
        }
        return false;
    }

    @Override
    public boolean isPhoneExistsForOther(String phone, Integer excludeAccountId) {
        String sql = "SELECT COUNT(*) FROM Account WHERE phone = ? AND account_id != ?";

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, phone);
            ps.setInt(2, excludeAccountId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to check phone existence", e);
        }
        return false;
    }

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
        Timestamp ts = rs.getTimestamp("created_at");
        account.setIsDeleted(rs.getBoolean("is_deleted"));

        account.setCreatedAt(ts != null ? ts.toLocalDateTime() : null);
        return account;
    }
}