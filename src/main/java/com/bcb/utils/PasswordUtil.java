package com.bcb.utils;

import java.security.SecureRandom;
import org.mindrot.jbcrypt.BCrypt;

public class PasswordUtil {
	
	private static final String CHARS =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*";

    // Tạo mật khẩu tạm
    public static String generateRandomPassword(int length) {
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < length; i++) {
            sb.append(CHARS.charAt(random.nextInt(CHARS.length())));
        }

        return sb.toString();
    }

    // BCrypt
    public static String hashPassword(String plainPassword) {
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(10));
    }
}
