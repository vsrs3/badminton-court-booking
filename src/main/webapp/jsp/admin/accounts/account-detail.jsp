<%-- account-detail.jsp --%>

<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>

<%@ include file="../layout/layout.jsp"%>
<%@ include file="../layout/sidebar.jsp"%>

<link rel="stylesheet"
	href="${pageContext.request.contextPath}/assets/css/account-management.css">

<div class="main-content">
	<%@ include file="../layout/header.jsp"%>

	<div class="content-area p-4">
		<div class="am-detail-wrapper">

			<%-- ===== ALERTS ===== --%>
			<c:if test="${param.success == '1'}">
				<div class="alert alert-success d-flex align-items-center gap-2 rounded-3 mb-3" role="alert">
					<i class="bi bi-check-circle-fill"></i> Cập nhật tài khoản thành công!
				</div>
			</c:if>
			<c:if test="${not empty requestScope.error}">
				<div class="alert alert-danger d-flex align-items-center gap-2 rounded-3 mb-3" role="alert">
					<i class="bi bi-exclamation-circle-fill"></i> ${requestScope.error}
				</div>
			</c:if>

			<%-- ===== TOP BAR ===== --%>
			<div class="d-flex flex-column flex-lg-row justify-content-between align-items-lg-center gap-3 mb-4">

				<%-- Avatar + Name --%>
				<div class="d-flex align-items-center gap-3">
					<div class="position-relative d-inline-block">
						<c:choose>
							<c:when test="${not empty account.avatarPath}">
								<img src="${pageContext.request.contextPath}/${account.avatarPath}"
									alt="${account.fullName}" class="am-detail-avatar">
							</c:when>
							<c:otherwise>
								<div class="am-detail-avatar-placeholder">
									<c:choose>
										<c:when test="${not empty account.fullName}">
											${account.fullName.substring(0,1).toUpperCase()}
										</c:when>
										<c:otherwise>?</c:otherwise>
									</c:choose>
								</div>
							</c:otherwise>
						</c:choose>
						<div class="am-detail-status-dot ${account.isActive ? 'active' : 'inactive'}"></div>
					</div>

					<div>
						<div class="d-flex align-items-center flex-wrap gap-2 mb-1">
							<h1 class="fw-black mb-0" style="font-size:1.5rem; color:#111827;">
								${account.fullName}
							</h1>
							<span class="am-status-badge ${account.isActive ? 'active' : 'inactive'}">
								<span class="am-status-dot ${account.isActive ? 'active' : 'inactive'}"></span>
								${account.isActive ? 'Hoạt động' : 'Ngừng hoạt động'}
							</span>
						</div>
						<p class="mb-0" style="font-size:0.8125rem; color:#9CA3AF; font-weight:600;">
							<span class="am-role-badge am-role-${account.role.toLowerCase()}">${account.role}</span>
							&middot; ID: #${account.accountId}
						</p>
					</div>
				</div>

				<%-- Action buttons --%>
				<div class="d-flex flex-wrap gap-2">
					<a href="${pageContext.request.contextPath}/admin/accounts/list"
						class="btn btn-outline-secondary rounded-3" style="font-size:0.8125rem; font-weight:600;">
						<i class="bi bi-arrow-left me-1"></i> Quay lại
					</a>
					<button onclick="switchTab('edit')" class="btn btn-brand rounded-3" id="am-btn-edit"
						style="font-size:0.8125rem; font-weight:600;">
						<i class="bi bi-pencil me-1"></i> Sửa thông tin
					</button>
					<button onclick="switchTab('overview')" class="btn btn-outline-secondary rounded-3 d-none" id="am-btn-cancel"
						style="font-size:0.8125rem; font-weight:600;">
						<i class="bi bi-x me-1"></i> Hủy chỉnh sửa
					</button>
					<a href="${pageContext.request.contextPath}/admin/accounts/toggle/${account.accountId}?redirect=detail"
						class="btn rounded-3 ${account.isActive ? 'btn-outline-warning' : 'btn-outline-success'}"
						style="font-size:0.8125rem; font-weight:600;"
						onclick="return confirm('Bạn có chắc muốn ${account.isActive ? 'vô hiệu hóa' : 'kích hoạt'} tài khoản này?');">
						<i class="bi bi-${account.isActive ? 'pause-circle' : 'play-circle'} me-1"></i>
						${account.isActive ? 'Vô hiệu hóa' : 'Kích hoạt'}
					</a>
					<a href="${pageContext.request.contextPath}/admin/accounts/delete/${account.accountId}"
						class="btn btn-outline-danger rounded-3"
						style="font-size:0.8125rem; font-weight:600;"
						onclick="return confirm('CẢNH BÁO: Bạn có chắc muốn XÓA VĨNH VIỄN tài khoản này? Hành động không thể hoàn tác!');">
						<i class="bi bi-trash me-1"></i> Xóa vĩnh viễn
					</a>
				</div>
			</div>

			<%-- ===== TAB NAV ===== --%>
			<div class="am-tabs mb-4">
				<button class="am-tab active" id="am-tab-ov" onclick="switchTab('overview')">
					<i class="bi bi-grid me-1"></i>Tổng quan
				</button>
				<button class="am-tab" id="am-tab-ed" onclick="switchTab('edit')">
					<i class="bi bi-pencil-square me-1"></i>Sửa thông tin
				</button>
			</div>

			<%-- ===== OVERVIEW PANEL ===== --%>
			<div id="am-panel-ov">
				<div class="row g-4">

					<%-- Contact Info --%>
					<div class="col-lg-8">
						<div class="am-card h-100">
							<div class="am-card-header">
								<div class="am-card-icon ci-blue">
									<i class="bi bi-envelope-fill"></i>
								</div>
								<h2 class="am-card-title">Thông tin liên hệ</h2>
							</div>
							<div class="row g-4">
								<div class="col-md-6 d-flex flex-column gap-4">
									<div>
										<span class="am-info-label">Họ và tên</span>
										<p class="am-info-value">${account.fullName}</p>
									</div>
									<div>
										<span class="am-info-label">Địa chỉ email</span>
										<p class="am-info-value">${account.email}</p>
									</div>
								</div>
								<div class="col-md-6 d-flex flex-column gap-4">
									<div>
										<span class="am-info-label">Số điện thoại</span>
										<p class="am-info-value">${not empty account.phone ? account.phone : '—'}</p>
									</div>
									<div>
										<span class="am-info-label">Ngày tạo</span>
										<p class="am-info-value">
											<c:choose>
												<c:when test="${not empty account.createdAt}">
													${account.createdAt.toString().substring(0,10)}
												</c:when>
												<c:otherwise>—</c:otherwise>
											</c:choose>
										</p>
									</div>
								</div>
							</div>
						</div>
					</div>

					<%-- Account Info --%>
					<div class="col-lg-4">
						<div class="am-card h-100 d-flex flex-column">
							<div class="am-card-header">
								<div class="am-card-icon ci-green">
									<i class="bi bi-shield-lock-fill"></i>
								</div>
								<h2 class="am-card-title">Thông tin tài khoản</h2>
							</div>
							<div class="d-flex flex-column gap-3 flex-grow-1">
								<div>
									<span class="am-info-label">Mã tài khoản</span>
									<p class="am-info-value">#${account.accountId}</p>
								</div>
								<div>
									<span class="am-info-label">Vai trò</span>
									<p class="am-info-value">
										<span class="am-role-badge am-role-${account.role.toLowerCase()}">${account.role}</span>
									</p>
								</div>
								<div>
									<span class="am-info-label">Đăng nhập Google</span>
									<p class="am-info-value">${not empty account.googleId ? 'Đã liên kết' : 'Chưa liên kết'}</p>
								</div>
								<div>
									<span class="am-info-label">Trạng thái</span>
									<p class="am-info-value">
										<span class="am-status-badge ${account.isActive ? 'active' : 'inactive'}">
											<span class="am-status-dot ${account.isActive ? 'active' : 'inactive'}"></span>
											${account.isActive ? 'Hoạt động' : 'Ngừng hoạt động'}
										</span>
									</p>
								</div>
							</div>
						</div>
					</div>

				</div>
			</div>

			<%-- ===== EDIT PANEL ===== --%>
			<div id="am-panel-ed" class="d-none">
				<div class="am-card">
					<form action="${pageContext.request.contextPath}/admin/accounts/update"
						method="POST" id="am-edit-form" novalidate>

						<input type="hidden" name="accountId" value="${account.accountId}">

						<div class="row g-4 g-xl-5">

							<%-- Left Column: Basic Info --%>
							<div class="col-md-6">
								<div class="am-section-hd">
									<div class="am-section-icon ci-blue">
										<i class="bi bi-person-fill"></i>
									</div>
									<h4 class="am-section-title">Thông tin cơ bản</h4>
								</div>

								<div class="d-flex flex-column gap-3">
									<div>
										<label class="am-form-label" for="am-fullName">Họ và tên *</label>
										<input type="text" name="fullName" id="am-fullName"
											class="am-form-input" value="${account.fullName}"
											placeholder="Nguyễn Văn A" required>
										<div class="am-error-msg" id="err-fullName"></div>
									</div>
									<div>
										<label class="am-form-label" for="am-email">Email *</label>
										<input type="email" name="email" id="am-email"
											class="am-form-input" value="${account.email}"
											placeholder="user@example.com" required>
										<div class="am-error-msg" id="err-email"></div>
									</div>
									<div>
										<label class="am-form-label" for="am-phone">Số điện thoại *</label>
										<input type="tel" name="phone" id="am-phone"
											class="am-form-input" value="${account.phone}"
											placeholder="0912345678" required>
										<div class="am-error-msg" id="err-phone"></div>
									</div>
								</div>
							</div>

							<%-- Right Column: Role --%>
							<div class="col-md-6">
								<div class="am-section-hd">
									<div class="am-section-icon ci-green">
										<i class="bi bi-shield-fill"></i>
									</div>
									<h4 class="am-section-title">Phân quyền</h4>
								</div>

								<div class="d-flex flex-column gap-3">
									<div>
										<label class="am-form-label" for="am-role">Vai trò *</label>
										<select name="role" id="am-role" class="am-form-input" required>
											<option value="ADMIN" ${account.role == 'ADMIN' ? 'selected' : ''}>Admin</option>
											<option value="OWNER" ${account.role == 'OWNER' ? 'selected' : ''}>Owner</option>
											<option value="STAFF" ${account.role == 'STAFF' ? 'selected' : ''}>Staff</option>
											<option value="CUSTOMER" ${account.role == 'CUSTOMER' ? 'selected' : ''}>Customer</option>
										</select>
										<div class="am-error-msg" id="err-role"></div>
									</div>

									<div class="am-info-box mt-3">
										<div class="d-flex align-items-center gap-2 mb-2">
											<i class="bi bi-info-circle-fill" style="color:#3B82F6;"></i>
											<span class="fw-bold" style="font-size:0.8125rem; color:#1E40AF;">Lưu ý về vai trò</span>
										</div>
										<ul class="mb-0" style="font-size:0.75rem; color:#4B5563; padding-left:1.25rem;">
											<li><strong>ADMIN</strong> — Quản trị toàn hệ thống</li>
											<li><strong>OWNER</strong> — Chủ sân, quản lý cơ sở</li>
											<li><strong>STAFF</strong> — Nhân viên cơ sở</li>
											<li><strong>CUSTOMER</strong> — Khách hàng</li>
										</ul>
									</div>
								</div>
							</div>

						</div>

						<%-- Action Bar --%>
						<div class="am-edit-actions">
							<button type="reset" class="btn btn-outline-secondary rounded-3"
								style="font-size:0.8125rem; font-weight:600;">
								<i class="bi bi-arrow-counterclockwise me-1"></i>Đặt lại
							</button>
							<button type="submit" class="btn btn-brand rounded-3"
								style="font-size:0.8125rem; font-weight:600;">
								<i class="bi bi-check-lg me-1"></i>Lưu thay đổi
							</button>
						</div>

					</form>
				</div>
			</div>

		</div>
	</div>

