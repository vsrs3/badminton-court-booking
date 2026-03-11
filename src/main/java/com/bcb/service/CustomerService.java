package com.bcb.service;

import java.util.List;

public interface CustomerService {

    int getTotalCustomers();

    List<Object[]> getLatestCustomers();

}