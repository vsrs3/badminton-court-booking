(function () {
    'use strict';

    const root = document.getElementById('ownerDashboardRoot');
    if (!root) {
        return;
    }

    const CONTEXT_PATH = root.dataset.contextPath || '';
    const BRAND = '#064E3B';
    const LIME = '#A3E635';
    const LIME_LIGHT = '#D9F99D';
    const GRAY_100 = '#F3F4F6';
    const GRAY_200 = '#E5E7EB';
    const GRAY_500 = '#6B7280';
    const GRAY_700 = '#374151';
    const DAY_LABELS = ['', 'T2', 'T3', 'T4', 'T5', 'T6', 'T7', 'CN'];

    const labelMap = {
        Mon: 'Thứ 2',
        Tue: 'Thứ 3',
        Wed: 'Thứ 4',
        Thu: 'Thứ 5',
        Fri: 'Thứ 6',
        Sat: 'Thứ 7',
        Sun: 'CN',
        Jan: 'Tháng 1',
        Feb: 'Tháng 2',
        Mar: 'Tháng 3',
        Apr: 'Tháng 4',
        May: 'Tháng 5',
        Jun: 'Tháng 6',
        Jul: 'Tháng 7',
        Aug: 'Tháng 8',
        Sep: 'Tháng 9',
        Oct: 'Tháng 10',
        Nov: 'Tháng 11',
        Dec: 'Tháng 12'
    };

    const currencyFormatter = new Intl.NumberFormat('vi-VN', {
        style: 'currency',
        currency: 'VND',
        maximumFractionDigits: 0
    });

    const numberFormatter = new Intl.NumberFormat('vi-VN');

    if (typeof Chart !== 'undefined') {
        Chart.defaults.font.family = "'Inter', sans-serif";
        Chart.defaults.color = GRAY_500;
    }

    const charts = {};

    const bookingStatusData = parseJsonScript('bookingStatusRaw', {
        Day: [],
        Week: [],
        Month: [],
        Year: []
    });
    const revenueChartData = parseJsonScript('revenueChartRaw', {
        weekly: {},
        yearly: {},
        trend: {}
    });
    const occupancyData = parseJsonScript('occupancyRaw', {
        Day: 0,
        Week: 0,
        Month: 0,
        Year: 0
    });
    const peakHourData = parseJsonScript('peakHourRaw', {
        heatmap: [],
        peakSlots: [],
        lowSlots: [],
        normalTimeRange: ''
    });

    const rentalState = {
        facilities: [],
        suggestionItems: [],
        facilityId: null,
        facilityName: '',
        year: null,
        month: 1,
        day: 1,
        inactiveMonth: 1,
        detailScope: 'month',
        hourKey: null,
        detailRows: [],
        detailCurrentPage: 1,
        detailPageSize: 5
    };

    const rentalEls = {
        searchInput: document.getElementById('rentalFacilitySearch'),
        searchButton: document.getElementById('rentalFacilitySearchBtn'),
        searchHiddenId: document.getElementById('rentalFacilityId'),
        suggestions: document.getElementById('rentalFacilitySuggestions'),
        topItems: document.getElementById('rentalTopItems'),
        inactiveMonthSelect: document.getElementById('rentalInactiveMonthSelect'),
        inactiveList: document.getElementById('rentalInactiveList'),
        deactivateButton: document.getElementById('rentalDeactivateBtn'),
        detailTableBody: document.getElementById('rentalDetailTableBody'),
        detailPagination: document.getElementById('rentalDetailPagination')
    };

    document.addEventListener('DOMContentLoaded', function () {
        initReportSwitch();
        initCourtReport();
        initRentalReport();
    });

    function parseJsonScript(id, fallbackValue) {
        const node = document.getElementById(id);
        if (!node) {
            return fallbackValue;
        }

        try {
            return JSON.parse(node.textContent);
        } catch (error) {
            console.error('JSON parse failed:', id, error);
            return fallbackValue;
        }
    }

    function initReportSwitch() {
        const buttons = Array.from(document.querySelectorAll('.owner-report-tab'));
        buttons.forEach(function (button) {
            button.addEventListener('click', function () {
                const targetId = this.dataset.reportTarget;
                if (!targetId) {
                    return;
                }

                buttons.forEach(function (item) {
                    item.classList.toggle('is-active', item === button);
                });

                document.querySelectorAll('.owner-report-panel').forEach(function (panel) {
                    panel.classList.toggle('is-active', panel.id === targetId);
                });

                if (targetId === 'rental-report-panel') {
                    resizeRentalCharts();
                }
            });
        });
    }

    function initCourtReport() {
        const weeklyDatasets = revenueChartData && revenueChartData.weekly ? revenueChartData.weekly : {};
        const yearlyDatasets = revenueChartData && revenueChartData.yearly ? revenueChartData.yearly : {};
        const trendDatasets = revenueChartData && revenueChartData.trend ? revenueChartData.trend : {};

        renderBookingStatus('Month');
        renderOccupancy('Month');
        initTabGroup('#bookingTabs', function (period) {
            renderBookingStatus(period);
        });
        initTabGroup('#occupancyTabs', function (period) {
            renderOccupancy(period);
        });

        renderCourtBarChart('weekly', 'weeklyChart', translateChartDataset(weeklyDatasets['This Week']));
        bindChartTabs('[data-tab="weekly"]', function (period) {
            updateCourtBarChart(charts.weekly, translateChartDataset(weeklyDatasets[period]));
        });

        renderCourtBarChart('yearly', 'yearlyChart', translateChartDataset(yearlyDatasets['This Year']));
        bindChartTabs('[data-tab="yearly"]', function (period) {
            updateCourtBarChart(charts.yearly, translateChartDataset(yearlyDatasets[period]));
        });

        if (document.getElementById('trendChart')) {
            renderTrendChart('trendChart', trendDatasets['Past 5 Years']);
            bindChartTabs('[data-tab="trend"]', function (period) {
                updateTrendChart(charts.trend, trendDatasets[period]);
            });
        }

        renderPeakStats();
        renderPeakHeatmap();
    }

    function renderBookingStatus(period) {
        const container = document.getElementById('bookingStatusList');
        if (!container) {
            return;
        }

        const items = Array.isArray(bookingStatusData[period]) ? bookingStatusData[period] : [];
        if (!items.length) {
            container.innerHTML = '<p class="text-muted mb-0">Không có dữ liệu.</p>';
            return;
        }

        container.innerHTML = items.map(function (item) {
            const percent = Number(item.pct || 0).toFixed(1).replace(/\.0$/, '');
            return '' +
                '<div class="dov-status-row">' +
                    '<span class="dov-status-name">' + escapeHtml(item.label || '') + '</span>' +
                    '<div class="dov-status-track">' +
                        '<div class="dov-status-bar"' +
                             ' style="width:' + Number(item.pct || 0) + '%;background:' + (item.color || BRAND) + ';"' +
                             ' data-label="' + escapeHtml(item.label || '') + '"' +
                             ' data-count="' + Number(item.count || 0) + '">' +
                        '</div>' +
                    '</div>' +
                    '<span class="dov-status-pct">' + percent + '%</span>' +
                '</div>';
        }).join('');

        attachSimpleTooltip('.dov-status-bar', 'statusTooltip', function (element) {
            return escapeHtml(element.dataset.label || '') + ': ' +
                numberFormatter.format(Number(element.dataset.count || 0)) + ' lượt đặt';
        });
    }

    function renderOccupancy(period) {
        const percent = Number(occupancyData[period] || 0);
        const occupied = percent.toFixed(2) + '%';
        const available = (100 - percent).toFixed(2) + '%';
        const circumference = 2 * Math.PI * 68;
        const dashLength = (percent / 100) * circumference;

        const arc = document.getElementById('donutArc');
        if (arc) {
            arc.setAttribute(
                'stroke-dasharray',
                dashLength.toFixed(2) + ' ' + (circumference - dashLength).toFixed(2)
            );
        }

        setText('donutpctText', occupied);
        setText('occpctOccupied', occupied);
        setText('occpctAvailable', available);
    }

    function initTabGroup(selector, onClick) {
        document.querySelectorAll(selector + ' .dov-tab').forEach(function (button) {
            button.addEventListener('click', function () {
                document.querySelectorAll(selector + ' .dov-tab').forEach(function (item) {
                    item.classList.remove('is-active');
                });
                this.classList.add('is-active');
                onClick(this.dataset.period);
            });
        });
    }

    function bindChartTabs(selector, onSelect) {
        document.querySelectorAll(selector).forEach(function (button) {
            button.addEventListener('click', function () {
                const tab = this.dataset.tab;
                document.querySelectorAll('[data-tab="' + tab + '"]').forEach(function (item) {
                    item.classList.remove('is-active');
                });
                this.classList.add('is-active');
                onSelect(this.dataset.period);
            });
        });
    }

    function translateChartDataset(dataset) {
        if (!dataset) {
            return { labels: [], data: [] };
        }

        return {
            labels: (dataset.labels || []).map(function (label) {
                return labelMap[label] || label;
            }),
            data: dataset.data || []
        };
    }

    function renderCourtBarChart(key, canvasId, dataset) {
        const canvas = document.getElementById(canvasId);
        if (!canvas || typeof Chart === 'undefined') {
            return;
        }

        destroyChart(key);
        charts[key] = new Chart(canvas.getContext('2d'), {
            type: 'bar',
            data: {
                labels: dataset.labels || [],
                datasets: [{
                    data: dataset.data || [],
                    backgroundColor: LIME,
                    hoverBackgroundColor: LIME_LIGHT,
                    borderRadius: 8,
                    borderSkipped: false
                }]
            },
            options: buildBarChartOptions()
        });
    }

    function updateCourtBarChart(chart, dataset) {
        if (!chart || !dataset) {
            return;
        }

        chart.data.labels = dataset.labels || [];
        chart.data.datasets[0].data = dataset.data || [];
        chart.update();
    }

    function renderTrendChart(canvasId, dataset) {
        const canvas = document.getElementById(canvasId);
        if (!canvas || typeof Chart === 'undefined') {
            return;
        }

        const ctx = canvas.getContext('2d');
        const gradient = ctx.createLinearGradient(0, 0, 0, 260);
        gradient.addColorStop(0, 'rgba(163,230,53,0.4)');
        gradient.addColorStop(1, 'rgba(163,230,53,0.04)');

        destroyChart('trend');
        charts.trend = new Chart(ctx, {
            type: 'line',
            data: {
                labels: dataset && dataset.labels ? dataset.labels : [],
                datasets: [{
                    data: dataset && dataset.data ? dataset.data : [],
                    borderColor: BRAND,
                    backgroundColor: gradient,
                    borderWidth: 3,
                    fill: true,
                    pointRadius: 0,
                    pointHoverRadius: 5,
                    pointHoverBackgroundColor: BRAND,
                    pointHoverBorderColor: '#fff',
                    pointHoverBorderWidth: 2,
                    tension: 0.35
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                interaction: {
                    mode: 'index',
                    intersect: false
                },
                plugins: {
                    legend: { display: false },
                    tooltip: buildRevenueTooltip()
                },
                scales: buildRevenueScales()
            }
        });
    }

    function updateTrendChart(chart, dataset) {
        if (!chart) {
            return;
        }

        chart.data.labels = dataset && dataset.labels ? dataset.labels : [];
        chart.data.datasets[0].data = dataset && dataset.data ? dataset.data : [];
        chart.update();
    }

    function buildBarChartOptions(onClick) {
        const options = {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: { display: false },
                tooltip: buildRevenueTooltip()
            },
            scales: buildRevenueScales()
        };

        if (typeof onClick === 'function') {
            options.onClick = onClick;
        }

        return options;
    }

    function buildRevenueScales() {
        return {
            x: {
                grid: { display: false },
                border: { display: false },
                ticks: {
                    color: GRAY_500,
                    font: {
                        size: 11,
                        weight: '600'
                    }
                }
            },
            y: {
                grid: { color: GRAY_100 },
                border: { display: false },
                ticks: {
                    color: GRAY_500,
                    callback: function (value) {
                        return formatCompactCurrency(Number(value || 0));
                    }
                }
            }
        };
    }

    function buildRevenueTooltip() {
        return {
            backgroundColor: '#fff',
            titleColor: GRAY_700,
            bodyColor: BRAND,
            borderColor: GRAY_200,
            borderWidth: 1,
            padding: 12,
            cornerRadius: 12,
            titleFont: {
                size: 11,
                weight: '700'
            },
            bodyFont: {
                size: 13,
                weight: '800'
            },
            callbacks: {
                label: function (context) {
                    return currencyFormatter.format(Number(context.parsed.y || 0));
                }
            }
        };
    }

    function renderPeakStats() {
        const peakSlots = Array.isArray(peakHourData.peakSlots) ? peakHourData.peakSlots : [];
        const lowSlots = Array.isArray(peakHourData.lowSlots) ? peakHourData.lowSlots : [];
        const peakText = peakSlots.length ? peakSlots.join(', ') : '--';
        const lowText = lowSlots.length ? lowSlots.join(', ') : '--';
        const normalText = peakHourData.normalTimeRange || '--';

        setText('peakSlotTime', peakText);
        setText(
            'peakSlotPct',
            peakSlots.length ? peakSlots.length + ' khung giờ có lượt đặt cao nhất' : 'Chưa có dữ liệu'
        );
        setText('normalSlotTime', normalText);
        setText(
            'normalSlotPct',
            normalText !== '--' ? 'Khoảng giờ vận hành ổn định' : 'Chưa có dữ liệu'
        );
        setText('lowSlotTime', lowText);
        setText('lowSlotPct', lowSlots.length ? lowSlots.length + ' khung giờ thấp điểm' : 'Chưa có dữ liệu');
    }

    function renderPeakHeatmap() {
        const container = document.getElementById('peakHeatmap');
        if (!container) {
            return;
        }

        const heatmapItems = Array.isArray(peakHourData.heatmap) ? peakHourData.heatmap : [];
        if (!heatmapItems.length) {
            container.innerHTML = '<div class="owner-rental-empty-state">Không có dữ liệu heatmap.</div>';
            return;
        }

        const slotTimes = Array.from(new Set(heatmapItems.map(function (item) {
            return item.slotTime || '';
        }))).filter(Boolean).sort();

        const itemMap = new Map();
        heatmapItems.forEach(function (item) {
            itemMap.set((item.slotTime || '') + '|' + Number(item.dayOfWeek || 0), item);
        });

        const headerCells = DAY_LABELS.slice(1).map(function (label) {
            return '<div class="dov-heatmap-cell dov-heatmap-day">' + label + '</div>';
        }).join('');

        const rows = slotTimes.map(function (slotTime) {
            const cells = DAY_LABELS.slice(1).map(function (_, dayIndex) {
                const dayOfWeek = dayIndex + 1;
                const item = itemMap.get(slotTime + '|' + dayOfWeek);
                const occupancyPct = Number(item && item.occupancyPct ? item.occupancyPct : 0);
                const bookingCount = Number(item && item.bookingCount ? item.bookingCount : 0);
                const slotType = item && item.slotType ? item.slotType : 'NO_DATA';
                const tooltipHtml = escapeHtml((DAY_LABELS[dayOfWeek] || '') + ' - ' + slotTime) +
                    '<br>' + escapeHtml('Tỷ lệ lấp đầy: ' + occupancyPct.toFixed(1) + '%') +
                    '<br>' + escapeHtml('Số lượt đặt: ' + numberFormatter.format(bookingCount));

                return '' +
                    '<div class="dov-heatmap-cell dov-heatmap-data"' +
                        ' style="background:' + getHeatmapColor(slotType, occupancyPct) + ';"' +
                        ' data-tooltip-html="' + tooltipHtml + '">' +
                    '</div>';
            }).join('');

            return '' +
                '<div class="dov-heatmap-row">' +
                    '<div class="dov-heatmap-cell dov-heatmap-time">' + escapeHtml(slotTime) + '</div>' +
                    cells +
                '</div>';
        }).join('');

        container.innerHTML = '' +
            '<div class="dov-heatmap">' +
                '<div class="dov-heatmap-row">' +
                    '<div class="dov-heatmap-cell dov-heatmap-time"></div>' +
                    headerCells +
                '</div>' +
                rows +
            '</div>' +
            '<div class="dov-peak-legend">' +
                '<span class="dov-peak-legend-title">Chú thích</span>' +
                '<span class="dov-peak-legend-item"><span class="dov-peak-legend-swatch" style="background:#064E3B;"></span>Cao điểm</span>' +
                '<span class="dov-peak-legend-item"><span class="dov-peak-legend-swatch" style="background:#9CA3AF;"></span>Bình thường</span>' +
                '<span class="dov-peak-legend-item"><span class="dov-peak-legend-swatch" style="background:#A3E635;"></span>Thấp điểm</span>' +
                '<span class="dov-peak-legend-item"><span class="dov-peak-legend-swatch" style="background:#E5E7EB;"></span>Không có dữ liệu</span>' +
            '</div>';

        attachSimpleTooltip('.dov-heatmap-data', 'heatmapTooltip', function (element) {
            return element.dataset.tooltipHtml || '';
        });
    }

    function getHeatmapColor(slotType, occupancyPct) {
        const opacity = Math.min(Math.max(occupancyPct / 100, 0.22), 1);
        switch ((slotType || '').toUpperCase()) {
            case 'PEAK':
                return 'rgba(6,78,59,' + opacity.toFixed(2) + ')';
            case 'LOW':
                return 'rgba(163,230,53,' + opacity.toFixed(2) + ')';
            case 'NORMAL':
                return 'rgba(107,114,128,' + opacity.toFixed(2) + ')';
            default:
                return '#E5E7EB';
        }
    }

    function initRentalReport() {
        if (!rentalEls.searchInput) {
            return;
        }

        const debouncedSuggest = debounce(function () {
            loadFacilitySuggestions(rentalEls.searchInput.value.trim(), true);
        }, 220);

        rentalEls.searchInput.addEventListener('input', function () {
            if (rentalEls.searchHiddenId) {
                rentalEls.searchHiddenId.value = '';
            }
            debouncedSuggest();
        });

        rentalEls.searchInput.addEventListener('focus', function () {
            if (rentalState.suggestionItems && rentalState.suggestionItems.length) {
                renderFacilitySuggestions(rentalState.suggestionItems, true);
                return;
            }
            loadFacilitySuggestions(rentalEls.searchInput.value.trim(), true);
        });

        rentalEls.searchInput.addEventListener('keydown', function (event) {
            if (event.key === 'Enter') {
                event.preventDefault();
                executeRentalSearch();
            }
        });

        if (rentalEls.searchButton) {
            rentalEls.searchButton.addEventListener('click', function () {
                executeRentalSearch();
            });
        }

        if (rentalEls.inactiveMonthSelect) {
            rentalEls.inactiveMonthSelect.addEventListener('change', function () {
                rentalState.inactiveMonth = Number(this.value || 1);
                loadRentalSummary();
            });
        }

        if (rentalEls.deactivateButton) {
            rentalEls.deactivateButton.addEventListener('click', function () {
                deactivateInactiveItems();
            });
        }

        if (rentalEls.detailPagination) {
            rentalEls.detailPagination.addEventListener('click', function (event) {
                const link = event.target.closest('a[data-page]');
                if (!link) {
                    return;
                }

                event.preventDefault();
                const targetPage = Number(link.dataset.page || 0);
                const totalPages = getDetailTotalPages();
                if (targetPage < 1 || targetPage > totalPages || targetPage === rentalState.detailCurrentPage) {
                    return;
                }

                rentalState.detailCurrentPage = targetPage;
                renderDetailTablePage();
            });
        }

        document.addEventListener('click', function (event) {
            const wrapper = rentalEls.suggestions
                ? rentalEls.suggestions.closest('.owner-rental-search-input-wrap')
                : null;
            if (wrapper && !wrapper.contains(event.target)) {
                closeFacilitySuggestions();
            }
        });

        window.addEventListener('resize', debounce(function () {
            resizeRentalCharts();
        }, 120));

        initializeRentalDefaults();
    }

    async function initializeRentalDefaults() {
        setText('rentalFacilityMeta', '');
        renderTopItems([]);
        renderInactiveItems([]);
        renderDetailRows({ title: 'Chi tiết doanh thu', rows: [] });

        try {
            const facilities = await loadFacilitySuggestions('', false);
            if (!facilities.length) {
                renderRentalUnavailable('Không tìm thấy địa điểm hoạt động để lập báo cáo thuê đồ.');
                return;
            }

            applyFacilitySelection(facilities[0]);
            rentalState.year = null;
            rentalState.month = 1;
            rentalState.day = 1;
            rentalState.inactiveMonth = 1;
            rentalState.detailScope = 'month';
            rentalState.hourKey = null;
            if (rentalEls.inactiveMonthSelect) {
                rentalEls.inactiveMonthSelect.value = '1';
            }
            await loadRentalSummary();
        } catch (error) {
            console.error('Failed to initialize rental report', error);
            renderRentalUnavailable(error.message || 'Không tải được báo cáo thuê đồ.');
        }
    }

    async function loadFacilitySuggestions(keyword, shouldRender) {
        const query = typeof keyword === 'string' ? keyword.trim() : '';
        rentalState.searchSequence = Number(rentalState.searchSequence || 0) + 1;
        const currentSequence = rentalState.searchSequence;

        const response = await requestJson(
            buildApiUrl('/api/owner/rental-report/facilities', query ? { q: query } : null)
        );
        const items = Array.isArray(response.data) ? response.data : [];
        if (currentSequence !== rentalState.searchSequence) {
            return rentalState.suggestionItems || [];
        }

        rentalState.suggestionItems = items;
        mergeFacilityCache(items);

        if (shouldRender) {
            renderFacilitySuggestions(items, true);
        }

        return items;
    }

    function renderFacilitySuggestions(items, visible) {
        if (!rentalEls.suggestions) {
            return;
        }

        if (!visible) {
            rentalEls.suggestions.innerHTML = '';
            rentalEls.suggestions.classList.remove('is-visible');
            return;
        }

        if (!items || !items.length) {
            rentalEls.suggestions.innerHTML =
                '<div class="owner-rental-suggestion-empty">Không tìm thấy địa điểm phù hợp.</div>';
            rentalEls.suggestions.classList.add('is-visible');
            return;
        }

        rentalEls.suggestions.innerHTML = items.map(function (item) {
            return '' +
                '<button type="button" class="owner-rental-suggestion-item" data-facility-id="' + Number(item.facilityId) + '">' +
                    '<span class="owner-rental-suggestion-name">' + escapeHtml(item.name || '') + '</span>' +
                    '<span class="owner-rental-suggestion-address">' + escapeHtml(item.address || '') + '</span>' +
                '</button>';
        }).join('');

        rentalEls.suggestions.classList.add('is-visible');
        rentalEls.suggestions.querySelectorAll('.owner-rental-suggestion-item').forEach(function (button) {
            button.addEventListener('click', function () {
                const facility = findFacilityById(Number(this.dataset.facilityId));
                if (!facility) {
                    return;
                }
                setPendingFacilitySelection(facility);
                closeFacilitySuggestions();
            });
        });
    }

    function closeFacilitySuggestions() {
        if (!rentalEls.suggestions) {
            return;
        }
        rentalEls.suggestions.classList.remove('is-visible');
    }

    function mergeFacilityCache(items) {
        if (!items || !items.length) {
            return;
        }

        const map = new Map();
        (rentalState.facilities || []).forEach(function (item) {
            map.set(Number(item.facilityId), item);
        });
        items.forEach(function (item) {
            map.set(Number(item.facilityId), item);
        });

        rentalState.facilities = Array.from(map.values()).sort(function (left, right) {
            return Number(left.facilityId) - Number(right.facilityId);
        });
    }

    function setPendingFacilitySelection(facility) {
        if (!facility) {
            return;
        }

        if (rentalEls.searchInput) {
            rentalEls.searchInput.value = facility.name || '';
        }
        if (rentalEls.searchHiddenId) {
            rentalEls.searchHiddenId.value = String(Number(facility.facilityId));
        }
    }

    function applyFacilitySelection(facility) {
        if (!facility) {
            return;
        }

        rentalState.facilityId = Number(facility.facilityId);
        rentalState.facilityName = facility.name || '';
        rentalState.facilityAddress = facility.address || '';
        setPendingFacilitySelection(facility);
    }

    function findFacilityById(facilityId) {
        return (rentalState.facilities || []).find(function (item) {
            return Number(item.facilityId) === Number(facilityId);
        }) || null;
    }

    async function executeRentalSearch() {
        try {
            const facility = await resolveSearchedFacility();
            if (!facility) {
                window.alert('Không tìm thấy địa điểm phù hợp với từ khóa đã nhập.');
                return;
            }

            applyFacilitySelection(facility);
            rentalState.year = null;
            rentalState.month = 1;
            rentalState.day = 1;
            rentalState.inactiveMonth = 1;
            rentalState.detailScope = 'month';
            rentalState.hourKey = null;
            if (rentalEls.inactiveMonthSelect) {
                rentalEls.inactiveMonthSelect.value = '1';
            }
            await loadRentalSummary();
            closeFacilitySuggestions();
        } catch (error) {
            console.error('Rental search failed', error);
            window.alert(error.message || 'Không thể tìm kiếm địa điểm.');
        }
    }

    async function resolveSearchedFacility() {
        if (rentalEls.searchHiddenId && rentalEls.searchHiddenId.value) {
            return findFacilityById(Number(rentalEls.searchHiddenId.value));
        }

        const keyword = rentalEls.searchInput ? rentalEls.searchInput.value.trim() : '';
        if (!keyword) {
            return findFacilityById(rentalState.facilityId) || (rentalState.facilities || [])[0] || null;
        }

        const exactMatch = (rentalState.facilities || []).find(function (item) {
            return (item.name || '').trim().toLowerCase() === keyword.toLowerCase();
        });
        if (exactMatch) {
            return exactMatch;
        }

        const remoteItems = await loadFacilitySuggestions(keyword, true);
        return remoteItems.length ? remoteItems[0] : null;
    }

    async function loadRentalSummary() {
        if (!rentalState.facilityId) {
            return;
        }

        rentalState.summarySequence = Number(rentalState.summarySequence || 0) + 1;
        const currentSequence = rentalState.summarySequence;
        const shouldRestoreHourDetail = rentalState.detailScope === 'hour' && !!rentalState.hourKey;

        setRentalLoadingState();

        try {
            const params = {
                facilityId: rentalState.facilityId,
                month: rentalState.month,
                day: rentalState.day,
                inactiveMonth: rentalState.inactiveMonth,
                detailScope: shouldRestoreHourDetail
                    ? 'day'
                    : (rentalState.detailScope === 'day' ? 'day' : 'month')
            };
            if (rentalState.year) {
                params.year = rentalState.year;
            }

            const response = await requestJson(buildApiUrl('/api/owner/rental-report/summary', params));
            if (currentSequence !== rentalState.summarySequence) {
                return;
            }

            const summary = response.data || {};
            hydrateRentalState(summary);
            renderRentalSummary(summary);

            if (shouldRestoreHourDetail && rentalState.hourKey) {
                await loadHourlyDetail();
            }
        } catch (error) {
            console.error('Failed to load rental summary', error);
            renderRentalUnavailable(error.message || 'Không tải được báo cáo thuê đồ.');
        }
    }

    function hydrateRentalState(summary) {
        rentalState.facilityId = Number(summary.facilityId || rentalState.facilityId || 0);
        rentalState.facilityName = summary.facilityName || rentalState.facilityName || '';
        rentalState.year = Number(summary.selectedYear || rentalState.year || new Date().getFullYear());
        rentalState.month = Number(summary.selectedMonth || rentalState.month || 1);
        rentalState.day = Number(summary.selectedDay || rentalState.day || 1);
        rentalState.inactiveMonth = Number(summary.selectedInactiveMonth || rentalState.inactiveMonth || 1);
        rentalState.topItems = Array.isArray(summary.topItems) ? summary.topItems : [];
        rentalState.inactiveItems = Array.isArray(summary.inactiveItems) ? summary.inactiveItems : [];

        const facility = findFacilityById(rentalState.facilityId);
        if (facility) {
            rentalState.facilityAddress = facility.address || '';
            setPendingFacilitySelection(facility);
        }

        if (rentalEls.inactiveMonthSelect) {
            rentalEls.inactiveMonthSelect.value = String(rentalState.inactiveMonth);
        }
    }

    function renderRentalSummary(summary) {
        updateRentalMeta();
        updateRentalTitles();

        renderSelectableRevenueChart(
            'rentalMonthly',
            'rentalMonthlyChart',
            Array.isArray(summary.monthlyRevenue) ? summary.monthlyRevenue : [],
            rentalState.month,
            function (point) {
                return Number(point.index || 0);
            },
            function (point) {
                rentalState.month = Number(point.index || 1);
                rentalState.day = 1;
                rentalState.detailScope = 'month';
                rentalState.hourKey = null;
                loadRentalSummary();
            }
        );

        renderTopItems(rentalState.topItems);

        renderSelectableRevenueChart(
            'rentalDaily',
            'rentalDailyChart',
            Array.isArray(summary.dailyRevenue) ? summary.dailyRevenue : [],
            rentalState.day,
            function (point) {
                return Number(point.index || 0);
            },
            function (point) {
                rentalState.day = Number(point.index || 1);
                rentalState.detailScope = 'day';
                rentalState.hourKey = null;
                loadRentalSummary();
            }
        );

        renderSelectableRevenueChart(
            'rentalHourly',
            'rentalHourlyChart',
            Array.isArray(summary.hourlyRevenue) ? summary.hourlyRevenue : [],
            rentalState.hourKey,
            function (point) {
                return point.key || point.label || '';
            },
            function (point) {
                rentalState.hourKey = point.key || point.label || '';
                rentalState.detailScope = 'hour';
                loadHourlyDetail();
            }
        );

        renderInactiveItems(rentalState.inactiveItems);

        if (rentalState.detailScope !== 'hour' || !rentalState.hourKey) {
            renderDetailRows(summary.details || { title: 'Chi tiết doanh thu', rows: [] });
        }

        resizeRentalCharts();
    }

    function updateRentalMeta() {
        const parts = [];
        if (rentalState.facilityName) {
            parts.push(rentalState.facilityName);
        }
        if (rentalState.facilityAddress) {
            parts.push(rentalState.facilityAddress);
        }
        if (rentalState.year) {
            parts.push('Năm báo cáo ' + rentalState.year);
        }
        setText('rentalFacilityMeta', parts.join(' | '));
    }

    function updateRentalTitles() {
        setText('rentalMonthlyChartTitle', '');
        setText('rentalTopItemsTitle', '');
        setText('rentalDailyChartTitle', '');
        setText('rentalHourlyChartTitle', '');
    }

    function renderSelectableRevenueChart(key, canvasId, points, selectedValue, valueResolver, onSelect) {
        const canvas = document.getElementById(canvasId);
        if (!canvas || typeof Chart === 'undefined') {
            return;
        }

        const labels = (points || []).map(function (point) {
            return point.label || point.key || '';
        });
        const values = (points || []).map(function (point) {
            return Number(point.revenue || 0);
        });
        const activeIndex = (points || []).findIndex(function (point) {
            return String(valueResolver(point)) === String(selectedValue);
        });

        if (!charts[key]) {
            charts[key] = new Chart(canvas.getContext('2d'), {
                type: 'bar',
                data: {
                    labels: labels,
                    datasets: [{
                        data: values,
                        backgroundColor: buildSelectableBarColors(values.length, activeIndex),
                        hoverBackgroundColor: buildSelectableHoverColors(values.length, activeIndex),
                        borderRadius: 8,
                        borderSkipped: false
                    }]
                },
                options: buildBarChartOptions(function (event, activeElements) {
                    if (!activeElements || !activeElements.length) {
                        return;
                    }

                    const point = points[activeElements[0].index];
                    if (point) {
                        onSelect(point);
                    }
                })
            });
            return;
        }

        charts[key].data.labels = labels;
        charts[key].data.datasets[0].data = values;
        charts[key].data.datasets[0].backgroundColor = buildSelectableBarColors(values.length, activeIndex);
        charts[key].data.datasets[0].hoverBackgroundColor = buildSelectableHoverColors(values.length, activeIndex);
        charts[key].options.onClick = function (event, activeElements) {
            if (!activeElements || !activeElements.length) {
                return;
            }

            const point = points[activeElements[0].index];
            if (point) {
                onSelect(point);
            }
        };
        charts[key].update();
    }

    function buildSelectableBarColors(length, activeIndex) {
        return Array.from({ length: length }, function (_, index) {
            return index === activeIndex ? BRAND : LIME;
        });
    }

    function buildSelectableHoverColors(length, activeIndex) {
        return Array.from({ length: length }, function (_, index) {
            return index === activeIndex ? BRAND : LIME_LIGHT;
        });
    }

    function renderTopItems(items) {
        if (!rentalEls.topItems) {
            return;
        }

        if (!items || !items.length) {
            rentalEls.topItems.innerHTML =
                '<div class="owner-rental-empty-state">Không có dữ liệu top 10 cho tháng đã chọn.</div>';
            return;
        }

        rentalEls.topItems.innerHTML = items.map(function (item, index) {
            const rank = Number(item.rank || index + 1);
            return '' +
                '<div class="owner-rental-top-item">' +
                    '<span class="owner-rental-top-rank">' + rank + '</span>' +
                    '<span class="owner-rental-top-name">' + escapeHtml(item.name || '') + '</span>' +
                '</div>';
        }).join('');
    }

    function renderInactiveItems(items) {
        if (!rentalEls.inactiveList) {
            return;
        }

        if (rentalEls.deactivateButton) {
            rentalEls.deactivateButton.disabled = !items || !items.length;
        }

        if (!items || !items.length) {
            rentalEls.inactiveList.innerHTML =
                '<div class="owner-rental-empty-state">Không có đồ nào nằm trong nhóm không phát sinh thuê của tháng đã chọn.</div>';
            return;
        }

        rentalEls.inactiveList.innerHTML = items.map(function (item, index) {
            const brand = item.brand ? ' | Hãng: ' + escapeHtml(item.brand) : '';
            return '' +
                '<div class="owner-rental-inactive-item">' +
                    '<span class="owner-rental-inactive-index">' + (index + 1) + '</span>' +
                    '<div>' +
                        '<p class="owner-rental-inactive-name">' + escapeHtml(item.inventoryName || '') + '</p>' +
                        '<p class="owner-rental-inactive-meta">Tổng số lượng: ' +
                            numberFormatter.format(Number(item.totalQuantity || 0)) + brand + '</p>' +
                    '</div>' +
                '</div>';
        }).join('');
    }

    function renderDetailRows(details) {
        setText('rentalDetailTitle', details && details.title ? details.title : 'Chi tiết doanh thu');

        if (!rentalEls.detailTableBody) {
            return;
        }

        rentalState.detailRows = details && Array.isArray(details.rows) ? details.rows : [];
        rentalState.detailCurrentPage = 1;
        renderDetailTablePage();
    }

    function renderDetailTablePage() {
        if (!rentalEls.detailTableBody) {
            return;
        }

        const rows = Array.isArray(rentalState.detailRows) ? rentalState.detailRows : [];
        const totalPages = getDetailTotalPages();

        if (!rows.length) {
            rentalEls.detailTableBody.innerHTML =
                '<tr><td colspan="6" class="text-center text-muted py-4">Không có dữ liệu chi tiết cho mốc đã chọn.</td></tr>';
            clearDetailPagination();
            return;
        }

        if (rentalState.detailCurrentPage > totalPages) {
            rentalState.detailCurrentPage = totalPages;
        }

        const pageSize = Number(rentalState.detailPageSize || 5);
        const startIndex = (rentalState.detailCurrentPage - 1) * pageSize;
        const pageRows = rows.slice(startIndex, startIndex + pageSize);

        rentalEls.detailTableBody.innerHTML = pageRows.map(function (row, index) {
            return '' +
                '<tr>' +
                    '<td>' + (startIndex + index + 1) + '</td>' +
                    '<td>' + escapeHtml(row.inventoryName || '') + '</td>' +
                    '<td>' + numberFormatter.format(Number(row.totalQuantity || 0)) + '</td>' +
                    '<td>' + numberFormatter.format(Number(row.rentedQuantity || 0)) + '</td>' +
                    '<td>' + currencyFormatter.format(Number(row.unitPrice || 0)) + '</td>' +
                    '<td>' + currencyFormatter.format(Number(row.totalRevenue || 0)) + '</td>' +
                '</tr>';
        }).join('');

        renderDetailPagination(totalPages);
    }

    function renderDetailPagination(totalPages) {
        if (!rentalEls.detailPagination) {
            return;
        }

        if (totalPages <= 1) {
            clearDetailPagination();
            return;
        }

        const currentPage = rentalState.detailCurrentPage;
        const pages = buildDetailPaginationPages(currentPage, totalPages);
        let html = '<nav class="mt-4"><ul class="pagination justify-content-center align-items-center gap-2 compact-pagination mb-0">';
        html += buildDetailPaginationControl(
            currentPage > 1 ? currentPage - 1 : null,
            'Trang trước',
            '<i class="bi bi-chevron-left"></i>'
        );

        let previousPage = 0;
        pages.forEach(function (page) {
            if (previousPage && page - previousPage > 1) {
                html += '<li class="page-item disabled"><span class="page-link-static pagination-ellipsis">...</span></li>';
            }

            if (page === currentPage) {
                html += '<li class="page-item active"><span class="page-link-static">' + page + '</span></li>';
            } else {
                html += '<li class="page-item"><a class="page-link" href="#" data-page="' + page + '">' + page + '</a></li>';
            }

            previousPage = page;
        });

        html += buildDetailPaginationControl(
            currentPage < totalPages ? currentPage + 1 : null,
            'Trang sau',
            '<i class="bi bi-chevron-right"></i>'
        );
        html += '</ul></nav>';
        rentalEls.detailPagination.innerHTML = html;
    }

    function buildDetailPaginationPages(currentPage, totalPages) {
        const pageSet = new Set([1, totalPages, currentPage - 1, currentPage, currentPage + 1]);
        return Array.from(pageSet).filter(function (page) {
            return page >= 1 && page <= totalPages;
        }).sort(function (left, right) {
            return left - right;
        });
    }

    function buildDetailPaginationControl(page, ariaLabel, content) {
        if (!page) {
            return '<li class="page-item disabled"><span class="page-link-static" aria-label="' + ariaLabel + '">' + content + '</span></li>';
        }

        return '<li class="page-item"><a class="page-link" href="#" data-page="' + page + '" aria-label="' + ariaLabel + '">' + content + '</a></li>';
    }

    function clearDetailPagination() {
        if (rentalEls.detailPagination) {
            rentalEls.detailPagination.innerHTML = '';
        }
    }

    function getDetailTotalPages() {
        const rows = Array.isArray(rentalState.detailRows) ? rentalState.detailRows.length : 0;
        if (!rows) {
            return 1;
        }
        return Math.ceil(rows / Number(rentalState.detailPageSize || 5));
    }

    async function loadHourlyDetail() {
        if (!rentalState.facilityId || !rentalState.hourKey || !rentalEls.detailTableBody) {
            return;
        }

        setText('rentalDetailTitle', 'Đang tải chi tiết khung giờ...');
        rentalState.detailRows = [];
        rentalState.detailCurrentPage = 1;
        clearDetailPagination();
        rentalEls.detailTableBody.innerHTML =
            '<tr><td colspan="6" class="text-center text-muted py-4">Đang tải dữ liệu...</td></tr>';

        try {
            const response = await requestJson(buildApiUrl('/api/owner/rental-report/details', {
                facilityId: rentalState.facilityId,
                year: rentalState.year,
                month: rentalState.month,
                day: rentalState.day,
                slotTime: rentalState.hourKey,
                scope: 'hour'
            }));

            renderDetailRows(response.data || { title: 'Chi tiết doanh thu', rows: [] });
            resizeRentalCharts();
        } catch (error) {
            console.error('Failed to load hourly detail', error);
            setText('rentalDetailTitle', 'Chi tiết doanh thu theo khung giờ');
            rentalState.detailRows = [];
            rentalState.detailCurrentPage = 1;
            clearDetailPagination();
            rentalEls.detailTableBody.innerHTML =
                '<tr><td colspan="6" class="text-center text-danger py-4">' +
                escapeHtml(error.message || 'Không tải được chi tiết theo khung giờ.') +
                '</td></tr>';
        }
    }

    async function deactivateInactiveItems() {
        if (!rentalState.facilityId) {
            return;
        }

        if (!rentalState.inactiveItems || !rentalState.inactiveItems.length) {
            window.alert('Không có đồ nào để chuyển sang trạng thái unactive trong tháng đã chọn.');
            return;
        }

        const confirmed = window.confirm(
            'Chuyển toàn bộ danh sách đồ không được thuê trong tháng ' +
            rentalState.inactiveMonth + '/' + rentalState.year +
            ' sang trạng thái unactive?'
        );
        if (!confirmed) {
            return;
        }

        toggleButtonLoading(rentalEls.deactivateButton, true, 'Đang xử lý...');
        try {
            const response = await requestJson(buildApiUrl('/api/owner/rental-report/inactive'), {
                method: 'POST',
                form: {
                    facilityId: rentalState.facilityId,
                    year: rentalState.year,
                    month: rentalState.inactiveMonth
                }
            });

            const result = response.data || {};
            window.alert(
                'Đã chuyển ' + numberFormatter.format(Number(result.deactivatedCount || 0)) +
                ' đồ sang trạng thái unactive.'
            );
            await loadRentalSummary();
        } catch (error) {
            console.error('Failed to deactivate inactive items', error);
            window.alert(error.message || 'Không thể ngừng hoạt động các đồ thuê.');
        } finally {
            toggleButtonLoading(rentalEls.deactivateButton, false, 'Ngừng hoạt động');
        }
    }

    function setRentalLoadingState() {
        renderTopItems([]);
        renderInactiveItems([]);
        rentalState.detailRows = [];
        rentalState.detailCurrentPage = 1;
        clearDetailPagination();

        if (rentalEls.detailTableBody) {
            rentalEls.detailTableBody.innerHTML =
                '<tr><td colspan="6" class="text-center text-muted py-4">Đang tải dữ liệu...</td></tr>';
        }
    }

    function renderRentalUnavailable(message) {
        setText('rentalFacilityMeta', message || 'Không tải được báo cáo thuê đồ.');
        renderTopItems([]);
        renderInactiveItems([]);
        renderDetailRows({ title: 'Chi tiết doanh thu', rows: [] });
        resetChartData('rentalMonthly');
        resetChartData('rentalDaily');
        resetChartData('rentalHourly');
    }

    function resetChartData(key) {
        if (!charts[key]) {
            return;
        }

        charts[key].data.labels = [];
        charts[key].data.datasets[0].data = [];
        charts[key].data.datasets[0].backgroundColor = [];
        charts[key].data.datasets[0].hoverBackgroundColor = [];
        charts[key].update();
    }

    function buildApiUrl(path, params) {
        const url = new URL(CONTEXT_PATH + path, window.location.origin);
        if (params) {
            Object.keys(params).forEach(function (key) {
                const value = params[key];
                if (value === null || value === undefined || value === '') {
                    return;
                }
                url.searchParams.set(key, String(value));
            });
        }
        return url.toString();
    }

    async function requestJson(url, options) {
        const requestOptions = {
            method: options && options.method ? options.method : 'GET',
            headers: {
                Accept: 'application/json'
            }
        };

        if (options && options.form) {
            requestOptions.method = options.method || 'POST';
            requestOptions.headers['Content-Type'] = 'application/x-www-form-urlencoded; charset=UTF-8';
            requestOptions.body = new URLSearchParams(options.form).toString();
        }

        const response = await fetch(url, requestOptions);
        let payload;

        try {
            payload = await response.json();
        } catch (error) {
            throw new Error('Phản hồi máy chủ không hợp lệ.');
        }

        if (!response.ok || !payload || payload.success !== true) {
            throw new Error(payload && payload.message ? payload.message : 'Yêu cầu thất bại.');
        }

        return payload;
    }

    function attachSimpleTooltip(selector, tooltipId, getContent) {
        const tooltip = getOrCreateTooltip(tooltipId);

        document.querySelectorAll(selector).forEach(function (element) {
            element.addEventListener('mouseenter', function (event) {
                const content = getContent(element);
                if (!content) {
                    return;
                }
                tooltip.innerHTML = content;
                tooltip.style.opacity = '1';
                positionTooltip(event, tooltip);
            });

            element.addEventListener('mousemove', function (event) {
                if (tooltip.style.opacity !== '1') {
                    return;
                }
                positionTooltip(event, tooltip);
            });

            element.addEventListener('mouseleave', function () {
                tooltip.style.opacity = '0';
            });
        });
    }

    function getOrCreateTooltip(id) {
        let tooltip = document.getElementById(id);
        if (tooltip) {
            return tooltip;
        }

        tooltip = document.createElement('div');
        tooltip.id = id;
        tooltip.style.position = 'fixed';
        tooltip.style.zIndex = '9999';
        tooltip.style.pointerEvents = 'none';
        tooltip.style.opacity = '0';
        tooltip.style.transition = 'opacity 120ms ease';
        tooltip.style.background = '#111827';
        tooltip.style.color = '#FFFFFF';
        tooltip.style.padding = '8px 10px';
        tooltip.style.borderRadius = '10px';
        tooltip.style.fontSize = '12px';
        tooltip.style.fontWeight = '600';
        tooltip.style.lineHeight = '1.45';
        tooltip.style.boxShadow = '0 8px 30px rgba(15,23,42,.28)';
        document.body.appendChild(tooltip);
        return tooltip;
    }

    function positionTooltip(event, tooltip) {
        const spacing = 14;
        const rect = tooltip.getBoundingClientRect();
        const maxLeft = window.innerWidth - rect.width - 8;
        const maxTop = window.innerHeight - rect.height - 8;
        const left = Math.min(event.clientX + spacing, Math.max(maxLeft, 8));
        const top = Math.min(event.clientY + spacing, Math.max(maxTop, 8));
        tooltip.style.left = left + 'px';
        tooltip.style.top = top + 'px';
    }

    function toggleButtonLoading(button, loading, loadingText) {
        if (!button) {
            return;
        }

        if (!button.dataset.originalText) {
            button.dataset.originalText = button.textContent || '';
        }

        button.disabled = !!loading;
        button.textContent = loading ? loadingText : button.dataset.originalText;
    }

    function resizeRentalCharts() {
        ['rentalMonthly', 'rentalDaily', 'rentalHourly'].forEach(function (key) {
            if (charts[key]) {
                charts[key].resize();
            }
        });
    }

    function destroyChart(key) {
        if (charts[key]) {
            charts[key].destroy();
            delete charts[key];
        }
    }

    function setText(target, value) {
        const element = typeof target === 'string' ? document.getElementById(target) : target;
        if (!element) {
            return;
        }
        element.textContent = value == null ? '' : String(value);
    }

    function formatCompactCurrency(value) {
        const amount = Number(value || 0);
        const absolute = Math.abs(amount);

        if (absolute >= 1000000000) {
            return trimDecimal(amount / 1000000000) + ' tỷ';
        }
        if (absolute >= 1000000) {
            return trimDecimal(amount / 1000000) + ' tr';
        }
        if (absolute >= 1000) {
            return trimDecimal(amount / 1000) + 'k';
        }
        return numberFormatter.format(amount);
    }

    function trimDecimal(value) {
        return Number(value || 0).toFixed(1).replace(/\.0$/, '');
    }

    function debounce(callback, delay) {
        let timerId = null;
        return function () {
            const args = arguments;
            clearTimeout(timerId);
            timerId = window.setTimeout(function () {
                callback.apply(null, args);
            }, delay);
        };
    }

    function escapeHtml(value) {
        return String(value == null ? '' : value)
            .replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;')
            .replace(/"/g, '&quot;')
            .replace(/'/g, '&#39;');
    }
})();
