package com.bcb.service.impl;

import com.bcb.dto.CustomerLoginDTO;
import com.bcb.repository.impl.CustomerLoginRepositoryImpl;
import com.bcb.service.CustomerLoginService;
import com.bcb.dto.response.CustomerResponse;
import com.bcb.model.Customer;
import com.bcb.repository.*;

public class CustomerLoginServiceImpl implements CustomerLoginService {
    private final CustomerLoginRepository repo;

    public CustomerLoginServiceImpl() {
        this.repo = new CustomerLoginRepositoryImpl();
    }

    @Override
    public CustomerResponse login(CustomerLoginDTO dto) {
        try {
            if(dto.getEmail() == null || dto.getEmail().isEmpty()) {
                return new CustomerResponse(false, "Email must require", 1000);
            }

            Customer customer = repo.getCustomerByEmailAndPass(dto.getEmail());
            if(customer == null){
                return new CustomerResponse(false, "Can not log in", 1000);
            }

            CustomerResponse result = new CustomerResponse(true, "Login successful", 1002);
            result.setCustomer(customer);
            return result;

        } catch (Exception e) {
            e.printStackTrace();
            return new CustomerResponse(false, e.getMessage(), 1004);
        }
    }
}
