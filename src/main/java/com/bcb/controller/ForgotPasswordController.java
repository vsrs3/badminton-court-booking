package com.bcb.controller;

import com.bcb.exception.BusinessException;
import com.bcb.service.AuthService;
import com.bcb.service.impl.AuthServiceImpl;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public class ForgotPasswordController extends HttpServlet {

    private final AuthService authService = new AuthServiceImpl();

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

        try {

            // ===============================
            // BƯỚC 1: CHECK EMAIL
            // ===============================
            if ("checkEmail".equals(action)) {

                String email = request.getParameter("email");

                // Gọi service xử lý
                authService.forgotPassword(email);

                // Nếu không có exception → email hợp lệ
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

                // Gọi service reset
                authService.resetPassword(email, password);

                response.sendRedirect(request.getContextPath() + "/auth/login");
            }

        } catch (BusinessException e) {

            request.setAttribute("error", e.getMessage());
            request.getRequestDispatcher("/jsp/common/forgot-password.jsp")
                    .forward(request, response);

        } catch (Exception e) {
            throw new ServletException(e);
        }
    }
}