package com.bcb.service.impl;

import com.bcb.model.Account;
import com.bcb.repository.AccountRepository;
import com.bcb.repository.impl.AccountRepositoryImpl;
import com.bcb.service.AuthService;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;

/**
 * Implementation of AuthService
 */
public class AuthServiceImpl implements AuthService {

    private final AccountRepository accountRepository;

    public AuthServiceImpl() {
        this.accountRepository = new AccountRepositoryImpl();
    }

    // Constructor for dependency injection (testing)
    public AuthServiceImpl(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Override
    public Account authenticate(String email, String password) {
        System.out.println("🔐 Authenticating user: " + email);

        // Find account by email
        Optional<Account> accountOpt = accountRepository.findByEmail(email);

        if (accountOpt.isEmpty()) {
            System.out.println("❌ Account not found: " + email);
            throw new RuntimeException("Invalid credentials");
        }

        Account account = accountOpt.get();

        // Check if account is active
        if (!account.getIsActive()) {
            System.out.println("❌ Account is inactive: " + email);
            throw new RuntimeException("Account is inactive");
        }

        // Verify password
        String hashedPassword = hashPassword(password);
        if (!hashedPassword.equals(account.getPasswordHash())) {
            System.out.println("❌ Invalid password for: " + email);
            throw new RuntimeException("Invalid credentials");
        }

        System.out.println("✅ Authentication successful: " + email + " (Role: " + account.getRole() + ")");
        return account;
    }

    @Override
    public boolean verifyPassword(String plainPassword, String hashedPassword) {
        String hashToCheck = hashPassword(plainPassword);
        return hashToCheck.equals(hashedPassword);
    }

    @Override
    public String hashPassword(String plainPassword) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(plainPassword.getBytes(StandardCharsets.UTF_8));

            // Convert byte array to hex string
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }

            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error hashing password: " + e.getMessage(), e);
        }
    }
}