package com.bcb.controller;

import com.bcb.dao.AccountDAO;
import com.bcb.model.Account;
import com.google.gson.JsonObject;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GoogleCallbackController extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        String code = request.getParameter("code");

        if (code == null) {
            response.sendError(400, "Missing code");
            return;
        }

        try {
            // 1️⃣ Exchange code -> access token
            String accessToken = GoogleOAuthUtil.getAccessToken(code);

            // 2️⃣ Get Google user info
            JsonObject userInfo =
                    (JsonObject) GoogleOAuthUtil.getUserInfo(accessToken);

            String googleId = userInfo.get("sub").getAsString();
            String googleEmail = userInfo.get("email").getAsString();

            AccountDAO accountDao = new AccountDAO();
            HttpSession session = request.getSession(false);

            // =====================================================
            // 🔵 LUỒNG 1: SAU KHI ĐĂNG KÝ (CÓ verifiedEmail)
            // =====================================================
            if (session != null &&
                    session.getAttribute("verifiedEmail") != null) {

                String verifiedEmail =
                        (String) session.getAttribute("verifiedEmail");

                // ❌ Nếu chọn sai Gmail
                if (!verifiedEmail.equalsIgnoreCase(googleEmail)) {

                    request.setAttribute("error",
                            "Đây không phải tài khoản email mà bạn đăng ký trước đó.");

                    request.getRequestDispatcher("/jsp/common/google-error.jsp")
                            .forward(request, response);
                    return;
                }

                // ✅ Email đúng → lấy account theo email
                Account acc = accountDao.findByEmail(googleEmail);

                if (acc == null) {
                    response.sendRedirect(request.getContextPath()
                            + "/auth/login");
                    return;
                }

                // Nếu chưa có google_id → cập nhật
                if (acc.getGoogleId() == null) {
                    accountDao.updateGoogleId(
                            acc.getAccountId(),
                            googleId
                    );
                }

                // Login
                session.setAttribute("account", acc);
                session.removeAttribute("verifiedEmail");

                response.sendRedirect(request.getContextPath() + "/");
                return;
            }

            // =====================================================
            // 🟢 LUỒNG 2: ĐĂNG NHẬP BẰNG GOOGLE
            // =====================================================

            Account acc = accountDao.findByGoogleId(googleId);

            if (acc == null) {
                request.setAttribute("error",
                        "Tài khoản Google này không tồn tại trong hệ thống.");
                request.getRequestDispatcher("/jsp/auth/login.jsp")
                        .forward(request, response);
                return;
            }

            if (!acc.getIsActive()) {
                request.setAttribute("error",
                        "Tài khoản đã bị khóa.");
                request.getRequestDispatcher("/jsp/auth/login.jsp")
                        .forward(request, response);
                return;
            }

            // Login
            session = request.getSession(true);
            session.setAttribute("account", acc);
            session.setAttribute("accountId", acc.getAccountId());
            session.setAttribute("email", acc.getEmail());
            session.setAttribute("fullName", acc.getFullName());
            session.setAttribute("role", acc.getRole());

            session.setMaxInactiveInterval(30 * 60);

            // Redirect theo role
            String redirectUrl;
            switch (acc.getRole()) {
                case "ADMIN":
                    redirectUrl = request.getContextPath() + "/admin/dashboard";
                    break;
                case "OWNER":
                    redirectUrl = request.getContextPath() + "/owner/dashboard";
                    break;
                case "STAFF":
                    redirectUrl = request.getContextPath() + "/staff/dashboard";
                    break;
                default:
                    redirectUrl = request.getContextPath() + "/";
            }

            response.sendRedirect(redirectUrl);

        } catch (Exception ex) {
            Logger.getLogger(getClass().getName())
                    .log(Level.SEVERE, null, ex);
            throw new ServletException(ex);
        }
    }





}
