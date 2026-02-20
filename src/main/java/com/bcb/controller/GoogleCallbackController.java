package com.bcb.controller;

import com.bcb.exception.BusinessException;
import com.bcb.model.Account;
import com.bcb.service.AuthService;
import com.bcb.service.GoogleAuthService;
import com.bcb.service.impl.AuthServiceImpl;
import com.bcb.service.impl.GoogleAuthServiceImpl;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

public class GoogleCallbackController extends HttpServlet {

    private final GoogleAuthService googleAuthService = new GoogleAuthServiceImpl();
    private final AuthService authService = new AuthServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws IOException, ServletException {

        String code = request.getParameter("code");

        if (code == null) {
            response.sendError(400, "Missing code");
            return;
        }

        try {

            HttpSession session = request.getSession(false);

            // ==================================================
            // 🔵 LUỒNG 1: SAU VERIFY EMAIL (LINK GOOGLE)
            // ==================================================
            if (session != null && session.getAttribute("verifiedEmail") != null) {

                String verifiedEmail =
                        (String) session.getAttribute("verifiedEmail");

                Account acc = googleAuthService.handleGoogleLinking(code, verifiedEmail);

                loginAndRedirect(request, response, acc);

                session.removeAttribute("verifiedEmail");
                return;
            }

            // ==================================================
            // 🟢 LUỒNG 2: LOGIN BẰNG GOOGLE
            // ==================================================
            Account acc = googleAuthService.handleGoogleLogin(code);
            if (acc == null) {
                request.setAttribute("error",
                        "Tài khoản Google này không tồn tại trong hệ thống.");
                request.getRequestDispatcher("/jsp/auth/login.jsp")
                        .forward(request, response);
                return;}

            if (!acc.getIsActive()) {
                request.setAttribute("error",
                        "Tài khoản đã bị khóa.");
                request.getRequestDispatcher("/jsp/auth/login.jsp")
                        .forward(request, response);
                return;}

            loginAndRedirect(request, response, acc);
        } catch (BusinessException e) {
            request.setAttribute("error", e.getMessage());
            request.getRequestDispatcher("/jsp/common/google-error.jsp")
                    .forward(request, response);
        } catch (Exception e) {
            throw new ServletException(e);}}



    private void loginAndRedirect(HttpServletRequest request,
                                  HttpServletResponse response,
                                  Account acc)
            throws IOException {
        HttpSession session = request.getSession(true);
        session.setAttribute("account", acc);
        session.setAttribute("accountId", acc.getAccountId());
        session.setAttribute("email", acc.getEmail());
        session.setAttribute("fullName", acc.getFullName());
        session.setAttribute("role", acc.getRole());
        session.setMaxInactiveInterval(30 * 60);
        String redirectUrl;

        switch (acc.getRole()) {
            case "ADMIN" -> redirectUrl = request.getContextPath() + "/admin/dashboard";
            case "OWNER" -> redirectUrl = request.getContextPath() + "/owner/dashboard";
            case "STAFF" -> redirectUrl = request.getContextPath() + "/staff/dashboard";
            default -> redirectUrl = request.getContextPath() + "/";
        }

        response.sendRedirect(redirectUrl);
    }
}