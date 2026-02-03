/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */

package com.bcb.controller;

import com.bcb.dao.AccountDAO;
import com.bcb.model.Account;
import java.io.IOException;
import java.io.PrintWriter;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 *
 * @author Nguyen Minh Duc
 */
public class LoginServlet extends HttpServlet {

    
    private boolean isValidGmail(String email) {
        String gmailRegex = "^[a-zA-Z0-9._%+-]+@gmail\\.com$";
        return email.matches(gmailRegex);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.getRequestDispatcher("login.jsp").forward(request, response);
    }

    @Override
protected void doPost(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {

    String email = request.getParameter("email");
    String password = request.getParameter("password");

    AccountDAO dao = new AccountDAO();
    Account acc;

    try {
        acc = dao.loginByEmailPassword(email, password);
    } catch (Exception e) {
        throw new ServletException(e);
    }

    if (acc != null) {
        HttpSession session = request.getSession();
        session.setAttribute("account", acc);
        response.sendRedirect("index.jsp");
    } else {
        request.setAttribute("error", "Tài khoản không tồn tại hoặc sai mật khẩu");
        request.getRequestDispatcher("login.jsp").forward(request, response);
    }
}

}
