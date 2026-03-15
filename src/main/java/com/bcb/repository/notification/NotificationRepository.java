package com.bcb.repository.notification;

import com.bcb.dto.notilication.NotificationDTO;

public interface NotificationRepository {
	/**
    * Inserts a new notification record.
    *
    * @param notification the notification entity
    */
   void insertNotification(Integer acountId, String title, String content);
}
