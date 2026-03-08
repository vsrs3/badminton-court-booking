package com.bcb.repository.staff;

import com.bcb.dto.staff.StaffCustomerSearchItemDto;

import java.util.List;

public interface StaffCustomerSearchRepository {
    List<StaffCustomerSearchItemDto> searchActiveCustomers(String keyword, int limit) throws Exception;
}
