package com.bcb.controller;

import com.bcb.exception.BusinessException;
import com.bcb.exception.ValidationException;
import com.bcb.model.Court;
import com.bcb.model.CourtType;
import com.bcb.model.Facility;
import com.bcb.service.CourtService;
import com.bcb.service.CourtTypeService;
import com.bcb.service.FacilityService;
import com.bcb.service.impl.CourtServiceImpl;
import com.bcb.service.impl.CourtTypeServiceImpl;
import com.bcb.service.impl.FacilityServiceImpl;
import com.bcb.validation.CourtValidator;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

/*
 *   @Author: AnhTN
 *
 */
@WebServlet("/owner/courts/*")
public class CourtController extends HttpServlet {

    private CourtService courtService;
    private FacilityService facilityService;
    private CourtTypeService courtTypeService;

    @Override
    public void init() {
        courtService = new CourtServiceImpl();
        facilityService = new FacilityServiceImpl();
        courtTypeService = new CourtTypeServiceImpl();
    }

    // ==========================
    // GET
    // ==========================
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String pathInfo = request.getPathInfo();

        try {
            if (pathInfo == null || "/".equals(pathInfo)) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            if (pathInfo.startsWith("/list/")) {
                listCourts(request, response, pathInfo);
            } else if (pathInfo.startsWith("/view/")) {
                viewCourt(request, response, pathInfo);
            } else if (pathInfo.startsWith("/detail/")) {
                getCourtDetailJson(request, response, pathInfo);
            } else if (pathInfo.startsWith("/delete/")) {
                deactivateCourt(request, response, pathInfo);
            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }

        } catch (BusinessException e) {
            throw new ServletException(e);
        }
    }

    // ==========================
    // POST
    // ==========================
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String pathInfo = request.getPathInfo();

        try {
            if ("/save".equals(pathInfo)) {
                saveCourt(request, response);
            } else {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            }

        } catch (BusinessException e) {
            throw new ServletException(e);
        }
    }

    // ==========================
    // METHODS
    // ==========================

    private void listCourts(HttpServletRequest request, HttpServletResponse response,
                            String pathInfo)
            throws ServletException, IOException, BusinessException {

        int facilityId = Integer.parseInt(pathInfo.substring("/list/".length()));

        request.setAttribute("facility", facilityService.findById(facilityId));
        request.setAttribute("courts", courtService.getCourtsByFacilityDTO(facilityId));
        request.setAttribute("courtTypes", courtTypeService.getAllTypes());

        request.getRequestDispatcher("/jsp/owner/court/court-list.jsp")
                .forward(request, response);
    }

    private void viewCourt(HttpServletRequest request, HttpServletResponse response,
                           String pathInfo)
            throws ServletException, IOException, BusinessException {

        int courtId = Integer.parseInt(pathInfo.substring("/view/".length()));

        Court court = courtService.getCourtById(courtId);
        Facility facility = facilityService.findById(court.getFacilityId());


        request.setAttribute("court", court);
        request.setAttribute("facility", facility);


        request.getRequestDispatcher("/jsp/owner/court/court-detail.jsp")
                .forward(request, response);
    }

    /**
     * API cho modal EDIT (fetch JSON)
     */
    private void getCourtDetailJson(HttpServletRequest request, HttpServletResponse response,
                                    String pathInfo)
            throws IOException, BusinessException {

        int courtId = Integer.parseInt(pathInfo.substring("/detail/".length()));
        Court court = courtService.getCourtById(courtId);

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String json = String.format("""
                        {
                            "courtId": %d,
                            "courtName": "%s",
                            "courtTypeId": %d
                        }
                        """,
                court.getCourtId(),
                escapeJson(court.getCourtName()),
                court.getCourtTypeId()
        );

        response.getWriter().write(json);
    }

    /**
     * CREATE + UPDATE dùng chung
     */
    private void saveCourt(HttpServletRequest request, HttpServletResponse response)
            throws ValidationException, BusinessException, IOException {

        String courtIdStr = request.getParameter("courtId");
        int facilityId = Integer.parseInt(request.getParameter("facilityId"));

        String courtName = request.getParameter("courtName");
        String courtTypeIdStr = request.getParameter("courtTypeId");

        if (courtTypeIdStr == null || courtTypeIdStr.isEmpty()) {
            throw new ValidationException("Court type is required");
        }

        int courtTypeId = Integer.parseInt(courtTypeIdStr);

        Court court;

        if (courtIdStr == null || courtIdStr.isEmpty()) {
            // create
            court = new Court();
            court.setFacilityId(facilityId);
            court.setActive(true);

        } else {
            // update
            int courtId = Integer.parseInt(courtIdStr);
            court = courtService.getCourtById(courtId);

            if (court == null) {
                throw new BusinessException("Court not found");
            }
        }

        court.setCourtName(courtName);
        court.setCourtTypeId(courtTypeId);

        CourtValidator.validate(court);

        if (courtIdStr == null || courtIdStr.isEmpty()) {
            courtService.createCourt(court);
        } else {
            courtService.updateCourt(court);
        }

        response.sendRedirect(
                request.getContextPath() + "/owner/courts/list/" + facilityId
        );
    }

    private void deactivateCourt(HttpServletRequest request, HttpServletResponse response,
                                 String pathInfo)
            throws IOException, BusinessException {

        int courtId = Integer.parseInt(pathInfo.substring("/delete/".length()));
        Court court = courtService.getCourtById(courtId);

        courtService.deactivateCourt(courtId);

        response.sendRedirect(
                request.getContextPath() + "/owner/courts/list/" + court.getFacilityId()
        );
    }

    // ==========================
    // UTIL
    // ==========================
    private String escapeJson(String value) {
        if (value == null) return "";
        return value.replace("\"", "\\\"");
    }
}

