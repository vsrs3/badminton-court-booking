package com.bcb.controller.api;

import com.bcb.model.FacilityDTO;
import com.bcb.service.FacilityService;
import com.bcb.service.impl.FacilityServiceImpl;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet(name = "FacilityApiController", urlPatterns = {"/api/facilities/*"})
public class FacilityApiController extends HttpServlet {

    private FacilityService facilityService;
    private Gson gson;

    @Override
    public void init() throws ServletException {
        super.init();
        this.facilityService = new FacilityServiceImpl();
        this.gson = new GsonBuilder()
                .setPrettyPrinting()
                .create();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Set response type to JSON
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String pathInfo = request.getPathInfo();

        try {
            if (pathInfo == null || pathInfo.equals("/")) {
                // GET /api/facilities - Get all facilities with pagination
                handleGetFacilities(request, response);
            } else {
                // GET /api/facilities/{id} - Get facility by ID
                String[] pathParts = pathInfo.split("/");
                if (pathParts.length == 2) {
                    handleGetFacilityById(request, response, pathParts[1]);
                } else {
                    sendErrorResponse(response, 404, "Endpoint not found");
                }
            }
        } catch (Exception e) {
            sendErrorResponse(response, 500, "Internal server error: " + e.getMessage());
        }
    }

    /**
     * Handle GET /api/facilities
     * Query params:
     * - page: page number (default: 0)
     * - pageSize: items per page (default: 12)
     * - userLat: user latitude (optional)
     * - userLng: user longitude (optional)
     */
    private void handleGetFacilities(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        // Get pagination parameters
        int page = getIntParameter(request, "page", 0);
        int pageSize = getIntParameter(request, "pageSize", 12);

        // Get user location (optional)
        Double userLat = getDoubleParameter(request, "userLat");
        Double userLng = getDoubleParameter(request, "userLng");

        // Get user account ID from session (if logged in)
        HttpSession session = request.getSession(false);
        Integer accountId = null;
        if (session != null && session.getAttribute("accountId") != null) {
            accountId = (Integer) session.getAttribute("accountId");
        }

        // Get facilities
        List<FacilityDTO> facilities = facilityService.getFacilities(page, pageSize, userLat, userLng, accountId);
        int totalCount = facilityService.getTotalCount();

        // Build response
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("success", true);
        responseData.put("data", facilities);
        responseData.put("pagination", Map.of(
                "page", page,
                "pageSize", pageSize,
                "totalItems", totalCount,
                "totalPages", (int) Math.ceil((double) totalCount / pageSize),
                "hasMore", (page + 1) * pageSize < totalCount
        ));

        // Send response
        response.getWriter().write(gson.toJson(responseData));
    }

    /**
     * Handle GET /api/facilities/{id}
     */
    private void handleGetFacilityById(HttpServletRequest request, HttpServletResponse response, String id)
            throws IOException {

        try {
            Integer facilityId = Integer.parseInt(id);

            // Get user account ID from session (if logged in)
            HttpSession session = request.getSession(false);
            Integer accountId = null;
            if (session != null && session.getAttribute("accountId") != null) {
                accountId = (Integer) session.getAttribute("accountId");
            }

            // Get facility
            FacilityDTO facility = facilityService.getFacilityById(facilityId, accountId);

            if (facility != null) {
                Map<String, Object> responseData = new HashMap<>();
                responseData.put("success", true);
                responseData.put("data", facility);

                response.getWriter().write(gson.toJson(responseData));
            } else {
                sendErrorResponse(response, 404, "Facility not found");
            }

        } catch (NumberFormatException e) {
            sendErrorResponse(response, 400, "Invalid facility ID");
        }
    }

    /**
     * Send error response
     */
    private void sendErrorResponse(HttpServletResponse response, int statusCode, String message)
            throws IOException {

        response.setStatus(statusCode);

        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("error", message);

        response.getWriter().write(gson.toJson(errorResponse));
    }

    /**
     * Get integer parameter with default value
     */
    private int getIntParameter(HttpServletRequest request, String paramName, int defaultValue) {
        String paramValue = request.getParameter(paramName);
        if (paramValue != null && !paramValue.isEmpty()) {
            try {
                return Integer.parseInt(paramValue);
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }

    /**
     * Get double parameter (nullable)
     */
    private Double getDoubleParameter(HttpServletRequest request, String paramName) {
        String paramValue = request.getParameter(paramName);
        if (paramValue != null && !paramValue.isEmpty()) {
            try {
                return Double.parseDouble(paramValue);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }
}