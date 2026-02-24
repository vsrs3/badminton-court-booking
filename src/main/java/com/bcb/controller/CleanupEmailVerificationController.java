package com.bcb.controller;

import com.bcb.repository.impl.EmailVerificationRepositoryImpl;
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
        System.out.println("❌ dddđ: " + token );
        if (token != null && !token.isEmpty()) {
            try {
                EmailVerificationRepositoryImpl dao = new EmailVerificationRepositoryImpl();
                dao.deleteByToken(token);
            } catch (Exception e) {
                throw new ServletException(e);
            }
        }
        response.setStatus(HttpServletResponse.SC_OK);
    }
}