package com.bcb.service;

import java.util.*;
import com.bcb.model.Account;

public interface StaffService {
	/**
	 * Find all users with pagination.
	 * @param limit Number of records to return
	 * @param offset Starting position
	 * @return List of users
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
}
