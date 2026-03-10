package com.bcb.dto.notilication;

public class NotificationDTO {
	private Integer accountId;
    private String title;
    private String content;
    
	public NotificationDTO() {
		super();
	}

	public NotificationDTO(Integer accountId, String title, String content) {
		super();
		this.accountId = accountId;
		this.title = title;
		this.content = content;
	}

	public Integer getAccountId() {
		return accountId;
	}

	public void setAccountId(Integer accountId) {
		this.accountId = accountId;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}
    
}
