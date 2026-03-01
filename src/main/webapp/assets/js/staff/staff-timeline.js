/**
 * staff-timeline.js — Task 3: Real AJAX fetch + dynamic render
 */
(function () {
    'use strict';

    // ─── DOM refs ───
    const btnToday      = document.getElementById('btnToday');
    const btnTomorrow   = document.getElementById('btnTomorrow');
    const dateInput     = document.getElementById('datePickerInput');
    const dateDisplay   = document.getElementById('currentDateDisplay');
    const stateLoading  = document.getElementById('stateLoading');
    const stateError    = document.getElementById('stateError');
    const stateEmpty    = document.getElementById('stateEmpty');
    const gridScroll    = document.getElementById('gridScroll');
    const gridHeaderRow = document.getElementById('gridHeaderRow');
    const gridBody      = document.getElementById('gridBody');

    const CTX         = window.ST_CTX || '';
    const FACILITY_ID = window.ST_FACILITY_ID || '';
    let currentDate   = todayStr();

    // ─── Date helpers ───
    function todayStr() {
        return fmtDate(new Date());
    }

    function tomorrowStr() {
        const d = new Date();
        d.setDate(d.getDate() + 1);
        return fmtDate(d);
    }

    function fmtDate(d) {
        const y = d.getFullYear();
        const m = String(d.getMonth() + 1).padStart(2, '0');
        const day = String(d.getDate()).padStart(2, '0');
        return y + '-' + m + '-' + day;
    }

    function fmtDisplayDate(dateStr) {
        const d = new Date(dateStr + 'T00:00:00');
        const wd = ['Chủ nhật','Thứ 2','Thứ 3','Thứ 4','Thứ 5','Thứ 6','Thứ 7'];
        const day = String(d.getDate()).padStart(2, '0');
        const mon = String(d.getMonth() + 1).padStart(2, '0');
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

        const url = CTX + '/api/staff/timeline?date=' + encodeURIComponent(dateStr);

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

                const data = body.data;

                // No courts → empty
                if (!data.courts || data.courts.length === 0) {
                    showState('empty');
                    return;
                }

                // No slots → empty
                if (!data.slots || data.slots.length === 0) {
                    showState('empty');
                    return;
                }

                // Build cell lookup map: "courtId-slotId" → cell
                const cellMap = {};
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
        // Clear
        gridHeaderRow.innerHTML = '<th class="st-grid-corner">Sân \\ Giờ</th>';
        gridBody.innerHTML = '';

        // Header row: time slots
        slots.forEach(function (s) {
            const th = document.createElement('th');
            th.textContent = s.startTime;
            gridHeaderRow.appendChild(th);
        });

        // Court rows
        courts.forEach(function (court) {
            const tr = document.createElement('tr');

            // Court name (sticky)
            const tdName = document.createElement('td');
            tdName.className = 'st-court-name';
            tdName.textContent = court.courtName;
            tr.appendChild(tdName);

            // Cells
            slots.forEach(function (slot) {
                const key = court.courtId + '-' + slot.slotId;
                const cell = cellMap[key] || { state: 'AVAILABLE' };

                const td = document.createElement('td');
                td.className = 'st-cell';

                const inner = document.createElement('div');
                inner.className = 'st-cell-inner';

                if (cell.state === 'BOOKED') {
                    const statusLower = cell.bookingStatus.toLowerCase();
                    td.classList.add('st-cell-' + statusLower);

                    // Customer name
                    const nameEl = document.createElement('span');
                    nameEl.className = 'st-cell-customer';
                    nameEl.textContent = cell.customerName || '—';
                    inner.appendChild(nameEl);

                    // Status label
                    const statusEl = document.createElement('span');
                    statusEl.className = 'st-cell-status';
                    statusEl.textContent = statusLabel(cell.bookingStatus);
                    inner.appendChild(statusEl);

                    // Click → detail page
                    if (cell.bookingId) {
                        inner.style.cursor = 'pointer';
                        inner.setAttribute('data-booking-id', cell.bookingId);
                        inner.addEventListener('click', function () {
                            window.location.href = CTX + '/staff/booking/detail/' + cell.bookingId;
                        });
                    }

                } else if (cell.state === 'DISABLED') {
                    td.classList.add('st-cell-disabled');
                    const reasonEl = document.createElement('span');
                    reasonEl.className = 'st-cell-reason';
                    reasonEl.textContent = cell.disabledReason || 'Không khả dụng';
                    inner.appendChild(reasonEl);

                } else {
                    // AVAILABLE
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

    // ─── Init: load today ───
    fetchTimeline(todayStr());

})();