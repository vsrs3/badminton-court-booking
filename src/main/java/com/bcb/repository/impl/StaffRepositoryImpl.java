package com.bcb.repository.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.bcb.exception.DataAccessException;
import com.bcb.model.Account;
import com.bcb.repository.StaffRepository;
import com.bcb.utils.DBContext;

public class StaffRepositoryImpl implements StaffRepository {

	@Override
	public List<Account> findAll(int limit, int offset) {
		String sql = "SELECT a.* FROM Account a " 
					+ "WHERE a.role = 'STAFF' " 
					+ "ORDER BY a.account_id DESC "
					+ "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

		List<Account> users = new ArrayList<>();

		try (Connection conn = DBContext.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

			pstmt.setInt(1, offset);
			pstmt.setInt(2, limit);

			try (ResultSet rs = pstmt.executeQuery()) {
				while (rs.next()) {
					users.add(mapResultSetToAccount(rs));
				}
			}
		} catch (SQLException e) {
			throw new DataAccessException("Failed to find all users", e);
		}

		return users;
	}

	@Override
	public int count() {
		String sql = "SELECT COUNT(*) FROM Account WHERE [role] = 'STAFF'";

		try (Connection conn = DBContext.getConnection();
				PreparedStatement pstmt = conn.prepareStatement(sql);
				ResultSet rs = pstmt.executeQuery()) {

			if (rs.next()) {
				return rs.getInt(1);
			}
		} catch (SQLException e) {
			throw new DataAccessException("Failed to count users", e);
		}

		return 0;
	}

	@Override
	public List<Account> findByKeyword(String keyword, int limit, int offset) {
		String sql = "SELECT a.* FROM Account a " + "WHERE a.role = 'STAFF' AND ("
				+ "a.full_name LIKE ? OR a.email LIKE ? OR a.phone LIKE ?) "
				+ "ORDER BY a.account_id DESC " + "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

		List<Account> users = new ArrayList<>();

		try (Connection conn = DBContext.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

			String likeParam = "%" + keyword + "%";
			pstmt.setString(1, likeParam);
			pstmt.setString(2, likeParam);
			pstmt.setString(3, likeParam);
			pstmt.setInt(4, offset);
			pstmt.setInt(5, limit);

			try (ResultSet rs = pstmt.executeQuery()) {
				while (rs.next()) {
					users.add(mapResultSetToAccount(rs));
				}
			}
		} catch (SQLException e) {
			throw new DataAccessException("Failed to find users by keyword", e);
		}

		return users;
	}

	@Override
	public int countByKeyword(String keyword) {
		String sql = "SELECT COUNT(*) FROM Account " + "WHERE [role] = 'STAFF' AND ("
				+ "full_name LIKE ? OR email LIKE ? OR phone LIKE ?)";

		try (Connection conn = DBContext.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

			String likeParam = "%" + keyword + "%";
			pstmt.setString(1, likeParam);
			pstmt.setString(2, likeParam);
			pstmt.setString(3, likeParam);
		
			try (ResultSet rs = pstmt.executeQuery()) {
				if (rs.next()) {
					return rs.getInt(1);
				}
			}
		} catch (SQLException e) {
			throw new DataAccessException("Failed to count users by keyword", e);
		}

		return 0;
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
				    WHERE account_id = ? AND role = 'STAFF'
				""";

		try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

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
	public boolean updateStatus(Integer accountId, boolean isActive) {
		String sql = "Update Account Set is_active = ? Where account_id = ?";

		try (Connection conn = DBContext.getConnection();) {
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setBoolean(1, isActive);
			ps.setInt(2, accountId);

			int affectedRows = ps.executeUpdate();
			return affectedRows > 0;

		} catch (Exception e) {
			throw new RuntimeException("Error updating account status: " + e.getMessage(), e);
		}
	}

	private Account mapResultSetToAccount(ResultSet rs) throws SQLException {
		Account account = new Account();
		account.setAccountId(rs.getInt("account_id"));
		account.setEmail(rs.getString("email"));
		account.setPasswordHash(rs.getString("password_hash"));
		account.setGoogleId(rs.getString("google_id"));
		account.setFullName(rs.getString("full_name"));
		account.setPhone(rs.getString("phone"));
		account.setRole(rs.getString("role"));
		account.setAvatarPath(rs.getString("avatar_path"));
		account.setIsActive(rs.getBoolean("is_active"));
		account.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
		return account;
	}

}
