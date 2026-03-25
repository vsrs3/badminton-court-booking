<!-- customer_booking_detail.jsp -->

<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/customer/customer-booking-detail.css?v=<%= System.currentTimeMillis() %>">

<div class="flex flex-col h-full bg-gray-50">
    <c:choose>
        <c:when test="${not empty bookingDetail}">
            <c:set var="d" value="${bookingDetail}" />
            <c:set var="expandAllFuture" value="${expandAll == true}" />
            <c:set var="showPastSession" value="${showPast == true}" />
            <c:set var="futurePageValue" value="${empty futurePage ? 1 : futurePage}" />
            <c:set var="pastPageValue" value="${empty pastPage ? 1 : pastPage}" />

            <%-- Pre-compute button flags (same logic as list) --%>
            <c:set var="canPay" value="${d.bookingStatus == 'PENDING'
                and d.paymentStatus == 'UNPAID'
                and not empty d.holdExpiredAt}" />
            <c:set var="canPayRemaining" value="${d.bookingStatus == 'CONFIRMED'
                and d.paymentStatus == 'PARTIAL'}" />
            <c:set var="canCancel" value="${(d.bookingStatus == 'PENDING'
                or d.bookingStatus == 'CONFIRMED')
                and d.paymentStatus == 'UNPAID'}" />

            <!-- Header with Back button -->
            <div class="bg-white p-5 border-b border-gray-100">

                <!-- Row 1: Title + Back button -->
                <div class="detail-header">
                    <div class="detail-title-group">
                        <i data-lucide="calendar-check" class="detail-title-icon"></i>
                        <h1 class="detail-page-title">Chi tiết đặt sân</h1>
                    </div>
                    <a href="${pageContext.request.contextPath}/my-bookings" class="btn-back-home">
                        <i data-lucide="arrow-left" class="w-4 h-4"></i>
                        <span>Quay Lại</span>
                    </a>
                </div>

                <!-- Row 2: badges + booking id -->
                <div class="flex items-center space-x-2 mt-3">
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
                    <span class="text-xs text-gray-400">#${d.bookingId}</span>
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
                    <c:choose>
                        <c:when test="${d.bookingType == 'RECURRING'}">
                            <div class="detail-row">
                                <span class="detail-label">Thời gian áp dụng</span>
                                <span class="detail-value">
                                    <c:if test="${not empty d.recurringStartDate}">
                                        <fmt:parseDate value="${d.recurringStartDate}" pattern="yyyy-MM-dd" var="parsedRecurringStart" type="date" />
                                        <fmt:formatDate value="${parsedRecurringStart}" pattern="dd/MM/yyyy" />
                                    </c:if>
                                    <c:if test="${not empty d.recurringEndDate}">
                                        -
                                        <fmt:parseDate value="${d.recurringEndDate}" pattern="yyyy-MM-dd" var="parsedRecurringEnd" type="date" />
                                        <fmt:formatDate value="${parsedRecurringEnd}" pattern="dd/MM/yyyy" />
                                    </c:if>
                                </span>
                            </div>
                            <div class="detail-row">
                                <span class="detail-label">Pattern hàng tuần</span>
                                <span class="detail-value text-gray-700" style="white-space: pre-line;">${d.recurringPatternDetails}</span>
                            </div>
                        </c:when>
                        <c:otherwise>
                            <div class="detail-row">
                                <span class="detail-label">Ngày đặt sân</span>
                                <span class="detail-value">
                                    <fmt:parseDate value="${d.bookingDate}" pattern="yyyy-MM-dd" var="parsedBookingDate" type="date" />
                                    <fmt:formatDate value="${parsedBookingDate}" pattern="dd/MM/yyyy" />
                                </span>
                            </div>
                        </c:otherwise>
                    </c:choose>
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
                            <span class="detail-value text-gray-500 font-normal">${d.createdAtDisplay}</span>
                        </div>
                    </c:if>
                    <%-- Hold expiry info for PENDING --%>
                    <c:if test="${canPay and not empty d.holdExpiredAt}">
                        <div class="detail-row">
                            <span class="detail-label">Hạn thanh toán</span>
                            <span class="detail-value text-amber-600 font-semibold"
                                id="detail-countdown"
                                data-expire="${d.holdExpiredAt}">
                                Đang tải...
                            </span>
                        </div>
                    </c:if>
                </div>

                <!-- Slot Details — merged view (same as list) -->
                <div class="detail-section">
                    <h3>
                        <i data-lucide="clock" class="w-4 h-4 text-orange-500"></i>
                        Lịch đặt sân
                    </h3>
                    <c:choose>
                        <c:when test="${d.bookingType == 'RECURRING'}">
                            <div class="text-xs font-semibold text-gray-500 mb-2 uppercase tracking-wide">Sắp tới</div>
                            <div id="future-recurring-scroll"
                                 class="max-h-80 overflow-y-auto pr-1"
                                 data-booking-id="${d.bookingId}"
                                 data-next-page="${futurePageValue + 1}"
                                 data-has-more="${d.hasMoreFutureSessions ? '1' : '0'}">
                                <div id="future-recurring-list" class="space-y-0.5">
                                    <c:choose>
                                        <c:when test="${not empty d.recurringSessions}">
                                            <c:forEach var="ms" items="${d.recurringSessions}">
                                                <div class="slot-row future-session-row">
                                                    <div class="flex items-center space-x-2">
                                                        <div class="w-2 h-2 rounded-full bg-green-400"></div>
                                                        <div>
                                                            <span class="text-xs text-gray-500">
                                                                <fmt:parseDate value="${ms.bookingDate}" pattern="yyyy-MM-dd" var="parsedSessionDate" type="date" />
                                                                <fmt:formatDate value="${parsedSessionDate}" pattern="dd/MM/yyyy" />
                                                            </span>
                                                            <span class="text-xs font-semibold text-gray-700 ml-1">${ms.courtName}</span>
                                                            <span class="text-xs text-gray-500 ml-1">${ms.startTime} - ${ms.endTime}</span>
                                                            <c:if test="${ms.slotCount > 1}">
                                                                <span class="text-xs text-gray-400 ml-1">(${ms.slotCount} slot)</span>
                                                            </c:if>
                                                        </div>
                                                    </div>
                                                    <span class="text-xs font-bold text-gray-700">
                                                        <fmt:formatNumber value="${ms.totalPrice}" type="number" groupingUsed="true" />đ
                                                    </span>
                                                </div>
                                            </c:forEach>
                                        </c:when>
                                        <c:otherwise>
                                            <p class="text-xs text-gray-400">Không có lịch tương lai trong phạm vi hiển thị.</p>
                                        </c:otherwise>
                                    </c:choose>
                                </div>

                                <div id="future-recurring-loading" class="hidden py-2 text-center text-xs text-gray-400">
                                    Đang tải thêm lịch...
                                </div>
                                <div id="future-recurring-end"
                                     class="py-2 text-center text-xs text-gray-400 ${d.hasMoreFutureSessions ? 'hidden' : ''}">
                                    Đã hiển thị hết lịch tương lai.
                                </div>
                            </div>

                            <div class="mt-3 flex flex-wrap gap-2">
                                <c:if test="${not showPastSession}">
                                    <a href="${pageContext.request.contextPath}/my-bookings?action=detail&id=${d.bookingId}&expandAll=${expandAllFuture ? 1 : 0}&futurePage=${futurePageValue}&showPast=1&pastPage=1"
                                       class="px-3 py-2 rounded-lg text-xs font-semibold border border-gray-200 hover:bg-gray-50">
                                        Xem các ngày đã qua
                                    </a>
                                </c:if>
                            </div>

                            <c:if test="${showPastSession}">
                                <div class="text-xs font-semibold text-gray-500 mt-4 mb-2 uppercase tracking-wide">Đã qua</div>
                                <c:choose>
                                    <c:when test="${not empty d.pastRecurringSessions}">
                                        <c:forEach var="ms" items="${d.pastRecurringSessions}">
                                            <div class="slot-row opacity-85">
                                                <div class="flex items-center space-x-2">
                                                    <div class="w-2 h-2 rounded-full bg-gray-400"></div>
                                                    <div>
                                                        <span class="text-xs text-gray-500">
                                                            <fmt:parseDate value="${ms.bookingDate}" pattern="yyyy-MM-dd" var="parsedPastSessionDate" type="date" />
                                                            <fmt:formatDate value="${parsedPastSessionDate}" pattern="dd/MM/yyyy" />
                                                        </span>
                                                        <span class="text-xs font-semibold text-gray-700 ml-1">${ms.courtName}</span>
                                                        <span class="text-xs text-gray-500 ml-1">${ms.startTime} - ${ms.endTime}</span>
                                                    </div>
                                                </div>
                                                <span class="text-xs font-bold text-gray-700">
                                                    <fmt:formatNumber value="${ms.totalPrice}" type="number" groupingUsed="true" />đ
                                                </span>
                                            </div>
                                        </c:forEach>

                                        <div class="mt-3 flex flex-wrap gap-2">
                                            <c:if test="${d.hasMorePastSessions}">
                                                <a href="${pageContext.request.contextPath}/my-bookings?action=detail&id=${d.bookingId}&expandAll=${expandAllFuture ? 1 : 0}&futurePage=${futurePageValue}&showPast=1&pastPage=${pastPageValue + 1}"
                                                   class="px-3 py-2 rounded-lg text-xs font-semibold border border-gray-200 hover:bg-gray-50">
                                                    Xem thêm ngày đã qua
                                                </a>
                                            </c:if>
                                            <a href="${pageContext.request.contextPath}/my-bookings?action=detail&id=${d.bookingId}&expandAll=${expandAllFuture ? 1 : 0}&futurePage=${futurePageValue}&showPast=0&pastPage=1"
                                               class="px-3 py-2 rounded-lg text-xs font-semibold border border-gray-200 hover:bg-gray-50">
                                                Ẩn các ngày đã qua
                                            </a>
                                        </div>
                                    </c:when>
                                    <c:otherwise>
                                        <p class="text-xs text-gray-400">Chưa có phiên đã qua.</p>
                                    </c:otherwise>
                                </c:choose>
                            </c:if>
                        </c:when>
                        <c:when test="${not empty d.mergedSlots}">
                            <c:forEach var="ms" items="${d.mergedSlots}">
                                <div class="slot-row">
                                    <div class="flex items-center space-x-2">
                                        <div class="w-2 h-2 rounded-full bg-green-400"></div>
                                        <div>
                                            <span class="text-xs font-semibold text-gray-700">${ms.courtName}</span>
                                            <span class="text-xs text-gray-500 ml-1">${ms.startTime} - ${ms.endTime}</span>
                                            <c:if test="${ms.slotCount > 1}">
                                                <span class="text-xs text-gray-400 ml-1">(${ms.slotCount} slot)</span>
                                            </c:if>
                                        </div>
                                    </div>
                                    <span class="text-xs font-bold text-gray-700">
                                        <fmt:formatNumber value="${ms.totalPrice}" type="number" groupingUsed="true" />đ
                                    </span>
                                </div>
                            </c:forEach>
                        </c:when>
                        <c:otherwise>
                            <%-- Fallback: render raw slots if mergedSlots not available --%>
                            <c:forEach var="slot" items="${d.slots}">
                                <div class="slot-row">
                                    <div class="flex items-center space-x-2">
                                        <div class="w-2 h-2 rounded-full bg-yellow-400"></div>
                                        <div>
                                            <c:if test="${not empty slot.bookingDate}">
                                                <span class="text-xs text-gray-500">
                                                    <fmt:parseDate value="${slot.bookingDate}" pattern="yyyy-MM-dd" var="parsedRawSessionDate" type="date" />
                                                    <fmt:formatDate value="${parsedRawSessionDate}" pattern="dd/MM/yyyy" />
                                                </span>
                                            </c:if>
                                            <span class="text-xs font-semibold text-gray-700">${slot.courtName}</span>
                                            <span class="text-xs text-gray-500 ml-1">${slot.startTime} - ${slot.endTime}</span>
                                            <c:if test="${not empty slot.rentalItems}">
                                                <div class="mt-1">
                                                    <button type="button"
                                                            class="slot-rental-trigger"
                                                            onclick="openSlotRentalModal('slot-rental-modal-${slot.bookingSlotId}')">
                                                        <i data-lucide="package" class="w-3 h-3"></i>
                                                        Chi tiết đồ thuê
                                                    </button>
                                                </div>
                                            </c:if>
                                        </div>
                                    </div>
                                    <span class="text-xs font-bold text-gray-700">
                                        <fmt:formatNumber value="${slot.price}" type="number" groupingUsed="true" />đ
                                    </span>
                                </div>
                            </c:forEach>
                        </c:otherwise>
                    </c:choose>
                </div>

                <!-- Slot breakdown (toggle — chi tiết từng slot khi merged) -->
                <c:if test="${d.bookingType == 'SINGLE' and fn:length(d.mergedSlots) < fn:length(d.slots)}">
                    <div class="detail-section">
                        <button type="button"
                                class="text-xs text-gray-400 flex items-center gap-1 hover:text-gray-600 transition-colors"
                                onclick="toggleSlotBreakdown(this)">
                            <i data-lucide="list" class="w-3.5 h-3.5"></i>
                            Xem chi tiết từng slot (${fn:length(d.slots)} slot)
                            <i data-lucide="chevron-down" class="w-3 h-3" id="breakdown-chevron"></i>
                        </button>
                        <div id="slot-breakdown" class="hidden mt-2 space-y-1">
                            <c:forEach var="slot" items="${d.slots}">
                                <div class="slot-row opacity-70">
                                    <div class="flex items-center space-x-2">
                                        <div class="w-1.5 h-1.5 rounded-full
                                            <c:choose>
                                                <c:when test='${slot.slotStatus == "PENDING"}'>bg-yellow-400</c:when>
                                                <c:when test='${slot.slotStatus == "CHECKED_IN"}'>bg-blue-400</c:when>
                                                <c:when test='${slot.slotStatus == "CHECK_OUT"}'>bg-green-400</c:when>
                                                <c:when test='${slot.slotStatus == "CANCELLED"}'>bg-red-400</c:when>
                                                <c:otherwise>bg-gray-400</c:otherwise>
                                            </c:choose>
                                        "></div>
                                        <div>
                                            <span class="text-xs text-gray-600">${slot.courtName}</span>
                                            <span class="text-xs text-gray-400 ml-1">${slot.startTime}-${slot.endTime}</span>
                                            <c:if test="${not empty slot.rentalItems}">
                                                <div class="mt-1">
                                                    <button type="button"
                                                            class="slot-rental-trigger"
                                                            onclick="openSlotRentalModal('slot-rental-modal-${slot.bookingSlotId}')">
                                                        <i data-lucide="package" class="w-3 h-3"></i>
                                                        Chi tiết đồ thuê
                                                    </button>
                                                </div>
                                            </c:if>
                                        </div>
                                    </div>
                                    <span class="text-xs text-gray-500">
                                        <fmt:formatNumber value="${slot.price}" type="number" groupingUsed="true" />đ
                                    </span>
                                </div>
                            </c:forEach>
                        </div>
                    </div>
                </c:if>

                <c:if test="${not empty d.slots}">
                    <c:forEach var="slot" items="${d.slots}">
                        <c:if test="${not empty slot.rentalItems}">
                            <div id="slot-rental-modal-${slot.bookingSlotId}"
                                 class="slot-rental-modal hidden"
                                 onclick="handleSlotRentalBackdrop(event, 'slot-rental-modal-${slot.bookingSlotId}')">
                                <div class="slot-rental-dialog" role="dialog" aria-modal="true"
                                     aria-labelledby="slot-rental-title-${slot.bookingSlotId}">
                                    <div class="slot-rental-header">
                                        <div>
                                            <h4 id="slot-rental-title-${slot.bookingSlotId}">Chi tiết đồ thuê</h4>
                                            <p>
                                                ${slot.courtName} · ${slot.startTime} - ${slot.endTime}
                                                <c:if test="${not empty slot.bookingDate}">
                                                    ·
                                                    <fmt:parseDate value="${slot.bookingDate}" pattern="yyyy-MM-dd" var="parsedRentalSlotDate" type="date" />
                                                    <fmt:formatDate value="${parsedRentalSlotDate}" pattern="dd/MM/yyyy" />
                                                </c:if>
                                            </p>
                                        </div>
                                        <button type="button"
                                                class="slot-rental-close"
                                                aria-label="Đóng"
                                                onclick="closeSlotRentalModal('slot-rental-modal-${slot.bookingSlotId}')">
                                            &times;
                                        </button>
                                    </div>

                                    <div class="slot-rental-table-wrap">
                                        <table class="slot-rental-table">
                                            <thead>
                                                <tr>
                                                    <th>STT</th>
                                                    <th>Tên đồ</th>
                                                    <th>Số lượng</th>
                                                    <th>Giá thuê/30 phút</th>
                                                    <th>Tổng giá</th>
                                                </tr>
                                                <tr class="slot-rental-head-fixed">
                                                    <th>STT</th>
                                                    <th>T&ecirc;n &#273;&#7891;</th>
                                                    <th>S&#7889; l&#432;&#7907;ng</th>
                                                    <th>Gi&aacute; thu&ecirc;/30 ph&uacute;t</th>
                                                    <th>T&#7893;ng gi&aacute;</th>
                                                </tr>
                                            </thead>
                                            <tbody>
                                                <c:forEach var="item" items="${slot.rentalItems}" varStatus="rentalStatus">
                                                    <tr>
                                                        <td>${rentalStatus.index + 1}</td>
                                                        <td>${item.inventoryName}</td>
                                                        <td>${item.quantity}</td>
                                                        <td>
                                                            <fmt:formatNumber value="${item.unitPrice}" type="number" groupingUsed="true" />đ
                                                        </td>
                                                        <td>
                                                            <fmt:formatNumber value="${item.lineTotal}" type="number" groupingUsed="true" />đ
                                                        </td>
                                                    </tr>
                                                </c:forEach>
                                            </tbody>
                                            <tfoot>
                                                <tr>
                                                    <th colspan="4">Tổng đồ thuê slot này</th>
                                                    <th>
                                                        <fmt:formatNumber value="${slot.rentalTotal}" type="number" groupingUsed="true" />đ
                                                    </th>
                                                </tr>
                                            </tfoot>
                                        </table>
                                    </div>
                                </div>
                            </div>
                        </c:if>
                    </c:forEach>
                </c:if>

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
                                <c:otherwise><span class="text-gray-500">-</span></c:otherwise>
                            </c:choose>
                        </span>
                    </div>
                    <div class="detail-row">
                        <span class="detail-label">Tổng tiền</span>
                        <span class="detail-value text-lg text-[#064E3B]">
                            <fmt:formatNumber value="${d.totalAmount}" type="number" groupingUsed="true" />đ
                        </span>
                    </div>
                    <c:if test="${d.paidAmount != null and d.paidAmount > 0}">
                        <div class="detail-row">
                            <span class="detail-label">Đã thanh toán</span>
                            <span class="detail-value text-green-600">
                                <fmt:formatNumber value="${d.paidAmount}" type="number" groupingUsed="true" />đ
                            </span>
                        </div>
                        <c:if test="${d.paymentStatus == 'PARTIAL'}">
                            <div class="detail-row">
                                <span class="detail-label">Còn lại</span>
                                <span class="detail-value text-orange-600 font-bold">
                                    <fmt:formatNumber value="${d.totalAmount - d.paidAmount}" type="number" groupingUsed="true" />đ
                                </span>
                            </div>
                        </c:if>
                    </c:if>
                </div>

                <!-- Staff Contact -->
                <c:if test="${not empty d.staffPhone or not empty d.staffName}">
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
                <div class="flex flex-col gap-2 pt-2 pb-4">
                    <%-- Row 1: Thanh Toán (full width when shown) --%>
                    <c:if test="${canPay}">
                        <form method="post" action="${pageContext.request.contextPath}/my-bookings"
                              onsubmit="return handlePayClick(this)">
                            <input type="hidden" name="action" value="retryPayment" />
                            <input type="hidden" name="bookingId" value="${d.bookingId}" />
                            <button type="submit"
                                    class="w-full py-3 bg-green-600 text-white rounded-xl text-sm font-semibold hover:bg-green-700 transition-colors flex items-center justify-center gap-2">
                                <i data-lucide="credit-card" class="w-4 h-4"></i> Thanh Toán Ngay
                            </button>
                        </form>
                    </c:if>

                    <%-- Row 2: Thanh Toán Phần Còn Lại --%>
                    <c:if test="${canPayRemaining}">
                        <form method="post" action="${pageContext.request.contextPath}/my-bookings"
                              onsubmit="return handlePayClick(this)">
                            <input type="hidden" name="action" value="payRemaining" />
                            <input type="hidden" name="bookingId" value="${d.bookingId}" />
                            <button type="submit"
                                    class="w-full py-3 bg-orange-500 text-white rounded-xl text-sm font-semibold hover:bg-orange-600 transition-colors flex items-center justify-center gap-2">
                                <i data-lucide="banknote" class="w-4 h-4"></i>
                                Thanh Toán Phần Còn Lại
                                <c:if test="${d.paidAmount != null}">
                                    (<fmt:formatNumber value="${d.totalAmount - d.paidAmount}" type="number" groupingUsed="true" />đ)
                                </c:if>
                            </button>
                        </form>
                    </c:if>

                    <%-- Row 3: Hủy + Quay lại side by side --%>
                    <div class="flex items-center gap-2">
                        <a href="${pageContext.request.contextPath}/my-bookings"
                           class="flex-1 text-center py-3 border border-gray-200 text-gray-600 rounded-xl text-sm font-semibold hover:bg-gray-50 transition-colors">
                            ← Quay lại
                        </a>
                        <c:if test="${canCancel}">
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
    function openSlotRentalModal(modalId) {
        var modal = document.getElementById(modalId);
        if (!modal) return;
        modal.classList.remove('hidden');
        document.body.style.overflow = 'hidden';
    }

    function closeSlotRentalModal(modalId) {
        var modal = document.getElementById(modalId);
        if (!modal) return;
        modal.classList.add('hidden');

        if (!document.querySelector('.slot-rental-modal:not(.hidden)')) {
            document.body.style.overflow = '';
        }
    }

    function handleSlotRentalBackdrop(event, modalId) {
        if (event.target && event.target.id === modalId) {
            closeSlotRentalModal(modalId);
        }
    }

    document.addEventListener('keydown', function (event) {
        if (event.key !== 'Escape') return;
        var openedModal = document.querySelector('.slot-rental-modal:not(.hidden)');
        if (!openedModal) return;
        closeSlotRentalModal(openedModal.id);
    });
    /* ── Toggle slot breakdown ── */
    function toggleSlotBreakdown(btn) {
        var panel   = document.getElementById('slot-breakdown');
        var chevron = document.getElementById('breakdown-chevron');
        var hidden  = panel.classList.toggle('hidden');
        if (chevron) {
            chevron.setAttribute('data-lucide', hidden ? 'chevron-down' : 'chevron-up');
            if (typeof lucide !== 'undefined') lucide.createIcons();
        }
    }

    /* ── Pay button: disable after click to prevent double-submit ── */
    function handlePayClick(form) {
        var btn = form.querySelector('button[type="submit"]');
        if (btn.disabled) return false;
        btn.disabled = true;
        btn.innerHTML = '⏳ Đang xử lý...';
        return true;
    }

    /* ── Cancel confirmation ── */
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
                if (result.isConfirmed) form.submit();
            });
            return false;
        }
        return confirm('Bạn có chắc chắn muốn hủy booking #' + bookingId + '?');
    }

    /* ── Infinite scroll for recurring future sessions ── */
    (function () {
        var wrap = document.getElementById('future-recurring-scroll');
        var list = document.getElementById('future-recurring-list');
        var loading = document.getElementById('future-recurring-loading');
        var endLabel = document.getElementById('future-recurring-end');
        if (!wrap || !list || !loading || !endLabel) return;

        var bookingId = wrap.getAttribute('data-booking-id');
        var nextPage = parseInt(wrap.getAttribute('data-next-page') || '2', 10);
        var hasMore = wrap.getAttribute('data-has-more') === '1';
        var isLoading = false;
        var contextPath = '${pageContext.request.contextPath}';

        function formatPrice(raw) {
            var value = Number(raw || 0);
            return new Intl.NumberFormat('vi-VN').format(isNaN(value) ? 0 : value) + 'đ';
        }

        function createSessionRow(item) {
            var row = document.createElement('div');
            row.className = 'slot-row future-session-row';

            var left = document.createElement('div');
            left.className = 'flex items-center space-x-2';

            var dot = document.createElement('div');
            dot.className = 'w-2 h-2 rounded-full bg-green-400';

            var textWrap = document.createElement('div');
            var date = document.createElement('span');
            date.className = 'text-xs text-gray-500';
            date.textContent = item.bookingDateDisplay || '';

            var court = document.createElement('span');
            court.className = 'text-xs font-semibold text-gray-700 ml-1';
            court.textContent = item.courtName || '';

            var time = document.createElement('span');
            time.className = 'text-xs text-gray-500 ml-1';
            time.textContent = (item.startTime || '') + ' - ' + (item.endTime || '');

            textWrap.appendChild(date);
            textWrap.appendChild(court);
            textWrap.appendChild(time);

            if ((item.slotCount || 0) > 1) {
                var count = document.createElement('span');
                count.className = 'text-xs text-gray-400 ml-1';
                count.textContent = '(' + item.slotCount + ' slot)';
                textWrap.appendChild(count);
            }

            left.appendChild(dot);
            left.appendChild(textWrap);

            var amount = document.createElement('span');
            amount.className = 'text-xs font-bold text-gray-700';
            amount.textContent = formatPrice(item.totalPrice);

            row.appendChild(left);
            row.appendChild(amount);
            return row;
        }

        function loadMoreFutureSessions() {
            if (!hasMore || isLoading) return;
            isLoading = true;
            loading.classList.remove('hidden');

            var url = contextPath + '/my-bookings?action=future-sessions&id=' + encodeURIComponent(bookingId)
                + '&futurePage=' + encodeURIComponent(nextPage);

            fetch(url)
                .then(function (res) { return res.json(); })
                .then(function (json) {
                    if (!json || json.success !== true || !json.data) return;
                    var sessions = json.data.sessions || [];
                    sessions.forEach(function (item) {
                        list.appendChild(createSessionRow(item));
                    });

                    hasMore = json.data.hasMore === true;
                    nextPage = Number(json.data.nextPage || (nextPage + 1));
                    wrap.setAttribute('data-has-more', hasMore ? '1' : '0');
                    wrap.setAttribute('data-next-page', String(nextPage));
                    endLabel.classList.toggle('hidden', hasMore);
                })
                .catch(function () {
                    // Keep silent to avoid noisy popups while user scrolls.
                })
                .finally(function () {
                    isLoading = false;
                    loading.classList.add('hidden');
                });
        }

        wrap.addEventListener('scroll', function () {
            if (!hasMore || isLoading) return;
            var remain = wrap.scrollHeight - wrap.scrollTop - wrap.clientHeight;
            if (remain <= 80) {
                loadMoreFutureSessions();
            }
        });

        // Auto-load if first batch is too short to create scrollbar.
        if (hasMore && wrap.scrollHeight <= wrap.clientHeight + 20) {
            loadMoreFutureSessions();
        }
    })();

    /* ── Detail countdown timer for PENDING hold expiry ── */
    (function() {
        var el = document.getElementById('detail-countdown');
        if (!el) return;
        var expireStr = el.getAttribute('data-expire');
        if (!expireStr) return;
        var expireTime = new Date(expireStr.replace('T', ' '));
        var interval = setInterval(function() {
            var diff = expireTime - new Date();
            if (diff <= 0) {
                el.textContent = 'Đã hết hạn thanh toán';
                el.classList.remove('text-amber-600');
                el.classList.add('text-red-600');
                clearInterval(interval);
                // Hide pay button
                var payBtn = document.querySelector('button[type="submit"]');
                if (payBtn) payBtn.style.display = 'none';
                return;
            }
            var mins = Math.floor(diff / 60000);
            var secs = Math.floor((diff % 60000) / 1000);
            el.textContent = 'Hết hạn sau: ' + mins + 'm ' + (secs < 10 ? '0' : '') + secs + 's';
        }, 1000);
    })();
</script>
