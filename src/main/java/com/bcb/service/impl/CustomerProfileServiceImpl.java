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
import java.util.List;

public class CustomerProfileServiceImpl implements CustomerProfileService {

    private CustomerProfileRepository repo;

    public CustomerProfileServiceImpl() {
        this.repo = new CustomerProfileRepositoryImpl();
    }

    @Override
    public CustomerResponse updateInfo (HttpServletRequest request, CustomerProfileDTO dto, int accountId) {

        try (Connection connect = DBContext.getConnection()) {
            connect.setAutoCommit(false);

            Customer customer = repo.getCustomerById(accountId);
            List<String> listEmail = repo.emailList(customer.getEmail());
            for(String email : listEmail){
                if(dto.getEmail().equals(email)){
                    return new CustomerResponse(false, "Email đã tồn tại!", 1000);
                }
            }

            String avatarPath = DBUpload.getAvatarPath(request, dto);

            boolean isUpdateInfo = repo.updateAccountInfo(avatarPath,
                    dto.getFullName(), dto.getEmail(), dto.getPhone(), accountId);

            if (!isUpdateInfo) {
                connect.rollback();
                return new CustomerResponse(false, "Cập nhật thông tin thất bại!", 1000);
            }
            connect.commit();
            Customer updatedCustomer = repo.getCustomerById(accountId);

            CustomerResponse result = new CustomerResponse(true, "Cập nhật thông tin thành công!", 1002);
            result.setCustomer(updatedCustomer);
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

            Customer customer = repo.getCustomerById(accountId);
            if(!dto.getOldPass().equals(customer.getPassword()))
                return new CustomerResponse(false, "Mật khẩu hiện tại không khớp", 1000);

            boolean isUpdate = repo.updatePassword(dto.getNewPass(), accountId);
            if(!isUpdate){
                connect.rollback();
                return new CustomerResponse(false, "Đổi mật khẩu thất bại!", 1000);
            }

            connect.commit();

            CustomerResponse result = new CustomerResponse(true, "Đổi mật khẩu thành công!", 1002);
            result.setCustomer(customer);
            return result;

        } catch (Exception e) {
            e.printStackTrace();
            return new CustomerResponse(false, e.getMessage(), 1004);
        }
    }
}
