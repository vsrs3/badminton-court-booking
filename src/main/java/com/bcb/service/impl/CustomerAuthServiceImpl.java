package com.bcb.service.impl;

import com.bcb.dto.CustomerLoginDTO;
import com.bcb.model.Account;
import com.bcb.repository.impl.AccountAuthRepositoryImpl;
import com.bcb.service.CustomerAuthService;
import com.bcb.dto.response.AccountResponse;
import com.bcb.repository.*;
import com.bcb.utils.DBContext;

import java.sql.Connection;

public class CustomerAuthServiceImpl implements CustomerAuthService {
    private final AccountAuthRepository repo;

    public CustomerAuthServiceImpl() {
        this.repo = new AccountAuthRepositoryImpl();
    }

    @Override
    public AccountResponse login(CustomerLoginDTO dto) {
        try {
            if(dto.getEmail() == null || dto.getEmail().isEmpty()) {
                return new AccountResponse(false, "Email must require", 1000);
            }

            Account account = repo.getAccountByEmailAndPass(dto.getEmail());
            if(account == null){
                return new AccountResponse(false, "Can not log in", 1000);
            }

            AccountResponse result = new AccountResponse(true, "Login successful", 1002);
            result.setAccount(account);
            return result;

        } catch (Exception e) {
            e.printStackTrace();
            return new AccountResponse(false, e.getMessage(), 1004);
        }
    }

    @Override
    public AccountResponse deleteAccount(Integer accountId) {

        Connection connect = DBContext.getConnection();
        try {
            connect.setAutoCommit(false);
            boolean isDelete = repo.deleteAccountById(accountId);
            if(!isDelete){
                connect.rollback();
                return new AccountResponse(false, "Delete account failed!", 1000);
            }
            connect.commit();
            return new AccountResponse(true, "Delete account successfully!", 1002);

        } catch (Exception e) {
            return new AccountResponse(false, e.getMessage(), 1004);
        }

    }
}
