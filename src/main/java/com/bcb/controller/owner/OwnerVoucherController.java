package com.bcb.controller.owner;

import com.bcb.exception.BusinessException;
import com.bcb.model.Facility;
import com.bcb.model.Voucher;
import com.bcb.service.voucher.VoucherService;
import com.bcb.service.voucher.impl.VoucherServiceImpl;
import com.bcb.utils.BreadcrumbUtils;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Owner Voucher Controller – handles page rendering for voucher management.
 * URL patterns:
 *   GET  /owner/vouchers             → Danh sách voucher
 *   GET  /owner/vouchers/dashboard   → Dashboard thống kê
 *   GET  /owner/vouchers/create      → Form tạo voucher
 *   GET  /owner/vouchers/edit        → Form sửa voucher (?id=...)
 *   GET  /owner/vouchers/detail      → Chi tiết voucher (?id=...)
 *   POST /owner/vouchers/create      → Lưu voucher mới
 *   POST /owner/vouchers/update      → Cập nhật voucher
 *   GET  /owner/vouchers/delete      → Xóa mềm voucher (?id=...)
 *
 * @author AnhTN
 */
@WebServlet("/owner/vouchers/*")
public class OwnerVoucherController extends HttpServlet {

    private static final DateTimeFormatter DT_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
    private static final DateTimeFormatter DT_FORMATTER_ALT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private VoucherService voucherService;

    @Override
    public void init() throws ServletException {
        voucherService = new VoucherServiceImpl();
    }

    // =====================================================================
    // GET
    // =====================================================================

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String path = req.getPathInfo();
        if (path == null || path.equals("/")) {
            resp.sendRedirect(req.getContextPath() + "/owner/vouchers/list");
            return;
        }

