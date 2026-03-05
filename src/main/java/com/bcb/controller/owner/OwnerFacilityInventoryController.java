package com.bcb.controller.owner;

import com.bcb.model.Inventory;
import com.bcb.service.InventoryService;
import com.bcb.service.impl.InventoryServiceImpl;
import com.bcb.utils.BreadcrumbUtils;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.util.List;

public class OwnerFacilityInventoryController extends HttpServlet {

    private InventoryService inventoryService;

    @Override
    public void init() {
        inventoryService = new InventoryServiceImpl();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String pathInfo = request.getPathInfo();

        if (pathInfo == null || pathInfo.equals("/")) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        int facilityId = Integer.parseInt(pathInfo.substring(1));

        String keyword = request.getParameter("keyword");
        String keywordUn = request.getParameter("keywordUn");

        int size = 10;

        /* =========================
           PAGINATION INVENTORY
        ========================= */

        int page = 1;

        try {
            page = Integer.parseInt(request.getParameter("page"));
        } catch (Exception ignored) {}

        int offset = (page - 1) * size;

        List<Inventory> inventories =
                inventoryService.getByFacility(facilityId, size, offset, keyword);

        int total = inventoryService.countByFacility(facilityId, keyword);

        int totalPages = (int) Math.ceil((double) total / size);

        /* =========================
           PAGINATION UNASSIGNED
        ========================= */

        int pageUn = 1;

        try {
            pageUn = Integer.parseInt(request.getParameter("pageUn"));
        } catch (Exception ignored) {}

        int offsetUn = (pageUn - 1) * size;

        List<Inventory> unassigned =
                inventoryService.getUnassigned(size, offsetUn, keywordUn);

        int totalUn = inventoryService.countUnassigned(keywordUn);

        int totalPagesUn = (int) Math.ceil((double) totalUn / size);

        /* =========================
           SET ATTRIBUTE
        ========================= */

        request.setAttribute("facilityId", facilityId);

        request.setAttribute("inventories", inventories);
        request.setAttribute("currentPage", page);
        request.setAttribute("totalPages", totalPages);

        request.setAttribute("unassigned", unassigned);
        request.setAttribute("currentPageUn", pageUn);
        request.setAttribute("totalPagesUn", totalPagesUn);

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

        String action = request.getParameter("action");
        String facilityParam = request.getParameter("facilityId");

        if (facilityParam == null) {
            response.sendError(400, "Facility ID missing");
            return;
        }

        int facilityId = Integer.parseInt(facilityParam);

        try {

            if ("assign".equals(action)) {

                int inventoryId = Integer.parseInt(request.getParameter("inventoryId"));

                inventoryService.assignToFacility(inventoryId, facilityId);

            }

            if ("remove".equals(action)) {

                int inventoryId = Integer.parseInt(request.getParameter("inventoryId"));

                inventoryService.removeFromFacility(inventoryId);

            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        response.sendRedirect(
                request.getContextPath()
                        + "/owner/facility/inventory/" + facilityId
        );
    }
}