package com.bcb.controller.owner;

import com.bcb.config.ConfigUpload;
import com.bcb.exception.BusinessException;
import com.bcb.exception.ValidationException;
import com.bcb.model.Facility;
import com.bcb.model.FacilityImage;
import com.bcb.service.FacilityImageService;
import com.bcb.service.FacilityService;
import com.bcb.service.UploadService;
import com.bcb.service.impl.FacilityImageServiceImpl;
import com.bcb.service.impl.FacilityServiceImpl;
import com.bcb.service.impl.UploadServiceImpl;
import com.bcb.utils.BreadcrumbUtils;
import com.bcb.validation.FacilityValidator;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/*
 *   @Author: AnhTN
 *
 */

@WebServlet("/owner/facility/*")
@MultipartConfig(
        fileSizeThreshold = 1024 * 1024 * 2, // 2MB
        maxFileSize = 1024 * 1024 * 10,      // 10MB
        maxRequestSize = 1024 * 1024 * 50    // 50MB
)
public class OwnerFacilityController extends HttpServlet {

    private FacilityService facilityService;
    private FacilityImageService facilityImageService;

    // Formatter for HTML <input type="time">
    private static final DateTimeFormatter TIME_INPUT_FORMATTER =
            DateTimeFormatter.ofPattern("HH:mm");

