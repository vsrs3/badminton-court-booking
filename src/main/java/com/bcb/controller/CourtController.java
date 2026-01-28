package com.bcb.controller;

import com.bcb.exception.BusinessException;
import com.bcb.exception.ValidationException;
import com.bcb.model.Court;
import com.bcb.model.Facility;
import com.bcb.service.CourtService;
import com.bcb.service.FacilityService;
import com.bcb.service.impl.CourtServiceImpl;
import com.bcb.service.impl.FacilityServiceImpl;
import com.bcb.validation.CourtValidator;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/*
*   @Author: AnhTN
*
*/
@WebServlet("/admin/courts/*")
public class CourtController extends HttpServlet {

    private CourtService courtService;
    private FacilityService facilityService;

    @Override
    public void init() {
        courtService = new CourtServiceImpl();
        facilityService = new FacilityServiceImpl();
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
            }
            else if (pathInfo.startsWith("/view/")) {
                viewCourt(request, response, pathInfo);
            }
            else if (pathInfo.startsWith("/detail/")) {
                getCourtDetailJson(request, response, pathInfo);
            }
            else if (pathInfo.startsWith("/delete/")) {
                deactivateCourt(request, response, pathInfo);
            }
            else {
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
        request.setAttribute("courts", courtService.getCourtsByFacility(facilityId));

        request.getRequestDispatcher("/jsp/admin/court/court-list.jsp")
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

        request.getRequestDispatcher("/jsp/admin/court/court-detail.jsp")
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
                    "description": "%s"
                }
                """,
                court.getCourtId(),
                escapeJson(court.getCourtName()),
                escapeJson(court.getDescription())
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

        if (courtIdStr == null || courtIdStr.isEmpty()) {
            // ===== CREATE =====
            Court court = new Court();
            court.setFacilityId(facilityId);
            court.setCourtName(request.getParameter("courtName"));
            court.setDescription(request.getParameter("description"));
            court.setActive(true);

            CourtValidator.validate(court);
            courtService.createCourt(court);

        } else {
            // ===== UPDATE =====
            int courtId = Integer.parseInt(courtIdStr);
            Court court = courtService.getCourtById(courtId);

            court.setCourtName(request.getParameter("courtName"));
            court.setDescription(request.getParameter("description"));

            CourtValidator.validate(court);
            courtService.updateCourt(court);
        }

        response.sendRedirect(
                request.getContextPath() + "/admin/courts/list/" + facilityId
        );
    }

    private void deactivateCourt(HttpServletRequest request, HttpServletResponse response,
                                 String pathInfo)
            throws IOException, BusinessException {

        int courtId = Integer.parseInt(pathInfo.substring("/delete/".length()));
        Court court = courtService.getCourtById(courtId);

        courtService.deactivateCourt(courtId);

        response.sendRedirect(
                request.getContextPath() + "/admin/courts/list/" + court.getFacilityId()
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

