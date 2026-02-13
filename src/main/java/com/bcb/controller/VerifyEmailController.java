package com.bcb.controller;

import com.bcb.dao.AccountDAO;
import com.bcb.dao.EmailVerificationDAO;
import com.bcb.model.Account;
import com.bcb.model.EmailVerification;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class VerifyEmailController extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String token = request.getParameter("token");

        // 0️⃣ Validate token
        if (token == null || token.trim().isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Thiếu token xác nhận");
            return;
        }

        EmailVerificationDAO evDao = new EmailVerificationDAO();
        AccountDAO accountDao = new AccountDAO();
        EmailVerification ev;

        try {
            ev = evDao.findByToken(token);
        } catch (Exception e) {
            throw new ServletException(e);
        }

        // 1️⃣ Token không tồn tại
        if (ev == null) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Token không hợp lệ");
            return;
        }

        // 2️⃣ Token hết hạn
        if (ev.isExpired()) {
            try {
                evDao.deleteByToken(token);
            } catch (Exception ex) {
                Logger.getLogger(VerifyEmailController.class.getName())
                        .log(Level.SEVERE, null, ex);
            }
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Token đã hết hạn");
            return;
        }

        try {
            // 🔒 3️⃣ CHẶN INSERT TRÙNG TỪ ĐẦU
            Account existing = accountDao.findByEmail(ev.getEmail());
            if (existing != null) {
                evDao.deleteByToken(token);
                response.sendRedirect(request.getContextPath() + "/google-link");
                return;
            }

            // 4️⃣ TẠO ACCOUNT TỪ BẢN GHI TẠM
            Account acc = new Account();
            acc.setEmail(ev.getEmail());
            acc.setPasswordHash(ev.getPasswordHash());
            acc.setFullName(ev.getFullName());
            acc.setPhone(ev.getPhone());
            acc.setRole(ev.getRole()); // CUSTOMER

            System.out.println("✅ Inserted account email = " + acc.getEmail());

            accountDao.register(acc);
            System.out.println("✅ Inserted account gggg ");

          // ✅ Chỉ tới đây khi insert OK
            evDao.deleteByToken(token);


            // 7️⃣ RESET SESSION & LƯU EMAIL ĐÃ VERIFY
            HttpSession session = request.getSession(false);
            if (session != null) {
                session.invalidate();
            }

            session = request.getSession(true);
            session.setAttribute("verifiedEmail", acc.getEmail());
            // 8️⃣ CHUYỂN SANG GOOGLE LINK (HOẶC LOGIN)
            response.sendRedirect(request.getContextPath() + "/google-link");

        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    @Override
    public String getServletInfo() {
        return "Verify email and activate account";
    }
}
