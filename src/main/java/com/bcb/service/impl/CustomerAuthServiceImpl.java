package com.bcb.service.impl;

import com.bcb.dto.CustomerLoginDTO;
import com.bcb.repository.impl.CustomerAuthRepositoryImpl;
import com.bcb.service.CustomerAuthService;
import com.bcb.dto.response.CustomerResponse;
import com.bcb.model.Customer;
import com.bcb.repository.*;
import com.bcb.utils.DBContext;

import java.sql.Connection;

public class CustomerAuthServiceImpl implements CustomerAuthService {
    private final CustomerAuthRepository repo;

    public CustomerAuthServiceImpl() {
        this.repo = new CustomerAuthRepositoryImpl();
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

    @Override
    public CustomerResponse deleteAccount(int customerId) {

        Connection connect = DBContext.getConnection();
        try {
            connect.setAutoCommit(false);
            boolean isDelete = repo.deleteCustomerById(customerId);
            if(!isDelete){
                connect.rollback();
                return new CustomerResponse(false, "Delete account failed!", 1000);
            }
            connect.commit();
            return new CustomerResponse(true, "Delete account successfully!", 1002);

        } catch (Exception e) {
            return new CustomerResponse(false, e.getMessage(), 1004);
        }

    }
}
