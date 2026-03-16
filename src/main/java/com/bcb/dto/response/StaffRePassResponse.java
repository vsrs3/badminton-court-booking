package com.bcb.dto.response;

public class StaffRePassResponse {
	
	private String title = "Nhân viên cần phải đổi mật khẩu";
	private String content = "Mật khẩu của bạn đã được Owner đặt lại. "
			+ "Vì lý do bảo mật, vui lòng đăng nhập và thay đổi mật khẩu mới ngay lập tức trước khi tiếp tục sử dụng hệ thống.";
	
	public StaffRePassResponse() {
		super();
	}

	public StaffRePassResponse(String title, String content) {
		super();
		this.title = title;
		this.content = content;
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
