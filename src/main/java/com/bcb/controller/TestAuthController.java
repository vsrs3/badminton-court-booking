package com.bcb.controller;

import com.bcb.model.User;
import com.bcb.service.impl.MockAuthService;
import com.bcb.utils.SessionUtils;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;

/**
 * Test controller for mock authentication
 * URL: /test-auth
 */
@WebServlet(name = "TestAuthController", urlPatterns = {"/test-auth"})
public class TestAuthController extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();

        // Test login with customer account
        User user = MockAuthService.login("customer@test.com", "password123");

        if (user != null) {
            SessionUtils.setCurrentUser(request, user);
        }

        // Check current user
        User currentUser = SessionUtils.getCurrentUser(request);

        out.println("<!DOCTYPE html>");
        out.println("<html>");
        out.println("<head><title>Auth Test</title></head>");
        out.println("<body>");
        out.println("<h1>Authentication Test</h1>");

        if (currentUser != null) {
            out.println("<p>✅ Logged in as: <strong>" + currentUser.getFullName() + "</strong></p>");
            out.println("<p>Email: " + currentUser.getEmail() + "</p>");
            out.println("<p>Role: " + currentUser.getRole() + "</p>");
            out.println("<p>Account ID: " + currentUser.getAccountId() + "</p>");
        } else {
            out.println("<p>❌ Not logged in</p>");
        }

        out.println("<hr>");
        out.println("<h2>Available Mock Accounts:</h2>");
        out.println("<ul>");
        out.println("<li>customer@test.com / password123 → CUSTOMER</li>");
        out.println("<li>staff@test.com / password123 → STAFF</li>");
        out.println("<li>owner@test.com / password123 → OWNER</li>");
        out.println("<li>admin@test.com / password123 → ADMIN</li>");
        out.println("</ul>");

        out.println("</body>");
        out.println("</html>");
    }
}