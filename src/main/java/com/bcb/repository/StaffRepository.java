package com.bcb.repository;

import java.util.*;
import com.bcb.model.Staff;
import com.bcb.model.Account;
import com.bcb.model.Facility;

public interface StaffRepository {
	
	/**
	 * Get all staff accounts
	 * @return List of staff accounts
	 */
	List<Account> findAll(int limit, int offset);

	/**
	 * Count total users.
	 * @return Total count
	 */
	int count();

	/**
	 * Update user status (active/inactive).
	 * @param accountId User ID
	 * @param isActive New status
	 * @return true if update successful, false otherwise
	 */
	boolean updateStatus(Integer accountId, boolean isActive);
	
	/**
	 * Search users by keyword across username and email fields.
	 * @param keyword Keyword to search
	 * @param limit Number of records to return
	 * @param offset Starting position
	 * @return List of matching users
	 */
	List<Account> findByKeyword(String keyword, int limit, int offset);
	
	/**
	 * Count users by keyword search across username and email fields.
	 * @param keyword Keyword to search
	 * @return Total count
	 */
	int countByKeyword(String keyword);
	
	/**
	 * Find user by ID.
	 * @param accountId User ID
	 * @return Optional containing user or empty
	 */
	Optional<Account> findById(Integer accountId);
	
	
	/**
	 * Find facilities associated with a user by account ID.
	 * @param accountId User ID
	 * @return List of facilities associated with the user
	 */
	List<Facility> findFacilitiesById( Integer accountId);
	
	/**
	 * Get all facilities for dropdown selection.
	 * @return List of all facilities
	 */
	List<Facility> findAllFacilities();
}
