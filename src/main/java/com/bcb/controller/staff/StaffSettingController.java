package com.bcb.controller.staff;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;

import java.io.IOException;

import com.bcb.dto.CustomerChangePassDTO;
import com.bcb.dto.CustomerProfileDTO;
import com.bcb.dto.response.AccountResponse;
import com.bcb.model.Account;
import com.bcb.service.CustomerProfileService;
import com.bcb.service.impl.CustomerProfileServiceImpl;

//@WebServlet("/staff/setting/*")
@MultipartConfig(
    fileSizeThreshold = 1024 * 1024,       // 1MB - bộ nhớ tạm trước khi lưu file
    maxFileSize       = 1024 * 1024 * 5,   // 5MB - max mỗi file
    maxRequestSize    = 1024 * 1024 * 10   // 10MB - max toàn request
)
public class StaffSettingController extends HttpServlet {
	
	private final CustomerProfileService profileService = new CustomerProfileServiceImpl();
    
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		String pathInfo = request.getPathInfo();

		if (pathInfo == null || "/".equals(pathInfo)) {
			request.getRequestDispatcher("/jsp/staff/setting/setting-form.jsp").forward(request, response);
			return;
		}
		
		  String action = pathInfo.replaceFirst("^/", ""); // "profile" | "change-password"

	        switch (action) {
	            case "profile":
	                request.getRequestDispatcher("/jsp/staff/setting/staff-profile.jsp")
	                       .forward(request, response);
	                break;
	            case "change-password":
	                request.getRequestDispatcher("/jsp/staff/setting/staff-password.jsp")
	                       .forward(request, response);
	                break;
	            default:
	                response.sendError(HttpServletResponse.SC_NOT_FOUND);
	        }
		
	}

	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		String pathInfo = request.getPathInfo();
		
		if (pathInfo == null || "/".equals(pathInfo)) {
			request.getRequestDispatcher("/jsp/staff/setting/setting-form.jsp").forward(request, response);
			return;
		}
		
		 String action = pathInfo.replaceFirst("^/", ""); // "profile" | "change-password"
		 
		 switch (action) {
	         case "profile":
	             updateProfile ( request,  response);
	             break;
	         case "change-password":
	             updatePassword ( request, response);
	             break;
	         default:
	             response.sendError(HttpServletResponse.SC_NOT_FOUND);
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
        	
        	// Debug ngay tại đây
            System.out.println("full_name from form: " + fullName);
            System.out.println("email: " + email);
            System.out.println("phone: " + phone);
        	
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
            response.sendRedirect(request.getContextPath() + "/staff/setting/profile");

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
            response.sendRedirect(request.getContextPath() + "/staff/setting/change-password");

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
        }
    }

}
