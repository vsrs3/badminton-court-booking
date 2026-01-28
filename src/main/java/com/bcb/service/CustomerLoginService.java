package com.bcb.service;

import com.bcb.dto.CustomerLoginDTO;
import com.bcb.dto.response.CustomerResponse;

public interface CustomerLoginService {
    public CustomerResponse login(CustomerLoginDTO dto);
}
