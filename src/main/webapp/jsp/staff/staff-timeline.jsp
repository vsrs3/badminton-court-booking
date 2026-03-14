<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<%-- 1. Layout --%>
<%@ include file="layout/staff-layout.jsp"%>

<%-- Page-specific CSS --%>
<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/staff/staff-timeline.css">

<%-- 2. Sidebar --%>
<%@ include file="layout/staff-sidebar.jsp"%>

<%-- 3. Main content --%>
<div class="main-content">

    <%-- 4. Header --%>
    <%@ include file="layout/staff-header.jsp"%>

    <%-- 5. Content area --%>
    <div class="content-area">

        <%-- ===== PAGE HEADER ===== --%>
        <div class="d-flex align-items-center justify-content-between flex-wrap gap-3 mb-4">
            <div>
                <h1 class="fw-bold mb-1" style="font-size:1.75rem; color:#111827; letter-spacing:-0.02em;">
                    <i class="bi bi-calendar3 me-2" style="color:var(--primary-color);"></i>Lịch Đặt Sân
                </h1>
                <p class="mb-0" style="font-size:0.875rem; color:#9CA3AF;">
                    Xem lịch đặt sân theo ngày tại
                    <strong style="color:#065F46;">
                        <c:out value="${sessionScope.facilityName}" default="cơ sở của bạn"/>
                    </strong>
                </p>
            </div>

            <%-- Task 9a: Booking proxy button --%>
            <div>
                <button type="button" class="st-proxy-btn" id="btnProxyMode">
                    <i class="bi bi-plus-circle me-1"></i>Đặt hộ khách
                </button>
                <button type="button" class="st-proxy-btn st-proxy-btn-secondary" id="btnRecurring">
                    <i class="bi bi-arrow-repeat me-1"></i>Đặt định kỳ
                </button>
                <button type="button" class="st-proxy-btn st-proxy-btn-warning" id="btnBlockMode">
                    <i class="bi bi-slash-circle me-1"></i>Block lịch
                </button>
                <button type="button" class="st-proxy-btn st-proxy-btn-cancel d-none" id="btnProxyCancel">
                    <i class="bi bi-x-circle me-1"></i>Hủy đặt hộ
                </button>
            </div>
        </div>

        <%-- ===== DATE FILTER BAR ===== --%>
        <div class="st-date-bar">
            <div class="st-date-buttons">
                <button type="button" class="st-date-btn active" id="btnToday">
                    <i class="bi bi-calendar-check me-1"></i>Hôm nay
                </button>
                <button type="button" class="st-date-btn" id="btnTomorrow">
                    <i class="bi bi-calendar-plus me-1"></i>Ngày mai
                </button>
                <div class="st-date-input-wrap">
                    <i class="bi bi-calendar-event"></i>
                    <input type="date" class="st-date-input" id="datePickerInput">
                </div>
            </div>

            <div class="st-date-display">
                <i class="bi bi-clock me-1"></i>
                <span id="currentDateDisplay">--</span>
            </div>
        </div>

        <%-- ===== LEGEND ===== --%>
            <div class="st-legend">
                <div class="st-legend-item">
                    <span class="st-legend-dot st-dot-available"></span>Trống
                </div>
                <div class="st-legend-item">
                    <span class="st-legend-dot st-dot-pending"></span>Chờ xác nhận
                </div>
                <div class="st-legend-item">
                    <span class="st-legend-dot st-dot-confirmed"></span>Đã xác nhận
                </div>
                <div class="st-legend-item">
                    <span class="st-legend-dot st-dot-completed"></span>Hoàn thành
                </div>
                <div class="st-legend-item">
                    <span class="st-legend-dot st-dot-cancelled"></span>Đã hủy
                </div>
                <div class="st-legend-item">
                    <span class="st-legend-dot st-dot-disabled"></span>Bảo trì
                </div>
                <%-- NO_SHOW legend --%>
                <div class="st-legend-item">
                    <span class="st-legend-dot st-dot-noshow"></span>Vắng mặt
                </div>
                <%-- Task 9a: Selected legend (hidden by default) --%>
                <div class="st-legend-item d-none" id="legendSelected">
                    <span class="st-legend-dot st-dot-selected"></span>Đã chọn
                </div>
            </div>

        <%-- ===== TIMELINE GRID CONTAINER ===== --%>
        <div class="st-grid-wrapper" id="timelineContainer">

            <%-- Loading state --%>
            <div class="st-state st-state-loading" id="stateLoading">
                <div class="st-spinner"></div>
                <p>Đang tải lịch đặt sân...</p>
            </div>

            <%-- Error state (hidden by default) --%>
            <div class="st-state st-state-error d-none" id="stateError">
                <i class="bi bi-exclamation-triangle" style="font-size:2.5rem; color:#EF4444;"></i>
                <p>Không thể tải dữ liệu. Vui lòng thử lại.</p>
                <button class="btn btn-brand btn-sm rounded-3" onclick="reloadTimeline()">
                    <i class="bi bi-arrow-clockwise me-1"></i>Thử lại
                </button>
            </div>

            <%-- Empty state (hidden by default) --%>
            <div class="st-state st-state-empty d-none" id="stateEmpty">
                <i class="bi bi-calendar-x" style="font-size:2.5rem; color:#D1D5DB;"></i>
                <p>Không có sân nào hoạt động tại cơ sở này.</p>
            </div>

            <%-- Actual grid (hidden until data loaded) --%>
            <div class="st-grid-scroll d-none" id="gridScroll">
                <table class="st-grid" id="timelineGrid">
                    <thead>
                    <tr id="gridHeaderRow">
                        <th class="st-grid-corner">Sân \ Giờ</th>
                    </tr>
                    </thead>
                    <tbody id="gridBody">
                    </tbody>
                </table>
            </div>

        </div>

        <%-- ===== Task 9a: BOTTOM BAR (sticky) ===== --%>
        <div class="st-bottom-bar d-none" id="bottomBar">
            <div class="st-bottom-info">
                <span class="st-bottom-count" id="bottomCount">0 slot</span>
                <span class="st-bottom-sep">·</span>
                <span class="st-bottom-price" id="bottomPrice">0đ</span>
            </div>
            <div class="st-bottom-actions">
                <button type="button" class="st-bottom-btn st-bottom-btn-clear" id="btnClearAll">
                    <i class="bi bi-trash me-1"></i>Bỏ chọn
                </button>
                <button type="button" class="st-bottom-btn st-bottom-btn-continue" id="btnContinue">
                    Tiếp tục<i class="bi bi-arrow-right ms-1"></i>
                </button>
            </div>
        </div>

    </div>
</div>

<%-- 6. Footer --%>
<footer class="staff-footer">
    <p class="mb-0">&copy; 2026 Badminton Court Booking System. All rights reserved.</p>
</footer>

<%-- Context path for JS --%>
<script>
    window.ST_CTX = '${pageContext.request.contextPath}';
    window.ST_FACILITY_ID = '${sessionScope.facilityId}';
</script>

<script src="${pageContext.request.contextPath}/assets/js/staff/staff-dialog.js"></script>
<script src="${pageContext.request.contextPath}/assets/js/staff/staff-timeline.js"></script>

</body>
</html>


