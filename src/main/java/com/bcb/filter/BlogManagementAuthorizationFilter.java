package com.bcb.filter;

import com.bcb.model.Account;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

@WebFilter("/blogs/manage/*")
public class BlogManagementAuthorizationFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        HttpSession session = httpRequest.getSession(false);
        if (session == null) {
            String contextPath = httpRequest.getContextPath();
            httpResponse.sendRedirect(contextPath + "/auth/login");
            return;
        }

        Account currentUser = (Account) session.getAttribute("account");
        if (currentUser == null) {
            String contextPath = httpRequest.getContextPath();
            httpResponse.sendRedirect(contextPath + "/auth/login");
            return;
        }

        String role = currentUser.getRole();
        if (!("ADMIN".equals(role) || "OWNER".equals(role) || "STAFF".equals(role))) {
            httpRequest.getRequestDispatcher("/jsp/error/403.jsp").forward(httpRequest, httpResponse);
            return;
        }

        chain.doFilter(request, response);
    }
}
