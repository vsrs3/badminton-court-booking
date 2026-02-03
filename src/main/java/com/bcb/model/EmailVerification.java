/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.bcb.model;
import java.sql.Timestamp;

public class EmailVerification {

    private int id;

    // dữ liệu đăng ký
    private String email;
    private String passwordHash;
    private String fullName;
    private String phone;
    private String role;

    // xác nhận
    private String token;
    private Timestamp expireAt;

    /* ======================
       LOGIC
       ====================== */
    public boolean isExpired() {
        return expireAt.before(new Timestamp(System.currentTimeMillis()));
    }

    /* ======================
       GETTERS & SETTERS
       ====================== */
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
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

    public String getRole() {
        return role;
    }
    public void setRole(String role) {
        this.role = role;
    }

    public String getToken() {
        return token;
    }
    public void setToken(String token) {
        this.token = token;
    }

    public Timestamp getExpireAt() {
        return expireAt;
    }
    public void setExpireAt(Timestamp expireAt) {
        this.expireAt = expireAt;
    }
}
