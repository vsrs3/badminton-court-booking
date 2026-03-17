package com.bcb.service.impl;

import com.bcb.dto.RegisterRequestDTO;
import com.bcb.exception.BusinessException;
import com.bcb.model.Account;
import com.bcb.model.EmailVerification;
import com.bcb.model.PasswordResetToken;
import com.bcb.repository.AccountRepository;
import com.bcb.repository.EmailVerificationRepository;
import com.bcb.repository.PasswordResetTokenRepository;
import com.bcb.repository.impl.AccountRepositoryImpl;
import com.bcb.repository.impl.EmailVerificationRepositoryImpl;
import com.bcb.repository.impl.PasswordResetTokenRepositoryImpl;
import com.bcb.service.AuthService;
import com.bcb.utils.MailUtil;
import org.mindrot.jbcrypt.BCrypt;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.text.Normalizer;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

public class AuthServiceImpl implements AuthService {
    private static final int MIN_PASSWORD_LENGTH = 6;
    private static final int FULL_NAME_MAX_LENGTH = 255;
    private static final long REGISTER_TOKEN_TTL_MS = 60 * 1000L;
    private static final long PASSWORD_RESET_TOKEN_TTL_MS = 15 * 60 * 1000L;
    private static final Pattern FULL_NAME_PATTERN = Pattern.compile("^[\\p{L}\\s]+$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\d{10}$");

    private final AccountRepository accountRepository;
    private final EmailVerificationRepository emailVerificationRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;

    public AuthServiceImpl() {
        this(
                new AccountRepositoryImpl(),
                new EmailVerificationRepositoryImpl(),
                new PasswordResetTokenRepositoryImpl()
        );
    }

    public AuthServiceImpl(AccountRepository accountRepository,
                           EmailVerificationRepository emailVerificationRepository) {
        this(accountRepository, emailVerificationRepository, new PasswordResetTokenRepositoryImpl());
    }

    public AuthServiceImpl(AccountRepository accountRepository,
                           EmailVerificationRepository emailVerificationRepository,
                           PasswordResetTokenRepository passwordResetTokenRepository) {
        this.accountRepository = accountRepository;
        this.emailVerificationRepository = emailVerificationRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
    }

    @Override
    public Account authenticate(String email, String password) {
        Optional<Account> accountOpt = accountRepository.findByEmail(email);
        if (accountOpt.isEmpty()) {
            throw new RuntimeException("Invalid credentials");
        }

        Account account = accountOpt.get();
        if (!account.getIsActive()) {
            throw new RuntimeException("Account is inactive");
        }

        if (!BCrypt.checkpw(password, account.getPasswordHash())) {
            throw new RuntimeException("Invalid credentials");
        }

        return account;
    }

    @Override
    public String register(RegisterRequestDTO dto) throws Exception {
        normalizeRegisterRequest(dto);
        validateRegisterRequest(dto);

        if (accountRepository.isEmailExists(dto.getEmail())) {
            throw new BusinessException("Email đã tồn tại");
        }

        if (accountRepository.isPhoneExists(dto.getPhone())) {
            throw new BusinessException("Số điện thoại đã tồn tại");
        }

        String hash = BCrypt.hashpw(dto.getPassword(), BCrypt.gensalt());
        String token = UUID.randomUUID().toString();
        Timestamp expireAt = new Timestamp(System.currentTimeMillis() + REGISTER_TOKEN_TTL_MS);

        emailVerificationRepository.savePendingRegister(
                dto.getEmail(),
                hash,
                dto.getFullName(),
                dto.getPhone(),
                "CUSTOMER",
                token,
                expireAt
        );

        String verifyLink = "http://localhost:8080/badminton_court_booking/verify-email?token=" + token;
        MailUtil.sendVerifyEmail(dto.getEmail(), verifyLink);
        return token;
    }

    @Override
    public void verifyEmail(String token) throws Exception {
        EmailVerification emailVerification = emailVerificationRepository.findByToken(token);
        if (emailVerification == null) {
            throw new BusinessException("Token không hợp lệ");
        }

        if (emailVerification.isExpired()) {
            emailVerificationRepository.deleteByToken(token);
            throw new BusinessException("Token đã hết hạn");
        }

        if (accountRepository.findByEmail(emailVerification.getEmail()).isPresent()) {
            emailVerificationRepository.deleteByToken(token);
            return;
        }

        Account account = new Account();
        account.setEmail(emailVerification.getEmail());
        account.setPasswordHash(emailVerification.getPasswordHash());
        account.setFullName(emailVerification.getFullName());
        account.setPhone(emailVerification.getPhone());
        account.setRole(emailVerification.getRole());

        accountRepository.register(account);
        emailVerificationRepository.deleteByToken(token);
    }

