<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<%@ include file="layout/staff-layout.jsp"%>

<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/staff/staff-refund-list.css">

<%@ include file="layout/staff-sidebar.jsp"%>

<div class="main-content">

    <%@ include file="layout/staff-header.jsp"%>

    <div class="content-area">

        <div class="d-flex align-items-center justify-content-between flex-wrap gap-3 mb-4">
            <div>
                <h1 class="fw-bold mb-1" style="font-size:1.75rem; color:#111827; letter-spacing:-0.02em;">
                    <i class="bi bi-arrow-counterclockwise me-2" style="color:var(--primary-color);"></i>Danh Sách Hoàn Tiền
                </h1>
                <p class="mb-0" style="font-size:0.875rem; color:#9CA3AF;">
                    Danh sách các yêu cầu hoàn tiền đang chờ xử lý tại
                    <strong style="color:#065F46;">
                        <c:out value="${sessionScope.facilityName}" default="cơ sở của bạn"/>
                    </strong>
                </p>
            </div>
            <div class="srl-toolbar">
                <div class="srl-search">
                    <div class="input-group srl-search-group">
                        <span class="input-group-text srl-search-icon">
                            <i class="bi bi-search"></i>
                        </span>
                        <input id="searchInput" type="text" class="form-control srl-search-input"
                               placeholder="Tìm theo mã booking hoặc số điện thoại" autocomplete="off">
                        <button id="searchClear" class="btn btn-outline-secondary srl-search-clear" type="button">Xóa</button>
                    </div>
                    <div class="srl-search-hint">Tìm theo mã booking hoặc số điện thoại</div>
                </div>
            </div>
        </div>

        <div class="srl-results-info d-none" id="resultsInfo">
            <span id="resultsText"></span>
        </div>

        <div class="srl-table-wrapper" id="tableWrapper">

            <div class="srl-state" id="stateLoading">
                <div class="st-spinner"></div>
                <p>Đang tải danh sách...</p>
            </div>

            <div class="srl-state d-none" id="stateError">
                <i class="bi bi-exclamation-triangle" style="font-size:2.5rem; color:#EF4444;"></i>
                <p id="errorMessage">Không thể tải dữ liệu.</p>
                <button class="btn btn-sm rounded-3" style="background:var(--primary-color);color:white;" onclick="loadRefunds()">
                    <i class="bi bi-arrow-clockwise me-1"></i>Thử lại
                </button>
            </div>

            <div class="srl-state d-none" id="stateEmpty">
                <i class="bi bi-inbox" style="font-size:2.5rem; color:#D1D5DB;"></i>
                <p>Không có yêu cầu hoàn tiền nào.</p>
            </div>

            <div class="d-none" id="tableContent">
                <div class="table-responsive">
                    <table class="table table-hover mb-0 srl-table">
                        <thead>
                        <tr>
                            <th>Mã</th>
                            <th>Khách hàng</th>
                            <th>SĐT</th>
                            <th>Ngày đặt</th>
                            <th>Đã trả / Tổng</th>
                            <th>Hoàn tiền</th>
                            <th>Ghi chú</th>
                            <th>Thao tác</th>
                        </tr>
                        </thead>
                        <tbody id="tableBody">
                        </tbody>
                    </table>
                </div>

                <div class="srl-pagination" id="paginationWrap">
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
\n<script src="${pageContext.request.contextPath}/assets/js/staff/staff-dialog.js"></script>\n<script src="${pageContext.request.contextPath}/assets/js/staff/staff-refund-list.js"></script>

</body>
</html>
