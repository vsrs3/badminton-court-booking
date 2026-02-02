package com.bcb.controller.owner;

import com.bcb.config.ConfigUpload;
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
            request.getRequestDispatcher("/jsp/owner/facility/facility-form.jsp")
                    .forward(request, response);
        } catch (BusinessException e) {
            request.setAttribute("error", e.getMessage());
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

        request.setAttribute("facilities", facilities);
        request.setAttribute("addressMap", addressMap);
        request.setAttribute("currentPage", page);
        request.setAttribute("pageSize", pageSize);
        request.setAttribute("totalPages", totalPages);
        request.setAttribute("totalRecords", totalCount);

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

        request.getRequestDispatcher("/jsp/owner/facility/facility-form.jsp")
                .forward(request, response);
    }

    /* ===================== CREATE ===================== */
    private void showCreateForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.getRequestDispatcher("/jsp/owner/facility/facility-form.jsp")
                .forward(request, response);
    }


    private void createFacility(HttpServletRequest request, HttpServletResponse response)
            throws BusinessException, IOException, ServletException {

        Facility facility = buildFacilityFromRequest(request);
        List<String> errors = FacilityValidator.validate(facility);

        if (!errors.isEmpty()) {
            request.setAttribute("errors", errors);
            request.setAttribute("facility", facility);
            request.getRequestDispatcher("/jsp/owner/facility/facility-form.jsp").forward(request, response);
            return;
        }

        int facilityId = facilityService.create(facility);

        Part thumbnailPart = request.getPart("thumbnail");
        if (thumbnailPart != null && thumbnailPart.getSize() > 0) {

            String thumbnailPath = saveFile(thumbnailPart);

            if (thumbnailPath != null) {
                FacilityImage thumbnail = new FacilityImage();
                thumbnail.setFacilityId(facilityId);
                thumbnail.setImagePath(thumbnailPath);
                thumbnail.setThumbnail(true);

                facilityImageService.addImage(thumbnail);
            }
        }

        // ================= GALLERY =================
        for (Part part : request.getParts()) {
            if ("gallery".equals(part.getName()) && part.getSize() > 0) {

                String imagePath = saveFile(part);

                if (imagePath != null) {
                    FacilityImage galleryImg = new FacilityImage();
                    galleryImg.setFacilityId(facilityId);
                    galleryImg.setImagePath(imagePath);
                    galleryImg.setThumbnail(false);

                    facilityImageService.addImage(galleryImg);
                }
            }
        }

        response.sendRedirect(request.getContextPath()
                + "/owner/facility/view/" + facilityId);
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
        String latStr = request.getParameter("latitude");
        if (latStr != null && !latStr.isBlank()) {
            facility.setLatitude(new BigDecimal(latStr));
        } else {
            facility.setLatitude(null);
        }

        String lngStr = request.getParameter("longitude");
        if (lngStr != null && !lngStr.isBlank()) {
            facility.setLongitude(new BigDecimal(lngStr));
        } else {
            facility.setLongitude(null);
        }

        List<String> errors = FacilityValidator.validate(facility);

        if (!errors.isEmpty()) {
            request.setAttribute("errors", errors);
            request.setAttribute("facility", facility);
            request.setAttribute("isEdit", true);

            // load lại ảnh cũ
            request.setAttribute("thumbnailImage",
                    facilityImageService.getThumbnail(facilityId));
            request.setAttribute("galleryImages",
                    facilityImageService.getGallery(facilityId));

            request.setAttribute("openTimeFormatted",
                    formatTimeForInput(facility.getOpenTime()));
            request.setAttribute("closeTimeFormatted",
                    formatTimeForInput(facility.getCloseTime()));

            request.getRequestDispatcher("/jsp/owner/facility/facility-form.jsp")
                    .forward(request, response);
            return;
        }

        facilityService.update(facility);

        // THUMBNAIL IMAGE
        Part thumbnailPart = request.getPart("thumbnail");
        if (thumbnailPart != null && thumbnailPart.getSize() > 0) {

            String newThumbnailPath = saveFile(thumbnailPart);

            if (newThumbnailPath != null) {
                FacilityImage currentThumbnail = facilityImageService.getThumbnail(facilityId);

                if (currentThumbnail != null) {

                    deleteFile(currentThumbnail.getImagePath());

                    currentThumbnail.setImagePath(newThumbnailPath);
                    facilityImageService.update(currentThumbnail);
                } else {
                    //  Add new
                    FacilityImage newThumb = new FacilityImage();
                    newThumb.setFacilityId(facilityId);
                    newThumb.setImagePath(newThumbnailPath);
                    newThumb.setThumbnail(true);
                    facilityImageService.addImage(newThumb);
                }
            }
        }


        // Delete gallery images
        String deletedIds = request.getParameter("deletedIds");
        if (deletedIds != null && !deletedIds.isEmpty()) {
            String[] ids = deletedIds.split(",");
            for (String idStr : ids) {
                try {

                    int imgId = Integer.parseInt(idStr);
                    FacilityImage image = facilityImageService.getImageById(imgId);

                    if (image != null) {
                        deleteFile(image.getImagePath());
                        facilityImageService.deleteImage(imgId);
                    }
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }
        }


        Collection<Part> parts = request.getParts();
        for (Part part : parts) {
            if ("gallery".equals(part.getName()) && part.getSize() > 0) {

                String imagePath = saveFile(part);

                if (imagePath != null) {
                    FacilityImage galleryImg = new FacilityImage();
                    galleryImg.setFacilityId(facilityId);
                    galleryImg.setImagePath(imagePath);
                    galleryImg.setThumbnail(false);

                    facilityImageService.addImage(galleryImg);
                }
            }
        }

        response.sendRedirect(request.getContextPath()
                + "/owner/facility/view/" + facilityId);
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
        facility.setActive(true);
        return facility;
    }

    // Hàm hỗ trợ lưu file từ Part vào thư mục server
    private String saveFile(Part part) throws IOException, BusinessException {

        String submitted = Paths.get(part.getSubmittedFileName())
                .getFileName().toString();

        if (submitted.isBlank()) return null;

        if (!part.getContentType().startsWith("image/")) {
            throw new BusinessException("Only image files allowed");
        }

        String ext = "";
        int dot = submitted.lastIndexOf('.');
        if (dot > 0) ext = submitted.substring(dot);

        String fileName = UUID.randomUUID() + ext;

        String rootPath = ConfigUpload.getUploadLocation();

        File uploadDir = new File(rootPath, "facility");
        if (!uploadDir.exists()) uploadDir.mkdirs();

        File file = new File(uploadDir, fileName);

        try (InputStream in = part.getInputStream()) {
            Files.copy(in, file.toPath());
        }

        // DB chỉ lưu path logic (relative từ /uploads/)
        return "facility/" + fileName;
    }

    // delete files
    private void deleteFile(String imagePath) {

        if (imagePath == null || imagePath.isBlank()) return;

        String rootPath = ConfigUpload.getUploadLocation();

        File file = new File(rootPath, imagePath);

        if (file.exists() && file.isFile()) {
            boolean deleted = file.delete();
            if (!deleted) {
                System.err.println("Cannot delete file: " + file.getAbsolutePath());
            }
        }
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