    @Override
    public void init() throws ServletException {
        facilityService = new FacilityServiceImpl();
        facilityImageService = new FacilityImageServiceImpl();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String pathInfo = request.getPathInfo();

        if (pathInfo == null || "/".equals(pathInfo)) {
            response.sendRedirect(request.getContextPath() + "/owner/facility/list");
            return;
        }

        try {
            if ("/list".equals(pathInfo)) {
                listFacilities(request, response);

            } else if (pathInfo.equals("/create")) {
                showCreateForm(request, response);
            } else if (pathInfo.startsWith("/view/")) {
                viewFacility(request, response, pathInfo);

            } else if (pathInfo.startsWith("/edit/")) {
                editFacility(request, response, pathInfo);

            } else if (pathInfo.startsWith("/delete/")) {
                deleteFacility(request, response, pathInfo);

            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (BusinessException e) {
            request.setAttribute("error", e.getMessage());
            BreadcrumbUtils.builder(request)
                    .dashboard()
                    .active("Địa Điểm Của Tôi")
                    .build();
            request.getRequestDispatcher("/jsp/owner/facility/facility-list.jsp")
                    .forward(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String action = request.getParameter("action");

        try {
            if ("create".equals(action)) {
                createFacility(request, response);

            } else if ("update".equals(action)) {
                updateFacility(request, response);

            } else {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            }
        } catch (ValidationException e) {
            request.setAttribute("error", "Validation error occurred");
            setBreadcrumbForForm(request, "update".equals(action));
            request.getRequestDispatcher("/jsp/owner/facility/facility-form.jsp")
                    .forward(request, response);
        } catch (BusinessException e) {
            request.setAttribute("error", e.getMessage());
            BreadcrumbUtils.builder(request)
                    .dashboard()
                    .active("Địa Điểm Của Tôi")
                    .build();
            request.getRequestDispatcher("/jsp/owner/facility/facility-list.jsp")
                    .forward(request, response);
        }
    }

    /* ===================== LIST ===================== */

    private void listFacilities(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        int pageSize = 5;
        int page = 1;

        String pageParam = request.getParameter("page");
        String sizeParam = request.getParameter("size");

        if (pageParam != null) {
            try {
                page = Math.max(1, Integer.parseInt(pageParam));
            } catch (NumberFormatException ignored) {
            }
        }

        if (sizeParam != null) {
            try {
                pageSize = Math.max(1, Math.min(100, Integer.parseInt(sizeParam)));
            } catch (NumberFormatException ignored) {
            }
        }

        int offset = (page - 1) * pageSize;

        String keyword = request.getParameter("keyword");
        boolean hasKeyword = keyword != null && !keyword.trim().isEmpty();

        List<Facility> facilities;
        int totalCount;
        if (hasKeyword) {
            String trimmedKeyword = keyword.trim();
            facilities = facilityService.findByKeyword(trimmedKeyword, pageSize, offset);
            totalCount = facilityService.countByKeyword(trimmedKeyword);
            request.setAttribute("keyword", trimmedKeyword);
        } else {
            facilities = facilityService.findAll(pageSize, offset);
            totalCount = facilityService.count();
        }
        int totalPages = (int) Math.ceil((double) totalCount / pageSize);

        Map<Integer, String> addressMap =
                facilityService.buildDisplayAddressMap(facilities);

        // Build time display map: facilityId -> "HH:mm – HH:mm" (23:59:59 → 24:00)
        Map<Integer, String> timeMap = new java.util.HashMap<>();
        for (Facility f : facilities) {
            if (f.getOpenTime() != null && f.getCloseTime() != null) {
                timeMap.put(f.getFacilityId(),
                        formatTimeForInput(f.getOpenTime()) + " – " + formatTimeForInput(f.getCloseTime()));
            } else {
                timeMap.put(f.getFacilityId(), "-");
            }
        }

        request.setAttribute("facilities", facilities);
        request.setAttribute("addressMap", addressMap);
        request.setAttribute("timeMap", timeMap);
        request.setAttribute("currentPage", page);
        request.setAttribute("pageSize", pageSize);
        request.setAttribute("totalPages", totalPages);
        request.setAttribute("totalRecords", totalCount);

        // Breadcrumb
        BreadcrumbUtils.builder(request)
                .dashboard()
                .active("Địa Điểm Của Tôi")
                .build();

        request.getRequestDispatcher("/jsp/owner/facility/facility-list.jsp")
                .forward(request, response);
    }

    /* ===================== VIEW ===================== */

    private void viewFacility(HttpServletRequest request, HttpServletResponse response,
                              String pathInfo)
            throws ServletException, IOException, BusinessException {

        int facilityId = Integer.parseInt(pathInfo.substring("/view/".length()));
        Facility facility = facilityService.findById(facilityId);
        FacilityImage thumbnail = facilityImageService.getThumbnail(facilityId);
        List<FacilityImage> gallery = facilityImageService.getGallery(facilityId);

        request.setAttribute("facility", facility);
        request.setAttribute("thumbnailImage", thumbnail);
        request.setAttribute("galleryImages", gallery);

        request.setAttribute("openTimeFormatted", formatTimeForInput(facility.getOpenTime()));
        request.setAttribute("closeTimeFormatted", formatTimeForInput(facility.getCloseTime()));

        // Breadcrumb
        BreadcrumbUtils.builder(request)
                .dashboard()
                .facilityList()
                .active("Chi Tiết Địa Điểm")
                .build();

        request.getRequestDispatcher("/jsp/owner/facility/facility-detail.jsp")
                .forward(request, response);
    }

    /* ===================== EDIT ===================== */

    private void editFacility(HttpServletRequest request, HttpServletResponse response,
                              String pathInfo)
            throws ServletException, IOException, BusinessException {

        int facilityId = Integer.parseInt(pathInfo.substring("/edit/".length()));
        Facility facility = facilityService.findById(facilityId);
        FacilityImage thumbnail = facilityImageService.getThumbnail(facilityId);
        List<FacilityImage> gallery = facilityImageService.getGallery(facilityId);

        request.setAttribute("facility", facility);
        request.setAttribute("thumbnailImage", thumbnail);
        request.setAttribute("galleryImages", gallery);

        request.setAttribute("openTimeFormatted", formatTimeForInput(facility.getOpenTime()));
        request.setAttribute("closeTimeFormatted", formatTimeForInput(facility.getCloseTime()));
        request.setAttribute("isEdit", true);

        // Breadcrumb
        BreadcrumbUtils.builder(request)
                .dashboard()
                .facilityList()
                .active("Chỉnh sửa địa điểm")
                .build();

        request.getRequestDispatcher("/jsp/owner/facility/facility-form.jsp")
                .forward(request, response);
    }

    /* ===================== CREATE ===================== */
    private void showCreateForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Breadcrumb
        BreadcrumbUtils.builder(request)
                .dashboard()
                .facilityList()
                .active("Tạo mới địa điểm")
                .build();

        request.getRequestDispatcher("/jsp/owner/facility/facility-form.jsp")
                .forward(request, response);
    }


    private void createFacility(HttpServletRequest request, HttpServletResponse response)
            throws BusinessException, IOException, ServletException {

        // Validate time inputs first (before parsing)
        String openTimeStr = request.getParameter("openTime");
        String closeTimeStr = request.getParameter("closeTime");
        List<String> timeErrors = new java.util.ArrayList<>();

        String openTimeError = validateTimeInput(openTimeStr, "Giờ mở cửa");
        String closeTimeError = validateTimeInput(closeTimeStr, "Giờ đóng cửa");
        if (openTimeError != null) timeErrors.add(openTimeError);
        if (closeTimeError != null) timeErrors.add(closeTimeError);

        Facility facility = buildFacilityFromRequest(request);
        List<String> errors = FacilityValidator.validate(facility);
        errors.addAll(timeErrors);

        // If time was invalid, reset the formatted value to empty
        request.setAttribute("openTimeFormatted",
                openTimeError == null ? (openTimeStr != null ? openTimeStr : "") : "");
        request.setAttribute("closeTimeFormatted",
                closeTimeError == null ? (closeTimeStr != null ? closeTimeStr : "") : "");

        if (facility.getLatitude() == null || facility.getLongitude() == null) {
            errors.add("Vui lòng chọn vị trí trên bản đồ");
        }

        if (!errors.isEmpty()) {
            request.setAttribute("errors", errors);
            request.setAttribute("facility", facility);
            setBreadcrumbForForm(request, false);
            request.getRequestDispatcher("/jsp/owner/facility/facility-form.jsp")
                    .forward(request, response);
            return;
        }

        // Multipart parts
        Part thumbnailPart = request.getPart("thumbnail");

        Collection<Part> galleryParts = request.getParts().stream()
                .filter(p -> "gallery".equals(p.getName()) && p.getSize() > 0)
                .toList();

        try {
            int facilityId = facilityService.createFacilityWithImages(
                    facility,
                    thumbnailPart,
                    galleryParts
            );

            response.sendRedirect(request.getContextPath()
                    + "/owner/facility/view/" + facilityId);

        } catch (BusinessException e) {
            request.setAttribute("error", e.getMessage());
            request.setAttribute("facility", facility);
            setBreadcrumbForForm(request, false);

            request.getRequestDispatcher("/jsp/owner/facility/facility-form.jsp")
                    .forward(request, response);
        }
    }

    /* ===================== UPDATE ===================== */

    private void updateFacility(HttpServletRequest request, HttpServletResponse response)
            throws BusinessException, IOException, ServletException {

        int facilityId = Integer.parseInt(request.getParameter("facilityId"));
        Facility facility = facilityService.findById(facilityId);

        // Validate time inputs first (before parsing)
        String openTimeStr = request.getParameter("openTime");
        String closeTimeStr = request.getParameter("closeTime");
        List<String> timeErrors = new java.util.ArrayList<>();

        String openTimeError = validateTimeInput(openTimeStr, "Giờ mở cửa");
        String closeTimeError = validateTimeInput(closeTimeStr, "Giờ đóng cửa");
        if (openTimeError != null) timeErrors.add(openTimeError);
        if (closeTimeError != null) timeErrors.add(closeTimeError);

        // Cập nhật các trường
        facility.setName(request.getParameter("name"));
        facility.setProvince(request.getParameter("province"));
        facility.setDistrict(request.getParameter("district"));
        facility.setWard(request.getParameter("ward"));
        facility.setAddress(request.getParameter("address"));
        facility.setDescription(request.getParameter("description"));
        facility.setOpenTime(parseTimeInput(openTimeStr));
        facility.setCloseTime(parseTimeInput(closeTimeStr));

        String latStr = request.getParameter("latitude");
        facility.setLatitude(latStr != null && !latStr.isBlank() ? new BigDecimal(latStr) : null);

        String lngStr = request.getParameter("longitude");
        facility.setLongitude(lngStr != null && !lngStr.isBlank() ? new BigDecimal(lngStr) : null);

        List<String> errors = FacilityValidator.validate(facility);
        errors.addAll(timeErrors);
        if (facility.getLatitude() == null || facility.getLongitude() == null) {
            errors.add("Vui lòng chọn vị trí trên bản đồ");
        }
        if (!errors.isEmpty()) {
            request.setAttribute("errors", errors);
            request.setAttribute("facility", facility);
            request.setAttribute("isEdit", true);
            request.setAttribute("thumbnailImage", facilityImageService.getThumbnail(facilityId));
            request.setAttribute("galleryImages", facilityImageService.getGallery(facilityId));
            // If time was invalid, reset to empty; if valid, keep the raw input
            request.setAttribute("openTimeFormatted",
                    openTimeError == null ? (openTimeStr != null ? openTimeStr : "") : "");
            request.setAttribute("closeTimeFormatted",
                    closeTimeError == null ? (closeTimeStr != null ? closeTimeStr : "") : "");

            setBreadcrumbForForm(request, true);
            request.getRequestDispatcher("/jsp/owner/facility/facility-form.jsp")
                    .forward(request, response);
            return;
        }

        // Lấy các phần multipart cần xử lý
        Part thumbnailPart = request.getPart("thumbnail");
        Collection<Part> galleryParts = request.getParts().stream()
                .filter(p -> "gallery".equals(p.getName()) && p.getSize() > 0)
                .toList();

        String deletedIds = request.getParameter("deletedIds");

        try {
            // Gọi service xử lý toàn bộ update + file
            facilityService.updateFacilityWithImages(
                    facility,
                    thumbnailPart,
                    galleryParts,
                    deletedIds
            );

            response.sendRedirect(request.getContextPath() + "/owner/facility/view/" + facilityId);

        } catch (BusinessException e) {
            request.setAttribute("error", e.getMessage());
            request.setAttribute("facility", facility);
            request.setAttribute("isEdit", true);
            request.setAttribute("thumbnailImage", facilityImageService.getThumbnail(facilityId));
            request.setAttribute("galleryImages", facilityImageService.getGallery(facilityId));
            request.setAttribute("openTimeFormatted", formatTimeForInput(facility.getOpenTime()));
            request.setAttribute("closeTimeFormatted", formatTimeForInput(facility.getCloseTime()));

            setBreadcrumbForForm(request, true);
            request.getRequestDispatcher("/jsp/owner/facility/facility-form.jsp")
                    .forward(request, response);
        }
    }

    /* ===================== DELETE ===================== */

    private void deleteFacility(HttpServletRequest request, HttpServletResponse response,
                                String pathInfo)
            throws BusinessException, IOException {

        int facilityId = Integer.parseInt(pathInfo.substring("/delete/".length()));
        facilityService.delete(facilityId);

        response.sendRedirect(request.getContextPath() + "/owner/facility/list");
    }

    /* ===================== HELPERS ===================== */

    /** Set breadcrumb for facility form (create or edit) */
    private void setBreadcrumbForForm(HttpServletRequest request, boolean isEdit) {
        BreadcrumbUtils.builder(request)
                .dashboard()
                .facilityList()
                .active(isEdit ? "Chỉnh sửa địa điểm" : "Tạo mới địa điểm")
                .build();
    }

    private Facility buildFacilityFromRequest(HttpServletRequest request) {
        Facility facility = new Facility();
        facility.setName(request.getParameter("name"));
        facility.setProvince(request.getParameter("province"));
        facility.setDistrict(request.getParameter("district"));
        facility.setWard(request.getParameter("ward"));
        facility.setAddress(request.getParameter("address"));

        String latStr = request.getParameter("latitude");
        if (latStr != null && !latStr.isBlank()) {
            facility.setLatitude(new BigDecimal(latStr));
        }

        String lngStr = request.getParameter("longitude");
        if (lngStr != null && !lngStr.isBlank()) {
            facility.setLongitude(new BigDecimal(lngStr));
        }

        facility.setDescription(request.getParameter("description"));
        facility.setOpenTime(parseTimeInput(request.getParameter("openTime")));
        facility.setCloseTime(parseTimeInput(request.getParameter("closeTime")));
        facility.setIsActive(true);
        return facility;
    }

    /**
     * Parse time input safely. Returns null if invalid (does NOT throw).
     */
    private LocalTime parseTimeInput(String timeStr) {
        if (timeStr == null || timeStr.isBlank()) return null;

        // Special case: Handle 24:00 as end of day
        if ("24:00".equals(timeStr)) {
            return LocalTime.of(23, 59, 59, 999999999);
        }

        // Reject invalid 24:XX formats
        if (timeStr.startsWith("24:")) {
            return null;
        }

        try {
            LocalTime time = LocalTime.parse(timeStr, TIME_INPUT_FORMATTER);
            int minute = time.getMinute();
            // Only 00 or 30 allowed
            if (minute != 0 && minute != 30) {
                return null;
            }
            return time;
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    /**
     * Validate time input and return error message, or null if valid.
     */
    private String validateTimeInput(String timeStr, String fieldLabel) {
        if (timeStr == null || timeStr.isBlank()) return null; // allow null, required check is elsewhere

        if ("24:00".equals(timeStr)) return null; // valid special case

        if (timeStr.startsWith("24:")) {
            return fieldLabel + " không hợp lệ. Chỉ 24:00 được phép để biểu thị cuối ngày.";
        }

        try {
            LocalTime time = LocalTime.parse(timeStr, TIME_INPUT_FORMATTER);
            int minute = time.getMinute();
            if (minute != 0 && minute != 30) {
                return fieldLabel + " phải là giờ chẵn hoặc nửa giờ (ví dụ: 08:00, 08:30).";
            }
        } catch (DateTimeParseException e) {
            return fieldLabel + " không đúng định dạng thời gian.";
        }

        return null;
    }

    private String formatTimeForInput(LocalTime time) {
        if (time == null) return "";

        // Special case: Display end of day (23:59:59.999999999) as 24:00
        if (time.getHour() == 23 && time.getMinute() == 59 && time.getSecond() == 59) {
            return "24:00";
        }

        return time
                .withSecond(0)
                .withNano(0)
                .format(TIME_INPUT_FORMATTER);
    }
}