        try {
            switch (path) {
                case "/dashboard" -> showDashboard(req, resp);
                case "/list"      -> showList(req, resp);
                case "/create"    -> showCreateForm(req, resp);
                case "/edit"      -> showEditForm(req, resp);
                case "/detail"    -> showDetail(req, resp);
                case "/delete"    -> handleDelete(req, resp);
                default           -> resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (BusinessException e) {
            req.setAttribute("error", e.getMessage());
            req.getRequestDispatcher("/jsp/owner/voucher/owner-voucher-list.jsp")
               .forward(req, resp);
        }
    }

    // =====================================================================
    // POST
    // =====================================================================

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");
        String path = req.getPathInfo();

        try {
            if ("/create".equals(path)) {
                handleCreate(req, resp);
            } else if ("/update".equals(path)) {
                handleUpdate(req, resp);
            } else {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (BusinessException e) {
            req.setAttribute("formError", e.getMessage());
            // Re-load form with error
            try {
                if ("/create".equals(path)) {
                    showCreateForm(req, resp);
                } else {
                    showEditForm(req, resp);
                }
            } catch (BusinessException be) {
                req.setAttribute("error", be.getMessage());
                req.getRequestDispatcher("/jsp/owner/voucher/owner-voucher-list.jsp").forward(req, resp);
            }
        }
    }

    // =====================================================================
    // PAGE HANDLERS
    // =====================================================================

    /** Render dashboard page – data loaded via AJAX */
    private void showDashboard(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        BreadcrumbUtils.builder(req)
            .dashboard()
            .add("Quản Lý Voucher", req.getContextPath() + "/owner/vouchers/dashboard")
            .active("Dashboard Voucher")
            .build();
        req.getRequestDispatcher("/jsp/owner/voucher/owner-voucher-dashboard.jsp")
           .forward(req, resp);
    }

    /** Render list page – table data loaded via AJAX */
    private void showList(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // Load facilities for filter dropdown
        List<Facility> facilities = voucherService.getAllFacilities();
        req.setAttribute("facilities", facilities);

        BreadcrumbUtils.builder(req)
            .dashboard()
            .add("Quản Lý Voucher", req.getContextPath() + "/owner/vouchers/dashboard")
            .active("Danh Sách Voucher")
            .build();
        req.getRequestDispatcher("/jsp/owner/voucher/owner-voucher-list.jsp")
           .forward(req, resp);
    }

    /** Render create form */
    private void showCreateForm(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        List<Facility> facilities = voucherService.getAllFacilities();
        req.setAttribute("facilities", facilities);
        req.setAttribute("mode", "create");

        BreadcrumbUtils.builder(req)
            .dashboard()
            .add("Quản Lý Voucher", req.getContextPath() + "/owner/vouchers/dashboard")
            .add("Danh Sách Voucher", req.getContextPath() + "/owner/vouchers/list")
            .active("Tạo Voucher")
            .build();
        req.getRequestDispatcher("/jsp/owner/voucher/owner-voucher-form.jsp")
           .forward(req, resp);
    }

    /** Render edit form – pre-fill with existing voucher data */
    private void showEditForm(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException, BusinessException {
        int id = parseId(req.getParameter("id"));
        Voucher voucher = voucherService.getVoucherEntity(id);
        List<Integer> linkedFacilityIds = voucherService.getVoucherDetail(id).getFacilityIds();
        List<Facility> facilities = voucherService.getAllFacilities();

        req.setAttribute("voucher", voucher);
        req.setAttribute("linkedFacilityIds", linkedFacilityIds);
        req.setAttribute("facilities", facilities);
        req.setAttribute("mode", "edit");

        BreadcrumbUtils.builder(req)
            .dashboard()
            .add("Quản Lý Voucher", req.getContextPath() + "/owner/vouchers/dashboard")
            .add("Danh Sách Voucher", req.getContextPath() + "/owner/vouchers/list")
            .active("Sửa Voucher")
            .build();
        req.getRequestDispatcher("/jsp/owner/voucher/owner-voucher-form.jsp")
           .forward(req, resp);
    }

    /** Render detail page – usage history loaded via AJAX */
    private void showDetail(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException, BusinessException {
        int id = parseId(req.getParameter("id"));
        req.setAttribute("voucher", voucherService.getVoucherDetail(id));

        BreadcrumbUtils.builder(req)
            .dashboard()
            .add("Quản Lý Voucher", req.getContextPath() + "/owner/vouchers/dashboard")
            .add("Danh Sách Voucher", req.getContextPath() + "/owner/vouchers/list")
            .active("Chi Tiết Voucher")
            .build();
        req.getRequestDispatcher("/jsp/owner/voucher/owner-voucher-detail.jsp")
           .forward(req, resp);
    }

    // =====================================================================
    // FORM HANDLERS
    // =====================================================================

    /** Handle POST /owner/vouchers/create */
    private void handleCreate(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException, BusinessException {
        Voucher voucher = bindVoucherFromRequest(req);
        List<Integer> facilityIds = parseFacilityIds(req);
        int newId = voucherService.createVoucher(voucher, facilityIds);
        resp.sendRedirect(req.getContextPath() + "/owner/vouchers/detail?id=" + newId + "&success=created");
    }

    /** Handle POST /owner/vouchers/update */
    private void handleUpdate(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException, BusinessException {
        Voucher voucher = bindVoucherFromRequest(req);
        String idStr = req.getParameter("voucherId");
        voucher.setVoucherId(Integer.parseInt(idStr));
        List<Integer> facilityIds = parseFacilityIds(req);
        voucherService.updateVoucher(voucher, facilityIds);
        resp.sendRedirect(req.getContextPath() + "/owner/vouchers/detail?id=" + voucher.getVoucherId() + "&success=updated");
    }

    /** Handle GET /owner/vouchers/delete?id=... */
    private void handleDelete(HttpServletRequest req, HttpServletResponse resp)
            throws IOException, BusinessException {
        int id = parseId(req.getParameter("id"));
        voucherService.deleteVoucher(id);
        resp.sendRedirect(req.getContextPath() + "/owner/vouchers/list?success=deleted");
    }

    // =====================================================================
    // HELPERS
    // =====================================================================

    /**
     * Bind form parameters to a Voucher entity.
     */
    private Voucher bindVoucherFromRequest(HttpServletRequest req) {
        Voucher v = new Voucher();
        v.setCode(req.getParameter("code") != null ? req.getParameter("code").trim() : "");
        v.setName(req.getParameter("name") != null ? req.getParameter("name").trim() : "");
        v.setDescription(req.getParameter("description"));
        v.setDiscountType(req.getParameter("discountType"));
        v.setApplicableBookingType(req.getParameter("applicableBookingType") != null
            ? req.getParameter("applicableBookingType") : "SINGLE");

        String dvStr = req.getParameter("discountValue");
        if (dvStr != null && !dvStr.isBlank()) {
            v.setDiscountValue(new BigDecimal(dvStr));
        }
        String minStr = req.getParameter("minOrderAmount");
        if (minStr != null && !minStr.isBlank()) {
            v.setMinOrderAmount(new BigDecimal(minStr));
        } else {
            v.setMinOrderAmount(BigDecimal.ZERO);
        }
        String maxStr = req.getParameter("maxDiscountAmount");
        if (maxStr != null && !maxStr.isBlank()) {
            v.setMaxDiscountAmount(new BigDecimal(maxStr));
        }

        // Parse datetime-local inputs
        v.setValidFrom(parseDateTime(req.getParameter("validFrom")));
        v.setValidTo(parseDateTime(req.getParameter("validTo")));

        String ulStr = req.getParameter("usageLimit");
        if (ulStr != null && !ulStr.isBlank()) {
            v.setUsageLimit(Integer.parseInt(ulStr));
        }
        String pulStr = req.getParameter("perUserLimit");
        if (pulStr != null && !pulStr.isBlank()) {
            v.setPerUserLimit(Integer.parseInt(pulStr));
        } else {
            v.setPerUserLimit(1);
        }

        String activeStr = req.getParameter("isActive");
        v.setIsActive(activeStr != null && !"false".equals(activeStr));
        return v;
    }

    /**
     * Parse facility IDs from form. Returns empty list if "all" is selected.
     * MultiSelect.js submits hidden inputs with name "facilityIds[]" (with brackets).
     */
    private List<Integer> parseFacilityIds(HttpServletRequest req) {
        String facilityScope = req.getParameter("facilityScope");
        if ("specific".equals(facilityScope)) {
            // MultiSelect.js renders: <input type="hidden" name="facilityIds[]" value="...">
            String[] ids = req.getParameterValues("facilityIds[]");
            if (ids == null) {
                // fallback: plain <select multiple> without MultiSelect widget
                ids = req.getParameterValues("facilityIds");
            }
            if (ids != null) {
                return Arrays.stream(ids)
                    .filter(s -> s != null && !s.isBlank())
                    .map(Integer::parseInt)
                    .collect(Collectors.toList());
            }
        }
        return new ArrayList<>();
    }

    private LocalDateTime parseDateTime(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            return LocalDateTime.parse(value, DT_FORMATTER);
        } catch (DateTimeParseException e) {
            try {
                return LocalDateTime.parse(value, DT_FORMATTER_ALT);
            } catch (DateTimeParseException ex) {
                return null;
            }
        }
    }

    private int parseId(String value) throws BusinessException {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new BusinessException("INVALID_ID", "ID không hợp lệ.");
        }
    }
}
