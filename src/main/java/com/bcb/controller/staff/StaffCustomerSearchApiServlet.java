package com.bcb.controller.staff;

import com.bcb.controller.staff.StaffAuthUtil.AuthResult;
import com.bcb.utils.DBContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

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
        String sql = "SELECT TOP 10 account_id, full_name, phone, email " +
                "FROM Account " +
                "WHERE role = 'CUSTOMER' AND is_active = 1 " +
                "AND (phone LIKE ? OR email LIKE ?) " +
                "ORDER BY full_name";

        String pattern = "%" + q + "%";

        StringBuilder json = new StringBuilder(512);
        json.append("{\"success\":true,\"data\":{\"customers\":[");

        boolean first = true;
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, pattern);
            ps.setString(2, pattern);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    if (!first) json.append(",");
                    first = false;
                    json.append("{\"accountId\":").append(rs.getInt("account_id"));
                    json.append(",\"fullName\":").append(StaffAuthUtil.escapeJson(rs.getString("full_name")));
                    json.append(",\"phone\":").append(StaffAuthUtil.escapeJson(rs.getString("phone")));
                    json.append(",\"email\":").append(StaffAuthUtil.escapeJson(rs.getString("email")));
                    json.append("}");
                }
            }
        }

        json.append("]}}");
        return json.toString();
    }
}