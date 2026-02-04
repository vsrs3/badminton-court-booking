package com.bcb.controller;

import com.bcb.dto.CustomerChangePassDTO;
import com.bcb.dto.CustomerProfileDTO;
import com.bcb.model.Account;
import com.bcb.service.CustomerProfileService;
import com.bcb.service.impl.CustomerProfileServiceImpl;
import com.bcb.dto.response.AccountResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;

@WebServlet(name = "CustomerController", urlPatterns = {"/customerController"})
@MultipartConfig(
        fileSizeThreshold = 1024 * 1024 * 2, // 2MB
        maxFileSize = 1024 * 1024 * 10,      // 10MB
        maxRequestSize = 1024 * 1024 * 50    // 50MB
)
public class CustomerController extends HttpServlet {

    private final CustomerAuthService authService = new CustomerAuthServiceImpl();
    private final CustomerProfileService profileService = new CustomerProfileServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");

        if(action != null) {
            switch (action) {
                case "logout" -> {
                    logout(request, response);
                }
                case "deleteAccount" -> {
                    deleteAccount(request, response);
                }
            }
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");

        if (action != null) {
            switch (action) {
                case "updateProfile" -> {
                    updateProfile(request, response);
                }
                case "updatePassword" -> {
                    updatePassword(request, response);
                }
            }
        }
    }

    private void updateProfile(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String fullName = request.getParameter("full_name");
        String email = request.getParameter("email");
        String phone = request.getParameter("phone");
        Part avatarFile = request.getPart("avatar");

        HttpSession session = request.getSession();
        Account account = (Account) session.getAttribute("account");
        try {
            CustomerProfileDTO dto = new CustomerProfileDTO(fullName, email, phone, avatarFile);
            if (account.getAvatarPath() != null && !account.getAvatarPath().isEmpty()) {
                dto.setAvatarPath(account.getAvatarPath());
            }

            Integer accountId = account.getAccountId();
            AccountResponse result = profileService.updateInfo(request, dto, accountId);

            if (result.isSuccess()) {
                session.setAttribute("account", result.getAccount());
                session.setAttribute("updateSuccess", result.getMessage());
            } else {
                session.setAttribute("updateError", result.getMessage());
            }
            response.sendRedirect(request.getContextPath() + "/profile?section=profile-info");

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
        }
    }

    private void updatePassword(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String oldPass = request.getParameter("oldPassword");
        String newPass = request.getParameter("newPassword");
        String confirmNewPass = request.getParameter("confirmPassword");

        HttpSession session = request.getSession();
        Account account = (Account) session.getAttribute("account");
        try{
            CustomerChangePassDTO dto = new CustomerChangePassDTO(oldPass, newPass, confirmNewPass);

            AccountResponse result = profileService.updatePassword(dto, account.getAccountId());
            if(result.isSuccess()){
                session.setAttribute("account", result.getAccount());
                session.setAttribute("updateSuccess", result.getMessage());
            } else {
                session.setAttribute("updateError", result.getMessage());
            }
            response.sendRedirect(request.getContextPath() + "/profile?section=change-password");

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
        }
    }

    private void logout (HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        try {
            HttpSession session = request.getSession();
            if (session != null) {
                session.invalidate();
            }
            response.sendRedirect(request.getContextPath() + "/home");

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private void deleteAccount (HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        Account account = (Account) session.getAttribute("account");

        if(account != null) {
            Integer customerId = account.getAccountId();
            AccountResponse result = authService.deleteAccount(customerId);

            if (result.isSuccess()) {
                session.setAttribute("successMessage", result.getMessage());
                response.sendRedirect(request.getContextPath() + "/home");
            } else {
                session.setAttribute("errorMessage", result.getMessage());
                response.sendRedirect(request.getContextPath() + "/profile?section=settings");
            }
        }
    }

}
