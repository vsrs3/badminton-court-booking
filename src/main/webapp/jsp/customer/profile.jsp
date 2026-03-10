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

<script src="https://cdn.tailwindcss.com"></script>
<link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.0/font/bootstrap-icons.css">

<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/badminton-pro.css">
<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/components.css">
<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/customer/customer-profile-page.css">

</head>

<body onload="lucide.createIcons();">
	<div class="flex flex-col h-screen w-full bg-white shadow-2xl">

		<%-- Header mobile — bỏ border-bottom --%>
		<header class="lg:hidden bg-green-brand text-white p-4 flex items-center justify-between">
			<div class="flex items-center space-x-2">
				<div class="w-8 h-8 bg-lime-brand rounded-full flex items-center justify-center">
					<span class="text-green-brand font-bold text-xs">▲</span>
				</div>
				<h1 class="font-bold text-sm tracking-tight uppercase">CHỌN SÂN CHƠI NGAY</h1>
			</div>
			<div class="flex space-x-2">
				<button class="bg-lime-brand text-green-brand px-3 py-1 rounded-md text-xs font-bold">ĐĂNG NHẬP</button>
			</div>
		</header>

		<div class="flex flex-1 min-h-0">

			<div class="w-full lg:w-96 flex-shrink-0 border-r border-gray-brand overflow-y-auto">
				<%@ include file="/jsp/customer/layout/sidebar.jsp"%>
			</div>

			<div class="hidden lg:block flex-1 min-h-0 bg-gray-brand overflow-y-auto">
				<%
				    String section = (String) request.getAttribute("section");
				    if (section == null) { section = request.getParameter("section"); }
				    if (section == null) { section = "history"; }

				    switch (section) {
				        case "history":
				%>
				            <%@ include file="/jsp/customer/profile/customer_history.jsp" %>
				<%      break;
				        case "booking-detail":
				%>
				            <%@ include file="/jsp/customer/profile/customer_booking_detail.jsp" %>
				<%      break;
				        case "settings":
				%>
				            <%@ include file="/jsp/customer/profile/customer_settings.jsp" %>
				<%      break;
				        case "profile-info":
				%>
				            <%@ include file="/jsp/customer/profile/customer_view.jsp" %>
				<%      break;
				        case "change-password":
				%>
				            <%@ include file="/jsp/customer/profile/change_password.jsp" %>
				<%      break;
				        case "review":
				%>
				            <%@ include file="/jsp/customer/review/review-creation.jsp" %>
				<%      break;
				        case "review-detail":
				%>
				            <%@ include file="/jsp/customer/review/review-detail.jsp" %>
				<%      break;
				        case "review-updation":
				%>
				            <%@ include file="/jsp/customer/review/review-updation.jsp" %>
				<%      break;
				        case "review-list-user":
				%>
				            <%@ include file="/jsp/customer/review/review-history.jsp" %>
				<%      break;

				        default:
				%>
				            <div class="p-6">Phần không tồn tại</div>
				<%  } %>
			</div>
		</div>
	</div>

	<script>
	(function () {
	    const contentPanel = document.querySelector('.hidden.lg\\:block.flex-1');
	    if (!contentPanel) return;

	    const EXTERNAL_PATHS = ['/home', '/auth', '/login', '/register'];
	    function isExternalLink(href) {
	        return EXTERNAL_PATHS.some(path => href.includes(path));
	    }

	    function loadContent(url) {
	        contentPanel.style.transition = 'opacity 0.12s ease';
	        contentPanel.style.opacity = '0';

	        fetch(url)
	            .then(res => res.text())
	            .then(html => {
	                const parser = new DOMParser();
	                const doc = parser.parseFromString(html, 'text/html');
	                const newPanel = doc.querySelector('.hidden.lg\\:block.flex-1');

	                if (newPanel) {
	                    contentPanel.innerHTML = newPanel.innerHTML;
	                } else {
	                    const body = doc.querySelector('body');
	                    contentPanel.innerHTML = body ? body.innerHTML : html;
	                }

	                if (window.lucide) lucide.createIcons();
	                contentPanel.style.opacity = '1';
	                history.pushState(null, '', url);
	            })
	            .catch(() => {
	                contentPanel.style.opacity = '1';
	                window.location.href = url;
	            });
	    }

	    window.loadContent = loadContent;

	    document.addEventListener('click', function (e) {
	        const link = e.target.closest('a[href]');
	        if (!link) return;

	        const href = link.getAttribute('href');
	        if (!href || href === '#' || href.startsWith('http') ||
	            href.startsWith('mailto') || href.startsWith('tel')) return;
	        if (isExternalLink(href)) return;

	        const inSidebar = link.closest('.w-full.lg\\:w-96');
	        const inContent = link.closest('.hidden.lg\\:block.flex-1');
	        if (!inSidebar && !inContent) return;

	        e.preventDefault();
	        loadContent(href);
	    });

	    window.addEventListener('popstate', () => loadContent(window.location.href));
	})();
	</script>

	<script src="https://unpkg.com/lucide@latest"></script>
	<script src="https://cdn.jsdelivr.net/npm/sweetalert2@11"></script>
	<script src="<c:url value='/assets/js/alertPopup.js' />"></script>
	<script src="<c:url value='/assets/js/previewAvatar.js' />"></script>
	<script src="<c:url value='/assets/js/badminton-pro.js' />"></script>
</body>
</html>
