<%-- review-detail.jsp --%>

<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>

<link rel="stylesheet"
	href="${pageContext.request.contextPath}/assets/css/customer/customer-review-detail.css" />

<c:choose>
	<c:when test="${not empty sessionScope.userReview}">
		<c:set var="r" value="${sessionScope.userReview}" />
		<c:set var="f" value="${sessionScope.facilityReview}" />

		<div class="review-page">

			<!-- HEADER -->
			<div class="review-header">
				<button onclick="history.back()"
					class="btn-back-home"> <i data-lucide="arrow-left"
					class="icon-sm"></i> <span>Quay Lại</span>
				</button>
				<div class="review-title-group">
					<h1 class="review-page-title">Đánh giá của bạn</h1>
					<i data-lucide="star" class="icon-md review-title-icon"></i>
				</div>
			</div>

			<!-- SESSION MESSAGES -->
			<c:if test="${not empty sessionScope.successMessage}">
				<div class="review-notification success">
					<i data-lucide="check-circle" class="icon-sm"></i>
					<span>${sessionScope.successMessage}</span>
				</div>
				<c:remove var="successMessage" scope="session" />
			</c:if>
			<c:if test="${not empty sessionScope.errorMessage}">
				<div class="review-notification error">
					<i data-lucide="alert-circle" class="icon-sm"></i>
					<span>${sessionScope.errorMessage}</span>
				</div>
				<c:remove var="errorMessage" scope="session" />
			</c:if>

			<!-- CONTAINER -->
			<div class="review-container">

				<!-- REVIEW VIEW CARD -->
				<div class="review-card">
					<%-- <span class="text-xs text-gray-400"># ${r.reviewId}</span> --%>

					<!-- Star Rating Display -->
					<div class="review-section">
						<span class="review-section-label"> <i data-lucide="star"
							class="icon-xs"></i> Đánh giá của bạn
						</span>
						
						<div class="stars-display">
							<c:forEach begin="1" end="5" var="i">
								<span
									class="star-icon ${i <= r.rating ? 'star-filled' : 'star-empty'}">&#9733;</span>
							</c:forEach>
							<%-- <span class="rating-number">${r.rating}/5</span> --%>
						</div>
						<p class="rating-label">
							<c:choose>
								<c:when test="${r.rating == 5}">Xuất sắc!</c:when>
								<c:when test="${r.rating == 4}">Tốt</c:when>
								<c:when test="${r.rating == 3}">Bình thường</c:when>
								<c:when test="${r.rating == 2}">Tệ</c:when>
								<c:when test="${r.rating == 1}">Rất tệ</c:when>
							</c:choose>
						</p>
					</div>

					<div class="divider"></div>

					<!-- Comment -->
					<div class="review-section">
						<span class="review-section-label"> <i
							data-lucide="message-square" class="icon-xs"></i> Nhận xét của
							bạn
						</span>
						<c:choose>
							<c:when test="${not empty r.comment}">
								<div class="comment-box">
									<i data-lucide="quote" class="quote-icon"></i>
									<p class="comment-text">
										<c:out value="${r.comment}" />
									</p>
								</div>
							</c:when>
							<c:otherwise>
								<p class="no-comment">Bạn chưa để lại nhận xét</p>
							</c:otherwise>
						</c:choose>
					</div>

					<div class="divider"></div>

					<!-- Created At -->
					<div class="review-meta">
						<i data-lucide="clock" class="icon-xs"></i> 
						<span style="color: gray">Đã đánh
							giá vào: <strong style="color: gray">${fn:substring(r.createdAt, 8, 10)}/${fn:substring(r.createdAt, 5, 7)}/${fn:substring(r.createdAt, 0, 4)}</strong>
						</span>
					</div>

					<!-- Actions -->
					<div class="review-actions">
						<a
							href="${pageContext.request.contextPath}/profile?section=review-updation&bookingId=${r.bookingId}"
							type="submit" class="btn-edit-review"> <i data-lucide="pen"
							class="icon-sm"></i> <span>Chỉnh sửa đánh giá</span>

						</a>

						<form method="POST" action="${pageContext.request.contextPath}/reviews">
							<input type="hidden" name="action" value="delete" /> 
							<input type="hidden" name="bookingId" value="${r.bookingId}" />
								
							<!-- <button type="submit" class="btn-back-review">
								<i data-lucide="trash" class="icon-sm"></i> <span>Xóa đánh giá</span>
							</button> -->
						</form>
					</div>

				</div>

				<!-- FACILITY INFO CARD -->
				<c:if test="${not empty f}">
					<div class="field-info-card">
						<div class="facility-body">
							<div class="facility-section">
								<h2 class="facility-name">
									<c:out value="${f.name}" />
								</h2>
								<div class="facility-address">
									<i data-lucide="map-pin" class="icon-sm"></i> <span> <c:out
											value="${f.address}" />, <c:out value="${f.ward}" />, <c:out
											value="${f.district}" />, <c:out value="${f.province}" />
									</span>
								</div>
							</div>

							<c:if test="${not empty f.description}">
								<div class="facility-section">
									<span class="review-section-label"> <i
										data-lucide="info" class="icon-xs"></i> Mô tả sân
									</span>
									<p class="facility-desc">
										<c:out value="${f.description}" />
									</p>
								</div>
							</c:if>

							<div class="facility-hours">
								<div class="hour-block">
									<span class="hour-label open"> <i data-lucide="clock"
										class="icon-xs"></i> Giờ mở cửa
									</span>
									<div class="hour-value">
										<c:out value="${f.openTime}" />
									</div>
								</div>
								<div class="hour-block">
									<span class="hour-label close"> <i data-lucide="clock"
										class="icon-xs"></i> Giờ đóng cửa
									</span>
									<div class="hour-value">
										<c:out value="${f.closeTime}" />
									</div>
								</div>
							</div>
						</div>
					</div>
				</c:if>

			</div>
		</div>

	</c:when>
	<c:otherwise>
		<div class="p-6 text-red-500 font-semibold">Không tìm thấy đánh
			giá.</div>
	</c:otherwise>
</c:choose>
