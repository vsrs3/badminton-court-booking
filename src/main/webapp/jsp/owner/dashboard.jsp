<%-- dashboard.jsp --%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ include file="layout/layout.jsp"%>
<%@ include file="layout/sidebar.jsp"%>

<link rel="stylesheet"
	href="${pageContext.request.contextPath}/assets/css/owner/dashboard.css">
<style>
.content-area {
	padding: 0 !important;
}
</style>

<div class="main-content">
	<%@ include file="layout/header.jsp"%>

	<div class="dov-wrap">

		<%-- ============================================================
             ERROR ALERT
        ============================================================ --%>
		<c:if test="${not empty requestScope.error}">
			<div
				class="alert alert-danger d-flex align-items-center gap-2 rounded-3 mb-4"
				role="alert">
				<i class="bi bi-exclamation-triangle-fill flex-shrink-0"></i> <span
					class="fw-semibold">${requestScope.error}</span>
			</div>
		</c:if>

		<%-- ============================================================
             STAT CARDS
        ============================================================ --%>
		<div class="dov-stats-grid">

			<%-- Daily Revenue --%>
			<div class="dov-card dov-fadein dov-d1">
				<div class="dov-stat-top">
					<div class="dov-stat-icon dov-stat-icon--green">
						<i class="bi bi-currency-dollar fs-5"></i>
					</div>
					<span class="dov-stat-badge dov-stat-badge--up"> <i
						class="bi bi-graph-up-arrow"></i> ${not empty dailyRevenue ? dailyRevenue.formattedPercent : '0%'}
					</span>
				</div>
				<p class="dov-stat-label">Doanh thu hàng ngày</p>
				<p class="dov-stat-value">${not empty dailyRevenue ? dailyRevenue.formattedAmount : '0 VND'}
				</p>
			</div>

			<%-- Weekly Revenue --%>
			<div class="dov-card dov-fadein dov-d2">
				<div class="dov-stat-top">
					<div class="dov-stat-icon dov-stat-icon--green">
						<i class="bi bi-currency-dollar fs-5"></i>
					</div>
					<span class="dov-stat-badge dov-stat-badge--up"> <i
						class="bi bi-graph-up-arrow"></i> ${not empty weeklyRevenue ? weeklyRevenue.formattedPercent : '0%'}
					</span>
				</div>
				<p class="dov-stat-label">Doanh thu hàng tuần</p>
				<p class="dov-stat-value">${not empty weeklyRevenue ? weeklyRevenue.formattedAmount : '0 VND'}
				</p>
			</div>

			<%-- Monthly Revenue --%>
			<div class="dov-card dov-fadein dov-d3">
				<div class="dov-stat-top">
					<div class="dov-stat-icon dov-stat-icon--green">
						<i class="bi bi-currency-dollar fs-5"></i>
					</div>
					<span class="dov-stat-badge dov-stat-badge--up"> <i
						class="bi bi-graph-up-arrow"></i> ${not empty monthlyRevenue ? monthlyRevenue.formattedPercent : '0%'}
					</span>
				</div>
				<p class="dov-stat-label">Doanh thu hàng tháng</p>
				<p class="dov-stat-value">${not empty monthlyRevenue ? monthlyRevenue.formattedAmount : '0 VND'}
				</p>
			</div>

			<%-- Yearly Revenue --%>
			<div class="dov-card dov-fadein dov-d4">
				<div class="dov-stat-top">
					<div class="dov-stat-icon dov-stat-icon--green">
						<i class="bi bi-currency-dollar fs-5"></i>
					</div>
					<span class="dov-stat-badge dov-stat-badge--up"> <i
						class="bi bi-graph-up-arrow"></i> ${not empty yearlyRevenue ? yearlyRevenue.formattedPercent : '0%'}
					</span>
				</div>
				<p class="dov-stat-label">Doanh thu hàng năm</p>
				<p class="dov-stat-value">${not empty yearlyRevenue ? yearlyRevenue.formattedAmount : '0 VND'}
				</p>
			</div>

		</div>
		<%-- end dov-stats-grid --%>

		<%-- ============================================================
             ROW 2: Booking Status + Occupancy Rate
        ============================================================ --%>
		<div class="dov-two-col">

			<%-- Booking Status --%>
			<div class="dov-card dov-fadein dov-d5">
				<div class="dov-card-header">
					<h3 class="dov-card-title">Trạng thái đặt lịch (%)</h3>
					<div class="dov-tabs" id="bookingTabs">
						<button class="dov-tab" data-tab="booking" data-period="Day">Hôm
							nay</button>
						<button class="dov-tab" data-tab="booking" data-period="Week">Tuần
							này</button>
						<button class="dov-tab is-active" data-tab="booking"
							data-period="Month">Tháng này</button>
						<button class="dov-tab" data-tab="booking" data-period="Year">Năm
							nay</button>
					</div>
				</div>
				<div class="dov-status-list" id="bookingStatusList"></div>
			</div>

			<%-- Occupancy Rate --%>
			<div class="dov-card dov-fadein dov-d6">
				<div class="dov-card-header">
					<h3 class="dov-card-title">Tỉ lệ lấp đầy địa điểm (%)</h3>
					<div class="dov-tabs" id="occupancyTabs">
						<button class="dov-tab" data-tab="occupancy" data-period="Day">Hôm
							nay</button>
						<button class="dov-tab" data-tab="occupancy" data-period="Week">Tuần
							này</button>
						<button class="dov-tab is-active" data-tab="occupancy"
							data-period="Month">Tháng này</button>
						<button class="dov-tab" data-tab="occupancy" data-period="Year">Năm
							nay</button>
					</div>
				</div>
				<div class="dov-occupancy-body">
					<div class="dov-occ-legend">
						<div class="dov-occ-item">
							<div class="dov-occ-dot-row">
								<div class="dov-occ-dot dov-occ-dot--lime"></div>
								<span class="dov-occ-tag">Khung giờ lấp đầy</span>
							</div>
							<p class="dov-occ-value" id="occpctOccupied">0%</p>
						</div>
						<div class="dov-occ-item">
							<div class="dov-occ-dot-row">
								<div class="dov-occ-dot dov-occ-dot--gray"></div>
								<span class="dov-occ-tag">Khung giờ còn trống</span>
							</div>
							<p class="dov-occ-value" id="occpctAvailable">100%</p>
						</div>
					</div>
					<svg class="dov-donut-svg" width="176" height="176"
						viewBox="0 0 176 176">
                        <circle class="dov-donut-track" cx="88" cy="88"
							r="68" />
                        <circle class="dov-donut-arc" cx="88" cy="88"
							r="68" id="donutArc" transform="rotate(-90 88 88)"
							stroke-dasharray="0 427.26" />
                        <text class="dov-donut-pct" x="88" y="83"
							text-anchor="middle" id="donutpctText">0%</text>
                        <text class="dov-donut-label" x="88" y="102"
							text-anchor="middle">Đã lấp đầy</text>
                    </svg>
				</div>
			</div>

		</div>
		<%-- end row 2 --%>

		<%-- ============================================================
             ROW 3: Weekly + Yearly Revenue Charts
        ============================================================ --%>
		<div class="dov-two-col">

			<div class="dov-card dov-fadein dov-d7">
				<div class="dov-card-header">
					<h3 class="dov-card-title">Doanh thu trong tuần</h3>
					<div class="dov-tabs">
						<button class="dov-tab is-active" data-tab="weekly"
							data-period="This Week">Tuần này</button>
						<button class="dov-tab" data-tab="weekly"
							data-period="Previous Week">Tuần trước</button>
					</div>
				</div>
				<div class="dov-chart-wrap">
					<canvas id="weeklyChart"></canvas>
				</div>
			</div>

			<div class="dov-card dov-fadein dov-d8">
				<div class="dov-card-header">
					<h3 class="dov-card-title">Doanh thu trong năm</h3>
					<div class="dov-tabs">
						<button class="dov-tab is-active" data-tab="yearly"
							data-period="This Year">Năm nay</button>
						<button class="dov-tab" data-tab="yearly"
							data-period="Previous Year">Năm trước</button>
					</div>
				</div>
				<div class="dov-chart-wrap">
					<canvas id="yearlyChart"></canvas>
				</div>
			</div>

		</div>
		<%-- end row 3 --%>

		<%-- ============================================================
             ROW 4: Peak Hours
        ============================================================ --%>
		<div class="dov-card dov-fadein dov-d10">
			<div class="dov-card-header">
				<h3 class="dov-card-title">Phân tích giờ cao điểm (30 ngày gần
					nhất)</h3>
			</div>

			<div class="dov-peak-stats">

				<div class="dov-peak-stat dov-peak-stat--peak">
					<div class="dov-peak-stat-icon">
						<i class="bi bi-clock-fill"></i>
					</div>
					<div class="dov-peak-stat-body">
						<p class="dov-peak-stat-label">Giờ cao điểm</p>
						<p class="dov-peak-stat-value" id="peakSlotTime">—</p>
						<p class="dov-peak-stat-sub" id="peakSlotPct">—</p>
					</div>
				</div>

				<div class="dov-peak-stat dov-peak-stat--normal">
					<div class="dov-peak-stat-icon">
						<i class="bi bi-clock"></i>
					</div>
					<div class="dov-peak-stat-body">
						<p class="dov-peak-stat-label">Giờ bình thường</p>
						<p class="dov-peak-stat-value" id="normalSlotTime">—</p>
						<p class="dov-peak-stat-sub" id="normalSlotPct">—</p>
					</div>
				</div>

				<div class="dov-peak-stat dov-peak-stat--low">
					<div class="dov-peak-stat-icon">
						<i class="bi bi-clock"></i>
					</div>
					<div class="dov-peak-stat-body">
						<p class="dov-peak-stat-label">Giờ thấp điểm</p>
						<p class="dov-peak-stat-value" id="lowSlotTime">—</p>
						<p class="dov-peak-stat-sub" id="lowSlotPct">—</p>
					</div>
				</div>

			</div>

			<div class="dov-heatmap-wrap" id="peakHeatmap"></div>

		</div>

		<%-- ============================================================
             QUICK ACTIONS
        ============================================================ --%>
		<div class="dov-card dov-fadein dov-d9">
			<div class="dov-card-header">
				<h3 class="dov-card-title">Thao tác nhanh</h3>
			</div>
			<div class="dov-quick-actions">

				<a href="${pageContext.request.contextPath}/owner/facility/list"
					class="dov-qa-btn">
					<div class="dov-qa-icon"
						style="background: rgba(6, 78, 59, 0.10); color: #064E3B;">
						<i class="bi bi-building fs-5"></i>
					</div> <span>Danh Sách Địa Điểm</span>
				</a> <a href="${pageContext.request.contextPath}/owner/staffs/list"
					class="dov-qa-btn">
					<div class="dov-qa-icon"
						style="background: rgba(59, 130, 246, 0.10); color: #3B82F6;">
						<i class="bi bi-person-badge fs-5"></i>
					</div> <span>Quản Lý Nhân Viên</span>
				</a> <a
					href="${pageContext.request.contextPath}/owner/vouchers/dashboard"
					class="dov-qa-btn">
					<div class="dov-qa-icon"
						style="background: rgba(234, 179, 8, 0.10); color: #B45309;">
						<i class="bi bi-ticket-perforated fs-5"></i>
					</div> <span>Quản Lý Voucher</span>
				</a> <a href="${pageContext.request.contextPath}/owner/inventory"
					class="dov-qa-btn">
					<div class="dov-qa-icon"
						style="background: rgba(163, 230, 53, 0.15); color: #4d7c0f;">
						<i class="bi bi-box-seam fs-5"></i>
					</div> <span>Quản Lý Dụng Cụ</span>
				</a> <a href="#" class="dov-qa-btn">
					<div class="dov-qa-icon"
						style="background: rgba(107, 114, 128, 0.10); color: #374151;">
						<i class="bi bi-gear fs-5"></i>
					</div> <span>Cài Đặt</span>
				</a>

			</div>
		</div>

	</div>
	<%-- end dov-wrap --%>

	<%-- ── JSON data cho JS đọc ─────────────────────────────────── --%>
	<script type="application/json" id="bookingStatusRaw">
        ${not empty bookingStatusJson ? bookingStatusJson : '{"Day":[],"Week":[],"Month":[],"Year":[]}'}
    </script>

	<%-- fallback khớp với JS: chỉ weekly + yearly, không có trend --%>
	<script type="application/json" id="revenueChartRaw">
        ${not empty revenueChartJson ? revenueChartJson : '{"weekly":{},"yearly":{}}'}
    </script>

	<script type="application/json" id="facilityStartDateRaw">
        "${not empty facilityStartDate ? facilityStartDate : ''}"
    </script>

	<script type="application/json" id="occupancyRaw">
        ${not empty occupancyJson ? occupancyJson : '{"Day":0,"Week":0,"Month":0,"Year":0}'}
    </script>

	<script id="peakHourRaw" type="application/json">
        ${not empty peakHourJson ? peakHourJson : '{"heatmap":[],"peakSlots":[],"lowSlots":[],"normalTimeRange":""}'}
    </script>

	<%-- Scripts --%>
	<script
		src="https://cdnjs.cloudflare.com/ajax/libs/Chart.js/4.4.1/chart.umd.min.js"></script>
	<script
		src="${pageContext.request.contextPath}/assets/js/owner/dashboard.js"></script>

</div>
<%-- end main-content --%>
