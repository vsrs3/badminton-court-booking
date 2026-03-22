<!-- sidebar.jsp -->

<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<div class="sidebar">
    <div class="sidebar-header">
        <a href="${pageContext.request.contextPath}/owner/dashboard" class="sidebar-brand">
            <i class="bi bi-circle-fill text-accent" style="color:#A3E635"></i>
            <span style="color:#A3E635">BCB Chủ sân</span>
        </a>
    </div>

    <div class="sidebar-nav">
        <a href="${pageContext.request.contextPath}/owner/dashboard"
           class="nav-link ${(pageContext.request.requestURI.contains('/dashboard') || pageContext.request.requestURI.contains('/rental-report')) ? 'active' : ''}">
            <i class="bi bi-speedometer2"></i>
            <span>Bảng điều khiển</span>
        </a>

        <a href="${pageContext.request.contextPath}/owner/facility/list"
           class="nav-link ${pageContext.request.requestURI.contains('/facility') ? 'active' : ''}">
            <i class="bi bi-building"></i>
            <span>Danh sách địa điểm</span>
        </a>

        <a href="${pageContext.request.contextPath}/owner/staffs/list"
           class="nav-link ${pageContext.request.requestURI.contains('/staff') ? 'active' : ''}">
            <i class="bi bi-person-badge"></i>
            <span>Quản lý nhân viên</span>
        </a>

        <a href="${pageContext.request.contextPath}/owner/vouchers/dashboard"
           class="nav-link ${pageContext.request.requestURI.contains('/voucher') ? 'active' : ''}">
            <i class="bi bi-ticket-perforated"></i>
            <span>Quản lý voucher</span>
        </a>

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

        <div class="mt-auto pt-4">
            <a href="${pageContext.request.contextPath}/auth/logout"
               class="nav-link nav-logout border-t border-gray-600 mt-3 pt-3">
                <i class="bi bi-box-arrow-right"></i>
                <span>Đăng xuất</span>
            </a>
        </div>
    </div>
</div>
