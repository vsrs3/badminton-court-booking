package com.bcb.repository;

import java.util.List;
import com.bcb.model.Facility;

public interface StaffProfileRepository {
	
	/**
	 * Update staff information (full name, email, phone number).
	 * @param accountId User ID
	 * @param fullName New full name
	 * @param email New email
	 * @param phoneNumber New phone number
	 * @return true if update successful, false otherwise
	 */
	boolean  updateInfo(Integer accountId, Integer facilityId, String fullName, String email, String phoneNumber);
	
	
	/**
	 * Update staff avatar.
	 * @param accountId User ID
	 * @param avatarPath New avatar path
	 * @return true if update successful, false otherwise
	 */
	//boolean updateAvatar(Integer accountId, String avatarPath);
	
	/**
	 * Soft delete or activate staff account.
	 * @param accountId User ID
	 * @return true if operation successful, false otherwise
	 */
	boolean softDeleteAndActive(Integer accountId);
}
