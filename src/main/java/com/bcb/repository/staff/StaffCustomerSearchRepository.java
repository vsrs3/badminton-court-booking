package com.bcb.repository.staff;

import com.bcb.dto.staff.StaffCustomerSearchItemDTO;

import java.util.List;

public interface StaffCustomerSearchRepository {
    List<StaffCustomerSearchItemDTO> searchActiveCustomers(String keyword, int limit) throws Exception;
}

