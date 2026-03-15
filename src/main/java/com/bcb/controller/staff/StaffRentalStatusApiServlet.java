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
import java.time.LocalDate;

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
            LocalDate selectedDate = parseDate(request.getParameter("date"));
            writeJson(response, JsonResponseUtil.success(
                    "Tai du lieu thanh cong",
                    service.getRentalStatusData(auth.facilityId, selectedDate)
            ));
        } catch (Exception e) {
            e.printStackTrace();
            writeError(response, 500, "Loi he thong");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        request.setCharacterEncoding("UTF-8");

        AuthResult auth = StaffAuthUtil.validateStaff(request, response);
        if (!auth.valid) return;

        int scheduleId = parseInt(request.getParameter("scheduleId"));
        String status = request.getParameter("status");

        if (scheduleId <= 0) {
            writeError(response, 400, "Thieu scheduleId");
            return;
        }

        if (!"RENTED".equals(status) && !"RENTING".equals(status) && !"RETURNED".equals(status)) {
            writeError(response, 400, "Trang thai khong hop le");
            return;
        }

        try {
            StaffRentalStatusUpdateResultDTO result =
                    service.updateRentalStatus(auth.facilityId, scheduleId, status);
            if (result.getUpdatedCount() <= 0) {
                writeError(response, 404, "Khong tim thay du lieu can cap nhat");
                return;
            }
            writeJson(response, JsonResponseUtil.success("Cap nhat trang thai thanh cong", result));
        } catch (IllegalArgumentException e) {
            writeError(response, 400, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            writeError(response, 500, "Loi he thong");
        }
    }

    private LocalDate parseDate(String rawDate) {
        try {
            if (rawDate == null || rawDate.trim().isEmpty()) {
                return LocalDate.now();
            }
            return LocalDate.parse(rawDate.trim());
        } catch (Exception e) {
            return LocalDate.now();
        }
    }

    private int parseInt(String rawValue) {
        try {
            return Integer.parseInt(rawValue);
        } catch (Exception e) {
            return 0;
        }
    }
}
