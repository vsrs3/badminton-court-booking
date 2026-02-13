package com.bcb.service;

import com.bcb.dto.CustomerChangePassDTO;
import com.bcb.dto.CustomerProfileDTO;
import com.bcb.dto.response.AccountResponse;
import jakarta.servlet.http.HttpServletRequest;

public interface CustomerProfileService {
    public AccountResponse updateInfo (HttpServletRequest request, CustomerProfileDTO dto, Integer cusId);

    public AccountResponse updatePassword (CustomerChangePassDTO dto, Integer cusId);
}
