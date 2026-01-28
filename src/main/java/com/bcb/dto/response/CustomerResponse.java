package com.bcb.dto.response;

import com.bcb.model.Customer;

public class CustomerResponse {
    private final boolean success;
    private final String message;
    private final int generatedId;

    private Customer customer;

    //constructor
    public CustomerResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
        this.generatedId = -1;
    }

    public CustomerResponse(boolean success, String message, int generatedId) {
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

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }
}
