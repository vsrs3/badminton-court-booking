/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.bcb.controller;

import com.bcb.dao.AccountDAO;
import com.bcb.model.Account;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import org.mindrot.jbcrypt.BCrypt;
import java.util.UUID;
import com.bcb.utils.MailUtil;
import com.bcb.dao.EmailVerificationDAO;
import java.sql.Timestamp;


public class RegisterController extends HttpServlet {

 @Override
protected void doPost(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {

    // 1️⃣ LẤY DỮ LIỆU TỪ FORM
    String email = request.getParameter("email");
    String phone = request.getParameter("phone");
    String password = request.getParameter("password");
    String fullName = request.getParameter("fullName");

    AccountDAO accountDao = new AccountDAO();

    // 2️⃣ KIỂM TRA EMAIL ĐÃ TỒN TẠI CHƯA
    try {
        if (accountDao.isEmailExists(email)) {
            request.setAttribute("oldEmail", email);
            request.setAttribute("oldFullName", fullName);
            request.setAttribute("oldPasword", password);
            request.setAttribute("oldPhone", phone);

            // ❌ Email đã tồn tại → quay lại form + thông báo
            request.setAttribute("error", "Tài khoản với email này đã tồn tại");
            request.getRequestDispatcher("register.jsp")
                   .forward(request, response);
            return; // ⛔ RẤT QUAN TRỌNG
        }
    } catch (Exception e) {
        throw new ServletException(e);
    }

    // 3️⃣ HASH PASSWORD
    String hash = BCrypt.hashpw(password, BCrypt.gensalt());

    // 4️⃣ TẠO ACCOUNT TẠM
    Account acc = new Account();
    acc.setEmail(email);
    acc.setPhone(phone);
    acc.setPasswordHash(hash);
    acc.setFullName(fullName);
    acc.setRole("USER");

    // 5️⃣ TẠO TOKEN + THỜI GIAN HẾT HẠN
    String token = UUID.randomUUID().toString();
    Timestamp expireAt = new Timestamp(
    System.currentTimeMillis() + 60 * 1000
    );

    EmailVerificationDAO evDao = new EmailVerificationDAO();

    // 6️⃣ LƯU THÔNG TIN ĐĂNG KÝ TẠM VÀO EmailVerification
    try {
        evDao.savePendingRegister(
            acc.getEmail(),
            acc.getPasswordHash(),
            acc.getFullName(),
            acc.getPhone(),
            acc.getRole(),
            token,
            expireAt
        );
    } catch (Exception e) {
        throw new ServletException(e);
    }

    // 7️⃣ GỬI EMAIL XÁC NHẬN
    String verifyLink =
        "http://localhost:8080/bcb/verify-email?token=" + token;

    MailUtil.sendVerifyEmail(email, verifyLink);

    // 8️⃣ CHUYỂN SANG TRANG CHECK EMAIL
    response.sendRedirect("check-email.jsp?token=" + token);

}



}
