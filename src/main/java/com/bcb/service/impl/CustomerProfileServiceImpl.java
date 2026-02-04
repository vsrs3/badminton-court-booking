package com.bcb.service.impl;

import com.bcb.config.ConfigUpload;
import com.bcb.dto.CustomerChangePassDTO;
import com.bcb.dto.CustomerProfileDTO;
import com.bcb.model.Account;
import com.bcb.repository.CustomerProfileRepository;
import com.bcb.repository.impl.CustomerProfileRepositoryImpl;
import com.bcb.service.CustomerProfileService;
import com.bcb.dto.response.AccountResponse;
import com.bcb.service.UploadService;
import com.bcb.utils.DBContext;
import jakarta.servlet.http.HttpServletRequest;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.util.List;

public class CustomerProfileServiceImpl implements CustomerProfileService {

    private CustomerProfileRepository repo;
    private final UploadService uploadService;

    public CustomerProfileServiceImpl() {
        this.repo = new CustomerProfileRepositoryImpl();
        this.uploadService = new UploadServiceImpl();
    }

    @Override
    public AccountResponse updateInfo(HttpServletRequest request,
                                      CustomerProfileDTO dto,
                                      Integer accountId) {

        UploadService uploadService = new UploadServiceImpl();

        try (Connection connect = DBContext.getConnection()) {
            connect.setAutoCommit(false);

            Account account = repo.getCustomerById(accountId);

            // Check email trùng
            List<String> listEmail = repo.emailList(account.getEmail());
            for (String email : listEmail) {
                if (dto.getEmail().equals(email)) {
                    return new AccountResponse(false, "Email đã tồn tại!", 1000);
                }
            }

            String avatarPath = dto.getAvatarPath(); // avatar cũ
            String newAvatarPath = null;

            try {
                // Nếu có upload mới
                if (dto.getAvatarFile() != null && dto.getAvatarFile().getSize() > 0) {

                    // upload file mới
                    newAvatarPath = uploadService.saveImage(
                            dto.getAvatarFile(),
                            ConfigUpload.AVATAR_IMAGE_FOLDER
                    );

                    avatarPath = newAvatarPath;
                }

                boolean isUpdateInfo = repo.updateAccountInfo(
                        avatarPath,
                        dto.getFullName(),
                        dto.getEmail(),
                        dto.getPhone(),
                        accountId
                );

                if (!isUpdateInfo) {
                    connect.rollback();

                    // rollback file
                    if (newAvatarPath != null) {
                        uploadService.deleteFile(newAvatarPath);
                    }

                    return new AccountResponse(false, "Cập nhật thông tin thất bại!", 1000);
                }

                connect.commit();

                // Sau khi DB commit xong mới xóa avatar cũ
                if (newAvatarPath != null && dto.getAvatarPath() != null && !dto.getAvatarPath().isBlank()) {
                    uploadService.deleteFile(dto.getAvatarPath());
                }

                Account updatedCustomer = repo.getCustomerById(accountId);
                AccountResponse result = new AccountResponse(true, "Cập nhật thông tin thành công!", 1002);
                result.setAccount(updatedCustomer);
                return result;

            } catch (Exception e) {
                connect.rollback();

                // rollback file nếu upload xong nhưng DB fail
                if (newAvatarPath != null) {
                    uploadService.deleteFile(newAvatarPath);
                }

                throw e;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return new AccountResponse(false, e.getMessage(), 1004);
        }
    }


    @Override
    public AccountResponse updatePassword(CustomerChangePassDTO dto, Integer accountId) {

        try (Connection connect = DBContext.getConnection()) {
            connect.setAutoCommit(false);

            Account account = repo.getCustomerById(accountId);
            if (account == null) {
                return new AccountResponse(false, "Tài khoản không tồn tại", 1000);
            }

            if (!BCrypt.checkpw(dto.getOldPass(), account.getPasswordHash())) {
                return new AccountResponse(false, "Mật khẩu hiện tại không khớp", 1000);
            }

            String hashedNewPass = BCrypt.hashpw(dto.getNewPass(), BCrypt.gensalt(12));

            boolean isUpdate = repo.updatePassword(hashedNewPass, accountId);
            if (!isUpdate) {
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
