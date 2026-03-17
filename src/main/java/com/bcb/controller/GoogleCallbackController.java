package com.bcb.controller;

import com.bcb.exception.BusinessException;
import com.bcb.model.Account;
import com.bcb.service.GoogleAuthService;
import com.bcb.service.impl.GoogleAuthServiceImpl;
import com.bcb.utils.AuthRedirectUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

public class GoogleCallbackController extends HttpServlet {

    private final GoogleAuthService googleAuthService = new GoogleAuthServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        String code = request.getParameter("code");
        if (code == null || code.isBlank()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing code");
            return;
        }

        try {
            HttpSession session = request.getSession(false);
            String verifiedEmail = session != null
                    ? (String) session.getAttribute("verifiedEmail")
                    : null;

            if (verifiedEmail == null || verifiedEmail.isBlank()) {
                verifiedEmail = request.getParameter("state");
            }

            if (verifiedEmail != null && !verifiedEmail.isBlank()) {
                Account account = googleAuthService.handleGoogleLinking(code, verifiedEmail);
                clearVerifiedEmail(session);
                loginAndRedirect(request, response, account);
                return;
            }

            Account account = googleAuthService.handleGoogleLogin(code);
            if (account == null) {
                request.setAttribute("error", "Tài khoản Google này không tồn tại trong hệ thống.");
                request.getRequestDispatcher("/jsp/auth/login.jsp").forward(request, response);
                return;
            }

            if (!account.getIsActive()) {
                request.setAttribute("error", "Tài khoản đã bị khóa.");
                request.getRequestDispatcher("/jsp/auth/login.jsp").forward(request, response);
                return;
            }

            loginAndRedirect(request, response, account);
        } catch (BusinessException e) {
            request.setAttribute("error", e.getMessage());
            request.getRequestDispatcher("/jsp/common/google-error.jsp").forward(request, response);
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    private void clearVerifiedEmail(HttpSession session) {
        if (session != null) {
            session.removeAttribute("verifiedEmail");
        }
    }

    private void loginAndRedirect(HttpServletRequest request, HttpServletResponse response, Account account)
            throws IOException {
        HttpSession session = request.getSession(true);
        session.setAttribute("account", account);
        session.setAttribute("accountId", account.getAccountId());
        session.setAttribute("email", account.getEmail());
        session.setAttribute("fullName", account.getFullName());
        session.setAttribute("role", account.getRole());
        session.setMaxInactiveInterval(30 * 60);

        String redirectUrl = request.getContextPath() + AuthRedirectUtil.resolvePathByRole(account.getRole());
        response.sendRedirect(redirectUrl);
    }
}
