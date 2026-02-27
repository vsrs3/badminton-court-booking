package com.bcb.service;

import jakarta.mail.MessagingException;

public interface SendEmailService {
	
	/**
	 * Method lõi , dùng chung cho tất cả hàm gửi email
	 * @param toEmail
	 * @param subject
	 * @param body
	 */
	void send(String toEmail, String subject, String body)  throws MessagingException;
	
	/**
	 * Tạo tài khoản mới
	 * @param toEmail
	 * @param userName
	 * @param link
	 */
	void sendWelcomeEmail(String toEmail, String userName, String loginLink)  throws MessagingException;
	
	/**
	 * Reset mật khẩu
	 * @param toEmail
	 * @param userName
	 * @param link
	 */
	void resetStaffPassword(String toEmail, String userName, String loginLink)  throws MessagingException;
}
