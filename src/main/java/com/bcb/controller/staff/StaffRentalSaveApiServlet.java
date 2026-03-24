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

@WebServlet(name = "StaffRentalSaveApiServlet", urlPatterns = {
        "/api/staff/rental/save",
        "/api/staff/rental/update",
        "/api/staff/rental/delete"
})
public class StaffRentalSaveApiServlet extends BaseStaffApiServlet {

    private final StaffRentalService service = new StaffRentalServiceImpl();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json;charset=UTF-8");

        AuthResult auth = StaffAuthUtil.validateStaff(request, response);
        if (!auth.valid) return;

        Integer staffId = (Integer) request.getSession().getAttribute("staffId");
        if (staffId == null) {
            writeError(response, 403, "Không xác định được staff");
            return;
        }

        String body = readRequestBody(request);
        String path = request.getServletPath();

        try {
            if (path.endsWith("/save")) {
                writeJson(response, service.saveRental(body, auth.facilityId, staffId));
            } else if (path.endsWith("/update")) {
                writeJson(response, service.updateRental(body, auth.facilityId, staffId));
            } else {
                writeJson(response, service.deleteRental(body, auth.facilityId, staffId));
            }
        } catch (Exception e) {
            e.printStackTrace();
            writeError(response, 500, "Lỗi xử lý thuê đồ");
        }
    }
}
