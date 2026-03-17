<!-- sidebar.jsp -->

<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<div class="sidebar">
	<div class="sidebar-header">
		<a href="${pageContext.request.contextPath}/owner/dashboard"
			class="sidebar-brand"> <i class="bi bi-circle-fill text-accent" style="color:#A3E635"></i>
			<span style="color:#A3E635">BCB Court Owner</span>
		</a>
	</div>

	<div class="sidebar-nav">

		<%-- Dashboard --%>
		<a href="${pageContext.request.contextPath}/owner/dashboard"
			class="nav-link ${pageContext.request.requestURI.contains('/dashboard') ? 'active' : ''}">
			<i class="bi bi-speedometer2"></i> <span>Dashboard</span>
		</a>


		<%-- My Locations --%>
		<a href="${pageContext.request.contextPath}/owner/facility/list"
			class="nav-link ${pageContext.request.requestURI.contains('/facility') ? 'active' : ''}">
			<i class="bi bi-building"></i> <span>Danh Sách Địa Điểm</span>
		</a>

		<%-- Staff Management --%>
		<a href="${pageContext.request.contextPath}/owner/staffs/list"
			class="nav-link ${pageContext.request.requestURI.contains('/staff') ? 'active' : ''}">
			<i class="bi bi-person-badge"></i> <span>Quản Lý Nhân Viên</span>
		</a>

		<%-- Voucher Management --%>
		<a href="${pageContext.request.contextPath}/owner/vouchers/dashboard"
			class="nav-link ${pageContext.request.requestURI.contains('/voucher') ? 'active' : ''}">
			<i class="bi bi-ticket-perforated"></i> <span>Quản Lý Voucher</span>
		</a>


        <%-- Inventory Management --%>
       <a href="${pageContext.request.contextPath}/owner/inventory"
            class="nav-link ${pageContext.request.requestURI.contains('/inventory') ? 'active' : ''}">
            <i class="bi bi-box-seam"></i>
            <span>Quản Lý Dụng Cụ</span>
       </a>
			<%-- Community / Blog --%>
			<a href="${pageContext.request.contextPath}/blogs"
			   class="nav-link ${pageContext.request.requestURI.contains('/blogs') ? 'active' : ''}">
				<i class="bi bi-chat-dots"></i>
				<span>Cộng Đồng</span>
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