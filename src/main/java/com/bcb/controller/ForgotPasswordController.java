package com.bcb.controller;

import com.bcb.dao.AccountDAO;
import com.bcb.model.Account;
import com.google.gson.JsonObject;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.mindrot.jbcrypt.BCrypt;

import java.io.IOException;


public class ForgotPasswordController extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.getRequestDispatcher("/jsp/common/forgot-password.jsp")
                .forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String action = request.getParameter("action");
        AccountDAO accountDao = new AccountDAO();

        try {

            // ===============================
            // BƯỚC 1: CHECK EMAIL
            // ===============================
            if ("checkEmail".equals(action)) {

                String email = request.getParameter("email");

                if (!accountDao.isEmailExists(email)) {
                    request.setAttribute("error", "Email không tồn tại.");
                    request.getRequestDispatcher("/jsp/common/forgot-password.jsp")
                            .forward(request, response);
                    return;
                }

                // email tồn tại → chuyển sang reset form
                request.setAttribute("email", email);
                request.setAttribute("step", "reset");
                request.getRequestDispatcher("/jsp/common/forgot-password.jsp")
                        .forward(request, response);
                return;
            }

            // ===============================
            // BƯỚC 2: RESET PASSWORD
            // ===============================
            if ("reset".equals(action)) {

                String email = request.getParameter("email");
                String password = request.getParameter("password");
                String repassword = request.getParameter("repassword");

                if (!password.equals(repassword)) {
                    request.setAttribute("error", "Mật khẩu không khớp.");
                    request.setAttribute("email", email);
                    request.setAttribute("step", "reset");
                    request.getRequestDispatcher("/jsp/common/forgot-password.jsp")
                            .forward(request, response);
                    return;
                }

                String hash = BCrypt.hashpw(password, BCrypt.gensalt());

                accountDao.updatePassword(email, hash);

                response.sendRedirect(request.getContextPath() + "/auth/login");
            }

        } catch (Exception e) {
            throw new ServletException(e);
        }
    }
}
