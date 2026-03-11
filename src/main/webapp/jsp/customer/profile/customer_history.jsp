<!-- customer_history.jsp -->


<%@ page language="java" contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<link rel="stylesheet"
      href="${pageContext.request.contextPath}/assets/css/customer/customer-booking-history.css">

<div class="flex flex-col h-full bg-white">

    <div class="p-6 pb-3">

        <!-- Row 1: Title + Back button -->
        <div class="history-header mb-4">
            <div class="review-title-group">
                <i data-lucide="calendar-check" class="icon-md review-title-icon"></i>
                <h1 class="review-page-title">Lịch sử đặt sân</h1>
            </div>
            <a href="${pageContext.request.contextPath}/home"
               class="btn-back-home"> <i data-lucide="arrow-left"
                                         class="w-4 h-4"></i> <span>Quay Lại Trang Chủ</span>
            </a>
        </div>

        <!-- Row 2: tất cả filter trên 1 hàng -->
        <form id="dateFilterForm" method="get"
              action="${pageContext.request.contextPath}/my-bookings">
            <input type="hidden" name="status" id="hiddenStatus"
                   value="${selectedStatus}"/>
            <div class="history-filter-row">
                <div class="history-date-group">
                    <label class="history-date-label">Từ ngày</label> <input
                        type="date" name="dateFrom" value="${dateFrom}"
                        class="history-date-input"/>
                </div>
                <div class="history-date-group">
                    <label class="history-date-label">Đến ngày</label> <input
                        type="date" name="dateTo" value="${dateTo}"
                        class="history-date-input"/>
                </div>
                <button type="submit" class="btn-search">
                    <i data-lucide="search" class="w-4 h-4"></i> <span>Tìm kiếm</span>
                </button>
                <a href="${pageContext.request.contextPath}/my-bookings"
                   class="btn-clear-filter"> Xóa bộ lọc </a>
            </div>
        </form>

        <!-- Status filter tabs -->
        <div
                class="flex items-center space-x-2 overflow-x-auto pb-3 no-scrollbar mt-3"
                id="filter-tabs">
            <button data-status="all"
                    class="filter-btn ${(selectedStatus == 'all' || empty selectedStatus) ? 'active' : ''}"
                    onclick="filterByStatus(this, 'all')">Tất cả
            </button>
            <button data-status="PENDING"
                    class="filter-btn ${selectedStatus == 'PENDING' ? 'active' : ''}"
                    onclick="filterByStatus(this, 'PENDING')">Chờ thanh toán
            </button>
            <button data-status="CONFIRMED"
                    class="filter-btn ${selectedStatus == 'CONFIRMED' ? 'active' : ''}"
                    onclick="filterByStatus(this, 'CONFIRMED')">Đã xác nhận
            </button>
            <button data-status="COMPLETED"
                    class="filter-btn ${selectedStatus == 'COMPLETED' ? 'active' : ''}"
                    onclick="filterByStatus(this, 'COMPLETED')">Hoàn thành
            </button>
            <button data-status="CANCELLED"
                    class="filter-btn ${selectedStatus == 'CANCELLED' ? 'active' : ''}"
                    onclick="filterByStatus(this, 'CANCELLED')">Đã hủy
            </button>
            <button data-status="EXPIRED"
                    class="filter-btn ${selectedStatus == 'EXPIRED' ? 'active' : ''}"
                    onclick="filterByStatus(this, 'EXPIRED')">Hết hạn
            </button>
        </div>
        <div class="h-[1px] bg-gray-100 w-full"></div>
    </div>

    <!-- Session messages -->
    <c:if test="${not empty sessionScope.successMessage}">
        <div
                class="mx-6 mb-3 p-3 bg-green-50 border border-green-200 rounded-lg flex items-center space-x-2">
            <i data-lucide="check-circle"
               class="w-5 h-5 text-green-600 flex-shrink-0"></i> <span
                class="text-sm text-green-800 font-medium">${sessionScope.successMessage}</span>
        </div>
        <c:remove var="successMessage" scope="session"/>
    </c:if>
    <c:if test="${not empty sessionScope.errorMessage}">
        <div
                class="mx-6 mb-3 p-3 bg-red-50 border border-red-200 rounded-lg flex items-center space-x-2">
            <i data-lucide="alert-circle"
               class="w-5 h-5 text-red-600 flex-shrink-0"></i> <span
                class="text-sm text-red-800 font-medium">${sessionScope.errorMessage}</span>
        </div>
        <c:remove var="errorMessage" scope="session"/>
    </c:if>

    <div id="booking-list-container" class="flex-1 overflow-y-auto">
        <c:choose>
            <c:when test="${not empty bookings}">
                <div class="flex-1 overflow-y-auto px-6 pb-6 space-y-3">
                    <div class="text-xs text-gray-400 font-medium pt-2 pb-1">
                        Tìm thấy <strong class="text-gray-600">${fn:length(bookings)}</strong>
                        lịch đặt
                    </div>
                    <c:forEach var="booking" items="${bookings}">
                        <%-- Pre-compute button visibility flags --%>
                        <%-- canPay: PENDING + UNPAID + hold not expired --%>
                        <c:set var="canPay" value="${booking.bookingStatus == 'PENDING'
						and booking.paymentStatus == 'UNPAID'
						and not empty booking.holdExpiredAt}"/>
                        <%-- canPayRemaining: CONFIRMED + PARTIAL --%>
                        <c:set var="canPayRemaining" value="${booking.bookingStatus == 'CONFIRMED'
						and booking.paymentStatus == 'PARTIAL'}"/>
                        <%-- canCancel: (PENDING or CONFIRMED) + UNPAID --%>
                        <c:set var="canCancel" value="${(booking.bookingStatus == 'PENDING'
						or booking.bookingStatus == 'CONFIRMED')
						and booking.paymentStatus == 'UNPAID'}"/>

                        <div class="booking-card rounded-xl bg-white p-4">
                            <!-- Top: type badge + status badge + id -->
                            <div class="flex items-start justify-between mb-3">
                                <div class="flex items-center space-x-2">
								<span class="badge ${booking.bookingType == 'SINGLE' ? 'type-single' : 'type-recurring'}">
									<c:choose>
                                        <c:when test="${booking.bookingType == 'SINGLE'}">Đặt lẻ</c:when>
                                        <c:otherwise>Đặt cố định</c:otherwise>
                                    </c:choose>
								</span>
                                    <c:choose>
                                        <c:when test="${booking.bookingStatus == 'PENDING'}">
                                            <span class="badge badge-pending">Chờ thanh toán</span>
                                        </c:when>
                                        <c:when test="${booking.bookingStatus == 'CONFIRMED'}">
                                            <span class="badge badge-confirmed">Đã xác nhận</span>
                                        </c:when>
                                        <c:when test="${booking.bookingStatus == 'COMPLETED'}">
                                            <span class="badge badge-completed">Hoàn thành</span>
                                        </c:when>
                                        <c:when test="${booking.bookingStatus == 'CANCELLED'}">
                                            <span class="badge badge-cancelled">Đã hủy</span>
                                        </c:when>
                                        <c:when test="${booking.bookingStatus == 'EXPIRED'}">
                                            <span class="badge badge-expired">Hủy do quá giờ</span>
                                        </c:when>
                                    </c:choose>
                                </div>
                                <span class="text-xs text-gray-400">#${booking.bookingId}</span>
                            </div>

                            <div class="flex items-start space-x-3 mb-3">
                                <c:choose>
                                    <c:when test="${not empty booking.thumbnailPath}">
                                        <img
                                                src="${pageContext.request.contextPath}/uploads/${booking.thumbnailPath}"
                                                class="w-14 h-14 rounded-lg object-cover flex-shrink-0 border border-gray-100"/>
                                    </c:when>
                                    <c:otherwise>
                                        <div
                                                class="w-14 h-14 rounded-lg bg-green-50 flex items-center justify-center flex-shrink-0 border border-green-100">
                                            <i data-lucide="map-pin" class="w-6 h-6 text-green-600"></i>
                                        </div>
                                    </c:otherwise>
                                </c:choose>
                                <div class="min-w-0 flex-1">
                                    <h3 class="text-sm font-bold text-gray-800 truncate">${booking.facilityName}</h3>
                                    <p class="text-xs text-gray-500 mt-0.5 truncate">${booking.fullAddress}</p>
                                </div>
                            </div>

                            <!-- Date + merged slot details -->
                            <div class="bg-gray-50 rounded-lg p-3 mb-3 space-y-1.5">
                                <div class="flex items-center space-x-2">
                                    <i data-lucide="calendar" class="w-3.5 h-3.5 text-gray-400"></i>
                                    <span class="text-xs text-gray-600 font-medium">
									<fmt:parseDate value="${booking.bookingDate}" pattern="yyyy-MM-dd" var="parsedDate"
                                                   type="date"/>
									<fmt:formatDate value="${parsedDate}" pattern="dd/MM/yyyy"/>
								</span>
                                </div>
                                <div class="flex items-start space-x-2">
                                    <i data-lucide="clock" class="w-3.5 h-3.5 text-gray-400 mt-0.5"></i>
                                    <span class="text-xs text-gray-600">${booking.slotDetails}</span>
                                </div>
                                    <%-- Hold expiry countdown for PENDING --%>
                                <c:if test="${canPay}">
                                    <div class="flex items-center space-x-2 mt-1">
                                        <i data-lucide="timer" class="w-3.5 h-3.5 text-amber-400"></i>
                                        <span class="text-xs text-amber-600 font-medium"
                                              id="countdown-${booking.bookingId}"
                                              data-expire="${booking.holdExpiredAt}">
										Đang tải thời gian...
									</span>
                                    </div>
                                </c:if>
                            </div>

                            <!-- Amount + payment status + action buttons -->
                            <div class="flex items-center justify-between flex-wrap gap-2">
                                <div>
								<span class="text-sm font-bold text-gray-800">
									<fmt:formatNumber value="${booking.totalAmount}" type="number" groupingUsed="true"/>đ
								</span>
                                    <c:if test="${not empty booking.paymentStatus}">
									<span class="text-xs ml-1.5 ${booking.paymentStatus == 'PAID' ? 'pay-paid' : (booking.paymentStatus == 'PARTIAL' ? 'pay-partial' : 'pay-unpaid')}">
										• ${booking.paymentStatus == 'PAID' ? 'Đã thanh toán'
                                            : (booking.paymentStatus == 'PARTIAL' ? 'Đã cọc' : 'Chưa thanh toán')}
									</span>
                                    </c:if>
                                        <%-- Show remaining amount for PARTIAL --%>
                                    <c:if test="${booking.paymentStatus == 'PARTIAL' and booking.paidAmount != null}">
                                        <div class="text-xs text-orange-600 mt-0.5">
                                            Còn lại: <fmt:formatNumber
                                                value="${booking.totalAmount - booking.paidAmount}" type="number"
                                                groupingUsed="true"/>đ
                                        </div>
                                    </c:if>
                                </div>

                                <div class="flex items-center flex-wrap gap-1.5">
                                        <%-- Nút Thanh Toán: PENDING + UNPAID + chưa hết hold --%>
                                    <c:if test="${canPay}">
                                        <form method="post" action="${pageContext.request.contextPath}/my-bookings"
                                              onsubmit="return handlePayClick(this)">
                                            <input type="hidden" name="action" value="retryPayment"/>
                                            <input type="hidden" name="bookingId" value="${booking.bookingId}"/>
                                            <button type="submit" class="btn-pay">
                                                <i data-lucide="credit-card" class="w-3.5 h-3.5"></i> Thanh Toán
                                            </button>
                                        </form>
                                    </c:if>

                                        <%-- Nút Thanh Toán Phần Còn Lại: CONFIRMED + PARTIAL --%>
                                    <c:if test="${canPayRemaining}">
                                        <form method="post" action="${pageContext.request.contextPath}/my-bookings"
                                              onsubmit="return handlePayClick(this)">
                                            <input type="hidden" name="action" value="payRemaining"/>
                                            <input type="hidden" name="bookingId" value="${booking.bookingId}"/>
                                            <button type="submit" class="btn-pay-remaining">
                                                <i data-lucide="banknote" class="w-3.5 h-3.5"></i> Thanh Toán Còn Lại
                                            </button>
                                        </form>
                                    </c:if>

                                        <%-- Nút Hủy: (PENDING/CONFIRMED) + UNPAID --%>
                                    <c:if test="${canCancel}">
                                        <form method="post" action="${pageContext.request.contextPath}/my-bookings"
                                              onsubmit="return confirmCancel(this, ${booking.bookingId})">
                                            <input type="hidden" name="action" value="cancel"/>
                                            <input type="hidden" name="bookingId" value="${booking.bookingId}"/>
                                            <button type="submit" class="cancel-btn">Hủy</button>
                                        </form>
                                    </c:if>

                                        <%-- Nút đánh giá: chỉ cho COMPLETED --%>
                                    <c:if test="${booking.bookingStatus == 'COMPLETED'}">
                                        <c:choose>
                                            <c:when test="${booking.reviewed}">
                                                <a href="${pageContext.request.contextPath}/reviews?action=view&bookingId=${booking.bookingId}"
                                                   class="btn-review btn-review-view">
                                                    <i data-lucide="eye" class="w-3.5 h-3.5"></i> Xem đánh giá
                                                </a>
                                                <a href="${pageContext.request.contextPath}/profile?section=review-updation&bookingId=${booking.bookingId}"
                                                   class="btn-review btn-review-edit">
                                                    <i data-lucide="pen" class="w-3.5 h-3.5"></i> Sửa đánh giá
                                                </a>
                                            </c:when>
                                            <c:otherwise>
                                                <a class="btn-review btn-review-write"
                                                   href="${pageContext.request.contextPath}/profile?section=review&bookingId=${booking.bookingId}">
                                                    <i data-lucide="star" class="w-3.5 h-3.5"></i> Viết đánh giá
                                                </a>
                                            </c:otherwise>
                                        </c:choose>
                                    </c:if>

                                        <%-- Nút Chi tiết --%>
                                    <a href="${pageContext.request.contextPath}/my-bookings?action=detail&id=${booking.bookingId}"
                                       class="btn-detail">
                                        <span>Chi tiết</span>
                                        <i data-lucide="chevron-right" class="w-3.5 h-3.5"></i>
                                    </a>
                                </div>
                            </div>
                        </div>
                    </c:forEach>
                </div>
            </c:when>
            <c:otherwise>
                <div class="flex-1 flex flex-col items-center justify-center p-8 text-center bg-gray-50/50">
                    <i data-lucide="search" class="w-12 h-12 text-gray-300 mb-4"></i>
                    <p class="text-gray-500 font-semibold text-lg">Bạn chưa có lịch đặt</p>
                    <a href="${pageContext.request.contextPath}/home"
                       class="mt-8 bg-[#9ef01a] text-[#004d3d] px-10 py-3.5 rounded-full font-bold text-sm shadow-md">
                        Đặt sân ngay
                    </a>
                </div>
            </c:otherwise>
        </c:choose>
    </div>
