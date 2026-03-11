<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<%@ include file="layout/staff-layout.jsp"%>
<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/staff/staff-booking-create.css">
<%@ include file="layout/staff-sidebar.jsp"%>

<div class="main-content">
    <%@ include file="layout/staff-header.jsp"%>

    <div class="content-area">

        <%-- Back link --%>
        <a href="${pageContext.request.contextPath}/staff/timeline" class="sbc-back-link" id="backLink">
            <i class="bi bi-arrow-left"></i> Quay lại chọn slot
        </a>

        <%-- No data state --%>
        <div class="sbc-state d-none" id="stateNoData">
            <i class="bi bi-exclamation-triangle" style="font-size:2.5rem; color:#EF4444;"></i>
            <p>Không có dữ liệu slot. Vui lòng quay lại chọn slot trước.</p>
            <a href="${pageContext.request.contextPath}/staff/timeline" class="btn btn-sm rounded-3"
               style="background:var(--primary-color);color:white;">
                <i class="bi bi-arrow-left me-1"></i>Quay lại Timeline
            </a>
        </div>

        <%-- Main content: 2 columns --%>
        <div class="row g-4 d-none" id="createContent">

            <%-- LEFT: Slot summary --%>
            <div class="col-lg-5">
                <div class="card sbc-card">
                    <div class="card-header sbc-card-header">
                        <i class="bi bi-receipt me-2"></i>Tóm tắt đặt sân
                    </div>
                    <div class="card-body">
                        <div class="sbc-field mb-3">
                            <span class="sbc-field-label">Ngày</span>
                            <span class="sbc-field-value fw-bold" id="summaryDate"></span>
                        </div>

                        <div id="sessionsContainer">
                            <%-- JS renders session summaries here --%>
                        </div>

                        <div class="sbc-total-row">
                            <span>TỔNG CỘNG</span>
                            <span class="sbc-total-price" id="summaryTotal"></span>
                        </div>
                    </div>
                </div>
            </div>

            <%-- RIGHT: Customer form --%>
            <div class="col-lg-7">
                <div class="card sbc-card">
                    <div class="card-header sbc-card-header">
                        <i class="bi bi-person me-2"></i>Thông tin khách hàng
                    </div>
                    <div class="card-body">

                        <%-- Customer type tabs --%>
                        <div class="sbc-tabs mb-3">
                            <button type="button" class="sbc-tab active" id="tabAccount"
                                    data-type="ACCOUNT">
                                <i class="bi bi-person-check me-1"></i>Khách có tài khoản
                            </button>
                            <button type="button" class="sbc-tab" id="tabGuest"
                                    data-type="GUEST">
                                <i class="bi bi-person-plus me-1"></i>Khách vãng lai
                            </button>
                        </div>

                        <%-- ACCOUNT form --%>
                        <div id="formAccount">
                            <div class="sbc-search-wrap mb-3">
                                <i class="bi bi-search"></i>
                                <input type="text" class="sbc-search-input" id="customerSearch"
                                       placeholder="Tìm bằng SĐT hoặc email..."
                                       autocomplete="off">
                                <div class="sbc-search-dropdown d-none" id="searchDropdown">
                                    <%-- JS renders search results here --%>
                                </div>
                            </div>

                            <%-- Selected customer display --%>
                            <div class="sbc-selected-customer d-none" id="selectedCustomer">
                                <div class="sbc-selected-info">
                                    <div class="sbc-selected-name" id="selName"></div>
                                    <div class="sbc-selected-meta">
                                        <span><i class="bi bi-telephone me-1"></i><span id="selPhone"></span></span>
                                        <span><i class="bi bi-envelope me-1"></i><span id="selEmail"></span></span>
                                    </div>
                                </div>
                                <button type="button" class="sbc-btn-remove" id="btnRemoveCustomer">
                                    <i class="bi bi-x-lg"></i>
                                </button>
                            </div>

                            <input type="hidden" id="selectedAccountId" value="">
                        </div>

                            <%-- GUEST form --%>
                            <div id="formGuest" class="d-none">
                                <div class="mb-3">
                                    <label class="sbc-label">Họ tên <span class="text-danger">*</span></label>
                                    <input type="text" class="sbc-input" id="guestName" placeholder="Nhập họ tên">
                                </div>
                                <div class="mb-3">
                                    <label class="sbc-label">Số điện thoại <span class="text-danger">*</span></label>
                                    <input type="tel" class="sbc-input" id="guestPhone"
                                           placeholder="Nhập SĐT (VD: 0912345678)"
                                           maxlength="10"
                                           pattern="0[0-9]{9}"
                                           inputmode="numeric">
                                    <div class="sbc-phone-hint d-none" id="phoneHint"></div>
                                </div>
                                <div class="mb-3">
                                    <label class="sbc-label">Email <span style="color:#9CA3AF;">(tùy chọn)</span></label>
                                    <input type="email" class="sbc-input" id="guestEmail" placeholder="Nhập email">
                                </div>
                            </div>

                        <%-- Error message --%>
                        <div class="sbc-error d-none" id="formError"></div>

                        <%-- Submit --%>
                        <button type="button" class="sbc-btn-submit" id="btnSubmit">
                            <i class="bi bi-check-circle me-2"></i>Xác nhận đặt sân
                        </button>

                    </div>
                </div>
            </div>

        </div>

    </div>
</div>

<footer class="staff-footer">
    <p class="mb-0">&copy; 2026 Badminton Court Booking System. All rights reserved.</p>
</footer>

<script>
    window.ST_CTX = '${pageContext.request.contextPath}';
</script>
<script src="${pageContext.request.contextPath}/assets/js/staff/staff-dialog.js"></script>
<script src="${pageContext.request.contextPath}/assets/js/staff/staff-booking-create.js"></script>

</body>
</html>
