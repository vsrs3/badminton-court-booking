package com.bcb.controller;

import com.bcb.dto.RegisterRequestDTO;
import com.bcb.exception.BusinessException;
import com.bcb.service.AuthService;
import com.bcb.service.impl.AuthServiceImpl;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public class RegisterController extends HttpServlet {

    private final AuthService authService = new AuthServiceImpl();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        String email = request.getParameter("email");
        String phone = request.getParameter("phone");
        String password = request.getParameter("password");
        String rePassword = request.getParameter("repassword");
        String fullName = request.getParameter("fullName");

        RegisterRequestDTO dto = new RegisterRequestDTO();
        dto.setEmail(email);
        dto.setPhone(phone);
        dto.setPassword(password);
        dto.setFullName(fullName);

        try {
            if (password == null || !password.equals(rePassword)) {
                throw new BusinessException("Mật khẩu nhập lại không khớp");
            }

            String token = authService.register(dto);

            response.sendRedirect(
                    request.getContextPath()
                            + "/jsp/common/check-email.jsp?token=" + token
            );

        } catch (BusinessException e) {
            request.setAttribute("error", e.getMessage());
            request.setAttribute("oldEmail", dto.getEmail());
            request.setAttribute("oldFullName", dto.getFullName());
            request.setAttribute("oldPhone", dto.getPhone());

            request.getRequestDispatcher("/jsp/auth/register.jsp")
                    .forward(request, response);

        } catch (Exception e) {
            throw new ServletException(e);
        }
    }
}
