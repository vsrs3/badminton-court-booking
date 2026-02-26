package com.bcb.controller.owner;

import java.util.*;
import java.util.stream.Collectors;
import java.util.Optional;
import com.bcb.service.StaffProfilService;
import com.bcb.service.impl.StaffProfileServiceImpl;

import java.io.IOException;
import com.bcb.service.impl.StaffServiceImpl;
import com.bcb.service.StaffService;
import com.bcb.model.Account;
import com.bcb.model.Facility;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/owner/staffs/*")
public class OwnerStaffController extends HttpServlet {
	
	// Service instance for staff-related operations
	private final StaffService staffService = new StaffServiceImpl();
	
	// Service instance for staff profile operations (update info, avatar, etc.)
	private final StaffProfilService staffProfileService = new StaffProfileServiceImpl();

	/**
	 * Handle GET requests for listing staff, viewing staff details, and deleting staff.
	 * URL patterns:
	 * - /owner/staffs/list : List all staff with pagination and search
	 * - /owner/staffs/view/{id} : View details of a specific staff member
	 * - /owner/staffs/delete/{id} : Soft delete or activate a staff member
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

            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (Exception e) {
            request.setAttribute("error", e.getMessage());
            request.getRequestDispatcher("/jsp/owner/staffs/staff-list.jsp").forward(request, response);
        }
		
	}
	
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
            	
            } else if (pathInfo.equals("/update-location")) {
                //deleteStaff(request, response);

            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (Exception e) {
            request.setAttribute("error", e.getMessage());
            request.getRequestDispatcher("/jsp/owner/staffs/staff-list.jsp").forward(request, response);
        }
	}
	
	/**
	 * Danh sách nhân viên với phân trang, tìm kiếm và filter isActive
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
            } catch (NumberFormatException ignored) {
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
            staffs = staffs.stream()
                    .filter(u -> u.getIsActive() == activeVal)
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
			Account staff = staffService.findById(accountId)
			        .orElseThrow(() -> new RuntimeException("Staff not found"));
		
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
	
	
	private void updateStaffInfo(HttpServletRequest request, HttpServletResponse response) 
			throws ServletException, IOException {
		
		String accountIdParam = request.getParameter("accountId");
		String facilityIdParam = request.getParameter("facilityId");
		String fullName = request.getParameter("fullName");
		String email = request.getParameter("email");
		String phone = request.getParameter("phone");

		if (accountIdParam == null || accountIdParam.trim().isEmpty()
				|| facilityIdParam == null || facilityIdParam.trim().isEmpty()) {
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
			
			boolean success = staffProfileService.softDeleteAndActive(accountId);
			if (success) {
			    switch (redirect) {
			        case "detail":
			            response.sendRedirect(request.getContextPath() + "/owner/staffs/view/" + accountId);
			            break;
			        default:
			            response.sendRedirect(request.getContextPath() + "/owner/staffs/list");
			            break;
			    }
			} else {
			    request.setAttribute("error", "Failed to update staff status");
			    switch (redirect) {
			        case "detail":
			            request.getRequestDispatcher("/jsp/owner/staffs/staff-detail.jsp").forward(request, response);
			            break;
			        default:
			            request.getRequestDispatcher("/jsp/owner/staffs/staff-list.jsp").forward(request, response);
			            break;
			    }
			}
			
		} catch(Exception e) {
			request.setAttribute("error", "Failed to update staff information");
			request.getRequestDispatcher("/jsp/owner/staffs/staff-detail.jsp").forward(request, response);
		}
		
	}
	
}
