package com.bcb.service.staff;

import com.bcb.dto.staff.StaffCustomerSearchItemDTO;

import java.util.List;

public interface StaffCustomerSearchService {
    List<StaffCustomerSearchItemDTO> searchCustomers(String keyword) throws Exception;
}

