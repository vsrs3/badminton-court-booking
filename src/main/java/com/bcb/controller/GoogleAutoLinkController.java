package com.bcb.controller;

import com.bcb.utils.RegisterGoogleLinkStore;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class GoogleAutoLinkController extends HttpServlet {
    private static final String REGISTER_STATE_PREFIX = "register:";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        String token = trimToNull(request.getParameter("token"));
        RegisterGoogleLinkStore.PendingLinkEntry pendingLink = RegisterGoogleLinkStore.get(token);

        if (pendingLink == null) {
            response.sendError(HttpServletResponse.SC_GONE, "Yeu cau lien ket Google da het hieu luc.");
            return;
        }

        String googleOAuthUrl =
                "https://accounts.google.com/o/oauth2/v2/auth" +
                        "?client_id=204559903564-apf9kc8g9k6e5lfgn00r7fr9gpq4ptgp.apps.googleusercontent.com" +
                        "&redirect_uri=http://localhost:8080/badminton_court_booking/google-callback" +
                        "&response_type=code" +
                        "&scope=openid email profile" +
                        "&prompt=select_account" +
                        "&login_hint=" + URLEncoder.encode(pendingLink.getEmail(), StandardCharsets.UTF_8) +
                        "&state=" + URLEncoder.encode(REGISTER_STATE_PREFIX + token, StandardCharsets.UTF_8);

        response.sendRedirect(googleOAuthUrl);
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
