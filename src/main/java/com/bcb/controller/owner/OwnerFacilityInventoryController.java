package com.bcb.controller.owner;

import com.bcb.model.FacilityInventory;
import com.bcb.model.Inventory;
import com.bcb.service.FacilityInventoryService;
import com.bcb.service.InventoryService;
import com.bcb.service.impl.FacilityInventoryServiceImpl;
import com.bcb.service.impl.InventoryServiceImpl;
import com.bcb.utils.BreadcrumbUtils;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

public class OwnerFacilityInventoryController extends HttpServlet {

    private InventoryService inventoryService;
    private FacilityInventoryService facilityInventoryService;

    private static final int ASSIGNED_PAGE_SIZE = 5;
    private static final int INVENTORY_PAGE_SIZE = 5;

    @Override
    public void init() {
        inventoryService = new InventoryServiceImpl();
        facilityInventoryService = new FacilityInventoryServiceImpl();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String pathInfo = request.getPathInfo();

        if (pathInfo == null || pathInfo.equals("/")) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Facility ID missing");
            return;
        }

        int facilityId;
        try {
            facilityId = Integer.parseInt(pathInfo.substring(1));
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid facility ID");
            return;
        }

        String assignedKeyword = trimToNull(request.getParameter("assignedKeyword"));
        String inventoryKeyword = trimToNull(request.getParameter("inventoryKeyword"));

        int assignedPage = parsePage(request.getParameter("assignedPage"));
        int inventoryPage = parsePage(request.getParameter("inventoryPage"));

        int assignedOffset = (assignedPage - 1) * ASSIGNED_PAGE_SIZE;
        int inventoryOffset = (inventoryPage - 1) * INVENTORY_PAGE_SIZE;

        // ===== Block 1: Đồ gán sân =====
        List<FacilityInventory> assignedItems =
                facilityInventoryService.getByFacilityId(facilityId, ASSIGNED_PAGE_SIZE, assignedOffset, assignedKeyword);

        int assignedTotal = facilityInventoryService.countByFacilityId(facilityId, assignedKeyword);
        int assignedTotalPages = (int) Math.ceil((double) assignedTotal / ASSIGNED_PAGE_SIZE);
        if (assignedTotalPages == 0) {
            assignedTotalPages = 1;
        }

        // ===== Block 2: Kho đồ =====
        List<Inventory> availableInventories =
                inventoryService.getActiveNotAssignedToFacilityWithPagination(
                        facilityId, INVENTORY_PAGE_SIZE, inventoryOffset, inventoryKeyword
                );

        int inventoryTotal = inventoryService.countActiveNotAssignedToFacility(facilityId, inventoryKeyword);
        int inventoryTotalPages = (int) Math.ceil((double) inventoryTotal / INVENTORY_PAGE_SIZE);
        if (inventoryTotalPages == 0) {
            inventoryTotalPages = 1;
        }

        request.setAttribute("facilityId", facilityId);

        request.setAttribute("assignedItems", assignedItems);
        request.setAttribute("assignedKeyword", assignedKeyword);
        request.setAttribute("assignedCurrentPage", assignedPage);
        request.setAttribute("assignedTotalPages", assignedTotalPages);
        request.setAttribute("assignedPageSize", ASSIGNED_PAGE_SIZE);

        request.setAttribute("inventories", availableInventories);
        request.setAttribute("inventoryKeyword", inventoryKeyword);
        request.setAttribute("inventoryCurrentPage", inventoryPage);
        request.setAttribute("inventoryTotalPages", inventoryTotalPages);
        request.setAttribute("inventoryPageSize", INVENTORY_PAGE_SIZE);

        BreadcrumbUtils.builder(request)
                .dashboard()
                .facilityList()
                .active("Kho đồ")
                .build();

        request.getRequestDispatcher("/jsp/owner/facility/facility-inventory.jsp")
                .forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String facilityParam = request.getParameter("facilityId");

