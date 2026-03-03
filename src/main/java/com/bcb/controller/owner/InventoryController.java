package com.bcb.controller.owner;

import com.bcb.model.Inventory;
import com.bcb.service.InventoryService;
import com.bcb.service.impl.InventoryServiceImpl;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

public class InventoryController extends HttpServlet {

    private final InventoryService service = new InventoryServiceImpl();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String action = req.getParameter("action");

        if ("edit".equals(action)) {

            int id = Integer.parseInt(req.getParameter("id"));
            Inventory inventory = service.getById(id);
            req.setAttribute("inventory", inventory);
            req.getRequestDispatcher("/jsp/owner/inventory/inventory-form.jsp")
                    .forward(req, resp);

        } else if ("add".equals(action)) {

            req.getRequestDispatcher("/jsp/owner/inventory/inventory-form.jsp")
                    .forward(req, resp);

        } else if ("delete".equals(action)) {

            int id = Integer.parseInt(req.getParameter("id"));
            service.delete(id);
            resp.sendRedirect(req.getContextPath() + "/owner/inventory");

        } else {

            String keyword = req.getParameter("keyword");

            List<Inventory> list = (keyword == null || keyword.trim().isEmpty())
                    ? service.getAll()
                    : service.search(keyword);

            req.setAttribute("inventories", list);
            req.setAttribute("keyword", keyword);

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