package com.bcb.service;

import com.bcb.dto.CustomerLoginDTO;
import com.bcb.dto.CustomerRegisterDTO;
import com.bcb.dto.response.AccountResponse;

public interface CustomerAuthService {
    AccountResponse login(CustomerLoginDTO dto);

    AccountResponse deleteAccount (Integer customerId);

    AccountResponse registerCustomer(CustomerRegisterDTO dto);
}
