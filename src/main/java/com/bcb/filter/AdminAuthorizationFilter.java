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
            String contextPath = httpRequest.getContextPath();
            httpResponse.sendRedirect(contextPath + "/auth/login");
            return;
        }

        Account currentUser = (Account) session.getAttribute("account");

        if (currentUser == null) {
            // Not logged in - redirect to login
            String contextPath = httpRequest.getContextPath();
            httpResponse.sendRedirect(contextPath + "/auth/login");
            return;
        }

        String role = currentUser.getRole();

        if (!"ADMIN".equals(role)) {
            // Wrong role - show 403
            httpRequest.getRequestDispatcher("/jsp/error/403.jsp").forward(httpRequest, httpResponse);
            return;
        }

        // Authorized - continue
        chain.doFilter(request, response);
    }
}
