package com.bcb.service;

import com.bcb.model.Account;

/**
 * Service interface for authentication business logic
 */
public interface AuthService {

    /**
     * Authenticate user with email and password
     *
     * @param email User's email
     * @param password Plain text password
     * @return Account if authentication successful
     * @throws RuntimeException if credentials invalid
     */
    Account authenticate(String email, String password);

    /**
     * Verify password against hash
     *
     * @param plainPassword Plain text password
     * @param hashedPassword Hashed password from database
     * @return true if password matches
     */
    boolean verifyPassword(String plainPassword, String hashedPassword);

    /**
     * Hash password for storage
     *
     * @param plainPassword Plain text password
     * @return Hashed password
     */
    String hashPassword(String plainPassword);
}