package com.bcb.service.impl;

import com.bcb.controller.GoogleOAuthUtil;

import com.bcb.exception.BusinessException;
import com.bcb.model.Account;
import com.bcb.repository.AccountRepository;
import com.bcb.repository.impl.AccountRepositoryImpl;
import com.bcb.service.GoogleAuthService;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.util.Optional;

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
        System.out.println("hiiiii" + googleEmail);
        System.out.println("llll" + verifiedEmail);
        if (!verifiedEmail.equalsIgnoreCase(googleEmail)) {
        throw new BusinessException(
        "Đây không phải tài khoản email bạn đã đăng ký.");}
        Account acc = accountRepository.findByEmailAnyStatus(googleEmail);
        if (acc.getGoogleId() == null) {
            accountRepository.updateGoogleId(
                    acc.getAccountId(),
                    googleId
            );
        }
        return acc;}


}

