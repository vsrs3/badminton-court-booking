package com.bcb.controller.staff;

import com.bcb.utils.staff.StaffAuthUtil;
import com.bcb.utils.staff.StaffAuthUtil.AuthResult;
import com.bcb.dto.staff.StaffCustomerSearchItemDto;
import com.bcb.service.impl.StaffCustomerSearchServiceImpl;
import com.bcb.service.staff.StaffCustomerSearchService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

/**
 * REST API: GET /api/staff/customer/search?q=...
 *
 * Searches for CUSTOMER accounts by phone or email (partial match).
 * Returns max 10 results.
 *
 * FIX: wrap array inside {"data":{"customers":[...]}} to match frontend expectation
 */
@WebServlet(name = "StaffCustomerSearchApiServlet", urlPatterns = {"/api/staff/customer/search"})
public class StaffCustomerSearchApiServlet extends HttpServlet {

    private final StaffCustomerSearchService staffCustomerSearchService = new StaffCustomerSearchServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json;charset=UTF-8");

        AuthResult auth = StaffAuthUtil.validateStaff(request, response);
        if (!auth.valid) return;

        String q = request.getParameter("q");
        if (q == null || q.trim().isEmpty()) {
            response.getWriter().print("{\"success\":true,\"data\":{\"customers\":[]}}");
            return;
        }

        q = q.trim();

        try {
            String json = searchCustomers(q);
            response.getWriter().print(json);
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(500);
            response.getWriter().print("{\"success\":false,\"message\":\"Lỗi hệ thống\"}");
        }
    }

    private String searchCustomers(String q) throws Exception {
        List<StaffCustomerSearchItemDto> customers = staffCustomerSearchService.searchCustomers(q);

        StringBuilder json = new StringBuilder(512);
        json.append("{\"success\":true,\"data\":{\"customers\":[");

        boolean first = true;
        for (StaffCustomerSearchItemDto customer : customers) {
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



