package com.bcb.controller;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.net.URLEncoder;

public class GoogleAutoLinkController extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("verifiedEmail") == null) {
            response.sendRedirect(request.getContextPath() + "/auth/login");
            return;
        }

        String email = (String) session.getAttribute("verifiedEmail");

        String googleOAuthUrl =
                "https://accounts.google.com/o/oauth2/v2/auth" +
                        "?client_id=204559903564-apf9kc8g9k6e5lfgn00r7fr9gpq4ptgp.apps.googleusercontent.com" +
                        "&redirect_uri=http://localhost:8080/badminton_court_booking/google-callback" +
                        "&response_type=code" +
                        "&scope=openid email profile" +
                        "&state=" + URLEncoder.encode(email, "UTF-8");

        response.sendRedirect(googleOAuthUrl);
    }

}
