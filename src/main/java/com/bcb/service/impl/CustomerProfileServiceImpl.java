package com.bcb.service.impl;

import com.bcb.dto.CustomerChangePassDTO;
import com.bcb.dto.CustomerProfileDTO;
import com.bcb.model.Account;
import com.bcb.repository.CustomerProfileRepository;
import com.bcb.repository.impl.CustomerProfileRepositoryImpl;
import com.bcb.service.CustomerProfileService;
import com.bcb.dto.response.AccountResponse;
import com.bcb.utils.DBContext;
import com.bcb.utils.DBUpload;
import jakarta.servlet.http.HttpServletRequest;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.util.List;

public class CustomerProfileServiceImpl implements CustomerProfileService {

    private CustomerProfileRepository repo;

    public CustomerProfileServiceImpl() {
        this.repo = new CustomerProfileRepositoryImpl();
    }

    @Override
    public AccountResponse updateInfo (HttpServletRequest request, CustomerProfileDTO dto, Integer accountId) {

        try (Connection connect = DBContext.getConnection()) {
            connect.setAutoCommit(false);

            Account account = repo.getCustomerById(accountId);
            List<String> listEmail = repo.emailList(account.getEmail());
            for(String email : listEmail){
                if(dto.getEmail().equals(email)){
                    return new AccountResponse(false, "Email đã tồn tại!", 1000);
                }
            }

            String avatarPath = DBUpload.getAvatarPath(request, dto);

            boolean isUpdateInfo = repo.updateAccountInfo(avatarPath,
                    dto.getFullName(), dto.getEmail(), dto.getPhone(), accountId);

            if (!isUpdateInfo) {
                connect.rollback();
                return new AccountResponse(false, "Cập nhật thông tin thất bại!", 1000);
            }
            connect.commit();
            Account updatedCustomer = repo.getCustomerById(accountId);

            AccountResponse result = new AccountResponse(true, "Cập nhật thông tin thành công!", 1002);
            result.setAccount(updatedCustomer);
            return result;

        } catch (Exception e) {
            e.printStackTrace();
            return new AccountResponse(false, e.getMessage(), 1004);
        }
    }

    @Override
    public AccountResponse updatePassword(CustomerChangePassDTO dto, Integer accountId) {

        try(Connection connect = DBContext.getConnection()) {
            connect.setAutoCommit(false);

            Account account = repo.getCustomerById(accountId);
            if(!BCrypt.checkpw(dto.getOldPass(), account.getPasswordHash())) {
                return new AccountResponse(false, "Mật khẩu hiện tại không khớp", 1000);
            }

            String hashedNewPass = BCrypt.hashpw(dto.getNewPass(), BCrypt.gensalt());

            boolean isUpdate = repo.updatePassword(hashedNewPass, accountId);
            if(!isUpdate){
                connect.rollback();
                return new AccountResponse(false, "Đổi mật khẩu thất bại!", 1000);
            }

            connect.commit();
            account.setPasswordHash(hashedNewPass);

            AccountResponse result = new AccountResponse(true, "Đổi mật khẩu thành công!", 1002);
            result.setAccount(account);
            return result;

        } catch (Exception e) {
            e.printStackTrace();
            return new AccountResponse(false, e.getMessage(), 1004);
        }
    }
}
