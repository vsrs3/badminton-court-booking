<%-- sidebar.jsp --%>

<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<div class="flex flex-col h-full"
	style="background-color: #064E3B; font-family: 'Be Vietnam Pro', 'Inter', -apple-system, sans-serif;">

	<%-- ── HEADER: avatar + tên ── --%>
	<div class="p-5"
		style="background-color: #053d2f; border-bottom: 1px solid rgba(163, 230, 53, 0.2);">
		<div class="flex items-center justify-between mb-4">
			<div class="flex items-center space-x-3">
				<div class="relative">
					<a href="profile?section=profile-info"> <c:if
							test="${not empty sessionScope.account.avatarPath}">
							<img
								src="${pageContext.request.contextPath}/uploads/${sessionScope.account.avatarPath}"
								alt="Avatar" class="w-14 h-14 rounded-full"
								style="border: 2px solid #A3E635;" />
							<div class="absolute bottom-0 right-0 w-4 h-4 rounded-full"
								style="background: #A3E635; border: 2px solid #053d2f;"></div>
						</c:if> <c:if test="${empty sessionScope.account.avatarPath}">
							<i data-lucide="user-circle" class="w-14 h-14"
								style="color: #A3E635;"></i>
						</c:if>
					</a>
				</div>
				<div>
					<a href="profile?section=profile-info">
						<h2 class="font-bold text-base text-white">${sessionScope.account.getFullName()}</h2>
					</a>
					<p class="text-xs" style="color: rgba(255, 255, 255, 0.6);">${sessionScope.account.getEmail()}</p>
				</div>
			</div>
			<a href="profile?section=profile-info"> <i
				data-lucide="chevron-right" class="w-5 h-5"
				style="color: rgba(255, 255, 255, 0.4);"></i>
			</a>
		</div>

		<%-- Hạng thành viên --%>
		<div
			class="rounded-xl p-3 flex items-center justify-between cursor-pointer"
			style="background: rgba(0, 0, 0, 0.2); border: 1px solid rgba(163, 230, 53, 0.25); transition: background 0.15s;"
			onmouseover="this.style.background='rgba(0,0,0,0.35)'"
			onmouseout="this.style.background='rgba(0,0,0,0.2)'">
			<div class="flex items-center space-x-2">
				<i data-lucide="diamond" class="w-5 h-5" style="color: #A3E635;"></i>
				<span class="text-xs font-bold uppercase tracking-widest"
					style="color: #A3E635;">Hạng thành viên</span>
			</div>
			<i data-lucide="chevron-right" class="w-4 h-4"
				style="color: #A3E635;"></i>
		</div>
	</div>

	<%-- ── QUICK ACTIONS ── --%>
	<div class="p-4"
		style="border-bottom: 1px solid rgba(163, 230, 53, 0.15);">
		<div class="grid grid-cols-4 gap-2">

			<a href="${pageContext.request.contextPath}/my-bookings"
				class="sidebar-quick-btn flex flex-col items-center justify-center p-2 rounded-xl cursor-pointer"
				data-match-path="/my-bookings"> <i data-lucide="calendar"
				class="w-5 h-5 text-orange-400"></i> <span
				class="sidebar-quick-label text-[10px] font-medium text-center mt-1">Lịch
					đã đặt</span>
			</a> <a href="#"
				class="sidebar-quick-btn flex flex-col items-center justify-center p-2 rounded-xl cursor-pointer">
				<i data-lucide="bell" class="w-5 h-5 text-yellow-400"></i> <span
				class="sidebar-quick-label text-[10px] font-medium text-center mt-1">Thông
					báo</span>
			</a> <a href="${pageContext.request.contextPath}/reviews?action=user-list" id="sidebarMapBtn" data-tab="map"
				class="sidebar-quick-btn flex flex-col items-center justify-center p-2 rounded-xl cursor-pointer">
				<i data-lucide="star" class="w-5 h-5" style="color: #A3E635;"></i> <span
				class="sidebar-quick-label text-[10px] font-medium text-center mt-1"> Đánh 
					giá</span>
			</a>

			<div
				class="sidebar-quick-btn flex flex-col items-center justify-center p-2 rounded-xl cursor-pointer">
				<i data-lucide="gift" class="w-5 h-5 text-red-400"></i> <span
					class="sidebar-quick-label text-[10px] font-medium text-center mt-1">Ưu
					đãi</span>
			</div>

		</div>
	</div>

	<%-- ── MENU CHÍNH ── --%>
	<div class="flex-1 pb-6 overflow-y-auto">

		<div class="px-4 pt-5 pb-2">
			<h3 class="text-[0.6rem] font-extrabold uppercase tracking-[0.2em]"
				style="color: rgba(163, 230, 53, 0.6);">Hoạt động</h3>
		</div>

		<a href="profile?section=profile-info"
			class="sidebar-menu-item flex items-center justify-between px-4 py-3 mx-2 rounded-xl transition-all"
			data-match-section="profile-info">
			<div class="flex items-center space-x-3">
				<i data-lucide="user" class="w-5 h-5 sidebar-menu-icon"></i> <span
					class="text-sm font-semibold sidebar-menu-text">Thông tin cá
					nhân</span>
			</div> <i data-lucide="chevron-right" class="w-4 h-4 sidebar-menu-chevron"></i>
		</a>

		<div
			class="sidebar-menu-item flex items-center justify-between px-4 py-3 mx-2 rounded-xl transition-all cursor-pointer">
			<div class="flex items-center space-x-3">
				<i data-lucide="heart" class="w-5 h-5 sidebar-menu-icon"></i> <span
					class="text-sm font-semibold sidebar-menu-text">Danh sách
					yêu thích</span>
			</div>
			<i data-lucide="chevron-right" class="w-4 h-4 sidebar-menu-chevron"></i>
		</div>

		<div
			class="sidebar-menu-item flex items-center justify-between px-4 py-3 mx-2 rounded-xl transition-all cursor-pointer">
			<div class="flex items-center space-x-3">
				<i data-lucide="crown" class="w-5 h-5 sidebar-menu-icon"></i> <span
					class="text-sm font-semibold sidebar-menu-text">Gói hội viên</span>
			</div>
			<i data-lucide="chevron-right" class="w-4 h-4 sidebar-menu-chevron"></i>
		</div>

		<div class="px-4 pt-5 pb-2">
			<h3 class="text-[0.6rem] font-extrabold uppercase tracking-[0.2em]"
				style="color: rgba(163, 230, 53, 0.6);">Hệ thống</h3>
		</div>

		<a href="profile?section=settings"
			class="sidebar-menu-item flex items-center justify-between px-4 py-3 mx-2 rounded-xl transition-all"
			data-match-section="settings">
			<div class="flex items-center space-x-3">
				<i data-lucide="settings" class="w-5 h-5 sidebar-menu-icon"></i> <span
					class="text-sm font-semibold sidebar-menu-text">Cài đặt</span>
			</div> <i data-lucide="chevron-right" class="w-4 h-4 sidebar-menu-chevron"></i>
		</a> <a href="profile?section=change-password"
			class="sidebar-menu-item flex items-center justify-between px-4 py-3 mx-2 rounded-xl transition-all"
			data-match-section="change-password">
			<div class="flex items-center space-x-3">
				<i data-lucide="shield-check" class="w-5 h-5 sidebar-menu-icon"></i>
				<span class="text-sm font-semibold sidebar-menu-text">Điều
					khoản và chính sách</span>
			</div> <i data-lucide="chevron-right" class="w-4 h-4 sidebar-menu-chevron"></i>
		</a>

		<%-- ── ĐĂNG XUẤT ── --%>
		<div class="px-2 pt-4">
			<form action="customerController" method="GET" id="logoutForm">
				<input type="hidden" name="action" value="logout">
				<button type="submit"
					class="sidebar-logout-btn w-full flex items-center justify-between px-4 py-3 rounded-xl"
					style="background: rgba(239, 68, 68, 0.12); border: 1px solid rgba(239, 68, 68, 0.3); cursor: pointer;">
					<div class="flex items-center space-x-3">
						<i data-lucide="log-out" class="w-5 h-5" style="color: #F87171;"></i>
						<span class="text-sm font-bold" style="color: #FCA5A5;">Đăng
							Xuất</span>
					</div>
					<i data-lucide="chevron-right" class="w-4 h-4"
						style="color: #F87171; pointer-events: none;"></i>
				</button>
			</form>
		</div>

	</div>
