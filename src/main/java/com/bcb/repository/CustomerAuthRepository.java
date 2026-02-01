package com.bcb.repository;

import com.bcb.model.Customer;

public interface CustomerAuthRepository {
    public Customer getCustomerByEmailAndPass(String email);

    public boolean deleteCustomerById (int customerId);
}