        if (facilityParam == null || facilityParam.isBlank()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Facility ID missing");
            return;
        }

        int facilityId;
        try {
            facilityId = Integer.parseInt(facilityParam);
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid facility ID");
            return;
        }

        String action = request.getParameter("action");
        if (action == null || action.isBlank()) {
            response.sendRedirect(request.getContextPath() + "/owner/facility/inventory/" + facilityId);
            return;
        }

        try {
            switch (action) {
                case "assign":
                    handleAssign(request, facilityId);
                    break;
                case "updateQuantity":
                    handleUpdateQuantity(request);
                    break;
                case "remove":
                    handleRemove(request);
                    break;
                default:
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid action");
                    return;
            }
        } catch (IllegalArgumentException e) {
            request.getSession().setAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            request.getSession().setAttribute("errorMessage", "Đã xảy ra lỗi khi xử lý kho đồ.");
        }

        response.sendRedirect(request.getContextPath() + "/owner/facility/inventory/" + facilityId);
    }

    private void handleAssign(HttpServletRequest request, int facilityId) {
        String inventoryIdParam = request.getParameter("inventoryId");

        if (inventoryIdParam == null || inventoryIdParam.isBlank()) {
            throw new IllegalArgumentException("Thiếu mã sản phẩm để gán.");
        }

        int inventoryId;
        try {
            inventoryId = Integer.parseInt(inventoryIdParam);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Mã sản phẩm không hợp lệ.");
        }

        if (facilityInventoryService.existsByFacilityAndInventory(facilityId, inventoryId)) {
            throw new IllegalArgumentException("Sản phẩm này đã được gán cho sân.");
        }

        facilityInventoryService.assignToFacility(facilityId, inventoryId, 0);
        request.getSession().setAttribute("successMessage", "Gán sản phẩm vào sân thành công.");
    }

    private void handleUpdateQuantity(HttpServletRequest request) {
        String facilityInventoryIdParam = request.getParameter("facilityInventoryId");
        String totalQuantityParam = request.getParameter("totalQuantity");

        if (facilityInventoryIdParam == null || facilityInventoryIdParam.isBlank()) {
            throw new IllegalArgumentException("Thiếu mã đồ gán sân.");
        }

        if (totalQuantityParam == null || totalQuantityParam.isBlank()) {
            throw new IllegalArgumentException("Vui lòng nhập số lượng sản phẩm.");
        }

        int facilityInventoryId;
        int totalQuantity;

        try {
            facilityInventoryId = Integer.parseInt(facilityInventoryIdParam);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Mã đồ gán sân không hợp lệ.");
        }

        try {
            totalQuantity = Integer.parseInt(totalQuantityParam);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Số lượng sản phẩm phải là số nguyên.");
        }

        if (totalQuantity < 0) {
            throw new IllegalArgumentException("Số lượng sản phẩm không được nhỏ hơn 0.");
        }

        facilityInventoryService.updateQuantity(facilityInventoryId, totalQuantity);
        request.getSession().setAttribute("successMessage", "Cập nhật số lượng thành công.");
    }

    private void handleRemove(HttpServletRequest request) {
        String facilityInventoryIdParam = request.getParameter("facilityInventoryId");

        if (facilityInventoryIdParam == null || facilityInventoryIdParam.isBlank()) {
            throw new IllegalArgumentException("Thiếu mã đồ gán sân để gỡ.");
        }

        int facilityInventoryId;
        try {
            facilityInventoryId = Integer.parseInt(facilityInventoryIdParam);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Mã đồ gán sân không hợp lệ.");
        }

        facilityInventoryService.removeById(facilityInventoryId);
        request.getSession().setAttribute("successMessage", "Gỡ sản phẩm khỏi sân thành công.");
    }

    private int parsePage(String pageParam) {
        try {
            int page = Integer.parseInt(pageParam);
            return Math.max(page, 1);
        } catch (Exception e) {
            return 1;
        }
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        value = value.trim();
        return value.isEmpty() ? null : value;
    }
}