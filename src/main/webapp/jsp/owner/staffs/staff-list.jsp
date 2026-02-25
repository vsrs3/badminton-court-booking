<!-- staff-list.jsp -->

<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ include file="../layout/layout.jsp"%>
<%@ include file="../layout/sidebar.jsp"%>

<style>
.bg-brand {
	background-color: #064E3B !important;
}

.text-brand {
	color: #064E3B !important;
}

.btn-brand {
	background-color: #064E3B;
	color: #fff;
	border: none;
}

.btn-brand:hover {
	background-color: #053d2f;
	color: #fff;
}

.staff-card {
	transition: all 0.25s;
	border-radius: 1.25rem !important;
	border: 1px solid #F3F4F6 !important;
}

.staff-card:hover {
	box-shadow: 0 12px 32px rgba(0, 0, 0, 0.1) !important;
	transform: translateY(-4px);
}

.staff-avatar {
	width: 76px;
	height: 76px;
	border-radius: 50%;
	object-fit: cover;
	border: 3px solid #F3F4F6;
	box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.staff-avatar-placeholder {
	width: 76px;
	height: 76px;
	border-radius: 50%;
	background: #064E3B;
	display: flex;
	align-items: center;
	justify-content: center;
	border: 3px solid #F3F4F6;
	box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
	font-size: 1.625rem;
	font-weight: 900;
	color: #A3E635;
	flex-shrink: 0;
}

.status-dot {
	width: 16px;
	height: 16px;
	border-radius: 50%;
	border: 2.5px solid #fff;
	position: absolute;
	bottom: 3px;
	right: 3px;
	box-shadow: 0 1px 3px rgba(0, 0, 0, 0.2);
}

.status-dot.active {
	background: #22C55E;
}

.status-dot.inactive {
	background: #9CA3AF;
}

.icon-box {
	width: 32px;
	height: 32px;
	border-radius: 0.5rem;
	background: #F3F4F6;
	display: flex;
	align-items: center;
	justify-content: center;
	flex-shrink: 0;
	color: #6B7280;
}

.info-block {
	background: #F9FAFB;
	border: 1px solid #F3F4F6;
	border-radius: 0.875rem;
	padding: 0.875rem;
}

.info-label {
	font-size: 0.5rem;
	font-weight: 900;
	color: #9CA3AF;
	text-transform: uppercase;
	letter-spacing: 0.1em;
	margin-bottom: 0.2rem;
}

.info-value {
	font-size: 0.8125rem;
	font-weight: 700;
	color: #111827;
	margin: 0;
	overflow: hidden;
	text-overflow: ellipsis;
	white-space: nowrap;
}

.menu-item {
	display: flex;
	align-items: center;
	gap: 0.75rem;
	padding: 0.625rem 0.875rem;
	border-radius: 0.625rem;
	font-size: 0.875rem;
	font-weight: 600;
	color: #374151;
	text-decoration: none;
	transition: background 0.15s;
}

.menu-item:hover {
	background: #F9FAFB;
	color: #374151;
}

.menu-item.danger {
	color: #DC2626;
}

.menu-item.danger:hover {
	background: #FEF2F2;
}

.menu-icon {
	width: 28px;
	height: 28px;
	border-radius: 0.5rem;
	display: flex;
	align-items: center;
	justify-content: center;
	flex-shrink: 0;
}

.form-control:focus, .form-select:focus {
	border-color: #064E3B !important;
	box-shadow: 0 0 0 0.2rem rgba(6, 78, 59, 0.15) !important;
}

.page-item.active .page-link {
	background-color: #064E3B;
	border-color: #064E3B;
}

.page-link {
	color: #064E3B;
}
</style>

<div class="main-content">
	<%@ include file="../layout/header.jsp"%>
	<div class="content-area">

		<%-- ===== PAGE HEADER ===== --%>
		<div class="mb-4">
			<h1 class="fw-black mb-1"
				style="font-size: 1.75rem; color: #111827; letter-spacing: -0.02em;">Nhân
				Viên BCB</h1>
			<p class="mb-0" style="font-size: 0.875rem; color: #9CA3AF;">Xem
				và quản lý danh sách nhân viên của bạn</p>
		</div>

		<%-- ===== ALERTS ===== --%>
		<c:if test="${not empty requestScope.success}">
			<div
				class="alert alert-success d-flex align-items-center gap-2 rounded-3 mb-3"
				role="alert">
				<i class="bi bi-check-circle-fill"></i> ${requestScope.success}
			</div>
		</c:if>
		<c:if test="${not empty requestScope.error}">
			<div
				class="alert alert-danger d-flex align-items-center gap-2 rounded-3 mb-3"
				role="alert">
				<i class="bi bi-exclamation-circle-fill"></i> ${requestScope.error}
			</div>
		</c:if>

		<%-- ===== SEARCH + FILTER BAR (giống customer-list) ===== --%>
		<div class="bg-white rounded-3 border mb-3 px-3 py-3"
			style="border-color: #F3F4F6 !important; box-shadow: 0 1px 4px rgba(0, 0, 0, 0.05);">
			<form method="GET"
				action="${pageContext.request.contextPath}/owner/staffs/list"
				class="d-flex gap-3 align-items-center flex-wrap">

				<%-- Search keyword --%>
				<div class="flex-grow-1 position-relative" style="min-width: 200px;">
					<i class="bi bi-search position-absolute text-secondary"
						style="left: 0.875rem; top: 50%; transform: translateY(-50%); font-size: 0.875rem; pointer-events: none;"></i>
					<input type="text" name="keyword" value="${requestScope.keyword}"
						class="form-control rounded-3"
						placeholder="Tìm kiếm theo tên, email..."
						style="padding-left: 2.5rem; font-size: 0.875rem;">
				</div>

				<%-- Status filter --%>
				<div style="position: relative; min-width: 160px;">
					<select name="isActive" class="form-select rounded-3 fw-bold"
						style="font-size: 0.875rem; color: #374151; appearance: none; padding-right: 2.5rem;">
						<option value=""
							${empty requestScope.isActiveFilter       ? 'selected':''}>Tất
							Cả</option>
						<option value="true"
							${'true'  == requestScope.isActiveFilter ? 'selected':''}>Hoạt
							Động</option>
						<option value="false"
							${'false' == requestScope.isActiveFilter ? 'selected':''}>Không
							Hoạt Động</option>
					</select> <i class="bi bi-chevron-down position-absolute text-secondary"
						style="right: 0.875rem; top: 50%; transform: translateY(-50%); font-size: 0.75rem; pointer-events: none;"></i>
				</div>

				<%-- Submit --%>
				<button type="submit" class="btn btn-brand fw-bold rounded-3"
					style="padding: 0.5rem 1.25rem; font-size: 0.875rem; white-space: nowrap;">
					<i class="bi bi-search me-1"></i> Tìm kiếm
				</button>

				<%-- Add Staff --%>
				<a href="${pageContext.request.contextPath}/owner/staffs/create"
					class="btn fw-bold rounded-3 text-nowrap"
					style="padding: 0.5rem 1.25rem; font-size: 0.875rem; background: #F97316; color: #fff; box-shadow: 0 4px 12px rgba(249, 115, 22, 0.3);">
					<i class="bi bi-plus-lg me-1"></i> Thêm Nhân Viên
				</a>
			</form>
		</div>

		<%-- ===== GRID or EMPTY ===== --%>
		<c:choose>
			<c:when test="${empty requestScope.staffs}">
				<div class="text-center py-5 text-secondary">
					<div
						class="rounded-circle bg-light d-inline-flex align-items-center justify-content-center mb-3"
						style="width: 80px; height: 80px;">
						<i class="bi bi-person-badge"
							style="font-size: 2.5rem; color: #D1D5DB;"></i>
					</div>
					<p class="text-uppercase fw-bold small mb-0"
						style="letter-spacing: 0.12em;">Chưa có nhân viên nào</p>
				</div>
			</c:when>
			<c:otherwise>
				<div class="row row-cols-1 row-cols-md-2 row-cols-lg-3 g-4">
					<c:forEach items="${requestScope.staffs}" var="staff">
						<div class="col">
							<div class="staff-card card h-100 shadow-sm">
								<div class="card-body p-4">

									<%-- ROW 1: Avatar + Three-dot menu --%>
									<div
										class="d-flex align-items-start justify-content-between mb-3">

										<%-- Avatar + status dot --%>
										<div class="position-relative d-inline-block">
											<a	style="text-decoration: none;"
												href="${pageContext.request.contextPath}/owner/staffs/view/${staff.accountId}">
												<c:choose>
													<c:when test="${not empty staff.avatarPath}">
														<img
															src="${pageContext.request.contextPath}/${staff.avatarPath}"
															alt="${staff.fullName}" class="staff-avatar"
															style="cursor: pointer;">
													</c:when>
													<c:otherwise>
														<div class="staff-avatar-placeholder"
															style="cursor: pointer;">${not empty staff.fullName ? staff.fullName.substring(0,1).toUpperCase() : '?'}
														</div>
													</c:otherwise>
												</c:choose>
											</a>
											<div
												class="status-dot ${staff.isActive == true ? 'active' : 'inactive'}"></div>
										</div>

										<%-- ⋯ Three-dot menu --%>
										<div class="position-relative">
											<button onclick="toggleMenu(event,'menu-${staff.accountId}')"
												class="btn btn-link text-secondary p-1 rounded-2"
												style="font-size: 1.25rem; line-height: 1;">
												<i class="bi bi-three-dots"></i>
											</button>
											<div id="menu-${staff.accountId}"
												class="position-absolute bg-white border d-none p-2"
												style="right: 0; top: calc(100% + 0.375rem); min-width: 200px; border-radius: 1rem; box-shadow: 0 12px 32px rgba(0, 0, 0, 0.14); z-index: 50;">

												<a
													href="${pageContext.request.contextPath}/owner/staffs/view/${staff.accountId}"
													class="menu-item"> <span class="menu-icon"
													style="background: #EFF6FF;"> <i class="bi bi-eye"
														style="color: #3B82F6;"></i>
												</span> Xem Chi Tiết
												</a> <a
													href="${pageContext.request.contextPath}/owner/staffs/edit/${staff.accountId}"
													class="menu-item"> <span class="menu-icon"
													style="background: #EFF6FF;"> <i
														class="bi bi-pencil" style="color: #3B82F6;"></i>
												</span> Chỉnh Sửa
												</a> <a
													href="${pageContext.request.contextPath}/owner/staffs/status/${staff.accountId}"
													onclick="return confirm('${staff.isActive ? 'Vô hiệu hoá nhân viên này?' : 'Kích hoạt lại tài khoản này?'}')"
													class="menu-item"
													style="color:${staff.isActive ? '#D97706' : '#16A34A'};">
													<span class="menu-icon"
													style="background:${staff.isActive ? '#FEF3C7' : '#F0FDF4'};">
														<i
														class="bi ${staff.isActive ? 'bi-person-slash' : 'bi-person-check'}"></i>
												</span> ${staff.isActive ? 'Vô Hiệu Hoá' : 'Kích Hoạt'}
												</a> <a
													href="${pageContext.request.contextPath}/owner/staffs/reset-password/${staff.accountId}"
													onclick="return confirm('Gửi link đặt lại mật khẩu đến ' + '${staff.email}' + '?')"
													class="menu-item"> <span class="menu-icon"
													style="background: #F5F3FF;"> <i class="bi bi-key"
														style="color: #7C3AED;"></i>
												</span> Đặt Lại Mật Khẩu
												</a>

												<hr class="my-1 mx-2">

												<a href="#"
													onclick="deleteStaff('${staff.accountId}','${staff.fullName}');return false;"
													class="menu-item danger"> <span class="menu-icon"
													style="background: #FEF2F2;"> <i class="bi bi-trash"
														style="color: #DC2626;"></i>
												</span> Xóa
												</a>
											</div>
										</div>
									</div>

									<%-- ROW 2: Name + Role --%>
									<div class="mb-3">
										<h3 class="fw-black mb-1"
											style="font-size: 1.125rem; line-height: 1.3;">
											<a
												href="${pageContext.request.contextPath}/owner/staffs/view/${staff.accountId}"
												class="text-decoration-none" style="color: #111827;">
												${staff.fullName} </a>
										</h3>
										<p class="text-uppercase fw-black mb-0"
											style="font-size: 0.6875rem; color: #9CA3AF; letter-spacing: 0.1em;">
											${not empty staff.role ? staff.role : '—'}</p>
									</div>

									<%-- ROW 3: Email + Phone --%>
									<div class="d-flex flex-column gap-2 mb-3">
										<div class="d-flex align-items-center gap-2">
											<div class="icon-box">
												<i class="bi bi-envelope" style="font-size: 0.875rem;"></i>
											</div>
											<span class="text-secondary fw-semibold text-truncate"
												style="font-size: 0.8125rem;"> ${not empty staff.email ? staff.email : '—'}
											</span>
										</div>
										<div class="d-flex align-items-center gap-2">
											<div class="icon-box">
												<i class="bi bi-telephone" style="font-size: 0.875rem;"></i>
											</div>
											<span class="text-secondary fw-semibold"
												style="font-size: 0.8125rem;"> ${not empty staff.phone ? staff.phone : '—'}
											</span>
										</div>
									</div>


									<%-- ROW 4: Ngày Vào --%>
									<div class="info-block d-grid gap-2"
										style="grid-template-columns: 1fr 1fr;">
										<div>
											<p class="info-label">Ngày Vào</p>
											<p class="info-value">${not empty staff.createdAt ? staff.createdAt : '—'}</p>
										</div>
									</div>

								</div>
							</div>
						</div>
					</c:forEach>
				</div>

				<%-- PAGINATION --%>
				<c:if test="${totalPages >= 1}">
					<div class="d-flex justify-content-center mt-4">
						<ul class="pagination mb-0">
							<li class="page-item ${currentPage == 1 ? 'disabled' : ''}">
								<a class="page-link"
								href="?page=${currentPage - 1}&keyword=${requestScope.keyword}&isActive=${requestScope.isActiveFilter}&facilityId=${requestScope.facilityFilter}">&laquo;</a>
							</li>
							<c:forEach begin="1" end="${totalPages}" var="p">
								<li class="page-item ${p == currentPage ? 'active' : ''}">
									<a class="page-link"
									href="?page=${p}&keyword=${requestScope.keyword}&isActive=${requestScope.isActiveFilter}&facilityId=${requestScope.facilityFilter}">${p}</a>
								</li>
							</c:forEach>
							<li
								class="page-item ${currentPage == totalPages ? 'disabled' : ''}">
								<a class="page-link"
								href="?page=${currentPage + 1}&keyword=${requestScope.keyword}&isActive=${requestScope.isActiveFilter}&facilityId=${requestScope.facilityFilter}">&raquo;</a>
							</li>
						</ul>
					</div>
				</c:if>

			</c:otherwise>
		</c:choose>

	</div>
</div>

<script>
// Three-dot menu toggle
function toggleMenu(e, menuId) {
    e.stopPropagation();
    document.querySelectorAll('[id^="menu-"]').forEach(m => {
        if (m.id !== menuId) m.classList.add('d-none');
    });
    document.getElementById(menuId).classList.toggle('d-none');
}
document.addEventListener('click', () => {
    document.querySelectorAll('[id^="menu-"]').forEach(m => m.classList.add('d-none'));
});

// Delete confirm
function deleteStaff(id, name) {
    if (confirm('Bạn có chắc chắn muốn xóa nhân viên "' + name + '"?\nHành động này không thể hoàn tác.')) {
        window.location.href = '${pageContext.request.contextPath}/owner/staffs/delete/' + id;
    }
}
</script>
