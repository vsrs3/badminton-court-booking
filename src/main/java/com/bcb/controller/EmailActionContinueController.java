package com.bcb.controller;

import com.bcb.exception.BusinessException;
import com.bcb.model.Account;
import com.bcb.repository.AccountRepository;
import com.bcb.repository.impl.AccountRepositoryImpl;
import com.bcb.utils.AuthRedirectUtil;
import com.bcb.utils.EmailActionSyncStore;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class EmailActionContinueController extends HttpServlet {
    private static final String SESSION_CONFIRMED_RESET_TOKEN = "confirmedPasswordResetToken";
    private static final String SESSION_CONFIRMED_RESET_EMAIL = "confirmedPasswordResetEmail";

    private final AccountRepository accountRepository = new AccountRepositoryImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String purpose = trimToNull(request.getParameter("purpose"));
        String token = trimToNull(request.getParameter("token"));

        if (purpose == null || token == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Thiếu thông tin tiếp tục");
            return;
        }

        EmailActionSyncStore.SyncEntry entry = EmailActionSyncStore.getConfirmed(purpose, token);
        if (entry == null) {
            response.sendError(HttpServletResponse.SC_GONE, "Xác nhận đã hết hiệu lực");
            return;
        }

        try {
            if (EmailActionSyncStore.PURPOSE_REGISTER.equalsIgnoreCase(purpose)) {
                continueRegister(request, response, token, entry);
                return;
            }

            if (EmailActionSyncStore.PURPOSE_FORGOT_PASSWORD.equalsIgnoreCase(purpose)) {
                continueForgotPassword(request, response, token, entry);
                return;
            }

            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Mục đích xác nhận không hợp lệ");
        } catch (BusinessException e) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, e.getMessage());
        }
    }

    private void continueRegister(HttpServletRequest request,
                                  HttpServletResponse response,
                                  String token,
                                  EmailActionSyncStore.SyncEntry entry) throws IOException, BusinessException {

        Account account = accountRepository.findByEmail(entry.getEmail())
                .orElseThrow(() -> new BusinessException("Không tìm thấy tài khoản đã xác nhận."));

        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }

        session = request.getSession(true);
        session.setAttribute("account", account);
        session.setAttribute("accountId", account.getAccountId());
        session.setAttribute("email", account.getEmail());
        session.setAttribute("fullName", account.getFullName());
        session.setAttribute("role", account.getRole());
        session.setMaxInactiveInterval(30 * 60);

        EmailActionSyncStore.remove(EmailActionSyncStore.PURPOSE_REGISTER, token);
        response.sendRedirect(request.getContextPath() + AuthRedirectUtil.resolvePathByRole(account.getRole()));
    }

    private void continueForgotPassword(HttpServletRequest request,
                                        HttpServletResponse response,
                                        String token,
                                        EmailActionSyncStore.SyncEntry entry) throws IOException {

        HttpSession session = request.getSession(true);
        session.setAttribute(SESSION_CONFIRMED_RESET_TOKEN, token);
        session.setAttribute(SESSION_CONFIRMED_RESET_EMAIL, entry.getEmail());

        response.sendRedirect(
                request.getContextPath()
                        + "/forgot-password?token="
                        + URLEncoder.encode(token, StandardCharsets.UTF_8)
        );
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
