<%-- staff-detail.jsp --%>

<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>

<%@ include file="../layout/layout.jsp"%>
<%@ include file="../layout/sidebar.jsp"%>

<link rel="stylesheet"
	href="${pageContext.request.contextPath}/assets/css/staff-detail.css">

<div class="main-content">
	<%@ include file="../layout/header.jsp"%>

	<div class="content-area p-4">
		<div class="sd-wrapper">

			<%--TOP BAR --%>
			<div
				class="d-flex flex-column flex-lg-row justify-content-between
                        align-items-lg-center gap-3 mb-4">

				<%-- Avatar + tên --%>
				<div class="d-flex align-items-center gap-3">

					<%-- Bọc avatar trong wrapper để có hover overlay --%>
					<div class="sd-avatar-wrap" id="sd-avatar-wrap"
						onclick="openAvatarModal()" title="Đổi ảnh đại diện">
						<c:choose>
							<c:when test="${not empty staff.avatarPath}">
								<div class="sd-avatar">
									<img id="sd-avatar-img"
										src="${pageContext.request.contextPath}/${staff.avatarPath}"
										alt="${staff.fullName}">
								</div>
							</c:when>
							<c:otherwise>
								<div class="sd-avatar-initials" id="sd-avatar-topbar-initials">
									<c:choose>
										<c:when test="${not empty staff.fullName}">
                                            ${staff.fullName.substring(0,1).toUpperCase()}
                                        </c:when>
										<c:otherwise>?</c:otherwise>
									</c:choose>
								</div>
							</c:otherwise>
						</c:choose>

						<%-- Hover overlay camera --%>
						<div class="sd-avatar-overlay">
							<!-- <i class="bi bi-camera-fill"></i> -->
							<!-- <span>Đổi ảnh</span> -->
						</div>
					</div>

					<div>
						<div class="d-flex align-items-center flex-wrap gap-2 mb-1">
							<h1 class="sd-name">${staff.fullName}</h1>
							<span class="sd-status ${staff.isActive ? 'active' : 'inactive'}">
								<span class="sd-dot ${staff.isActive ? 'active' : 'inactive'}"></span>
								${staff.isActive ? 'Đang hoạt động' : 'Ngừng hoạt động'}
							</span>
						</div>
						<p class="sd-role">${staff.role}</p>
					</div>
				</div>

				<%-- Action buttons --%>
				<div class="d-flex flex-wrap gap-2">
					<button onclick="history.back()" class="sd-btn back">
						<i class="bi bi-arrow-left"></i> Quay lại
					</button>
					<button onclick="switchTab('edit')" class="sd-btn edit"
						id="sd-btn-edit">
						<i class="bi bi-pencil"></i> Sửa thông tin
					</button>
					<button onclick="switchTab('overview')" class="sd-btn edit d-none"
						id="sd-btn-cancel">
						<i class="bi bi-x"></i> Hủy chỉnh sửa
					</button>
					<button
						onclick="openResetModal(${staff.accountId}, '${staff.fullName}')"
						class="sd-btn changePass">
						<i class="bi bi-shield-lock"></i> Đặt lại mật khẩu
					</button>
					<button onclick="toggleStaff()"
						class="sd-btn ${staff.isActive ? 'term' : 'restore'}">
						<i class="bi bi-person-${staff.isActive ? 'x' : 'check'}"></i>
						${staff.isActive ? 'Xóa Nhân Viên' : 'Khôi Phục Nhân Viên'}
					</button>
				</div>

			</div>

			<%-- TAB NAV --%>
			<div class="sd-tabs">
				<button class="sd-tab active" id="sd-tab-ov"
					onclick="switchTab('overview')">
					<i class="bi bi-grid me-1"></i>Tổng quan
				</button>
				<button class="sd-tab" id="sd-tab-ed" onclick="switchTab('edit')">
					<i class="bi bi-pencil-square me-1"></i>Sửa thông tin
				</button>
			</div>

			<%--OVERVIEW PANEL--%>
			<div id="sd-panel-ov">
				<div class="row g-4">

					<%-- Thông tin liên hệ --%>
					<div class="col-lg-8">
						<div class="sd-card h-100">
							<div class="sd-card-header">
								<div class="sd-card-icon ci-blue">
									<i class="bi bi-envelope-fill"></i>
								</div>
								<h2 class="sd-card-title">Thông tin liên hệ</h2>
							</div>
							<div class="row g-4">
								<div class="col-md-6 d-flex flex-column gap-4">
									<div>
										<span class="sd-info-label">Địa chỉ email</span>
										<p class="sd-info-value">${staff.email}</p>
									</div>
									<div>
										<span class="sd-info-label">Số điện thoại</span>
										<p class="sd-info-value">${staff.phone}</p>
									</div>
								</div>
								<div class="col-md-6 d-flex flex-column gap-4">
									<div>
										<span class="sd-info-label">Ngày nhận việc</span>
										<p class="sd-info-value">
											<c:choose>
												<c:when test="${not empty staff.createdAt}">
                                                    ${requestScope.staff.createdAt.toString().substring(0,10)}
                                                </c:when>
												<c:otherwise>—</c:otherwise>
											</c:choose>
										</p>
									</div>
									<div>
										<span class="sd-info-label">Mã nhân viên</span>
										<p class="sd-info-value">#${staff.accountId}</p>
									</div>
								</div>
							</div>
						</div>
					</div>

					<%-- Giới thiệu --%>
					<div class="col-lg-4">
						<div class="sd-card h-100 d-flex flex-column">
							<div class="sd-card-header">
								<div class="sd-card-icon ci-yellow">
									<i class="bi bi-person-lines-fill"></i>
								</div>
								<h2 class="sd-card-title">Giới thiệu</h2>
							</div>
							<p class="flex-grow-1 mb-0"
								style="font-size: .875rem; font-weight: 500; color: var(--color-gray-600); line-height: 1.75;">
								Đội ngũ nhân viên BCB luôn làm việc với tinh thần chuyên nghiệp,
								tận tâm và trách nhiệm cao nhằm mang đến trải nghiệm dịch vụ tốt
								nhất cho khách hàng</p>
						</div>
					</div>

					<%-- Cơ sở phụ trách --%>
					<div class="col-12">
						<div class="sd-card">
							<div
								class="d-flex flex-column flex-md-row align-items-md-center
                                        justify-content-between gap-3 pb-3 mb-4"
								style="border-bottom: 1px solid var(--color-gray-100);">
								<div class="d-flex align-items-center flex-wrap gap-2">
									<div class="sd-card-icon ci-green">
										<i class="bi bi-building-fill"></i>
									</div>
									<h2 class="sd-card-title">Cơ sở phụ trách</h2>
									<span class="sd-count-badge"> <c:choose>
											<c:when test="${not empty staffFacilities}">
                                                ${staffFacilities.size()} cơ sở
                                            </c:when>
											<c:otherwise>Chưa phân công</c:otherwise>
										</c:choose>
									</span>
								</div>
							</div>

							<c:choose>
								<c:when test="${not empty requestScope.staffFacilities}">
									<div class="row g-3">
										<c:forEach items="${staffFacilities}" var="facility">
											<div class="col-md-6 col-xl-4">
												<div class="sd-site-card">
													<div
														class="d-flex justify-content-between
                                                                align-items-start mb-2">
														<div class="sd-site-icon">
															<i class="bi bi-building"></i>
														</div>
														<span class="sd-site-id"> ID:
															${facility.facilityId} </span>
													</div>
													<p class="sd-site-name">${facility.name}</p>
													<div class="d-flex align-items-start gap-1 sd-site-addr">
														<i class="bi bi-geo-alt-fill"
															style="font-size: .62rem; flex-shrink: 0; margin-top: 2px;"></i>
														<span>${facility.address}</span>
													</div>
												</div>
											</div>
										</c:forEach>
									</div>
								</c:when>
								<c:otherwise>
									<div class="text-center py-5">
										<i class="bi bi-building-x"
											style="font-size: 2rem; color: var(--color-gray-300);"></i>
										<p class="mt-2 mb-0"
											style="font-weight: 600; color: var(--color-gray-400); font-size: .875rem;">
											Nhân viên chưa được phân công cơ sở nào</p>
									</div>
								</c:otherwise>
							</c:choose>
						</div>
					</div>

				</div>
			</div>

			<%-- EDIT PANEL --%>
			<div id="sd-panel-ed" class="d-none">
				<div class="sd-card">
					<form id="sd-edit-form"
						action="${pageContext.request.contextPath}/owner/staffs/update"
						method="POST">

						<!-- staffId -->
						<input type="hidden" name="accountId" value="${staff.accountId}">

						<%-- Nếu allFacilities có dữ liệu ->lấy facilityId của phần tử đầu tiên --%>
						<%-- facilityId được JS set lại khi user click chọn --%>
						<input type="hidden" name="facilityId" id="selectedFacilityId"
							value="${not empty allFacilities ? allFacilities[0].facilityId : ''}">

						<div class="row g-4 g-xl-5">

							<%-- ── CỘT TRÁI: Thông tin cơ bản ── --%>
							<div class="col-md-6">
								<div class="sd-section-hd">
									<div class="sd-section-icon ci-blue">
										<i class="bi bi-person-fill"></i>
									</div>
									<h4 class="sd-section-title">Thông tin cơ bản</h4>
								</div>

								<div class="d-flex flex-column gap-3">
									<div>
										<label class="sd-form-label">Họ và tên *</label> <input
											type="text" name="fullName" class="sd-form-input"
											value="${staff.fullName}" required>
									</div>
									<div>
										<label class="sd-form-label">Email *</label> <input
											type="email" name="email" class="sd-form-input"
											value="${staff.email}" required>
									</div>
									<div>
										<label class="sd-form-label">Số điện thoại *</label> <input
											type="tel" name="phone" class="sd-form-input"
											value="${staff.phone}" required>
									</div>
								</div>
							</div>

							<%-- ── CỘT PHẢI: Phân công cơ sở ── --%>
							<div class="col-md-6">
								<div class="sd-section-hd">
									<div class="sd-section-icon ci-green">
										<i class="bi bi-building-fill"></i>
									</div>
									<h4 class="sd-section-title">Phân công cơ sở</h4>
								</div>

								<div class="sd-search-wrap">
									<i class="bi bi-search sd-si"></i> <input type="text"
										class="sd-search" id="sd-loc-search"
										placeholder="Tìm cơ sở..." oninput="renderLocList(this.value)">
								</div>
								<div class="sd-loc-list" id="sd-loc-list"></div>
							</div>

						</div>

						<%-- ── ACTION BAR ── --%>
						<div class="sd-edit-actions">
							<button type="reset" class="sd-btn-reset">
								<i class="bi bi-arrow-counterclockwise me-1"></i>Đặt lại
							</button>
							<button type="submit" class="sd-btn-save">
								<i class="bi bi-check-lg me-1"></i>Lưu tất cả thay đổi
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
<%-- Đóng main-content --%>

