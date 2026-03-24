package com.bcb.utils;

public final class AuthRedirectUtil {

    private AuthRedirectUtil() {
    }

    public static String resolvePathByRole(String role) {
        if (role == null) {
            return "/";
        }

        return switch (role) {
            case "ADMIN" -> "/admin/dashboard";
            case "OWNER" -> "/owner/dashboard";
            case "STAFF" -> "/staff/dashboard";
            case "CUSTOMER" -> "/home";
            default -> "/";
        };
    }
}
