/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */

package com.bcb.controller;

import com.bcb.dao.AccountDAO;
import com.bcb.model.Account;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.io.PrintWriter;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONObject;

/**
 *
 * @author Nguyen Minh Duc
 */

public class GoogleCallbackController extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        String code = request.getParameter("code");

        if (code == null) {
            response.sendError(400, "Missing code");
            return;
        }

        // 1️⃣ Exchange code -> access token
        String accessToken = GoogleOAuthUtil.getAccessToken(code);

        // 2️⃣ Call Google UserInfo API
        JsonObject userInfo = (JsonObject) GoogleOAuthUtil.getUserInfo(accessToken);

        String googleId = userInfo.get("sub").getAsString();   // 🔥 GOOGLE ID
        String email = userInfo.get("email").getAsString();

        AccountDAO accountDao = new AccountDAO();
        Account acc = null;
        try {
            acc = accountDao.findByEmail(email);
            String stateEmail = request.getParameter("state");

if (stateEmail == null || !stateEmail.equals(email)) {
    request.setAttribute("error", "Tài khoản Google chưa được liên kết");
    request.getRequestDispatcher("login.jsp").forward(request, response);
    return;
}
        } catch (Exception ex) {
            Logger.getLogger(GoogleCallbackController.class.getName()).log(Level.SEVERE, null, ex);
        }if (acc != null && acc.getGoogleId() == null) {
            try {
                accountDao.updateGoogleId(acc.getAccountId(), googleId);
            } catch (Exception ex) {
                Logger.getLogger(GoogleCallbackController.class.getName()).log(Level.SEVERE, null, ex);
            }}
        response.sendRedirect("index.jsp?linked=google");
}}