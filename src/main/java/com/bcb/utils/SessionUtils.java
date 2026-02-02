package com.bcb.utils;

import com.bcb.model.Role;
import com.bcb.model.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

/**
 * Utility class for managing user session
 */
public class SessionUtils {

    // Session attribute keys
    public static final String SESSION_USER = "currentUser";
    public static final String SESSION_RETURN_URL = "returnUrl";

    // Private constructor - utility class
    private SessionUtils() {
    }

    /**
     * Get current logged-in user from session
     * @param request HttpServletRequest
     * @return User object or null if not logged in
     */
    public static User getCurrentUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }

        return (User) session.getAttribute(SESSION_USER);
    }

    /**
     * Set current user in session (after login)
     * @param request HttpServletRequest
     * @param user User object
     */
    public static void setCurrentUser(HttpServletRequest request, User user) {
        HttpSession session = request.getSession(true);
        session.setAttribute(SESSION_USER, user);

        // Set timeout (30 minutes)
        session.setMaxInactiveInterval(30 * 60);
    }

    /**
     * Remove user from session (logout)
     * @param request HttpServletRequest
     */
    public static void removeCurrentUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.removeAttribute(SESSION_USER);
            session.invalidate();
        }
    }

    /**
     * Check if user is logged in
     * @param request HttpServletRequest
     * @return true if logged in, false otherwise
     */
    public static boolean isLoggedIn(HttpServletRequest request) {
        User user = getCurrentUser(request);
        return User.isLoggedIn(user);
    }

    /**
     * Check if current user has specific role
     * @param request HttpServletRequest
     * @param role Role to check
     * @return true if user has role, false otherwise
     */
    public static boolean hasRole(HttpServletRequest request, Role role) {
        User user = getCurrentUser(request);
        return user != null && user.hasRole(role);
    }

    /**
     * Check if current user has any of the specified roles
     * @param request HttpServletRequest
     * @param roles Roles to check
     * @return true if user has any role, false otherwise
     */
    public static boolean hasAnyRole(HttpServletRequest request, Role... roles) {
        User user = getCurrentUser(request);
        return user != null && user.hasAnyRole(roles);
    }

    /**
     * Store return URL for redirect after login
     * @param request HttpServletRequest
     * @param returnUrl URL to return to
     */
    public static void setReturnUrl(HttpServletRequest request, String returnUrl) {
        HttpSession session = request.getSession(true);
        session.setAttribute(SESSION_RETURN_URL, returnUrl);
    }

    /**
     * Get and remove return URL
     * @param request HttpServletRequest
     * @return Return URL or null
     */
    public static String getAndClearReturnUrl(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }

        String returnUrl = (String) session.getAttribute(SESSION_RETURN_URL);
        session.removeAttribute(SESSION_RETURN_URL);
        return returnUrl;
    }

    /**
     * Get redirect URL based on user role
     * @param user User object
     * @return Redirect URL
     */
    public static String getRedirectUrlByRole(User user) {
        if (user == null) {
            return "/home";
        }

        switch (user.getRole()) {
            case ADMIN:
                return "/admin/dashboard";
            case STAFF:
                return "/staff/dashboard";
            case OWNER:
                return "/owner/dashboard";
            case CUSTOMER:
            default:
                return "/home";
        }
    }
}