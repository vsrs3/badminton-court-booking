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
		
		String body = "<h2>Xin chào " + userName + "</h2>"

        + "<p>Tài khoản nhân viên của bạn đã được tạo thành công trên hệ thống.</p>"
        + "<p>Vui lòng nhấn vào nút bên dưới để đăng nhập và bắt đầu sử dụng:</p>"
        + "<p>Email đăng nhập: " + toEmail + "</p>"
        + "<p>Mật khẩu đăng nhập: 123456</p>"
        + "<p>"
	        + "<a href='" + loginLink + "' "
	        + "style='display:inline-block;"
	        + "padding:12px 20px;"
	        + "background-color:#A3E635;"
	        + "color:#064E3B;"
	        + "text-decoration:none;"
	        + "font-weight:600;"
	        + "border-radius:8px;'>"
	        + "Đăng nhập ngay"
	        + "</a>"
        + "</p>"
		+ "<p style='margin-top:20px;font-size:14px;color:#555;'>"
			+ "Vì lý do bảo mật, vui lòng không chia sẻ email này với bất kỳ ai."
		+ "</p>"
        + "<p>Trân trọng,<br><strong>Ban Quản Trị Hệ Thống BCB</strong></p>"
        + "</div>";

        send(toEmail, "Tài khoản của bạn đã được tạo", body);
	}

	@Override
	public void resetStaffPassword(String toEmail, String userName, String tempPassword, String loginLink)  throws MessagingException{
		String body = "<div style='font-family: Arial, sans-serif; line-height: 1.6;'>"
		        + "<h2>Xin chào " + userName + "</h2>"
		        + "<p>Hệ thống đã nhận được yêu cầu đặt lại mật khẩu cho tài khoản của bạn.</p>"
		        + "<p>Mật khẩu tạm thời đã được cấp. Vui lòng đăng nhập và thay đổi mật khẩu ngay sau khi truy cập.</p>"
		        + "<p>Email đăng nhập: " + toEmail + "</p>"
		        + "<p>Mật khẩu đăng nhập: " + tempPassword + "</p>"
		        + "<p style='margin: 24px 0;'>"
			        + "<a href='" + loginLink + "' "
			        + "style='display:inline-block;"
			        + "padding:12px 20px;"
			        + "background-color:#A3E635;"
			        + "color:#064E3B;"
			        + "text-decoration:none;"
			        + "font-weight:600;"
			        + "border-radius:8px;'>"
			        + "Đăng nhập ngay"
			        + "</a>"
		        + "</p>"
		        + "<p style='margin-top:20px;font-size:14px;color:#555;'>"
		        	+ "Vì lý do bảo mật, vui lòng không chia sẻ email này với bất kỳ ai."
		        + "</p>"
		        + "<p>Trân trọng,<br>Ban Quản Trị Hệ Thống BCB</p>"
		        + "</div>";

		send(toEmail, "Mật khẩu tạm thời của bạn", body);

	}

}
