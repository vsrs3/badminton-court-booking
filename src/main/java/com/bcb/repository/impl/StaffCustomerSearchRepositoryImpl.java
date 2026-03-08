package com.bcb.repository.impl;

import com.bcb.dto.staff.StaffCustomerSearchItemDto;
import com.bcb.repository.staff.StaffCustomerSearchRepository;
import com.bcb.utils.DBContext;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class StaffCustomerSearchRepositoryImpl implements StaffCustomerSearchRepository {

    @Override
    public List<StaffCustomerSearchItemDto> searchActiveCustomers(String keyword, int limit) throws Exception {
        String sql = "SELECT TOP " + limit + " account_id, full_name, phone, email " +
                "FROM Account " +
                "WHERE role = 'CUSTOMER' AND is_active = 1 " +
                "AND (phone LIKE ? OR email LIKE ?) " +
                "ORDER BY full_name";

        String pattern = "%" + keyword + "%";
        List<StaffCustomerSearchItemDto> customers = new ArrayList<>();

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, pattern);
            ps.setString(2, pattern);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    StaffCustomerSearchItemDto item = new StaffCustomerSearchItemDto();
                    item.setAccountId(rs.getInt("account_id"));
                    item.setFullName(rs.getString("full_name"));
                    item.setPhone(rs.getString("phone"));
                    item.setEmail(rs.getString("email"));
                    customers.add(item);
                }
            }
        }

        return customers;
    }
}