    @Override
    public void forgotPassword(String email, String resetLinkBase) throws BusinessException {
        String normalizedEmail = trimToEmpty(email);
        if (normalizedEmail.isEmpty()) {
            throw new BusinessException("Vui lòng nhập email.");
        }

        if (!accountRepository.existsByEmail(normalizedEmail)) {
            throw new BusinessException("Email không tồn tại.");
        }

        String token = UUID.randomUUID().toString();
        Timestamp expireAt = new Timestamp(System.currentTimeMillis() + PASSWORD_RESET_TOKEN_TTL_MS);

        passwordResetTokenRepository.deleteExpiredTokens();
        passwordResetTokenRepository.deleteByEmail(normalizedEmail);
        passwordResetTokenRepository.save(normalizedEmail, token, expireAt);

        String resetLink = resetLinkBase + "?token=" + URLEncoder.encode(token, StandardCharsets.UTF_8);
        try {
            MailUtil.sendPasswordResetEmail(normalizedEmail, resetLink);
        } catch (RuntimeException e) {
            passwordResetTokenRepository.deleteByToken(token);
            throw new BusinessException("Không thể gửi email xác nhận lúc này. Vui lòng thử lại sau.");
        }
    }

    @Override
    public String getPasswordResetEmail(String token) throws BusinessException {
        return getValidPasswordResetToken(token).getEmail();
    }

    @Override
    public void resetPassword(String token, String password) throws BusinessException {
        if (password == null || password.length() < MIN_PASSWORD_LENGTH) {
            throw new BusinessException("Mật khẩu phải có ít nhất 6 ký tự.");
        }

        PasswordResetToken passwordResetToken = getValidPasswordResetToken(token);
        String hash = BCrypt.hashpw(password, BCrypt.gensalt());
        accountRepository.updatePassword(passwordResetToken.getEmail(), hash);
        passwordResetTokenRepository.deleteByToken(token);
    }

    @Override
    public boolean verifyPassword(String plainPassword, String hashedPassword) {
        return plainPassword != null
                && hashedPassword != null
                && BCrypt.checkpw(plainPassword, hashedPassword);
    }

    @Override
    public String hashPassword(String plainPassword) {
        if (plainPassword == null || plainPassword.isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }

        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(10));
    }

    private void normalizeRegisterRequest(RegisterRequestDTO dto) {
        dto.setEmail(trimToEmpty(dto.getEmail()));
        dto.setPhone(normalizePhone(dto.getPhone()));
        dto.setFullName(normalizeFullName(dto.getFullName()));
    }

    private void validateRegisterRequest(RegisterRequestDTO dto) throws BusinessException {
        if (dto.getEmail().isEmpty()) {
            throw new BusinessException("Vui lòng nhập email");
        }

        if (dto.getPassword() == null || dto.getPassword().length() < MIN_PASSWORD_LENGTH) {
            throw new BusinessException("Mật khẩu phải có ít nhất 6 ký tự");
        }

        if (dto.getFullName().isEmpty()) {
            throw new BusinessException("Vui lòng nhập họ và tên");
        }

        if (dto.getFullName().length() > FULL_NAME_MAX_LENGTH) {
            throw new BusinessException("Họ và tên không được vượt quá 255 ký tự");
        }

        if (!FULL_NAME_PATTERN.matcher(dto.getFullName()).matches()) {
            throw new BusinessException("Họ và tên chỉ được chứa chữ cái và khoảng trắng");
        }

        if (dto.getPhone().isEmpty()) {
            throw new BusinessException("Vui lòng nhập số điện thoại");
        }

        if (!PHONE_PATTERN.matcher(dto.getPhone()).matches()) {
            throw new BusinessException("Số điện thoại phải gồm đúng 10 chữ số");
        }
    }

    private PasswordResetToken getValidPasswordResetToken(String token) throws BusinessException {
        String normalizedToken = trimToEmpty(token);
        if (normalizedToken.isEmpty()) {
            throw new BusinessException("Liên kết đổi mật khẩu không hợp lệ.");
        }

        PasswordResetToken passwordResetToken = passwordResetTokenRepository.findByToken(normalizedToken);
        if (passwordResetToken == null) {
            throw new BusinessException("Liên kết đổi mật khẩu không hợp lệ hoặc đã hết hạn.");
        }

        if (passwordResetToken.isExpired()) {
            passwordResetTokenRepository.deleteByToken(normalizedToken);
            throw new BusinessException("Liên kết đổi mật khẩu đã hết hạn.");
        }

        if (!accountRepository.existsByEmail(passwordResetToken.getEmail())) {
            passwordResetTokenRepository.deleteByToken(normalizedToken);
            throw new BusinessException("Liên kết đổi mật khẩu không hợp lệ hoặc đã hết hạn.");
        }

        return passwordResetToken;
    }

    private String normalizeFullName(String fullName) {
        if (fullName == null) {
            return "";
        }

        return Normalizer.normalize(fullName, Normalizer.Form.NFC)
                .replaceAll("\\s+", " ")
                .trim();
    }

    private String normalizePhone(String phone) {
        if (phone == null) {
            return "";
        }

        return phone.replaceAll("\\s+", "").trim();
    }

    private String trimToEmpty(String value) {
        return value == null ? "" : value.trim();
    }
}
