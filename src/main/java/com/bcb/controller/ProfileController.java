package com.bcb.controller;

import com.bcb.model.Account;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/**
 * Controller for customer profile page
 */
@WebServlet(name = "ProfileController", urlPatterns = {"/profile"})
public class ProfileController extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("account") == null) {
            // Not logged in
            response.sendRedirect(request.getContextPath() + "/auth/login");
            return;
        }

        Account account = (Account) session.getAttribute("account");

        if (!"CUSTOMER".equals(account.getRole())) {
            // Wrong role - redirect to their dashboard
            String redirectUrl = switch (account.getRole()) {
                case "ADMIN" -> "/admin/dashboard";
                case "OWNER" -> "/owner/dashboard";
                case "STAFF" -> "/staff/dashboard";
                default -> "/";
            };
            response.sendRedirect(request.getContextPath() + redirectUrl);
            return;
        }

        // Show profile page
        request.getRequestDispatcher("/jsp/customer/profile/profile.jsp").forward(request, response);
    }
}