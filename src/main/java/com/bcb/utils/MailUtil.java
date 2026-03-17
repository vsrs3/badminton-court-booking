package com.bcb.utils;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeUtility;

import java.util.Properties;

public class MailUtil {
    private static final String FROM = "duc29081234@gmail.com";
    private static final String PASSWORD = "ikgfakajggozqtsr";

    public static void sendVerifyEmail(String to, String link) {
        sendHtmlEmail(
                to,
                "Xác nhận đăng ký tài khoản",
                "<h3>Xin chào,</h3>"
                        + "<p>Vui lòng nhấn vào liên kết sau để xác nhận đăng ký tài khoản:</p>"
                        + "<a href='" + link + "'>Xác nhận tài khoản</a>"
                        + "<p><i>Liên kết có hiệu lực trong 1 phút.</i></p>"
        );
    }

    public static void sendPasswordResetEmail(String to, String link) {
        sendHtmlEmail(
                to,
                "Xác nhận đổi mật khẩu",
                "<h3>Xin chào,</h3>"
                        + "<p>Chúng tôi đã nhận được yêu cầu đổi mật khẩu cho tài khoản của bạn.</p>"
                        + "<p>Vui lòng nhấn vào liên kết sau để xác nhận thay đổi mật khẩu trên website:</p>"
                        + "<a href='" + link + "'>Xác nhận đổi mật khẩu</a>"
                        + "<p>Sau khi xác nhận, hệ thống sẽ chuyển bạn tới trang nhập mật khẩu mới.</p>"
                        + "<p><i>Liên kết có hiệu lực trong 15 phút.</i></p>"
        );
    }

    private static void sendHtmlEmail(String to, String subject, String htmlContent) {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(FROM, PASSWORD);
            }
        });

        try {
            Message msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress(FROM));
            msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            msg.setSubject(MimeUtility.encodeText(subject, "UTF-8", "B"));
            msg.setContent(htmlContent, "text/html; charset=UTF-8");
            Transport.send(msg);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
