package com.bcb.repository;

import com.bcb.model.Account;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Admin Account Management
 */
public interface AccountManagementRepository {

    /**
     * Find all accounts with pagination (all roles).
     */
    List<Account> findAll(int limit, int offset);

    /**
     * Count total accounts.
     */
    int count();

    /**
     * Search accounts by keyword (name, email, phone) with pagination.
     */
    List<Account> findByKeyword(String keyword, int limit, int offset);

    /**
     * Count accounts matching keyword.
     */
    int countByKeyword(String keyword);

    /**
     * Search accounts by keyword and role filter with pagination.
     */
    List<Account> findByKeywordAndRole(String keyword, String role, int limit, int offset);

    /**
     * Count accounts matching keyword and role.
     */
    int countByKeywordAndRole(String keyword, String role);

    /**
     * Find accounts by role with pagination.
     */
    List<Account> findByRole(String role, int limit, int offset);

    /**
     * Count accounts by role.
     */
    int countByRole(String role);

    /**
     * Find account by ID (any role, any status).
     */
    Optional<Account> findById(Integer accountId);

    /**
     * Update account status (active/inactive).
     */
    boolean updateStatus(Integer accountId, boolean isActive);

    /**
     * Update account information (fullName, email, phone, role).
     */
    boolean updateAccountInfo(Integer accountId, String fullName, String email, String phone, String role);

    /**
     * Delete account permanently.
     */
    boolean deleteAccount(Integer accountId);

    /**
     * Check if email exists for another account (exclude given accountId).
     */
    boolean isEmailExistsForOther(String email, Integer excludeAccountId);

    /**
     * Check if phone exists for another account (exclude given accountId).
     */
    boolean isPhoneExistsForOther(String phone, Integer excludeAccountId);
}
