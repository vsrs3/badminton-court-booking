package com.bcb.repository.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import com.bcb.exception.DataAccessException;
import com.bcb.utils.DBContext;
import com.bcb.repository.StaffProfileRepository;

public class StaffProfileRepositoryImpl implements StaffProfileRepository {

	@Override
	public boolean updateInfo(Integer accountId, Integer facilityId, String fullName, String email, String phoneNumber) {
		
		String sqlAccount = "UPDATE Account SET full_name = ?, email = ?, phone = ? WHERE account_id = ?";
	    String sqlStaff   = "UPDATE Staff SET facility_id = ? WHERE account_id = ?";
		
		try(Connection conn = DBContext.getConnection()) {
			conn.setAutoCommit(false);
			
			try(PreparedStatement ps1 = conn.prepareStatement(sqlAccount);
	             PreparedStatement ps2 = conn.prepareStatement(sqlStaff)) {
				
				// Update Account
	            ps1.setString(1, fullName);
	            ps1.setString(2, email);
	            ps1.setString(3, phoneNumber);
	            ps1.setInt(4, accountId);
	            ps1.executeUpdate();

	            // Update Staff
	            ps2.setInt(1, facilityId);
	            ps2.setInt(2, accountId);
	            ps2.executeUpdate();

	            conn.commit();
	            return true;
	            
			} catch (SQLException e) {
	            conn.rollback();
	            throw new DataAccessException("Failed to update staff info", e);
			}
			
		} catch (SQLException e) {
			throw new DataAccessException("Failed to update staff info", e);
		}
	}

	
//	@Override
//	public boolean updateAvatar(Integer accountId, String avatarPath) {
//		 
//		String sql = "UPDATE Account SET avatar_path = ? WHERE account_id = ?";
//		
//		try(Connection conn = DBContext.getConnection();
//				PreparedStatement ps = conn.prepareStatement(sql)) {
//			
//			ps.setString(1, avatarPath);
//			ps.setInt(2, accountId);
//			
//			int rowsAffected = ps.executeUpdate();
//			return rowsAffected > 0;
//			
//		} catch (SQLException e) {
//			throw new DataAccessException("Failed to update staff avatar", e);
//		}
//	}

	@Override
	public boolean softDeleteAndActive(Integer accountId) {
		
		String sql = "Update Account Set is_active = Case "
						+ "WHEN is_active = 1 THEN 0 "
						+ "ELSE 1 "
						+ "END "
					+ "Where account_id = ?";
		
		try(Connection conn = DBContext.getConnection();
				PreparedStatement ps = conn.prepareStatement(sql)) {
			
			ps.setInt(1, accountId);
			
			int rowsAffected = ps.executeUpdate();
			return rowsAffected > 0;
			
		} catch (SQLException e) {
			throw new DataAccessException("Failed to soft delete staff in detail", e);
		}
	}

}
