package com.bcb.filter;

import com.bcb.model.Account;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/**
 * Redirect users to appropriate dashboard if they access wrong URL
 * Example: STAFF accessing "/" → redirect to "/staff/dashboard"
 */
@WebFilter("/*")
public class RoleRedirectFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String requestURI = httpRequest.getRequestURI();
        String contextPath = httpRequest.getContextPath();
        String path = requestURI.substring(contextPath.length());

        // Only process home page access
        if (!path.equals("/") && !path.equals("")) {
            chain.doFilter(request, response);
            return;
        }

        // Get current user
        HttpSession session = httpRequest.getSession(false);
        if (session == null) {
            // Guest - allow access to home
            chain.doFilter(request, response);
            return;
        }

        Account currentUser = (Account) session.getAttribute("account");
        if (currentUser == null) {
            // Guest - allow access to home
            chain.doFilter(request, response);
            return;
        }

        String role = currentUser.getRole();
        String redirectUrl = null;

        // Redirect non-customer roles to their dashboard
        switch (role) {
            case "ADMIN":
                redirectUrl = contextPath + "/admin/dashboard";
                break;
            case "OWNER":
                redirectUrl = contextPath + "/owner/dashboard";
                break;
            case "STAFF":
                redirectUrl = contextPath + "/staff/dashboard";
                break;
            case "CUSTOMER":
                // Customer can access home page - continue
                chain.doFilter(request, response);
                return;
            default:
                // Unknown role - treat as guest
                chain.doFilter(request, response);
                return;
        }

        // Redirect to appropriate dashboard
        if (redirectUrl != null) {
            System.out.println("🔄 Redirecting " + role + " from / to " + redirectUrl);
            httpResponse.sendRedirect(redirectUrl);
        }
    }
}