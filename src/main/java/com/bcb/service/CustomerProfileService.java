package com.bcb.service;

import com.bcb.dto.CustomerChangePassDTO;
import com.bcb.dto.CustomerProfileDTO;
import com.bcb.dto.response.CustomerResponse;
import jakarta.servlet.http.HttpServletRequest;

public interface CustomerProfileService {
    public CustomerResponse updateInfo (HttpServletRequest request, CustomerProfileDTO dto, int cusId);

    public CustomerResponse updatePassword (CustomerChangePassDTO dto, int cusId);
}
