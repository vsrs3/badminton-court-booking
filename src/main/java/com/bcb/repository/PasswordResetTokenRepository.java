package com.bcb.repository;

import com.bcb.model.PasswordResetToken;

import java.sql.Timestamp;

public interface PasswordResetTokenRepository {

    void save(String email, String token, Timestamp expireAt);

    PasswordResetToken findByToken(String token);

    void deleteByToken(String token);

    void deleteByEmail(String email);

    void deleteExpiredTokens();

}
