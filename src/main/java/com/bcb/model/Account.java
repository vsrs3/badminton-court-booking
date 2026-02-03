package com.bcb.model;

import java.time.LocalDateTime;

public class Account {
    private Integer accountId;
    private String email;
    private String passwordHash;
    private String googleId;
    private String fullName;
    private String phone;
    private String avatarPath;
    private String role;
    private Boolean isActive;
    private LocalDateTime createdAt;

    public Account() {}

    public Account(Integer accountId, String email, String passwordHash,
                   String googleId, String fullName, String phone, String avatarPath,
                   String role, Boolean isActive, LocalDateTime createdAt) {
        this.accountId = accountId;
        this.email = email;
        this.passwordHash = passwordHash;
        this.googleId = googleId;
        this.fullName = fullName;
        this.phone = phone;
        this.avatarPath = avatarPath;
        this.role = role;
        this.isActive = isActive;
        this.createdAt = createdAt;
    }

    public Integer getAccountId() { return accountId; }
    public void setAccountId(Integer accountId) { this.accountId = accountId; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public String getGoogleId() { return googleId; }
    public void setGoogleId(String googleId) { this.googleId = googleId; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getAvatarPath() { return avatarPath; }
    public void setAvatarPath(String avatarPath) { this.avatarPath = avatarPath; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}