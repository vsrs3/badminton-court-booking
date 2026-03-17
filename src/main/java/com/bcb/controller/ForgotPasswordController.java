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

        String token = trimToNull(request.getParameter("token"));
        if (token != null) {
            try {
                prepareResetStep(request, token);
            } catch (BusinessException e) {
                request.setAttribute("error", e.getMessage());
            }
        }

        request.getRequestDispatcher("/jsp/common/forgot-password.jsp")
                .forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String action = request.getParameter("action");

        try {
            if ("checkEmail".equals(action)) {
                handleEmailConfirmationRequest(request, response);
                return;
            }

            if ("reset".equals(action)) {
                handlePasswordReset(request, response);
                return;
            }

            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
        } catch (BusinessException e) {
            request.setAttribute("error", e.getMessage());
            request.getRequestDispatcher("/jsp/common/forgot-password.jsp")
                    .forward(request, response);
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    private void handleEmailConfirmationRequest(HttpServletRequest request, HttpServletResponse response)
            throws Exception {

        String email = request.getParameter("email");
        authService.forgotPassword(email, request.getRequestURL().toString());

        request.setAttribute("message",
                "Đã gửi email xác nhận đổi mật khẩu. Vui lòng kiểm tra hộp thư để tiếp tục.");
        request.setAttribute("email", email);
        request.getRequestDispatcher("/jsp/common/forgot-password.jsp")
                .forward(request, response);
    }

    private void handlePasswordReset(HttpServletRequest request, HttpServletResponse response)
            throws Exception {

        String token = request.getParameter("token");
        String password = request.getParameter("password");
        String repassword = request.getParameter("repassword");

        if (password == null || password.length() < 6) {
            prepareResetStep(request, token);
            request.setAttribute("error", "Mật khẩu phải có ít nhất 6 ký tự.");
            request.getRequestDispatcher("/jsp/common/forgot-password.jsp")
                    .forward(request, response);
            return;
        }

        if (repassword == null || !password.equals(repassword)) {
            prepareResetStep(request, token);
            request.setAttribute("error", "Mật khẩu nhập lại phải trùng với mật khẩu mới.");
            request.getRequestDispatcher("/jsp/common/forgot-password.jsp")
                    .forward(request, response);
            return;
        }

        authService.resetPassword(token, password);
        response.sendRedirect(request.getContextPath() + "/auth/login");
    }

    private void prepareResetStep(HttpServletRequest request, String token) throws BusinessException {
        request.setAttribute("step", "reset");
        request.setAttribute("token", token);
        request.setAttribute("email", authService.getPasswordResetEmail(token));
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
