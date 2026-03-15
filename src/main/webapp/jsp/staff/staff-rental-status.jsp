<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<%@ include file="layout/staff-layout.jsp"%>

<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/staff/staff-rental-status.css?v=<%= System.currentTimeMillis() %>">

<%@ include file="layout/staff-sidebar.jsp"%>

<div class="main-content">

    <%@ include file="layout/staff-header.jsp"%>

    <div class="content-area">
        <div class="d-flex align-items-center justify-content-between flex-wrap gap-3 mb-4">
            <div>
                <h1 class="fw-bold mb-1" style="font-size:1.75rem; color:#111827; letter-spacing:-0.02em;">
                    <i class="bi bi-bag-check me-2" style="color:var(--primary-color);"></i>Tình Trạng Đồ Thuê
                </h1>
                <p class="mb-0" style="font-size:0.875rem; color:#9CA3AF;">
                    Theo dõi các lượt thuê và trả đồ tại
                    <strong style="color:#065F46;">
                        <c:out value="${sessionScope.facilityName}" default="cơ sở của bạn"/>
                    </strong>
                </p>
            </div>
        </div>

        <div class="srs-board">
            <div class="srs-state" id="stateLoading">
                <div class="st-spinner"></div>
                <p>Đang tải tình trạng đồ thuê...</p>
            </div>

            <div class="srs-state d-none" id="stateError">
                <i class="bi bi-exclamation-triangle" style="font-size:2.5rem; color:#EF4444;"></i>
                <p id="errorMessage">Không thể tải dữ liệu.</p>
                <button class="btn btn-sm rounded-3" style="background:var(--primary-color);color:white;" onclick="loadRentalStatus()">
                    <i class="bi bi-arrow-clockwise me-1"></i>Thử lại
                </button>
            </div>

            <div class="srs-state d-none" id="stateEmpty">
                <i class="bi bi-inbox" style="font-size:2.5rem; color:#D1D5DB;"></i>
                <p>Hiện chưa có sân nào để hiển thị.</p>
            </div>

            <div class="srs-page-grid d-none" id="courtsContainer"></div>
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
