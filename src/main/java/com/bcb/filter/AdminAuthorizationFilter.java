package com.bcb.filter;

import com.bcb.model.Account;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/**
 * Authorization filter for Admin area
 * Only users with ADMIN role can access /admin/*
 */
@WebFilter("/admin/*")
public class AdminAuthorizationFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // Get current user from session
        HttpSession session = httpRequest.getSession(false);

        if (session == null) {
            // Not logged in - redirect to login
            System.out.println("❌ Admin access denied: Not logged in");
            String contextPath = httpRequest.getContextPath();
            httpResponse.sendRedirect(contextPath + "/auth/login");
            return;
        }

        Account currentUser = (Account) session.getAttribute("account");

        if (currentUser == null) {
            // Not logged in - redirect to login
            System.out.println("❌ Admin access denied: No account in session");
            String contextPath = httpRequest.getContextPath();
            httpResponse.sendRedirect(contextPath + "/auth/login");
            return;
        }

        String role = currentUser.getRole();

        if (!"ADMIN".equals(role)) {
            // Wrong role - show 403
            System.out.println("❌ Admin access denied: User has role " + role);
            httpRequest.getRequestDispatcher("/jsp/error/403.jsp").forward(httpRequest, httpResponse);
            return;
        }

        // Authorized - continue
        System.out.println("✅ Admin access granted: " + currentUser.getEmail());
        chain.doFilter(request, response);
    }
}