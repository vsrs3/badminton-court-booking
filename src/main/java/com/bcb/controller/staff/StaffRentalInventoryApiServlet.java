package com.bcb.controller.staff;

import com.bcb.service.impl.StaffRentalServiceImpl;
import com.bcb.service.staff.StaffRentalService;
import com.bcb.utils.staff.StaffAuthUtil;
import com.bcb.utils.staff.StaffAuthUtil.AuthResult;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet(name = "StaffRentalInventoryApiServlet", urlPatterns = {"/api/staff/rental/inventory"})
public class StaffRentalInventoryApiServlet extends BaseStaffApiServlet {

    private final StaffRentalService service = new StaffRentalServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json;charset=UTF-8");

        AuthResult auth = StaffAuthUtil.validateStaff(request, response);
        if (!auth.valid) return;

        String q = request.getParameter("q");
        int page = parseInt(request.getParameter("page"), 1);
        int pageSize = normalizePageSize(request.getParameter("pageSize"));

        try {
            writeJson(response, service.getInventoryJson(auth.facilityId, q, page, pageSize));
        } catch (Exception e) {
            e.printStackTrace();
            writeError(response, 500, "Lỗi tải danh sách đồ thuê");
        }
    }

    private int parseInt(String val, int def) {
        try {
            return Integer.parseInt(val);
        } catch (Exception e) {
            return def;
        }
    }

    private int normalizePageSize(String rawPageSize) {
        int pageSize = parseInt(rawPageSize, 5);
        if (pageSize < 1) {
            return 5;
        }
        return Math.min(pageSize, 50);
    }
}
