package com.bcb.model;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * User model - represents logged in user
 * Stored in HttpSession
 * Maps to Account table in database
 */
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer accountId;
    private String email;
    private String fullName;
    private String phone;
    private String avatarPath;
    private Role role;
    private Boolean isActive;
    private LocalDateTime createdAt;

    // For Owner/Staff - facility association
    private Integer facilityId;
    private String facilityName;

    // Constructors
    public User() {
    }

    /**
     * Constructor for basic user info (used in mock/session)
     */
    public User(Integer accountId, String email, String fullName, Role role) {
        this.accountId = accountId;
        this.email = email;
        this.fullName = fullName;
        this.role = role;
        this.isActive = true;
    }

    /**
     * Full constructor
     */
    public User(Integer accountId, String email, String fullName, String phone,
                String avatarPath, Role role, Boolean isActive, LocalDateTime createdAt) {
        this.accountId = accountId;
        this.email = email;
        this.fullName = fullName;
        this.phone = phone;
        this.avatarPath = avatarPath;
        this.role = role;
        this.isActive = isActive;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public Integer getAccountId() {
        return accountId;
    }

    public void setAccountId(Integer accountId) {
        this.accountId = accountId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAvatarPath() {
        return avatarPath;
    }

    public void setAvatarPath(String avatarPath) {
        this.avatarPath = avatarPath;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Integer getFacilityId() {
        return facilityId;
    }

    public void setFacilityId(Integer facilityId) {
        this.facilityId = facilityId;
    }

    public String getFacilityName() {
        return facilityName;
    }

    public void setFacilityName(String facilityName) {
        this.facilityName = facilityName;
    }

    // Convenience methods

    /**
     * Check if user is logged in (not null)
     */
    public static boolean isLoggedIn(User user) {
        return user != null && user.getIsActive();
    }

    /**
     * Check if user has specific role
     */
    public boolean hasRole(Role role) {
        return this.role == role;
    }

    /**
     * Check if user has any of the specified roles
     */
    public boolean hasAnyRole(Role... roles) {
        for (Role r : roles) {
            if (this.role == r) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get display name for UI
     */
    public String getDisplayName() {
        if (fullName != null && !fullName.isEmpty()) {
            return fullName;
        }
        if (email != null && !email.isEmpty()) {
            return email.split("@")[0];
        }
        return "User #" + accountId;
    }

    /**
     * Get first name
     */
    public String getFirstName() {
        if (fullName != null && !fullName.isEmpty()) {
            String[] parts = fullName.split("\\s+");
            return parts[parts.length - 1]; // Vietnamese: last word is first name
        }
        return getDisplayName();
    }

    @Override
    public String toString() {
        return "User{" +
                "accountId=" + accountId +
                ", email='" + email + '\'' +
                ", fullName='" + fullName + '\'' +
                ", role=" + role +
                ", isActive=" + isActive +
                '}';
    }
}