package com.bcb.service.impl;

import com.bcb.dto.staff.StaffCustomerSearchItemDTO;
import com.bcb.repository.impl.StaffCustomerSearchRepositoryImpl;
import com.bcb.repository.staff.StaffCustomerSearchRepository;
import com.bcb.service.staff.StaffCustomerSearchService;

import java.util.List;

public class StaffCustomerSearchServiceImpl implements StaffCustomerSearchService {

    private static final int DEFAULT_LIMIT = 10;
    private final StaffCustomerSearchRepository repository = new StaffCustomerSearchRepositoryImpl();

    @Override
    public List<StaffCustomerSearchItemDTO> searchCustomers(String keyword) throws Exception {
        return repository.searchActiveCustomers(keyword, DEFAULT_LIMIT);
    }
}