</div>

<style>
/*  MENU ITEMS */
.sidebar-menu-item {
	color: rgba(255, 255, 255, 0.75);
}

.sidebar-menu-item .sidebar-menu-icon {
	color: rgba(255, 255, 255, 0.45);
}

.sidebar-menu-item .sidebar-menu-text {
	color: rgba(255, 255, 255, 0.85);
}

.sidebar-menu-item .sidebar-menu-chevron {
	color: rgba(255, 255, 255, 0.2);
}

.sidebar-menu-item:hover {
	background: rgba(163, 230, 53, 0.1);
}

.sidebar-menu-item:hover .sidebar-menu-icon {
	color: #A3E635;
}

.sidebar-menu-item:hover .sidebar-menu-text {
	color: #A3E635;
}

.sidebar-menu-item:hover .sidebar-menu-chevron {
	color: #A3E635;
}

.sidebar-menu-item.active {
	background: rgba(163, 230, 53, 0.18);
}

.sidebar-menu-item.active .sidebar-menu-icon {
	color: #A3E635;
}

.sidebar-menu-item.active .sidebar-menu-text {
	color: #A3E635;
	font-weight: 800;
}

.sidebar-menu-item.active .sidebar-menu-chevron {
	color: #A3E635;
}

/* QUICK BUTTONS  */
.sidebar-quick-btn {
	background: rgba(255, 255, 255, 0.07);
	border: 1px solid rgba(255, 255, 255, 0.1);
	transition: background 0.15s ease, border-color 0.15s ease;
	text-decoration: none;
}

