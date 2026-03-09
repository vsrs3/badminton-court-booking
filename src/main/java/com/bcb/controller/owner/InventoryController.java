package com.bcb.controller.owner;

import com.bcb.model.Inventory;
import com.bcb.service.InventoryService;
import com.bcb.service.impl.InventoryServiceImpl;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

public class InventoryController extends HttpServlet {

    private final InventoryService service = new InventoryServiceImpl();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String action = req.getParameter("action");

        /* =========================
           EDIT
        ========================= */
        if ("edit".equals(action)) {

            int id = Integer.parseInt(req.getParameter("id"));

            Inventory inventory = service.getById(id);

            req.setAttribute("inventory", inventory);

            req.getRequestDispatcher("/jsp/owner/inventory/inventory-form.jsp")
                    .forward(req, resp);
            return;
        }

        /* =========================
           ADD
        ========================= */
        else if ("add".equals(action)) {

            req.getRequestDispatcher("/jsp/owner/inventory/inventory-form.jsp")
                    .forward(req, resp);
            return;
        }

        /* =========================
           DELETE
        ========================= */
        else if ("delete".equals(action)) {

            int id = Integer.parseInt(req.getParameter("id"));

            service.delete(id);

            resp.sendRedirect(req.getContextPath() + "/owner/inventory");
            return;
        }

        /* =========================
           LIST + PAGINATION
        ========================= */
        else {

            String keyword = req.getParameter("keyword");

            int page = 1;
            int size = 10;

            try {
                page = Integer.parseInt(req.getParameter("page"));
            } catch (Exception ignored) {
            }

            int offset = (page - 1) * size;

            List<Inventory> list = service.getWithPagination(size, offset, keyword);

            int total = service.countInventory(keyword);

            int totalPages = (int) Math.ceil((double) total / size);

            req.setAttribute("inventories", list);
            req.setAttribute("keyword", keyword);
            req.setAttribute("currentPage", page);
            req.setAttribute("totalPages", totalPages);

            req.getRequestDispatcher("/jsp/owner/inventory/inventory-list.jsp")
                    .forward(req, resp);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String idParam = req.getParameter("id");

        Inventory inventory = new Inventory();

        inventory.setName(req.getParameter("name"));
        inventory.setBrand(req.getParameter("brand"));
        inventory.setDescription(req.getParameter("description"));
        inventory.setRentalPrice(new BigDecimal(req.getParameter("price")));
        inventory.setActive(req.getParameter("active") != null);

        if (idParam == null || idParam.isEmpty()) {
            service.create(inventory);
        } else {
            inventory.setInventoryId(Integer.parseInt(idParam));
            service.update(inventory);
        }

        resp.sendRedirect(req.getContextPath() + "/owner/inventory");
    }
}