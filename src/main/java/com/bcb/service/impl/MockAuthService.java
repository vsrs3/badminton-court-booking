package com.bcb.service.impl;

import com.bcb.model.Role;
import com.bcb.model.User;

import java.util.HashMap;
import java.util.Map;

/**
 * Mock Authentication Service for development
 * Simulates login without hitting real database
 *
 * Test accounts:
 * - customer@test.com / password123 → CUSTOMER
 * - staff@test.com / password123 → STAFF
 * - owner@test.com / password123 → OWNER
 * - admin@test.com / password123 → ADMIN
 */
public class MockAuthService {

    // Mock users database
    private static final Map<String, MockAccount> MOCK_ACCOUNTS = new HashMap<>();

    static {
        // Initialize mock accounts
        MOCK_ACCOUNTS.put("customer@test.com",
                new MockAccount(1, "customer@test.com", "password123", "Nguyễn Văn A", "0987654321", Role.CUSTOMER));

        MOCK_ACCOUNTS.put("staff@test.com",
                new MockAccount(2, "staff@test.com", "password123", "Trần Thị B", "0987654322", Role.STAFF, 1, "CLB Cầu Lông TPT Sport"));

        MOCK_ACCOUNTS.put("owner@test.com",
                new MockAccount(3, "owner@test.com", "password123", "Lê Văn C", "0987654323", Role.OWNER, 1, "CLB Cầu Lông TPT Sport"));

        MOCK_ACCOUNTS.put("admin@test.com",
                new MockAccount(4, "admin@test.com", "password123", "Phạm Thị D", "0987654324", Role.ADMIN));
    }

    /**
     * Mock login
     * @param email Email
     * @param password Password
     * @return User object if successful, null otherwise
     */
    public static User login(String email, String password) {
        System.out.println("🔓 MockAuthService.login() called with email: " + email);

        MockAccount account = MOCK_ACCOUNTS.get(email);

        if (account == null) {
            System.out.println("❌ Account not found: " + email);
            return null;
        }

        if (!account.password.equals(password)) {
            System.out.println("❌ Invalid password for: " + email);
            return null;
        }

        System.out.println("✅ Login successful: " + email + " (" + account.role + ")");

        // Convert to User object
        User user = new User(
                account.accountId,
                account.email,
                account.fullName,
                account.role
        );

        user.setPhone(account.phone);
        user.setFacilityId(account.facilityId);
        user.setFacilityName(account.facilityName);
        user.setIsActive(true);

        return user;
    }

    /**
     * Check if email exists
     */
    public static boolean emailExists(String email) {
        return MOCK_ACCOUNTS.containsKey(email);
    }

    /**
     * Get all mock accounts (for testing/debug)
     */
    public static Map<String, MockAccount> getAllAccounts() {
        return new HashMap<>(MOCK_ACCOUNTS);
    }

    /**
     * Inner class for mock account data
     */
    public static class MockAccount {
        public final Integer accountId;
        public final String email;
        public final String password;
        public final String fullName;
        public final String phone;
        public final Role role;
        public final Integer facilityId;
        public final String facilityName;

        public MockAccount(Integer accountId, String email, String password,
                           String fullName, String phone, Role role) {
            this(accountId, email, password, fullName, phone, role, null, null);
        }

        public MockAccount(Integer accountId, String email, String password,
                           String fullName, String phone, Role role,
                           Integer facilityId, String facilityName) {
            this.accountId = accountId;
            this.email = email;
            this.password = password;
            this.fullName = fullName;
            this.phone = phone;
            this.role = role;
            this.facilityId = facilityId;
            this.facilityName = facilityName;
        }

        @Override
        public String toString() {
            return "MockAccount{" +
                    "email='" + email + '\'' +
                    ", fullName='" + fullName + '\'' +
                    ", role=" + role +
                    '}';
        }
    }
}