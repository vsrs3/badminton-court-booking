package com.bcb.service.staff;

import com.bcb.dto.staff.StaffCustomerSearchItemDto;

import java.util.List;

public interface StaffCustomerSearchService {
    List<StaffCustomerSearchItemDto> searchCustomers(String keyword) throws Exception;
}

