package com.bcb.controller;

import com.bcb.dto.BulkPriceUpdateRequestDTO;
import com.bcb.dto.FacilityPriceViewDTO;
import com.bcb.exception.BusinessException;
import com.bcb.service.FacilityPriceRuleService;
import com.bcb.service.impl.FacilityPriceRuleServiceImpl;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/owner/prices/*")
public class FacilityPriceRuleController extends HttpServlet {

    private FacilityPriceRuleService facilityPriceRuleService;

    @Override
    public void init() {
        facilityPriceRuleService = new FacilityPriceRuleServiceImpl();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        try {
            String facilityIdStr = request.getParameter("facilityId");
            if (facilityIdStr == null) {
                response.sendRedirect(request.getContextPath() + "/owner/facility/list");
                return;
            }

            int facilityId = Integer.parseInt(facilityIdStr);
            String courtTypeIdStr = request.getParameter("courtTypeId");
            Integer courtTypeId = (courtTypeIdStr != null && !courtTypeIdStr.isEmpty()) ? Integer.parseInt(courtTypeIdStr) : null;
            String dayType = request.getParameter("dayType");

            FacilityPriceViewDTO viewData = facilityPriceRuleService.getPriceView(facilityId, courtTypeId, dayType);
            request.setAttribute("viewData", viewData);
            request.getRequestDispatcher("/jsp/owner/facility/price-config.jsp").forward(request, response);

        } catch (BusinessException | NumberFormatException e) {
            request.setAttribute("error", e.getMessage());
            request.getRequestDispatcher("/jsp/owner/error.jsp").forward(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String path = request.getPathInfo();

        try {
            if ("/update-single".equals(path)) {
                updateSingle(request, response);
            } else if ("/bulk-update".equals(path)) {
                bulkUpdate(request, response);
            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (BusinessException | NumberFormatException e) {
            request.setAttribute("error", e.getMessage());
            // Redirect back with error
            String facilityId = request.getParameter("facilityId");
            response.sendRedirect(request.getContextPath() + "/owner/prices?facilityId=" + facilityId + "&error=" + e.getMessage());
        }
    }

    private void updateSingle(HttpServletRequest request, HttpServletResponse response)
            throws IOException, BusinessException {

        int facilityId = Integer.parseInt(request.getParameter("facilityId"));
        int courtTypeId = Integer.parseInt(request.getParameter("courtTypeId"));
        String dayType = request.getParameter("dayType");
        int slotId = Integer.parseInt(request.getParameter("slotId"));
        BigDecimal price = new BigDecimal(request.getParameter("price"));

        facilityPriceRuleService.updateSinglePrice(facilityId, courtTypeId, dayType, slotId, price);

        response.sendRedirect(request.getContextPath() + "/owner/prices?facilityId=" + facilityId 
                + "&courtTypeId=" + courtTypeId + "&dayType=" + dayType);
    }

    private void bulkUpdate(HttpServletRequest request, HttpServletResponse response)
            throws IOException, BusinessException {

        int facilityId = Integer.parseInt(request.getParameter("facilityId"));
        int courtTypeId = Integer.parseInt(request.getParameter("courtTypeId"));
        String dayType = request.getParameter("dayType");
        BigDecimal price = new BigDecimal(request.getParameter("price"));

        String[] slotIdsStr = request.getParameterValues("slotIds");
        if (slotIdsStr == null || slotIdsStr.length == 0) {
            throw new BusinessException("No slots selected");
        }

        List<Integer> slotIds = new ArrayList<>();
        for (String id : slotIdsStr) {
            slotIds.add(Integer.parseInt(id));
        }

        BulkPriceUpdateRequestDTO bulkDTO = new BulkPriceUpdateRequestDTO();
        bulkDTO.setFacilityId(facilityId);
        bulkDTO.setCourtTypeId(courtTypeId);
        bulkDTO.setDayType(dayType);
        bulkDTO.setPrice(price);
        bulkDTO.setSlotIds(slotIds);

        facilityPriceRuleService.bulkUpdatePrices(bulkDTO);

        response.sendRedirect(request.getContextPath() + "/owner/prices?facilityId=" + facilityId 
                + "&courtTypeId=" + courtTypeId + "&dayType=" + dayType);
    }
}
