package com.bcb.controller;

import com.bcb.model.Account;
import com.bcb.service.AuthService;
import com.bcb.service.impl.AuthServiceImpl;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/**
 * Controller for authentication
 * Handles login, register, logout
 */
@WebServlet(name = "AuthController", urlPatterns = {"/auth/*"})
public class AuthController extends HttpServlet {

    private AuthService authService;

    @Override
    public void init() throws ServletException {
        super.init();
        this.authService = new AuthServiceImpl();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String pathInfo = request.getPathInfo();

        if (pathInfo == null) {
            response.sendRedirect(request.getContextPath() + "/auth/login");
            return;
        }

        switch (pathInfo) {
            case "/login":
                // Check if already logged in
                HttpSession session = request.getSession(false);
                if (session != null && session.getAttribute("account") != null) {
                    // Already logged in, redirect to home
                    response.sendRedirect(request.getContextPath() + "/");
                    return;
                }

                // Show login page
                request.getRequestDispatcher("/jsp/auth/login.jsp").forward(request, response);
                break;

            case "/logout":
                handleLogout(request, response);
                break;

            case "/register":
                // TODO: Show register page (future)
                response.sendRedirect(request.getContextPath() + "/auth/login");
                break;

            case "/forgot-password":
                // TODO: Show forgot password page (future)
                response.sendRedirect(request.getContextPath() + "/auth/login");
                break;

            default:
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String pathInfo = request.getPathInfo();

        if (pathInfo == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        switch (pathInfo) {
            case "/login":
                handleLogin(request, response);
                break;

            default:
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    /**
     * Handle login POST request
     */
    private void handleLogin(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        // Get form data
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        String rememberMe = request.getParameter("rememberMe");

        System.out.println("📥 Login request: email=" + email + ", rememberMe=" + rememberMe);

        // Validate input (backend validation)
        if (email == null || email.trim().isEmpty() ||
                password == null || password.trim().isEmpty()) {

            System.out.println("❌ Missing email or password");
            request.setAttribute("error", "Email và mật khẩu không được để trống");
            request.getRequestDispatcher("/jsp/auth/login.jsp").forward(request, response);
            return;
        }

        try {
            // Authenticate
            Account account = authService.authenticate(email.trim(), password);

            // Create session
            HttpSession session = request.getSession(true);
            session.setAttribute("account", account);
            session.setAttribute("accountId", account.getAccountId());
            session.setAttribute("email", account.getEmail());
            session.setAttribute("fullName", account.getFullName());
            session.setAttribute("role", account.getRole());

            // Set session timeout (30 minutes default, 7 days if remember me)
            if ("on".equals(rememberMe)) {
                session.setMaxInactiveInterval(7 * 24 * 60 * 60); // 7 days
            } else {
                session.setMaxInactiveInterval(30 * 60); // 30 minutes
            }

            System.out.println("✅ Login successful, session created for: " + account.getEmail());

            // ✅ UPDATED: Role-based redirect
            String redirectUrl;
            String role = account.getRole();

            switch (role) {
                case "ADMIN":
                    redirectUrl = request.getContextPath() + "/admin/dashboard";
                    break;
                case "OWNER":
                    redirectUrl = request.getContextPath() + "/owner/dashboard";
                    break;
                case "STAFF":
                    redirectUrl = request.getContextPath() + "/staff/dashboard";
                    break;
                case "CUSTOMER":
                    redirectUrl = request.getContextPath() + "/";
                    break;
                default:
                    // Unknown role - treat as customer
                    redirectUrl = request.getContextPath() + "/";
            }

            System.out.println("🔄 Redirecting " + role + " to: " + redirectUrl);
            response.sendRedirect(redirectUrl);

        } catch (RuntimeException e) {
            // Authentication failed - show generic error
            System.out.println("❌ Login failed: " + e.getMessage());

            request.setAttribute("error", "Email hoặc mật khẩu không đúng");
            request.setAttribute("email", email); // Preserve email input
            request.getRequestDispatcher("/jsp/auth/login.jsp").forward(request, response);
        }
    }

    /**
     * Handle logout
     */
    private void handleLogout(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        HttpSession session = request.getSession(false);
        if (session != null) {
            String email = (String) session.getAttribute("email");
            session.invalidate();
            System.out.println("👋 User logged out: " + email);
        }

        response.sendRedirect(request.getContextPath() + "/auth/login");
    }
}