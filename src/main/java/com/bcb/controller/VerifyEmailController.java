package com.bcb.controller;

import com.bcb.exception.BusinessException;
import com.bcb.service.AuthService;
import com.bcb.service.impl.AuthServiceImpl;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

public class VerifyEmailController extends HttpServlet {

    private final AuthService authService = new AuthServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String token = request.getParameter("token");

        // 1️⃣ Validate cơ bản
        if (token == null || token.trim().isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Thiếu token xác nhận");
            return;
        }

        try {

            // 2️⃣ Gọi service xử lý toàn bộ business logic
            authService.verifyEmail(token);

            // 3️⃣ Reset session
            HttpSession session = request.getSession(false);
            if (session != null) {
                session.invalidate();
            }

            session = request.getSession(true);
            session.setAttribute("verifiedEmail", "verified");

            // 4️⃣ Redirect sau khi verify thành công
            response.sendRedirect(request.getContextPath() + "/google-link");

        } catch (BusinessException e) {

            // Lỗi nghiệp vụ (token sai, hết hạn…)
            response.sendError(HttpServletResponse.SC_FORBIDDEN, e.getMessage());

        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    @Override
    public String getServletInfo() {
        return "Verify email and activate account";
    }
}