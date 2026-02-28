<!-- staff-list.jsp -->

<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<!-- CSS -->
<link rel="stylesheet"
	href="${pageContext.request.contextPath}/assets/css/staff-list.css">
<link rel="stylesheet"
	href="${pageContext.request.contextPath}/assets/css/reset-password.css">

<%@ include file="../layout/layout.jsp"%>
<%@ include file="../layout/sidebar.jsp"%>

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
				<button type="button" onclick="openHireModal()"
					class="btn fw-bold rounded-3 text-nowrap"
					style="padding: 0.5rem 1.25rem; font-size: 0.875rem; background: #A3E635; color: #000;">
					<i class="bi bi-plus-lg me-1"></i> Thêm Nhân Viên
				</button>
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
											<a style="text-decoration: none;"
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
												</a>

												<button type="button" class="menu-item"
													data-account-id="${staff.accountId}"
													data-staff-name="${staff.fullName}"
													onclick="openResetModal(this.dataset.accountId, this.dataset.staffName)">

													<span class="menu-icon" style="background: #F5F3FF;">
														<i class="bi bi-key" style="color: #7C3AED;"></i>
													</span> Đặt Lại Mật Khẩu
												</button>

												<hr class="my-1 mx-2">

												<a
													href="${pageContext.request.contextPath}/owner/staffs/toggle/${staff.accountId}?redirect=list"
													class="menu-item"
													style="color:${staff.isActive ? '#DC2626' : '#16A34A'};">
													<span class="menu-icon"
													style="background:${staff.isActive ? '#FEF2F2' : '#F0FDF4'};">
														<i
														class="bi ${staff.isActive ? 'bi-trash' : 'bi-person-check'}"></i>
												</span> ${staff.isActive ? 'Xóa Nhân Viên' : 'Khôi Phục Nhân Viên'}
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
											<p class="info-value">${not empty staff.createdAt ? staff.createdAt.toString().substring(0,10) : '—'}</p>
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

		<!-- HIRE STAFF MODAL -->
		<div class="hs-modal-bg" id="hs-modal">
			<div class="hs-modal">

				<!-- Header -->
				<div class="hs-modal-hd">
					<div class="hs-modal-hd-left">
						<div class="hs-modal-hd-icon">
							<i class="bi bi-plus-lg"></i>
						</div>
						<h3 class="hs-modal-title">Thêm Nhân Viên Mới</h3>
					</div>
				</div>

				<!-- Body -->
				<div class="hs-modal-bd">

					<!-- CỘT TRÁI: Basic Info -->
					<div>
						<div class="hs-section-hd">
							<div class="hs-section-icon ci-blue">
								<i class="bi bi-person-fill"></i>
							</div>
							<h4 class="hs-section-title">Thông tin cơ bản</h4>
						</div>

						<div class="hs-form-group">
							<div>
								<label class="hs-form-label">Họ và tên *</label> <input
									type="text" id="hs-fullName" class="hs-form-input"
									placeholder="Nguyễn Văn A" autocomplete="off">
							</div>
							<div>
								<label class="hs-form-label">Email *</label> <input type="email"
									id="hs-email" class="hs-form-input" placeholder="staff@bcb.com"
									autocomplete="off">
							</div>
							<div>
								<label class="hs-form-label">Số điện thoại *</label> <input
									type="tel" id="hs-phone" class="hs-form-input"
									placeholder="09xx xxx xxx" autocomplete="off">
							</div>

						</div>
					</div>

					<!-- CỘT PHẢI: Assign Location -->
					<div>
						<div class="hs-section-hd">
							<div class="hs-section-icon ci-green">
								<i class="bi bi-building-fill"></i>
							</div>
							<h4 class="hs-section-title">Phân công cơ sở</h4>
						</div>

						<div class="hs-search-wrap">
							<i class="bi bi-search hs-si"></i> <input type="text"
								class="hs-search" id="hs-loc-search" placeholder="Tìm cơ sở..."
								oninput="renderHsLocList(this.value)">
						</div>

						<div class="hs-loc-meta">
							<span class="hs-selected-count"> <span id="hs-sel-count">0</span>
								đã chọn
							</span>
						</div>

						<div class="hs-loc-list" id="hs-loc-list"></div>
					</div>

				</div>

				<!-- Footer -->
				<div class="hs-modal-ft">
					<button class="hs-btn-discard" onclick="closeHireModal()">Hủy</button>
					<button class="hs-btn-send" id="hs-send-btn"
						onclick="submitHireForm()">
						<i class="bi bi-send-fill"></i> <span id="hs-send-txt">Gửi
							Lời Mời</span>
					</button>
				</div>

			</div>
		</div>

	</div>
</div>

<!-- RESET PASSWORD -->
<%@ include file="/jsp/owner/staffs/reset-password.jsp"%>


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

window.SD_CONTEXT_PATH  = "${pageContext.request.contextPath}";

<%-- Tất cả facility của owner --%>
window.SD_ALL_FACILITIES  = [
    <c:forEach items="${allFacilities}" var="f" varStatus="st">
    {
        id:   "${f.facilityId}",
        name: "<c:out value='${f.name}'/>",
        addr: "<c:out value='${f.address}'/>"
    }<c:if test="${!st.last}">,</c:if>
    </c:forEach>
];
</script>

<!-- JS load -->
<script
	src="${pageContext.request.contextPath}/assets/js/staff/toggle-status.js"></script>

<script
	src="${pageContext.request.contextPath}/assets/js/validation/hire-form-validation.js"></script>

<script
	src="${pageContext.request.contextPath}/assets/js/staff/staff-list.js"></script>

<script
	src="${pageContext.request.contextPath}/assets/js/staff/reset-password.js"></script>
