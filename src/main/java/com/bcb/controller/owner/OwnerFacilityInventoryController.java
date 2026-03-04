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

        int page = 1;
        int size = 10;

        try {
            page = Integer.parseInt(request.getParameter("page"));
        } catch (Exception ignored) {}

        int offset = (page - 1) * size;

        List<Inventory> inventories =
                inventoryService.getByFacility(facilityId, size, offset, keyword);

        int total = inventoryService.countByFacility(facilityId, keyword);

        List<Inventory> unassigned =
                inventoryService.getUnassigned(size, 0, keywordUn);

        int totalPages = (int) Math.ceil((double) total / size);

        request.setAttribute("facilityId", facilityId);
        request.setAttribute("inventories", inventories);
        request.setAttribute("unassigned", unassigned);
        request.setAttribute("currentPage", page);
        request.setAttribute("totalPages", totalPages);

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

        if (facilityParam == null || facilityParam.isEmpty()) {
            response.sendError(400, "Facility ID missing");
            return;
        }

        int facilityId = Integer.parseInt(facilityParam);

        try {

            if ("assign".equals(action)) {

                String inventoryParam = request.getParameter("inventoryId");

                if (inventoryParam != null) {

                    int inventoryId = Integer.parseInt(inventoryParam);

                    inventoryService.assignToFacility(inventoryId, facilityId);
                }
            }

            if ("remove".equals(action)) {

                String inventoryParam = request.getParameter("inventoryId");

                if (inventoryParam != null) {

                    int inventoryId = Integer.parseInt(inventoryParam);

                    inventoryService.removeFromFacility(inventoryId);
                }
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