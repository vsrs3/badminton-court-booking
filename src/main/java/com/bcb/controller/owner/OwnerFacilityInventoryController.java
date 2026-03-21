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
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class OwnerFacilityInventoryController extends HttpServlet {

    private static final int ASSIGNED_PAGE_SIZE = 5;
    private static final int INVENTORY_PAGE_SIZE = 5;
    private static final int SUGGESTION_LIMIT = 50;

    private InventoryService inventoryService;
    private FacilityInventoryService facilityInventoryService;

    @Override
    public void init() {
        inventoryService = new InventoryServiceImpl();
        facilityInventoryService = new FacilityInventoryServiceImpl();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setCharacterEncoding("UTF-8");

        Integer facilityId = parseFacilityIdFromPath(request.getPathInfo());
        if (facilityId == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Mã sân không hợp lệ");
            return;
        }

        String assignedKeyword = trimToNull(request.getParameter("assignedKeyword"));
        String inventoryKeyword = trimToNull(request.getParameter("inventoryKeyword"));

        int assignedTotal = facilityInventoryService.countByFacilityId(facilityId, assignedKeyword);
        int assignedTotalPages = Math.max(1, (int) Math.ceil((double) assignedTotal / ASSIGNED_PAGE_SIZE));
        int assignedPage = Math.min(parsePage(request.getParameter("assignedPage")), assignedTotalPages);
        int assignedOffset = (assignedPage - 1) * ASSIGNED_PAGE_SIZE;

        List<FacilityInventory> assignedItems = facilityInventoryService.getByFacilityId(
                facilityId,
                ASSIGNED_PAGE_SIZE,
                assignedOffset,
                assignedKeyword
        );

        int inventoryTotal = inventoryService.countActiveNotAssignedToFacility(facilityId, inventoryKeyword);
        int inventoryTotalPages = Math.max(1, (int) Math.ceil((double) inventoryTotal / INVENTORY_PAGE_SIZE));
        int inventoryPage = Math.min(parsePage(request.getParameter("inventoryPage")), inventoryTotalPages);
        int inventoryOffset = (inventoryPage - 1) * INVENTORY_PAGE_SIZE;

        List<Inventory> availableInventories = inventoryService.getActiveNotAssignedToFacilityWithPagination(
                facilityId,
                INVENTORY_PAGE_SIZE,
                inventoryOffset,
                inventoryKeyword
        );

        List<FacilityInventory> assignedSuggestionItems = facilityInventoryService.getByFacilityId(
                facilityId,
                SUGGESTION_LIMIT,
                0,
                assignedKeyword
        );

        List<Inventory> inventorySuggestionItems = inventoryService.getActiveNotAssignedToFacilityWithPagination(
                facilityId,
                SUGGESTION_LIMIT,
                0,
                inventoryKeyword
        );

        request.setAttribute("facilityId", facilityId);
        request.setAttribute("assignedItems", assignedItems);
        request.setAttribute("assignedKeyword", assignedKeyword);
        request.setAttribute("assignedCurrentPage", assignedPage);
        request.setAttribute("assignedTotalPages", assignedTotalPages);
        request.setAttribute("assignedPageSize", ASSIGNED_PAGE_SIZE);
        request.setAttribute("assignedSuggestionItems", assignedSuggestionItems);

        request.setAttribute("inventories", availableInventories);
        request.setAttribute("inventoryKeyword", inventoryKeyword);
        request.setAttribute("inventoryCurrentPage", inventoryPage);
        request.setAttribute("inventoryTotalPages", inventoryTotalPages);
        request.setAttribute("inventoryPageSize", INVENTORY_PAGE_SIZE);
        request.setAttribute("inventorySuggestionItems", inventorySuggestionItems);
        request.setAttribute("suggestionLimit", SUGGESTION_LIMIT);

        BreadcrumbUtils.builder(request)
                .add("Bảng điều khiển", request.getContextPath() + "/owner/dashboard")
                .add("Danh sách địa điểm", request.getContextPath() + "/owner/facility/list")
                .active("Quản lý kho đồ của sân")
                .build();

        request.getRequestDispatcher("/jsp/owner/facility/facility-inventory.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        Integer facilityId = parsePositiveInteger(request.getParameter("facilityId"));
        if (facilityId == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Mã sân không hợp lệ");
            return;
        }

        String action = trimToNull(request.getParameter("action"));
        if (action == null) {
            response.sendRedirect(buildRedirectUrl(request, facilityId));
            return;
        }

        try {
            switch (action) {
                case "assign":
                    handleAssign(request, facilityId);
                    break;
                case "assignAll":
                    handleAssignAll(request, facilityId);
                    break;
                case "updateQuantity":
                    handleUpdateQuantity(request);
                    break;
                case "bulkUpdateQuantity":
                    handleBulkUpdateQuantity(request, facilityId);
                    break;
                case "remove":
                    handleRemove(request);
                    break;
                case "removeAll":
                    handleRemoveAll(request, facilityId);
                    break;
                default:
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Thao tác không hợp lệ");
                    return;
            }
        } catch (IllegalArgumentException e) {
            request.getSession().setAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            request.getSession().setAttribute("errorMessage", "Đã xảy ra lỗi khi xử lý kho đồ của sân.");
        }

        response.sendRedirect(buildRedirectUrl(request, facilityId));
    }

    private void handleAssign(HttpServletRequest request, int facilityId) {
        int inventoryId = parseRequiredPositiveInt(
                request.getParameter("inventoryId"),
                "Thiếu mã sản phẩm cần gán.",
                "Mã sản phẩm không hợp lệ"
        );

        facilityInventoryService.assignToFacility(facilityId, inventoryId, 0);
        request.getSession().setAttribute("successMessage", "Gán sản phẩm vào sân thành công.");
    }

    private void handleAssignAll(HttpServletRequest request, int facilityId) {
        String inventoryKeyword = trimToNull(request.getParameter("inventoryKeyword"));
        int assignedCount = facilityInventoryService.assignAllToFacility(facilityId, 0, inventoryKeyword);
        request.getSession().setAttribute("successMessage", "Đã gán " + assignedCount + " đồ vào sân thành công.");
    }

    private void handleUpdateQuantity(HttpServletRequest request) {
        int facilityInventoryId = parseRequiredPositiveInt(
                request.getParameter("facilityInventoryId"),
                "Thiếu mã đồ gán sân.",
                "Mã đồ gán sân không hợp lệ"
        );

        int totalQuantity = parseRequiredNonNegativeInt(
                request.getParameter("totalQuantity"),
                "Vui lòng nhập số lượng sản phẩm.",
                "Số lượng sản phẩm phải là số nguyên không âm."
        );

        facilityInventoryService.updateQuantity(facilityInventoryId, totalQuantity);
        request.getSession().setAttribute("successMessage", "Cập nhật số lượng thành công.");
    }

    private void handleBulkUpdateQuantity(HttpServletRequest request, int facilityId) {
        int bulkQuantity = parseRequiredNonNegativeInt(
                request.getParameter("bulkQuantity"),
                "Vui lòng nhập số lượng muốn áp dụng.",
                "Số lượng muốn áp dụng phải là số nguyên không âm."
        );

        facilityInventoryService.updateAllQuantitiesByFacility(facilityId, bulkQuantity);
        request.getSession().setAttribute("successMessage", "Cập nhật số lượng hàng loạt thành công.");
    }

    private void handleRemove(HttpServletRequest request) {
        int facilityInventoryId = parseRequiredPositiveInt(
                request.getParameter("facilityInventoryId"),
                "Thiếu mã đồ gán sân cần gỡ.",
                "Mã đồ gán sân không hợp lệ"
        );

        facilityInventoryService.removeById(facilityInventoryId);
        request.getSession().setAttribute("successMessage", "Gỡ sản phẩm khỏi sân thành công.");
    }

    private void handleRemoveAll(HttpServletRequest request, int facilityId) {
        String assignedKeyword = trimToNull(request.getParameter("assignedKeyword"));
        int removedCount = facilityInventoryService.removeAllByFacility(facilityId, assignedKeyword);
        request.getSession().setAttribute("successMessage", "Đã gỡ " + removedCount + " sản phẩm khỏi sân thành công.");
    }

    private Integer parseFacilityIdFromPath(String pathInfo) {
        if (pathInfo == null || pathInfo.isBlank() || "/".equals(pathInfo)) {
            return null;
        }

        String normalizedPath = pathInfo.startsWith("/") ? pathInfo.substring(1) : pathInfo;
        if (normalizedPath.contains("/")) {
            return null;
        }

        return parsePositiveInteger(normalizedPath);
    }

    private Integer parsePositiveInteger(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        try {
            int parsedValue = Integer.parseInt(value.trim());
            return parsedValue > 0 ? parsedValue : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private int parseRequiredPositiveInt(String value, String missingMessage, String invalidMessage) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(missingMessage);
        }

        try {
            int parsedValue = Integer.parseInt(value.trim());
            if (parsedValue <= 0) {
                throw new IllegalArgumentException(invalidMessage);
            }
            return parsedValue;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(invalidMessage);
        }
    }

    private int parseRequiredNonNegativeInt(String value, String missingMessage, String invalidMessage) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(missingMessage);
        }

        try {
            int parsedValue = Integer.parseInt(value.trim());
            if (parsedValue < 0) {
                throw new IllegalArgumentException(invalidMessage);
            }
            return parsedValue;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(invalidMessage);
        }
    }

    private int parsePage(String pageParam) {
        if (pageParam == null || pageParam.isBlank()) {
            return 1;
        }

        try {
            int page = Integer.parseInt(pageParam.trim());
            return Math.max(page, 1);
        } catch (NumberFormatException e) {
            return 1;
        }
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }

        String trimmedValue = value.trim();
        return trimmedValue.isEmpty() ? null : trimmedValue;
    }

    private String buildRedirectUrl(HttpServletRequest request, int facilityId) {
        List<String> queryParams = new ArrayList<>();

        appendQueryParam(queryParams, "assignedKeyword", trimToNull(request.getParameter("assignedKeyword")));
        appendQueryParam(queryParams, "inventoryKeyword", trimToNull(request.getParameter("inventoryKeyword")));
        appendPageParam(queryParams, "assignedPage", request.getParameter("assignedPage"));
        appendPageParam(queryParams, "inventoryPage", request.getParameter("inventoryPage"));

        String baseUrl = request.getContextPath() + "/owner/facility/inventory/" + facilityId;
        return queryParams.isEmpty() ? baseUrl : baseUrl + "?" + String.join("&", queryParams);
    }

    private void appendPageParam(List<String> queryParams, String key, String rawValue) {
        Integer page = parsePositiveInteger(rawValue);
        if (page != null) {
            appendQueryParam(queryParams, key, String.valueOf(page));
        }
    }

    private void appendQueryParam(List<String> queryParams, String key, String value) {
        if (value == null || value.isBlank()) {
            return;
        }

        queryParams.add(
                URLEncoder.encode(key, StandardCharsets.UTF_8)
                        + "="
                        + URLEncoder.encode(value, StandardCharsets.UTF_8)
        );
    }
}
