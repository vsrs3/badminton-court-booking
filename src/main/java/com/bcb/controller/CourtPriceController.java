package com.bcb.controller;

import com.bcb.exception.BusinessException;
import com.bcb.model.CourtPrice;
import com.bcb.service.CourtPriceService;
import com.bcb.service.impl.CourtPriceServiceImpl;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/*
 *   @Author: AnhTN
 *
 */

@WebServlet("/admin/court-prices/*")
public class CourtPriceController extends HttpServlet {

    private CourtPriceService courtPriceService;
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    @Override
    public void init() {
        courtPriceService = new CourtPriceServiceImpl();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String path = request.getPathInfo();

        try {
            if (path.startsWith("/list/")) {
                list(request, response);
            } else if (path.startsWith("/edit/")) {
                edit(request, response);
            } else if (path.startsWith("/delete/")) {
                delete(request, response);
            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (BusinessException e) {
            request.setAttribute("error", e.getMessage());
            request.getRequestDispatcher("/jsp/admin/error.jsp").forward(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String action = request.getParameter("action");

        try {
            if ("create".equals(action)) {
                create(request, response);
            } else if ("update".equals(action)) {
                update(request, response);
            } else {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            }
        } catch (BusinessException e) {
            request.setAttribute("error", e.getMessage());
            request.getRequestDispatcher("/jsp/admin/court-price-form.jsp").forward(request, response);
        }
    }

    /* ================== handlers ================== */

    private void list(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        int courtId = Integer.parseInt(request.getPathInfo().substring(6));
        request.setAttribute("courtId", courtId);
        request.setAttribute("prices", courtPriceService.getPricesByCourt(courtId));
        request.getRequestDispatcher("/jsp/admin/court-price-list.jsp").forward(request, response);
    }

    private void edit(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, BusinessException {

        int priceId = Integer.parseInt(request.getPathInfo().substring(6));
        request.setAttribute("price", courtPriceService.getPriceById(priceId));
        request.setAttribute("isEdit", true);
        request.getRequestDispatcher("/jsp/admin/court-price-form.jsp").forward(request, response);
    }

    private void delete(HttpServletRequest request, HttpServletResponse response)
            throws IOException, BusinessException {

        int priceId = Integer.parseInt(request.getPathInfo().substring(8));
        courtPriceService.deletePrice(priceId);
        response.sendRedirect(request.getContextPath() + "/admin/courts");
    }

    private void create(HttpServletRequest request, HttpServletResponse response)
            throws IOException, BusinessException {

        CourtPrice price = buildFromRequest(request);
        int courtId = price.getCourtId();
        courtPriceService.createPrice(price);
        response.sendRedirect(request.getContextPath() + "/admin/court-prices/list/" + courtId);
    }

    private void update(HttpServletRequest request, HttpServletResponse response)
            throws IOException, BusinessException {

        CourtPrice price = buildFromRequest(request);
        price.setPriceId(Integer.parseInt(request.getParameter("priceId")));
        courtPriceService.updatePrice(price);
        response.sendRedirect(request.getContextPath() + "/admin/court-prices/list/" + price.getCourtId());
    }

    /* ================== helper ================== */

    private CourtPrice buildFromRequest(HttpServletRequest request) {
        CourtPrice price = new CourtPrice();
        price.setCourtId(Integer.parseInt(request.getParameter("courtId")));
        price.setStartTime(LocalTime.parse(request.getParameter("startTime"), TIME_FORMATTER));
        price.setEndTime(LocalTime.parse(request.getParameter("endTime"), TIME_FORMATTER));
        price.setPricePerHour(new BigDecimal(request.getParameter("pricePerHour")));
        return price;
    }
}
