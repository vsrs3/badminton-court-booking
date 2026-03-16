<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<%@ include file="layout/staff-layout.jsp"%>

<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/staff/staff-timeline.css?v=<%= System.currentTimeMillis() %>">
<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/staff/staff-rental-status.css?v=<%= System.currentTimeMillis() %>">

<%@ include file="layout/staff-sidebar.jsp"%>

<div class="main-content">
    <%@ include file="layout/staff-header.jsp"%>

    <div class="content-area">
        <div class="srs-screen-alert d-none" id="rentalStatusScreenAlert" role="alert" aria-live="polite"></div>

        <div class="d-flex align-items-center justify-content-between flex-wrap gap-3 mb-4">
            <div>
                <h1 class="fw-bold mb-1" style="font-size:1.75rem; color:#111827; letter-spacing:-0.02em;">
                    <i class="bi bi-bag-check me-2" style="color:var(--primary-color);"></i>Danh Sách Đồ Thuê
                </h1>
                <p class="mb-0" style="font-size:0.875rem; color:#9CA3AF;">
                    Theo dõi tình trạng thuê đồ và tồn kho tại
                    <strong style="color:#065F46;">
                        <c:out value="${sessionScope.facilityName}" default="cơ sở của bạn"/>
                    </strong>
                </p>
            </div>
        </div>

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

        <div class="st-legend srs-legend">
            <div class="st-legend-item">
                <span class="st-legend-dot srs-dot-empty"></span>Trống
            </div>
            <div class="st-legend-item">
                <span class="st-legend-dot srs-dot-rented"></span>Đã thuê
            </div>
            <div class="st-legend-item">
                <span class="st-legend-dot srs-dot-renting"></span>Đang thuê
            </div>
            <div class="st-legend-item">
                <span class="st-legend-dot srs-dot-returned"></span>Đã trả
            </div>
            <div class="st-legend-item">
                <span class="st-legend-dot srs-dot-past"></span>Quá khứ
            </div>
            <div class="st-legend-item">
                <span class="st-legend-dot srs-dot-grouped"></span>Cùng người thuê liền kề
            </div>
        </div>

        <section class="srs-section-card">
            <div class="srs-section-head">
                <div>
                    <h2 class="srs-section-title">
                        <i class="bi bi-calendar3 me-2"></i>Lịch Thuê Đồ
                    </h2>
                    <p class="srs-section-subtitle">Xem trạng thái thuê đồ theo sân và từng mốc thời gian.</p>
                </div>
            </div>

            <div class="st-grid-wrapper srs-timeline-wrapper">
                <div class="st-state" id="stateLoading">
                    <div class="st-spinner"></div>
                    <p>Đang tải dữ liệu thuê đồ...</p>
                </div>

                <div class="st-state d-none" id="stateError">
                    <i class="bi bi-exclamation-triangle" style="font-size:2.5rem; color:#EF4444;"></i>
                    <p id="errorMessage">Không thể tải dữ liệu.</p>
                    <button class="btn btn-sm rounded-3" style="background:var(--primary-color);color:white;" onclick="loadRentalStatus()">
                        <i class="bi bi-arrow-clockwise me-1"></i>Thử lại
                    </button>
                </div>

                <div class="st-state d-none" id="stateEmpty">
                    <i class="bi bi-calendar-x" style="font-size:2.5rem; color:#D1D5DB;"></i>
                    <p>Hiện chưa có sân hoặc khung giờ để hiển thị.</p>
                </div>

                <div class="st-grid-scroll d-none" id="gridScroll">
                    <table class="st-grid srs-grid" id="rentalTimelineGrid">
                        <thead>
                        <tr id="gridHeaderRow">
                            <th class="st-grid-corner">Sân \ Giờ</th>
                        </tr>
                        </thead>
                        <tbody id="gridBody"></tbody>
                    </table>
                </div>
            </div>
        </section>

        <section class="srs-section-card mt-4">
            <div class="srs-section-head">
                <div>
                    <h2 class="srs-section-title">
                        <i class="bi bi-box-seam me-2"></i>Kho Đồ
                    </h2>
                    <div class="srs-realtime-panel mt-3">
                        <div class="srs-realtime-pill">
                            <span class="srs-realtime-label">Thời gian hiện tại</span>
                            <strong class="srs-realtime-value" id="inventoryCurrentTime">--:--</strong>
                        </div>
                        <div class="srs-realtime-pill">
                            <span class="srs-realtime-label">Slot hiện tại</span>
                            <strong class="srs-realtime-value" id="inventoryCurrentSlot">Slot không khả dụng</strong>
                        </div>
                    </div>
                </div>
            </div>

            <div class="srs-inventory-toolbar">
                <div class="srs-search-input-wrap">
                    <i class="bi bi-search"></i>
                    <input type="text"
                           id="inventorySearchInput"
                           class="form-control"
                           placeholder="Tìm kiếm theo tên đồ"
                           aria-label="Tìm kiếm theo tên đồ">
                </div>
            </div>

            <div class="table-responsive srs-inventory-wrap">
                <table class="table srs-inventory-table">
                    <thead>
                    <tr>
                        <th style="width:90px;">STT</th>
                        <th>Tên đồ</th>
                        <th style="width:180px;">Số lượng tổng</th>
                        <th style="width:180px;">Số lượng khả dụng</th>
                    </tr>
                    </thead>
                    <tbody id="inventoryTableBody"></tbody>
                </table>
            </div>

            <div class="srs-inventory-pagination" id="inventoryPagination"></div>
        </section>
    </div>
</div>

<div class="modal fade" id="rentalDetailModal" tabindex="-1" aria-labelledby="rentalDetailModalLabel" aria-hidden="true">
    <div class="modal-dialog modal-lg modal-dialog-scrollable">
        <div class="modal-content">
            <div class="modal-header">
                <h5 class="modal-title" id="rentalDetailModalLabel">
                    <i class="bi bi-bag-check me-2"></i>Chi Tiết Thuê Đồ
                </h5>
                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Đóng"></button>
            </div>
            <div class="modal-body">
                <div class="srs-modal-context" id="rentalDetailContext"></div>
                <div class="srs-modal-note d-none" id="rentalDetailModeHint"></div>

                <div class="table-responsive">
                    <table class="table srs-detail-table">
                        <thead>
                        <tr>
                            <th>Tên đồ</th>
                            <th style="width:140px;">Số lượng</th>
                            <th style="width:220px;">Trạng thái</th>
                        </tr>
                        </thead>
                        <tbody id="rentalDetailBody"></tbody>
                    </table>
                </div>

                <div class="text-center text-muted py-3 d-none" id="rentalDetailEmpty">
                    Không có đồ thuê trong ô này.
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
<script src="${pageContext.request.contextPath}/assets/js/staff/staff-rental-status.js?v=<%= System.currentTimeMillis() %>"></script>

</body>
</html>
