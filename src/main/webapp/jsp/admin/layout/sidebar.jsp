<!-- sidebar.jsp (Admin) -->

<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<div class="sidebar">
	<div class="sidebar-header">
		<a href="${pageContext.request.contextPath}/admin/dashboard"
			class="sidebar-brand"> <i class="bi bi-circle-fill text-accent" style="color:#A3E635"></i>
			<span style="color:#A3E635">BCB Admin</span>
		</a>
	</div>

	<div class="sidebar-nav">

		<%-- Dashboard --%>
		<a href="${pageContext.request.contextPath}/admin/dashboard"
			class="nav-link ${pageContext.request.requestURI.contains('/dashboard') && !pageContext.request.requestURI.contains('/accounts') ? 'active' : ''}">
			<i class="bi bi-speedometer2"></i> <span>Dashboard</span>
		</a>

		<%-- Account Management --%>
		<a href="${pageContext.request.contextPath}/admin/accounts/list"
			class="nav-link ${pageContext.request.requestURI.contains('/accounts') ? 'active' : ''}">
			<i class="bi bi-people"></i> <span>Quản Lý Tài Khoản</span>
		</a>


		<%-- Settings --%>
		<a href="#"
			class="nav-link ${pageContext.request.requestURI.contains('/settings') ? 'active' : ''}">
			<i class="bi bi-gear"></i> <span>Cài Đặt</span>
		</a>

		<%-- Logout --%>
		<div class="mt-auto pt-4">
			<a href="${pageContext.request.contextPath}/auth/logout"
				class="nav-link nav-logout border-t border-gray-600 mt-3 pt-3">
				<i class="bi bi-box-arrow-right"></i> <span>Đăng Xuất</span>
			</a>
		</div>

	</div>
</div>
