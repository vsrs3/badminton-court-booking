package com.bcb.repository;

import com.bcb.model.Customer;

public interface CustomerProfileRepository {
    public boolean updateAccountInfo (String avatarPath, String fullName, String email, String phone, int accountId);

    public boolean updatePassword (String oldPass, String newPass, int accountId);

    public Customer getCustomerById(int cusId);
}
