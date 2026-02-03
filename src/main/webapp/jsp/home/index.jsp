<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>BadmintonPro - Đặt sân cầu lông chuyên nghiệp</title>

    <!-- Bootstrap 5.3 CSS -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">

    <!-- Bootstrap Icons -->
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.0/font/bootstrap-icons.css">

    <!-- Leaflet CSS -->
    <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css"
          integrity="sha256-p4NxAoJBhIIN+hmNHrzRCf9tD/miZyoHS5obTRR9BMY=" crossorigin=""/>

    <!-- Google Fonts -->
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700;800;900&display=swap" rel="stylesheet">

    <!-- Custom CSS -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/badminton-pro.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/components.css">
</head>
<body>
<div id="app" class="app-container">

    <!-- Header Component -->
    <jsp:include page="../common/header.jsp">
        <jsp:param name="contextPath" value="${pageContext.request.contextPath}" />
    </jsp:include>

    <!-- Main Content Area -->
    <main id="mainContent" class="main-content">

        <!-- Home Tab - Court Grid -->
        <div id="homeTab" class="tab-content active">
            <div class="container-fluid px-3 px-sm-5 px-lg-6 mt-4 pb-40">

                <!-- Favorites Header (hidden by default) -->
                <div id="favoritesHeader" class="favorites-header mb-4" style="display: none;">
                    <div class="d-flex align-items-center justify-content-between">
                        <h3 class="favorites-title">
                            <i class="bi bi-heart-fill text-danger me-2"></i>
                            Sân yêu thích của bạn
                        </h3>
                        <button id="showAllBtn" class="btn-show-all">
                            Hiện tất cả
                        </button>
                    </div>
                </div>

                <!-- Court Cards Grid -->
                <div id="courtsGrid" class="courts-grid">
                    <!-- Court cards will be inserted here via JavaScript -->
                </div>

                <!-- No Results Message -->
                <div id="noResults" class="no-results" style="display: none;">
                    <div class="no-results-icon">
                        <i class="bi bi-search"></i>
                    </div>
                    <h3 class="no-results-title">Không tìm thấy sân</h3>
                    <p class="no-results-text">Thử thay đổi từ khóa hoặc bộ lọc của bạn</p>
                    <button id="clearFiltersBtn" class="btn-clear-filters">
                        Xóa tất cả bộ lọc
                    </button>
                </div>

                <!-- Loading More Indicator -->
                <div class="loading-more text-center py-5">
                    <div class="d-flex justify-content-center align-items-center gap-2">
                        <div class="loading-dot"></div>
                        <div class="loading-dot" style="animation-delay: 0.2s;"></div>
                        <div class="loading-dot" style="animation-delay: 0.4s;"></div>
                        <span class="loading-text ms-4">Đang cập nhật thêm sân mới...</span>
                    </div>
                </div>
            </div>

            <!-- Bottom Fade Effect -->
            <div class="bottom-fade"></div>
        </div>

        <!-- Map Tab -->
        <div id="mapTab" class="tab-content">
            <jsp:include page="../common/map-view.jsp" />
        </div>

        <!-- Other Tabs (Coming Soon) -->
        <div id="bookingTab" class="tab-content">
            <div class="coming-soon">
                <i class="bi bi-info-circle"></i>
                <h2>Tính năng đang phát triển</h2>
                <p>Vui lòng quay lại sau</p>
            </div>
        </div>

        <div id="offerTab" class="tab-content">
            <div class="coming-soon">
                <i class="bi bi-info-circle"></i>
                <h2>Tính năng đang phát triển</h2>
                <p>Vui lòng quay lại sau</p>
            </div>
        </div>

        <div id="profileTab" class="tab-content">
            <div class="coming-soon">
                <i class="bi bi-info-circle"></i>
                <h2>Tính năng đang phát triển</h2>
                <p>Vui lòng quay lại sau</p>
            </div>
        </div>

    </main>

    <!-- Bottom Navigation -->
    <jsp:include page="../common/bottom-nav.jsp" />

    <!-- Filter Panel (Sidebar) -->
    <jsp:include page="../common/filter-panel.jsp" />

    <!-- Court Detail Modal -->
    <jsp:include page="../common/court-card.jsp" />

    <!-- Court Detail Modal -->
    <jsp:include page="../common/court-detail.jsp" />

    <!-- Auth Modal -->
    <jsp:include page="../common/auth-modal.jsp" />

    <!-- Toast Notification -->
    <div id="toastContainer" class="toast-container position-fixed top-0 end-0 p-3" style="z-index: 9999;">
        <div id="toast" class="toast custom-toast align-items-center" role="alert">
            <div class="d-flex">
                <div class="toast-body d-flex align-items-center gap-3">
                    <div class="toast-icon">
                        <i class="bi bi-heart-fill"></i>
                    </div>
                    <span id="toastMessage" class="toast-message"></span>
                </div>
            </div>
        </div>
    </div>
</div>

<!-- Data will be loaded from API -->
<script>
    window.COURTS_DATA = []; // Empty initially, will be populated by API
</script>

<!-- Bootstrap 5.3 JS Bundle -->
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>

<!-- Leaflet JS -->
<script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"
        integrity="sha256-20nQCchB9co0qIjJZRGuk2/Z9VM+kNiyxNV1lvTlZBo=" crossorigin=""></script>

<!-- Vietnam Locations Data -->
<script src="${pageContext.request.contextPath}/assets/js/vietnam-locations.js"></script>

<!-- Custom JavaScript -->
<script src="${pageContext.request.contextPath}/assets/js/badminton-pro.js"></script>
<script src="${pageContext.request.contextPath}/assets/js/map-handler.js"></script>

</body>
</html>