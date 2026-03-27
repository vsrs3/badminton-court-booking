package com.bcb.controller;

import com.bcb.model.Account;
import com.bcb.service.AuthService;
import com.bcb.service.impl.AuthServiceImpl;
import com.bcb.utils.AuthRedirectUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

@WebServlet(name = "AuthController", urlPatterns = {"/auth/*"})
public class AuthController extends HttpServlet {

    private AuthService authService;

    @Override
    public void init() throws ServletException {
        super.init();
        this.authService = new AuthServiceImpl();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String pathInfo = request.getPathInfo();
        if (pathInfo == null) {
            response.sendRedirect(request.getContextPath() + "/auth/login");
            return;
        }

        switch (pathInfo) {
            case "/login":
                HttpSession session = request.getSession(false);
                if (session != null && session.getAttribute("account") != null) {
                    Account account = (Account) session.getAttribute("account");
                    response.sendRedirect(request.getContextPath()
                            + AuthRedirectUtil.resolvePathByRole(account.getRole()));
                    return;
                }

                request.getRequestDispatcher("/jsp/auth/login.jsp").forward(request, response);
                break;

            case "/logout":
                handleLogout(request, response);
                break;

            case "/register":
            case "/forgot-password":
                response.sendRedirect(request.getContextPath() + "/auth/login");
                break;

            default:
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String pathInfo = request.getPathInfo();
        if (pathInfo == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        switch (pathInfo) {
            case "/login":
                handleLogin(request, response);
                break;

            default:
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    /**
     * Authenticates a user and initializes the session for role-based routing.
     */
    private void handleLogin(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        String email = request.getParameter("email");
        String password = request.getParameter("password");
        String rememberMe = request.getParameter("rememberMe");

        if (email == null || email.trim().isEmpty()
                || password == null || password.trim().isEmpty()) {
            request.setAttribute("error", "Email và mật khẩu không được để trống");
            request.getRequestDispatcher("/jsp/auth/login.jsp").forward(request, response);
            return;
        }

        try {
            Account account = authService.authenticate(email.trim(), password);

            HttpSession session = request.getSession(true);
            session.setAttribute("account", account);
            session.setAttribute("accountId", account.getAccountId());
            session.setAttribute("email", account.getEmail());
            session.setAttribute("fullName", account.getFullName());
            session.setAttribute("role", account.getRole());

            if ("on".equals(rememberMe)) {
                session.setMaxInactiveInterval(7 * 24 * 60 * 60);
            } else {
                session.setMaxInactiveInterval(30 * 60);
            }

            response.sendRedirect(request.getContextPath()
                    + AuthRedirectUtil.resolvePathByRole(account.getRole()));
        } catch (RuntimeException e) {
            request.setAttribute("error", "Email hoặc mật khẩu không đúng");
            request.setAttribute("email", email);
            request.getRequestDispatcher("/jsp/auth/login.jsp").forward(request, response);
        }
    }

    private void handleLogout(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }

        response.sendRedirect(request.getContextPath() + "/auth/login");
    }
}
