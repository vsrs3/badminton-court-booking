package com.bcb.filter;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/**
 * Authorization filter for Owner area
 * Only users with OWNER role can access /owner/*
 */
@WebFilter("/owner/*")
public class OwnerAuthorizationFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // Get current user from session
        HttpSession session = httpRequest.getSession(false);

        if (session == null) {
            System.out.println("❌ Owner access denied: Not logged in");
            String contextPath = httpRequest.getContextPath();
            httpResponse.sendRedirect(contextPath + "/auth/login");
            return;
        }

        Account currentUser = (Account) session.getAttribute("account");

        if (currentUser == null) {
            System.out.println("❌ Owner access denied: No account in session");
            String contextPath = httpRequest.getContextPath();
            httpResponse.sendRedirect(contextPath + "/auth/login");
            return;
        }

        String role = currentUser.getRole();

        if (!"OWNER".equals(role)) {
            System.out.println("❌ Owner access denied: User has role " + role);
            httpRequest.getRequestDispatcher("/jsp/error/403.jsp").forward(httpRequest, httpResponse);
            return;
        }

        System.out.println("✅ Owner access granted: " + currentUser.getEmail());
        chain.doFilter(request, response);
    }
}