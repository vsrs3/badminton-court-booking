package com.bcb.repository;

import com.bcb.model.EmailVerification;
import java.sql.Timestamp;

public interface EmailVerificationRepository {

    void savePendingRegister(
            String email,
            String passwordHash,
            String fullName,
            String phone,
            String role,
            String token,
            Timestamp expireAt
    );

    EmailVerification findByToken(String token);

    void deleteByToken(String token);

    void deleteExpiredTokens();
}
