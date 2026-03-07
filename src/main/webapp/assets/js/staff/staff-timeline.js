/**
 * staff-timeline.js - proxy create mode + booking edit mode
 */
(function () {
    'use strict';

    var btnToday = document.getElementById('btnToday');
    var btnTomorrow = document.getElementById('btnTomorrow');
    var dateInput = document.getElementById('datePickerInput');
    var dateDisplay = document.getElementById('currentDateDisplay');
    var stateLoading = document.getElementById('stateLoading');
    var stateError = document.getElementById('stateError');
    var stateEmpty = document.getElementById('stateEmpty');
    var gridScroll = document.getElementById('gridScroll');
    var gridHeaderRow = document.getElementById('gridHeaderRow');
    var gridBody = document.getElementById('gridBody');

    var btnProxyMode = document.getElementById('btnProxyMode');
    var btnProxyCancel = document.getElementById('btnProxyCancel');
    var legendSelected = document.getElementById('legendSelected');
    var bottomBar = document.getElementById('bottomBar');
    var bottomCount = document.getElementById('bottomCount');
    var bottomPrice = document.getElementById('bottomPrice');
    var btnClearAll = document.getElementById('btnClearAll');
    var btnContinue = document.getElementById('btnContinue');

    var CTX = window.ST_CTX || '';

    var MODE_NORMAL = 'NORMAL';
    var MODE_PROXY = 'PROXY';
    var MODE_EDIT = 'EDIT';

    var mode = MODE_NORMAL;
    var currentDate = '';
    var courtsData = [];
    var slotsData = [];
    var cellMapData = {};
    var selectedSlots = [];
    var priceMap = {};
    var priceLoaded = false;

    var editBookingId = null;
    var editEtag = null;
    var editOriginalSlots = [];
    var editOriginalKeyToBookingSlotId = {};
    var editEditableKeySet = {};
    var editLockedKeySet = {};

    var TODAY_STR = (function () {
        var d = new Date();
        return d.getFullYear() + '-' + String(d.getMonth() + 1).padStart(2, '0') + '-' + String(d.getDate()).padStart(2, '0');
    })();

    function todayStr() { return TODAY_STR; }
    function tomorrowStr() {
        var d = new Date();
        d.setDate(d.getDate() + 1);
        return fmtDate(d);
    }

    function fmtDate(d) {
        return d.getFullYear() + '-' + String(d.getMonth() + 1).padStart(2, '0') + '-' + String(d.getDate()).padStart(2, '0');
    }

    function fmtDisplayDate(dateStr) {
        var d = new Date(dateStr + 'T00:00:00');
        var wd = ['Ch\u1ee7 nh\u1eadt', 'Th\u1ee9 2', 'Th\u1ee9 3', 'Th\u1ee9 4', 'Th\u1ee9 5', 'Th\u1ee9 6', 'Th\u1ee9 7'];
        return wd[d.getDay()] + ', ' + String(d.getDate()).padStart(2, '0') + '/' +
            String(d.getMonth() + 1).padStart(2, '0') + '/' + d.getFullYear();
    }

    function formatMoney(amount) {
        if (amount == null) return '0\u0111';
        return Number(amount).toLocaleString('vi-VN') + '\u0111';
    }

    function isSlotPast(slotEndTime) {
        if (currentDate < TODAY_STR) return true;
        if (currentDate !== TODAY_STR) return false;
        var p = slotEndTime.split(':');
        var slotEnd = parseInt(p[0], 10) * 60 + parseInt(p[1], 10);
        var now = new Date();
        var nowM = now.getHours() * 60 + now.getMinutes();
        return nowM >= slotEnd;
    }

    function hasNoPrice(courtId, slotId) {
        if (!priceLoaded) return false;
        return !(courtId + '-' + slotId in priceMap);
    }

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
        if (state === 'loading') stateLoading.classList.remove('d-none');
        if (state === 'error') stateError.classList.remove('d-none');
        if (state === 'empty') stateEmpty.classList.remove('d-none');
        if (state === 'grid') gridScroll.classList.remove('d-none');
    }

    function setMode(newMode) {
        mode = newMode;
        document.getElementById('timelineContainer').classList.toggle('st-proxy-mode', mode !== MODE_NORMAL);

        if (mode === MODE_PROXY) {
            btnProxyMode.classList.add('d-none');
            btnProxyCancel.classList.remove('d-none');
            btnProxyCancel.innerHTML = '<i class="bi bi-x-circle me-1"></i>H\u1ee7y \u0111\u1eb7t h\u1ed9';
            legendSelected.classList.remove('d-none');
            btnContinue.innerHTML = 'Ti\u1ebfp t\u1ee5c<i class="bi bi-arrow-right ms-1"></i>';
            btnClearAll.innerHTML = '<i class="bi bi-trash me-1"></i>B\u1ecf ch\u1ecdn';
        } else if (mode === MODE_EDIT) {
            btnProxyMode.classList.add('d-none');
            btnProxyCancel.classList.remove('d-none');
            btnProxyCancel.innerHTML = '<i class="bi bi-arrow-left me-1"></i>Tho\u00e1t ch\u1ec9nh s\u1eeda';
            legendSelected.classList.remove('d-none');
            btnContinue.innerHTML = '<i class="bi bi-save me-1"></i>L\u01b0u thay \u0111\u1ed5i';
            btnClearAll.innerHTML = '<i class="bi bi-arrow-counterclockwise me-1"></i>Ho\u00e0n t\u00e1c';
        } else {
            btnProxyMode.classList.remove('d-none');
            btnProxyCancel.classList.add('d-none');
            legendSelected.classList.add('d-none');
            bottomBar.classList.add('d-none');
            selectedSlots = [];
        }
    }

    function cellKey(courtId, slotId) {
        return courtId + '-' + slotId;
    }

    function isSlotSelected(courtId, slotId) {
        return selectedSlots.some(function (s) { return s.courtId === courtId && s.slotId === slotId; });
    }

    function fetchTimeline(dateStr) {
        currentDate = dateStr;
        updateButtons(dateStr);
        showState('loading');

        var urlParams = new URLSearchParams();
        urlParams.set('date', dateStr);
        if (mode === MODE_EDIT && editBookingId) urlParams.set('editBookingId', String(editBookingId));
        history.replaceState(null, '', window.location.pathname + '?' + urlParams.toString());

        fetch(CTX + '/api/staff/timeline?date=' + encodeURIComponent(dateStr), {
            method: 'GET',
            credentials: 'same-origin',
            headers: { 'Accept': 'application/json' }
        })
            .then(function (res) {
                if (!res.ok) throw new Error('HTTP ' + res.status);
                return res.json();
            })
            .then(function (body) {
                if (!body.success) throw new Error(body.message || 'L\u1ed7i');
                var data = body.data;
                if (!data.courts || !data.courts.length || !data.slots || !data.slots.length) {
                    showState('empty');
                    return;
                }

                courtsData = data.courts;
                slotsData = data.slots;
                cellMapData = {};
                (data.cells || []).forEach(function (c) {
                    cellMapData[cellKey(c.courtId, c.slotId)] = c;
                });

                renderGrid();
                if (mode !== MODE_NORMAL) {
                    fetchPrices(dateStr, function () {
                        if (mode === MODE_EDIT) {
                            ensureEditContextLoaded();
                        }
                    });
                }
            })
            .catch(function (err) {
                console.error('Timeline fetch error:', err);
                showState('error');
            });
    }

    function fetchPrices(dateStr, done) {
        priceLoaded = false;
        fetch(CTX + '/api/staff/booking/slot-prices?date=' + encodeURIComponent(dateStr), {
            credentials: 'same-origin',
            headers: { 'Accept': 'application/json' }
        })
            .then(function (res) { return res.json(); })
            .then(function (body) {
                if (!body.success) return;
                priceMap = {};
                (body.data.prices || []).forEach(function (p) {
                    priceMap[cellKey(p.courtId, p.slotId)] = p.price;
                });
                priceLoaded = true;
                renderGrid();
                if (done) done();
            })
            .catch(function (err) {
                console.error('Price fetch error:', err);
                if (done) done();
            });
    }

    function ensureEditContextLoaded() {
        if (!editBookingId) return;
        fetch(CTX + '/api/staff/booking/detail/' + editBookingId, {
            credentials: 'same-origin',
            headers: { 'Accept': 'application/json' }
        })
            .then(function (res) {
                if (!res.ok) throw new Error('Kh\u00f4ng th\u1ec3 t\u1ea3i booking c\u1ea7n ch\u1ec9nh s\u1eeda');
                return res.json();
            })
            .then(function (body) {
                if (!body.success) throw new Error(body.message || 'L\u1ed7i');
                var d = body.data;
                if (d.bookingStatus !== 'CONFIRMED') {
                    uiAlert('Chi co the chinh sua booking o trang thai CONFIRMED', 'Khong the chinh sua').then(function () {
                        window.location.href = CTX + '/staff/booking/detail/' + editBookingId;
                    });
                    return;
                }

                editEtag = d.etag;
                editOriginalSlots = [];
                editOriginalKeyToBookingSlotId = {};
                editEditableKeySet = {};
                editLockedKeySet = {};

                (d.slots || []).forEach(function (s) {
                    var key = cellKey(s.courtId, s.slotId);
                    if (s.slotStatus === 'PENDING') {
                        editEditableKeySet[key] = true;
                        editOriginalKeyToBookingSlotId[key] = s.bookingSlotId;
                        editOriginalSlots.push({
                            bookingSlotId: s.bookingSlotId,
                            courtId: s.courtId,
                            slotId: s.slotId,
                            startTime: s.startTime,
                            endTime: s.endTime,
                            courtName: findCourtName(s.courtId),
                            price: priceMap[key]
                        });
                    } else {
                        editLockedKeySet[key] = true;
                    }
                });

                selectedSlots = cloneSlots(editOriginalSlots);
                updateBottomBar();
                renderGrid();
            })
            .catch(function (err) {
                console.error('Edit context error:', err);
                uiAlert(err.message || 'Khong the mo du lieu chinh sua booking', 'Loi').then(function () {
                    window.location.href = CTX + '/staff/timeline?date=' + encodeURIComponent(currentDate);
                });
            });
    }

    function cloneSlots(slots) {
        return slots.map(function (s) {
            return {
                bookingSlotId: s.bookingSlotId || null,
                courtId: s.courtId,
                courtName: s.courtName,
                slotId: s.slotId,
                startTime: s.startTime,
                endTime: s.endTime,
                price: s.price
            };
        });
    }

    function renderGrid() {
        gridHeaderRow.innerHTML = '<th class="st-grid-corner">S\u00e2n \\ Gi\u1edd</th>';
        gridBody.innerHTML = '';

        slotsData.forEach(function (s) {
            var th = document.createElement('th');
            th.textContent = s.startTime;
            if (mode !== MODE_NORMAL && isSlotPast(s.endTime)) th.classList.add('st-th-past');
            gridHeaderRow.appendChild(th);
        });

        courtsData.forEach(function (court) {
            var tr = document.createElement('tr');
            var tdName = document.createElement('td');
            tdName.className = 'st-court-name';
            tdName.textContent = court.courtName;
            tr.appendChild(tdName);

            slotsData.forEach(function (slot) {
                var key = cellKey(court.courtId, slot.slotId);
                var cell = cellMapData[key] || { state: 'AVAILABLE' };
                var past = isSlotPast(slot.endTime);
                var isSelected = (mode !== MODE_NORMAL) && isSlotSelected(court.courtId, slot.slotId);

                var td = document.createElement('td');
                td.className = 'st-cell';
                td.setAttribute('data-court-id', court.courtId);
                td.setAttribute('data-slot-id', slot.slotId);

                var inner = document.createElement('div');
                inner.className = 'st-cell-inner';

                if (isSelected) {
                    td.className = 'st-cell st-cell-selected';
                    inner.innerHTML = '<i class="bi bi-check-lg" style="font-size:1rem;"></i>';
                    var sp = document.createElement('span');
                    sp.style.cssText = 'font-size:0.6rem;font-weight:700;';
                    var p = findSelectedPrice(court.courtId, slot.slotId);
                    if (p == null) p = priceMap[key];
                    if (p != null) sp.textContent = formatMoney(p);
                    inner.appendChild(sp);
                    bindToggle(inner, court.courtId, slot.slotId);
                } else if (cell.state === 'BOOKED') {
                    var ownBooking = (mode === MODE_EDIT && editBookingId && String(cell.bookingId) === String(editBookingId));
                    if (ownBooking && editEditableKeySet[key]) {
                        td.classList.add('st-cell-available');
                        if (past) td.classList.add('st-cell-past');
                        var txt = document.createElement('span');
                        txt.className = 'st-cell-status';
                        txt.textContent = '\u0110ang gi\u1eef';
                        inner.appendChild(txt);
                        bindToggle(inner, court.courtId, slot.slotId);
                    } else if (ownBooking && editLockedKeySet[key]) {
                        td.classList.add('st-cell-confirmed');
                        var lockEl = document.createElement('span');
                        lockEl.className = 'st-cell-status';
                        lockEl.textContent = '\u0110\u00e3 kh\u00f3a';
                        inner.appendChild(lockEl);
                    } else {
                        td.classList.add('st-cell-' + (cell.bookingStatus || 'confirmed').toLowerCase());
                        if (cell.slotStatus === 'NO_SHOW') td.classList.add('st-cell-noshow');
                        if (mode !== MODE_NORMAL && past) td.classList.add('st-cell-past');

                        var nameEl = document.createElement('span');
                        nameEl.className = 'st-cell-customer';
                        nameEl.textContent = cell.customerName || '\u2014';
                        inner.appendChild(nameEl);

                        var statusEl = document.createElement('span');
                        statusEl.className = 'st-cell-status';
                        statusEl.textContent = (cell.slotStatus === 'NO_SHOW') ? statusLabel('NO_SHOW') : statusLabel(cell.bookingStatus);
                        inner.appendChild(statusEl);

                        if (mode === MODE_NORMAL && cell.bookingId) {
                            inner.style.cursor = 'pointer';
                            inner.addEventListener('click', function () {
                                window.location.href = CTX + '/staff/booking/detail/' + cell.bookingId;
                            });
                        }
                    }
                } else if (cell.state === 'DISABLED') {
                    td.classList.add('st-cell-disabled');
                    if (mode !== MODE_NORMAL && past) td.classList.add('st-cell-past');
                    var reasonEl = document.createElement('span');
                    reasonEl.className = 'st-cell-reason';
                    reasonEl.textContent = cell.disabledReason || 'Kh\u00f4ng kh\u1ea3 d\u1ee5ng';
                    inner.appendChild(reasonEl);
                } else {
                    td.classList.add('st-cell-available');
                    if (mode !== MODE_NORMAL && past) td.classList.add('st-cell-past');
                    var noPrice = (mode !== MODE_NORMAL && !past && hasNoPrice(court.courtId, slot.slotId));
                    if (noPrice) {
                        td.classList.remove('st-cell-available');
                        td.classList.add('st-cell-no-price');
                        var np = document.createElement('span');
                        np.className = 'st-cell-reason';
                        np.textContent = 'Ch\u01b0a c\u00f3 gi\u00e1';
                        inner.appendChild(np);
                    } else if (mode !== MODE_NORMAL) {
                        var pVal = priceMap[key];
                        if (pVal != null) {
                            inner.textContent = formatMoney(pVal);
                            inner.style.fontSize = '0.625rem';
                        }
                        bindToggle(inner, court.courtId, slot.slotId);
                    }
                }

                td.appendChild(inner);
                tr.appendChild(td);
            });

            gridBody.appendChild(tr);
        });

        showState('grid');
    }

    function bindToggle(el, courtId, slotId) {
        el.addEventListener('click', function () {
            toggleSlot(courtId, slotId);
        });
    }

    function findSelectedPrice(courtId, slotId) {
        var item = selectedSlots.find(function (s) { return s.courtId === courtId && s.slotId === slotId; });
        return item ? item.price : null;
    }

    function findCourtName(courtId) {
        var c = courtsData.find(function (x) { return x.courtId === courtId; });
        return c ? c.courtName : ('S\u00e2n ' + courtId);
    }

    function findSlot(slotId) {
        return slotsData.find(function (s) { return s.slotId === slotId; }) || null;
    }

    function toggleSlot(courtId, slotId) {
        if (mode === MODE_NORMAL) return;

        var idx = selectedSlots.findIndex(function (s) {
            return s.courtId === courtId && s.slotId === slotId;
        });
        var currentlySelected = idx >= 0;

        var slot = findSlot(slotId);
        if (!slot) return;
        var past = isSlotPast(slot.endTime);

        if (currentlySelected) {
            selectedSlots.splice(idx, 1);
        } else {
            if (past) return;
            if (hasNoPrice(courtId, slotId)) return;

            var key = cellKey(courtId, slotId);
            var cell = cellMapData[key];

            if (mode === MODE_PROXY) {
                if (cell && cell.state !== 'AVAILABLE' && cell.bookingStatus !== 'CANCELLED') return;
            } else if (mode === MODE_EDIT) {
                var ownPending = !!editEditableKeySet[key];
                if (cell && cell.state === 'BOOKED' && !ownPending) return;
                if (cell && cell.state === 'DISABLED') return;
            }

            selectedSlots.push({
                bookingSlotId: editOriginalKeyToBookingSlotId[key] || null,
                courtId: courtId,
                courtName: findCourtName(courtId),
                slotId: slotId,
                startTime: slot.startTime,
                endTime: slot.endTime,
                price: priceMap[key]
            });
        }

        updateBottomBar();
        renderGrid();
    }
    function updateBottomBar() {
        if (mode === MODE_NORMAL) {
            bottomBar.classList.add('d-none');
            return;
        }

        bottomBar.classList.remove('d-none');
        var total = 0;
        selectedSlots.forEach(function (s) { total += Number(s.price || 0); });

        if (mode === MODE_PROXY) {
            bottomCount.textContent = selectedSlots.length + ' slot';
            bottomPrice.textContent = formatMoney(total);
        } else {
            var delta = buildEditDelta();
            bottomCount.textContent = '+' + delta.addSlots.length + ' / -' + delta.removeBookingSlotIds.length + ' slot';
            bottomPrice.textContent = formatMoney(total);
        }
    }


    function buildEditDelta() {
        var selectedKeySet = {};
        selectedSlots.forEach(function (s) {
            selectedKeySet[cellKey(s.courtId, s.slotId)] = true;
        });

        var removeBookingSlotIds = [];
        Object.keys(editOriginalKeyToBookingSlotId).forEach(function (key) {
            if (!selectedKeySet[key]) {
                removeBookingSlotIds.push(editOriginalKeyToBookingSlotId[key]);
            }
        });

        var addSlots = [];
        selectedSlots.forEach(function (s) {
            var key = cellKey(s.courtId, s.slotId);
            if (!editOriginalKeyToBookingSlotId[key]) {
                addSlots.push({
                    courtId: s.courtId,
                    slotId: s.slotId
                });
            }
        });

        return {
            addSlots: addSlots,
            removeBookingSlotIds: removeBookingSlotIds
        };
    }

    function validateSelection() {
        if (!selectedSlots.length) return false;

        var byCourt = {};
        selectedSlots.forEach(function (s) {
            if (!byCourt[s.courtId]) byCourt[s.courtId] = [];
            byCourt[s.courtId].push(s);
        });

        var valid = true;
        Object.keys(byCourt).forEach(function (courtId) {
            var arr = byCourt[courtId].slice().sort(function (a, b) {
                return a.startTime.localeCompare(b.startTime);
            });

            var streak = [arr[0]];
            for (var i = 1; i < arr.length; i++) {
                var prev = arr[i - 1];
                var cur = arr[i];
                if (prev.endTime === cur.startTime) {
                    streak.push(cur);
                } else {
                    if (streak.length < 2) valid = false;
                    streak = [cur];
                }
            }
            if (streak.length < 2) valid = false;
        });

        return valid;
    }

    function goToCreatePage() {
        if (!validateSelection()) {
            uiAlert('Moi phien choi phai co it nhat 2 slot lien tiep tren cung 1 san.', 'Du lieu chua hop le');
            return;
        }

        sessionStorage.setItem('staffBookingSlots', JSON.stringify({ date: currentDate, slots: selectedSlots }));
        window.location.href = CTX + '/staff/booking/create';
    }

    async function saveEditChanges() {
        if (!validateSelection()) {
            uiAlert('M\u1ed7i phi\u00ean ch\u01a1i ph\u1ea3i c\u00f3 \u00edt nh\u1ea5t 2 slot li\u00ean ti\u1ebfp tr\u00ean c\u00f9ng 1 s\u00e2n.', 'D\u1eef li\u1ec7u ch\u01b0a h\u1ee3p l\u1ec7');
            return;
        }

        var delta = buildEditDelta();
        if (delta.addSlots.length === 0 && delta.removeBookingSlotIds.length === 0) {
            uiAlert('Kh\u00f4ng c\u00f3 thay \u0111\u1ed5i \u0111\u1ec3 l\u01b0u.', 'Th\u00f4ng b\u00e1o');
            return;
        }

        var editableCount = Object.keys(editOriginalKeyToBookingSlotId).length;
        var removeAllEditable = editableCount > 0
            && delta.removeBookingSlotIds.length === editableCount
            && delta.addSlots.length === 0;

        var reason = '';
        if (removeAllEditable) {
            var firstConfirm = await uiConfirm('B\u1ea1n s\u1eafp b\u1ecf to\u00e0n b\u1ed9 slot c\u00f2n l\u1ea1i. Booking c\u00f3 th\u1ec3 chuy\u1ec3n sang CANCELLED.', 'X\u00e1c nh\u1eadn thay \u0111\u1ed5i l\u1edbn');
            if (!firstConfirm) return;

            var secondConfirm = await uiConfirm('X\u00e1c nh\u1eadn l\u1ea7n 2: b\u1ea1n ch\u1eafc ch\u1eafn mu\u1ed1n h\u1ee7y to\u00e0n b\u1ed9 slot c\u00f2n l\u1ea1i?', 'X\u00e1c nh\u1eadn l\u1ea7n 2');
            if (!secondConfirm) return;

            reason = await uiPrompt('Nh\u1eadp l\u00fd do h\u1ee7y (b\u1eaft bu\u1ed9c):', '', 'L\u00fd do h\u1ee7y');
            if (reason == null || !reason.trim()) {
                uiAlert('Vui l\u00f2ng nh\u1eadp l\u00fd do h\u1ee7y.', 'Thi\u1ebfu th\u00f4ng tin');
                return;
            }
        } else {
            reason = await uiPrompt('Nh\u1eadp ghi ch\u00fa thay \u0111\u1ed5i (kh\u00f4ng b\u1eaft bu\u1ed9c):', '', 'Ghi ch\u00fa thay \u0111\u1ed5i');
            if (reason == null) return;
        }

        btnContinue.disabled = true;
        btnContinue.innerHTML = '<span class="sbc-spinner"></span>\u0110ang l\u01b0u...';

        try {
            var res = await fetch(CTX + '/api/staff/booking/edit/save', {
                method: 'POST',
                credentials: 'same-origin',
                headers: { 'Content-Type': 'application/json', 'Accept': 'application/json' },
                body: JSON.stringify({
                    bookingId: parseInt(editBookingId, 10),
                    etag: editEtag,
                    addSlots: delta.addSlots,
                    removeBookingSlotIds: delta.removeBookingSlotIds,
                    reason: (reason || '').trim()
                })
            });

            var body = await res.json();
            if (!res.ok || !body.success) {
                if (body && body.data && body.data.currentEtag) {
                    editEtag = body.data.currentEtag;
                }
                await uiAlert((body && body.message) || 'L\u01b0u thay \u0111\u1ed5i th\u1ea5t b\u1ea1i', 'L\u01b0u th\u1ea5t b\u1ea1i');
                return;
            }

            await uiAlert('L\u01b0u thay \u0111\u1ed5i th\u00e0nh c\u00f4ng', 'Th\u00e0nh c\u00f4ng');
            sessionStorage.setItem('staffBookingListDirty', '1');
            window.location.href = CTX + '/staff/booking/detail/' + editBookingId;
        } catch (err) {
            console.error('Save edit error:', err);
            uiAlert('L\u1ed7i k\u1ebft n\u1ed1i. Vui l\u00f2ng th\u1eed l\u1ea1i.', 'L\u1ed7i');
        } finally {
            btnContinue.disabled = false;
            btnContinue.innerHTML = '<i class="bi bi-save me-1"></i>L\u01b0u thay \u0111\u1ed5i';
        }
    }
    function exitMode() {
        if (mode === MODE_EDIT && editBookingId) {
            window.location.href = CTX + '/staff/booking/detail/' + editBookingId;
            return;
        }

        setMode(MODE_NORMAL);
        selectedSlots = [];
        editBookingId = null;
        editEtag = null;
        fetchTimeline(currentDate || todayStr());
    }



    function uiAlert(message, title) {
        if (window.StaffDialog && typeof window.StaffDialog.alert === 'function') {
            return window.StaffDialog.alert({ title: title || 'Thong bao', message: message || '' });
        }
        alert(message || '');
        return Promise.resolve();
    }

    function uiConfirm(message, title) {
        if (window.StaffDialog && typeof window.StaffDialog.confirm === 'function') {
            return window.StaffDialog.confirm({ title: title || 'Xac nhan', message: message || '' });
        }
        return Promise.resolve(confirm(message || ''));
    }

    function uiPrompt(message, defaultValue, title) {
        if (window.StaffDialog && typeof window.StaffDialog.prompt === 'function') {
            return window.StaffDialog.prompt({
                title: title || 'Nhap thong tin',
                message: message || '',
                defaultValue: defaultValue || ''
            });
        }
        return Promise.resolve(prompt(message || '', defaultValue || ''));
    }

    function statusLabel(status) {
        switch (status) {
            case 'PENDING': return 'Ch\u1edd XN';
            case 'CONFIRMED': return '\u0110\u00e3 XN';
            case 'COMPLETED': return 'Xong';
            case 'CANCELLED': return '\u0110\u00e3 h\u1ee7y';
            case 'NO_SHOW': return 'V\u1eafng';
            default: return status || '';
        }
    }

    window.reloadTimeline = function () {
        fetchTimeline(currentDate || todayStr());
    };

    btnToday.addEventListener('click', function () { fetchTimeline(todayStr()); });
    btnTomorrow.addEventListener('click', function () { fetchTimeline(tomorrowStr()); });
    dateInput.addEventListener('change', function () {
        if (this.value) fetchTimeline(this.value);
    });

    btnProxyMode.addEventListener('click', function () {
        setMode(MODE_PROXY);
        selectedSlots = [];
        fetchTimeline(currentDate || todayStr());
    });

    btnProxyCancel.addEventListener('click', exitMode);

    btnClearAll.addEventListener('click', function () {
        if (mode === MODE_EDIT) {
            selectedSlots = cloneSlots(editOriginalSlots);
        } else {
            selectedSlots = [];
        }
        updateBottomBar();
        renderGrid();
    });

    btnContinue.addEventListener('click', function () {
        if (mode === MODE_EDIT) {
            saveEditChanges();
        } else if (mode === MODE_PROXY) {
            goToCreatePage();
        }
    });

    var urlParams = new URLSearchParams(window.location.search);
    var initDate = urlParams.get('date');
    var rawEditBookingId = urlParams.get('editBookingId');

    if (rawEditBookingId && /^\d+$/.test(rawEditBookingId)) {
        editBookingId = parseInt(rawEditBookingId, 10);
        setMode(MODE_EDIT);
    }

    if (initDate && /^\d{4}-\d{2}-\d{2}$/.test(initDate)) {
        fetchTimeline(initDate);
    } else {
        fetchTimeline(todayStr());
    }
})();
