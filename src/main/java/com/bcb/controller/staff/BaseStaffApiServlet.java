package com.bcb.controller.staff;

import com.bcb.utils.api.JsonResponseUtil;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;

public abstract class BaseStaffApiServlet extends HttpServlet {

    protected String readRequestBody(HttpServletRequest request) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }
        return sb.toString();
    }

    protected void writeJson(HttpServletResponse response, int status, String json) throws IOException {
        response.setStatus(status);
        response.getWriter().print(json);
    }

    protected void writeJson(HttpServletResponse response, String json) throws IOException {
        response.getWriter().print(json);
    }

    protected void writeError(HttpServletResponse response, int status, String message) throws IOException {
        writeJson(response, status, JsonResponseUtil.error(message));
    }

    protected void writeError(HttpServletResponse response, String message) throws IOException {
        writeJson(response, JsonResponseUtil.error(message));
    }
}
