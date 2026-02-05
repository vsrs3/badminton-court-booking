package com.bcb.dto.response;

import com.bcb.model.Account;

public class AccountResponse {
    private final boolean success;
    private final String message;
    private final int generatedId;

    private Account account;

    //constructor
    public AccountResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
        this.generatedId = -1;
    }

    public AccountResponse(boolean success, String message, int generatedId) {
        this.success = success;
        this.message = message;
        this.generatedId = generatedId;
    }

    //get, set
    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public int getGeneratedId() {
        return generatedId;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }
}
