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
    let BOOKING_STATUS_DATA, REVENUE_CHART_DATA, FACILITY_START_DATE,
        OCCUPANCY_DATA, PEAK_HOUR_DATA;

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

    try {
        OCCUPANCY_DATA = JSON.parse(document.getElementById('occupancyRaw').textContent);
    } catch (e) {
        console.error('occupancyRaw parse failed:', e);
        OCCUPANCY_DATA = { Day: 0, Week: 0, Month: 0, Year: 0 };
    }

    try {
        PEAK_HOUR_DATA = JSON.parse(document.getElementById('peakHourRaw').textContent);
    } catch (e) {
        console.error('peakHourRaw parse failed:', e);
        PEAK_HOUR_DATA = { heatmap: [], peakSlots: [], lowSlots: [], normalTimeRange: '' };
    }

    /* ── Dataset registry ──────────────────────────────────────── */
    const DATA = {
        booking:   BOOKING_STATUS_DATA,
        occupancy: OCCUPANCY_DATA,
        weekly:    REVENUE_CHART_DATA.weekly,
        yearly:    REVENUE_CHART_DATA.yearly,
        trend:     REVENUE_CHART_DATA.trend,
    };

    /* ── Map label tiếng Anh → tiếng Việt ─────────────────────── */
    const LABEL_MAP = {
        'Mon': 'Thứ 2', 'Tue': 'Thứ 3', 'Wed': 'Thứ 4',
        'Thu': 'Thứ 5', 'Fri': 'Thứ 6', 'Sat': 'Thứ 7', 'Sun': 'CN',
        'Jan': 'Tháng 1',  'Feb': 'Tháng 2',  'Mar': 'Tháng 3',
        'Apr': 'Tháng 4',  'May': 'Tháng 5',  'Jun': 'Tháng 6',
        'Jul': 'Tháng 7',  'Aug': 'Tháng 8',  'Sep': 'Tháng 9',
        'Oct': 'Tháng 10', 'Nov': 'Tháng 11', 'Dec': 'Tháng 12',
    };

    function translateLabels(dataset) {
        if (!dataset || !dataset.labels) return dataset;
        return {
            labels: dataset.labels.map(l => LABEL_MAP[l] || l),
            data:   dataset.data
        };
    }

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

    /* ── Tooltip riêng cho trend ───────────────────────────────── */
    const trendTooltip = {
        ...limeTooltip,
        callbacks: {
            title: ctx => {
                const label = ctx[0].label;
                if (FACILITY_START_DATE) {
                    const startYear  = FACILITY_START_DATE.getFullYear();
                    const startMonth = FACILITY_START_DATE.toLocaleString('en-US', { month: 'short' });
                    const startDay   = FACILITY_START_DATE.getDate();
                    if (/^\d{4}$/.test(label) && parseInt(label) === startYear) {
                        return label + ' (từ ngày ' + startDay + ' ' + startMonth + ' ' + startYear + ')';
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
                    x: {
                        grid: { display: false },
                        border: { display: false },
                        ticks: { font: { size: 11, weight: '600' }, color: G400 }
                    },
                    y: {
                        grid: { color: G100 },
                        border: { display: false },
                        ticks: {
                            font: { size: 11 }, color: G400,
                            callback: v => v >= 1_000_000
                                ? (v / 1_000_000).toLocaleString('vi-VN', { maximumFractionDigits: 1 }) + 'tr'
                                : v.toLocaleString('vi-VN')
                        }
                    }
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
                    tooltip: trendTooltip
                },
                scales: {
                    x: {
                        grid: { display: false },
                        border: { display: false },
                        ticks: { font: { size: 11, weight: '600' }, color: G400 }
                    },
                    y: {
                        grid: { color: G100 },
                        border: { display: false },
                        ticks: {
                            font: { size: 11 }, color: G400,
                            callback: v => v >= 1_000_000
                                ? (v / 1_000_000).toLocaleString('vi-VN', { maximumFractionDigits: 1 }) + 'tr'
                                : v.toLocaleString('vi-VN')
                        }
                    }
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
            list.innerHTML = '<p style="color:#9CA3AF;font-size:13px;padding:16px 0;">Không có dữ liệu</p>';
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
        const tooltip = document.getElementById('statusTooltip') || createTooltipEl('statusTooltip');

        document.querySelectorAll('.dov-status-bar').forEach(bar => {
            bar.addEventListener('mouseenter', function () {
                tooltip.textContent   = this.dataset.label + ': '
                                      + Number(this.dataset.count).toLocaleString('vi-VN')
                                      + ' lượt đặt';
                tooltip.style.display = 'block';
            });
            bar.addEventListener('mousemove', function (e) {
                tooltip.style.left = (e.clientX + 12) + 'px';
                tooltip.style.top  = (e.clientY - 36) + 'px';
            });
            bar.addEventListener('mouseleave', function () {
                tooltip.style.display = 'none';
            });
        });
    }

    /* ── Tạo tooltip element dùng chung ────────────────────────── */
    function createTooltipEl(id) {
        const el = document.createElement('div');
        el.id = id;
        el.style.cssText = `
            position: fixed;
            background: #fff;
            border: 1px solid #E5E7EB;
            border-radius: 10px;
            padding: 8px 14px;
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
        const pct      = parseFloat(DATA.occupancy[period]) || 0;
        const dashArr  = (pct / 100) * CIRC;
        const arc      = document.getElementById('donutArc');
        const txtpct   = document.getElementById('donutpctText');
        const lblOcc   = document.getElementById('occpctOccupied');
        const lblAvail = document.getElementById('occpctAvailable');

        arc.setAttribute('stroke-dasharray', dashArr.toFixed(2) + ' ' + (CIRC - dashArr).toFixed(2));
        txtpct.textContent   = pct.toFixed(2) + '%';
        lblOcc.textContent   = pct.toFixed(2) + '%';
        lblAvail.textContent = (100 - pct).toFixed(2) + '%';
    }

    /* ── Peak Hour: type → background color ───────────────────── */
    /* SWAPPED: NORMAL ↔ NO_DATA                                   */
    const DAY_LABELS_VI = ['', 'T2', 'T3', 'T4', 'T5', 'T6', 'T7', 'CN'];

    function getTypeBgColor(type) {
        switch (type) {
            case 'PEAK':    return 'rgba(6,78,59,0.10)';
            case 'LOW':     return 'rgba(163,230,53,0.18)';
            case 'NORMAL':  return '#F8FAFC';           // ← đổi: lấy màu cũ của NO_DATA
            default:        return '#F3F4F6';           // NO_DATA ← đổi: lấy màu cũ của NORMAL
        }
    }

    /* ── Peak Hour: type → text color ─────────────────────────── */
    /* SWAPPED: NORMAL ↔ NO_DATA                                   */
    function getTypeTextColor(type) {
        switch (type) {
            case 'PEAK':    return '#064E3B';
            case 'LOW':     return '#A3E635';
            case 'NORMAL':  return '#111827';           // ← đổi: lấy màu cũ của NO_DATA
            default:        return '#9CA3AF';           // NO_DATA ← đổi: lấy màu cũ của NORMAL
        }
    }

    /* ── Peak Hour: type → border color (cho legend swatch) ────── */
    /* SWAPPED: NORMAL ↔ NO_DATA                                    */
    function getTypeBorderColor(type) {
        switch (type) {
            case 'PEAK':    return '#064E3B';
            case 'LOW':     return '#A3E635';
            case 'NORMAL':  return '#D1D5DB';           // ← đổi: lấy màu cũ của NO_DATA
            default:        return '#9CA3AF';           // NO_DATA ← đổi: lấy màu cũ của NORMAL
        }
    }

    /* ── Render 3 stat cards ───────────────────────────────────── */
    function renderPeakStats() {
        const peak   = PEAK_HOUR_DATA.peakSlots   || [];
        const low    = PEAK_HOUR_DATA.lowSlots    || [];
        const normal = PEAK_HOUR_DATA.normalTimeRange || 'Không xác định';

        const peakEl    = document.getElementById('peakSlotTime');
        const peakPctEl = document.getElementById('peakSlotPct');
        if (peakEl) {
            if (peak.length === 0) {
                peakEl.textContent    = '--';
                peakPctEl.textContent = 'Chưa có dữ liệu';
            } else if (peak.length === 1) {
                peakEl.textContent    = peak[0];
                peakPctEl.textContent = 'Khung giờ bận nhất';
            } else {
                peakEl.textContent    = peak[0] + ' – ' + peak[peak.length - 1];
                peakPctEl.textContent = peak.length + ' khung giờ cao điểm';
            }
        }

        const lowEl    = document.getElementById('lowSlotTime');
        const lowPctEl = document.getElementById('lowSlotPct');
        if (lowEl) {
            if (low.length === 0) {
                lowEl.textContent    = '--';
                lowPctEl.textContent = 'Chưa có dữ liệu';
            } else if (low.length === 1) {
                lowEl.textContent    = low[0];
                lowPctEl.textContent = 'Khung giờ ít khách nhất';
            } else {
                lowEl.textContent    = low[0] + ' – ' + low[low.length - 1];
                lowPctEl.textContent = low.length + ' khung giờ thấp điểm';
            }
        }

        const normalEl    = document.getElementById('normalSlotTime');
        const normalPctEl = document.getElementById('normalSlotPct');
        if (normalEl) {
            normalEl.textContent    = normal;
            normalPctEl.textContent = 'Nhu cầu đặt sân ổn định';
        }
    }

    /* ── Render legend động theo dữ liệu thực ─────────────────── */
    function renderPeakLegend(afterEl) {
        const peak = PEAK_HOUR_DATA.peakSlots  || [];
        const low  = PEAK_HOUR_DATA.lowSlots   || [];

        var items = [];
        if (peak.length > 0) {
            items.push({ type: 'PEAK',    label: 'Giờ cao điểm' });
        }
        if (low.length > 0) {
            items.push({ type: 'LOW',     label: 'Giờ thấp điểm' });
        }
        items.push({ type: 'NORMAL',  label: 'Giờ bình thường' });
        items.push({ type: 'NO_DATA', label: 'Chưa có lịch đặt' });

        var html = '<div class="dov-peak-legend">'
            + '<span class="dov-peak-legend-title">Chú thích:</span>';

        items.forEach(function (item) {
            var bg     = getTypeBgColor(item.type);
            var border = getTypeBorderColor(item.type);
            var color  = (item.type === 'NO_DATA') ? getTypeTextColor(item.type) : getTypeTextColor(item.type);

            html += '<div class="dov-peak-legend-item">'
                + '<div class="dov-peak-legend-swatch"'
                + '     style="background:' + bg + ';'
                + '            border:2px solid ' + border + ';'
                + '            border-radius:4px;"></div>'
                + '<span style="color:' + color + ';font-weight:700;">'
                + item.label
                + '</span>'
                + '</div>';
        });

        html += '</div>';
        afterEl.insertAdjacentHTML('afterend', html);
    }

    /* ── Render Heatmap ───────────────────────────────────────── */
    function renderPeakHeatmap() {
        const wrap    = document.getElementById('peakHeatmap');
        const heatmap = PEAK_HOUR_DATA.heatmap || [];

        if (!wrap) return;

        if (heatmap.length === 0) {
            wrap.innerHTML = '<p style="color:#9CA3AF;font-size:13px;padding:16px 0;">Không có dữ liệu</p>';
            return;
        }

        const map = {};
        heatmap.forEach(function (s) {
            map[s.dayOfWeek + '-' + s.slotTime] = {
                pct:   s.occupancyPct  || 0,
                count: s.bookingCount  || 0,
                type:  s.slotType      || 'NO_DATA'
            };
        });

        const times = [...new Set(heatmap.map(s => s.slotTime))].sort();

        let html = '<div class="dov-heatmap">';

        html += '<div class="dov-heatmap-row">';
        html += '<div class="dov-heatmap-cell dov-heatmap-time"></div>';
        for (var d = 1; d <= 7; d++) {
            html += '<div class="dov-heatmap-cell dov-heatmap-day">' + DAY_LABELS_VI[d] + '</div>';
        }
        html += '</div>';

        times.forEach(function (time) {
            html += '<div class="dov-heatmap-row">';
            html += '<div class="dov-heatmap-cell dov-heatmap-time">' + time + '</div>';

            for (var d = 1; d <= 7; d++) {
                var slot  = map[d + '-' + time] || { pct: 0, count: 0, type: 'NO_DATA' };
                var bg    = getTypeBgColor(slot.type);
                var color = getTypeTextColor(slot.type);

                html += '<div class="dov-heatmap-cell dov-heatmap-data"'
                      + ' style="background:' + bg + ';color:' + color + ';"'
                      + ' data-day="'   + DAY_LABELS_VI[d] + '"'
                      + ' data-time="'  + time       + '"'
                      + ' data-pct="'   + slot.pct   + '"'
                      + ' data-count="' + slot.count + '"'
                      + ' data-type="'  + slot.type  + '">'
                      + '<span class="dov-cell-time">' + time + '</span>'
                      + '</div>';
            }
            html += '</div>';
        });

        html += '</div>';
        wrap.innerHTML = html;

        attachHeatmapTooltip();
        renderPeakLegend(wrap);
    }

    /* ── Hover tooltip cho heatmap ─────────────────────────────── */
    function attachHeatmapTooltip() {
        const tooltip = document.getElementById('heatmapTooltip') || createHeatmapTooltipEl();

        document.querySelectorAll('.dov-heatmap-data').forEach(function (cell) {

            cell.addEventListener('mouseenter', function () {
                var type  = this.dataset.type;
                var day   = this.dataset.day;
                var time  = this.dataset.time;
                var count = parseInt(this.dataset.count) || 0;
                var color = getTypeTextColor(type);
                var bg    = getTypeBgColor(type);

                var typeLabel = {
                    'PEAK':    'Giờ cao điểm',
                    'LOW':     'Giờ thấp điểm',
                    'NORMAL':  'Giờ bình thường',
                    'NO_DATA': 'Chưa có lịch đặt'
                }[type] || '';

                tooltip.innerHTML =
                    '<div style="'
                    +   'font-size:11px;font-weight:700;'
                    +   'color:#6B7280;margin-bottom:8px;'
                    +   'padding-bottom:7px;border-bottom:1px solid #F3F4F6;">'
                    + day + ' &nbsp;·&nbsp; ' + time
                    + '</div>'
                    + '<div style="display:flex;align-items:center;gap:8px;margin-bottom:4px;">'
                    + '<div style="'
                    +   'width:12px;height:12px;border-radius:3px;flex-shrink:0;'
                    +   'background:' + bg + ';'
                    +   'border:2px solid ' + color + ';'
                    + '"></div>'
                    + '<span style="font-size:15px;font-weight:800;color:' + color + ';">'
                    + (count > 0
                        ? count.toLocaleString('vi-VN') + ' lượt booking'
                        : 'Chưa có booking')
                    + '</span>'
                    + '</div>'
                    + '<div style="font-size:10px;font-weight:600;color:#9CA3AF;">'
                    + typeLabel
                    + '</div>';

                tooltip.style.display = 'block';
            });

            cell.addEventListener('mousemove', function (e) {
                var tw = tooltip.offsetWidth;
                var th = tooltip.offsetHeight;
                var left = e.clientX + 14;
                if (left + tw > window.innerWidth - 16) {
                    left = e.clientX - tw - 14;
                }
                var top = e.clientY - th - 12;
                if (top < 8) { top = e.clientY + 16; }
                tooltip.style.left = left + 'px';
                tooltip.style.top  = top  + 'px';
            });

            cell.addEventListener('mouseleave', function () {
                tooltip.style.display = 'none';
            });
        });
    }

    /* ── Tạo heatmap tooltip element ───────────────────────────── */
    function createHeatmapTooltipEl() {
        const el = document.createElement('div');
        el.id = 'heatmapTooltip';
        el.style.cssText = `
            position: fixed;
            background: #fff;
            border: 1px solid #E5E7EB;
            border-radius: 10px;
            padding: 11px 14px;
            pointer-events: none;
            display: none;
            z-index: 9999;
            box-shadow: 0 4px 14px rgba(0,0,0,0.10);
            min-width: 148px;
        `;
        document.body.appendChild(el);
        return el;
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

        renderBookingStatus('Month');
        initTabGroup('#bookingTabs', period => renderBookingStatus(period));

        renderOccupancy('Month');
        initTabGroup('#occupancyTabs', period => renderOccupancy(period));

        charts.weekly = makeBarChart('weeklyChart', translateLabels(DATA.weekly['This Week']));
        document.querySelectorAll('[data-tab="weekly"]').forEach(btn => {
            btn.addEventListener('click', function () {
                document.querySelectorAll('[data-tab="weekly"]').forEach(b => b.classList.remove('is-active'));
                this.classList.add('is-active');
                updateChart(charts.weekly, translateLabels(DATA.weekly[this.dataset.period]));
            });
        });

        charts.yearly = makeBarChart('yearlyChart', translateLabels(DATA.yearly['This Year']));
        document.querySelectorAll('[data-tab="yearly"]').forEach(btn => {
            btn.addEventListener('click', function () {
                document.querySelectorAll('[data-tab="yearly"]').forEach(b => b.classList.remove('is-active'));
                this.classList.add('is-active');
                updateChart(charts.yearly, translateLabels(DATA.yearly[this.dataset.period]));
            });
        });

        charts.trend = makeAreaChart('trendChart', DATA.trend['Past 5 Years']);
        document.querySelectorAll('[data-tab="trend"]').forEach(btn => {
            btn.addEventListener('click', function () {
                document.querySelectorAll('[data-tab="trend"]').forEach(b => b.classList.remove('is-active'));
                this.classList.add('is-active');
                updateChart(charts.trend, DATA.trend[this.dataset.period]);
            });
        });

        renderPeakStats();
        renderPeakHeatmap();
    });

})();

/*(function () {
    'use strict';

     ── Design tokens ─────────────────────────────────────────── 
    const BRAND = '#064E3B';
    const LIME  = '#A3E635';
    const LIME2 = '#d9f99d';
    const G100  = '#F3F4F6';
    const G200  = '#E5E7EB';
    const G400  = '#9CA3AF';

    Chart.defaults.font.family = "'Inter', sans-serif";
    Chart.defaults.color = G400;

     ── Parse tất cả JSON một chỗ ────────────────────────────── 
    let BOOKING_STATUS_DATA, REVENUE_CHART_DATA, FACILITY_START_DATE,
        OCCUPANCY_DATA, PEAK_HOUR_DATA;

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

    try {
        OCCUPANCY_DATA = JSON.parse(document.getElementById('occupancyRaw').textContent);
    } catch (e) {
        console.error('occupancyRaw parse failed:', e);
        OCCUPANCY_DATA = { Day: 0, Week: 0, Month: 0, Year: 0 };
    }

    try {
        PEAK_HOUR_DATA = JSON.parse(document.getElementById('peakHourRaw').textContent);
    } catch (e) {
        console.error('peakHourRaw parse failed:', e);
        PEAK_HOUR_DATA = { heatmap: [], peakSlots: [], lowSlots: [], normalTimeRange: '' };
    }

     ── Dataset registry ──────────────────────────────────────── 
    const DATA = {
        booking:   BOOKING_STATUS_DATA,
        occupancy: OCCUPANCY_DATA,
        weekly:    REVENUE_CHART_DATA.weekly,
        yearly:    REVENUE_CHART_DATA.yearly,
        trend:     REVENUE_CHART_DATA.trend,
    };

     ── Map label tiếng Anh → tiếng Việt ─────────────────────── 
    const LABEL_MAP = {
        'Mon': 'Thứ 2', 'Tue': 'Thứ 3', 'Wed': 'Thứ 4',
        'Thu': 'Thứ 5', 'Fri': 'Thứ 6', 'Sat': 'Thứ 7', 'Sun': 'CN',
        'Jan': 'Tháng 1',  'Feb': 'Tháng 2',  'Mar': 'Tháng 3',
        'Apr': 'Tháng 4',  'May': 'Tháng 5',  'Jun': 'Tháng 6',
        'Jul': 'Tháng 7',  'Aug': 'Tháng 8',  'Sep': 'Tháng 9',
        'Oct': 'Tháng 10', 'Nov': 'Tháng 11', 'Dec': 'Tháng 12',
    };

    function translateLabels(dataset) {
        if (!dataset || !dataset.labels) return dataset;
        return {
            labels: dataset.labels.map(l => LABEL_MAP[l] || l),
            data:   dataset.data
        };
    }

     ── Format VND ────────────────────────────────────────────── 
    function formatVND(v) {
        if (v >= 1_000_000) {
            return ' ' + (v / 1_000_000).toLocaleString('vi-VN', { maximumFractionDigits: 1 }) + ' tr VND';
        }
        return ' ' + v.toLocaleString('vi-VN') + ' VND';
    }

     ── Shared tooltip ────────────────────────────────────────── 
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

     ── Tooltip riêng cho trend ───────────────────────────────── 
    const trendTooltip = {
        ...limeTooltip,
        callbacks: {
            title: ctx => {
                const label = ctx[0].label;
                if (FACILITY_START_DATE) {
                    const startYear  = FACILITY_START_DATE.getFullYear();
                    const startMonth = FACILITY_START_DATE.toLocaleString('en-US', { month: 'short' });
                    const startDay   = FACILITY_START_DATE.getDate();
                    if (/^\d{4}$/.test(label) && parseInt(label) === startYear) {
                        return label + ' (từ ngày ' + startDay + ' ' + startMonth + ' ' + startYear + ')';
                    }
                }
                return label;
            },
            label: ctx => formatVND(ctx.parsed.y)
        }
    };

     ── Build bar chart ───────────────────────────────────────── 
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
                    x: {
                        grid: { display: false },
                        border: { display: false },
                        ticks: { font: { size: 11, weight: '600' }, color: G400 }
                    },
                    y: {
                        grid: { color: G100 },
                        border: { display: false },
                        ticks: {
                            font: { size: 11 }, color: G400,
                            callback: v => v >= 1_000_000
                                ? (v / 1_000_000).toLocaleString('vi-VN', { maximumFractionDigits: 1 }) + 'tr'
                                : v.toLocaleString('vi-VN')
                        }
                    }
                }
            }
        });
    }

     ── Build area chart ──────────────────────────────────────── 
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
                    tooltip: trendTooltip
                },
                scales: {
                    x: {
                        grid: { display: false },
                        border: { display: false },
                        ticks: { font: { size: 11, weight: '600' }, color: G400 }
                    },
                    y: {
                        grid: { color: G100 },
                        border: { display: false },
                        ticks: {
                            font: { size: 11 }, color: G400,
                            callback: v => v >= 1_000_000
                                ? (v / 1_000_000).toLocaleString('vi-VN', { maximumFractionDigits: 1 }) + 'tr'
                                : v.toLocaleString('vi-VN')
                        }
                    }
                }
            }
        });
    }

     ── Update chart data in-place ────────────────────────────── 
    function updateChart(chart, dataset) {
        chart.data.labels = dataset.labels;
        chart.data.datasets[0].data = dataset.data;
        chart.update('active');
    }

     ── Booking Status bars + tooltip ────────────────────────── 
    function renderBookingStatus(period) {
        const rows = DATA.booking[period];
        const list = document.getElementById('bookingStatusList');
        list.innerHTML = '';

        if (!rows || rows.length === 0) {
            list.innerHTML = '<p style="color:#9CA3AF;font-size:13px;padding:16px 0;">Không có dữ liệu</p>';
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

     ── Custom tooltip hover thanh bar ────────────────────────── 
    function attachBarTooltips() {
        const tooltip = document.getElementById('statusTooltip') || createTooltipEl('statusTooltip');

        document.querySelectorAll('.dov-status-bar').forEach(bar => {
            bar.addEventListener('mouseenter', function () {
                tooltip.textContent   = this.dataset.label + ': '
                                      + Number(this.dataset.count).toLocaleString('vi-VN')
                                      + ' lượt đặt';
                tooltip.style.display = 'block';
            });
            bar.addEventListener('mousemove', function (e) {
                tooltip.style.left = (e.clientX + 12) + 'px';
                tooltip.style.top  = (e.clientY - 36) + 'px';
            });
            bar.addEventListener('mouseleave', function () {
                tooltip.style.display = 'none';
            });
        });
    }

     ── Tạo tooltip element dùng chung ────────────────────────── 
    function createTooltipEl(id) {
        const el = document.createElement('div');
        el.id = id;
        el.style.cssText = `
            position: fixed;
            background: #fff;
            border: 1px solid #E5E7EB;
            border-radius: 10px;
            padding: 8px 14px;
            pointer-events: none;
            display: none;
            z-index: 9999;
            box-shadow: 0 2px 8px rgba(0,0,0,0.08);
        `;
        document.body.appendChild(el);
        return el;
    }

     ── Occupancy donut ───────────────────────────────────────── 
    const CIRC = 2 * Math.PI * 68;

    function renderOccupancy(period) {
        const pct      = parseFloat(DATA.occupancy[period]) || 0;
        const dashArr  = (pct / 100) * CIRC;
        const arc      = document.getElementById('donutArc');
        const txtpct   = document.getElementById('donutpctText');
        const lblOcc   = document.getElementById('occpctOccupied');
        const lblAvail = document.getElementById('occpctAvailable');

        arc.setAttribute('stroke-dasharray', dashArr.toFixed(2) + ' ' + (CIRC - dashArr).toFixed(2));
        txtpct.textContent   = pct.toFixed(2) + '%';
        lblOcc.textContent   = pct.toFixed(2) + '%';
        lblAvail.textContent = (100 - pct).toFixed(2) + '%';
    }

     ── Peak Hour: type → background color ───────────────────── 
    const DAY_LABELS_VI = ['', 'T2', 'T3', 'T4', 'T5', 'T6', 'T7', 'CN'];

    function getTypeBgColor(type) {
        switch (type) {
            case 'PEAK':    return 'rgba(6,78,59,0.10)';
            case 'LOW':     return 'rgba(163,230,53,0.18)';
            case 'NORMAL':  return '#F3F4F6';
            default:        return '#F8FAFC';  // NO_DATA
        }
    }

     ── Peak Hour: type → text color ─────────────────────────── 
    function getTypeTextColor(type) {
        switch (type) {
            case 'PEAK':    return '#064E3B';
            case 'LOW':     return '#A3E635';
            case 'NORMAL':  return '#9CA3AF';
            default:        return '#111827';  // NO_DATA
        }
    }

     ── Peak Hour: type → border color (cho legend swatch) ────── 
    function getTypeBorderColor(type) {
        switch (type) {
            case 'PEAK':    return '#064E3B';
            case 'LOW':     return '#A3E635';
            case 'NORMAL':  return '#9CA3AF';
            default:        return '#D1D5DB';
        }
    }

     ── Render 3 stat cards ───────────────────────────────────── 
    function renderPeakStats() {
        const peak   = PEAK_HOUR_DATA.peakSlots   || [];
        const low    = PEAK_HOUR_DATA.lowSlots    || [];
        const normal = PEAK_HOUR_DATA.normalTimeRange || 'Không xác định';

        // ── Card Cao điểm ─────────────────────────────────────────
        const peakEl    = document.getElementById('peakSlotTime');
        const peakPctEl = document.getElementById('peakSlotPct');
        if (peakEl) {
            if (peak.length === 0) {
                peakEl.textContent    = '--';
                peakPctEl.textContent = 'Chưa có dữ liệu';
            } else if (peak.length === 1) {
                peakEl.textContent    = peak[0];
                peakPctEl.textContent = 'Khung giờ bận nhất';
            } else {
                peakEl.textContent    = peak[0] + ' – ' + peak[peak.length - 1];
                peakPctEl.textContent = peak.length + ' khung giờ cao điểm';
            }
        }

        // ── Card Thấp điểm ────────────────────────────────────────
        const lowEl    = document.getElementById('lowSlotTime');
        const lowPctEl = document.getElementById('lowSlotPct');
        if (lowEl) {
            if (low.length === 0) {
                lowEl.textContent    = '--';
                lowPctEl.textContent = 'Chưa có dữ liệu';
            } else if (low.length === 1) {
                lowEl.textContent    = low[0];
                lowPctEl.textContent = 'Khung giờ ít khách nhất';
            } else {
                lowEl.textContent    = low[0] + ' – ' + low[low.length - 1];
                lowPctEl.textContent = low.length + ' khung giờ thấp điểm';
            }
        }

        // ── Card Bình thường — không gắn giờ cụ thể ──────────────
        const normalEl    = document.getElementById('normalSlotTime');
        const normalPctEl = document.getElementById('normalSlotPct');
        if (normalEl) {
            normalEl.textContent    = normal;
            normalPctEl.textContent = 'Nhu cầu đặt sân ổn định';
        }
    }

     ── Render legend động theo dữ liệu thực ─────────────────── 
    function renderPeakLegend(afterEl) {
        const peak = PEAK_HOUR_DATA.peakSlots  || [];
        const low  = PEAK_HOUR_DATA.lowSlots   || [];

        // Luôn có NORMAL và NO_DATA; PEAK/LOW chỉ hiện nếu có dữ liệu
        var items = [];

        if (peak.length > 0) {
            items.push({ type: 'PEAK',    label: 'Giờ cao điểm' });
        }
        if (low.length > 0) {
            items.push({ type: 'LOW',     label: 'Giờ thấp điểm' });
        }
        items.push({ type: 'NORMAL',  label: 'Giờ bình thường' });
        items.push({ type: 'NO_DATA', label: 'Chưa có lịch đặt' });

        var html = '<div class="dov-peak-legend">'
            + '<span class="dov-peak-legend-title">Chú thích:</span>';

        items.forEach(function (item) {
            var bg     = getTypeBgColor(item.type);
            var border = getTypeBorderColor(item.type);
            var color  = (item.type === 'NO_DATA') ? '#6B7280' : getTypeTextColor(item.type);

            html += '<div class="dov-peak-legend-item">'
                + '<div class="dov-peak-legend-swatch"'
                + '     style="background:' + bg + ';'
                + '            border:2px solid ' + border + ';'
                + '            border-radius:4px;"></div>'
                + '<span style="color:' + color + ';font-weight:700;">'
                + item.label
                + '</span>'
                + '</div>';
        });

        html += '</div>';
        afterEl.insertAdjacentHTML('afterend', html);
    }

     ── Render Heatmap ───────────────────────────────────────── 
    function renderPeakHeatmap() {
        const wrap    = document.getElementById('peakHeatmap');
        const heatmap = PEAK_HOUR_DATA.heatmap || [];

        if (!wrap) return;

        if (heatmap.length === 0) {
            wrap.innerHTML = '<p style="color:#9CA3AF;font-size:13px;padding:16px 0;">Không có dữ liệu</p>';
            return;
        }

        // Build map: "day-time" → { pct, count, type }
        const map = {};
        heatmap.forEach(function (s) {
            map[s.dayOfWeek + '-' + s.slotTime] = {
                pct:   s.occupancyPct  || 0,
                count: s.bookingCount  || 0,
                type:  s.slotType      || 'NO_DATA'
            };
        });

        // Lấy danh sách giờ duy nhất, sort tăng dần
        const times = [...new Set(heatmap.map(s => s.slotTime))].sort();

        let html = '<div class="dov-heatmap">';

        // ── Header row (tên thứ) ──────────────────────────────────
        html += '<div class="dov-heatmap-row">';
        html += '<div class="dov-heatmap-cell dov-heatmap-time"></div>';
        for (var d = 1; d <= 7; d++) {
            html += '<div class="dov-heatmap-cell dov-heatmap-day">' + DAY_LABELS_VI[d] + '</div>';
        }
        html += '</div>';

        // ── Data rows ─────────────────────────────────────────────
        times.forEach(function (time) {
            html += '<div class="dov-heatmap-row">';
            // Cột giờ bên trái
            html += '<div class="dov-heatmap-cell dov-heatmap-time">' + time + '</div>';

            for (var d = 1; d <= 7; d++) {
                var slot  = map[d + '-' + time] || { pct: 0, count: 0, type: 'NO_DATA' };
                var bg    = getTypeBgColor(slot.type);
                var color = getTypeTextColor(slot.type);

                html += '<div class="dov-heatmap-cell dov-heatmap-data"'
                      + ' style="background:' + bg + ';color:' + color + ';"'
                      + ' data-day="'   + DAY_LABELS_VI[d] + '"'
                      + ' data-time="'  + time       + '"'
                      + ' data-pct="'   + slot.pct   + '"'
                      + ' data-count="' + slot.count + '"'
                      + ' data-type="'  + slot.type  + '">'
                      + '<span class="dov-cell-time">' + time + '</span>'
                      + '</div>';
            }
            html += '</div>';
        });

        html += '</div>';
        wrap.innerHTML = html;

        attachHeatmapTooltip();
        renderPeakLegend(wrap);
    }

     ── Hover tooltip cho heatmap ─────────────────────────────── 
    function attachHeatmapTooltip() {
        const tooltip = document.getElementById('heatmapTooltip') || createHeatmapTooltipEl();

        document.querySelectorAll('.dov-heatmap-data').forEach(function (cell) {

            cell.addEventListener('mouseenter', function () {
                var type  = this.dataset.type;
                var day   = this.dataset.day;
                var time  = this.dataset.time;
                var count = parseInt(this.dataset.count) || 0;
                var color = getTypeTextColor(type);
                var bg    = getTypeBgColor(type);

                // ── Nhãn loại giờ ──────────────────────────────────
                var typeLabel = {
                    'PEAK':    'Giờ cao điểm',
                    'LOW':     'Giờ thấp điểm',
                    'NORMAL':  'Giờ bình thường',
                    'NO_DATA': 'Chưa có lịch đặt'
                }[type] || '';

                // ── Nội dung tooltip ───────────────────────────────
                tooltip.innerHTML =
                    // Hàng 1: Thứ · Giờ
                    '<div style="'
                    +   'font-size:11px;font-weight:700;'
                    +   'color:#6B7280;margin-bottom:8px;'
                    +   'padding-bottom:7px;border-bottom:1px solid #F3F4F6;">'
                    + day + ' &nbsp;·&nbsp; ' + time
                    + '</div>'

                    // Hàng 2: Swatch + số lượt booking
                    + '<div style="display:flex;align-items:center;gap:8px;margin-bottom:4px;">'
                    + '<div style="'
                    +   'width:12px;height:12px;border-radius:3px;flex-shrink:0;'
                    +   'background:' + bg + ';'
                    +   'border:2px solid ' + color + ';'
                    + '"></div>'
                    + '<span style="font-size:15px;font-weight:800;color:' + color + ';">'
                    + (count > 0
                        ? count.toLocaleString('vi-VN') + ' lượt booking'
                        : 'Chưa có booking')
                    + '</span>'
                    + '</div>'

                    // Hàng 3: Loại giờ
                    + '<div style="font-size:10px;font-weight:600;color:#9CA3AF;">'
                    + typeLabel
                    + '</div>';

                tooltip.style.display = 'block';
            });

            cell.addEventListener('mousemove', function (e) {
                var tw = tooltip.offsetWidth;
                var th = tooltip.offsetHeight;

                // Dùng clientX/Y vì tooltip là position:fixed
                var left = e.clientX + 14;
                if (left + tw > window.innerWidth - 16) {
                    left = e.clientX - tw - 14;
                }
                var top = e.clientY - th - 12;
                if (top < 8) { top = e.clientY + 16; }

                tooltip.style.left = left + 'px';
                tooltip.style.top  = top  + 'px';
            });

            cell.addEventListener('mouseleave', function () {
                tooltip.style.display = 'none';
            });
        });
    }

     ── Tạo heatmap tooltip element ───────────────────────────── 
    function createHeatmapTooltipEl() {
        const el = document.createElement('div');
        el.id = 'heatmapTooltip';
        el.style.cssText = `
            position: fixed;
            background: #fff;
            border: 1px solid #E5E7EB;
            border-radius: 10px;
            padding: 11px 14px;
            pointer-events: none;
            display: none;
            z-index: 9999;
            box-shadow: 0 4px 14px rgba(0,0,0,0.10);
            min-width: 148px;
        `;
        document.body.appendChild(el);
        return el;
    }

     ── Tab click handler ─────────────────────────────────────── 
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

     ── Init ──────────────────────────────────────────────────── 
    document.addEventListener('DOMContentLoaded', function () {

         Booking status 
        renderBookingStatus('Month');
        initTabGroup('#bookingTabs', period => renderBookingStatus(period));

         Occupancy 
        renderOccupancy('Month');
        initTabGroup('#occupancyTabs', period => renderOccupancy(period));

         Weekly bar 
        charts.weekly = makeBarChart('weeklyChart', translateLabels(DATA.weekly['This Week']));
        document.querySelectorAll('[data-tab="weekly"]').forEach(btn => {
            btn.addEventListener('click', function () {
                document.querySelectorAll('[data-tab="weekly"]').forEach(b => b.classList.remove('is-active'));
                this.classList.add('is-active');
                updateChart(charts.weekly, translateLabels(DATA.weekly[this.dataset.period]));
            });
        });

         Yearly bar 
        charts.yearly = makeBarChart('yearlyChart', translateLabels(DATA.yearly['This Year']));
        document.querySelectorAll('[data-tab="yearly"]').forEach(btn => {
            btn.addEventListener('click', function () {
                document.querySelectorAll('[data-tab="yearly"]').forEach(b => b.classList.remove('is-active'));
                this.classList.add('is-active');
                updateChart(charts.yearly, translateLabels(DATA.yearly[this.dataset.period]));
            });
        });

         Revenue trend area 
        charts.trend = makeAreaChart('trendChart', DATA.trend['Past 5 Years']);
        document.querySelectorAll('[data-tab="trend"]').forEach(btn => {
            btn.addEventListener('click', function () {
                document.querySelectorAll('[data-tab="trend"]').forEach(b => b.classList.remove('is-active'));
                this.classList.add('is-active');
                updateChart(charts.trend, DATA.trend[this.dataset.period]);
            });
        });

         Peak Hour 
        renderPeakStats();
        renderPeakHeatmap();
    });

})();*/