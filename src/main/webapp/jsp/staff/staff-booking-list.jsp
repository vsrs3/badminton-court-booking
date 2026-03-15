<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<%@ include file="layout/staff-layout.jsp"%>

<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/staff/staff-booking-list.css">

<%@ include file="layout/staff-sidebar.jsp"%>

<div class="main-content">

    <%@ include file="layout/staff-header.jsp"%>

    <div class="content-area">

        <%-- Page header --%>
        <div class="d-flex align-items-center justify-content-between flex-wrap gap-3 mb-4">
            <div>
                <h1 class="fw-bold mb-1" style="font-size:1.75rem; color:#111827; letter-spacing:-0.02em;">
                    <i class="bi bi-list-ul me-2" style="color:var(--primary-color);"></i>Danh Sách Booking
                </h1>
                <p class="mb-0" style="font-size:0.875rem; color:#9CA3AF;">
                    Tìm kiếm và xem lịch sử booking tại
                    <strong style="color:#065F46;">
                        <c:out value="${sessionScope.facilityName}" default="cơ sở của bạn"/>
                    </strong>
                </p>
            </div>
        </div>

        <%-- Search bar --%>
        <div class="sbl-search-bar">
            <div class="sbl-search-input-wrap">
                <i class="bi bi-search"></i>
                <input type="text" class="sbl-search-input" id="searchInput"
                       placeholder="Tìm theo tên, SĐT hoặc mã booking..."
                       autocomplete="off">
                <button type="button" class="sbl-search-clear d-none" id="searchClear">
                    <i class="bi bi-x-lg"></i>
                </button>
            </div>
            <button type="button" class="sbl-search-btn" id="searchBtn">
                <i class="bi bi-search me-1"></i>Tìm kiếm
            </button>
        </div>

        <%-- Quick filters --%>
        <div class="sbl-filters">
            <div class="sbl-filter-group" aria-label="Lọc theo ngày">
                <span class="sbl-filter-label">Ngày</span>
                <button type="button" class="sbl-chip is-active" data-filter="today" data-value="ALL">Tất cả</button>
                <button type="button" class="sbl-chip" data-filter="today" data-value="TODAY">Hôm nay</button>
            </div>
            <div class="sbl-filter-group" aria-label="Lọc theo trạng thái">
                <span class="sbl-filter-label">Trạng thái</span>
                <button type="button" class="sbl-chip is-active" data-filter="status" data-value="ALL">Tất cả</button>
                <button type="button" class="sbl-chip" data-filter="status" data-value="CONFIRMED">Đã xác nhận</button>
                <button type="button" class="sbl-chip" data-filter="status" data-value="PENDING">Chờ</button>
                <button type="button" class="sbl-chip" data-filter="status" data-value="COMPLETED">Hoàn thành</button>
                <button type="button" class="sbl-chip" data-filter="status" data-value="CANCELLED">Đã hủy</button>
            </div>
        </div>

        <%-- Results info --%>
        <div class="sbl-results-info d-none" id="resultsInfo">
            <span id="resultsText"></span>
        </div>

        <%-- Table wrapper --%>
        <div class="sbl-table-wrapper" id="tableWrapper">

            <%-- Loading --%>
            <div class="sbd-state" id="stateLoading">
                <div class="sbl-loading-card">
                    <div class="sbl-loading-icon">
                        <div class="st-spinner"></div>
                    </div>
                    <div class="sbl-loading-text">
                        <h4>Đang tải danh sách</h4>
                        <p>Vui lòng chờ trong giây lát...</p>
                    </div>
                </div>
            </div>

            <%-- Error --%>
            <div class="sbd-state d-none" id="stateError">
                <i class="bi bi-exclamation-triangle" style="font-size:2.5rem; color:#EF4444;"></i>
                <p id="errorMessage">Không thể tải dữ liệu.</p>
                <button class="btn btn-sm rounded-3" style="background:var(--primary-color);color:white;" onclick="loadBookings()">
                    <i class="bi bi-arrow-clockwise me-1"></i>Thử lại
                </button>
            </div>

            <%-- Empty --%>
            <div class="sbd-state d-none" id="stateEmpty">
                <div class="sbl-empty-card">
                    <div class="sbl-empty-icon">
                        <i class="bi bi-inbox"></i>
                    </div>
                    <div class="sbl-empty-text">
                        <h4>Không tìm thấy booking nào</h4>
                        <p>Thử thay đổi từ khóa hoặc bộ lọc để xem kết quả.</p>
                    </div>
                </div>
            </div>

            <%-- Table --%>
            <div class="d-none" id="tableContent">
                <div class="table-responsive">
                    <table class="table table-hover mb-0 sbl-table">
                        <thead>
                        <tr>
                            <th>Mã</th>
                            <th>Khách hàng</th>
                            <th>SĐT</th>
                            <th>Ngày đặt</th>
                            <th>Sân</th>
                            <th>Trạng thái</th>
                            <th>Thanh toán</th>
                        </tr>
                        </thead>
                        <tbody id="tableBody">
                        </tbody>
                    </table>
                </div>

                <%-- Pagination --%>
                <div class="sbl-pagination" id="paginationWrap">
                    <nav>
                        <ul class="pagination mb-0" id="paginationUl">
                        </ul>
                    </nav>
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
<script src="${pageContext.request.contextPath}/assets/js/staff/staff-booking-list.js"></script>

</body>
</html>
