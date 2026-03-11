<%-- review-history.jsp --%>

<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>

<link rel="stylesheet"
	href="${pageContext.request.contextPath}/assets/css/customer/customer-review-history.css">

<div class="rh-page">

	<div class="rh-static">

		<!-- Row 1: Title + Back button -->
		<div class="rh-header">
			<div class="rh-title-group">
				<i data-lucide="star" class="icon-md rh-title-icon"></i>
				<h1 class="rh-page-title">Lịch sử đánh giá</h1>
			</div>
			<a href="${pageContext.request.contextPath}/home"
				class="btn-back-home"> <i data-lucide="arrow-left"
				class="icon-sm"></i> <span>Quay Lại Trang Chủ</span>
			</a>
		</div>

		<!-- Row 2: tất cả filter trên 1 hàng -->
		<form id="rhFilterForm" method="GET"
			action="${pageContext.request.contextPath}/reviews"
			class="rh-filter-row">
			<input type="hidden" name="action" value="user-list"> <input
				type="hidden" name="rating" id="hiddenRating"
				value="${selectedRating}">

			<div class="rh-date-group">
				<label class="rh-date-label">Từ ngày</label> <input type="date"
					name="dateFrom" value="${dateFrom}" class="rh-date-input">
			</div>
			<div class="rh-date-group">
				<label class="rh-date-label">Đến ngày</label> <input type="date"
					name="dateTo" value="${dateTo}" class="rh-date-input">
			</div>
			<button type="submit" class="btn-rh-search">
				<i data-lucide="search" class="icon-sm"></i> <span>Tìm kiếm</span>
			</button>
			<a href="${pageContext.request.contextPath}/reviews?action=user-list"
				class="btn-rh-clear"> Xóa bộ lọc </a>
		</form>

		<!-- Rating tab filters -->
		<div class="rh-tabs" id="rh-filter-tabs">
			<button data-rating="all"
				class="rh-tab ${(empty selectedRating || selectedRating == 'all') ? 'active' : ''}"
				onclick="filterByRating(this, 'all')">Tất cả</button>
			<button data-rating="5"
				class="rh-tab ${selectedRating == '5' ? 'active' : ''}"
				onclick="filterByRating(this, '5')">
				<span class="tab-star">★</span> 5 sao
			</button>
			<button data-rating="4"
				class="rh-tab ${selectedRating == '4' ? 'active' : ''}"
				onclick="filterByRating(this, '4')">
				<span class="tab-star">★</span> 4 sao
			</button>
			<button data-rating="3"
				class="rh-tab ${selectedRating == '3' ? 'active' : ''}"
				onclick="filterByRating(this, '3')">
				<span class="tab-star">★</span> 3 sao
			</button>
			<button data-rating="2"
				class="rh-tab ${selectedRating == '2' ? 'active' : ''}"
				onclick="filterByRating(this, '2')">
				<span class="tab-star">★</span> 2 sao
			</button>
			<button data-rating="1"
				class="rh-tab ${selectedRating == '1' ? 'active' : ''}"
				onclick="filterByRating(this, '1')">
				<span class="tab-star">★</span> 1 sao
			</button>
		</div>

		<div class="rh-divider"></div>
	</div>

	<!-- Session messages -->
	<c:if test="${not empty sessionScope.successMessage}">
		<div class="rh-notif success">
			<i data-lucide="check-circle" class="icon-sm"></i> <span>${sessionScope.successMessage}</span>
		</div>
		<c:remove var="successMessage" scope="session" />
	</c:if>
	<c:if test="${not empty sessionScope.errorMessage}">
		<div class="rh-notif error">
			<i data-lucide="alert-circle" class="icon-sm"></i> <span>${sessionScope.errorMessage}</span>
		</div>
		<c:remove var="errorMessage" scope="session" />
	</c:if>

	<!-- PHẦN ĐỘNG -->
	<div id="rh-list-container" class="rh-list-container">
		<c:choose>
			<c:when test="${not empty listUserReview}">
				<div class="px-6 pb-6 space-y-3">
					<div class="rh-result-count pt-2" id="rh-result-count">
						Tìm thấy <strong id="rh-count-num">${fn:length(listUserReview)}</strong>
						đánh giá
					</div>
					<c:forEach var="review" items="${listUserReview}">
						<div class="rh-card" data-rating="${review.rating}">
							<div class="rh-card-top">
								<div class="rh-facility-info">
									<h3 class="rh-facility-name">
										<c:out value="${review.name}" />
									</h3>
									<div class="rh-facility-address">
										<i data-lucide="map-pin" class="icon-xs"></i> <span><c:out
												value="${review.address}" />, <c:out value="${review.ward}" />,
											<c:out value="${review.district}" />, <c:out
												value="${review.province}" /></span>
									</div>
								</div>
								<div
									style="display: flex; flex-direction: column; align-items: flex-end; gap: 6px; flex-shrink: 0;">
									<div class="rh-stars">
										<c:forEach begin="1" end="5" var="i">
											<span class="rh-star ${i <= review.rating ? 'filled' : ''}">★</span>
										</c:forEach>
									</div>
									<span class="rh-rating-badge rh-rating-${review.rating}">${review.rating}
										sao</span>
								</div>
							</div>
							<div class="rh-booking-tag" style="margin-bottom: 0.5rem;">#Booking
								${review.bookingId}</div>
							<div class="rh-comment${empty review.comment ? ' empty' : ''}">
								<c:choose>
									<c:when test="${not empty review.comment}">
										<c:out value="${review.comment}" />
									</c:when>
									<c:otherwise>Không có nhận xét</c:otherwise>
								</c:choose>
							</div>
							<div class="rh-card-footer">
								<div class="rh-date">
									<i data-lucide="clock" class="icon-xs"></i>
									<fmt:parseDate value="${review.createdAt}"
										pattern="yyyy-MM-dd'T'HH:mm" var="parsedDate" type="both" />
									<fmt:formatDate value="${parsedDate}"
										pattern="dd/MM/yyyy HH:mm" />
								</div>
								<div class="rh-actions">
									<a
										href="${pageContext.request.contextPath}/reviews?action=view&bookingId=${review.bookingId}"
										class="btn-rh-view"> <i data-lucide="eye" class="icon-xs"></i><span>Xem
											chi tiết</span>
									</a> <a
										href="${pageContext.request.contextPath}/profile?section=review-updation&bookingId=${review.bookingId}"
										class="btn-rh-edit"> <i data-lucide="pen" class="icon-xs"></i><span>Sửa
											đánh giá</span>
									</a>
								</div>
							</div>
						</div>
					</c:forEach>
				</div>
			</c:when>
			<c:otherwise>
				<div class="rh-empty">
					<i data-lucide="star"
						style="width: 3rem; height: 3rem; color: #E5E7EB;"></i>
					<p class="rh-empty-title">Chưa có đánh giá nào</p>
					<p class="rh-empty-sub">Hãy đặt sân và để lại đánh giá của bạn!</p>
				</div>
			</c:otherwise>
		</c:choose>
	</div>

</div>

<script>
	function filterByRating(btn, rating) {
		document.querySelectorAll('#rh-filter-tabs .rh-tab').forEach(b => b.classList.remove('active'));
		btn.classList.add('active');
		let visibleCount = 0;
		document.querySelectorAll('.rh-card').forEach(card => {
			const show = rating === 'all' || card.dataset.rating === rating;
			card.style.display = show ? '' : 'none';
			if (show) visibleCount++;
		});
		const countEl = document.getElementById('rh-count-num');
		if (countEl) countEl.textContent = visibleCount;
	}
</script>
