package com.bcb.service.impl;

import com.bcb.config.ConfigMail;
import com.bcb.model.EmailAuthenticator;
import com.bcb.service.SendEmailService;
import java.util.Properties;
import jakarta.mail.*;
import jakarta.mail.internet.*;

public class SendEmailServiceImpl implements SendEmailService {

	/**
	 *  host : địa chỉ máy chủ SMTP dùng để gửi mail
	 *  from : email của owner (người gửi)
	 *  pass : mk của app bảo mật trong gmail sau khi xác minh 2 lớp
	 *  toEmail: email của staff (người nhận) 
	 */
	
	private String host = ConfigMail.get("mail.smtp.host");
	private String port = ConfigMail.get("mail.smtp.port");
	private String from = ConfigMail.get("mail.username");
	private String pass = ConfigMail.get("mail.password");

	@Override
	public void send(String toEmail, String subject, String body) throws MessagingException {

		Properties props = new Properties();
		props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.smtp.host", host);
		//props.put("mail.smtp.user", from);
		//props.put("mail.smtp.password", pass);
		props.put("mail.smtp.port", port);
		props.put("mail.smtp.auth", "true");

		//Lấy xác nhận mật khẩu
		Session session = Session.getInstance(props, new EmailAuthenticator(from, pass));

		MimeMessage message = new MimeMessage(session);
		message.setFrom(new InternetAddress(from));
		message.addRecipient(Message.RecipientType.TO, new InternetAddress(toEmail));
		message.setSubject(subject, "UTF-8");
		message.setContent(body, "text/html; charset=UTF-8");

		Transport.send(message);
	}

	@Override
	public void sendWelcomeEmail(String toEmail, String userName, String loginLink)
			throws MessagingException{
		
		String body = "<h2>Xin chào " + userName + ",</h2>"

        + "<p>Tài khoản nhân viên của bạn đã được tạo thành công trên hệ thống.</p>"
        + "<p>Vui lòng nhấn vào nút bên dưới để đăng nhập và bắt đầu sử dụng:</p>"

        + "<p>"
	        + "<a href='" + loginLink + "' "
	        + "style='display:inline-block;padding:10px 18px;"
	        + "background-color:#064E3B;color:white;"
	        + "text-decoration:none;border-radius:5px;font-weight:bold;'>"
	        + "Đăng nhập hệ thống"
	        + "</a>"
        + "</p>"

        + "<p>Vui lòng không chia sẻ email này để đảm bảo an toàn cho doanh nghiệp.</p>"
        + "<p>Trân trọng,<br><strong>Ban Quản Trị Hệ Thống BCB</strong></p>";

        send(toEmail, "Tài khoản của bạn đã được tạo", body);
	}

	@Override
	public void resetStaffPassword(String toEmail, String userName, String loginLink)  throws MessagingException{
		String body = "<h2>Xin chào " + userName + ",</h2>"
	            + "<p>Yêu cầu đặt lại mật khẩu đã được gửi.</p>"
	            + "<a href='" + loginLink + "'>Đăng nhập ngay</a>"
	            + "<p>Link có hiệu lực trong 1 giờ.</p>";
	        send(toEmail, "Yêu cầu đặt lại mật khẩu", body);

	}

}
