package com.bcb.model;

import java.sql.Timestamp;

public class PasswordResetToken {

    private int id;
    private String email;
    private String token;
    private Timestamp expireAt;

    public boolean isExpired() {
        return expireAt == null || expireAt.before(new Timestamp(System.currentTimeMillis()));
    }

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
