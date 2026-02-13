package com.bcb.controller;

import com.bcb.dao.EmailVerificationDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 *
 * @author Nguyen Minh Duc
 */
public class CleanupEmailVerificationController extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String token = request.getParameter("token");
        if (token != null && !token.isEmpty()) {
            try {
                EmailVerificationDAO dao = new EmailVerificationDAO();
                dao.deleteByToken(token);
            } catch (Exception e) {
                throw new ServletException(e);
            }
        }
        response.setStatus(HttpServletResponse.SC_OK);
    }
}