/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.bcb.utils;

import java.util.Properties;
import jakarta.mail.*;
import jakarta.mail.internet.*;

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
            msg.setSubject("Xác nhận đăng ký tài khoản");

            msg.setText(
                "Xin chào,\n\n"
              + "Vui lòng nhấn link sau để xác nhận đăng ký:\n"
              + link + "\n\n"
              + "Link có hiệu lực trong 15 phút."
            );

            Transport.send(msg);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
