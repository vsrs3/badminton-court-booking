package com.bcb.service.notification;

import com.bcb.dto.notilication.NotificationDTO;

public interface NotificationService {
	
	/**
	 * Insert notification to staff when owner reset pass
	 * @param dto
	 * @return void
	 */
	void insertResetPassNotification(NotificationDTO dto);
}
