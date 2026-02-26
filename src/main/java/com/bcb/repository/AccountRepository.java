package com.bcb.repository;

import com.bcb.model.Account;

import java.sql.SQLException;
import java.util.Optional;

/**
 * Repository interface for Account data access
 */
public interface AccountRepository {

    /**
     * Find account by email
     *
     * @param email User's email
     * @return Optional containing account if found
     */
    Optional<Account> findByEmail(String email);

    /**
     * Find account by ID
     *
     * @param accountId Account ID
     * @return Optional containing account if found
     */
    Optional<Account> findById(Integer accountId);

    /**
     * Create new account
     *
     * @param account Account to create
     * @return Created account with ID
     */
    Account create(Account account);

    /**
     * Update existing account
     *
     * @param account Account to update
     * @return Updated account
     */
    Account update(Account account);

    /**
     * Check if email exists
     *
     * @param email Email to check
     * @return true if exists
     */

    public Account findByEmailAnyStatus(String email) throws SQLException;

    boolean existsByEmail(String email);

    boolean isPhoneExists(String phone);

    boolean isEmailExists(String email);

    void register(Account acc);

    void registerByGoogle(Account acc);

    Account findByGoogleId(String googleId);

    void updateGoogleId(int accountId, String googleId);

    Account loginByEmailPassword(String email, String rawPassword);

    void updatePassword(String email, String newHashedPassword);
}