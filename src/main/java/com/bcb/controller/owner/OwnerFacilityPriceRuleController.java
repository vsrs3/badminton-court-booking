package com.bcb.controller.owner;

import com.bcb.dto.FacilityPriceViewDTO;
import com.bcb.dto.PriceRuleRequestDTO;
import com.bcb.exception.BusinessException;
import com.bcb.service.FacilityPriceRuleService;
import com.bcb.service.impl.FacilityPriceRuleServiceImpl;
import com.bcb.utils.BreadcrumbUtils;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalTime;

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

    private void createPriceRule(HttpServletRequest request, HttpServletResponse response)
            throws IOException, BusinessException {

        PriceRuleRequestDTO dto = buildRequestDTO(request, null);
        facilityPriceRuleService.createPriceRule(dto);

        // Store success message in session (flash message)
        request.getSession().setAttribute("flashSuccess", "Tạo cấu hình giá thành công");

        // Redirect without message in URL
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
