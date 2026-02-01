<%--
    Document   : customer_historyBook
    Created on : Jan 25, 2026, 3:51:33 PM
    Author     : dattr
--%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>JSP Page</title>
</head>
<body>
<section class="profile-section">
    <div class="section-header">
        <h2><i class="fas fa-volleyball-ball"></i> Lịch sử đặt sân</h2>
        <div class="filter-tabs">
            <button class="tab-btn active" data-status="all">Tất cả</button>
            <button class="tab-btn" data-status="PENDING">Chờ xác nhận</button>
            <button class="tab-btn" data-status="CONFIRMED">Đã xác nhận</button>
            <button class="tab-btn" data-status="COMPLETED">Hoàn thành</button>
            <button class="tab-btn" data-status="CANCELLED">Đã hủy</button>
        </div>
    </div>

    <c:choose>
        <c:when test="${empty sessionScope.bookingList}">
            <div class="empty-state">
                <i class="fas fa-calendar-times"></i>
                <h3>Bạn chưa có lịch đặt sân</h3>
                <p>Hãy đặt sân ngay để trải nghiệm dịch vụ của chúng tôi</p>
                <a href="${pageContext.request.contextPath}/facility" class="btn btn-primary">
                    <i class="fas fa-plus"></i> Đặt sân ngay
                </a>
            </div>
        </c:when>
        <c:otherwise>
            <div class="bookings-list">
                <c:forEach var="booking" items="${sessionScope.bookingList}">
                    <div class="booking-card" data-status="${booking.booking_status}">
                        <div class="booking-header">
                            <div class="booking-info-left">
                                <div class="booking-date">
                                    <i class="fas fa-calendar"></i>
                                    <span>${booking.booking_date}</span>
                                </div>
                                <div class="booking-time">
                                    <i class="fas fa-clock"></i>
                                    <span>${booking.start_time} - ${booking.end_time}</span>
                                </div>
                            </div>
                            <div class="booking-status status-${booking.booking_status}">
                                <c:choose>
                                    <c:when test="${booking.booking_status == 'PENDING'}">
                                        <i class="fas fa-clock"></i> Chờ xác nhận
                                    </c:when>
                                    <c:when test="${booking.booking_status == 'CONFIRMED'}">
                                        <i class="fas fa-check"></i> Đã xác nhận
                                    </c:when>
                                    <c:when test="${booking.booking_status == 'COMPLETED'}">
                                        <i class="fas fa-check-double"></i> Hoàn thành
                                    </c:when>
                                    <c:when test="${booking.booking_status == 'CANCELLED'}">
                                        <i class="fas fa-times"></i> Đã hủy
                                    </c:when>
                                    <c:otherwise>
                                        <i class="fas fa-exclamation"></i> Hết hạn
                                    </c:otherwise>
                                </c:choose>
                            </div>
                        </div>

                        <div class="booking-body">
                            <div class="court-info">
                                <h4>
                                    <i class="fas fa-volleyball-ball"></i>
                                        ${booking.court_name}
                                </h4>
                                <p class="facility-name">
                                    <i class="fas fa-map-marker-alt"></i>
                                        ${booking.facility_name}
                                </p>
                            </div>

                            <div class="booking-details">
                                <div class="detail-item">
                                    <span class="label">Loại sân:</span>
                                    <span class="value">
                                                <c:choose>
                                                    <c:when test="${booking.court_type == 'SINGLE'}">
                                                        <i class="fas fa-user"></i> Đơn
                                                    </c:when>
                                                    <c:otherwise>
                                                        <i class="fas fa-users"></i> Đôi
                                                    </c:otherwise>
                                                </c:choose>
                                            </span>
                                </div>

                                <div class="detail-item">
                                    <span class="label">Loại đặt:</span>
                                    <span class="value">
                                                <c:choose>
                                                    <c:when test="${booking.booking_type == 'SINGLE'}">
                                                        <i class="fas fa-calendar-day"></i> Đơn lẻ
                                                    </c:when>
                                                    <c:otherwise>
                                                        <i class="fas fa-calendar-week"></i> Cố định
                                                    </c:otherwise>
                                                </c:choose>
                                            </span>
                                </div>

                                <div class="detail-item">
                                    <span class="label">Thanh toán:</span>
                                    <span class="value payment-${booking.payment_status}">
                                                <c:choose>
                                                    <c:when test="${booking.payment_status == 'PAID'}">
                                                        <i class="fas fa-check-circle"></i> Đã thanh toán
                                                    </c:when>
                                                    <c:when test="${booking.payment_status == 'DEPOSIT'}">
                                                        <i class="fas fa-clock"></i> Đã cọc
                                                    </c:when>
                                                    <c:otherwise>
                                                        <i class="fas fa-times-circle"></i> Chưa thanh toán
                                                    </c:otherwise>
                                                </c:choose>
                                            </span>
                                </div>

                                <c:if test="${not empty booking.total_amount}">
                                    <div class="detail-item price-item">
                                        <span class="label">Tổng tiền:</span>
                                        <span class="value price">${booking.total_amount} VNĐ</span>
                                    </div>
                                </c:if>
                            </div>

                            <c:if test="${booking.booking_type == 'RECURRING'}">
                                <div class="recurring-info">
                                    <i class="fas fa-sync-alt"></i>
                                    <span>
                                                Lặp lại: Thứ ${booking.day_of_week}
                                                (${booking.start_date} - ${booking.end_date})
                                            </span>
                                </div>
                            </c:if>
                        </div>

                        <div class="booking-actions">
                            <button class="btn-action btn-detail" onclick="viewBookingDetail(${booking.booking_id})">
                                <i class="fas fa-eye"></i> Chi tiết
                            </button>

                            <c:if test="${booking.booking_status == 'CONFIRMED' && booking.payment_status != 'PAID'}">
                                <button class="btn-action btn-pay" onclick="payBooking(${booking.booking_id})">
                                    <i class="fas fa-credit-card"></i> Thanh toán
                                </button>
                            </c:if>

                            <c:if test="${booking.booking_status == 'PENDING' || booking.booking_status == 'CONFIRMED'}">
                                <button class="btn-action btn-cancel" onclick="cancelBooking(${booking.booking_id})">
                                    <i class="fas fa-times"></i> Hủy đặt
                                </button>
                            </c:if>

                            <c:if test="${booking.booking_status == 'COMPLETED' && empty booking.has_review}">
                                <button class="btn-action btn-review" onclick="reviewBooking(${booking.booking_id})">
                                    <i class="fas fa-star"></i> Đánh giá
                                </button>
                            </c:if>
                        </div>
                    </div>
                </c:forEach>
            </div>
        </c:otherwise>
    </c:choose>
</section>
</body>
</html>
