package com.bcb.model;

import java.time.LocalDateTime;

/**
 * Entity representing Account table (Pure DB mapping)
 * Maps to: Account table in database
 */
public class Account {

    // ============================================
    // DATABASE FIELDS ONLY
    // ============================================

    private Integer accountId;
    private String email;
    private String passwordHash;
    private String googleId;
    private String fullName;
    private String phone;
    private String avatarPath;
    private String role; // ADMIN, OWNER, STAFF, USER
    private Boolean isActive;
    private LocalDateTime createdAt;

    // ============================================
    // CONSTRUCTORS
    // ============================================

    public Account() {
    }

    public Account(Integer accountId, String email, String passwordHash, String fullName,
                   String phone, String role, Boolean isActive) {
        this.accountId = accountId;
        this.email = email;
        this.passwordHash = passwordHash;
        this.fullName = fullName;
        this.phone = phone;
        this.role = role;
        this.isActive = isActive;
    }

    // ============================================
    // GETTERS AND SETTERS
    // ============================================

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

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getGoogleId() {
        return googleId;
    }

    public void setGoogleId(String googleId) {
        this.googleId = googleId;
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

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
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

    @Override
    public String toString() {
        return "Account{" +
                "accountId=" + accountId +
                ", email='" + email + '\'' +
                ", fullName='" + fullName + '\'' +
                ", role='" + role + '\'' +
                ", isActive=" + isActive +
                '}';
    }
}