package com.bcb.config;

/**
 * Authentication configuration
 * Toggle between mock mode (development) and real mode (production)
 */
public class AuthConfig {

    /**
     * Enable mock authentication for development
     * Set to false in production
     */
    public static final boolean MOCK_MODE = true;

    /**
     * Session timeout in seconds (30 minutes)
     */
    public static final int SESSION_TIMEOUT = 30 * 60;

    /**
     * Login page URL
     */
    public static final String LOGIN_URL = "/auth/login";

    /**
     * Default redirect after login (for CUSTOMER)
     */
    public static final String DEFAULT_HOME_URL = "/home";

    /**
     * URLs that don't require authentication
     */
    public static final String[] PUBLIC_URLS = {
            "/home",
            "/auth/login",
            "/auth/register",
            "/auth/logout",
            "/api/facilities",
            "/auth/mock-login",
            "/assets/*",
            "/jsp/error/*"
    };

    /**
     * URLs that require CUSTOMER role
     */
    public static final String[] CUSTOMER_URLS = {
            "/booking/*",
            "/payment/*",
            "/profile/*"
    };

    /**
     * URLs that require STAFF role
     */
    public static final String[] STAFF_URLS = {
            "/staff/*"
    };

    /**
     * URLs that require OWNER role
     */
    public static final String[] OWNER_URLS = {
            "/owner/*"
    };

    /**
     * URLs that require ADMIN role
     */
    public static final String[] ADMIN_URLS = {
            "/admin/*"
    };

    /**
     * Check if URL is public (doesn't require authentication)
     */
    public static boolean isPublicUrl(String url) {
        if (url == null) return false;

        for (String publicUrl : PUBLIC_URLS) {
            if (publicUrl.endsWith("*")) {
                String prefix = publicUrl.substring(0, publicUrl.length() - 1);
                if (url.startsWith(prefix)) {
                    return true;
                }
            } else if (url.equals(publicUrl)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Check if URL matches pattern
     */
    public static boolean matchesPattern(String url, String[] patterns) {
        if (url == null || patterns == null) return false;

        for (String pattern : patterns) {
            if (pattern.endsWith("*")) {
                String prefix = pattern.substring(0, pattern.length() - 1);
                if (url.startsWith(prefix)) {
                    return true;
                }
            } else if (url.equals(pattern)) {
                return true;
            }
        }

        return false;
    }
}