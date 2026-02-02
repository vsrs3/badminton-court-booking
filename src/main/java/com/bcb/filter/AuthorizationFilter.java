package com.bcb.filter;

import com.bcb.config.AuthConfig;
import com.bcb.model.Role;
import com.bcb.model.User;
import com.bcb.utils.SessionUtils;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Authorization Filter
 * Checks if user has required role for specific resources
 * Shows 403 error page if unauthorized
 */
@WebFilter(filterName = "AuthorizationFilter", urlPatterns = {"/*"})
public class AuthorizationFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        System.out.println("🔒 AuthorizationFilter initialized");
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        String requestURI = request.getRequestURI();
        String contextPath = request.getContextPath();
        String path = requestURI.substring(contextPath.length());

        // Skip filter for static resources and public URLs
        if (isStaticResource(path) || AuthConfig.isPublicUrl(path)) {
            chain.doFilter(request, response);
            return;
        }

        // Get current user
        User currentUser = SessionUtils.getCurrentUser(request);

        // If not logged in, AuthenticationFilter will handle it
        if (currentUser == null) {
            chain.doFilter(request, response);
            return;
        }

        // Check role-based access
        Role userRole = currentUser.getRole();

        // Check ADMIN access
        if (AuthConfig.matchesPattern(path, AuthConfig.ADMIN_URLS)) {
            if (userRole != Role.ADMIN) {
                System.out.println("❌ Unauthorized: " + currentUser.getEmail() + " (" + userRole + ") tried to access ADMIN resource: " + path);
                forward403(request, response);
                return;
            }
        }

        // Check OWNER access
        if (AuthConfig.matchesPattern(path, AuthConfig.OWNER_URLS)) {
            if (userRole != Role.OWNER && userRole != Role.ADMIN) {
                System.out.println("❌ Unauthorized: " + currentUser.getEmail() + " (" + userRole + ") tried to access OWNER resource: " + path);
                forward403(request, response);
                return;
            }
        }

        // Check STAFF access
        if (AuthConfig.matchesPattern(path, AuthConfig.STAFF_URLS)) {
            if (userRole != Role.STAFF && userRole != Role.OWNER && userRole != Role.ADMIN) {
                System.out.println("❌ Unauthorized: " + currentUser.getEmail() + " (" + userRole + ") tried to access STAFF resource: " + path);
                forward403(request, response);
                return;
            }
        }

        // Check CUSTOMER access
        if (AuthConfig.matchesPattern(path, AuthConfig.CUSTOMER_URLS)) {
            if (userRole != Role.CUSTOMER && userRole != Role.ADMIN) {
                System.out.println("❌ Unauthorized: " + currentUser.getEmail() + " (" + userRole + ") tried to access CUSTOMER resource: " + path);
                forward403(request, response);
                return;
            }
        }

        // User has required role, continue
        System.out.println("✅ Authorized: " + currentUser.getEmail() + " (" + userRole + ") accessing: " + path);
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        System.out.println("🔒 AuthorizationFilter destroyed");
    }

    /**
     * Forward to 403 error page
     */
    private void forward403(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("/jsp/error/403.jsp").forward(request, response);
    }

    /**
     * Check if request is for static resource
     */
    private boolean isStaticResource(String path) {
        return path.startsWith("/assets/") ||
                path.endsWith(".css") ||
                path.endsWith(".js") ||
                path.endsWith(".jpg") ||
                path.endsWith(".jpeg") ||
                path.endsWith(".png") ||
                path.endsWith(".gif") ||
                path.endsWith(".svg") ||
                path.endsWith(".ico");
    }
}