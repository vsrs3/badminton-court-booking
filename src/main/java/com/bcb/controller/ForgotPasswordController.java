package com.bcb.controller;

import com.bcb.exception.BusinessException;
import com.bcb.service.AuthService;
import com.bcb.service.impl.AuthServiceImpl;
import com.bcb.utils.EmailActionSyncStore;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

public class ForgotPasswordController extends HttpServlet {
    private static final String SESSION_CONFIRMED_RESET_TOKEN = "confirmedPasswordResetToken";
    private static final String SESSION_CONFIRMED_RESET_EMAIL = "confirmedPasswordResetEmail";

    private final AuthService authService = new AuthServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String token = trimToNull(request.getParameter("token"));
        if (token != null) {
            try {
                ensureConfirmedResetAccess(request, token);
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
        authService.forgotPassword(email, buildAbsoluteUrl(request, "/verify-email?purpose=forgot-password"));

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

        ensureConfirmedResetAccess(request, token);

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
        clearConfirmedResetSession(request.getSession(false), token);
        EmailActionSyncStore.remove(EmailActionSyncStore.PURPOSE_FORGOT_PASSWORD, token);
        response.sendRedirect(request.getContextPath() + "/auth/login");
    }

    private void prepareResetStep(HttpServletRequest request, String token) throws BusinessException {
        request.setAttribute("step", "reset");
        request.setAttribute("token", token);
        request.setAttribute("email", authService.getPasswordResetEmail(token));
    }

    private void ensureConfirmedResetAccess(HttpServletRequest request, String token) throws BusinessException {
        HttpSession session = request.getSession(false);
        if (session != null) {
            String confirmedToken = (String) session.getAttribute(SESSION_CONFIRMED_RESET_TOKEN);
            if (token.equals(confirmedToken)) {
                return;
            }
        }

        EmailActionSyncStore.SyncEntry entry = EmailActionSyncStore.getConfirmed(
                EmailActionSyncStore.PURPOSE_FORGOT_PASSWORD,
                token
        );

        if (entry == null) {
            throw new BusinessException("Vui lòng xác nhận yêu cầu đổi mật khẩu trong email trước.");
        }

        session = request.getSession(true);
        session.setAttribute(SESSION_CONFIRMED_RESET_TOKEN, token);
        session.setAttribute(SESSION_CONFIRMED_RESET_EMAIL, entry.getEmail());
    }

    private void clearConfirmedResetSession(HttpSession session, String token) {
        if (session == null) {
            return;
        }

        String confirmedToken = (String) session.getAttribute(SESSION_CONFIRMED_RESET_TOKEN);
        if (token != null && token.equals(confirmedToken)) {
            session.removeAttribute(SESSION_CONFIRMED_RESET_TOKEN);
            session.removeAttribute(SESSION_CONFIRMED_RESET_EMAIL);
        }
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String buildAbsoluteUrl(HttpServletRequest request, String pathWithQuery) {
        StringBuilder url = new StringBuilder();
        url.append(request.getScheme())
                .append("://")
                .append(request.getServerName());

        boolean isDefaultPort = ("http".equalsIgnoreCase(request.getScheme()) && request.getServerPort() == 80)
                || ("https".equalsIgnoreCase(request.getScheme()) && request.getServerPort() == 443);

        if (!isDefaultPort) {
            url.append(":").append(request.getServerPort());
        }

        url.append(request.getContextPath()).append(pathWithQuery);
        return url.toString();
    }
}
