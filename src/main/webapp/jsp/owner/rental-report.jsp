<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ include file="layout/layout.jsp"%>
<%@ include file="layout/sidebar.jsp"%>

<link rel="stylesheet"
      href="${pageContext.request.contextPath}/assets/css/owner/dashboard.css?v=20260325-owner-dashboard-merge-fix">
<style>
    .content-area {
        padding: 0 !important;
    }
</style>

<div class="main-content" id="ownerDashboardRoot" data-context-path="${pageContext.request.contextPath}">
    <%@ include file="layout/header.jsp"%>

    <div class="dov-wrap">
        <c:if test="${not empty requestScope.error}">
            <div class="alert alert-danger d-flex align-items-center gap-2 rounded-3 mb-4" role="alert">
                <i class="bi bi-exclamation-triangle-fill flex-shrink-0"></i>
                <span class="fw-semibold">${requestScope.error}</span>
            </div>
        </c:if>

        <div class="owner-report-switch">
            <a href="${pageContext.request.contextPath}/owner/dashboard"
               class="owner-report-tab">
                Báo cáo doanh thu đặt sân
            </a>
            <a href="${pageContext.request.contextPath}/owner/rental-report"
               class="owner-report-tab is-active">
                Báo cáo doanh thu thuê đồ
            </a>
        </div>

        <section>
            <div class="owner-rental-stack">
                <div class="dov-card owner-rental-search-card">
                    <div class="owner-rental-card-header">
                        <div>
                            <h3 class="owner-rental-title">Tìm kiếm địa điểm báo cáo thuê đồ</h3>
                            <p class="owner-rental-subtitle" id="rentalFacilityMeta"></p>
                        </div>
                    </div>
                    <div class="owner-rental-search-layout">
                        <input type="hidden" id="rentalFacilityId">
                        <div class="owner-rental-search-input-wrap">
                            <input type="text"
                                   id="rentalFacilitySearch"
                                   class="form-control owner-rental-search-input"
                                   autocomplete="off"
                                   placeholder="Nhập tên địa điểm...">
                            <div class="owner-rental-suggestion-menu" id="rentalFacilitySuggestions"></div>
                        </div>
                        <button type="button" class="btn btn-brand owner-rental-search-button" id="rentalFacilitySearchBtn">
                            Tìm kiếm
                        </button>
                    </div>
                </div>

                <div class="dov-two-col owner-rental-top-grid">
                    <div class="dov-card owner-rental-chart-card">
                        <div class="owner-rental-card-header">
                            <div>
                                <h3 class="owner-rental-title">Doanh thu thuê đồ theo tháng</h3>
                            </div>
                        </div>
                        <div class="owner-rental-chart-wrap owner-rental-chart-wrap--lg">
                            <canvas id="rentalMonthlyChart"></canvas>
                        </div>
                    </div>

                    <div class="dov-card owner-rental-top-card">
                        <div class="owner-rental-card-header">
                            <div>
                                <h3 class="owner-rental-title">Top 10 đồ thuê nổi bật</h3>
                            </div>
                        </div>
                        <div class="owner-rental-top-items" id="rentalTopItems"></div>
                    </div>
                </div>

                <div class="dov-two-col owner-rental-bottom-grid">
                    <div class="dov-card owner-rental-chart-card">
                        <div class="owner-rental-card-header">
                            <div>
                                <h3 class="owner-rental-title">Doanh thu thuê đồ theo ngày</h3>
                            </div>
                        </div>
                        <div class="owner-rental-chart-wrap">
                            <canvas id="rentalDailyChart"></canvas>
                        </div>
                    </div>

                    <div class="dov-card owner-rental-chart-card">
                        <div class="owner-rental-card-header">
                            <div>
                                <h3 class="owner-rental-title">Doanh thu theo khung giờ</h3>
                            </div>
                        </div>
                        <div class="owner-rental-chart-wrap">
                            <canvas id="rentalHourlyChart"></canvas>
                        </div>
                    </div>
                </div>

                <div class="dov-card owner-rental-detail-card">
                    <div class="owner-rental-card-header">
                        <div>
                            <h3 class="owner-rental-title" id="rentalDetailTitle">Chi tiết doanh thu</h3>
                        </div>
                    </div>
                    <div class="table-responsive owner-rental-detail-table-wrap">
                        <table class="table table-hover align-middle owner-rental-detail-table">
                            <thead>
                            <tr>
                                <th>STT</th>
                                <th>Tên sản phẩm</th>
                                <th>Tổng số lượng</th>
                                <th>Số lượng thuê</th>
                                <th>Giá thuê / 30 phút</th>
                                <th>Tổng giá</th>
                            </tr>
                            </thead>
                            <tbody id="rentalDetailTableBody">
                            <tr>
                                <td colspan="6" class="text-center text-muted py-4">Đang tải dữ liệu...</td>
                            </tr>
                            </tbody>
                        </table>
                    </div>
                    <div class="owner-rental-detail-pagination" id="rentalDetailPagination"></div>
                </div>

                <div class="dov-card owner-rental-inactive-card">
                    <div class="owner-rental-card-header owner-rental-card-header--spread">
                        <div>
                            <h3 class="owner-rental-title">10 đồ không được thuê trong tháng</h3>
                        </div>
                        <div class="owner-rental-inline-actions">
                            <select class="form-select owner-rental-month-select" id="rentalInactiveMonthSelect">
                                <c:forEach begin="1" end="12" var="monthValue">
                                    <option value="${monthValue}">Tháng ${monthValue}</option>
                                </c:forEach>
                            </select>
                            <button type="button" class="btn btn-outline-danger owner-rental-danger-btn" id="rentalDeactivateBtn">
                                Ngừng hoạt động
                            </button>
                        </div>
                    </div>
                    <div class="owner-rental-inactive-list" id="rentalInactiveList">
                        <p class="text-muted mb-0">Đang tải dữ liệu...</p>
                    </div>
                </div>

            </div>
        </section>
    </div>

    <script src="https://cdnjs.cloudflare.com/ajax/libs/Chart.js/4.4.1/chart.umd.min.js"></script>
    <script src="${pageContext.request.contextPath}/assets/js/owner/dashboard.js?v=20260325-owner-dashboard-merge-fix"></script>
</div>
