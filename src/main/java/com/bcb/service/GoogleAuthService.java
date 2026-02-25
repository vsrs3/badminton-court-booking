package com.bcb.service;

import com.bcb.model.Account;

import java.io.IOException;

public interface GoogleAuthService {
    Account handleGoogleLogin(String code) throws Exception;

    Account handleGoogleLinking(String code, String verifiedEmail) throws Exception;
}
