package com.bcb.controller.owner;

import com.bcb.model.Inventory;
import com.bcb.model.Court;
import com.bcb.repository.InventoryRepository;
import com.bcb.repository.CourtRepository;
import com.bcb.repository.impl.InventoryRepositoryImpl;
import com.bcb.repository.impl.CourtRepositoryImpl;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

public class OwnerFacilityInventoryController extends HttpServlet {

    private final InventoryRepository inventoryRepo = new InventoryRepositoryImpl();
    private final CourtRepository courtRepo = new CourtRepositoryImpl();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String path = req.getPathInfo();
        int facilityId = Integer.parseInt(path.substring(1));

        int page = parseInt(req.getParameter("page"), 1);
        int pageUn = parseInt(req.getParameter("pageUn"), 1);
        int size = 5;

        String keyword = req.getParameter("keyword");
        String keywordUn = req.getParameter("keywordUn");

        int offset = (page - 1) * size;
        int offsetUn = (pageUn - 1) * size;

        List<Inventory> inventories =
                inventoryRepo.findByFacility(facilityId, size, offset, keyword);

        int total = inventoryRepo.countByFacility(facilityId, keyword);
        int totalPages = (int) Math.ceil((double) total / size);

        List<Inventory> unassigned =
                inventoryRepo.findUnassigned(size, offsetUn, keywordUn);

        int totalUn = inventoryRepo.countUnassigned(keywordUn);
        int totalPagesUn = (int) Math.ceil((double) totalUn / size);

        List<Court> courts = courtRepo.findAllActive();

        req.setAttribute("inventories", inventories);
        req.setAttribute("unassigned", unassigned);
        req.setAttribute("courts", courts);

        req.setAttribute("facilityId", facilityId);
        req.setAttribute("currentPage", page);
        req.setAttribute("totalPages", totalPages);
        req.setAttribute("currentPageUn", pageUn);
        req.setAttribute("totalPagesUn", totalPagesUn);

        req.getRequestDispatcher("/jsp/owner/facility/facility-inventory.jsp")
                .forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        String action = req.getParameter("action");
        int facilityId = Integer.parseInt(req.getParameter("facilityId"));

        if ("assign".equals(action)) {
            int inventoryId = Integer.parseInt(req.getParameter("inventoryId"));
            int courtId = Integer.parseInt(req.getParameter("courtId"));
            inventoryRepo.assignToCourt(inventoryId, courtId);
        }

        if ("remove".equals(action)) {
            int inventoryId = Integer.parseInt(req.getParameter("inventoryId"));
            inventoryRepo.removeFromCourt(inventoryId);
        }

        resp.sendRedirect(req.getContextPath()
                + "/owner/facility/inventory/" + facilityId);
    }

    private int parseInt(String val, int def) {
        try { return Integer.parseInt(val); }
        catch (Exception e) { return def; }
    }
}