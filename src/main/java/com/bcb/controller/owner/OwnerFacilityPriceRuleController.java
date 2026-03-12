package com.bcb.controller.owner;

import com.bcb.dto.FacilityPriceViewDTO;
import com.bcb.dto.PriceRuleRequestDTO;
import com.bcb.dto.SmartPriceConfigRequestDTO;
import com.bcb.dto.SmartPriceConfigRowDTO;
import com.bcb.exception.BusinessException;
import com.bcb.service.FacilityPriceRuleService;
import com.bcb.service.impl.FacilityPriceRuleServiceImpl;
import com.bcb.utils.BreadcrumbUtils;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/owner/prices/*")
public class OwnerFacilityPriceRuleController extends HttpServlet {

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
            Integer courtTypeId = (courtTypeIdStr != null && !courtTypeIdStr.isEmpty())
                    ? Integer.parseInt(courtTypeIdStr) : null;
            String dayType = request.getParameter("dayType");

            FacilityPriceViewDTO viewData = facilityPriceRuleService.getPriceView(facilityId, courtTypeId, dayType);
            request.setAttribute("viewData", viewData);

            // Breadcrumb
            BreadcrumbUtils.builder(request)
                    .dashboard()
                    .facilityList()
                    .facility(viewData.getFacilityName(), facilityId)
                    .active("Cài đặt giá")
                    .build();

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
            if ("/create".equals(path)) {
                createPriceRule(request, response);
            } else if ("/update".equals(path)) {
                updatePriceRule(request, response);
            } else if ("/delete".equals(path)) {
                deletePriceRule(request, response);
            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (BusinessException | NumberFormatException e) {
            // Store error message in session (flash message)
            request.getSession().setAttribute("flashError", e.getMessage());

            String facilityId = request.getParameter("facilityId");
            String courtTypeId = request.getParameter("courtTypeId");
            String dayType = request.getParameter("dayType");

            // Redirect without error in URL
            response.sendRedirect(request.getContextPath() + "/owner/prices?facilityId=" + facilityId
                    + "&courtTypeId=" + courtTypeId
                    + "&dayType=" + dayType);
        }
    }

    /**
     * PUT /owner/prices/smart-config
     * Replaces ALL price rules for a facility atomically.
     * Returns JSON: {"success":true} or {"success":false,"message":"..."}
     */
    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String path = request.getPathInfo();
        response.setContentType("application/json;charset=UTF-8");

        if (!"/smart-config".equals(path)) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            writeJson(response, false, "Not found");
            return;
        }

        try {
            SmartPriceConfigRequestDTO dto = parseSmartConfigJson(request);
            facilityPriceRuleService.saveSmartPriceConfig(dto);
            writeJson(response, true, "Cau hinh gia da duoc luu thanh cong");
        } catch (BusinessException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            writeJson(response, false, e.getMessage());
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            writeJson(response, false, "Loi server: " + e.getMessage());
        }
    }

    // ── JSON parser (no external library) ──────────────────────────────────────

    private SmartPriceConfigRequestDTO parseSmartConfigJson(HttpServletRequest request)
            throws IOException, BusinessException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) sb.append(line);
        }
        String body = sb.toString().trim();

        SmartPriceConfigRequestDTO dto = new SmartPriceConfigRequestDTO();

        String facilityIdStr = extractJsonValue(body, "facilityId");
        if (facilityIdStr == null) throw new BusinessException("facilityId is required");
        dto.setFacilityId(Integer.parseInt(facilityIdStr.trim()));

        int arrayStart = body.indexOf("[");
        int arrayEnd   = body.lastIndexOf("]");
        if (arrayStart < 0 || arrayEnd < 0) throw new BusinessException("priceConfigs array is required");

        String arrayContent = body.substring(arrayStart + 1, arrayEnd).trim();
        List<SmartPriceConfigRowDTO> rows = new ArrayList<>();

        for (String obj : splitJsonObjects(arrayContent)) {
            if (obj.isBlank()) continue;
            SmartPriceConfigRowDTO row = new SmartPriceConfigRowDTO();
            row.setStartTime(stripQuotes(extractJsonValue(obj, "startTime")));
            row.setEndTime(stripQuotes(extractJsonValue(obj, "endTime")));

            String nw = extractJsonValue(obj, "normalWeekdayPrice");
            String ne = extractJsonValue(obj, "normalWeekendPrice");
            String vw = extractJsonValue(obj, "vipWeekdayPrice");
            String ve = extractJsonValue(obj, "vipWeekendPrice");

            if (nw != null) row.setNormalWeekdayPrice(new BigDecimal(nw.trim()));
            if (ne != null) row.setNormalWeekendPrice(new BigDecimal(ne.trim()));
            if (vw != null) row.setVipWeekdayPrice(new BigDecimal(vw.trim()));
            if (ve != null) row.setVipWeekendPrice(new BigDecimal(ve.trim()));
            rows.add(row);
        }

        dto.setPriceConfigs(rows);
        return dto;
    }

    private String extractJsonValue(String json, String key) {
        String search = "\"" + key + "\"";
        int idx = json.indexOf(search);
        if (idx < 0) return null;
        int colon = json.indexOf(':', idx + search.length());
        if (colon < 0) return null;
        int vs = colon + 1;
        while (vs < json.length() && json.charAt(vs) == ' ') vs++;
        if (vs >= json.length()) return null;
        if (json.charAt(vs) == '"') {
            int end = json.indexOf('"', vs + 1);
            return json.substring(vs + 1, end);
        }
        int end = vs;
        while (end < json.length() && ",}\n\r ".indexOf(json.charAt(end)) < 0) end++;
        return json.substring(vs, end);
    }

    private List<String> splitJsonObjects(String arrayContent) {
        List<String> objects = new ArrayList<>();
        int depth = 0, start = -1;
        for (int i = 0; i < arrayContent.length(); i++) {
            char c = arrayContent.charAt(i);
            if (c == '{') { if (depth == 0) start = i; depth++; }
            else if (c == '}') {
                depth--;
                if (depth == 0 && start >= 0) { objects.add(arrayContent.substring(start + 1, i)); start = -1; }
            }
        }
        return objects;
    }

    private String stripQuotes(String s) {
        if (s == null) return null;
        s = s.trim();
        return (s.startsWith("\"") && s.endsWith("\"")) ? s.substring(1, s.length() - 1) : s;
    }

    private void writeJson(HttpServletResponse response, boolean success, String message) throws IOException {
        String escaped = message == null ? "" : message
                .replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
        try (PrintWriter out = response.getWriter()) {
            out.print("{\"success\":" + success + ",\"message\":\"" + escaped + "\"}");
        }
    }

    // ── Form-POST helpers ───────────────────────────────────────────────────────

    private void createPriceRule(HttpServletRequest request, HttpServletResponse response)
            throws IOException, BusinessException {

        PriceRuleRequestDTO dto = buildRequestDTO(request, null);
        facilityPriceRuleService.createPriceRule(dto);
        request.getSession().setAttribute("flashSuccess", "Tao cau hinh gia thanh cong");
        response.sendRedirect(request.getContextPath() + "/owner/prices?facilityId=" + dto.getFacilityId()
                + "&courtTypeId=" + dto.getCourtTypeId()
                + "&dayType=" + dto.getDayType());
    }

    private void updatePriceRule(HttpServletRequest request, HttpServletResponse response)
            throws IOException, BusinessException {

        String priceIdStr = request.getParameter("priceId");
        if (priceIdStr == null || priceIdStr.isEmpty()) {
            throw new BusinessException("Price ID is required");
        }

        Integer priceId = Integer.parseInt(priceIdStr);
        PriceRuleRequestDTO dto = buildRequestDTO(request, priceId);
        facilityPriceRuleService.updatePriceRule(dto);

        // Store success message in session (flash message)
        request.getSession().setAttribute("flashSuccess", "Cập nhật cấu hình giá thành công");

        // Redirect without message in URL
        response.sendRedirect(request.getContextPath() + "/owner/prices?facilityId=" + dto.getFacilityId()
                + "&courtTypeId=" + dto.getCourtTypeId()
                + "&dayType=" + dto.getDayType());
    }

    private void deletePriceRule(HttpServletRequest request, HttpServletResponse response)
            throws IOException, BusinessException {

        String priceIdStr = request.getParameter("priceId");
        if (priceIdStr == null || priceIdStr.isEmpty()) {
            throw new BusinessException("Price ID is required");
        }

        int priceId = Integer.parseInt(priceIdStr);
        int facilityId = Integer.parseInt(request.getParameter("facilityId"));
        int courtTypeId = Integer.parseInt(request.getParameter("courtTypeId"));
        String dayType = request.getParameter("dayType");

        facilityPriceRuleService.deletePriceRule(priceId);

        // Store success message in session (flash message)
        request.getSession().setAttribute("flashSuccess", "Xóa cấu hình giá thành công");

        // Redirect without message in URL
        response.sendRedirect(request.getContextPath() + "/owner/prices?facilityId=" + facilityId
                + "&courtTypeId=" + courtTypeId
                + "&dayType=" + dayType);
    }

    private PriceRuleRequestDTO buildRequestDTO(HttpServletRequest request, Integer priceId)
            throws BusinessException {

        PriceRuleRequestDTO dto = new PriceRuleRequestDTO();
        dto.setPriceId(priceId);

        try {
            dto.setFacilityId(Integer.parseInt(request.getParameter("facilityId")));
            dto.setCourtTypeId(Integer.parseInt(request.getParameter("courtTypeId")));
            dto.setDayType(request.getParameter("dayType"));

            String startTimeStr = request.getParameter("startTime");
            String endTimeStr = request.getParameter("endTime");
            String priceStr = request.getParameter("price");

            if (startTimeStr == null || endTimeStr == null || priceStr == null) {
                throw new BusinessException("Start time, end time, and price are required");
            }

            // Validate time inputs
            String startTimeError = validateTimeInput(startTimeStr, "Giờ bắt đầu");
            String endTimeError = validateTimeInput(endTimeStr, "Giờ kết thúc");
            if (startTimeError != null) throw new BusinessException(startTimeError);
            if (endTimeError != null) throw new BusinessException(endTimeError);

            // Parse times with support for 24:00
            dto.setStartTime(parseTimeInput(startTimeStr));
            dto.setEndTime(parseTimeInput(endTimeStr));
            dto.setPricePerHour(new BigDecimal(priceStr));

        } catch (NumberFormatException e) {
            throw new BusinessException("Invalid number format");
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException("Invalid input: " + e.getMessage());
        }

        return dto;
    }

    /**
     * Validate time input and return error message, or null if valid.
     */
    private String validateTimeInput(String timeStr, String fieldLabel) {
        if (timeStr == null || timeStr.isBlank()) return null;

        if ("24:00".equals(timeStr)) return null; // valid special case

        if (timeStr.startsWith("24:")) {
            return fieldLabel + " không hợp lệ. Chỉ 24:00 được phép để biểu thị cuối ngày.";
        }

        try {
            LocalTime time = LocalTime.parse(timeStr);
            int minute = time.getMinute();
            if (minute != 0 && minute != 30) {
                return fieldLabel + " phải là giờ chẵn hoặc nửa giờ (ví dụ: 08:00, 08:30).";
            }
        } catch (Exception e) {
            return fieldLabel + " không đúng định dạng thời gian.";
        }

        return null;
    }

    /**
     * Parse time string from UI (supports 24:00 for end of day).
     * Returns null if invalid (does NOT throw).
     */
    private LocalTime parseTimeInput(String timeStr) {
        if (timeStr == null || timeStr.isBlank()) {
            return null;
        }

        // Special case: Handle 24:00 as end of day
        if ("24:00".equals(timeStr)) {
            return LocalTime.of(23, 59, 59, 999999999);
        }

        // Reject invalid 24:XX formats
        if (timeStr.startsWith("24:")) {
            return null;
        }

        try {
            LocalTime time = LocalTime.parse(timeStr);
            int minute = time.getMinute();
            if (minute != 0 && minute != 30) {
                return null;
            }
            return time;
        } catch (Exception e) {
            return null;
        }
    }
}

