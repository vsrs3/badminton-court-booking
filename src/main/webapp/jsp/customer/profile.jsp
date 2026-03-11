<!-- profile-page.jsp -->

<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ page isErrorPage="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<!DOCTYPE html>
<html lang="vi">
<head>
<meta charset="UTF-8" />
<meta name="viewport" content="width=device-width, initial-scale=1.0" />
<title>Badminton Profile</title>

<!-- Tail Wind -->
<script src="https://cdn.tailwindcss.com"></script>

<!-- Bootrap Icon -->
<link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.0/font/bootstrap-icons.css">

<!-- Custom CSS -->
<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/badminton-pro.css">
<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/components.css">
<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/customer/customer-profile-page.css">

<link rel="stylesheet" href="https://fonts.googleapis.com/css2?family=Inter:wght=300;400;500;600;700&display=swap">

</head>

<body onload="lucide.createIcons();">
	<div class="flex flex-col h-screen w-full bg-white shadow-2xl">

		<header
			class="lg:hidden bg-green-brand text-white p-4 flex items-center justify-between">
			<div class="flex items-center space-x-2">
				<div
					class="w-8 h-8 bg-lime-brand rounded-full flex items-center justify-center">
					<span class="text-green-brand font-bold text-xs">▲</span>
				</div>
				<h1 class="font-bold text-sm tracking-tight uppercase">CHỌN SÂN
					CHƠI NGAY</h1>
			</div>
			<div class="flex space-x-2">
				<button
					class="bg-lime-brand text-green-brand px-3 py-1 rounded-md text-xs font-bold">ĐĂNG
					NHẬP</button>
			</div>
		</header>

		<div class="flex flex-1 min-h-0">

			<div
				class="w-full lg:w-96 flex-shrink-0 border-r border-gray-brand overflow-y-auto">
				<%@ include file="/jsp/customer/layout/sidebar.jsp"%>
			</div>

			<div
				class="hidden lg:block flex-1 min-h-0 bg-gray-brand overflow-y-auto">
				<%
				    String section = (String) request.getAttribute("section");
				    if (section == null) { section = request.getParameter("section"); }
				    if (section == null) { section = "history"; }
				    
				    switch (section) {
				        case "history":
				%>
				            <%@ include file="/jsp/customer/profile/customer_history.jsp" %>
				<%
			            break;
				        case "booking-detail":
				%>
				            <%@ include file="/jsp/customer/profile/customer_booking_detail.jsp" %>
				<%
		            	break;
				        case "settings":
				%>
				            <%@ include file="/jsp/customer/profile/customer_settings.jsp" %>
				<%
			            break;
				        case "profile-info":
				%>
				            <%@ include file="/jsp/customer/profile/customer_view.jsp" %>
				<%
			            break;
				        case "change-password":
				%>
				            <%@ include file="/jsp/customer/profile/change_password.jsp" %>
				<%
			            break;
				        case "review":
	        	%>
				            <%@ include file="/jsp/customer/review/review-creation.jsp" %>
	            <%
			            break;
				        case "review-detail":
	        	%>
				            <%@ include file="/jsp/customer/review/review-detail.jsp" %>
	            <%
	            		break;
				        case "review-updation":
	        	%>
				            <%@ include file="/jsp/customer/review/review-updation.jsp" %>
	            <%
				        break;
				        default:
				%>
				            <div class="p-6">Phần không tồn tại</div>
				<%
				    }
				%>
			</div>
		</div>
	</div>

	<script>
	(function () {

	    // ============================================
	    // BƯỚC 1: KHỞI TẠO
	    // Tìm content panel bên phải khi trang load xong
	    // ============================================
	    const contentPanel = document.querySelector('.hidden.lg\\:block.flex-1');
	    if (!contentPanel) return; // Không tìm thấy panel → dừng toàn bộ script

	    // Danh sách URL sẽ navigate bình thường, KHÔNG intercept
	    const EXTERNAL_PATHS = ['/home', '/auth', '/login', '/register'];

	    function isExternalLink(href) {
	        return EXTERNAL_PATHS.some(path => href.includes(path));
	    }


	    // ============================================
	    // BƯỚC 3: HÀM LOAD NỘI DUNG MỚI VÀO PANEL
	    // Được gọi sau khi click được xác định là hợp lệ
	    // ============================================
	    function loadContent(url) {

	        // 3.1 — Fade out panel hiện tại trong khi chờ fetch
	        contentPanel.style.transition = 'opacity 0.12s ease';
	        contentPanel.style.opacity = '0';

	        // 3.2 — Gọi server lấy HTML của trang mới
	        fetch(url)
	            .then(res => res.text())
	            .then(html => {

	                // 3.3 — Parse chuỗi HTML trả về thành DOM object
	                const parser = new DOMParser();
	                const doc = parser.parseFromString(html, 'text/html');

	                // 3.4 — Tìm đúng content panel trong HTML mới
	                const newPanel = doc.querySelector('.hidden.lg\\:block.flex-1');

	                if (newPanel) {
	                    // Trường hợp A: Trang profile-page.jsp → có panel → lấy đúng phần content
	                    contentPanel.innerHTML = newPanel.innerHTML;
	                } else {
	                    // Trường hợp B: Trang servlet khác (my-bookings...) → không có panel → lấy toàn bộ body
	                    const body = doc.querySelector('body');
	                    contentPanel.innerHTML = body ? body.innerHTML : html;
	                }

	                // 3.5 — Re-render lại các icon Lucide vì DOM vừa bị thay mới
	                if (window.lucide) lucide.createIcons();

	                // 3.6 — Fade in panel sau khi đã inject nội dung mới
	                contentPanel.style.opacity = '1';

	                // 3.7 — Cập nhật URL trên browser mà KHÔNG reload trang
	                history.pushState(null, '', url);
	            })
	            .catch(() => {
	                // 3.8 — Nếu fetch lỗi → fallback: navigate bình thường
	                contentPanel.style.opacity = '1';
	                window.location.href = url;
	            });
	    }


	    // ============================================
	    // BƯỚC 2: BẮT SỰ KIỆN CLICK (EVENT DELEGATION)
	    // Gắn 1 listener duy nhất lên document thay vì từng link
	    // → Hoạt động cả với các link được inject động sau này
	    // ============================================
	    document.addEventListener('click', function (e) {

	        // 2.1 — Tìm thẻ <a> gần nhất từ element được click
	        const link = e.target.closest('a[href]');
	        if (!link) return; // Click không phải vào link → bỏ qua

	        const href = link.getAttribute('href');

	        // 2.2 — Lọc các href không hợp lệ → để browser xử lý bình thường
	        if (
	            !href ||
	            href === '#' ||           // Link neo
	            href.startsWith('http') || // Link tuyệt đối ra ngoài
	            href.startsWith('mailto') ||
	            href.startsWith('tel')
	        ) return;

	        // 2.3 — Nếu link thuộc EXTERNAL_PATHS → navigate bình thường, không intercept
	        if (isExternalLink(href)) return;

	        // 2.4 — Chỉ intercept link nằm trong sidebar hoặc content panel
	        const inSidebar = link.closest('.w-full.lg\\:w-96');
	        const inContent = link.closest('.hidden.lg\\:block.flex-1');
	        if (!inSidebar && !inContent) return;

	        // 2.5 — Tất cả điều kiện thỏa → chặn reload và gọi loadContent
	        e.preventDefault();
	        loadContent(href);
	    });


	    // ============================================
	    // BƯỚC 4: XỬ LÝ NÚT BACK / FORWARD CỦA TRÌNH DUYỆT
	    // popstate kích hoạt khi user nhấn ← → trên browser
	    // → Load lại nội dung tương ứng URL hiện tại mà không reload trang
	    // ============================================
	    window.addEventListener('popstate', () => loadContent(window.location.href));

	})();
	</script>

	<!--  -->
	<script src="https://unpkg.com/lucide@latest"></script>

	<!-- Add SweetAlert2 -->
	<script src="https://cdn.jsdelivr.net/npm/sweetalert2@11"></script>

	<!-- JS load -->
	<script src="<c:url value='/assets/js/alertPopup.js' />"></script>
	<script src="<c:url value='/assets/js/previewAvatar.js' />"></script>
	<script src="<c:url value='/assets/js/badminton-pro.js' />"></script>

</body>
</html>