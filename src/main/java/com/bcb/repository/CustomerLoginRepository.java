package com.bcb.repository;

import com.bcb.model.Customer;

public interface CustomerLoginRepository {
    public Customer getCustomerByEmailAndPass(String email);
}
