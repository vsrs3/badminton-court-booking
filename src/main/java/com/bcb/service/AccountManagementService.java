package com.bcb.service;

import com.bcb.model.Account;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for Admin Account Management
 */
public interface AccountManagementService {

    List<Account> findAll(int limit, int offset);

    int count();

    List<Account> findByKeyword(String keyword, int limit, int offset);

    int countByKeyword(String keyword);

    List<Account> findByKeywordAndRole(String keyword, String role, int limit, int offset);

    int countByKeywordAndRole(String keyword, String role);

    List<Account> findByRole(String role, int limit, int offset);

    int countByRole(String role);

    Optional<Account> findById(Integer accountId);

    boolean updateStatus(Integer accountId, boolean isActive);

    boolean toggleStatus(Integer accountId);

    /**
     * Update account info with validation.
     * @return null if success, error message if validation fails
     */
    String updateAccountInfo(Integer accountId, String fullName, String email, String phone, String role);

    boolean deleteAccount(Integer accountId);
}

