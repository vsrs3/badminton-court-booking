package com.bcb.controller.staff;

import com.bcb.service.impl.StaffRentalScheduleServiceImpl;
import com.bcb.service.staff.StaffRentalScheduleService;
import com.bcb.utils.staff.StaffAuthUtil;
import com.bcb.utils.staff.StaffAuthUtil.AuthResult;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet(name = "StaffRentalScheduleApiServlet", urlPatterns = {
        "/api/staff/rental/schedule/inventory",
        "/api/staff/rental/schedule/save"
})
public class StaffRentalScheduleApiServlet extends BaseStaffApiServlet {

    private final StaffRentalScheduleService service = new StaffRentalScheduleServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");

        AuthResult auth = StaffAuthUtil.validateStaff(request, response);
        if (!auth.valid) return;

        String bookingDate = request.getParameter("bookingDate");
        String keyword = request.getParameter("q");
        String priceSort = normalizePriceSort(request.getParameter("sort"));
        int courtId = parseInt(request.getParameter("courtId"), 0);
        int slotId = parseInt(request.getParameter("slotId"), 0);
        int page = parseInt(request.getParameter("page"), 1);
        int pageSize = 5;

        try {
            writeJson(response, service.getSlotInventoryJson(
                    auth.facilityId, bookingDate, courtId, slotId, keyword, priceSort, page, pageSize));
        } catch (IllegalArgumentException e) {
            writeError(response, 400, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            writeError(response, 500, "Lỗi tải danh sách đồ thuê");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        request.setCharacterEncoding("UTF-8");

        AuthResult auth = StaffAuthUtil.validateStaff(request, response);
        if (!auth.valid) return;

        try {
            writeJson(response, service.saveSlotRentalSchedule(readRequestBody(request), auth.facilityId));
        } catch (IllegalArgumentException e) {
            writeError(response, 400, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            writeError(response, 500, "Lỗi lưu lịch đồ thuê");
        }
    }

    private int parseInt(String value, int defaultValue) {
        try {
            return Integer.parseInt(value);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    private String normalizePriceSort(String value) {
        if ("price_desc".equals(value) || "price_asc".equals(value)) {
            return value;
        }
        return "default";
    }
}
