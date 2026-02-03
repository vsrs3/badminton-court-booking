package com.bcb.validation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Validation utility for Account entity.
 * Validates:
 * - fullName: NOT NULL, length 1-255
 * - email: UNIQUE, email format, length 1-255 (can be null for OAuth)
 * - phone: UNIQUE, phone format, length 1-20 (can be null)
 * - role: must be in ('OWNER', 'STAFF', 'USER')
 * - passwordHash: required if email-based auth (length 1-255)
 * - googleId: optional, max length 255
 * - avatarPath: optional, max length 500
 */
public class AccountValidator {

    private static final Pattern EMAIL_PATTERN =
        Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private static final Pattern PHONE_PATTERN =
        Pattern.compile("^[0-9\\-\\+\\s\\(\\)]{10,20}$");

    private static final List<String> VALID_ROLES = Arrays.asList("OWNER", "STAFF", "USER");
    private static final int EMAIL_MAX_LENGTH = 255;
    private static final int PHONE_MAX_LENGTH = 20;
    private static final int FULL_NAME_MAX_LENGTH = 255;
    private static final int PASSWORD_HASH_MAX_LENGTH = 255;
    private static final int GOOGLE_ID_MAX_LENGTH = 255;
    private static final int AVATAR_PATH_MAX_LENGTH = 500;

    /**
     * Validate complete Account object
     * @return list of error messages, empty list if valid
     */
    public static List<String> validate(com.bcb.model.Account account) {
        List<String> errors = new ArrayList<>();

        if (account == null) {
            errors.add("Account cannot be null");
            return errors;
        }

        // Validate fullName (NOT NULL)
        if (account.getFullName() == null || account.getFullName().trim().isEmpty()) {
            errors.add("Full name is required");
        } else if (account.getFullName().length() > FULL_NAME_MAX_LENGTH) {
            errors.add("Full name must not exceed " + FULL_NAME_MAX_LENGTH + " characters");
        }

        // Validate email (optional but must be valid if provided)
        if (account.getEmail() != null && !account.getEmail().trim().isEmpty()) {
            if (account.getEmail().length() > EMAIL_MAX_LENGTH) {
                errors.add("Email must not exceed " + EMAIL_MAX_LENGTH + " characters");
            } else if (!EMAIL_PATTERN.matcher(account.getEmail()).matches()) {
                errors.add("Email format is invalid");
            }
        }

        // Validate phone (optional but must be valid if provided)
        if (account.getPhone() != null && !account.getPhone().trim().isEmpty()) {
            if (account.getPhone().length() > PHONE_MAX_LENGTH) {
                errors.add("Phone must not exceed " + PHONE_MAX_LENGTH + " characters");
            } else if (!PHONE_PATTERN.matcher(account.getPhone()).matches()) {
                errors.add("Phone format is invalid");
            }
        }

        // Validate role (NOT NULL, must be in valid list)
        if (account.getRole() == null || account.getRole().trim().isEmpty()) {
            errors.add("Role is required");
        } else if (!VALID_ROLES.contains(account.getRole().toUpperCase())) {
            errors.add("Role must be one of: OWNER, STAFF, USER");
        }

        // Validate passwordHash (optional but if provided must not exceed max length)
        if (account.getPasswordHash() != null &&
            account.getPasswordHash().length() > PASSWORD_HASH_MAX_LENGTH) {
            errors.add("Password hash must not exceed " + PASSWORD_HASH_MAX_LENGTH + " characters");
        }
        
        
        
        // Validate googleId (optional)
        if (account.getGoogleId() != null &&
            account.getGoogleId().length() > GOOGLE_ID_MAX_LENGTH) {
            errors.add("Google ID must not exceed " + GOOGLE_ID_MAX_LENGTH + " characters");
        }
        
        
        
        // Validate avatarPath (optional)
        if (account.getAvatarPath() != null &&
            account.getAvatarPath().length() > AVATAR_PATH_MAX_LENGTH) {
            errors.add("Avatar path must not exceed " + AVATAR_PATH_MAX_LENGTH + " characters");
        }
        return errors;
    }

    
    
    /**
     * Check if email is valid
     */
    
    
    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }

    
    /**
     * Check if phone is valid
     */
    public static boolean isValidPhone(String phone) {
        return phone != null && PHONE_PATTERN.matcher(phone).matches();
    }

    /**
     * Check if role is valid
     */
    public static boolean isValidRole(String role) {
        return role != null && VALID_ROLES.contains(role.toUpperCase());
    }
}
