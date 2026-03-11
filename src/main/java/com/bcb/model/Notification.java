package com.bcb.model;

import java.time.LocalDateTime;

/**
 * Entity representing the Notification table.
 */
public class Notification {
    private Integer notificationId;
    private Integer accountId;
    private String title;
    private String content;
    private String type; // EMAIL, SYSTEM
    private Boolean isSent;
    private LocalDateTime createdAt;

    public Notification() {}

    public Integer getNotificationId() { return notificationId; }
    public void setNotificationId(Integer notificationId) { this.notificationId = notificationId; }

    public Integer getAccountId() { return accountId; }
    public void setAccountId(Integer accountId) { this.accountId = accountId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public Boolean getIsSent() { return isSent; }
    public void setIsSent(Boolean isSent) { this.isSent = isSent; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}

