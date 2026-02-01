package com.bcb.service.impl;

import com.bcb.dto.CustomerChangePassDTO;
import com.bcb.dto.CustomerProfileDTO;
import com.bcb.repository.CustomerProfileRepository;
import com.bcb.repository.impl.CustomerProfileRepositoryImpl;
import com.bcb.service.CustomerProfileService;
import com.bcb.dto.response.CustomerResponse;
import com.bcb.model.Customer;
import com.bcb.utils.DBContext;
import com.bcb.utils.DBUpload;
import jakarta.servlet.http.HttpServletRequest;

import java.sql.Connection;

public class CustomerProfileServiceImpl implements CustomerProfileService {

    private CustomerProfileRepository repo;

    public CustomerProfileServiceImpl() {
        this.repo = new CustomerProfileRepositoryImpl();
    }

    @Override
    public CustomerResponse updateInfo (HttpServletRequest request, CustomerProfileDTO dto, int accountId) {

        try (Connection connect = DBContext.getConnection()) {
            connect.setAutoCommit(false);

            if (dto.getFullName() == null || dto.getFullName().isEmpty()
                    || dto.getEmail() == null || dto.getEmail().isEmpty()
                    || dto.getPhone() == null || dto.getPhone().isEmpty()) {

                return new CustomerResponse(false, "Missing info", 1000);
            }

            String avatarPath = DBUpload.getAvatarPath(request, dto);

            boolean isUpdateInfo = repo.updateAccountInfo(avatarPath,
                    dto.getFullName(), dto.getEmail(), dto.getPhone(), accountId);

            if (!isUpdateInfo) {
                connect.rollback();
                return new CustomerResponse(false, "Update failed! Check repository.", 1000);
            }
            connect.commit();

            CustomerResponse result = new CustomerResponse(true, "Update successull", 1002);
            Customer customer = repo.getCustomerById(accountId);
            result.setCustomer(customer);
            return result;

        } catch (Exception e) {
            e.printStackTrace();
            return new CustomerResponse(false, e.getMessage(), 1004);
        }

    }

    @Override
    public CustomerResponse updatePassword(CustomerChangePassDTO dto, int accountId) {

        try(Connection connect = DBContext.getConnection()) {
            connect.setAutoCommit(false);

            if(dto.getNewPass() == null || dto.getNewPass().isEmpty()
                    || dto.getOldPass() == null || dto.getOldPass().isEmpty()
                    || dto.getConfirmNewPass() == null || dto.getConfirmNewPass().isEmpty()) {

                return new CustomerResponse(false, "Missing info", 1000);
            }

            if(dto.getNewPass().length() < 6 || dto.getConfirmNewPass().length() < 6)
                return new CustomerResponse(false, "Password length must larger than 6", 1000);

            if(!dto.getConfirmNewPass().equals(dto.getNewPass()))
                return new CustomerResponse(false, "Password confirmation does not match", 1000);

            boolean isUpdate = repo.updatePassword(dto.getOldPass(), dto.getNewPass(), accountId);
            if(!isUpdate){
                connect.rollback();
                return new CustomerResponse(false, "Change password failed", 1000);
            }

            connect.commit();

            CustomerResponse result = new CustomerResponse(true, "Update successull", 1002);
            Customer customer = repo.getCustomerById(accountId);
            result.setCustomer(customer);
            return result;

        } catch (Exception e) {
            e.printStackTrace();
            return new CustomerResponse(false, e.getMessage(), 1004);
        }
    }
}