</div>

<script>
    /* ── Filter by status tab ── */
    function filterByStatus(btn, status) {
        document.querySelectorAll('#filter-tabs .filter-btn').forEach(function(b) { b.classList.remove('active'); });
        btn.classList.add('active');
        var hiddenStatus = document.getElementById('hiddenStatus');
        if (hiddenStatus) hiddenStatus.value = status;
        var params = new URLSearchParams(window.location.search);
        params.set('status', status);
        var url = '${pageContext.request.contextPath}/my-bookings?' + params.toString();
        var container = document.getElementById('booking-list-container');
        if (window.loadContent && container) {
            fetch(url).then(function(res) { return res.text(); }).then(function(html) {
                var doc = new DOMParser().parseFromString(html, 'text/html');
                var newList = doc.getElementById('booking-list-container');
                if (newList) { container.innerHTML = newList.innerHTML; if (window.lucide) lucide.createIcons(); }
            });
        } else { window.location.href = url; }
    }

    /* ── Cancel confirmation ── */
    function confirmCancel(form, bookingId) {
        if (typeof Swal !== 'undefined') {
            Swal.fire({
                title: 'Xác nhận hủy?',
                html: 'Bạn có chắc chắn muốn hủy booking <strong>#' + bookingId + '</strong>?',
                icon: 'warning',
                showCancelButton: true,
                confirmButtonColor: '#DC2626',
                cancelButtonColor: '#6B7280',
                confirmButtonText: 'Hủy booking',
                cancelButtonText: 'Quay lại'
            }).then(function (result) {
                if (result.isConfirmed) form.submit();
            });
            return false;
        }
        return confirm('Bạn có chắc chắn muốn hủy booking #' + bookingId + '?');
    }

    /* ── Pay button: disable after first click to prevent double-submit ── */
    function handlePayClick(form) {
        var btn = form.querySelector('button[type="submit"]');
        if (btn.disabled) return false; // already submitted
        btn.disabled = true;
        btn.innerHTML = '<span class="animate-spin">⏳</span> Đang xử lý...';
        return true;
    }

    /* ── Countdown timer for PENDING hold expiry ── */
    (function initCountdowns() {
        var elements = document.querySelectorAll('[id^="countdown-"]');
        elements.forEach(function (el) {
            var expireStr = el.getAttribute('data-expire');
            if (!expireStr) return;
            // Format from Java LocalDateTime: "2026-03-09T15:30:00"
            var expireTime = new Date(expireStr.replace('T', ' '));
            var interval = setInterval(function () {
                var now = new Date();
                var diff = expireTime - now;
                if (diff <= 0) {
                    el.textContent = 'Đã hết hạn thanh toán';
                    el.classList.remove('text-amber-600');
                    el.classList.add('text-red-600');
                    clearInterval(interval);
                    // Hide pay button for this card
                    var bookingId = el.id.replace('countdown-', '');
                    var payForms = document.querySelectorAll('input[value="' + bookingId + '"]');
                    payForms.forEach(function (input) {
                        if (input.name === 'bookingId') {
                            var btn = input.closest('form').querySelector('button');
                            if (btn) btn.style.display = 'none';
                        }
                    });
                    return;
                }
                var mins = Math.floor(diff / 60000);
                var secs = Math.floor((diff % 60000) / 1000);
                el.textContent = 'Hết hạn sau: ' + mins + 'm ' + (secs < 10 ? '0' : '') + secs + 's';
            }, 1000);
        });
    })();
</script>