</div>
</div>
</div>

<script>
/* ===== Tab switching ===== */
function switchTab(tab) {
    const panelOv = document.getElementById('am-panel-ov');
    const panelEd = document.getElementById('am-panel-ed');
    const tabOv   = document.getElementById('am-tab-ov');
    const tabEd   = document.getElementById('am-tab-ed');
    const btnEdit   = document.getElementById('am-btn-edit');
    const btnCancel = document.getElementById('am-btn-cancel');

    if (tab === 'edit') {
        panelOv.classList.add('d-none');
        panelEd.classList.remove('d-none');
        tabOv.classList.remove('active');
        tabEd.classList.add('active');
        btnEdit.classList.add('d-none');
        btnCancel.classList.remove('d-none');
    } else {
        panelOv.classList.remove('d-none');
        panelEd.classList.add('d-none');
        tabOv.classList.add('active');
        tabEd.classList.remove('active');
        btnEdit.classList.remove('d-none');
        btnCancel.classList.add('d-none');
    }
}

/* ===== Client-side form validation ===== */
(function() {
    const form = document.getElementById('am-edit-form');
    if (!form) return;

    const emailRegex = /^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$/;
    const phoneRegex = /^(0|\+84)(3|5|7|8|9)[0-9]{8}$/;

    function showError(id, msg) {
        const el = document.getElementById(id);
        if (el) { el.textContent = msg; el.style.display = 'block'; }
    }
    function clearError(id) {
        const el = document.getElementById(id);
        if (el) { el.textContent = ''; el.style.display = 'none'; }
    }
    function clearAllErrors() {
        ['err-fullName','err-email','err-phone','err-role'].forEach(clearError);
    }

    form.addEventListener('submit', function(e) {
        clearAllErrors();
        let valid = true;

        const fullName = form.fullName.value.trim();
        const email    = form.email.value.trim();
        const phone    = form.phone.value.trim();
        const role     = form.role.value;

        if (!fullName) {
            showError('err-fullName', 'Vui lòng nhập họ và tên');
            valid = false;
        } else if (fullName.length < 2 || fullName.length > 100) {
            showError('err-fullName', 'Họ và tên phải từ 2 đến 100 ký tự');
            valid = false;
        }

        if (!email) {
            showError('err-email', 'Vui lòng nhập email');
            valid = false;
        } else if (!emailRegex.test(email)) {
            showError('err-email', 'Email không đúng định dạng');
            valid = false;
        }

        if (!phone) {
            showError('err-phone', 'Vui lòng nhập số điện thoại');
            valid = false;
        } else if (!phoneRegex.test(phone)) {
            showError('err-phone', 'SĐT không đúng định dạng (VD: 0912345678)');
            valid = false;
        }

        if (!role) {
            showError('err-role', 'Vui lòng chọn vai trò');
            valid = false;
        }

        if (!valid) {
            e.preventDefault();
        }
    });

    // Real-time validation on blur
    document.getElementById('am-fullName').addEventListener('blur', function() {
        const v = this.value.trim();
        if (!v) showError('err-fullName', 'Vui lòng nhập họ và tên');
        else if (v.length < 2 || v.length > 100) showError('err-fullName', 'Họ và tên phải từ 2 đến 100 ký tự');
        else clearError('err-fullName');
    });

    document.getElementById('am-email').addEventListener('blur', function() {
        const v = this.value.trim();
        if (!v) showError('err-email', 'Vui lòng nhập email');
        else if (!emailRegex.test(v)) showError('err-email', 'Email không đúng định dạng');
        else clearError('err-email');
    });

    document.getElementById('am-phone').addEventListener('blur', function() {
        const v = this.value.trim();
        if (!v) showError('err-phone', 'Vui lòng nhập số điện thoại');
        else if (!phoneRegex.test(v)) showError('err-phone', 'SĐT không đúng định dạng (VD: 0912345678)');
        else clearError('err-phone');
    });
})();
</script>
