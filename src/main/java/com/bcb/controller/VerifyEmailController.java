/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */

package com.bcb.controller;
import com.bcb.dao.AccountDAO;
import com.bcb.dao.EmailVerificationDAO;
import com.bcb.model.Account;
import com.bcb.model.EmailVerification;
import java.io.IOException;
import java.io.PrintWriter;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.util.logging.Level;
import java.util.logging.Logger;

public class VerifyEmailController extends HttpServlet {
   
  
@Override
protected void doGet(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {

    String token = request.getParameter("token");

    if (token == null || token.isEmpty()) {
        response.sendError(400, "Thiếu token xác nhận");
        return;
    }

    EmailVerificationDAO evDao = new EmailVerificationDAO();
    EmailVerification ev;

    try {
        ev = evDao.findByToken(token);
    } catch (Exception e) {
        throw new ServletException(e);
    }

    // 1️⃣ Token không tồn tại
    if (ev == null) {
        response.sendError(403, "Token không hợp lệ");
        return;
    }

    // 2️⃣ Token hết hạn
    if (ev.isExpired()) {
        try {
            evDao.deleteByToken(token); // 🔥 XÓA LUÔN
        } catch (Exception ex) {
            Logger.getLogger(VerifyEmailController.class.getName()).log(Level.SEVERE, null, ex);
        }
    response.sendError(403, "Token đã hết hạn");
    return;
}
    // 3️⃣ Tạo Account từ EmailVerification
    Account acc = new Account();
    acc.setEmail(ev.getEmail());
    acc.setPasswordHash(ev.getPasswordHash());
    acc.setFullName(ev.getFullName());
    acc.setPhone(ev.getPhone());
    acc.setRole(ev.getRole());

    AccountDAO accountDao = new AccountDAO();

    try {
        // 4️⃣ LƯU ACCOUNT
        accountDao.register(acc);
        // 5️⃣ 🔥 XOÁ DỮ LIỆU TẠM SAU KHI LƯU THÀNH CÔNG
        evDao.deleteByToken(token);

    } catch (Exception e) {
        // ❌ Nếu có lỗi → KHÔNG xoá token (để retry)
        throw new ServletException(e);
    }

    // 6️⃣ Thành công
  HttpSession session = request.getSession();
session.invalidate();               // ❗ xoá session cũ
session = request.getSession(true); // tạo session mới
session.setAttribute("verifiedEmail", acc.getEmail());

response.sendRedirect(request.getContextPath() + "/google-link");

}


    /** 
     * Returns a short description of the servlet.
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>
     
     
}
