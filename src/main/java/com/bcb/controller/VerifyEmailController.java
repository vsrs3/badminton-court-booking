package com.bcb.controller;

import com.bcb.exception.BusinessException;
import com.bcb.model.EmailVerification;
import com.bcb.repository.impl.EmailVerificationRepositoryImpl;
import com.bcb.service.AuthService;
import com.bcb.service.impl.AuthServiceImpl;
import com.bcb.utils.EmailActionSyncStore;
import com.bcb.utils.RegisterGoogleLinkStore;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class VerifyEmailController extends HttpServlet {
    private static final long REGISTER_CONTINUE_TTL_MS = 60 * 1000L;
    private static final long FORGOT_PASSWORD_CONTINUE_TTL_MS = 15 * 60 * 1000L;

    private final AuthService authService = new AuthServiceImpl();
    private final EmailVerificationRepositoryImpl emailVerificationRepository = new EmailVerificationRepositoryImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String token = trimToNull(request.getParameter("token"));
        String purpose = trimToNull(request.getParameter("purpose"));

        if (token == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Thiếu token xác nhận");
            return;
        }

        try {
            if (EmailActionSyncStore.PURPOSE_FORGOT_PASSWORD.equalsIgnoreCase(purpose)) {
                handleForgotPasswordConfirmation(request, response, token);
                return;
            }

            handleRegisterConfirmation(request, response, token);
        } catch (BusinessException e) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, e.getMessage());
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    private void handleRegisterConfirmation(HttpServletRequest request,
                                            HttpServletResponse response,
                                            String token) throws Exception {

        EmailVerification emailVerification = emailVerificationRepository.findByToken(token);
        if (emailVerification == null) {
            throw new BusinessException("Token khong hop le.");
        }

        if (emailVerification.isExpired()) {
            emailVerificationRepository.deleteByToken(token);
            throw new BusinessException("Token da het han.");
        }

        authService.verifyEmail(token);
        RegisterGoogleLinkStore.save(token, emailVerification.getEmail(), REGISTER_CONTINUE_TTL_MS);
        response.sendRedirect(buildGoogleLinkUrl(request, token));
    }

    private void handleForgotPasswordConfirmation(HttpServletRequest request,
                                                  HttpServletResponse response,
                                                  String token) throws Exception {

        String email = authService.getPasswordResetEmail(token);
        EmailActionSyncStore.markConfirmed(
                EmailActionSyncStore.PURPOSE_FORGOT_PASSWORD,
                token,
                email,
                FORGOT_PASSWORD_CONTINUE_TTL_MS
        );

        forwardCompletionPage(
                request,
                response,
                EmailActionSyncStore.PURPOSE_FORGOT_PASSWORD,
                token,
                email,
                buildContinueUrl(request, EmailActionSyncStore.PURPOSE_FORGOT_PASSWORD, token),
                "Email đã được xác nhận, vui lòng quay lại trang để tiếp tục đặt mật khẩu.",
                "Bạn có thể đóng trang này."
        );
    }

    private void forwardCompletionPage(HttpServletRequest request,
                                       HttpServletResponse response,
                                       String syncKey,
                                       String token,
                                       String email,
                                       String redirectUrl,
                                       String message,
                                       String instruction) throws ServletException, IOException {

        request.setAttribute("syncKey", syncKey);
        request.setAttribute("syncToken", token);
        request.setAttribute("syncEmail", email);
        request.setAttribute("syncRedirectUrl", redirectUrl);
        request.setAttribute("message", message);
        request.setAttribute("instruction", instruction);
        request.getRequestDispatcher("/jsp/common/email-action-complete.jsp").forward(request, response);
    }

    private String buildContinueUrl(HttpServletRequest request, String purpose, String token) {
        return request.getContextPath()
                + "/email-action-continue?purpose="
                + URLEncoder.encode(purpose, StandardCharsets.UTF_8)
                + "&token="
                + URLEncoder.encode(token, StandardCharsets.UTF_8);
    }

    private String buildGoogleLinkUrl(HttpServletRequest request, String token) {
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

    @Override
    public String getServletInfo() {
        return "Verify email actions";
    }
}
