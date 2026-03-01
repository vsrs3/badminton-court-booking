package com.bcb.repository.mybooking;

import com.bcb.model.Notification;

/**
 * Repository interface for Notification CRUD.
 */
public interface NotificationRepository {

    /**
     * Inserts a new notification record.
     *
     * @param notification the notification entity
     */
    void insertNotification(Notification notification);
}
