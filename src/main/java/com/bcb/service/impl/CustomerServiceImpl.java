package com.bcb.service.impl;

import com.bcb.repository.CustomerRepository;
import com.bcb.repository.impl.CustomerRepositoryImpl;
import com.bcb.service.CustomerService;

import java.util.List;

public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository repo = new CustomerRepositoryImpl();

    @Override
    public int getTotalCustomers() {
        return repo.countCustomers();
    }

    @Override
    public List<Object[]> getLatestCustomers() {
        return repo.getLatestCustomers();
    }
}