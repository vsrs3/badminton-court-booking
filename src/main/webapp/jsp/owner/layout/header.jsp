<!-- header.jsp -->

<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<nav class="bg-white border-bottom sticky-top shadow-sm"
	style="height:64px;z-index:30;">
	<div class="d-flex align-items-center justify-content-between h-100 px-4">

		<%-- Brand label --%>
		<span class="d-none d-md-block text-uppercase fw-semibold text-secondary"
			style="font-size:0.75rem;letter-spacing:0.12em;">
			Trung tâm quản lý chủ sân
		</span>

		<%-- Right side --%>
		<div class="d-flex align-items-center gap-3 ms-auto">

			<%-- Notification bell --%>
			<div class="position-relative">
				<button class="btn btn-link text-secondary p-2 rounded-circle"
					style="line-height:1;"
					onmouseover="this.style.color='var(--color-green-brand)';this.style.background='var(--color-green-50)'"
					onmouseout="this.style.color='';this.style.background=''">
					<i class="bi bi-bell fs-5"></i>
				</button>
				<c:if test="${sessionScope.unreadNotifications > 0}">
					<span class="position-absolute badge rounded-pill bg-danger border border-white fw-bold"
						style="font-size:0.5625rem;top:4px;right:4px;padding:2px 5px;">
						${sessionScope.unreadNotifications}
					</span>
				</c:if>
			</div>

			<%-- Divider --%>
			<div class="border-start" style="height:32px;"></div>

			<%-- User info + Avatar --%>
			<div class="d-flex align-items-center gap-2">
				<div class="text-end d-none d-sm-block">
					<p class="fw-bold mb-0 lh-1" style="font-size:0.875rem;color:var(--color-gray-800);">Court Owner</p>
					<p class="mb-0 fw-semibold" style="font-size:0.75rem;color:var(--color-green-600);">Proprietor</p>
				</div>
				<img src="${pageContext.request.contextPath}/uploads/${sessionScope.account.avatarPath}"
					alt="Avatar"
					class="rounded-circle object-fit-cover border"
					style="width:40px;height:40px;border-color:var(--color-green-100)!important;background:var(--color-green-50);box-shadow:0 1px 4px rgba(0,0,0,0.1);">
			</div>

		</div>
	</div>
</nav>
