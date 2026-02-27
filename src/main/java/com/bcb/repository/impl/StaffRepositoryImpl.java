package com.bcb.repository.impl;

import java.sql.Statement;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.bcb.exception.DataAccessException;
import com.bcb.model.Account;
import com.bcb.model.Facility;
import com.bcb.model.Staff;
import com.bcb.repository.StaffRepository;
import com.bcb.utils.DBContext;

public class StaffRepositoryImpl implements StaffRepository {

	@Override
	public List<Account> findAll(int limit, int offset) {
		String sql = "SELECT a.* FROM Account a " + "WHERE a.role = 'STAFF' " + "ORDER BY a.account_id DESC "
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
				+ "a.full_name LIKE ? OR a.email LIKE ? OR a.phone LIKE ?) " + "ORDER BY a.account_id DESC "
				+ "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

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

	/**
	 * Chuyển đổi ResultSet thành đối tượng Account
	 * 
	 * @param rs ResultSet chứa dữ liệu của tài khoản
	 * @return Đối tượng Account được tạo từ ResultSet
	 * @throws SQLException Nếu có lỗi khi truy xuất dữ liệu từ ResultSet
	 */
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

	@Override
	public List<Facility> findFacilitiesById(Integer accountId) {
		String sql = "SELECT f.* FROM Facility f " + "JOIN Staff s ON f.facility_id = s.facility_id "
				+ "WHERE s.account_id = ?";

		try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setInt(1, accountId);

			ResultSet rs = ps.executeQuery();
			List<Facility> facilities = new ArrayList<>();

			while (rs.next()) {
				facilities.add(mapResultSetToFacility(rs));
			}
			return facilities;

		} catch (SQLException e) {
			throw new RuntimeException("Error finding facilities by staff ID: " + e.getMessage(), e);
		}
	}

	/**
	 * Chuyển đổi ResultSet thành đối tượng Facility
	 * 
	 * @param rs ResultSet chứa dữ liệu của cơ sở y tế
	 * @return Đối tượng Facility được tạo từ ResultSet
	 * @throws SQLException Nếu có lỗi khi truy xuất dữ liệu từ ResultSet
	 */
	private Facility mapResultSetToFacility(ResultSet rs) throws SQLException {
		Facility f = new Facility();
		f.setFacilityId(rs.getInt("facility_id"));
		f.setName(rs.getString("name"));
		f.setProvince(rs.getString("province"));
		f.setDistrict(rs.getString("district"));
		f.setWard(rs.getString("ward"));
		f.setAddress(rs.getString("address"));
		BigDecimal lat = rs.getBigDecimal("latitude");
		BigDecimal lng = rs.getBigDecimal("longitude");

		f.setLatitude(lat);
		f.setLongitude(lng);
		f.setDescription(rs.getString("description"));
		Time openTime = rs.getTime("open_time");
		f.setOpenTime(openTime != null ? openTime.toLocalTime() : null);

		Time closeTime = rs.getTime("close_time");
		f.setCloseTime(closeTime != null ? closeTime.toLocalTime() : null);
		f.setIsActive(rs.getBoolean("is_active"));
		return f;
	}

	@Override
	public List<Facility> findAllFacilities() {
		String sql = "SELECT * FROM Facility WHERE is_active = 1 ORDER BY name ASC";

		try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

			ResultSet rs = ps.executeQuery();
			List<Facility> facilities = new ArrayList<>();

			while (rs.next()) {
				facilities.add(mapResultSetToFacility(rs));
			}
			return facilities;

		} catch (SQLException e) {
			throw new RuntimeException("Error finding all facilities: " + e.getMessage(), e);
		}
	}

	@Override
	public boolean createStaff(String fullName, String email, String phone, Integer facilityId) {

	    String sql1 = "INSERT INTO Account "
	            + "(email, password_hash, google_id, full_name, phone, avatar_path, role, is_active, created_at) "
	            + "VALUES "
	            + "(?, '$2a$10$l9VhV2BupVyxxagpB243S.AuynE7hf7bSEVjFwaRl9KEO/IPQYFrO', NULL, ?, ?, NULL, 'STAFF', 1, GETDATE())";

	    String sql2 = "INSERT INTO Staff (account_id, facility_id, is_active) VALUES (?, ?, 1)";

	    try (Connection conn = DBContext.getConnection()) {
	        conn.setAutoCommit(false);

	        try (PreparedStatement ps1 = conn.prepareStatement(sql1, Statement.RETURN_GENERATED_KEYS);) {

	            //INSERT ACCOUNT
	            ps1.setString(1, email);
	            ps1.setString(2, fullName);
	            ps1.setString(3, phone);
	            ps1.executeUpdate();

	            // Lấy account_id vừa tạo
	            ResultSet rs = ps1.getGeneratedKeys();
	            if (!rs.next()) throw new SQLException("Cannot get account_id");
	            int accountId = rs.getInt(1);

	            // INSERT STAFF
	            try (PreparedStatement ps2 = conn.prepareStatement(sql2)) {
	                ps2.setInt(1, accountId);
	                ps2.setInt(2, facilityId);
	                ps2.executeUpdate();
	            }

	            conn.commit();
	            return true;

	        } catch (Exception e) {
	            conn.rollback();
	        	System.out.print(e.getMessage());
	        	return false;
	        }

	    } catch (Exception e) {
	    	System.out.print(e.getMessage());
	        return false;
	    }
	}

}
