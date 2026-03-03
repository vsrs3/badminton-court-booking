<!-- reset-password.jsp -->

<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>

<%-- RESET PASSWORD --%>
<div class="sd-modal-bg" id="sd-rp-modal">
	<div class="sd-modal">
		<div class="sd-modal-hd">
			<div class="d-flex align-items-center gap-3">
				<div class="sd-modal-hd-icon">
					<i class="bi bi-shield-lock-fill"></i>
				</div>
				<h3 class="sd-modal-title">Đặt lại mật khẩu</h3>
			</div>
			<button class="sd-modal-x" onclick="closeResetModal()">
				<i class="bi bi-x-lg"></i>
			</button>
		</div>
		<div class="sd-modal-bd">

			<%-- Bước 1: xác nhận --%>
			<div id="sd-rp-step1">
				<p
					style="font-size: .875rem; font-weight: 500; color: var(--color-gray-600); line-height: 1.6; margin-bottom: 1rem;">
					Bạn có chắc muốn đặt lại mật khẩu cho <strong
						style="color: var(--color-gray-900);" id="sd-rp-name"></strong>?
				</p>
				<div class="sd-warn mb-4">
					<i class="bi bi-exclamation-triangle-fill me-1"></i> Hành động này
					sẽ tạo mật khẩu tạm thời mới. Nhân viên sẽ phải đổi mật khẩu ở lần
					đăng nhập tiếp theo.
				</div>
				<div class="d-flex gap-3">
					<button class="sd-btn-m-cancel" onclick="closeResetModal()">Hủy</button>
					<button class="sd-btn-m-confirm" id="sd-rp-cfm-btn"
						onclick="doResetPassword()">
						<span id="sd-rp-cfm-txt">Xác nhận đặt lại</span>
					</button>
				</div>
			</div>

			<%-- Bước 2: hiển thị mật khẩu tạm --%>
			<div id="sd-rp-step2" class="d-none">
				<div class="sd-success-box mb-4">
					<div class="sd-success-circle">
						<i class="bi bi-check-lg"></i>
					</div>
					<p
						style="font-size: .875rem; font-weight: 700; color: var(--color-green-700); margin: 0;">
						Đặt lại mật khẩu thành công!</p>
				</div>
				<div class="mb-4">
					<span class="sd-info-label">Mật khẩu tạm thời</span>
					<div class="sd-pass-box mt-1">
						<div class="sd-pass-val" id="sd-pass-val">—</div>
						<button class="sd-copy-btn" id="sd-copy-btn"
							onclick="copyPassword()" title="Sao chép">
							<i class="bi bi-clipboard"></i>
						</button>
					</div>
					<p class="sd-pass-note mt-2">Mật khẩu này sẽ không hiển thị
						lại. Hãy sao chép ngay.</p>
				</div>
				<button class="sd-btn-done" onclick="closeResetModal()">Hoàn
					tất</button>
			</div>

		</div>
	</div>
</div>