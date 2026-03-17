package com.bcb.service.impl;

import com.bcb.controller.GoogleOAuthUtil;
import com.bcb.exception.BusinessException;
import com.bcb.model.Account;
import com.bcb.repository.AccountRepository;
import com.bcb.repository.impl.AccountRepositoryImpl;
import com.bcb.service.GoogleAuthService;
import com.google.gson.JsonObject;

public class GoogleAuthServiceImpl implements GoogleAuthService {

    private final AccountRepository accountRepository;

    public GoogleAuthServiceImpl() {
        this.accountRepository = new AccountRepositoryImpl();
    }

    @Override
    public Account handleGoogleLogin(String code) throws Exception {
        String accessToken = GoogleOAuthUtil.getAccessToken(code);
        JsonObject userInfo = GoogleOAuthUtil.getUserInfo(accessToken);
        String googleId = userInfo.get("sub").getAsString();

        return accountRepository.findByGoogleId(googleId);
    }

    @Override
    public Account handleGoogleLinking(String code, String verifiedEmail) throws Exception {
        String accessToken = GoogleOAuthUtil.getAccessToken(code);
        JsonObject userInfo = GoogleOAuthUtil.getUserInfo(accessToken);

        String googleId = userInfo.get("sub").getAsString();
        String googleEmail = userInfo.get("email").getAsString();

        if (!verifiedEmail.equalsIgnoreCase(googleEmail)) {
            throw new BusinessException("Đây không phải tài khoản email bạn đã đăng ký.");
        }

        Account account = accountRepository.findByEmailAnyStatus(googleEmail);
        if (account == null) {
            throw new BusinessException("Tài khoản đăng ký không tồn tại trong hệ thống.");
        }

        if (account.getGoogleId() == null || account.getGoogleId().isBlank()) {
            accountRepository.updateGoogleId(account.getAccountId(), googleId);
            account.setGoogleId(googleId);
        }

        return account;
    }
}
