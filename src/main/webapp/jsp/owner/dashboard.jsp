<%-- dashboard.jsp --%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ include file="layout/layout.jsp"%>
<%@ include file="layout/sidebar.jsp"%>

<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/owner/dashboard.css">
<style>
  /* Xóa padding thừa của .content-area khi dùng dashboard overview */
  .content-area { padding: 0 !important; }
</style>

<div class="main-content">
    <%@ include file="layout/header.jsp"%>

    <div class="dov-wrap">

        <%-- ============================================================
             ERROR ALERT
        ============================================================ --%>
        <c:if test="${not empty requestScope.error}">
            <div class="alert alert-danger d-flex align-items-center gap-2 rounded-3 mb-4" role="alert">
                <i class="bi bi-exclamation-triangle-fill flex-shrink-0"></i>
                <span class="fw-semibold">${requestScope.error}</span>
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
                    <span class="dov-stat-badge dov-stat-badge--up">
                        <i class="bi bi-graph-up-arrow"></i> 
                        ${sessionScope.dailyRevenue.changePercent != null ? sessionScope.dailyRevenue.formattedPercent : 'null'}
                    </span>
                </div>
                <p class="dov-stat-label">Doanh thu hàng ngày</p>
                <p class="dov-stat-value">
                    ${sessionScope.dailyRevenue.currentAmount != null ? sessionScope.dailyRevenue.formattedAmount  : 'null'}
                </p>
            </div>

            <%-- Weekly Revenue --%>
            <div class="dov-card dov-fadein dov-d2">
                <div class="dov-stat-top">
                    <div class="dov-stat-icon dov-stat-icon--green">
                        <i class="bi bi-currency-dollar fs-5"></i>
                    </div>
                    <span class="dov-stat-badge dov-stat-badge--up">
                        <i class="bi bi-graph-up-arrow"></i> 
                        ${sessionScope.weeklyRevenue.changePercent != null ? sessionScope.weeklyRevenue.formattedPercent : 'null'}
                    </span>
                </div>
                <p class="dov-stat-label">>Doanh thu hàng tuần</p>
                <p class="dov-stat-value">
                    ${sessionScope.weeklyRevenue.currentAmount != null ? sessionScope.weeklyRevenue.formattedAmount  : 'null'}
                </p>
            </div>

            <%-- Monthly Revenue --%>
            <div class="dov-card dov-fadein dov-d3">
                <div class="dov-stat-top">
                    <div class="dov-stat-icon dov-stat-icon--green">
                        <i class="bi bi-currency-dollar fs-5"></i>
                    </div>
                    <span class="dov-stat-badge dov-stat-badge--up">
                        <i class="bi bi-graph-up-arrow"></i> 
                        ${sessionScope.monthlyRevenue.changePercent != null ? sessionScope.monthlyRevenue.formattedPercent : 'null'}
                    </span>
                </div>
                <p class="dov-stat-label">>Doanh thu hàng tháng</p>
                <p class="dov-stat-value">
                    ${sessionScope.monthlyRevenue.currentAmount != null ? sessionScope.monthlyRevenue.formattedAmount  : 'null'}
                </p>
            </div>

            <%-- Yearly Revenue --%>
            <div class="dov-card dov-fadein dov-d4">
                <div class="dov-stat-top">
                    <div class="dov-stat-icon dov-stat-icon--green">
                        <i class="bi bi-currency-dollar fs-5"></i>
                    </div>
                    <span class="dov-stat-badge dov-stat-badge--up">
                        <i class="bi bi-graph-up-arrow"></i>
                        ${sessionScope.yearlyRevenue.changePercent != null ? sessionScope.yearlyRevenue.formattedPercent : 'null'}
                    </span>
                </div>
                <p class="dov-stat-label">>Doanh thu hàng năm</p>
                <p class="dov-stat-value">
                    ${sessionScope.yearlyRevenue.currentAmount != null ? sessionScope.yearlyRevenue.formattedAmount  : 'null'}
                </p>
            </div>

            <%-- <%-- Total Bookings
            <div class="dov-card dov-fadein dov-d4">
                <div class="dov-stat-top">
                    <div class="dov-stat-icon dov-stat-icon--blue">
                        <i class="bi bi-check2-square fs-5"></i>
                    </div>
                    <span class="dov-stat-badge dov-stat-badge--down">
                        <i class="bi bi-graph-down-arrow"></i> -2.1%
                    </span>
                </div>
                <p class="dov-stat-label">Total Bookings</p>
                <p class="dov-stat-value">
                    ${requestScope.totalBookings != null ? requestScope.totalBookings : '4,250'}
                </p>
            </div> --%>

        </div><%-- end dov-stats-grid --%>

        <%-- ============================================================
             ROW 2: Booking Status Distribution + Occupancy Rate
        ============================================================ --%>
        <div class="dov-two-col">

            <%-- Booking Status Distribution --%>
            <div class="dov-card dov-fadein dov-d5">
                <div class="dov-card-header">
                    <h3 class="dov-card-title">Trạng thái đặt lịch (%)</h3>
                    <div class="dov-tabs" id="bookingTabs">
                        <button class="dov-tab" data-tab="booking" data-period="Day">Hôm nay</button>
                        <button class="dov-tab" data-tab="booking" data-period="Week">Tuần này</button>
                        <button class="dov-tab is-active" data-tab="booking" data-period="Month">Tháng này</button>
                        <button class="dov-tab" data-tab="booking" data-period="Year">Năm nay</button>
                    </div>
                </div>
                <div class="dov-status-list" id="bookingStatusList">
                    <%-- Rendered by JS --%>
                </div>
            </div>

            <%-- Occupancy Rate --%>
            <div class="dov-card dov-fadein dov-d6">
                <div class="dov-card-header">
                    <h3 class="dov-card-title">Tỉ lệ lấp đầy địa điểm (%)</h3>
                    <div class="dov-tabs" id="occupancyTabs">
                        <button class="dov-tab" data-tab="occupancy" data-period="Day">Hôm nay</button>
                        <button class="dov-tab" data-tab="occupancy" data-period="Week">Tuần này</button>
                        <button class="dov-tab is-active" data-tab="occupancy" data-period="Month">Tháng này</button>
                        <button class="dov-tab" data-tab="occupancy" data-period="Year">Năm nay</button>
                    </div>
                </div>
                <div class="dov-occupancy-body">
                    <%-- Legend --%>
                    <div class="dov-occ-legend">
                        <div class="dov-occ-item">
                            <div class="dov-occ-dot-row">
                                <div class="dov-occ-dot dov-occ-dot--lime"></div>
                                <span class="dov-occ-tag">Khung giờ lấp đầy</span>
                            </div>
                            <p class="dov-occ-value" id="occpctOccupied">65%</p>
                        </div>
                        <div class="dov-occ-item">
                            <div class="dov-occ-dot-row">
                                <div class="dov-occ-dot dov-occ-dot--gray"></div>
                                <span class="dov-occ-tag">Khung giờ còn trống</span>
                            </div>
                            <p class="dov-occ-value" id="occpctAvailable">35%</p>
                        </div>
                    </div>
                    <%-- SVG Donut --%>
                    <svg class="dov-donut-svg" id="donutSvg"
                         width="176" height="176" viewBox="0 0 176 176">
                        <circle class="dov-donut-track" cx="88" cy="88" r="68"/>
                        <circle class="dov-donut-arc"   cx="88" cy="88" r="68"
                                id="donutArc"
                                transform="rotate(-90 88 88)"
                                stroke-dasharray="0 427.26"/>
                        <text class="dov-donut-pct" x="88" y="83"
                              text-anchor="middle" id="donutpctText">65%</text>
                        <text class="dov-donut-label" x="88" y="102"
                              text-anchor="middle">Đã lấp đầy</text>
                    </svg>
                </div>
            </div>

        </div><%-- end row 2 --%>

        <%-- ============================================================
             ROW 3: Weekly Revenue Chart + Yearly Revenue Chart
        ============================================================ --%>
        <div class="dov-two-col">

            <%-- Weekly Revenue Bar Chart --%>
            <div class="dov-card dov-fadein dov-d7">
                <div class="dov-card-header">
                    <h3 class="dov-card-title">Daily Revenue</h3>
                    <div class="dov-tabs">
                        <button class="dov-tab is-active" data-tab="weekly" data-period="This Week">This Week</button>
                        <button class="dov-tab" data-tab="weekly" data-period="Previous Week">Previous Week</button>
                    </div>
                </div>
                <div class="dov-chart-wrap">
                    <canvas id="weeklyChart"></canvas>
                </div>
            </div>

            <%-- Yearly Revenue Bar Chart --%>
            <div class="dov-card dov-fadein dov-d8">
                <div class="dov-card-header">
                    <h3 class="dov-card-title">Montly Revenue</h3>
                    <div class="dov-tabs">
                        <button class="dov-tab is-active" data-tab="yearly" data-period="This Year">This Year</button>
                        <button class="dov-tab" data-tab="yearly" data-period="Previous Year">Previous Year</button>
                    </div>
                </div>
                <div class="dov-chart-wrap">
                    <canvas id="yearlyChart"></canvas>
                </div>
            </div>

        </div><%-- end row 3 --%>

        <%-- ============================================================
             ROW 4: Revenue Trend Area Chart
        ============================================================ --%>
        <div class="dov-card dov-fadein dov-d9">
            <div class="dov-card-header">
                <h3 class="dov-card-title">Revenue Trend</h3>
                <div class="dov-tabs">
                    <button class="dov-tab is-active" data-tab="trend" data-period="Monthly">Monthly</button>
                    <button class="dov-tab" data-tab="trend" data-period="Yearly">Yearly</button>
                </div>
            </div>
            <div class="dov-chart-wrap--lg">
                <canvas id="trendChart"></canvas>
            </div>
        </div>

    </div><%-- end dov-wrap --%>


    <%-- ================================================================
         SCRIPTS
    ================================================================ --%>
    <script src="https://cdnjs.cloudflare.com/ajax/libs/Chart.js/4.4.1/chart.umd.min.js"></script>

	<script type="application/json" id="bookingStatusRaw">
    	${not empty bookingStatusJson ? bookingStatusJson : '{"Day":[],"Week":[],"Month":[],"Year":[]}'}
	</script>
	
	<script type="application/json" id="revenueChartRaw">
   	 	${not empty revenueChartJson ? revenueChartJson : '{"weekly":{},"yearly":{},"trend":{}}'}
	</script>
	
	<script type="application/json" id="facilityStartDateRaw">
    	"${not empty facilityStartDate ? facilityStartDate : ''}"
	</script>
	
	<script>
	(function () {
	    'use strict';
	
	    /* ── Design tokens ─────────────────────────────────────────── */
	    const BRAND = '#064E3B';
	    const LIME  = '#A3E635';
	    const LIME2 = '#d9f99d';
	    const G100  = '#F3F4F6';
	    const G200  = '#E5E7EB';
	    const G400  = '#9CA3AF';
	
	    Chart.defaults.font.family = "'Inter', sans-serif";
	    Chart.defaults.color = G400;
	
	    /* ── Parse tất cả JSON một chỗ ────────────────────────────── */
	    let BOOKING_STATUS_DATA, REVENUE_CHART_DATA, FACILITY_START_DATE;
	
	    try {
	        BOOKING_STATUS_DATA = JSON.parse(document.getElementById('bookingStatusRaw').textContent);
	    } catch (e) {
	        console.error('bookingStatusRaw parse failed:', e);
	        BOOKING_STATUS_DATA = { Day: [], Week: [], Month: [], Year: [] };
	    }
	
	    try {
	        REVENUE_CHART_DATA = JSON.parse(document.getElementById('revenueChartRaw').textContent);
	    } catch (e) {
	        console.error('revenueChartRaw parse failed:', e);
	        REVENUE_CHART_DATA = { weekly: {}, yearly: {}, trend: {} };
	    }
	
	    try {
	        const raw = JSON.parse(document.getElementById('facilityStartDateRaw').textContent);
	        FACILITY_START_DATE = raw ? new Date(raw) : null;
	    } catch (e) {
	        FACILITY_START_DATE = null;
	    }
	
	    /* ── Dataset registry ──────────────────────────────────────── */
	    const DATA = {
	        booking:   BOOKING_STATUS_DATA,
	        occupancy: { Day: 58, Week: 62, Month: 65, Year: 70 },
	        weekly:    REVENUE_CHART_DATA.weekly,
	        yearly:    REVENUE_CHART_DATA.yearly,
	        trend:     REVENUE_CHART_DATA.trend,
	    };
	
	    /* ── Format VND ────────────────────────────────────────────── */
	    function formatVND(v) {
	        if (v >= 1_000_000) {
	            return ' ' + (v / 1_000_000).toLocaleString('vi-VN', { maximumFractionDigits: 1 }) + ' tr VND';
	        }
	        return ' ' + v.toLocaleString('vi-VN') + ' VND';
	    }
	
	    /* ── Shared tooltip ────────────────────────────────────────── */
	    const limeTooltip = {
	        backgroundColor: '#fff',
	        titleColor: G400,
	        bodyColor: BRAND,
	        borderColor: G200,
	        borderWidth: 1,
	        padding: 10,
	        cornerRadius: 10,
	        titleFont: { size: 10, weight: '700' },
	        bodyFont:  { size: 14, weight: '800' },
	        callbacks: {
	            label: ctx => formatVND(ctx.parsed.y)
	        }
	    };
	
	    /* ── Tooltip riêng cho trend — hiện ngày bắt đầu kinh doanh ─ */
	    const trendTooltip = {
	        ...limeTooltip,
	        callbacks: {
	            title: ctx => {
	                const label = ctx[0].label; // "2026" hoặc "Jan"
	
	                if (FACILITY_START_DATE) {
	                    const startYear  = FACILITY_START_DATE.getFullYear();
	                    const startMonth = FACILITY_START_DATE.toLocaleString('en-US', { month: 'short' });
	                    const startDay   = FACILITY_START_DATE.getDate();
	
	                    // Yearly trend — label là năm
	                    if (/^\d{4}$/.test(label) && parseInt(label) === startYear) {
	                        return label + ' (from ' + startDay + ' ' + startMonth + ' ' + startYear + ')';
	                    }
	                }
	                return label;
	            },
	            label: ctx => formatVND(ctx.parsed.y)
	        }
	    };
	
	    /* ── Build bar chart ───────────────────────────────────────── */
	    function makeBarChart(canvasId, dataset) {
	        const ctx = document.getElementById(canvasId).getContext('2d');
	        return new Chart(ctx, {
	            type: 'bar',
	            data: {
	                labels: dataset.labels,
	                datasets: [{
	                    data: dataset.data,
	                    backgroundColor: LIME,
	                    hoverBackgroundColor: LIME2,
	                    borderRadius: 6,
	                    borderSkipped: false,
	                }]
	            },
	            options: {
	                responsive: true, maintainAspectRatio: false,
	                plugins: { legend: { display: false }, tooltip: limeTooltip },
	                scales: {
	                    x: { grid: { display: false }, border: { display: false }, ticks: { font: { size: 11, weight: '600' }, color: G400 } },
	                    y: { grid: { color: G100 },    border: { display: false }, ticks: { font: { size: 11 }, color: G400,
	                        callback: v => v >= 1_000_000
	                            ? (v / 1_000_000).toLocaleString('vi-VN', { maximumFractionDigits: 1 }) + 'tr'
	                            : v.toLocaleString('vi-VN')
	                    }}
	                }
	            }
	        });
	    }
	
	    /* ── Build area chart ──────────────────────────────────────── */
	    function makeAreaChart(canvasId, dataset) {
	        const ctx = document.getElementById(canvasId).getContext('2d');
	        const grad = ctx.createLinearGradient(0, 0, 0, 240);
	        grad.addColorStop(0, 'rgba(163,230,53,0.35)');
	        grad.addColorStop(1, 'rgba(163,230,53,0.02)');
	
	        return new Chart(ctx, {
	            type: 'line',
	            data: {
	                labels: dataset.labels,
	                datasets: [{
	                    data: dataset.data,
	                    borderColor: BRAND,
	                    borderWidth: 2.5,
	                    backgroundColor: grad,
	                    fill: true,
	                    tension: 0.45,
	                    pointRadius: 0,
	                    pointHoverRadius: 5,
	                    pointHoverBackgroundColor: BRAND,
	                    pointHoverBorderColor: '#fff',
	                    pointHoverBorderWidth: 2,
	                }]
	            },
	            options: {
	                responsive: true, maintainAspectRatio: false,
	                interaction: { mode: 'index', intersect: false },
	                plugins: {
	                    legend: { display: false },
	                    tooltip: trendTooltip       // ← dùng trendTooltip có title ngày
	                },
	                scales: {
	                    x: { grid: { display: false }, border: { display: false }, ticks: { font: { size: 11, weight: '600' }, color: G400 } },
	                    y: { grid: { color: G100 },    border: { display: false }, ticks: { font: { size: 11 }, color: G400,
	                        callback: v => v >= 1_000_000
	                            ? (v / 1_000_000).toLocaleString('vi-VN', { maximumFractionDigits: 1 }) + 'tr'
	                            : v.toLocaleString('vi-VN')
	                    }}
	                }
	            }
	        });
	    }
	
	    /* ── Update chart data in-place ────────────────────────────── */
	    function updateChart(chart, dataset) {
	        chart.data.labels = dataset.labels;
	        chart.data.datasets[0].data = dataset.data;
	        chart.update('active');
	    }
	
	    /* ── Booking Status bars + tooltip ────────────────────────── */
	    function renderBookingStatus(period) {
	        const rows = DATA.booking[period];
	        const list = document.getElementById('bookingStatusList');
	        list.innerHTML = '';
	
	        if (!rows || rows.length === 0) {
	            list.innerHTML = '<p style="color:#9CA3AF;font-size:13px;padding:16px 0;">No data available</p>';
	            return;
	        }
	
	        rows.forEach(function (row) {
	            var html = '<div class="dov-status-row">'
	                + '<span class="dov-status-name">' + row.label + '</span>'
	                + '<div class="dov-status-track">'
	                + '<div class="dov-status-bar"'
	                + '     style="width:' + row.pct + '%;background:' + row.color + ';"'
	                + '     data-count="' + row.count + '"'
	                + '     data-label="' + row.label + '">'
	                + '</div>'
	                + '</div>'
	                + '<span class="dov-status-pct">' + row.pct + '%</span>'
	                + '</div>';
	            list.insertAdjacentHTML('beforeend', html);
	        });
	
	        attachBarTooltips();
	    }
	
	    /* ── Custom tooltip hover thanh bar ────────────────────────── */
	    function attachBarTooltips() {
	        const tooltip = document.getElementById('statusTooltip') || createTooltipEl();
	
	        document.querySelectorAll('.dov-status-bar').forEach(bar => {
	            bar.addEventListener('mouseenter', function () {
	                tooltip.textContent   = this.dataset.label + ': '
	                                      + Number(this.dataset.count).toLocaleString('vi-VN')
	                                      + ' bookings';
	                tooltip.style.display = 'block';
	            });
	            bar.addEventListener('mousemove', function (e) {
	                tooltip.style.left = (e.pageX + 12) + 'px';
	                tooltip.style.top  = (e.pageY - 28) + 'px';
	            });
	            bar.addEventListener('mouseleave', function () {
	                tooltip.style.display = 'none';
	            });
	        });
	    }
	
	    function createTooltipEl() {
	        const el = document.createElement('div');
	        el.id = 'statusTooltip';
	        el.style.cssText = `
	            position: fixed;
	            background: #fff;
	            border: 1px solid #E5E7EB;
	            border-radius: 8px;
	            padding: 6px 12px;
	            font-size: 13px;
	            font-weight: 700;
	            color: #064E3B;
	            pointer-events: none;
	            display: none;
	            z-index: 9999;
	            box-shadow: 0 2px 8px rgba(0,0,0,0.08);
	        `;
	        document.body.appendChild(el);
	        return el;
	    }
	
	    /* ── Occupancy donut ───────────────────────────────────────── */
	    const CIRC = 2 * Math.PI * 68;
	
	    function renderOccupancy(period) {
	        const pct      = DATA.occupancy[period];
	        const dashArr  = (pct / 100) * CIRC;
	        const arc      = document.getElementById('donutArc');
	        const txtpct   = document.getElementById('donutpctText');
	        const lblOcc   = document.getElementById('occpctOccupied');
	        const lblAvail = document.getElementById('occpctAvailable');
	
	        arc.setAttribute('stroke-dasharray', dashArr.toFixed(2) + ' ' + (CIRC - dashArr).toFixed(2));
	        txtpct.textContent   = pct + '%';
	        lblOcc.textContent   = pct + '%';
	        lblAvail.textContent = (100 - pct) + '%';
	    }
	
	    /* ── Tab click handler ─────────────────────────────────────── */
	    const charts = {};
	
	    function initTabGroup(containerSelector, handler) {
	        document.querySelectorAll(containerSelector + ' .dov-tab').forEach(btn => {
	            btn.addEventListener('click', function () {
	                document.querySelectorAll(containerSelector + ' .dov-tab')
	                    .forEach(b => b.classList.remove('is-active'));
	                this.classList.add('is-active');
	                handler(this.dataset.period);
	            });
	        });
	    }
	
	    /* ── Init ──────────────────────────────────────────────────── */
	    document.addEventListener('DOMContentLoaded', function () {
	
	        /* Booking status */
	        renderBookingStatus('Month');
	        initTabGroup('#bookingTabs', period => renderBookingStatus(period));
	
	        /* Occupancy */
	        renderOccupancy('Month');
	        initTabGroup('#occupancyTabs', period => renderOccupancy(period));
	
	        /* Weekly bar */
	        charts.weekly = makeBarChart('weeklyChart', DATA.weekly['This Week']);
	        document.querySelectorAll('[data-tab="weekly"]').forEach(btn => {
	            btn.addEventListener('click', function () {
	                document.querySelectorAll('[data-tab="weekly"]').forEach(b => b.classList.remove('is-active'));
	                this.classList.add('is-active');
	                updateChart(charts.weekly, DATA.weekly[this.dataset.period]);
	            });
	        });
	
	        /* Yearly bar */
	        charts.yearly = makeBarChart('yearlyChart', DATA.yearly['This Year']);
	        document.querySelectorAll('[data-tab="yearly"]').forEach(btn => {
	            btn.addEventListener('click', function () {
	                document.querySelectorAll('[data-tab="yearly"]').forEach(b => b.classList.remove('is-active'));
	                this.classList.add('is-active');
	                updateChart(charts.yearly, DATA.yearly[this.dataset.period]);
	            });
	        });
	
	        /* Revenue trend area */
	        charts.trend = makeAreaChart('trendChart', DATA.trend['Monthly']);
	        document.querySelectorAll('[data-tab="trend"]').forEach(btn => {
	            btn.addEventListener('click', function () {
	                document.querySelectorAll('[data-tab="trend"]').forEach(b => b.classList.remove('is-active'));
	                this.classList.add('is-active');
	                updateChart(charts.trend, DATA.trend[this.dataset.period]);
	            });
	        });
	    });
	
	})();
	</script>

</div><%-- end main-content --%>
