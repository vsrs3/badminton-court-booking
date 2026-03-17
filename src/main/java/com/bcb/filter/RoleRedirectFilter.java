package com.bcb.filter;

import com.bcb.model.Account;
import com.bcb.utils.AuthRedirectUtil;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/**
 * Redirect logged-in users from root path to the correct home page.
 */
@WebFilter("/*")
public class RoleRedirectFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String requestURI = httpRequest.getRequestURI();
        String contextPath = httpRequest.getContextPath();
        String path = requestURI.substring(contextPath.length());

        if (!path.equals("/") && !path.equals("")) {
            chain.doFilter(request, response);
            return;
        }

        HttpSession session = httpRequest.getSession(false);
        if (session == null) {
            chain.doFilter(request, response);
            return;
        }

        Account currentUser = (Account) session.getAttribute("account");
        if (currentUser == null) {
            chain.doFilter(request, response);
            return;
        }

        String redirectPath = AuthRedirectUtil.resolvePathByRole(currentUser.getRole());
        if ("/".equals(redirectPath)) {
            chain.doFilter(request, response);
            return;
        }

        httpResponse.sendRedirect(contextPath + redirectPath);
    }
}