.sidebar-quick-label {
	color: rgba(255, 255, 255, 0.75);
}

.sidebar-quick-btn:hover {
	background: rgba(163, 230, 53, 0.15);
	border-color: rgba(163, 230, 53, 0.5);
}

.sidebar-quick-btn:hover .sidebar-quick-label {
	color: #A3E635;
}

.sidebar-quick-btn.active {
	background: rgba(163, 230, 53, 0.22);
	border-color: #A3E635;
	box-shadow: 0 0 0 1px rgba(163, 230, 53, 0.3);
}

.sidebar-quick-btn.active .sidebar-quick-label {
	color: #A3E635;
	font-weight: 800;
}

/* ── LOGOUT BUTTON ─────────────────────────────────── */
.sidebar-logout-btn {
	background: rgba(239, 68, 68, 0.12);
	border: 1px solid rgba(239, 68, 68, 0.3);
	transition: background 0.15s ease, border-color 0.15s ease;
}

.sidebar-logout-btn:hover {
	background: rgba(239, 68, 68, 0.25);
	border-color: #EF4444;
}
</style>

<script>
	(function() {
		// Lấy section hiện tại từ URL (?section=xxx)
		function getCurrentSection() {
			var params = new URLSearchParams(window.location.search);
			return params.get('section') || '';
		}

		// Lấy pathname hiện tại (/my-bookings, /profile, ...)
		function getCurrentPath() {
			return window.location.pathname;
		}

		function clearAll() {
			document.querySelectorAll('.sidebar-menu-item').forEach(
					function(el) {
						el.classList.remove('active');
					});
			document.querySelectorAll('.sidebar-quick-btn').forEach(
					function(el) {
						el.classList.remove('active');
					});
		}

		function setActive() {
			clearAll();
			var section = getCurrentSection();
			var path = getCurrentPath();

			// Menu items — match theo data-match-section
			document.querySelectorAll('.sidebar-menu-item[data-match-section]')
					.forEach(function(el) {
						if (el.getAttribute('data-match-section') === section) {
							el.classList.add('active');
						}
					});

			// Quick buttons — match theo data-match-path
			document.querySelectorAll('.sidebar-quick-btn[data-match-path]')
					.forEach(function(el) {
						var matchPath = el.getAttribute('data-match-path');
						if (path.includes(matchPath)) {
							el.classList.add('active');
						}
					});
		}

		// Chạy khi load
		setActive();

		// Cập nhật khi SPA điều hướng
		window.addEventListener('popstate', setActive);

		// Active ngay khi click (trước khi URL thay đổi) — cả menu lẫn quick btn
		document.querySelectorAll(
				'.sidebar-menu-item[href], .sidebar-quick-btn[href]').forEach(
				function(el) {
					el.addEventListener('click', function() {
						clearAll();
						if (el.classList.contains('sidebar-menu-item')) {
							el.classList.add('active');
						} else if (el.classList.contains('sidebar-quick-btn')) {
							el.classList.add('active');
						}
					});
				});

		// Expose để SPA có thể gọi sau khi loadContent
		window.sidebarSetActive = setActive;
	})();
</script>
