/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */

package com.bcb.controller;

import java.io.IOException;
import java.io.PrintWriter;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 *
 * @author Nguyen Minh Duc
 */
public class GoogleLoginServlet extends HttpServlet {

    private static final String CLIENT_ID = "204559903564-apf9kc8g9k6e5lfgn00r7fr9gpq4ptgp.apps.googleusercontent.com";
    private static final String REDIRECT_URI = "http://localhost:8080/bcb/google-callback";
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String googleUrl =
            "https://accounts.google.com/o/oauth2/v2/auth"
            + "?client_id=" + CLIENT_ID
            + "&redirect_uri=" + URLEncoder.encode(REDIRECT_URI, StandardCharsets.UTF_8)
            + "&response_type=code"
            + "&scope=email%20profile";

        response.sendRedirect(googleUrl);
    }
}

