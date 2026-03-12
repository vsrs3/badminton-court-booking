package com.bcb.model;

import jakarta.mail.Authenticator;
import jakarta.mail.PasswordAuthentication;

public class EmailAuthenticator extends Authenticator {

    private final String username;
    private final String password;

    public EmailAuthenticator(String username, String password) {
        this.username = username;
        this.password = password;
    }

    @Override
    protected PasswordAuthentication getPasswordAuthentication() {
        return new PasswordAuthentication(username, password);
    }
}
