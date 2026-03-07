package com.bcb.controller.api;

import com.bcb.dto.voucher.VoucherDashboardDTO;
import com.bcb.dto.voucher.VoucherFilterDTO;
import com.bcb.exception.BusinessException;
import com.bcb.service.voucher.VoucherService;
import com.bcb.service.voucher.impl.VoucherServiceImpl;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * AJAX API Controller for Owner Voucher endpoints.
 * Returns JSON data for dynamic rendering.
 *
 * URL patterns:
 *   GET /api/owner/vouchers             → Paginated voucher list
 *   GET /api/owner/vouchers/dashboard   → Dashboard statistics
 *   GET /api/owner/vouchers/{id}/usage  → Voucher usage history
 *   DELETE /api/owner/vouchers/{id}     → Soft delete voucher
 *
 * @author AnhTN
 */
@WebServlet("/api/owner/vouchers/*")
public class OwnerVoucherApiController extends HttpServlet {

    private VoucherService voucherService;
    private Gson gson;

    @Override
    public void init() throws ServletException {
        voucherService = new VoucherServiceImpl();
        gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new TypeAdapter<LocalDateTime>() {
                private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                @Override public void write(JsonWriter out, LocalDateTime value) throws IOException {
                    out.value(value != null ? value.format(fmt) : null);
                }
                @Override public LocalDateTime read(JsonReader in) throws IOException {
                    String s = in.nextString();
                    return s == null ? null : LocalDateTime.parse(s, fmt);
                }
            })
            .serializeNulls()
            .create();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setContentType("application/json;charset=UTF-8");
        String path = req.getPathInfo();

        try {
            if (path == null || path.equals("/")) {
                // GET /api/owner/vouchers
                handleListVouchers(req, resp);
            } else if ("/dashboard".equals(path)) {
                // GET /api/owner/vouchers/dashboard
                handleDashboard(req, resp);
            } else if (path.matches("/\\d+/usage")) {
                // GET /api/owner/vouchers/{id}/usage
                int voucherId = Integer.parseInt(path.split("/")[1]);
                handleUsageHistory(req, resp, voucherId);
            } else {
                sendError(resp, 404, "Endpoint không tồn tại.");
            }
        } catch (BusinessException e) {
            sendError(resp, 400, e.getMessage());
        } catch (Exception e) {
            sendError(resp, 500, "Lỗi server: " + e.getMessage());
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setContentType("application/json;charset=UTF-8");
        String path = req.getPathInfo();
        try {
            if (path != null && path.matches("/\\d+")) {
                int id = Integer.parseInt(path.substring(1));
                voucherService.deleteVoucher(id);
                resp.getWriter().write(gson.toJson(Map.of("success", true, "message", "Đã xóa voucher.")));
            } else {
                sendError(resp, 400, "ID không hợp lệ.");
            }
        } catch (BusinessException e) {
            sendError(resp, 400, e.getMessage());
        }
    }

    // =====================================================================
    // HANDLERS
    // =====================================================================

    /**
     * Return paginated voucher list with filter applied.
     * Query params: keyword, status, discountType, facilityId,
     *               dateFrom, dateTo, sortBy, sortDir, page, pageSize
     */
    private void handleListVouchers(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        VoucherFilterDTO filter = buildFilter(req);
        Map<String, Object> result = voucherService.getVoucherList(filter);
        resp.getWriter().write(gson.toJson(result));
    }

    /**
     * Return dashboard statistics including chart data.
     */
    private void handleDashboard(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        VoucherDashboardDTO stats = voucherService.getDashboardStats();
        resp.getWriter().write(gson.toJson(stats));
    }

    /**
     * Return paginated usage history for a specific voucher.
     * Query params: page, pageSize
     */
    private void handleUsageHistory(HttpServletRequest req, HttpServletResponse resp, int voucherId)
            throws IOException, BusinessException {
        int page     = getIntParam(req, "page", 1);
        int pageSize = getIntParam(req, "pageSize", 10);
        Map<String, Object> result = voucherService.getUsageHistory(voucherId, page, pageSize);
        resp.getWriter().write(gson.toJson(result));
    }

    // =====================================================================
    // HELPERS
    // =====================================================================

    private VoucherFilterDTO buildFilter(HttpServletRequest req) {
        VoucherFilterDTO f = new VoucherFilterDTO();
        f.setKeyword(req.getParameter("keyword"));
        f.setStatus(req.getParameter("status"));
        f.setDiscountType(req.getParameter("discountType"));
        f.setDateFrom(req.getParameter("dateFrom"));
        f.setDateTo(req.getParameter("dateTo"));
        f.setSortBy(req.getParameter("sortBy"));
        f.setSortDir(req.getParameter("sortDir"));
        f.setPage(getIntParam(req, "page", 1));
        f.setPageSize(getIntParam(req, "pageSize", 10));
        String fid = req.getParameter("facilityId");
        if (fid != null && !fid.isBlank()) {
            try { f.setFacilityId(Integer.parseInt(fid)); } catch (NumberFormatException ignored) {}
        }
        return f;
    }

    private void sendError(HttpServletResponse resp, int code, String message) throws IOException {
        resp.setStatus(code);
        resp.getWriter().write(gson.toJson(Map.of("error", message)));
    }

    private int getIntParam(HttpServletRequest req, String name, int defaultVal) {
        try {
            String v = req.getParameter(name);
            return v != null ? Integer.parseInt(v) : defaultVal;
        } catch (NumberFormatException e) {
            return defaultVal;
        }
    }
}
