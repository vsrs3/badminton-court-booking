<!-- customer_history.jsp -->

<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/customer/customer-booking-history.css">
<%-- <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/customer/customer-review.css"> --%>

<div class="flex flex-col h-full bg-white">
    <div class="p-6 pb-3">
        <div class="flex items-center justify-between mb-5">
            <div class="flex items-center space-x-2">
                <div class="text-green-600">
                    <i data-lucide="calendar-check" class="w-6 h-6"></i>
                </div>
                <h1 class="text-xl font-bold text-gray-800">Lịch sử đặt sân</h1>
            </div>
            <a href="${pageContext.request.contextPath}/home"
                class="flex items-center gap-2 p-2 hover:bg-gray-100 rounded-full transition-colors text-gray-600">
                <i data-lucide="arrow-left" class="w-6 h-6"></i>
                <span>Quay Lại Trang Chủ</span>
            </a>
        </div>

        <form method="get" action="${pageContext.request.contextPath}/my-bookings" class="mb-4">
            <div class="flex flex-wrap items-end gap-3">
                <div class="flex flex-col">
                    <label class="text-xs text-gray-500 font-medium mb-1">Từ ngày</label>
                    <input type="date" name="dateFrom" value="${dateFrom}" class="search-input" />
                </div>
                <div class="flex flex-col">
                    <label class="text-xs text-gray-500 font-medium mb-1">Đến ngày</label>
                    <input type="date" name="dateTo" value="${dateTo}" class="search-input" />
                </div>
                <input type="hidden" name="status" id="hiddenStatus" value="${selectedStatus}" />
                <button type="submit" class="flex items-center space-x-1.5 px-4 py-2 bg-[#064E3B] text-white rounded-lg text-sm font-medium hover:bg-[#065F46] transition-colors">
                    <i data-lucide="search" class="w-4 h-4"></i>
                    <span>Tìm kiếm</span>
                </button>
                <a href="${pageContext.request.contextPath}/my-bookings"
                   class="px-4 py-2 border border-gray-200 text-gray-500 rounded-lg text-sm font-medium hover:bg-gray-50 transition-colors">
                    Xóa bộ lọc
                </a>
            </div>
        </form>

        <div class="flex items-center space-x-2 overflow-x-auto pb-3 no-scrollbar" id="filter-tabs">
            <button data-status="all" class="filter-btn <c:if test='${selectedStatus == "all" || empty selectedStatus}'>active</c:if>" onclick="filterByStatus('all')">Tất cả</button>
            <button data-status="PENDING" class="filter-btn <c:if test='${selectedStatus == "PENDING"}'>active</c:if>" onclick="filterByStatus('PENDING')">Chờ thanh toán</button>
            <button data-status="CONFIRMED" class="filter-btn <c:if test='${selectedStatus == "CONFIRMED"}'>active</c:if>" onclick="filterByStatus('CONFIRMED')">Đã xác nhận</button>
            <button data-status="COMPLETED" class="filter-btn <c:if test='${selectedStatus == "COMPLETED"}'>active</c:if>" onclick="filterByStatus('COMPLETED')">Hoàn thành</button>
            <button data-status="CANCELLED" class="filter-btn <c:if test='${selectedStatus == "CANCELLED"}'>active</c:if>" onclick="filterByStatus('CANCELLED')">Đã hủy</button>
            <button data-status="EXPIRED" class="filter-btn <c:if test='${selectedStatus == "EXPIRED"}'>active</c:if>" onclick="filterByStatus('EXPIRED')">Hết hạn</button>
        </div>
        <div class="h-[1px] bg-gray-100 w-full"></div>
    </div>

    <c:if test="${not empty sessionScope.successMessage}">
        <div class="mx-6 mb-3 p-3 bg-green-50 border border-green-200 rounded-lg flex items-center space-x-2">
            <i data-lucide="check-circle" class="w-5 h-5 text-green-600 flex-shrink-0"></i>
            <span class="text-sm text-green-800 font-medium">${sessionScope.successMessage}</span>
        </div>
        <c:remove var="successMessage" scope="session" />
    </c:if>
    <c:if test="${not empty sessionScope.errorMessage}">
        <div class="mx-6 mb-3 p-3 bg-red-50 border border-red-200 rounded-lg flex items-center space-x-2">
            <i data-lucide="alert-circle" class="w-5 h-5 text-red-600 flex-shrink-0"></i>
            <span class="text-sm text-red-800 font-medium">${sessionScope.errorMessage}</span>
        </div>
        <c:remove var="errorMessage" scope="session" />
    </c:if>

    <c:choose>
        <c:when test="${not empty bookings}">
            <div class="flex-1 overflow-y-auto px-6 pb-6 space-y-3">
                <div class="text-xs text-gray-400 font-medium pt-2 pb-1">
                    Tìm thấy <strong class="text-gray-600">${fn:length(bookings)}</strong> lịch đặt
                </div>
                <c:forEach var="booking" items="${bookings}">
                    <div class="booking-card rounded-xl bg-white p-4">
                        <div class="flex items-start justify-between mb-3">
                            <div class="flex items-center space-x-2">
                                <span class="badge ${booking.bookingType == 'SINGLE' ? 'type-single' : 'type-recurring'}">
                                    <c:choose>
                                        <c:when test="${booking.bookingType == 'SINGLE'}">Đặt lẻ</c:when>
                                        <c:otherwise>Đặt cố định</c:otherwise>
                                    </c:choose>
                                </span>
                                <c:choose>
                                    <c:when test="${booking.bookingStatus == 'PENDING'}"><span class="badge badge-pending">Chờ thanh toán</span></c:when>
                                    <c:when test="${booking.bookingStatus == 'CONFIRMED'}"><span class="badge badge-confirmed">Đã xác nhận</span></c:when>
                                    <c:when test="${booking.bookingStatus == 'COMPLETED'}"><span class="badge badge-completed">Hoàn thành</span></c:when>
                                    <c:when test="${booking.bookingStatus == 'CANCELLED'}"><span class="badge badge-cancelled">Đã hủy</span></c:when>
                                    <c:when test="${booking.bookingStatus == 'EXPIRED'}"><span class="badge badge-expired">Hủy do quá giờ</span></c:when>
                                </c:choose>
                            </div>
                            <span class="text-xs text-gray-400">#${booking.bookingId}</span>
                        </div>

                        <div class="flex items-start space-x-3 mb-3">
                            <c:choose>
                                <c:when test="${not empty booking.thumbnailPath}">
                                    <img src="${pageContext.request.contextPath}/uploads/${booking.thumbnailPath}" class="w-14 h-14 rounded-lg object-cover flex-shrink-0 border border-gray-100" />
                                </c:when>
                                <c:otherwise>
                                    <div class="w-14 h-14 rounded-lg bg-green-50 flex items-center justify-center flex-shrink-0 border border-green-100">
                                        <i data-lucide="map-pin" class="w-6 h-6 text-green-600"></i>
                                    </div>
                                </c:otherwise>
                            </c:choose>
                            <div class="min-w-0 flex-1">
                                <h3 class="text-sm font-bold text-gray-800 truncate">${booking.facilityName}</h3>
                                <p class="text-xs text-gray-500 mt-0.5 truncate">${booking.fullAddress}</p>
                            </div>
                        </div>

                        <div class="bg-gray-50 rounded-lg p-3 mb-3 space-y-1.5">
                            <div class="flex items-center space-x-2">
                                <i data-lucide="calendar" class="w-3.5 h-3.5 text-gray-400"></i>
                                <span class="text-xs text-gray-600 font-medium">
                                    <fmt:parseDate value="${booking.bookingDate}" pattern="yyyy-MM-dd" var="parsedDate" type="date" />
                                    <fmt:formatDate value="${parsedDate}" pattern="dd/MM/yyyy" />
                                </span>
                            </div>
                            <div class="flex items-start space-x-2">
                                <i data-lucide="clock" class="w-3.5 h-3.5 text-gray-400 mt-0.5"></i>
                                <span class="text-xs text-gray-600">${booking.slotDetails}</span>
                            </div>
                        </div>

                        <div class="flex items-center justify-between flex-wrap gap-2">
                            <div>
                                <span class="text-sm font-bold text-gray-800">
                                    <fmt:formatNumber value="${booking.totalAmount}" type="number" groupingUsed="true" />đ
                                </span>
                                <c:if test="${not empty booking.paymentStatus}">
                                    <span class="text-xs ml-1.5 ${booking.paymentStatus == 'PAID' ? 'pay-paid' : (booking.paymentStatus == 'PARTIAL' ? 'pay-partial' : 'pay-unpaid')}">
                                        • ${booking.paymentStatus == 'PAID' ? 'Đã thanh toán' : (booking.paymentStatus == 'PARTIAL' ? 'Đã cọc' : 'Chưa thanh toán')}
                                    </span>
                                </c:if>
                            </div>
                            <div class="flex items-center space-x-2 flex-wrap gap-1">
                                <%-- Cancel button --%>
                                <c:if test="${(booking.bookingStatus == 'PENDING' || booking.bookingStatus == 'CONFIRMED') && booking.paymentStatus == 'UNPAID'}">
                                    <form method="post" action="${pageContext.request.contextPath}/my-bookings" onsubmit="return confirmCancel(this, ${booking.bookingId})">
                                        <input type="hidden" name="action" value="cancel" />
                                        <input type="hidden" name="bookingId" value="${booking.bookingId}" />
                                        <button type="submit" class="cancel-btn">Hủy</button>
                                    </form>
                                </c:if>

                                <%-- ========================================
                                 Review buttons — only for COMPLETED bookings 
                                =============================================--%>
                                <c:if test="${booking.bookingStatus == 'COMPLETED'}">
                                    <c:choose>
                                        <c:when test="${booking.reviewed}">
                                            <%-- Already reviewed: Edit + View --%>
                                            
                                            <%-- <a href="${pageContext.request.contextPath}/review-locations/view/id=${booking.facilityId}"
                                               class="btn-review btn-review-view">
                                                <i data-lucide="eye" class="w-3.5 h-3.5"></i> Xem đánh giá
                                            </a> --%>
                                            
                                            <a href="${pageContext.request.contextPath}/review-locations/edit?booking_id=${booking.bookingId}&facility_id=${booking.facilityId}"
                                               class="btn-review btn-review-edit">
                                                <i data-lucide="pen" class="w-3.5 h-3.5"></i> Sửa đánh giá
                                            </a>
                                        </c:when>
                                        <c:otherwise>
                                            <%-- Not yet reviewed --%>
                                            <a href="${pageContext.request.contextPath}/review-locations/write?booking_id=${booking.bookingId}&facility_id=${booking.facilitId}"
                                               class="btn-review btn-review-write">
                                                <i data-lucide="star" class="w-3.5 h-3.5"></i> Viết Đánh giá
                                            </a>
                                        </c:otherwise>
                                    </c:choose>
                                </c:if>

                                <%-- Detail button --%>
                                <a href="${pageContext.request.contextPath}/my-bookings?action=detail&id=${booking.bookingId}"
                                   class="inline-flex items-center space-x-1 px-3 py-1.5 bg-[#064E3B] text-white rounded-md text-xs font-semibold hover:bg-[#065F46] transition-colors">
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
                <a href="${pageContext.request.contextPath}/home" class="mt-8 bg-[#9ef01a] text-[#004d3d] px-10 py-3.5 rounded-full font-bold text-sm shadow-md">Đặt sân ngay</a>
            </div>
        </c:otherwise>
    </c:choose>
</div>

<script>
    /* ── Filter ── */
    function filterByStatus(status) {
        var params = new URLSearchParams(window.location.search);
        params.set('status', status);
        window.location.href = '${pageContext.request.contextPath}/my-bookings?' + params.toString();
    }
    function confirmCancel(form, bookingId) {
        return confirm('Bạn có chắc chắn muốn hủy booking #' + bookingId + '?');
    }
</script>