<%-- staff-booking-detail.jsp — Task 8 v2: Sessions checkin/checkout --%>

<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<%-- 1. Layout --%>
<%@ include file="layout/staff-layout.jsp"%>

<%-- Page CSS --%>
<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/staff/staff-booking-detail.css">

<%-- 2. Sidebar --%>
<%@ include file="layout/staff-sidebar.jsp"%>

<%-- 3. Main content --%>
<div class="main-content">

    <%-- 4. Header --%>
    <%@ include file="layout/staff-header.jsp"%>

    <%-- 5. Content area --%>
    <div class="content-area">

        <%-- Back button --%>
        <a href="#" class="sbd-back-link" id="backLink">
            <i class="bi bi-arrow-left"></i> Quay lại
        </a>

        <%-- Loading --%>
        <div id="stateLoading" class="sbd-state">
            <div class="st-spinner"></div>
            <p>Đang tải thông tin booking...</p>
        </div>

        <%-- Error --%>
        <div id="stateError" class="sbd-state d-none">
            <i class="bi bi-exclamation-triangle" style="font-size:2.5rem; color:#EF4444;"></i>
            <p id="errorMessage">Không thể tải dữ liệu.</p>
            <a href="#" class="btn btn-sm rounded-3" id="backLinkError"
               style="background:var(--primary-color);color:white;">
                <i class="bi bi-arrow-left me-1"></i>Quay lại
            </a>
        </div>

        <%-- Detail content (hidden until loaded) --%>
        <div id="detailContent" class="d-none">

            <%-- Page title --%>
            <div class="d-flex align-items-center justify-content-between flex-wrap gap-2 mb-4">
                <div>
                    <h1 class="fw-bold mb-1" style="font-size:1.5rem; color:#111827;">
                        <i class="bi bi-receipt me-2" style="color:var(--primary-color);"></i>
                        Booking #<span id="dBookingId"></span>
                    </h1>
                    <p class="mb-0" style="font-size:0.8125rem; color:#9CA3AF;">
                        Chi tiết đặt sân
                    </p>
                </div>
                <span class="sbd-status-badge" id="dStatusBadge"></span>
            </div>

            <%-- Cards grid --%>
            <div class="row g-4">

                <%-- LEFT COLUMN --%>
                <div class="col-lg-8">

                    <%-- Card: Booking Info (removed checkin/checkout fields) --%>
                    <div class="card sbd-card">
                        <div class="card-header sbd-card-header">
                            <i class="bi bi-info-circle me-2"></i>Thông tin đặt sân
                        </div>
                        <div class="card-body">
                            <div class="row g-3">
                                <div class="col-sm-6">
                                    <div class="sbd-field">
                                        <span class="sbd-field-label">Mã đặt sân</span>
                                        <span class="sbd-field-value" id="dBookingIdField"></span>
                                    </div>
                                </div>
                                <div class="col-sm-6">
                                    <div class="sbd-field">
                                        <span class="sbd-field-label">Ngày đặt sân</span>
                                        <span class="sbd-field-value" id="dBookingDate"></span>
                                    </div>
                                </div>
                                <div class="col-sm-6">
                                    <div class="sbd-field">
                                        <span class="sbd-field-label">Trạng thái</span>
                                        <span class="sbd-field-value" id="dBookingStatus"></span>
                                    </div>
                                </div>
                                <div class="col-sm-6">
                                    <div class="sbd-field">
                                        <span class="sbd-field-label">Ngày tạo</span>
                                        <span class="sbd-field-value" id="dCreatedAt"></span>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>

                    <%-- Card: Phiên chơi (Sessions) — rendered by JS --%>
                    <div class="card sbd-card">
                        <div class="card-header sbd-card-header">
                            <i class="bi bi-controller me-2"></i>Phiên chơi
                            <span class="sbd-session-progress" id="sessionProgress"></span>
                        </div>
                        <div class="card-body p-0" id="sessionsContainer">
                            <%-- JS will render session rows here --%>
                        </div>
                    </div>

                </div>

                <%-- RIGHT COLUMN --%>
                <div class="col-lg-4">

                    <%-- Card: Customer Info --%>
                    <div class="card sbd-card">
                        <div class="card-header sbd-card-header">
                            <i class="bi bi-person me-2"></i>Thông tin khách
                        </div>
                        <div class="card-body">
                            <div class="sbd-field mb-3">
                                <span class="sbd-field-label">Họ tên</span>
                                <span class="sbd-field-value fw-bold" id="dCustomerName"></span>
                            </div>
                            <div class="sbd-field mb-3">
                                <span class="sbd-field-label">Số điện thoại</span>
                                <span class="sbd-field-value" id="dCustomerPhone"></span>
                            </div>
                            <div class="sbd-field">
                                <span class="sbd-field-label">Loại khách</span>
                                <span class="sbd-field-value" id="dCustomerType"></span>
                            </div>
                        </div>
                    </div>

                    <%-- Card: Payment Info --%>
                    <div class="card sbd-card">
                        <div class="card-header sbd-card-header">
                            <i class="bi bi-credit-card me-2"></i>Thanh toán
                        </div>
                        <div class="card-body">
                            <div class="sbd-field mb-3">
                                <span class="sbd-field-label">Tổng tiền</span>
                                <span class="sbd-field-value sbd-money" id="dTotalAmount"></span>
                            </div>
                            <div class="sbd-field mb-3">
                                <span class="sbd-field-label">Đã thanh toán</span>
                                <span class="sbd-field-value sbd-money" id="dPaidAmount"></span>
                            </div>
                            <div class="sbd-field">
                                <span class="sbd-field-label">Trạng thái TT</span>
                                <span class="sbd-field-value" id="dPaymentStatus"></span>
                            </div>
                        </div>
                    </div>

                </div>
            </div>

        </div>

    </div>
</div>

<%-- Footer --%>
<footer class="staff-footer">
    <p class="mb-0">&copy; 2026 Badminton Court Booking System. All rights reserved.</p>
</footer>

<script>
    window.ST_CTX = '${pageContext.request.contextPath}';
</script>
<script src="${pageContext.request.contextPath}/assets/js/staff/staff-booking-detail.js"></script>

</body>
</html>