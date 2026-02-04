package com.bcb.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Controller for home page
 * Simply forwards to the JSP view
 */
@WebServlet(name = "HomeController", urlPatterns = {"/home", "/"})
public class HomeController extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Set context path for JSP to use in asset URLs
        request.setAttribute("contextPath", request.getContextPath());

        // Forward to home page
        request.getRequestDispatcher("/jsp/home/index.jsp").forward(request, response);
    }
}