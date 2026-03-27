package com.bcb.controller.staff;

import com.bcb.dto.staff.StaffCustomerSearchItemDTO;
import com.bcb.service.impl.StaffCustomerSearchServiceImpl;
import com.bcb.service.staff.StaffCustomerSearchService;
import com.bcb.utils.staff.StaffAuthUtil;
import com.bcb.utils.staff.StaffAuthUtil.AuthResult;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

@WebServlet(name = "StaffCustomerSearchApiServlet", urlPatterns = {"/api/staff/customer/search"})
public class StaffCustomerSearchApiServlet extends BaseStaffApiServlet {

    private final StaffCustomerSearchService staffCustomerSearchService = new StaffCustomerSearchServiceImpl();

    /**
     * Searches active customers for the proxy booking flow.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json;charset=UTF-8");

        AuthResult auth = StaffAuthUtil.validateStaff(request, response);
        if (!auth.valid) return;

        String q = request.getParameter("q");
        // Empty query returns an empty list to keep the UI responsive.
        if (q == null || q.trim().isEmpty()) {
            writeJson(response, "{\"success\":true,\"data\":{\"customers\":[]}}");
            return;
        }

        q = q.trim();

        try {
            String json = searchCustomers(q);
            writeJson(response, json);
        } catch (Exception e) {
            e.printStackTrace();
            writeError(response, 500, "Lỗi hệ thống");
        }
    }

    private String searchCustomers(String q) throws Exception {
        List<StaffCustomerSearchItemDTO> customers = staffCustomerSearchService.searchCustomers(q);

        StringBuilder json = new StringBuilder(512);
        json.append("{\"success\":true,\"data\":{\"customers\":[");

        boolean first = true;
        for (StaffCustomerSearchItemDTO customer : customers) {
            if (!first) json.append(",");
            first = false;
            json.append("{\"accountId\":").append(customer.getAccountId());
            json.append(",\"fullName\":").append(StaffAuthUtil.escapeJson(customer.getFullName()));
            json.append(",\"phone\":").append(StaffAuthUtil.escapeJson(customer.getPhone()));
            json.append(",\"email\":").append(StaffAuthUtil.escapeJson(customer.getEmail()));
            json.append("}");
        }

        json.append("]}}");
        return json.toString();
    }
}
