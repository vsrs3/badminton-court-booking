package com.bcb.service.impl;

import com.bcb.model.Account;
import com.bcb.repository.AccountManagementRepository;
import com.bcb.repository.impl.AccountManagementRepositoryImpl;
import com.bcb.service.AccountManagementService;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Implementation of AccountManagementService
 */
public class AccountManagementServiceImpl implements AccountManagementService {

    private final AccountManagementRepository repo = new AccountManagementRepositoryImpl();

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );
    private static final Pattern PHONE_PATTERN = Pattern.compile(
            "^(0|\\+84)(3|5|7|8|9)[0-9]{8}$"
    );
    private static final Set<String> VALID_ROLES = Set.of("ADMIN", "OWNER", "STAFF", "CUSTOMER");

    @Override
    public List<Account> findAll(int limit, int offset) {
        return repo.findAll(limit, offset);
    }

    @Override
    public int count() {
        return repo.count();
    }

    @Override
    public List<Account> findByKeyword(String keyword, int limit, int offset) {
        return repo.findByKeyword(keyword, limit, offset);
    }

    @Override
    public int countByKeyword(String keyword) {
        return repo.countByKeyword(keyword);
    }

    @Override
    public List<Account> findByKeywordAndRole(String keyword, String role, int limit, int offset) {
        return repo.findByKeywordAndRole(keyword, role, limit, offset);
    }

    @Override
    public int countByKeywordAndRole(String keyword, String role) {
        return repo.countByKeywordAndRole(keyword, role);
    }

    @Override
    public List<Account> findByRole(String role, int limit, int offset) {
        return repo.findByRole(role, limit, offset);
    }

    @Override
    public int countByRole(String role) {
        return repo.countByRole(role);
    }

    @Override
    public Optional<Account> findById(Integer accountId) {
        if (accountId == null) {
            throw new IllegalArgumentException("Account ID cannot be null");
        }
        return repo.findById(accountId);
    }

    @Override
    public boolean updateStatus(Integer accountId, boolean isActive) {
        if (accountId == null) {
            throw new IllegalArgumentException("Account ID cannot be null");
        }
        return repo.updateStatus(accountId, isActive);
    }

    @Override
    public boolean toggleStatus(Integer accountId) {
        if (accountId == null) {
            throw new IllegalArgumentException("Account ID cannot be null");
        }
        Optional<Account> opt = repo.findById(accountId);
        if (opt.isEmpty()) {
            return false;
        }
        Account account = opt.get();
        boolean newStatus = !account.getIsActive();
        return repo.updateStatus(accountId, newStatus);
    }

    @Override
    public String updateAccountInfo(Integer accountId, String fullName, String email, String phone, String role) {
        if (accountId == null) {
            return "Account ID không hợp lệ";
        }

        // Validate fullName
        if (fullName == null || fullName.trim().isEmpty()) {
            return "Họ và tên không được để trống";
        }
        fullName = fullName.trim();
        if (fullName.length() < 2 || fullName.length() > 100) {
            return "Họ và tên phải từ 2 đến 100 ký tự";
        }

        // Validate email
        if (email == null || email.trim().isEmpty()) {
            return "Email không được để trống";
        }
        email = email.trim();
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            return "Email không đúng định dạng";
        }

        // Validate phone
        if (phone == null || phone.trim().isEmpty()) {
            return "Số điện thoại không được để trống";
        }
        phone = phone.trim();
        if (!PHONE_PATTERN.matcher(phone).matches()) {
            return "Số điện thoại không đúng định dạng (VD: 0912345678)";
        }

        // Validate role
        if (role == null || role.trim().isEmpty()) {
            return "Vai trò không được để trống";
        }
        role = role.trim().toUpperCase();
        if (!VALID_ROLES.contains(role)) {
            return "Vai trò không hợp lệ";
        }

        // Check email uniqueness
        if (repo.isEmailExistsForOther(email, accountId)) {
            return "Email đã được sử dụng bởi tài khoản khác";
        }

        // Check phone uniqueness
        if (repo.isPhoneExistsForOther(phone, accountId)) {
            return "Số điện thoại đã được sử dụng bởi tài khoản khác";
        }

        boolean success = repo.updateAccountInfo(accountId, fullName, email, phone, role);
        return success ? null : "Cập nhật thất bại, vui lòng thử lại";
    }

    @Override
    public boolean deleteAccount(Integer accountId) {
        if (accountId == null) {
            throw new IllegalArgumentException("Account ID cannot be null");
        }
        return repo.deleteAccount(accountId);
    }
}
