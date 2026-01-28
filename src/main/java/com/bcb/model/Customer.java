package com.bcb.model;

import java.time.LocalDateTime;

public class Customer {
    private int accountId;
    private String email;
    private String password;
    private String googleId;
    private String fullName;
    private String phone;
    private String avatarPath;
    private String role;       // OWNER | STAFF | USER
    private boolean isActive;
    private LocalDateTime createdAt;

    // Constructor không tham số
    public Customer() {
    }

    // Constructor đầy đủ
    public Customer(int accountId, String email, String password,
                    String googleId, String fullName, String phone,
                    String avatarPath, String role, boolean isActive,
                    LocalDateTime createdAt) {
        this.accountId = accountId;
        this.email = email;
        this.password = password;
        this.googleId = googleId;
        this.fullName = fullName;
        this.phone = phone;
        this.avatarPath = avatarPath;
        this.role = role;
        this.isActive = isActive;
        this.createdAt = createdAt;
    }

    // Getter & Setter
    public int getAccountId() {
        return accountId;
    }

    public void setAccountId(int accountId) {
        this.accountId = accountId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
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

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
