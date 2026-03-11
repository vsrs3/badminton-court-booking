<%-- owner-voucher-dashboard.jsp --%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ include file="../layout/layout.jsp" %>
<%@ include file="../layout/sidebar.jsp" %>

<div class="main-content">
    <%@ include file="../layout/header.jsp" %>

    <div class="content-area">
        <%@ include file="../layout/page-header.jsp" %>

        <%-- ===== STAT CARDS ===== --%>
        <div class="row row-cols-1 row-cols-sm-2 row-cols-xl-4 g-4 mb-4" id="statCards">
            <div class="col"><div class="card border-0 rounded-4 h-100" style="min-height:120px;box-shadow:0 1px 4px rgba(0,0,0,0.06);background:var(--color-gray-100);"></div></div>
            <div class="col"><div class="card border-0 rounded-4 h-100" style="min-height:120px;box-shadow:0 1px 4px rgba(0,0,0,0.06);background:var(--color-gray-100);"></div></div>
            <div class="col"><div class="card border-0 rounded-4 h-100" style="min-height:120px;box-shadow:0 1px 4px rgba(0,0,0,0.06);background:var(--color-gray-100);"></div></div>
            <div class="col"><div class="card border-0 rounded-4 h-100" style="min-height:120px;box-shadow:0 1px 4px rgba(0,0,0,0.06);background:var(--color-gray-100);"></div></div>
        </div>

        <%-- ===== CHARTS ROW ===== --%>
        <div class="row g-4 mb-4">
            <%-- Line chart --%>
            <div class="col-12 col-xl-8">
                <div class="card border-0 rounded-4" style="box-shadow:0 1px 4px rgba(0,0,0,0.06);">
                    <div class="card-header bg-white border-0 pt-4 pb-0 px-4">
                        <h6 class="fw-bold mb-0" style="color:var(--color-gray-900);">
                            <i class="bi bi-graph-up me-2" style="color:var(--color-green-600);"></i>
                            Lượt sử dụng voucher (30 ngày qua)
                        </h6>
                    </div>
                    <div class="card-body px-4 pb-4" style="position:relative;height:260px;">
                        <canvas id="lineChart"></canvas>
                    </div>
                </div>
            </div>
            <%-- Bar chart --%>
            <div class="col-12 col-xl-4">
                <div class="card border-0 rounded-4" style="box-shadow:0 1px 4px rgba(0,0,0,0.06);">
                    <div class="card-header bg-white border-0 pt-4 pb-0 px-4">
                        <h6 class="fw-bold mb-0" style="color:var(--color-gray-900);">
                            <i class="bi bi-bar-chart-fill me-2" style="color:var(--color-green-600);"></i>
                            Top voucher được dùng nhiều nhất
                        </h6>
                    </div>
                    <div class="card-body px-4 pb-4" style="position:relative;height:260px;">
                        <canvas id="barChart"></canvas>
                    </div>
                </div>
            </div>
        </div>

        <%-- ===== QUICK ACTIONS ===== --%>
        <div class="d-flex gap-3 mb-4">
            <a href="${pageContext.request.contextPath}/owner/vouchers/list"
               class="btn btn-brand d-flex align-items-center gap-2 fw-semibold rounded-3">
                <i class="bi bi-list-ul"></i> Xem danh sách voucher
            </a>
            <a href="${pageContext.request.contextPath}/owner/vouchers/create"
               class="btn btn-outline-secondary d-flex align-items-center gap-2 fw-semibold rounded-3">
                <i class="bi bi-plus-circle"></i> Tạo voucher mới
            </a>
        </div>

    </div><%-- end .content-area --%>

    <%-- Chart.js – MUST be loaded inside body, before footer closes it --%>
    <script src="https://cdn.jsdelivr.net/npm/chart.js@4.4.0/dist/chart.umd.min.js"></script>
    <script>
    (function () {
        'use strict';

        const API_URL = '${pageContext.request.contextPath}/api/owner/vouchers/dashboard';

        function formatVND(n) {
            if (!n && n !== 0) return '0 ₫';
            return Number(n).toLocaleString('vi-VN') + ' ₫';
        }

        const statDefs = [
            { key: 'totalVouchers',        label: 'Tổng số voucher',            icon: 'bi-ticket-perforated', bg: 'var(--color-green-600)', shadow: 'rgba(22,163,74,0.3)' },
            { key: 'activeVouchers',       label: 'Voucher đang hoạt động',     icon: 'bi-check-circle',      bg: 'var(--color-blue-500)',  shadow: 'rgba(59,130,246,0.28)' },
            { key: 'monthlyUsageCount',    label: 'Lượt dùng trong tháng',      icon: 'bi-arrow-repeat',      bg: 'var(--color-orange-600)',shadow: 'rgba(234,88,12,0.28)' },
            { key: 'monthlyDiscountTotal', label: 'Tổng tiền giảm trong tháng', icon: 'bi-cash-stack',        bg: '#7C3AED',               shadow: 'rgba(124,58,237,0.28)', formatter: formatVND },
        ];

        function renderStats(data) {
            const container = document.getElementById('statCards');
            container.innerHTML = statDefs.map(function(def) {
                const raw = data[def.key];
                const val = def.formatter ? def.formatter(raw) : (raw != null ? raw : 0);
                return '<div class="col">'
                    + '<div class="card border-0 rounded-4 h-100" style="box-shadow:0 1px 4px rgba(0,0,0,0.06);">'
                    + '<div class="card-body p-4">'
                    + '<div class="d-flex align-items-start justify-content-between mb-3">'
                    + '<div class="rounded-3 d-flex align-items-center justify-content-center text-white"'
                    + ' style="width:48px;height:48px;background:' + def.bg + ';box-shadow:0 6px 16px ' + def.shadow + ';">'
                    + '<i class="bi ' + def.icon + ' fs-5"></i></div></div>'
                    + '<p class="text-uppercase fw-black mb-1" style="font-size:0.625rem;letter-spacing:0.15em;color:var(--color-gray-400);">' + def.label + '</p>'
                    + '<p class="fw-black mb-0" style="font-size:1.875rem;color:var(--color-gray-900);">' + val + '</p>'
                    + '</div></div></div>';
            }).join('');
        }

        let lineChart, barChart;

        function renderLineChart(dailyData) {
            const today = new Date();
            const labels = [], counts = [];
            for (let i = 29; i >= 0; i--) {
                const d = new Date(today);
                d.setDate(d.getDate() - i);
                const key = d.toISOString().slice(0, 10);
                const found = (dailyData || []).find(function(r) { return r.date === key; });
                labels.push(key.slice(5));
                counts.push(found ? found.count : 0);
            }
            const ctx = document.getElementById('lineChart').getContext('2d');
            if (lineChart) lineChart.destroy();
            lineChart = new Chart(ctx, {
                type: 'line',
                data: {
                    labels: labels,
                    datasets: [{
                        label: 'Lượt dùng',
                        data: counts,
                        borderColor: '#16A34A',
                        backgroundColor: 'rgba(22,163,74,0.08)',
                        borderWidth: 2,
                        pointRadius: 3,
                        pointBackgroundColor: '#16A34A',
                        tension: 0.4,
                        fill: true
                    }]
                },
                options: {
                    responsive: true,
                    maintainAspectRatio: false,
                    animation: false,
                    plugins: { legend: { display: false } },
                    scales: {
                        y: { beginAtZero: true, ticks: { stepSize: 1 } },
                        x: { ticks: { maxTicksLimit: 10 } }
                    }
                }
            });
        }

        function renderBarChart(topData) {
            const labels = (topData || []).map(function(r) { return r.code; });
            const counts = (topData || []).map(function(r) { return r.usageCount; });
            const ctx = document.getElementById('barChart').getContext('2d');
            if (barChart) barChart.destroy();
            barChart = new Chart(ctx, {
                type: 'bar',
                data: {
                    labels: labels,
                    datasets: [{
                        label: 'Lượt dùng',
                        data: counts,
                        backgroundColor: 'rgba(6,78,59,0.75)',
                        borderRadius: 6
                    }]
                },
                options: {
                    indexAxis: 'y',
                    responsive: true,
                    maintainAspectRatio: false,
                    animation: false,
                    plugins: { legend: { display: false } },
                    scales: { x: { beginAtZero: true, ticks: { stepSize: 1 } } }
                }
            });
        }

        function loadDashboard() {
            fetch(API_URL)
                .then(function(r) {
                    if (!r.ok) throw new Error('API error ' + r.status);
                    return r.json();
                })
                .then(function(data) {
                    renderStats(data);
                    renderLineChart(data.dailyUsageChart);
                    renderBarChart(data.topVouchers);
                })
                .catch(function(err) { console.error('Dashboard load error:', err); });
        }

        document.addEventListener('DOMContentLoaded', loadDashboard);
    })();
    </script>

    <%@ include file="../layout/footer.jsp" %>
