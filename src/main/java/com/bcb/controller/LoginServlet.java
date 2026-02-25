package com.bcb.controller;
import com.bcb.model.Account;
import com.bcb.repository.impl.AccountRepositoryImpl;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
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

        AccountRepositoryImpl dao = new AccountRepositoryImpl();
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
