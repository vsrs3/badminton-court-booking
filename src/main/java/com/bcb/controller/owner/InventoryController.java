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

        if ("edit".equals(action)) {
            int id = Integer.parseInt(req.getParameter("id"));
            Inventory inventory = service.getById(id);
            req.setAttribute("inventory", inventory);
            req.getRequestDispatcher("/jsp/owner/inventory/inventory-form.jsp").forward(req, resp);
            return;
        }

        if ("add".equals(action)) {
            req.getRequestDispatcher("/jsp/owner/inventory/inventory-form.jsp").forward(req, resp);
            return;
        }

        if ("delete".equals(action)) {
            int id = Integer.parseInt(req.getParameter("id"));
            service.delete(id);
            resp.sendRedirect(req.getContextPath() + "/owner/inventory");
            return;
        }

        String keyword = normalizeKeyword(req.getParameter("keyword"));
        String priceSort = normalizePriceSort(req.getParameter("priceSort"));
        String status = normalizeStatus(req.getParameter("status"));
        Boolean activeStatus = resolveActiveStatus(status);

        int page = parsePositiveInt(req.getParameter("page"), 1);
        int size = 10;
        int total = service.countInventory(keyword, activeStatus);
        int totalPages = total == 0 ? 0 : (int) Math.ceil((double) total / size);

        if (totalPages > 0 && page > totalPages) {
            page = totalPages;
        }

        int offset = (page - 1) * size;
        List<Inventory> list = service.getWithPagination(size, offset, keyword, activeStatus, priceSort);
        List<Inventory> suggestionInventories = service.getWithPagination(50, 0, keyword, activeStatus, priceSort);

        req.setAttribute("inventories", list);
        req.setAttribute("suggestionInventories", suggestionInventories);
        req.setAttribute("suggestionLimit", 50);
        req.setAttribute("keyword", keyword == null ? "" : keyword);
        req.setAttribute("priceSort", priceSort);
        req.setAttribute("status", status);
        req.setAttribute("currentPage", page);
        req.setAttribute("totalPages", totalPages);

        req.getRequestDispatcher("/jsp/owner/inventory/inventory-list.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");

        String idParam = req.getParameter("id");
        Inventory inventory = new Inventory();

        inventory.setName(trimToEmpty(req.getParameter("name")));
        inventory.setBrand(trimToEmpty(req.getParameter("brand")));
        inventory.setDescription(trimToEmpty(req.getParameter("description")));
        inventory.setActive(req.getParameter("active") != null);

        try {
            String priceRaw = trimToEmpty(req.getParameter("price"));
            if (priceRaw.isEmpty()) {
                forwardForm(req, resp, inventory, "Vui lòng nhập giá thuê");
                return;
            }

            inventory.setRentalPrice(new BigDecimal(priceRaw));

            if (idParam == null || idParam.isEmpty()) {
                service.create(inventory);
            } else {
                inventory.setInventoryId(Integer.parseInt(idParam));
                service.update(inventory);
            }

            resp.sendRedirect(req.getContextPath() + "/owner/inventory");
        } catch (NumberFormatException e) {
            forwardForm(req, resp, inventory, "Giá thuê phải là số hợp lệ");
        } catch (IllegalArgumentException e) {
            forwardForm(req, resp, inventory, e.getMessage());
        }
    }

    private void forwardForm(HttpServletRequest req, HttpServletResponse resp, Inventory inventory, String error)
            throws ServletException, IOException {
        req.setAttribute("inventory", inventory);
        req.setAttribute("error", error);
        req.getRequestDispatcher("/jsp/owner/inventory/inventory-form.jsp").forward(req, resp);
    }

    private String trimToEmpty(String value) {
        return value == null ? "" : value.trim();
    }

    private String normalizeKeyword(String value) {
        String trimmed = trimToEmpty(value);
        return trimmed.isEmpty() ? null : trimmed;
    }

    private int parsePositiveInt(String rawValue, int fallback) {
        try {
            int value = Integer.parseInt(rawValue);
            return value > 0 ? value : fallback;
        } catch (Exception ignored) {
            return fallback;
        }
    }

    private String normalizePriceSort(String value) {
        if ("price_desc".equals(value) || "price_asc".equals(value)) {
            return value;
        }
        return "default";
    }

    private String normalizeStatus(String value) {
        if ("active".equals(value) || "inactive".equals(value)) {
            return value;
        }
        return "all";
    }

    private Boolean resolveActiveStatus(String status) {
        if ("active".equals(status)) {
            return Boolean.TRUE;
        }
        if ("inactive".equals(status)) {
            return Boolean.FALSE;
        }
        return null;
    }
}
