<!-- account-list.jsp -->

<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<!-- CSS -->
<link rel="stylesheet"
	href="${pageContext.request.contextPath}/assets/css/account-management.css">

<%@ include file="../layout/layout.jsp"%>
<%@ include file="../layout/sidebar.jsp"%>

<div class="main-content">
	<%@ include file="../layout/header.jsp"%>
	<div class="content-area">

		<%-- ===== PAGE HEADER ===== --%>
		<div class="mb-4">
			<h1 class="fw-black mb-1"
				style="font-size: 1.75rem; color: #111827; letter-spacing: -0.02em;">
				Quản Lý Tài Khoản</h1>
			<p class="mb-0" style="font-size: 0.875rem; color: #9CA3AF;">
				Xem, tìm kiếm, chỉnh sửa và quản lý tất cả tài khoản trong hệ thống
			</p>
		</div>

		<%-- ===== ALERTS ===== --%>
		<c:if test="${param.deleted == '1'}">
			<div class="alert alert-success d-flex align-items-center gap-2 rounded-3 mb-3" role="alert">
				<i class="bi bi-check-circle-fill"></i> Xóa tài khoản thành công!
			</div>
		</c:if>
		<c:if test="${param.error == 'delete_failed'}">
			<div class="alert alert-danger d-flex align-items-center gap-2 rounded-3 mb-3" role="alert">
				<i class="bi bi-exclamation-circle-fill"></i> Xóa tài khoản thất bại. Tài khoản có thể đang liên kết với dữ liệu khác.
			</div>
		</c:if>
		<c:if test="${not empty requestScope.error}">
			<div class="alert alert-danger d-flex align-items-center gap-2 rounded-3 mb-3" role="alert">
				<i class="bi bi-exclamation-circle-fill"></i> ${requestScope.error}
			</div>
		</c:if>

		<%-- ===== STATS ROW ===== --%>
		<div class="row g-3 mb-4">
			<div class="col-md-3">
				<div class="am-stat-card">
					<div class="am-stat-icon" style="background:#DBEAFE; color:#1E40AF;">
						<i class="bi bi-people-fill"></i>
					</div>
					<div>
						<div class="am-stat-value">${requestScope.totalRecords}</div>
						<div class="am-stat-label">Tổng tài khoản</div>
					</div>
				</div>
			</div>
		</div>

		<%-- ===== SEARCH + FILTER BAR ===== --%>
		<div class="bg-white rounded-3 border mb-3 px-3 py-3"
			style="border-color: #F3F4F6 !important; box-shadow: 0 1px 4px rgba(0, 0, 0, 0.05);">
			<form method="GET"
				action="${pageContext.request.contextPath}/admin/accounts/list"
				class="d-flex gap-3 align-items-center flex-wrap">

				<%-- Search keyword --%>
				<div class="flex-grow-1 position-relative" style="min-width: 200px;">
					<i class="bi bi-search position-absolute text-secondary"
						style="left: 0.875rem; top: 50%; transform: translateY(-50%); font-size: 0.875rem; pointer-events: none;"></i>
					<input type="text" name="keyword" value="${requestScope.keyword}"
						class="form-control rounded-3"
						placeholder="Tìm kiếm theo tên, email, SĐT..."
						style="padding-left: 2.5rem; font-size: 0.875rem;">
				</div>

				<%-- Role filter --%>
				<div style="position: relative; min-width: 160px;">
					<select name="role" class="form-select rounded-3 fw-bold"
						style="font-size: 0.875rem; color: #374151; appearance: none; padding-right: 2.5rem;">
						<option value="" ${empty requestScope.roleFilter ? 'selected' : ''}>Tất Cả Vai Trò</option>
						<option value="ADMIN" ${'ADMIN' == requestScope.roleFilter ? 'selected' : ''}>Admin</option>
						<option value="OWNER" ${'OWNER' == requestScope.roleFilter ? 'selected' : ''}>Owner</option>
						<option value="STAFF" ${'STAFF' == requestScope.roleFilter ? 'selected' : ''}>Staff</option>
						<option value="CUSTOMER" ${'CUSTOMER' == requestScope.roleFilter ? 'selected' : ''}>Customer</option>
					</select>
					<i class="bi bi-chevron-down position-absolute text-secondary"
						style="right: 0.875rem; top: 50%; transform: translateY(-50%); font-size: 0.75rem; pointer-events: none;"></i>
				</div>

				<%-- Status filter --%>
				<div style="position: relative; min-width: 160px;">
					<select name="status" class="form-select rounded-3 fw-bold"
						style="font-size: 0.875rem; color: #374151; appearance: none; padding-right: 2.5rem;">
						<option value="" ${empty requestScope.statusFilter ? 'selected' : ''}>Tất Cả Trạng Thái</option>
						<option value="true" ${'true' == requestScope.statusFilter ? 'selected' : ''}>Hoạt Động</option>
						<option value="false" ${'false' == requestScope.statusFilter ? 'selected' : ''}>Không Hoạt Động</option>
					</select>
					<i class="bi bi-chevron-down position-absolute text-secondary"
						style="right: 0.875rem; top: 50%; transform: translateY(-50%); font-size: 0.75rem; pointer-events: none;"></i>
				</div>

				<%-- Submit --%>
				<button type="submit" class="btn btn-brand fw-bold rounded-3"
					style="padding: 0.5rem 1.25rem; font-size: 0.875rem; white-space: nowrap;">
					<i class="bi bi-search me-1"></i> Tìm kiếm
				</button>
			</form>
		</div>

		<%-- ===== TABLE or EMPTY ===== --%>
		<c:choose>
			<c:when test="${empty requestScope.accounts}">
				<div class="text-center py-5 text-secondary">
					<div class="rounded-circle bg-light d-inline-flex align-items-center justify-content-center mb-3"
						style="width: 80px; height: 80px;">
						<i class="bi bi-person-x" style="font-size: 2.5rem; color: #D1D5DB;"></i>
					</div>
					<p class="text-uppercase fw-bold small mb-0"
						style="letter-spacing: 0.12em;">Không tìm thấy tài khoản nào</p>
				</div>
			</c:when>
			<c:otherwise>
				<div class="bg-white rounded-3 border overflow-hidden"
					style="border-color: #F3F4F6 !important; box-shadow: 0 1px 4px rgba(0, 0, 0, 0.05);">
					<div class="table-responsive">
						<table class="table table-hover mb-0 am-table">
							<thead>
								<tr>
									<th style="width:50px;">#</th>
									<th>Tài khoản</th>
									<th>Email</th>
									<th>SĐT</th>
									<th>Vai trò</th>
									<th>Trạng thái</th>
									<th>Ngày tạo</th>
									<th style="width:120px;">Thao tác</th>
								</tr>
							</thead>
							<tbody>
								<c:forEach items="${requestScope.accounts}" var="acc" varStatus="st">
									<tr>
										<td class="fw-bold text-secondary">${acc.accountId}</td>
										<td>
											<div class="d-flex align-items-center gap-2">
												<c:choose>
													<c:when test="${not empty acc.avatarPath}">
														<img src="${pageContext.request.contextPath}/${acc.avatarPath}"
															alt="${acc.fullName}" class="am-avatar">
													</c:when>
													<c:otherwise>
														<div class="am-avatar-placeholder">
															${not empty acc.fullName ? acc.fullName.substring(0,1).toUpperCase() : '?'}
														</div>
													</c:otherwise>
												</c:choose>
												<div>
													<div class="fw-bold" style="font-size:0.875rem; color:#111827;">
														${acc.fullName}
													</div>
												</div>
											</div>
										</td>
										<td style="font-size:0.8125rem; color:#6B7280;">${acc.email}</td>
										<td style="font-size:0.8125rem; color:#6B7280;">${not empty acc.phone ? acc.phone : '—'}</td>
										<td>
											<span class="am-role-badge am-role-${acc.role.toLowerCase()}">${acc.role}</span>
										</td>
										<td>
											<span class="am-status-badge ${acc.isActive ? 'active' : 'inactive'}">
												<span class="am-status-dot ${acc.isActive ? 'active' : 'inactive'}"></span>
												${acc.isActive ? 'Hoạt động' : 'Ngừng'}
											</span>
										</td>
										<td style="font-size:0.8125rem; color:#6B7280;">
											${not empty acc.createdAt ? acc.createdAt.toString().substring(0,10) : '—'}
										</td>
										<td>
											<div class="d-flex gap-1">
												<a href="${pageContext.request.contextPath}/admin/accounts/view/${acc.accountId}"
													class="btn btn-sm btn-outline-primary rounded-2"
													title="Xem chi tiết" style="padding:0.25rem 0.5rem;">
													<i class="bi bi-eye"></i>
												</a>
												<a href="${pageContext.request.contextPath}/admin/accounts/toggle/${acc.accountId}?redirect=list"
													class="btn btn-sm rounded-2 ${acc.isActive ? 'btn-outline-warning' : 'btn-outline-success'}"
													title="${acc.isActive ? 'Vô hiệu hóa' : 'Kích hoạt'}"
													style="padding:0.25rem 0.5rem;"
													onclick="return confirm('Bạn có chắc muốn ${acc.isActive ? 'vô hiệu hóa' : 'kích hoạt'} tài khoản này?');">
													<i class="bi bi-${acc.isActive ? 'pause-circle' : 'play-circle'}"></i>
												</a>
												<a href="${pageContext.request.contextPath}/admin/accounts/delete/${acc.accountId}"
													class="btn btn-sm btn-outline-danger rounded-2"
													title="Xóa vĩnh viễn" style="padding:0.25rem 0.5rem;"
													onclick="return confirm('CẢNH BÁO: Bạn có chắc muốn XÓA VĨNH VIỄN tài khoản này? Hành động không thể hoàn tác!');">
													<i class="bi bi-trash"></i>
												</a>
											</div>
										</td>
									</tr>
								</c:forEach>
							</tbody>
						</table>
					</div>
				</div>

				<%-- PAGINATION --%>
				<c:if test="${totalPages > 1}">
					<div class="d-flex justify-content-between align-items-center mt-4">
						<div style="font-size:0.8125rem; color:#6B7280;">
							Trang ${currentPage} / ${totalPages} (${totalRecords} tài khoản)
						</div>
						<ul class="pagination mb-0">
							<li class="page-item ${currentPage == 1 ? 'disabled' : ''}">
								<a class="page-link"
								href="?page=${currentPage - 1}&keyword=${requestScope.keyword}&role=${requestScope.roleFilter}&status=${requestScope.statusFilter}">&laquo;</a>
							</li>
							<c:forEach begin="1" end="${totalPages}" var="p">
								<li class="page-item ${p == currentPage ? 'active' : ''}">
									<a class="page-link"
									href="?page=${p}&keyword=${requestScope.keyword}&role=${requestScope.roleFilter}&status=${requestScope.statusFilter}">${p}</a>
								</li>
							</c:forEach>
							<li class="page-item ${currentPage == totalPages ? 'disabled' : ''}">
								<a class="page-link"
								href="?page=${currentPage + 1}&keyword=${requestScope.keyword}&role=${requestScope.roleFilter}&status=${requestScope.statusFilter}">&raquo;</a>
							</li>
						</ul>
					</div>
				</c:if>

			</c:otherwise>
		</c:choose>

	</div>
</div>
