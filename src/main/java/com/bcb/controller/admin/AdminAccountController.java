package com.bcb.controller.admin;

import com.bcb.model.Account;
import com.bcb.service.AccountManagementService;
import com.bcb.service.impl.AccountManagementServiceImpl;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * Controller for Admin Account Management
 * URL: /admin/accounts/*
 */
@WebServlet(name = "AdminAccountController", urlPatterns = {"/admin/accounts/*"})
public class AdminAccountController extends HttpServlet {

    private final AccountManagementService accountService = new AccountManagementServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String pathInfo = request.getPathInfo();
        System.out.print("hello");
        if (pathInfo == null || "/".equals(pathInfo)) {
            response.sendRedirect(request.getContextPath() + "/admin/accounts/list");
            return;
        }

        try {
            if ("/list".equals(pathInfo)) {
                listAccounts(request, response);
            } else if (pathInfo.startsWith("/view/")) {
                viewAccount(request, response);
            } else if (pathInfo.startsWith("/toggle/")) {
                toggleStatus(request, response);
            } else if (pathInfo.startsWith("/delete/")) {
                deleteAccount(request, response);
            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (Exception e) {
            request.setAttribute("error", "Đã xảy ra lỗi: " + e.getMessage());
            request.getRequestDispatcher("/jsp/admin/accounts/account-list.jsp").forward(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String pathInfo = request.getPathInfo();

        if (pathInfo == null || "/".equals(pathInfo)) {
            response.sendRedirect(request.getContextPath() + "/admin/accounts/list");
            return;
        }

        try {
            if ("/update".equals(pathInfo)) {
                updateAccount(request, response);
            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (Exception e) {
            request.setAttribute("error", "Đã xảy ra lỗi: " + e.getMessage());
            request.getRequestDispatcher("/jsp/admin/accounts/account-list.jsp").forward(request, response);
        }
    }

    /**
     * List accounts with pagination, search, and role filter
     */
    private void listAccounts(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        int pageSize = 8;
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

        String roleFilter = request.getParameter("role");
        boolean hasRole = roleFilter != null && !roleFilter.trim().isEmpty();

        String statusFilter = request.getParameter("status");

        List<Account> accounts;
        int totalCount;

        if (hasKeyword && hasRole) {
            String trimmedKeyword = keyword.trim();
            accounts = accountService.findByKeywordAndRole(trimmedKeyword, roleFilter, pageSize, offset);
            totalCount = accountService.countByKeywordAndRole(trimmedKeyword, roleFilter);
            request.setAttribute("keyword", trimmedKeyword);
        } else if (hasKeyword) {
            String trimmedKeyword = keyword.trim();
            accounts = accountService.findByKeyword(trimmedKeyword, pageSize, offset);
            totalCount = accountService.countByKeyword(trimmedKeyword);
            request.setAttribute("keyword", trimmedKeyword);
        } else if (hasRole) {
            accounts = accountService.findByRole(roleFilter, pageSize, offset);
            totalCount = accountService.countByRole(roleFilter);
        } else {
            accounts = accountService.findAll(pageSize, offset);
            totalCount = accountService.count();
        }

        // Filter by status in Java (active/inactive)
        if (statusFilter != null && !statusFilter.isEmpty()) {
            boolean activeVal = "true".equals(statusFilter);
            accounts = accounts.stream()
                    .filter(a -> a.getIsActive() == activeVal)
                    .collect(java.util.stream.Collectors.toList());
        }

        int totalPages = Math.max(1, (int) Math.ceil((double) totalCount / pageSize));

        request.setAttribute("accounts", accounts);
        request.setAttribute("currentPage", page);
        request.setAttribute("pageSize", pageSize);
        request.setAttribute("totalPages", totalPages);
        request.setAttribute("totalRecords", totalCount);
        request.setAttribute("roleFilter", roleFilter);
        request.setAttribute("statusFilter", statusFilter);

        request.getRequestDispatcher("/jsp/admin/accounts/account-list.jsp").forward(request, response);
    }

    /**
     * View account detail
     */
    private void viewAccount(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String pathInfo = request.getPathInfo();
        String[] pathParts = pathInfo.split("/");

        if (pathParts.length != 3) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        try {
            int accountId = Integer.parseInt(pathParts[2]);
            Optional<Account> opt = accountService.findById(accountId);

            if (opt.isEmpty()) {
                request.setAttribute("error", "Không tìm thấy tài khoản #" + accountId);
                response.sendRedirect(request.getContextPath() + "/admin/accounts/list");
                return;
            }

            request.setAttribute("account", opt.get());
            request.getRequestDispatcher("/jsp/admin/accounts/account-detail.jsp").forward(request, response);

        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "ID không hợp lệ");
        }
    }

    /**
     * Update account information
     */
    private void updateAccount(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String accountIdParam = request.getParameter("accountId");
        String fullName = request.getParameter("fullName");
        String email = request.getParameter("email");
        String phone = request.getParameter("phone");
        String role = request.getParameter("role");

        if (accountIdParam == null || accountIdParam.trim().isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Account ID is required");
            return;
        }

        try {
            int accountId = Integer.parseInt(accountIdParam);
            String error = accountService.updateAccountInfo(accountId, fullName, email, phone, role);

            if (error == null) {
                // Success
                response.sendRedirect(request.getContextPath() + "/admin/accounts/view/" + accountId + "?success=1");
            } else {
                // Validation error - reload the detail page with error
                Optional<Account> opt = accountService.findById(accountId);
                if (opt.isPresent()) {
                    request.setAttribute("account", opt.get());
                }
                request.setAttribute("error", error);
                request.getRequestDispatcher("/jsp/admin/accounts/account-detail.jsp").forward(request, response);
            }

        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid Account ID");
        }
    }

    /**
     * Toggle account status (activate/deactivate)
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

            accountService.toggleStatus(accountId);

            if ("detail".equals(redirect)) {
                response.sendRedirect(request.getContextPath() + "/admin/accounts/view/" + accountId);
            } else {
                response.sendRedirect(request.getContextPath() + "/admin/accounts/list");
            }

        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid Account ID");
        }
    }

    /**
     * Delete account permanently
     */
    private void deleteAccount(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String pathInfo = request.getPathInfo();
        String[] pathParts = pathInfo.split("/");

        if (pathParts.length != 3) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        try {
            int accountId = Integer.parseInt(pathParts[2]);
            boolean success = accountService.deleteAccount(accountId);

            if (success) {
                response.sendRedirect(request.getContextPath() + "/admin/accounts/list?deleted=1");
            } else {
                response.sendRedirect(request.getContextPath() + "/admin/accounts/list?error=delete_failed");
            }

        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid Account ID");
        }
    }
}
