<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<style>
    .detail-section {
        background: white;
        border: 1px solid #F3F4F6;
        border-radius: 0.75rem;
        padding: 1.25rem;
        margin-bottom: 1rem;
    }
    .detail-section h3 {
        font-size: 0.8rem;
        font-weight: 700;
        color: #374151;
        text-transform: uppercase;
        letter-spacing: 0.05em;
        margin-bottom: 0.75rem;
        display: flex;
        align-items: center;
        gap: 0.5rem;
    }
    .detail-row {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 0.5rem 0;
        border-bottom: 1px solid #F9FAFB;
    }
    .detail-row:last-child {
        border-bottom: none;
    }
    .detail-label {
        font-size: 0.8rem;
        color: #6B7280;
    }
    .detail-value {
        font-size: 0.8rem;
        font-weight: 600;
        color: #1F2937;
        text-align: right;
    }
    .slot-row {
        display: flex;
        align-items: center;
        justify-content: space-between;
        padding: 0.6rem 0.75rem;
        background-color: #F9FAFB;
        border-radius: 0.5rem;
        margin-bottom: 0.5rem;
    }
    .slot-row:last-child {
        margin-bottom: 0;
    }
    /* Reuse badge styles from customer_history */
    .detail-badge {
        display: inline-flex;
        align-items: center;
        padding: 0.25rem 0.75rem;
        border-radius: 9999px;
        font-size: 0.7rem;
        font-weight: 600;
        text-transform: uppercase;
        letter-spacing: 0.025em;
    }
    .detail-badge-pending { background-color: #FEF3C7; color: #92400E; border: 1px solid #FCD34D; }
    .detail-badge-confirmed { background-color: #DBEAFE; color: #1E40AF; border: 1px solid #93C5FD; }
    .detail-badge-completed { background-color: #D1FAE5; color: #065F46; border: 1px solid #6EE7B7; }
    .detail-badge-cancelled { background-color: #FEE2E2; color: #991B1B; border: 1px solid #FCA5A5; }
    .detail-badge-expired { background-color: #F3F4F6; color: #6B7280; border: 1px solid #D1D5DB; }
    .detail-type-single { background-color: #EDE9FE; color: #5B21B6; }
    .detail-type-recurring { background-color: #FCE7F3; color: #9D174D; }
</style>

<div class="flex flex-col h-full bg-gray-50">
    <c:choose>
        <c:when test="${not empty bookingDetail}">
            <c:set var="d" value="${bookingDetail}" />

            <!-- Header with Back button -->
            <div class="bg-white p-5 border-b border-gray-100">
                <div class="flex items-center space-x-3 mb-3">
                    <a href="${pageContext.request.contextPath}/my-bookings"
                       class="w-8 h-8 flex items-center justify-center rounded-full hover:bg-gray-100 transition-colors">
                        <i data-lucide="arrow-left" class="w-5 h-5 text-gray-600"></i>
                    </a>
                    <div>
                        <h1 class="text-lg font-bold text-gray-800">Chi tiết đặt sân</h1>
                        <span class="text-xs text-gray-400">Booking #${d.bookingId}</span>
                    </div>
                </div>
                <div class="flex items-center space-x-2">
                    <span class="detail-badge ${d.bookingType == 'SINGLE' ? 'detail-type-single' : 'detail-type-recurring'}">
                        <c:choose>
                            <c:when test="${d.bookingType == 'SINGLE'}">Đặt lẻ</c:when>
                            <c:otherwise>Đặt cố định</c:otherwise>
                        </c:choose>
                    </span>
                    <c:choose>
                        <c:when test="${d.bookingStatus == 'PENDING'}">
                            <span class="detail-badge detail-badge-pending">Chờ thanh toán</span>
                        </c:when>
                        <c:when test="${d.bookingStatus == 'CONFIRMED'}">
                            <span class="detail-badge detail-badge-confirmed">Đã xác nhận</span>
                        </c:when>
                        <c:when test="${d.bookingStatus == 'COMPLETED'}">
                            <span class="detail-badge detail-badge-completed">Hoàn thành</span>
                        </c:when>
                        <c:when test="${d.bookingStatus == 'CANCELLED'}">
                            <span class="detail-badge detail-badge-cancelled">Đã hủy</span>
                        </c:when>
                        <c:when test="${d.bookingStatus == 'EXPIRED'}">
                            <span class="detail-badge detail-badge-expired">Hủy do quá giờ</span>
                        </c:when>
                    </c:choose>
                </div>
            </div>

            <div class="flex-1 overflow-y-auto p-5 space-y-0">

                <!-- Facility Info -->
                <div class="detail-section">
                    <h3>
                        <i data-lucide="map-pin" class="w-4 h-4 text-green-600"></i>
                        Thông tin sân
                    </h3>
                    <div class="flex items-start space-x-3">
                        <c:choose>
                            <c:when test="${not empty d.thumbnailPath}">
                                <img src="${pageContext.request.contextPath}/uploads/${d.thumbnailPath}"
                                     alt="${d.facilityName}"
                                     class="w-16 h-16 rounded-lg object-cover flex-shrink-0 border border-gray-100" />
                            </c:when>
                            <c:otherwise>
                                <div class="w-16 h-16 rounded-lg bg-green-50 flex items-center justify-center flex-shrink-0">
                                    <i data-lucide="map-pin" class="w-7 h-7 text-green-600"></i>
                                </div>
                            </c:otherwise>
                        </c:choose>
                        <div>
                            <h4 class="text-sm font-bold text-gray-800">${d.facilityName}</h4>
                            <p class="text-xs text-gray-500 mt-1">${d.fullAddress}</p>
                        </div>
                    </div>
                </div>

                <!-- Booking Info -->
                <div class="detail-section">
                    <h3>
                        <i data-lucide="calendar" class="w-4 h-4 text-blue-600"></i>
                        Thông tin đặt sân
                    </h3>
                    <div class="detail-row">
                        <span class="detail-label">Ngày đặt sân</span>
                        <span class="detail-value">
                            <fmt:parseDate value="${d.bookingDate}" pattern="yyyy-MM-dd" var="parsedBookingDate" type="date" />
                            <fmt:formatDate value="${parsedBookingDate}" pattern="dd/MM/yyyy" />
                        </span>
                    </div>
                    <div class="detail-row">
                        <span class="detail-label">Trạng thái booking</span>
                        <span class="detail-value">
                            <c:choose>
                                <c:when test="${d.bookingStatus == 'PENDING'}">Chờ thanh toán</c:when>
                                <c:when test="${d.bookingStatus == 'CONFIRMED'}">Đã xác nhận</c:when>
                                <c:when test="${d.bookingStatus == 'COMPLETED'}">Hoàn thành</c:when>
                                <c:when test="${d.bookingStatus == 'CANCELLED'}">Đã hủy</c:when>
                                <c:when test="${d.bookingStatus == 'EXPIRED'}">Hủy do quá giờ thanh toán</c:when>
                            </c:choose>
                        </span>
                    </div>
                    <div class="detail-row">
                        <span class="detail-label">Tổng giờ chơi</span>
                        <span class="detail-value">
                            <fmt:formatNumber value="${d.totalHours}" maxFractionDigits="1" /> giờ
                        </span>
                    </div>
                    <c:if test="${not empty d.createdAt}">
                        <div class="detail-row">
                            <span class="detail-label">Ngày tạo</span>
                            <span class="detail-value text-gray-500 font-normal">
                                ${d.createdAt}
                            </span>
                        </div>
                    </c:if>
                </div>

                <!-- Slot Details -->
                <div class="detail-section">
                    <h3>
                        <i data-lucide="clock" class="w-4 h-4 text-orange-500"></i>
                        Chi tiết lịch đặt (${fn:length(d.slots)} slot)
                    </h3>
                    <c:forEach var="slot" items="${d.slots}">
                        <div class="slot-row">
                            <div class="flex items-center space-x-2">
                                <div class="w-2 h-2 rounded-full
                                    <c:choose>
                                        <c:when test='${slot.slotStatus == "PENDING"}'>bg-yellow-400</c:when>
                                        <c:when test='${slot.slotStatus == "CHECKED_IN"}'>bg-blue-400</c:when>
                                        <c:when test='${slot.slotStatus == "CHECK_OUT"}'>bg-green-400</c:when>
                                        <c:when test='${slot.slotStatus == "CANCELLED"}'>bg-red-400</c:when>
                                        <c:otherwise>bg-gray-400</c:otherwise>
                                    </c:choose>
                                "></div>
                                <div>
                                    <span class="text-xs font-semibold text-gray-700">${slot.courtName}</span>
                                    <span class="text-xs text-gray-500 ml-1">${slot.startTime} - ${slot.endTime}</span>
                                </div>
                            </div>
                            <span class="text-xs font-bold text-gray-700">
                                <fmt:formatNumber value="${slot.price}" type="number" groupingUsed="true" />đ
                            </span>
                        </div>
                    </c:forEach>
                </div>

                <!-- Payment Info -->
                <div class="detail-section">
                    <h3>
                        <i data-lucide="credit-card" class="w-4 h-4 text-emerald-600"></i>
                        Thanh toán
                    </h3>
                    <div class="detail-row">
                        <span class="detail-label">Trạng thái thanh toán</span>
                        <span class="detail-value">
                            <c:choose>
                                <c:when test="${d.paymentStatus == 'UNPAID'}">
                                    <span class="text-red-600">Chưa thanh toán</span>
                                </c:when>
                                <c:when test="${d.paymentStatus == 'PARTIAL'}">
                                    <span class="text-yellow-600">Đã cọc</span>
                                </c:when>
                                <c:when test="${d.paymentStatus == 'PAID'}">
                                    <span class="text-green-600">Đã thanh toán</span>
                                </c:when>
                                <c:otherwise>
                                    <span class="text-gray-500">-</span>
                                </c:otherwise>
                            </c:choose>
                        </span>
                    </div>
                    <div class="detail-row">
                        <span class="detail-label">Tổng tiền</span>
                        <span class="detail-value text-lg text-[#064E3B]">
                            <fmt:formatNumber value="${d.totalAmount}" type="number" groupingUsed="true" />đ
                        </span>
                    </div>
                    <c:if test="${d.paidAmount != null && d.paidAmount > 0}">
                        <div class="detail-row">
                            <span class="detail-label">Đã thanh toán</span>
                            <span class="detail-value text-green-600">
                                <fmt:formatNumber value="${d.paidAmount}" type="number" groupingUsed="true" />đ
                            </span>
                        </div>
                    </c:if>
                </div>

                <!-- Staff Contact -->
                <c:if test="${not empty d.staffPhone || not empty d.staffName}">
                    <div class="detail-section">
                        <h3>
                            <i data-lucide="phone" class="w-4 h-4 text-purple-600"></i>
                            Liên hệ sân
                        </h3>
                        <c:if test="${not empty d.staffName}">
                            <div class="detail-row">
                                <span class="detail-label">Nhân viên</span>
                                <span class="detail-value">${d.staffName}</span>
                            </div>
                        </c:if>
                        <c:if test="${not empty d.staffPhone}">
                            <div class="detail-row">
                                <span class="detail-label">Số điện thoại</span>
                                <a href="tel:${d.staffPhone}" class="detail-value text-blue-600 hover:underline">
                                    ${d.staffPhone}
                                </a>
                            </div>
                        </c:if>
                    </div>
                </c:if>

                <!-- Action Buttons -->
                <div class="flex items-center space-x-3 pt-2 pb-4">
                    <a href="${pageContext.request.contextPath}/my-bookings"
                       class="flex-1 text-center py-3 border border-gray-200 text-gray-600 rounded-xl text-sm font-semibold hover:bg-gray-50 transition-colors">
                        ← Quay lại
                    </a>
                    <c:if test="${d.bookingStatus == 'PENDING' && d.paymentStatus == 'UNPAID'}">
                        <button type="button" id="detailPayBtn"
                                class="flex-1 py-3 bg-emerald-600 text-white rounded-xl text-sm font-semibold hover:bg-emerald-700 transition-colors"
                                onclick="retryPaymentDetail(${d.bookingId}, this)">
                            Thanh toán
                        </button>
                    </c:if>
                    <c:if test="${(d.bookingStatus == 'PENDING' || d.bookingStatus == 'CONFIRMED') && d.paymentStatus == 'UNPAID'}">
                        <form method="post" action="${pageContext.request.contextPath}/my-bookings"
                              class="flex-1"
                              onsubmit="return confirmCancelDetail(this, ${d.bookingId})">
                            <input type="hidden" name="action" value="cancel" />
                            <input type="hidden" name="bookingId" value="${d.bookingId}" />
                            <button type="submit"
                                    class="w-full py-3 bg-red-600 text-white rounded-xl text-sm font-semibold hover:bg-red-700 transition-colors">
                                Hủy booking
                            </button>
                        </form>
                    </c:if>
                </div>
            </div>
        </c:when>
        <c:otherwise>
            <!-- Not found -->
            <div class="flex-1 flex flex-col items-center justify-center p-8 text-center">
                <div class="w-20 h-20 bg-red-50 rounded-full flex items-center justify-center mb-4">
                    <i data-lucide="alert-triangle" class="w-10 h-10 text-red-400"></i>
                </div>
                <p class="text-gray-600 font-semibold">Không tìm thấy booking</p>
                <p class="text-sm text-gray-400 mt-1">${errorMessage}</p>
                <a href="${pageContext.request.contextPath}/my-bookings"
                   class="mt-6 px-6 py-2.5 bg-[#064E3B] text-white rounded-lg text-sm font-semibold hover:bg-[#065F46] transition-colors">
                    Quay lại danh sách
                </a>
            </div>
        </c:otherwise>
    </c:choose>
</div>

<script>
    function confirmCancelDetail(form, bookingId) {
        if (typeof Swal !== 'undefined') {
            Swal.fire({
                title: 'Xác nhận hủy?',
                html: 'Bạn có chắc chắn muốn hủy booking <strong>#' + bookingId + '</strong>?<br><small class="text-gray-500">Hành động này không thể hoàn tác.</small>',
                icon: 'warning',
                showCancelButton: true,
                confirmButtonColor: '#DC2626',
                cancelButtonColor: '#6B7280',
                confirmButtonText: 'Hủy booking',
                cancelButtonText: 'Quay lại'
            }).then(function(result) {
                if (result.isConfirmed) {
                    form.submit();
                }
            });
            return false;
        }
        return confirm('Bạn có chắc chắn muốn hủy booking #' + bookingId + '?');
    }

    function retryPaymentDetail(bookingId, btnEl) {
        if (btnEl.disabled) return;
        btnEl.disabled = true;
        var origText = btnEl.innerHTML;
        btnEl.innerHTML = 'Đang xử lý...';

        fetch('${pageContext.request.contextPath}/api/payment/retry', {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify({ bookingId: bookingId })
        })
        .then(function(res) { return res.json(); })
        .then(function(json) {
            if (json.success && json.data && json.data.paymentUrl) {
                window.location.href = json.data.paymentUrl;
            } else {
                var msg = (json.error && json.error.message) || 'Không thể tạo link thanh toán.';
                if (typeof Swal !== 'undefined') {
                    Swal.fire({ icon: 'error', title: 'Lỗi', text: msg });
                } else { alert(msg); }
                btnEl.disabled = false;
                btnEl.innerHTML = origText;
            }
        })
        .catch(function() {
            if (typeof Swal !== 'undefined') {
                Swal.fire({ icon: 'error', title: 'Lỗi', text: 'Lỗi kết nối. Vui lòng thử lại.' });
            } else { alert('Lỗi kết nối.'); }
            btnEl.disabled = false;
            btnEl.innerHTML = origText;
        });
    }
</script>
