package com.bcb.controller.api;

import com.bcb.dto.FacilityDTO;
import com.bcb.model.Account;
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

/**
 * REST API Controller for Facility endpoints
 */
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

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String pathInfo = request.getPathInfo();

        try {
            if (pathInfo == null || pathInfo.equals("/")) {
                // GET /api/facilities
                handleGetFacilities(request, response);
                return;
            }

            // GET /api/facilities/{id}
            String[] pathParts = pathInfo.split("/");
            if (pathParts.length == 2) {
                handleGetFacilityById(request, response, pathParts[1]);
            } else {
                sendErrorResponse(response, 404, "Endpoint not found");
            }
        } catch (Exception e) {
            sendErrorResponse(response, 500, "Internal server error: " + e.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String pathInfo = request.getPathInfo();
        String[] pathParts = pathInfo == null ? new String[0] : pathInfo.split("/");

        try {
            // POST /api/facilities/favorites/{facilityId}
            if (pathParts.length == 3 && "favorites".equals(pathParts[1])) {
                Integer accountId = getSessionAccountId(request);
                if (accountId == null) {
                    sendErrorResponse(response, 401, "Unauthorized");
                    return;
                }

                int facilityId = Integer.parseInt(pathParts[2]);
                boolean ok = facilityService.addFavorite(accountId, facilityId);

                Map<String, Object> data = new HashMap<>();
                data.put("success", ok);
                data.put("data", Map.of("facilityId", facilityId, "isFavorite", true));
                response.getWriter().write(gson.toJson(data));
                return;
            }

            sendErrorResponse(response, 404, "Endpoint not found");
        } catch (NumberFormatException e) {
            sendErrorResponse(response, 400, "Invalid facility ID");
        } catch (Exception e) {
            sendErrorResponse(response, 500, "Internal server error: " + e.getMessage());
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String pathInfo = request.getPathInfo();
        String[] pathParts = pathInfo == null ? new String[0] : pathInfo.split("/");

        try {
            // DELETE /api/facilities/favorites/{facilityId}
            if (pathParts.length == 3 && "favorites".equals(pathParts[1])) {
                Integer accountId = getSessionAccountId(request);
                if (accountId == null) {
                    sendErrorResponse(response, 401, "Unauthorized");
                    return;
                }

                int facilityId = Integer.parseInt(pathParts[2]);
                boolean ok = facilityService.removeFavorite(accountId, facilityId);

                Map<String, Object> data = new HashMap<>();
                data.put("success", ok);
                data.put("data", Map.of("facilityId", facilityId, "isFavorite", false));
                response.getWriter().write(gson.toJson(data));
                return;
            }

            sendErrorResponse(response, 404, "Endpoint not found");
        } catch (NumberFormatException e) {
            sendErrorResponse(response, 400, "Invalid facility ID");
        } catch (Exception e) {
            sendErrorResponse(response, 500, "Internal server error: " + e.getMessage());
        }
    }

    /**
     * Handle GET /api/facilities
     */
    private void handleGetFacilities(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        int page = getIntParameter(request, "page", 0);
        int pageSize = getIntParameter(request, "pageSize", 12);

        Double userLat = getDoubleParameter(request, "userLat");
        Double userLng = getDoubleParameter(request, "userLng");

        String keyword = trimToNull(request.getParameter("q"));
        String province = trimToNull(request.getParameter("province"));
        String district = trimToNull(request.getParameter("district"));
        Double maxDistance = getDoubleParameter(request, "maxDistance");
        boolean favoritesOnly = "true".equalsIgnoreCase(request.getParameter("favoritesOnly"));

        Integer accountId = getSessionAccountId(request);
        if (favoritesOnly && accountId == null) {
            sendErrorResponse(response, 401, "Unauthorized");
            return;
        }

        List<FacilityDTO> facilities = facilityService.getFacilities(
                page,
                pageSize,
                userLat,
                userLng,
                maxDistance,
                accountId,
                keyword,
                province,
                district,
                favoritesOnly
        );
        int totalCount = facilityService.getTotalCount(keyword, province, district, accountId, favoritesOnly,
                userLat, userLng, maxDistance);

        int totalPages = (int) Math.ceil((double) totalCount / pageSize);
        boolean hasMore = (page + 1) < totalPages;

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("success", true);
        responseData.put("data", facilities);
        responseData.put("pagination", Map.of(
                "page", page,
                "pageSize", pageSize,
                "totalItems", totalCount,
                "totalPages", totalPages,
                "hasMore", hasMore
        ));

        response.getWriter().write(gson.toJson(responseData));
    }

    /**
     * Handle GET /api/facilities/{id}
     */
    private void handleGetFacilityById(HttpServletRequest request, HttpServletResponse response, String id)
            throws IOException {

        try {
            Integer facilityId = Integer.parseInt(id);
            Integer accountId = getSessionAccountId(request);

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

    private Integer getSessionAccountId(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }

        if (session.getAttribute("accountId") != null) {
            return (Integer) session.getAttribute("accountId");
        }

        Object accountObj = session.getAttribute("account");
        if (accountObj instanceof Account) {
            return ((Account) accountObj).getAccountId();
        }

        return null;
    }

    private void sendErrorResponse(HttpServletResponse response, int statusCode, String message)
            throws IOException {

        response.setStatus(statusCode);

        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("error", message);

        response.getWriter().write(gson.toJson(errorResponse));
    }

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

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}


