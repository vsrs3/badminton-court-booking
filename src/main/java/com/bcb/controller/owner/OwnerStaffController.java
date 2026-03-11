package com.bcb.controller.owner;

import java.util.*;
import java.util.stream.Collectors;
import java.util.Optional;

import com.bcb.service.SendEmailService;
import com.bcb.service.ManagementStaffService;
import com.bcb.service.impl.SendEmailServiceImpl;
import com.bcb.service.impl.ManagementStaffServiceImpl;

import java.io.IOException;
import com.bcb.service.impl.StaffServiceImpl;
import com.bcb.service.mybooking.MyBookingService;
import com.bcb.service.mybooking.impl.MyBookingServiceImpl;
import com.bcb.service.notification.NotificationService;
import com.bcb.service.notification.impl.NotificationServiceImpl;
import com.bcb.utils.PasswordUtil;
import com.bcb.service.StaffService;
import com.bcb.dto.notilication.NotificationDTO;
import com.bcb.dto.response.StaffRePassResponse;
import com.bcb.model.Account;
import com.bcb.model.Facility;

import jakarta.mail.MessagingException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/owner/staffs/*")
public class OwnerStaffController extends HttpServlet {

	// Service instance for staff location operations
	private final StaffService staffService = new StaffServiceImpl();

	// Service instance for staff profile operations (update info, avatar, etc.)
	private final ManagementStaffService staffProfileService = new ManagementStaffServiceImpl();

	// Service instance for sending staff email
	private final SendEmailService sendEmail = new SendEmailServiceImpl();
	
	// Service notification
	private final NotificationService notificationService = new NotificationServiceImpl();

	/**
	 * Handle GET requests for listing staff, viewing staff details, and deleting
	 * staff. URL patterns: - /owner/staffs/list : List all staff with pagination
	 * and search - /owner/staffs/view/{id} : View details of a specific staff
	 * member - /owner/staffs/toggle/{id} : Soft delete or activate a staff member
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		String pathInfo = request.getPathInfo();

		if (pathInfo == null || "/".equals(pathInfo)) {
			response.sendRedirect(request.getContextPath() + "/owner/staffs/list");
			return;
		}

		try {
			if (pathInfo.equals("/list")) {
				listStaff(request, response);

			} else if (pathInfo.startsWith("/view/")) {
				viewStaff(request, response);

			} else if (pathInfo.startsWith("/toggle/")) {
				toggleStatus(request, response);

			}
			
		} catch (Exception e) {
			request.setAttribute("error", e.getMessage());
			request.getRequestDispatcher("/jsp/owner/staffs/staff-list.jsp").forward(request, response);
		}

	}

	/**
	 * 
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String pathInfo = request.getPathInfo();

		if (pathInfo == null || "/".equals(pathInfo)) {
			response.sendRedirect(request.getContextPath() + "/owner/staffs/list");
			return;
		}

		try {
			if (pathInfo.equals("/update")) {
				updateStaffInfo(request, response);

			} else if (pathInfo.equals("/create")) {
				createStaff(request, response);

			} else if (pathInfo.equals("/reset-password")) {
				resetPassword(request, response);

			} 
			
		} catch (Exception e) {
			request.setAttribute("error", e.getMessage());
			request.getRequestDispatcher("/jsp/owner/staffs/staff-list.jsp").forward(request, response);
		}
	}

	/**
	 * Danh sách nhân viên với phân trang, tìm kiếm và filter isActive
	 * 
	 * @param request
	 * @param response
	 * @throws ServletException
	 * @throws IOException
	 */
	private void listStaff(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		int pageSize = 5;
		int page = 1;

		String pageParam = request.getParameter("page");
		String sizeParam = request.getParameter("size");

		if (pageParam != null) {
			try {
				page = Math.max(1, Integer.parseInt(pageParam));
			} 
			catch (NumberFormatException ignored) {
			}
		}

		if (sizeParam != null) {
			try {
				pageSize = Math.max(1, Math.min(100, Integer.parseInt(sizeParam)));
			} catch (NumberFormatException ignored) {
			}
		}

		int offset = (page - 1) * pageSize;

		String keyword = request.getParameter("keyword");
		boolean hasKeyword = keyword != null && !keyword.trim().isEmpty();

		// Xử lý filter isActive
		String isActiveParam = request.getParameter("isActive");
		Boolean isActiveFilter = null;
		if ("true".equals(isActiveParam)) {
			isActiveFilter = true;
		} else if ("false".equals(isActiveParam)) {
			isActiveFilter = false;
		}

		List<Account> staffs;
		int totalCount;

		if (hasKeyword) {
			String trimmedKeyword = keyword.trim();
			staffs = staffService.findByKeyword(trimmedKeyword, pageSize, offset);
			totalCount = staffService.countByKeyword(trimmedKeyword);
			request.setAttribute("keyword", trimmedKeyword);
		} else {
			staffs = staffService.findAll(pageSize, offset);
			totalCount = staffService.count();
		}

		// Lọc isActive phía Java nếu có filter (hoặc tích hợp vào SQL sau)
		if (isActiveFilter != null) {
			final boolean activeVal = isActiveFilter;
			staffs = staffs.stream().filter(u -> u.getIsActive() == activeVal)
					.collect(java.util.stream.Collectors.toList());
		}

		int totalPages = Math.max(1, (int) Math.ceil((double) totalCount / pageSize));

		// Lấy danh sách tất cả cơ sở để hiển thị trong dropdown (nếu cần)
		List<Facility> allFacilities = staffService.findFacilities();

		request.setAttribute("staffs", staffs);
		request.setAttribute("allFacilities", allFacilities);
		request.setAttribute("currentPage", page);
		request.setAttribute("pageSize", pageSize);
		request.setAttribute("totalPages", totalPages);
		request.setAttribute("totalRecords", totalCount);
		request.setAttribute("isActiveFilter", isActiveParam);

		request.getRequestDispatcher("/jsp/owner/staffs/staff-list.jsp").forward(request, response);
	}

	/**
	 * Xem chi tiết nhân viên
	 * 
	 * @param request
	 * @param response
	 * @throws ServletException
	 * @throws IOException
	 */
	private void viewStaff(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		String pathInfo = request.getPathInfo();
		String[] pathParts = pathInfo.split("/");
		if (pathParts.length != 3) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}

		try {
			int accountId = Integer.parseInt(pathParts[2]);

			// Lấy thông tin nhân viên
			Account staff = staffService.findById(accountId).orElseThrow(() -> new RuntimeException("Staff not found"));

			// Lấy danh sách cơ sở mà nhân viên này quản lý
			List<Facility> staffFacilities = staffService.findFacilitiesById(accountId);

			// Lấy danh sách tất cả cơ sở để hiển thị trong dropdown (nếu cần)
			List<Facility> allFacilities = staffService.findFacilities();

			int staffFacilityId = staffFacilities.isEmpty() ? 0 : staffFacilities.get(0).getFacilityId();

			request.setAttribute("staff", staff);
			request.setAttribute("staffFacilities", staffFacilities);
			request.setAttribute("allFacilities", allFacilities);
			request.setAttribute("staffFacilityId", staffFacilityId);
			request.getRequestDispatcher("/jsp/owner/staffs/staff-detail.jsp").forward(request, response);

		} catch (NumberFormatException e) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST);
		}
	}

	/**
	 * Chỉnh sửa Thông tin Nhân Viên
	 * 
	 * @param request
	 * @param response
	 * @throws ServletException
	 * @throws IOException
	 */
	private void updateStaffInfo(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		String accountIdParam = request.getParameter("accountId");
		String facilityIdParam = request.getParameter("facilityId");
		String fullName = request.getParameter("fullName");
		String email = request.getParameter("email");
		String phone = request.getParameter("phone");

		if (accountIdParam == null || accountIdParam.trim().isEmpty() || facilityIdParam == null
				|| facilityIdParam.trim().isEmpty()) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Account ID or Facility ID is required");
			return;
		}

		try {
			int accountId = Integer.parseInt(accountIdParam);
			int facilityId = Integer.parseInt(facilityIdParam);

			boolean success = staffProfileService.updateInfo(accountId, facilityId, fullName, email, phone);

			if (success) {
				response.sendRedirect(request.getContextPath() + "/owner/staffs/view/" + accountId);
			} else {
				request.setAttribute("error", "Failed to update staff information");
				request.getRequestDispatcher("/jsp/owner/staffs/staff-detail.jsp").forward(request, response);
			}

		} catch (NumberFormatException e) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid Account ID");
		}

	}

	/**
	 * Chuyển đổi trạng thái giữa active và inactive
	 * 
	 * @param request
	 * @param response
	 * @throws ServletException
	 * @throws IOException
	 */
	private void toggleStatus(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		String pathInfo = request.getPathInfo();
		String[] pathParts = pathInfo.split("/");

		if (pathParts.length != 3) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}

		try {
			int accountId = Integer.parseInt(pathParts[2]);
			String redirect = request.getParameter("redirect");
			String url;
			String forwardJsp;

			boolean success = staffProfileService.softDeleteAndActive(accountId);

			if ("detail".equals(redirect)) {
				url = request.getContextPath() + "/owner/staffs/view/" + accountId;
				forwardJsp = "/jsp/owner/staffs/staff-detail.jsp";
			} else {
				url = request.getContextPath() + "/owner/staffs/list";
				forwardJsp = "/jsp/owner/staffs/staff-list.jsp";
			}

			if (success) {
				response.sendRedirect(url);
			} else {
				request.setAttribute("error", "Failed to update staff status");
				request.getRequestDispatcher(forwardJsp).forward(request, response);
			}

		} catch (Exception e) {
			request.setAttribute("error", "Failed to update staff information");
			request.getRequestDispatcher("/jsp/owner/staffs/staff-detail.jsp").forward(request, response);
		}

	}

	/**
	 * Create a new staff
	 * 
	 * @param request
	 * @param response
	 * @throws ServletException
	 * @throws IOException
	 */
	private void createStaff(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		String fullName = request.getParameter("fullName");
		String email = request.getParameter("email");
		String phone = request.getParameter("phone");
		String facilityParam = request.getParameter("facilityId");

		try {
			int facilityId = Integer.parseInt(facilityParam);

			boolean success = staffService.createStaff(fullName, email, phone, facilityId);

			if (success) {
				try {
					String loginLink = request.getScheme() + "://" + request.getServerName() + ":"
							+ request.getServerPort() + request.getContextPath() + "/auth/login";

					// Send email to staff if create account successful
					sendEmail.sendWelcomeEmail(email, fullName, loginLink);

				} catch (Exception e) {
					System.out.print(e.getMessage());
				}

				response.sendRedirect(request.getContextPath() + "/owner/staffs/list");

			} else {
				request.setAttribute("error", "Failed to create a new staff");
				request.getRequestDispatcher("/jsp/owner/staffs/staff-list.jsp").forward(request, response);
			}

		} catch (Exception e) {
			request.setAttribute("error", "Exception error");
			request.getRequestDispatcher("/jsp/owner/staffs/staff-list.jsp").forward(request, response);
		}
	}

	/**
	 * Đặt lại mật khẩu tạm thời cho staff
	 * 
	 * @param request
	 * @param response
	 * @throws ServletException
	 * @throws IOException
	 */
	private void resetPassword(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		String idParam = request.getParameter("accountId");

		try {
			int accountId = Integer.parseInt(idParam);
			Account staff = staffService.findById(accountId).orElseThrow(() -> new RuntimeException("Staff not found"));
			String email = staff.getEmail();
			String fullName = staff.getFullName();

			String tempPassword = PasswordUtil.generateRandomPassword(10);
			String hassPassword = PasswordUtil.hashPassword(tempPassword);

			boolean success = staffProfileService.resetPassword(accountId, hassPassword);

			if (success) {
				try {
					String loginLink = request.getScheme() + "://" + request.getServerName() + ":"
							+ request.getServerPort() + request.getContextPath() + "/auth/login";

					// Send email to staff if create account successful
					sendEmail.resetStaffPassword(email, fullName, tempPassword, loginLink);
					
					// Send staff notification
					/*
					 * NotificationDTO dto = new NotificationDTO(); StaffRePassResponse description
					 * = new StaffRePassResponse();
					 * 
					 * dto.setAccountId(accountId); dto.setTitle(description.getTitle());
					 * dto.setContent(description.getContent());
					 * 
					 * notificationService.insertResetPassNotification(dto);
					 */
				} catch (Exception e) {
					throw new IllegalArgumentException("Lỗi khi gửi email chứa link đăng nhập hoặc thông báo cho nhân viên");
				}
				
				response.getWriter().write("{\"success\":true,\"tempPassword\":\"" + tempPassword + "\"}");
			} else {
				response.getWriter().write("{\"success\":false,\"message\":\"Không thể đặt lại mật khẩu\"}");
			}

		} catch (Exception e) {
			response.setContentType("application/json");
			response.setCharacterEncoding("UTF-8");
			response.getWriter().write("{\"success\":false,\"message\":\"Lỗi hệ thống\"}");
		}

	}

}
