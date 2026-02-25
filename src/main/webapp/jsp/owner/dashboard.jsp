<!-- dashboard.jsp -->

<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ include file="layout/layout.jsp"%>
<%@ include file="layout/sidebar.jsp"%>

<div class="main-content">
	<%@ include file="layout/header.jsp"%>

	<div class="content-area">

		<%-- ===== PAGE HEADER ROW ===== --%>
		<div class="d-flex align-items-center justify-content-between mb-4">
			<div>
				<h1 class="fw-black mb-1" style="font-size:1.75rem;color:var(--color-gray-900);letter-spacing:-0.02em;">Dashboard</h1>
				<p class="text-secondary mb-0" style="font-size:0.875rem;">Performance summary of all locations</p>
			</div>
			<a href="${pageContext.request.contextPath}/owner/export"
				class="btn btn-brand d-flex align-items-center gap-2 fw-bold rounded-3"
				style="padding:0.625rem 1.25rem;box-shadow:0 4px 14px rgba(6,78,59,0.25);">
				<i class="bi bi-download"></i> Export Data
			</a>
		</div>

		<%-- ===== ERROR ALERT ===== --%>
		<c:if test="${not empty requestScope.error}">
			<div class="alert alert-danger d-flex align-items-center gap-2 rounded-3 mb-4" role="alert">
				<i class="bi bi-exclamation-triangle-fill flex-shrink-0"></i>
				<span class="fw-semibold">${requestScope.error}</span>
			</div>
		</c:if>

		<%-- ===== STAT CARDS ===== --%>
		<div class="row row-cols-1 row-cols-sm-2 row-cols-xl-4 g-4 mb-4">

			<%-- Card: Total Locations --%>
			<div class="col">
				<div class="card border-0 rounded-4 h-100" style="box-shadow:0 1px 4px rgba(0,0,0,0.06);">
					<div class="card-body p-4">
						<div class="d-flex align-items-start justify-content-between mb-3">
							<div class="rounded-3 d-flex align-items-center justify-content-center text-white"
								style="width:48px;height:48px;background:var(--color-green-600);box-shadow:0 6px 16px rgba(22,163,74,0.3);">
								<i class="bi bi-building fs-5"></i>
							</div>
							<span class="badge rounded-pill fw-bold" style="background:var(--color-green-100);color:var(--color-green-700);">↑ 12%</span>
						</div>
						<p class="text-uppercase fw-black mb-1" style="font-size:0.625rem;letter-spacing:0.15em;color:var(--color-gray-400);">Total Locations</p>
						<p class="fw-black mb-0" style="font-size:1.875rem;color:var(--color-gray-900);">
							${requestScope.totalLocations != null ? requestScope.totalLocations : 0}
						</p>
					</div>
				</div>
			</div>

			<%-- Card: Total Customers --%>
			<div class="col">
				<div class="card border-0 rounded-4 h-100" style="box-shadow:0 1px 4px rgba(0,0,0,0.06);">
					<div class="card-body p-4">
						<div class="d-flex align-items-start justify-content-between mb-3">
							<div class="rounded-3 d-flex align-items-center justify-content-center text-white"
								style="width:48px;height:48px;background:var(--color-blue-500);box-shadow:0 6px 16px rgba(59,130,246,0.28);">
								<i class="bi bi-people fs-5"></i>
							</div>
							<span class="badge rounded-pill fw-bold" style="background:#EFF6FF;color:#1D4ED8;">↓ 1</span>
						</div>
						<p class="text-uppercase fw-black mb-1" style="font-size:0.625rem;letter-spacing:0.15em;color:var(--color-gray-400);">Total Customers</p>
						<p class="fw-black mb-0" style="font-size:1.875rem;color:var(--color-gray-900);">
							${requestScope.totalCourts != null ? requestScope.totalCourts : 0}
						</p>
					</div>
				</div>
			</div>

			<%-- Card: Monthly Revenue --%>
			<div class="col">
				<div class="card border-0 rounded-4 h-100" style="box-shadow:0 1px 4px rgba(0,0,0,0.06);">
					<div class="card-body p-4">
						<div class="d-flex align-items-start justify-content-between mb-3">
							<div class="rounded-3 d-flex align-items-center justify-content-center text-white"
								style="width:48px;height:48px;background:var(--color-green-600);box-shadow:0 6px 16px rgba(22,163,74,0.3);">
								<i class="bi bi-cash-stack fs-5"></i>
							</div>
							<span class="badge rounded-pill fw-bold" style="background:var(--color-green-100);color:var(--color-green-700);">↑ 8%</span>
						</div>
						<p class="text-uppercase fw-black mb-1" style="font-size:0.625rem;letter-spacing:0.15em;color:var(--color-gray-400);">Monthly Revenue</p>
						<p class="fw-black mb-0" style="font-size:1.875rem;color:var(--color-gray-900);">
							${requestScope.activeCourts != null ? requestScope.activeCourts : 0}
						</p>
					</div>
				</div>
			</div>

			<%-- Card: Monthly Bookings --%>
			<div class="col">
				<div class="card border-0 rounded-4 h-100" style="box-shadow:0 1px 4px rgba(0,0,0,0.06);">
					<div class="card-body p-4">
						<div class="d-flex align-items-start justify-content-between mb-3">
							<div class="rounded-3 d-flex align-items-center justify-content-center text-white"
								style="width:48px;height:48px;background:var(--color-green-brand);box-shadow:0 6px 16px rgba(6,78,59,0.25);">
								<i class="bi bi-calendar-check fs-5"></i>
							</div>
							<span class="badge rounded-pill fw-bold" style="background:var(--color-green-100);color:var(--color-green-700);">↑ 24%</span>
						</div>
						<p class="text-uppercase fw-black mb-1" style="font-size:0.625rem;letter-spacing:0.15em;color:var(--color-gray-400);">Monthly Bookings</p>
						<p class="fw-black mb-0" style="font-size:1.875rem;color:var(--color-gray-900);">
							${requestScope.monthlyBookings != null ? requestScope.monthlyBookings : 0}
						</p>
					</div>
				</div>
			</div>

		</div>

		<%-- ===== CHARTS ROW ===== --%>
		<div class="row g-4 mb-4">

			<%-- Revenue Trends --%>
			<div class="col-12 col-lg-6">
				<div class="card border-0 rounded-4 h-100" style="box-shadow:0 1px 4px rgba(0,0,0,0.06);">
					<div class="card-body p-4">
						<div class="d-flex align-items-center gap-2 mb-4">
							<i class="bi bi-graph-up" style="color:var(--color-gray-400);"></i>
							<p class="text-uppercase fw-black mb-0" style="font-size:0.625rem;letter-spacing:0.15em;color:var(--color-gray-400);">Revenue Trends</p>
						</div>
						<div style="height:240px;position:relative;">
							<canvas id="revenueChart"></canvas>
						</div>
					</div>
				</div>
			</div>

			<%-- Daily Bookings --%>
			<div class="col-12 col-lg-6">
				<div class="card border-0 rounded-4 h-100" style="box-shadow:0 1px 4px rgba(0,0,0,0.06);">
					<div class="card-body p-4">
						<div class="d-flex align-items-center gap-2 mb-4">
							<i class="bi bi-bar-chart" style="color:var(--color-gray-400);"></i>
							<p class="text-uppercase fw-black mb-0" style="font-size:0.625rem;letter-spacing:0.15em;color:var(--color-gray-400);">Daily Bookings</p>
						</div>
						<div style="height:240px;position:relative;">
							<canvas id="bookingsChart"></canvas>
						</div>
					</div>
				</div>
			</div>

		</div>

		<%-- ===== QUICK ACTIONS ===== --%>
		<div class="card border-0 rounded-4" style="box-shadow:0 1px 4px rgba(0,0,0,0.06);">
			<div class="card-body p-4">
				<p class="text-uppercase fw-black mb-3" style="font-size:0.625rem;letter-spacing:0.15em;color:var(--color-gray-400);">Quick Actions</p>
				<div class="d-flex flex-wrap gap-3">
					<a href="${pageContext.request.contextPath}/owner/facility/list"
						class="btn btn-brand d-flex align-items-center gap-2 fw-bold rounded-3"
						style="padding:0.625rem 1.25rem;box-shadow:0 4px 14px rgba(6,78,59,0.2);">
						<i class="bi bi-plus-circle"></i> Manage Locations
					</a>
					<a href="#"
						class="btn btn-outline-secondary d-flex align-items-center gap-2 fw-bold rounded-3"
						style="padding:0.625rem 1.25rem;">
						<i class="bi bi-gear"></i> System Settings
					</a>
				</div>
			</div>
		</div>

	</div><%-- end content-area --%>

	<%-- ===== CHART.JS — giữ nguyên toàn bộ logic ===== --%>
	<script src="https://cdnjs.cloudflare.com/ajax/libs/Chart.js/4.4.1/chart.umd.min.js"></script>
	<script>
		Chart.defaults.font.family = "'Inter', sans-serif";
		Chart.defaults.color = '#9CA3AF';

		const days = ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'];

		const crosshairPlugin = {
			id: 'crosshair',
			afterDraw(chart) {
				if (chart.tooltip._active && chart.tooltip._active.length) {
					const ctx = chart.ctx;
					const x = chart.tooltip._active[0].element.x;
					const topY = chart.scales.y.top;
					const bottomY = chart.scales.y.bottom;
					ctx.save();
					ctx.beginPath();
					ctx.moveTo(x, topY);
					ctx.lineTo(x, bottomY);
					ctx.lineWidth = 1.5;
					ctx.strokeStyle = '#064E3B';
					ctx.setLineDash([4, 4]);
					ctx.stroke();
					ctx.restore();
				}
			}
		};

		const revenueCtx = document.getElementById('revenueChart').getContext('2d');
		const greenGradient = revenueCtx.createLinearGradient(0, 0, 0, 240);
		greenGradient.addColorStop(0, 'rgba(22,163,74,0.20)');
		greenGradient.addColorStop(1, 'rgba(22,163,74,0.00)');

		new Chart(revenueCtx, {
			type: 'line',
			plugins: [crosshairPlugin],
			data: {
				labels: days,
				datasets: [{
					data: [2800, 2600, 2200, 3100, 4800, 6300, 5900],
					borderColor: '#064E3B',
					borderWidth: 2.5,
					backgroundColor: greenGradient,
					fill: true,
					tension: 0.45,
					pointRadius: 0,
					pointHoverRadius: 5,
					pointHoverBackgroundColor: '#064E3B',
					pointHoverBorderColor: '#fff',
					pointHoverBorderWidth: 2
				}]
			},
			options: {
				responsive: true,
				maintainAspectRatio: false,
				interaction: { mode: 'index', intersect: false },
				plugins: {
					legend: { display: false },
					tooltip: {
						backgroundColor: '#fff',
						titleColor: '#111827',
						bodyColor: '#374151',
						borderColor: '#E5E7EB',
						borderWidth: 1,
						padding: 10,
						cornerRadius: 8,
						callbacks: { label: ctx => ' revenue : ' + ctx.parsed.y.toLocaleString() }
					}
				},
				scales: {
					x: { grid: { display: false }, border: { display: false }, ticks: { font: { size: 11, weight: '600' } } },
					y: { grid: { color: '#F3F4F6', lineWidth: 1 }, border: { display: false, dash: [4,4] }, ticks: { font: { size: 11 } } }
				}
			}
		});

		new Chart(document.getElementById('bookingsChart').getContext('2d'), {
			type: 'bar',
			data: {
				labels: days,
				datasets: [{
					data: [40, 50, 38, 72, 90, 125, 110],
					backgroundColor: '#A3E635',
					hoverBackgroundColor: '#84CC16',
					borderRadius: 8,
					borderSkipped: false
				}]
			},
			options: {
				responsive: true,
				maintainAspectRatio: false,
				plugins: {
					legend: { display: false },
					tooltip: {
						backgroundColor: '#064E3B',
						titleColor: '#A3E635',
						bodyColor: '#fff',
						padding: 10,
						cornerRadius: 8,
						callbacks: { label: ctx => ' ' + ctx.parsed.y + ' bookings' }
					}
				},
				scales: {
					x: { grid: { display: false }, border: { display: false }, ticks: { font: { size: 11, weight: '600' } } },
					y: { grid: { color: '#F3F4F6' }, border: { display: false }, beginAtZero: true, ticks: { font: { size: 11 } } }
				}
			}
		});
	</script>

</div><%-- end main-content --%>