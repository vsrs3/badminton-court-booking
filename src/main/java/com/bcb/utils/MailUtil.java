package com.bcb.utils;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeUtility;

import java.util.Properties;


public class MailUtil {

    public static void sendVerifyEmail(String to, String link) {

        final String from = "duc29081234@gmail.com";
        final String password = "ikgfakajggozqtsr";

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        Session session = Session.getInstance(props,
                new Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(from, password);
                    }
                });
        try {
            Message msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress(from));
            msg.setRecipients(
                    Message.RecipientType.TO,
                    InternetAddress.parse(to)
            );
            msg.setSubject(
                    MimeUtility.encodeText("Xác nhận đăng ký tài khoản", "UTF-8", "B"));
            msg.setContent(
                    "<h3>Xin chào,</h3>"
                            + "<p>Vui lòng nhấn link sau để xác nhận đăng ký:</p>"
                            + "<a href='" + link + "'>Xác nhận tài khoản</a>"
                            + "<p><i>Link có hiệu lực trong 1 phút.</i></p>",
                    "text/html; charset=UTF-8"
            );Transport.send(msg);
        } catch (Exception e) {
            throw new RuntimeException(e);}}}
