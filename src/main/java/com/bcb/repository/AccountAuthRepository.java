package com.bcb.repository;

import com.bcb.model.Account;

public interface AccountAuthRepository {
        Account getAccountByEmailAndPass(String email);

        boolean deleteAccountById (Integer accountId);
}
