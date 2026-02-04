package com.bcb.filter;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/**
 * Global authentication filter
 * Runs on every request to set current user context
 */
@WebFilter("/*")
public class AuthenticationFilter implements Filter {

    private static final String[] PUBLIC_PATHS = {
            "/auth/",
            "/assets/",
            "/api/facilities", // Public API for guest viewing
            "/jsp/error/"
    };

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String requestURI = httpRequest.getRequestURI();
        String contextPath = httpRequest.getContextPath();
        String path = requestURI.substring(contextPath.length());

        System.out.println("🔍 AuthenticationFilter: " + path);

        // Get session (don't create new one)
        HttpSession session = httpRequest.getSession(false);
        Account currentUser = null;

        if (session != null) {
            currentUser = (Account) session.getAttribute("account");
        }

        // Set request attributes for easy access in JSP
        if (currentUser != null) {
            httpRequest.setAttribute("currentUser", currentUser);
            httpRequest.setAttribute("currentRole", currentUser.getRole());
            httpRequest.setAttribute("isLoggedIn", true);
            System.out.println("✅ User: " + currentUser.getEmail() + " (Role: " + currentUser.getRole() + ")");
        } else {
            httpRequest.setAttribute("isLoggedIn", false);
            System.out.println("👤 Guest user");
        }

        // Continue filter chain
        chain.doFilter(request, response);
    }
}