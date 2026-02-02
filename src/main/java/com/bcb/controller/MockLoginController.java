package com.bcb.controller;

import com.bcb.config.AuthConfig;
import com.bcb.model.User;
import com.bcb.service.impl.MockAuthService;
import com.bcb.utils.SessionUtils;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Mock Login Controller
 * Simulates login functionality for development (MOCK_MODE = true)
 *
 * URL: /auth/mock-login
 * GET: Show mock login page
 * POST: Process mock login
 */
@WebServlet(name = "MockLoginController", urlPatterns = {"/auth/mock-login"})
public class MockLoginController extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Only available in mock mode
        if (!AuthConfig.MOCK_MODE) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        // Forward to mock login page
        request.getRequestDispatcher("/jsp/auth/mock-login.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Only available in mock mode
        if (!AuthConfig.MOCK_MODE) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        String email = request.getParameter("email");
        String password = request.getParameter("password");

        System.out.println("🔐 Mock login attempt: " + email);

        // Attempt login with MockAuthService
        User user = MockAuthService.login(email, password);

        if (user != null) {
            // Login successful - set session
            SessionUtils.setCurrentUser(request, user);

            System.out.println("✅ Login successful: " + user.getEmail() + " (" + user.getRole() + ")");

            // Get return URL or redirect based on role
            String returnUrl = SessionUtils.getAndClearReturnUrl(request);

            if (returnUrl != null && !returnUrl.isEmpty()) {
                response.sendRedirect(returnUrl);
            } else {
                String redirectUrl = SessionUtils.getRedirectUrlByRole(user);
                response.sendRedirect(request.getContextPath() + redirectUrl);
            }
        } else {
            // Login failed
            System.out.println("❌ Login failed: " + email);

            request.setAttribute("error", "Email hoặc mật khẩu không đúng");
            request.setAttribute("email", email);
            request.getRequestDispatcher("/jsp/auth/mock-login.jsp").forward(request, response);
        }
    }
}