<!-- RESET PASSWORD -->
<%@ include file="/jsp/owner/staffs/reset-password.jsp"%>

<%--DATA BRIDGE  JSP → JS--%>
<script>
    window.SD_STAFF_NAME     = "${staff.fullName}";
    window.SD_ACCOUNT_ID     = "${staff.accountId}";
    window.SD_AVATAR_PATH    = "${staff.avatarPath}";
    window.SD_IS_ACTIVE 	 = "${staff.isActive}";
    window.SD_CONTEXT_PATH   = "${pageContext.request.contextPath}";

    <%-- facilityId(s) hiện tại của nhân viên --%>
    window.SD_ASSIGNED_IDS = ["${staffFacilityId}"];
    
    <%-- Tất cả facility của owner --%>
    window.SD_ALL_FACILITIES = [
        <c:forEach items="${allFacilities}" var="f" varStatus="st">
        {
            id:   "${f.facilityId}",
            name: "<c:out value='${f.name}'/>",
            addr: "<c:out value='${f.address}'/>"
        }<c:if test="${!st.last}">,</c:if>
        </c:forEach>
    ];
</script>

<!-- Link JS -->
<script
	src="${pageContext.request.contextPath}/assets/js/staff/staff-detail.js"></script>

<script
	src="${pageContext.request.contextPath}/assets/js/previewAvatar.js"></script>

<script
	src="${pageContext.request.contextPath}/assets/js/validation/staff-validation.js"></script>

<script
	src="${pageContext.request.contextPath}/assets/js/staff/toggle-status.js"></script>

<script
	src="${pageContext.request.contextPath}/assets/js/staff/reset-password.js"></script>


