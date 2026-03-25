package com.bcb.controller.api;

import com.bcb.dto.owner.OwnerRentalDetailsDTO;
import com.bcb.model.Account;
import com.bcb.service.owner.OwnerRentalReportService;
import com.bcb.service.owner.impl.OwnerRentalReportServiceImpl;
import com.bcb.utils.api.JsonResponseUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

@WebServlet(name = "OwnerRentalReportApiController", urlPatterns = {"/api/owner/rental-report/*"})
public class OwnerRentalReportApiController extends HttpServlet {

    private final OwnerRentalReportService service = new OwnerRentalReportServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        prepareResponse(response);
        if (!ensureOwner(request, response)) {
            return;
        }

        String pathInfo = normalizePath(request.getPathInfo());

        try {
            if ("/facilities".equals(pathInfo)) {
                writeJson(response, JsonResponseUtil.success(
                        "Tải danh sách địa điểm thành công",
                        service.getFacilityOptions(request.getParameter("q"))
                ));
                return;
            }

            if ("/details".equals(pathInfo)) {
                OwnerRentalDetailsDTO details = service.getDetails(
                        parseNullableInt(request.getParameter("facilityId")),
                        parseNullableInt(request.getParameter("year")),
                        parseNullableInt(request.getParameter("month")),
                        parseNullableInt(request.getParameter("day")),
                        request.getParameter("slotTime"),
                        request.getParameter("scope")
                );
                writeJson(response, JsonResponseUtil.success("Tải chi tiết doanh thu thành công", details));
                return;
            }

            if ("/summary".equals(pathInfo) || "/".equals(pathInfo)) {
                writeJson(response, JsonResponseUtil.success(
                        "Tải báo cáo doanh thu thuê đồ thành công",
                        service.getSummary(
                                parseNullableInt(request.getParameter("facilityId")),
                                parseNullableInt(request.getParameter("year")),
                                parseNullableInt(request.getParameter("month")),
                                parseNullableInt(request.getParameter("day")),
                                parseNullableInt(request.getParameter("inactiveMonth")),
                                request.getParameter("detailScope")
                        )
                ));
                return;
            }

            writeJson(response, HttpServletResponse.SC_NOT_FOUND, JsonResponseUtil.error("Không tìm thấy endpoint"));
        } catch (IllegalArgumentException exception) {
            writeJson(response, HttpServletResponse.SC_BAD_REQUEST, JsonResponseUtil.error(exception.getMessage()));
        } catch (Exception exception) {
            exception.printStackTrace();
            writeJson(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    JsonResponseUtil.error("Không thể tải báo cáo thuê đồ"));
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        prepareResponse(response);
        request.setCharacterEncoding("UTF-8");
        if (!ensureOwner(request, response)) {
            return;
        }

        String pathInfo = normalizePath(request.getPathInfo());

        try {
            if ("/inactive".equals(pathInfo)) {
                writeJson(response, JsonResponseUtil.success(
                        "Ngừng hoạt động các đồ không thuê thành công",
                        service.deactivateInactiveItems(
                                parseNullableInt(request.getParameter("facilityId")),
                                parseNullableInt(request.getParameter("year")),
                                parseNullableInt(request.getParameter("month"))
                        )
                ));
                return;
            }

            writeJson(response, HttpServletResponse.SC_NOT_FOUND, JsonResponseUtil.error("Không tìm thấy endpoint"));
        } catch (IllegalArgumentException exception) {
            writeJson(response, HttpServletResponse.SC_BAD_REQUEST, JsonResponseUtil.error(exception.getMessage()));
        } catch (Exception exception) {
            exception.printStackTrace();
            writeJson(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    JsonResponseUtil.error("Không thể xử lý yêu cầu báo cáo thuê đồ"));
        }
    }

    private void prepareResponse(HttpServletResponse response) {
        response.setContentType("application/json;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
    }

    private boolean ensureOwner(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        if (session == null) {
            writeJson(response, HttpServletResponse.SC_UNAUTHORIZED, JsonResponseUtil.error("Phiên đăng nhập đã hết."));
            return false;
        }

        Object accountObject = session.getAttribute("account");
        if (!(accountObject instanceof Account account)) {
            writeJson(response, HttpServletResponse.SC_UNAUTHORIZED, JsonResponseUtil.error("Bạn chưa đăng nhập."));
            return false;
        }

        if (!"OWNER".equals(account.getRole())) {
            writeJson(response, HttpServletResponse.SC_FORBIDDEN,
                    JsonResponseUtil.error("Bạn không có quyền truy cập báo cáo này."));
            return false;
        }

        return true;
    }

    private String normalizePath(String pathInfo) {
        if (pathInfo == null || pathInfo.isBlank()) {
            return "/";
        }
        return pathInfo;
    }

    private Integer parseNullableInt(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Tham số số nguyên không hợp lệ: " + value);
        }
    }

    private void writeJson(HttpServletResponse response, String json) throws IOException {
        response.getWriter().print(json);
    }

    private void writeJson(HttpServletResponse response, int status, String json) throws IOException {
        response.setStatus(status);
        response.getWriter().print(json);
    }
}
