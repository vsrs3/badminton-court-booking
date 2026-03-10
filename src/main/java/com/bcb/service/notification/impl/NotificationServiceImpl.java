package com.bcb.service.notification.impl;

import com.bcb.dto.notilication.NotificationDTO;
import com.bcb.exception.DataAccessException;
import com.bcb.repository.notification.NotificationRepository;
import com.bcb.repository.notification.impl.NotificationRepositoryImpl;
import com.bcb.service.notification.NotificationService;

public class NotificationServiceImpl implements NotificationService{

	// notification repository
	NotificationRepository notification = new NotificationRepositoryImpl();
	
	@Override
	public void insertResetPassNotification(NotificationDTO dto) {
		
		if(dto.getAccountId() == null) {
			throw new DataAccessException("Account ID không được null");
		}
		if(dto.getContent() == null || dto.getContent().isEmpty() 
				|| dto.getTitle() == null || dto.getTitle().isEmpty()) {
			throw new DataAccessException("Tiêu đề hoặc Nội dung không được null");
		}
		
		notification.insertNotification(dto.getAccountId(), dto.getTitle(), dto.getContent());
	}

}
