package com.bcb.controller;

import com.bcb.exception.BusinessException;
import com.bcb.model.Account;
import com.bcb.service.GoogleAuthService;
import com.bcb.service.impl.GoogleAuthServiceImpl;
import com.bcb.utils.AuthRedirectUtil;
import com.bcb.utils.EmailActionSyncStore;
import com.bcb.utils.RegisterGoogleLinkStore;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class GoogleCallbackController extends HttpServlet {
    private static final String REGISTER_STATE_PREFIX = "register:";
    private static final long REGISTER_CONTINUE_TTL_MS = 60 * 1000L;

    private final GoogleAuthService googleAuthService = new GoogleAuthServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        String code = trimToNull(request.getParameter("code"));
        String state = trimToNull(request.getParameter("state"));

        if (code == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing code");
            return;
        }

        try {
            if (state != null && state.startsWith(REGISTER_STATE_PREFIX)) {
                handleRegisterLinking(request, response, code, state.substring(REGISTER_STATE_PREFIX.length()));
                return;
            }

            Account account = googleAuthService.handleGoogleLogin(code);
            if (account == null) {
                request.setAttribute("error", "Tai khoan Google nay khong ton tai trong he thong.");
                request.getRequestDispatcher("/jsp/auth/login.jsp").forward(request, response);
                return;
            }

            if (!account.getIsActive()) {
                request.setAttribute("error", "Tai khoan da bi khoa.");
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

    private void handleRegisterLinking(HttpServletRequest request,
                                       HttpServletResponse response,
                                       String code,
                                       String token) throws IOException, ServletException {

        RegisterGoogleLinkStore.PendingLinkEntry pendingLink = RegisterGoogleLinkStore.get(token);
        if (pendingLink == null) {
            request.setAttribute("error", "Yeu cau xac nhan dang ky da het hieu luc.");
            request.getRequestDispatcher("/jsp/common/google-error.jsp").forward(request, response);
            return;
        }

        try {
            Account account = googleAuthService.handleGoogleLinking(code, pendingLink.getEmail());
            RegisterGoogleLinkStore.remove(token);
            EmailActionSyncStore.markConfirmed(
                    EmailActionSyncStore.PURPOSE_REGISTER,
                    token,
                    account.getEmail(),
                    REGISTER_CONTINUE_TTL_MS
            );

            forwardCompletionPage(
                    request,
                    response,
                    token,
                    account.getEmail(),
                    "Email đã được xác nhận, vui lòng quay lại trang để xem tài khoản.",
                    "Bạn có thể đóng trang này."
            );
        } catch (BusinessException e) {
            request.setAttribute("error", e.getMessage());
            request.setAttribute("retryUrl", buildRetryUrl(request, token));
            request.setAttribute("retryLabel", "Quay lai chon tai khoan khac");
            request.getRequestDispatcher("/jsp/common/google-error.jsp").forward(request, response);
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    private void forwardCompletionPage(HttpServletRequest request,
                                       HttpServletResponse response,
                                       String token,
                                       String email,
                                       String message,
                                       String instruction) throws ServletException, IOException {

        request.setAttribute("syncKey", EmailActionSyncStore.PURPOSE_REGISTER);
        request.setAttribute("syncToken", token);
        request.setAttribute("syncEmail", email);
        request.setAttribute("syncRedirectUrl", buildContinueUrl(request, token));
        request.setAttribute("message", message);
        request.setAttribute("instruction", instruction);
        request.getRequestDispatcher("/jsp/common/email-action-complete.jsp").forward(request, response);
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

    private String buildContinueUrl(HttpServletRequest request, String token) {
        return request.getContextPath()
                + "/email-action-continue?purpose="
                + URLEncoder.encode(EmailActionSyncStore.PURPOSE_REGISTER, StandardCharsets.UTF_8)
                + "&token="
                + URLEncoder.encode(token, StandardCharsets.UTF_8);
    }

    private String buildRetryUrl(HttpServletRequest request, String token) {
        return request.getContextPath()
                + "/google-link?token="
                + URLEncoder.encode(token, StandardCharsets.UTF_8);
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
