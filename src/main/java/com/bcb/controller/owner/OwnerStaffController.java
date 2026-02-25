package com.bcb.controller.owner;

import java.util.*;
import java.io.IOException;
import com.bcb.service.impl.StaffServiceImpl;
import com.bcb.service.StaffService;
import com.bcb.model.Account;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/owner/staffs/*")
public class OwnerStaffController extends HttpServlet {
	
	private final StaffService staffService = new StaffServiceImpl();

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
                //viewStaff(request, response);

            } else if (pathInfo.startsWith("/delete/")) {
                //deleteStaff(request, response);

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
            if (pathInfo.equals("/edit")) {
                //viewStaff(request, response);

            } else if (pathInfo.equals("/kjdsbfjhd")) {
                //deleteStaff(request, response);

            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (Exception e) {
            request.setAttribute("error", e.getMessage());
            request.getRequestDispatcher("/jsp/owner/staffs/staff-list.jsp").forward(request, response);
        }
	}
	
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

        request.setAttribute("staffs", staffs);
        request.setAttribute("currentPage", page);
        request.setAttribute("pageSize", pageSize);
        request.setAttribute("totalPages", totalPages);
        request.setAttribute("totalRecords", totalCount);
        request.setAttribute("isActiveFilter", isActiveParam);
		
		request.getRequestDispatcher("/jsp/owner/staffs/staff-list.jsp").forward(request, response);
	}
	
}
