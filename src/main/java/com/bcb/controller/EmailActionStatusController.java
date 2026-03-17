package com.bcb.controller;

import com.bcb.utils.EmailActionSyncStore;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class EmailActionStatusController extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String purpose = trimToNull(request.getParameter("purpose"));
        String token = trimToNull(request.getParameter("token"));
        String email = trimToNull(request.getParameter("email"));

        response.setContentType("application/json;charset=UTF-8");

        if (purpose == null || (token == null && email == null)) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"status\":\"invalid\"}");
            return;
        }

        String effectiveToken = token;
        if (effectiveToken == null) {
            EmailActionSyncStore.ConfirmedAction action = EmailActionSyncStore.findConfirmedByEmail(purpose, email);
            if (action != null) {
                effectiveToken = action.getToken();
            }
        }

        EmailActionSyncStore.SyncEntry entry = effectiveToken == null
                ? null
                : EmailActionSyncStore.getConfirmed(purpose, effectiveToken);

        if (entry == null || effectiveToken == null) {
            response.getWriter().write("{\"status\":\"pending\"}");
            return;
        }

        String continueUrl = request.getContextPath()
                + "/email-action-continue?purpose="
                + URLEncoder.encode(purpose, StandardCharsets.UTF_8)
                + "&token="
                + URLEncoder.encode(effectiveToken, StandardCharsets.UTF_8);

        response.getWriter().write(
                "{\"status\":\"confirmed\",\"continueUrl\":\"" + escapeJson(continueUrl) + "\"}"
        );
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String escapeJson(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
