package com.bcb.controller;

import com.bcb.exception.BusinessException;
import com.bcb.exception.ValidationException;
import com.bcb.model.Facility;
import com.bcb.model.FacilityImage;
import com.bcb.service.FacilityImageService;
import com.bcb.service.FacilityService;
import com.bcb.service.impl.FacilityImageServiceImpl;
import com.bcb.service.impl.FacilityServiceImpl;
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
import java.nio.file.Paths;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

/*
 *   @Author: AnhTN
 *
 */

@WebServlet("/admin/facility/*")
@MultipartConfig(
        fileSizeThreshold = 1024 * 1024 * 2, // 2MB
        maxFileSize = 1024 * 1024 * 10,      // 10MB
        maxRequestSize = 1024 * 1024 * 50    // 50MB
)
public class FacilityController extends HttpServlet {

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
            response.sendRedirect(request.getContextPath() + "/admin/facility/list");
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
            request.getRequestDispatcher("/jsp/admin/facility/facility-list.jsp")
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
            request.getRequestDispatcher("/jsp/admin/facility/facility-form.jsp")
                    .forward(request, response);
        } catch (BusinessException e) {
            request.setAttribute("error", e.getMessage());
            request.getRequestDispatcher("/jsp/admin/facility/facility-list.jsp")
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

        request.setAttribute("facilities", facilities);
        request.setAttribute("currentPage", page);
        request.setAttribute("pageSize", pageSize);
        request.setAttribute("totalPages", totalPages);
        request.setAttribute("totalRecords", totalCount);

        request.getRequestDispatcher("/jsp/admin/facility/facility-list.jsp")
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

        request.getRequestDispatcher("/jsp/admin/facility/facility-detail.jsp")
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

        request.getRequestDispatcher("/jsp/admin/facility/facility-form.jsp")
                .forward(request, response);
    }

    /* ===================== CREATE ===================== */
    private void showCreateForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.getRequestDispatcher("/jsp/admin/facility/facility-form.jsp")
                .forward(request, response);
    }


    private void createFacility(HttpServletRequest request, HttpServletResponse response)
            throws BusinessException, IOException {

        Facility facility = buildFacilityFromRequest(request);
        FacilityValidator.validate(facility);

        int facilityId = facilityService.create(facility);

        response.sendRedirect(request.getContextPath()
                + "/admin/facility/view/" + facilityId);
    }

    /* ===================== UPDATE ===================== */

    private void updateFacility(HttpServletRequest request, HttpServletResponse response)
            throws BusinessException, IOException, ServletException {

        int facilityId = Integer.parseInt(request.getParameter("facilityId"));
        Facility facility = facilityService.findById(facilityId);

        facility.setName(request.getParameter("name"));
        facility.setProvince(request.getParameter("province"));
        facility.setDistrict(request.getParameter("district"));
        facility.setWard(request.getParameter("ward"));
        facility.setAddress(request.getParameter("address"));
        facility.setDescription(request.getParameter("description"));
        facility.setOpenTime(parseTimeInput(request.getParameter("openTime")));
        facility.setCloseTime(parseTimeInput(request.getParameter("closeTime")));

        FacilityValidator.validate(facility);
        facilityService.update(facility);

        // THUMBNAIL IMAGE
        Part thumbnailPart = request.getPart("thumbnail");
        if (thumbnailPart != null && thumbnailPart.getSize() > 0) {

            String newThumbnailPath = saveFile(thumbnailPart, request);

            if (newThumbnailPath != null) {
                FacilityImage currentThumbnail = facilityImageService.getThumbnail(facilityId);

                if (currentThumbnail != null) {
                    currentThumbnail.setImagePath(newThumbnailPath);
                    // (Tuỳ chọn: Xóa file ảnh cũ trên ổ đĩa tại đây)
                    facilityImageService.update(currentThumbnail);
                } else {
                    //  Tạo mới
                    FacilityImage newThumb = new FacilityImage();
                    newThumb.setFacilityId(facilityId);
                    newThumb.setImagePath(newThumbnailPath);
                    newThumb.setThumbnail(true);
                    facilityImageService.addImage(newThumb);
                }
            }
        }


        //XÓA ẢNH GALLERY CŨ
        String deletedIds = request.getParameter("deletedIds");
        if (deletedIds != null && !deletedIds.isEmpty()) {
            String[] ids = deletedIds.split(",");
            for (String idStr : ids) {
                try {
                    int imgId = Integer.parseInt(idStr);
                    // Hàm delete này sẽ: DELETE FROM FacilityImage WHERE image_id = ?
                    facilityImageService.deleteImage(imgId);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }
        }


        // THÊM ẢNH GALLERY MỚI
        Collection<Part> parts = request.getParts();
        for (Part part : parts) {
            // Chỉ lấy input có name="gallery"
            if ("gallery".equals(part.getName()) && part.getSize() > 0) {

                String imagePath = saveFile(part, request);

                if (imagePath != null) {
                    FacilityImage galleryImg = new FacilityImage();
                    galleryImg.setFacilityId(facilityId);
                    galleryImg.setImagePath(imagePath);
                    galleryImg.setThumbnail(false); // Set bit = 0 (Mặc định)

                    facilityImageService.addImage(galleryImg);
                }
            }
        }

        response.sendRedirect(request.getContextPath()
                + "/admin/facility/view/" + facilityId);
    }

    /* ===================== DELETE ===================== */

    private void deleteFacility(HttpServletRequest request, HttpServletResponse response,
                                String pathInfo)
            throws BusinessException, IOException {

        int facilityId = Integer.parseInt(pathInfo.substring("/delete/".length()));
        facilityService.delete(facilityId);

        response.sendRedirect(request.getContextPath() + "/admin/facility/list");
    }

    /* ===================== HELPERS ===================== */

    private Facility buildFacilityFromRequest(HttpServletRequest request) {
        Facility facility = new Facility();
        facility.setName(request.getParameter("name"));
        facility.setProvince(request.getParameter("province"));
        facility.setDistrict(request.getParameter("district"));
        facility.setWard(request.getParameter("ward"));
        facility.setAddress(request.getParameter("address"));
        facility.setDescription(request.getParameter("description"));
        facility.setOpenTime(parseTimeInput(request.getParameter("openTime")));
        facility.setCloseTime(parseTimeInput(request.getParameter("closeTime")));
        facility.setActive(true);
        return facility;
    }

    // Hàm hỗ trợ lưu file từ Part vào thư mục server
    private String saveFile(Part part, HttpServletRequest request) throws IOException {

        String submitted = Paths.get(part.getSubmittedFileName())
                .getFileName().toString();

        if (submitted.isBlank()) {
            return null;
        }

        // validate mime
        if (!part.getContentType().startsWith("image/")) {
            throw new IOException("Only image files are allowed");
        }

        // lấy extension
        String ext = "";
        int dot = submitted.lastIndexOf('.');
        if (dot > 0) {
            ext = submitted.substring(dot);
        }

        String uniqueFileName = UUID.randomUUID() + ext;

        String uploadPath = request.getServletContext()
                .getRealPath("/assets/images/facility");

        File uploadDir = new File(uploadPath);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }

        part.write(uploadPath + File.separator + uniqueFileName);

        return "assets/images/facility/" + uniqueFileName;
    }


    private LocalTime parseTimeInput(String timeStr) {
        if (timeStr == null || timeStr.isBlank()) return null;
        try {
            return LocalTime.parse(timeStr, TIME_INPUT_FORMATTER);
        } catch (DateTimeParseException e) {
            throw new DateTimeParseException(
                    "Invalid time format (expected HH:mm)", timeStr, 0);
        }
    }

    private String formatTimeForInput(LocalTime time) {
        return time == null ? "" : time.format(TIME_INPUT_FORMATTER);
    }
}
