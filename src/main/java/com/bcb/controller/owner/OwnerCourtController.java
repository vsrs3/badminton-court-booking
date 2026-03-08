package com.bcb.controller.owner;

import com.bcb.exception.BusinessException;
import com.bcb.exception.ValidationException;
import com.bcb.model.Court;
import com.bcb.model.Facility;
import com.bcb.service.CourtService;
import com.bcb.service.CourtTypeService;
import com.bcb.service.FacilityService;
import com.bcb.service.impl.CourtServiceImpl;
import com.bcb.service.impl.CourtTypeServiceImpl;
import com.bcb.service.impl.FacilityServiceImpl;
import com.bcb.utils.BreadcrumbUtils;
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
@WebServlet("/owner/courts/*")
public class OwnerCourtController extends HttpServlet {

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

        Facility facility = facilityService.findById(facilityId);
        request.setAttribute("facility", facility);
        request.setAttribute("courts", courtService.getCourtsByFacilityDTO(facilityId));
        request.setAttribute("courtTypes", courtTypeService.getAllTypes());
        request.setAttribute("totalRecords", courtService.getCourtsByFacilityDTO(facilityId).size());

        // Flash messages forwarded via query params (e.g. after bulk create redirect)
        String successMsg = request.getParameter("success");
        String errorMsg = request.getParameter("error");
        if (successMsg != null && !successMsg.isEmpty()) {
            request.setAttribute("success", successMsg);
        }
        if (errorMsg != null && !errorMsg.isEmpty()) {
            request.setAttribute("error", errorMsg);
        }

        // Breadcrumb
        BreadcrumbUtils.builder(request)
                .dashboard()
                .facilityList()
                .facility(facility.getName(), facilityId)
                .active("Quản lý sân")
                .build();

        request.getRequestDispatcher("/jsp/owner/court/court-list.jsp")
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
                            "courtTypeId": %d,
                            "description": "%s"
                        }
                        """,
                court.getCourtId(),
                escapeJson(court.getCourtName()),
                court.getCourtTypeId(),
                escapeJson(court.getDescription())
        );

        response.getWriter().write(json);
    }

    /**
     * CREATE + UPDATE dùng chung. Hỗ trợ chế độ tạo nhiều sân (bulk).
     *
     * @author AnhTN
     */
    private void saveCourt(HttpServletRequest request, HttpServletResponse response)
            throws ValidationException, BusinessException, IOException {

        String courtIdStr = request.getParameter("courtId");
        int facilityId = Integer.parseInt(request.getParameter("facilityId"));

        boolean isBulk = "true".equals(request.getParameter("isBulk"));

        if (isBulk) {
            // ---- BULK CREATE ----
            String courtNamePrefix = request.getParameter("courtNamePrefix");
            String courtCountStr = request.getParameter("courtCount");
            String courtTypeIdStr = request.getParameter("courtTypeId");
            String description = request.getParameter("description");

            if (courtTypeIdStr == null || courtTypeIdStr.isEmpty()) {
                throw new ValidationException("Loại sân là bắt buộc");
            }
            if (courtCountStr == null || courtCountStr.isEmpty()) {
                throw new ValidationException("Số lượng sân là bắt buộc");
            }

            int courtTypeId = Integer.parseInt(courtTypeIdStr);
            int courtCount = Integer.parseInt(courtCountStr);

            java.util.List<String> created = courtService.createBulkCourts(
                    facilityId, courtNamePrefix, courtCount, courtTypeId, description);

            String message = "Đã tạo " + created.size() + " sân: " + String.join(", ", created);
            response.sendRedirect(
                    request.getContextPath() + "/owner/courts/list/" + facilityId
                            + "?success=" + java.net.URLEncoder.encode(message, "UTF-8")
            );
        } else {
            // ---- SINGLE CREATE / UPDATE ----
            String courtName = request.getParameter("courtName");
            String courtTypeIdStr = request.getParameter("courtTypeId");
            String description = request.getParameter("description");

            if (courtTypeIdStr == null || courtTypeIdStr.isEmpty()) {
                throw new ValidationException("Loại sân là bắt buộc");
            }

            int courtTypeId = Integer.parseInt(courtTypeIdStr);

            Court court;

            if (courtIdStr == null || courtIdStr.isEmpty()) {
                court = new Court();
                court.setFacilityId(facilityId);
                court.setIsActive(true);
            } else {
                int courtId = Integer.parseInt(courtIdStr);
                court = courtService.getCourtById(courtId);
                if (court == null) {
                    throw new BusinessException("Court not found");
                }
            }

            court.setCourtName(courtName);
            court.setCourtTypeId(courtTypeId);
            court.setDescription(description);

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

