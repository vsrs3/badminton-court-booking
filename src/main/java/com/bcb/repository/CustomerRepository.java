package com.bcb.repository;

import java.util.List;

public interface CustomerRepository {

    int countCustomers();

    List<Object[]> getLatestCustomers();

}