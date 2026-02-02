package com.bcb.filter;

import com.bcb.config.AuthConfig;
import com.bcb.model.User;
import com.bcb.utils.SessionUtils;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Authentication Filter
 * Checks if user is logged in for protected resources
 * Redirects to login page if not authenticated
 */
@WebFilter(filterName = "AuthenticationFilter", urlPatterns = {"/*"})
public class AuthenticationFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        System.out.println("🔐 AuthenticationFilter initialized");
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        String requestURI = request.getRequestURI();
        String contextPath = request.getContextPath();
        String path = requestURI.substring(contextPath.length());

        // Log request for debugging
        System.out.println("🔍 AuthenticationFilter: " + request.getMethod() + " " + path);

        // Skip filter for static resources
        if (isStaticResource(path)) {
            chain.doFilter(request, response);
            return;
        }

        // Check if path is public (doesn't require authentication)
        if (AuthConfig.isPublicUrl(path)) {
            System.out.println("✅ Public URL, skipping auth: " + path);
            chain.doFilter(request, response);
            return;
        }

        // Check if user is logged in
        User currentUser = SessionUtils.getCurrentUser(request);

        if (currentUser == null) {
            System.out.println("❌ Not authenticated, redirecting to login: " + path);

            // Store return URL for redirect after login
            SessionUtils.setReturnUrl(request, requestURI);

            // Redirect to login page
            response.sendRedirect(contextPath + AuthConfig.LOGIN_URL);
            return;
        }

        // User is authenticated, continue
        System.out.println("✅ Authenticated as: " + currentUser.getEmail() + " (" + currentUser.getRole() + ")");
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        System.out.println("🔐 AuthenticationFilter destroyed");
    }

    /**
     * Check if request is for static resource (CSS, JS, images, etc.)
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
                path.endsWith(".ico") ||
                path.endsWith(".woff") ||
                path.endsWith(".woff2") ||
                path.endsWith(".ttf") ||
                path.endsWith(".eot");
    }
}