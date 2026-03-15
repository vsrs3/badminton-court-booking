package com.bcb.controller.staff;

import com.bcb.dto.staff.StaffRentalStatusUpdateResultDTO;
import com.bcb.service.impl.StaffRentalStatusServiceImpl;
import com.bcb.service.staff.StaffRentalStatusService;
import com.bcb.utils.api.JsonResponseUtil;
import com.bcb.utils.staff.StaffAuthUtil;
import com.bcb.utils.staff.StaffAuthUtil.AuthResult;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@WebServlet(name = "StaffRentalStatusApiServlet", urlPatterns = {"/api/staff/rental/status"})
public class StaffRentalStatusApiServlet extends BaseStaffApiServlet {

    private final StaffRentalStatusService service = new StaffRentalStatusServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");

        AuthResult auth = StaffAuthUtil.validateStaff(request, response);
        if (!auth.valid) return;

        try {
            writeJson(response, JsonResponseUtil.success("Tải dữ liệu thành công",
                    service.getRentalStatusData(auth.facilityId)));
        } catch (Exception e) {
            e.printStackTrace();
            writeError(response, 500, "Lỗi hệ thống");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        request.setCharacterEncoding("UTF-8");

        AuthResult auth = StaffAuthUtil.validateStaff(request, response);
        if (!auth.valid) return;

        List<Integer> rentalIds = parseRentalIds(request.getParameter("rentalIds"));
        String status = request.getParameter("status");

        if (rentalIds.isEmpty()) {
            writeError(response, 400, "Thiếu rentalIds");
            return;
        }

        if (!"RENTED".equals(status) && !"RETURNED".equals(status)) {
            writeError(response, 400, "Trạng thái không hợp lệ");
            return;
        }

        try {
            StaffRentalStatusUpdateResultDTO result =
                    service.updateRentalStatus(auth.facilityId, rentalIds, "RETURNED".equals(status));
            if (result.getUpdatedCount() <= 0) {
                writeError(response, 404, "Không tìm thấy dữ liệu cần cập nhật");
                return;
            }
            writeJson(response, JsonResponseUtil.success("Cập nhật trạng thái thành công", result));
        } catch (Exception e) {
            e.printStackTrace();
            writeError(response, 500, "Lỗi hệ thống");
        }
    }

    private List<Integer> parseRentalIds(String csv) {
        List<Integer> ids = new ArrayList<>();
        if (csv == null || csv.trim().isEmpty()) {
            return ids;
        }

        String[] parts = csv.split(",");
        for (String part : parts) {
            String value = part.trim();
            if (!value.matches("\\d+")) {
                continue;
            }
            ids.add(Integer.parseInt(value));
        }
        return ids;
    }
}
