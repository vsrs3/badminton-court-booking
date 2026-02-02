package com.bcb.model;

/**
 * User roles in the system
 * Maps to 'role' column in Account table
 */
public enum Role {
    CUSTOMER("CUSTOMER", "Khách hàng"),
    STAFF("STAFF", "Nhân viên"),
    OWNER("OWNER", "Chủ sân"),
    ADMIN("ADMIN", "Quản trị viên");

    private final String code;
    private final String displayName;

    Role(String code, String displayName) {
        this.code = code;
        this.displayName = displayName;
    }

    public String getCode() {
        return code;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Convert database string to Role enum
     * @param code Role code from database (CUSTOMER, STAFF, OWNER, ADMIN)
     * @return Role enum or null if not found
     */
    public static Role fromCode(String code) {
        if (code == null) return null;

        for (Role role : Role.values()) {
            if (role.code.equalsIgnoreCase(code)) {
                return role;
            }
        }

        return null;
    }

    /**
     * Check if this role has admin privileges
     */
    public boolean isAdmin() {
        return this == ADMIN;
    }

    /**
     * Check if this role can manage facilities
     */
    public boolean canManageFacilities() {
        return this == OWNER || this == ADMIN;
    }

    /**
     * Check if this role can manage bookings
     */
    public boolean canManageBookings() {
        return this == STAFF || this == OWNER || this == ADMIN;
    }

    @Override
    public String toString() {
        return displayName;
    }
}