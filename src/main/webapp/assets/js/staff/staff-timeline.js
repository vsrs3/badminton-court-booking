/**
 * staff-timeline.js — Task 3 + Task 6: Support ?date= URL param
 */
(function () {
    'use strict';

    // ─── DOM refs ───
    var btnToday      = document.getElementById('btnToday');
    var btnTomorrow   = document.getElementById('btnTomorrow');
    var dateInput     = document.getElementById('datePickerInput');
    var dateDisplay   = document.getElementById('currentDateDisplay');
    var stateLoading  = document.getElementById('stateLoading');
    var stateError    = document.getElementById('stateError');
    var stateEmpty    = document.getElementById('stateEmpty');
    var gridScroll    = document.getElementById('gridScroll');
    var gridHeaderRow = document.getElementById('gridHeaderRow');
    var gridBody      = document.getElementById('gridBody');

    var CTX         = window.ST_CTX || '';
    var FACILITY_ID = window.ST_FACILITY_ID || '';
    var currentDate = todayStr();

    // ─── Date helpers ───
    function todayStr() {
        return fmtDate(new Date());
    }

    function tomorrowStr() {
        var d = new Date();
        d.setDate(d.getDate() + 1);
        return fmtDate(d);
    }

    function fmtDate(d) {
        var y = d.getFullYear();
        var m = String(d.getMonth() + 1).padStart(2, '0');
        var day = String(d.getDate()).padStart(2, '0');
        return y + '-' + m + '-' + day;
    }

    function fmtDisplayDate(dateStr) {
        var d = new Date(dateStr + 'T00:00:00');
        var wd = ['Chủ nhật','Thứ 2','Thứ 3','Thứ 4','Thứ 5','Thứ 6','Thứ 7'];
        var day = String(d.getDate()).padStart(2, '0');
        var mon = String(d.getMonth() + 1).padStart(2, '0');
        return wd[d.getDay()] + ', ' + day + '/' + mon + '/' + d.getFullYear();
    }

    // ─── UI state helpers ───
    function updateButtons(dateStr) {
        btnToday.classList.toggle('active', dateStr === todayStr());
        btnTomorrow.classList.toggle('active', dateStr === tomorrowStr());
        dateDisplay.textContent = fmtDisplayDate(dateStr);
        dateInput.value = dateStr;
    }

    function showState(state) {
        stateLoading.classList.add('d-none');
        stateError.classList.add('d-none');
        stateEmpty.classList.add('d-none');
        gridScroll.classList.add('d-none');

        switch (state) {
            case 'loading': stateLoading.classList.remove('d-none'); break;
            case 'error':   stateError.classList.remove('d-none');   break;
            case 'empty':   stateEmpty.classList.remove('d-none');   break;
            case 'grid':    gridScroll.classList.remove('d-none');   break;
        }
    }

    // ─── FETCH timeline data from API ───
    function fetchTimeline(dateStr) {
        currentDate = dateStr;
        updateButtons(dateStr);
        showState('loading');

        // Update URL without reload (so back button preserves date)
        var newUrl = window.location.pathname + '?date=' + dateStr;
        history.replaceState(null, '', newUrl);

        var url = CTX + '/api/staff/timeline?date=' + encodeURIComponent(dateStr);

        fetch(url, {
            method: 'GET',
            credentials: 'same-origin',
            headers: { 'Accept': 'application/json' }
        })
            .then(function (res) {
                if (!res.ok) {
                    return res.json().then(function (err) {
                        throw new Error(err.message || 'HTTP ' + res.status);
                    }).catch(function () {
                        throw new Error('HTTP ' + res.status);
                    });
                }
                return res.json();
            })
            .then(function (body) {
                if (!body.success) {
                    throw new Error(body.message || 'Lỗi không xác định');
                }

                var data = body.data;

                if (!data.courts || data.courts.length === 0) {
                    showState('empty');
                    return;
                }

                if (!data.slots || data.slots.length === 0) {
                    showState('empty');
                    return;
                }

                var cellMap = {};
                if (data.cells) {
                    data.cells.forEach(function (c) {
                        cellMap[c.courtId + '-' + c.slotId] = c;
                    });
                }

                renderGrid(data.courts, data.slots, cellMap);
            })
            .catch(function (err) {
                console.error('Timeline fetch error:', err);
                showState('error');
            });
    }

    // ─── RENDER grid ───
    function renderGrid(courts, slots, cellMap) {
        gridHeaderRow.innerHTML = '<th class="st-grid-corner">Sân \\ Giờ</th>';
        gridBody.innerHTML = '';

        slots.forEach(function (s) {
            var th = document.createElement('th');
            th.textContent = s.startTime;
            gridHeaderRow.appendChild(th);
        });

        courts.forEach(function (court) {
            var tr = document.createElement('tr');

            var tdName = document.createElement('td');
            tdName.className = 'st-court-name';
            tdName.textContent = court.courtName;
            tr.appendChild(tdName);

            slots.forEach(function (slot) {
                var key = court.courtId + '-' + slot.slotId;
                var cell = cellMap[key] || { state: 'AVAILABLE' };

                var td = document.createElement('td');
                td.className = 'st-cell';

                var inner = document.createElement('div');
                inner.className = 'st-cell-inner';

                if (cell.state === 'BOOKED') {
                    var statusLower = cell.bookingStatus.toLowerCase();
                    td.classList.add('st-cell-' + statusLower);

                    var nameEl = document.createElement('span');
                    nameEl.className = 'st-cell-customer';
                    nameEl.textContent = cell.customerName || '—';
                    inner.appendChild(nameEl);

                    var statusEl = document.createElement('span');
                    statusEl.className = 'st-cell-status';
                    statusEl.textContent = statusLabel(cell.bookingStatus);
                    inner.appendChild(statusEl);

                    if (cell.bookingId) {
                        inner.style.cursor = 'pointer';
                        inner.setAttribute('data-booking-id', cell.bookingId);
                        inner.addEventListener('click', function () {
                            window.location.href = CTX + '/staff/booking/detail/' + cell.bookingId;
                        });
                    }

                } else if (cell.state === 'DISABLED') {
                    td.classList.add('st-cell-disabled');
                    var reasonEl = document.createElement('span');
                    reasonEl.className = 'st-cell-reason';
                    reasonEl.textContent = cell.disabledReason || 'Không khả dụng';
                    inner.appendChild(reasonEl);

                } else {
                    td.classList.add('st-cell-available');
                }

                td.appendChild(inner);
                tr.appendChild(td);
            });

            gridBody.appendChild(tr);
        });

        showState('grid');
    }

    function statusLabel(status) {
        switch (status) {
            case 'PENDING':   return 'Chờ XN';
            case 'CONFIRMED': return 'Đã XN';
            case 'COMPLETED': return 'Xong';
            case 'CANCELLED': return 'Đã hủy';
            default:          return status;
        }
    }

    // ─── Public: retry button ───
    window.reloadTimeline = function () {
        fetchTimeline(currentDate);
    };

    // ─── Event listeners ───
    btnToday.addEventListener('click', function () {
        fetchTimeline(todayStr());
    });

    btnTomorrow.addEventListener('click', function () {
        fetchTimeline(tomorrowStr());
    });

    dateInput.addEventListener('change', function () {
        if (this.value) {
            fetchTimeline(this.value);
        }
    });

    // ─── Init: check ?date= URL param, else load today ───
    var urlParams = new URLSearchParams(window.location.search);
    var initDate = urlParams.get('date');
    if (initDate && /^\d{4}-\d{2}-\d{2}$/.test(initDate)) {
        fetchTimeline(initDate);
    } else {
        fetchTimeline(todayStr());
    }

})();