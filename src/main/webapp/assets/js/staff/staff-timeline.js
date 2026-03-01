/**
 * staff-timeline.js — Task 9a + 9c-fix v3: Past slot ONLY in proxy mode
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

    // Task 9a DOM
    var btnProxyMode   = document.getElementById('btnProxyMode');
    var btnProxyCancel = document.getElementById('btnProxyCancel');
    var legendSelected = document.getElementById('legendSelected');
    var bottomBar      = document.getElementById('bottomBar');
    var bottomCount    = document.getElementById('bottomCount');
    var bottomPrice    = document.getElementById('bottomPrice');
    var btnClearAll    = document.getElementById('btnClearAll');
    var btnContinue    = document.getElementById('btnContinue');

    var CTX         = window.ST_CTX || '';
    var FACILITY_ID = window.ST_FACILITY_ID || '';
    var currentDate = '';

    // ─── FIX: Cache today's date string ONCE at page load ───
    var TODAY_STR = (function () {
        var d = new Date();
        var y = d.getFullYear();
        var m = String(d.getMonth() + 1).padStart(2, '0');
        var day = String(d.getDate()).padStart(2, '0');
        return y + '-' + m + '-' + day;
    })();

    // ─── Task 9a: Proxy mode state ───
    var proxyMode     = false;
    var selectedSlots = [];
    var priceMap      = {};
    var courtsData    = [];
    var slotsData     = [];
    var cellMapData   = {};

    // ─── Date helpers ───
    function todayStr() {
        return TODAY_STR;
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

    function formatMoney(amount) {
        if (amount == null) return '0đ';
        return Number(amount).toLocaleString('vi-VN') + 'đ';
    }

    /**
     * Check if a slot is in the past (only relevant when viewing today).
     * A slot is "past" if current time >= slot's end_time.
     */
    function isSlotPast(slotEndTime) {
        if (currentDate !== TODAY_STR) return false;

        var now = new Date();
        var nowMinutes = now.getHours() * 60 + now.getMinutes();

        var parts = slotEndTime.split(':');
        var h = parseInt(parts[0], 10);
        var m = parseInt(parts[1], 10);
        if (isNaN(h) || isNaN(m)) return false;

        var slotEndMinutes = h * 60 + m;
        return nowMinutes >= slotEndMinutes;
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

        if (proxyMode) {
            selectedSlots = [];
            updateBottomBar();
        }

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
                if (!body.success) throw new Error(body.message || 'Lỗi không xác định');

                var data = body.data;

                if (!data.courts || data.courts.length === 0 || !data.slots || data.slots.length === 0) {
                    showState('empty');
                    return;
                }

                courtsData = data.courts;
                slotsData  = data.slots;

                cellMapData = {};
                if (data.cells) {
                    data.cells.forEach(function (c) {
                        cellMapData[c.courtId + '-' + c.slotId] = c;
                    });
                }

                renderGrid(data.courts, data.slots, cellMapData);

                if (proxyMode) {
                    fetchPrices(dateStr);
                }
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
            // FIX: Only dim past headers in proxy mode
            if (proxyMode && isSlotPast(s.endTime)) {
                th.classList.add('st-th-past');
            }
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
                var past = isSlotPast(slot.endTime);

                var td = document.createElement('td');
                td.className = 'st-cell';
                td.setAttribute('data-court-id', court.courtId);
                td.setAttribute('data-slot-id', slot.slotId);

                var inner = document.createElement('div');
                inner.className = 'st-cell-inner';

                // Check if this slot is selected (proxy mode)
                var isSelected = proxyMode && isSlotSelectedFn(court.courtId, slot.slotId);

                if (isSelected) {
                    // ─── SELECTED (proxy mode) ───
                    td.className = 'st-cell st-cell-selected';
                    inner.innerHTML = '<i class="bi bi-check-lg" style="font-size:1rem;"></i>';
                    var priceVal = priceMap[key];
                    if (priceVal != null) {
                        var priceSpan = document.createElement('span');
                        priceSpan.style.cssText = 'font-size:0.6rem;font-weight:700;';
                        priceSpan.textContent = formatMoney(priceVal);
                        inner.appendChild(priceSpan);
                    }
                    (function (cId, sId) {
                        inner.addEventListener('click', function () {
                            toggleSlot(cId, sId);
                        });
                    })(court.courtId, slot.slotId);

                } else if (cell.state === 'BOOKED') {
                    // ─── BOOKED ───
                    var statusLower = cell.bookingStatus.toLowerCase();
                    td.classList.add('st-cell-' + statusLower);

                    // FIX: Only grey out past booked cells in proxy mode
                    if (proxyMode && past && cell.bookingStatus !== 'COMPLETED') {
                        td.classList.add('st-cell-past');
                    }

                    var nameEl = document.createElement('span');
                    nameEl.className = 'st-cell-customer';
                    nameEl.textContent = cell.customerName || '—';
                    inner.appendChild(nameEl);

                    var statusEl = document.createElement('span');
                    statusEl.className = 'st-cell-status';
                    statusEl.textContent = statusLabel(cell.bookingStatus);
                    inner.appendChild(statusEl);

                    if (!proxyMode && cell.bookingId) {
                        inner.style.cursor = 'pointer';
                        inner.setAttribute('data-booking-id', cell.bookingId);
                        (function (bid) {
                            inner.addEventListener('click', function () {
                                window.location.href = CTX + '/staff/booking/detail/' + bid;
                            });
                        })(cell.bookingId);
                    }

                } else if (cell.state === 'DISABLED') {
                    // ─── DISABLED ───
                    td.classList.add('st-cell-disabled');
                    var reasonEl = document.createElement('span');
                    reasonEl.className = 'st-cell-reason';
                    reasonEl.textContent = cell.disabledReason || 'Không khả dụng';
                    inner.appendChild(reasonEl);

                } else {
                    // ─── AVAILABLE ───
                    td.classList.add('st-cell-available');

                    // FIX: Only grey out past available cells in proxy mode
                    if (proxyMode && past) {
                        td.classList.add('st-cell-past');
                    }

                    if (proxyMode && !past) {
                        // Show price hint in proxy mode
                        var pVal = priceMap[key];
                        if (pVal != null) {
                            inner.textContent = formatMoney(pVal);
                            inner.style.fontSize = '0.625rem';
                        }
                        // Click to select
                        (function (cId, sId) {
                            inner.addEventListener('click', function () {
                                toggleSlot(cId, sId);
                            });
                        })(court.courtId, slot.slotId);
                    }
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

    // ─── Task 9a: Proxy mode functions ───

    function enterProxyMode() {
        proxyMode = true;
        selectedSlots = [];
        document.getElementById('timelineContainer').classList.add('st-proxy-mode');
        btnProxyMode.classList.add('d-none');
        btnProxyCancel.classList.remove('d-none');
        legendSelected.classList.remove('d-none');
        fetchPrices(currentDate);
    }

    function exitProxyMode() {
        proxyMode = false;
        selectedSlots = [];
        priceMap = {};
        document.getElementById('timelineContainer').classList.remove('st-proxy-mode');
        btnProxyMode.classList.remove('d-none');
        btnProxyCancel.classList.add('d-none');
        legendSelected.classList.add('d-none');
        bottomBar.classList.add('d-none');
        renderGrid(courtsData, slotsData, cellMapData);
    }

    function fetchPrices(dateStr) {
        fetch(CTX + '/api/staff/booking/slot-prices?date=' + encodeURIComponent(dateStr), {
            credentials: 'same-origin',
            headers: { 'Accept': 'application/json' }
        })
            .then(function (res) { return res.json(); })
            .then(function (body) {
                if (!body.success) return;
                priceMap = {};
                (body.data.prices || []).forEach(function (p) {
                    priceMap[p.courtId + '-' + p.slotId] = p.price;
                });
                renderGrid(courtsData, slotsData, cellMapData);
            })
            .catch(function (err) {
                console.error('Price fetch error:', err);
            });
    }

    function isSlotSelectedFn(courtId, slotId) {
        return selectedSlots.some(function (s) {
            return s.courtId === courtId && s.slotId === slotId;
        });
    }

    function toggleSlot(courtId, slotId) {
        if (!proxyMode) return;

        // Find slot data to check if past
        var slot = slotsData.find(function (s) { return s.slotId === slotId; });
        if (slot && isSlotPast(slot.endTime)) return; // can't select past slots

        var idx = selectedSlots.findIndex(function (s) {
            return s.courtId === courtId && s.slotId === slotId;
        });

        if (idx >= 0) {
            selectedSlots.splice(idx, 1);
        } else {
            var key = courtId + '-' + slotId;
            var cell = cellMapData[key];
            if (cell && cell.state !== 'AVAILABLE' && cell.bookingStatus !== 'CANCELLED') return;

            var court = courtsData.find(function (c) { return c.courtId === courtId; });
            if (!court || !slot) return;

            var price = priceMap[key] || 0;

            selectedSlots.push({
                courtId: courtId,
                courtName: court.courtName,
                slotId: slotId,
                startTime: slot.startTime,
                endTime: slot.endTime,
                price: price
            });
        }

        updateBottomBar();
        renderGrid(courtsData, slotsData, cellMapData);
    }

    function updateBottomBar() {
        if (selectedSlots.length === 0) {
            bottomBar.classList.add('d-none');
            return;
        }

        bottomBar.classList.remove('d-none');

        var totalPrice = 0;
        selectedSlots.forEach(function (s) { totalPrice += (s.price || 0); });

        bottomCount.textContent = selectedSlots.length + ' slot';
        bottomPrice.textContent = formatMoney(totalPrice);
    }

    function validateSelection() {
        var groups = {};
        selectedSlots.forEach(function (s) {
            if (!groups[s.courtId]) groups[s.courtId] = [];
            groups[s.courtId].push(s);
        });

        var errors = [];

        for (var courtId in groups) {
            var courtSlots = groups[courtId];
            courtSlots.sort(function (a, b) {
                return a.startTime.localeCompare(b.startTime);
            });

            var sessions = [];
            var currentSession = [courtSlots[0]];

            for (var i = 1; i < courtSlots.length; i++) {
                var prev = currentSession[currentSession.length - 1];
                if (prev.endTime === courtSlots[i].startTime) {
                    currentSession.push(courtSlots[i]);
                } else {
                    sessions.push(currentSession);
                    currentSession = [courtSlots[i]];
                }
            }
            sessions.push(currentSession);

            for (var j = 0; j < sessions.length; j++) {
                if (sessions[j].length < 2) {
                    errors.push(sessions[j]);
                }
            }
        }

        if (errors.length > 0) {
            errors.forEach(function (session) {
                session.forEach(function (s) {
                    var td = document.querySelector('td[data-court-id="' + s.courtId + '"][data-slot-id="' + s.slotId + '"]');
                    if (td) {
                        td.classList.add('st-cell-error');
                        setTimeout(function () { td.classList.remove('st-cell-error'); }, 1500);
                    }
                });
            });

            alert('Mỗi phiên chơi phải có ít nhất 2 slot liên tiếp trên cùng 1 sân.\n\nVui lòng chọn thêm slot hoặc bỏ slot lẻ.');
            return false;
        }

        return true;
    }

    function goToCreatePage() {
        if (!validateSelection()) return;

        var payload = {
            date: currentDate,
            slots: selectedSlots
        };

        sessionStorage.setItem('staffBookingSlots', JSON.stringify(payload));
        window.location.href = CTX + '/staff/booking/create';
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

    // Task 9a events
    btnProxyMode.addEventListener('click', enterProxyMode);
    btnProxyCancel.addEventListener('click', exitProxyMode);
    btnClearAll.addEventListener('click', function () {
        selectedSlots = [];
        updateBottomBar();
        renderGrid(courtsData, slotsData, cellMapData);
    });
    btnContinue.addEventListener('click', goToCreatePage);

    // ─── Init ───
    var urlParams = new URLSearchParams(window.location.search);
    var initDate = urlParams.get('date');
    if (initDate && /^\d{4}-\d{2}-\d{2}$/.test(initDate)) {
        fetchTimeline(initDate);
    } else {
        fetchTimeline(todayStr());
    }

})();