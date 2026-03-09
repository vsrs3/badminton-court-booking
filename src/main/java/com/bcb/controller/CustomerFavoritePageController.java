package com.bcb.controller;

import com.bcb.model.Account;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

@WebServlet(name = "CustomerFavoritePageController", urlPatterns = {"/profile/favorites"})
public class CustomerFavoritePageController extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("account") == null) {
            response.sendRedirect(request.getContextPath() + "/auth/login");
            return;
        }

        Account account = (Account) session.getAttribute("account");
        if (!"CUSTOMER".equals(account.getRole())) {
            String redirectUrl = switch (account.getRole()) {
                case "ADMIN" -> "/admin/dashboard";
                case "OWNER" -> "/owner/dashboard";
                case "STAFF" -> "/staff/dashboard";
                default -> "/";
            };
            response.sendRedirect(request.getContextPath() + redirectUrl);
            return;
        }

        request.getRequestDispatcher("/jsp/customer/profile/customer_favorite_page.jsp").forward(request, response);
    }
}
