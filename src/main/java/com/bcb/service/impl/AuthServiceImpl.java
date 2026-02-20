package com.bcb.service.impl;

import com.bcb.dao.EmailVerificationDAO;
import com.bcb.dto.RegisterRequestDTO;
import com.bcb.exception.BusinessException;
import com.bcb.model.Account;
import com.bcb.model.EmailVerification;
import com.bcb.repository.AccountRepository;
import com.bcb.repository.EmailVerificationRepository;
import com.bcb.repository.impl.AccountRepositoryImpl;
import com.bcb.repository.impl.EmailVerificationRepositoryImpl;
import com.bcb.service.AuthService;
import com.bcb.utils.MailUtil;
import org.mindrot.jbcrypt.BCrypt;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.util.Optional;
import java.util.UUID;

/**
 * Implementation of AuthService
 */
public class AuthServiceImpl implements AuthService {


    private final AccountRepository accountRepository;
    private final EmailVerificationRepository emailVerificationRepository;
    public AuthServiceImpl() {
        this.accountRepository = new AccountRepositoryImpl();
        this.emailVerificationRepository = new EmailVerificationRepositoryImpl();
    }

    // Constructor for dependency injection (testing)
    public AuthServiceImpl(AccountRepository accountRepository, EmailVerificationRepository emailVerificationRepository) {
        this.accountRepository = accountRepository;
        this.emailVerificationRepository = emailVerificationRepository;
    }

    @Override
    public Account authenticate(String email, String password) {
        System.out.println("🔐 Authenticating user: " + email);

        // Find account by email
        Optional<Account> accountOpt = accountRepository.findByEmail(email);

        if (accountOpt.isEmpty()) {
            System.out.println("❌ Account not found: " + email);
            throw new RuntimeException("Invalid credentials");
        }

        Account account = accountOpt.get();

        // Check if account is active
        if (!account.getIsActive()) {
            System.out.println("❌ Account is inactive: " + email);
            throw new RuntimeException("Account is inactive");
        }

        // Verify password
//        String hashedPassword = hashPassword(password);
//        System.out.println("CHẸKCE" + hashedPassword);
//        System.out.println("Pass" + password);

        if (!BCrypt.checkpw(password, account.getPasswordHash())) {
            System.out.println("❌ Invalid password for: " + email);
            throw new RuntimeException("Invalid credentials");
        }

        System.out.println("✅ Authentication successful: " + email + " (Role: " + account.getRole() + ")");
        return account;
    }

    @Override
    public void register(RegisterRequestDTO dto) throws Exception {

        if (accountRepository.isEmailExists(dto.getEmail())) {
            throw new BusinessException("Email đã tồn tại");
        }

        String hash = BCrypt.hashpw(dto.getPassword(), BCrypt.gensalt());

        String token = UUID.randomUUID().toString();
        Timestamp expireAt = new Timestamp(
                System.currentTimeMillis() + 60 * 1000
        );

        emailVerificationRepository.savePendingRegister(
                dto.getEmail(),
                hash,
                dto.getFullName(),
                dto.getPhone(),
                "CUSTOMER",
                token,
                expireAt
        );

        String verifyLink =
                "http://localhost:8080/bcb/verify-email?token=" + token;

        MailUtil.sendVerifyEmail(dto.getEmail(), verifyLink);
    }

    @Override
    public void verifyEmail(String token) throws Exception {

        EmailVerification ev =
                emailVerificationRepository.findByToken(token);

        if (ev == null)
            throw new BusinessException("Token không hợp lệ");

        if (ev.isExpired()) {
            emailVerificationRepository.deleteByToken(token);
            throw new BusinessException("Token hết hạn");
        }

        if (accountRepository.findByEmail(ev.getEmail()).isPresent()) {
            emailVerificationRepository.deleteByToken(token);
            return;
        }

        Account acc = new Account();
        acc.setEmail(ev.getEmail());
        acc.setPasswordHash(ev.getPasswordHash());
        acc.setFullName(ev.getFullName());
        acc.setPhone(ev.getPhone());
        acc.setRole(ev.getRole());

        accountRepository.register(acc);
        emailVerificationRepository.deleteByToken(token);
    }


    @Override
    public void forgotPassword(String email) throws BusinessException {
        if (!accountRepository.existsByEmail(email)) {
            throw new BusinessException("Email không tồn tại.");
        }
    }


    @Override
    public void resetPassword(String email, String password) throws BusinessException {
        if (!accountRepository.isEmailExists(email))
            throw new BusinessException("Email không tồn tại");
        String hash =
                BCrypt.hashpw(password, BCrypt.gensalt());
        accountRepository.updatePassword(email, hash);
    }

    @Override
    public boolean verifyPassword(String plainPassword, String hashedPassword) {
        String hashToCheck = hashPassword(plainPassword);
        return hashToCheck.equals(hashedPassword);
    }

    @Override
    public String hashPassword(String plainPassword) {
        if (plainPassword == null || plainPassword.isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }

        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(10));
    }

}

