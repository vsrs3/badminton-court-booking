(function () {
    'use strict';

    var CTX = window.ST_CTX || '';

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
    var errorMessage = document.getElementById('errorMessage');
    var inventoryCurrentTime = document.getElementById('inventoryCurrentTime');
    var inventoryCurrentSlot = document.getElementById('inventoryCurrentSlot');
    var inventoryRealtimeHint = document.getElementById('inventoryRealtimeHint');
    var inventoryTableBody = document.getElementById('inventoryTableBody');
    var rentalDetailContext = document.getElementById('rentalDetailContext');
    var rentalDetailModeHint = document.getElementById('rentalDetailModeHint');
    var rentalDetailBody = document.getElementById('rentalDetailBody');
    var rentalDetailEmpty = document.getElementById('rentalDetailEmpty');
    var rentalDetailModalEl = document.getElementById('rentalDetailModal');
    var detailModal = null;
    var liveClockTimerId = null;

    var state = {
        selectedDate: getTodayStr(),
        courts: [],
        slots: [],
        cells: [],
        inventoryItems: [],
        cellMap: Object.create(null),
        modalCellKey: null,
        reopenCellKey: null
    };

    window.loadRentalStatus = loadRentalStatus;

    init();

    function init() {
        if (window.bootstrap && rentalDetailModalEl) {
            detailModal = bootstrap.Modal.getOrCreateInstance(rentalDetailModalEl);
        }

        bindEvents();
        startLiveClock();

        var params = new URLSearchParams(window.location.search);
        var initialDate = normalizeDate(params.get('date')) || getTodayStr();
        loadRentalStatus(initialDate);
    }

    function bindEvents() {
        if (btnToday) {
            btnToday.addEventListener('click', function () {
                loadRentalStatus(getTodayStr());
            });
        }

        if (btnTomorrow) {
            btnTomorrow.addEventListener('click', function () {
                loadRentalStatus(getOffsetDateStr(1));
            });
        }

        if (dateInput) {
            dateInput.addEventListener('change', function () {
                var nextDate = normalizeDate(this.value);
                if (nextDate) {
                    loadRentalStatus(nextDate);
                }
            });
        }

        if (rentalDetailBody) {
            rentalDetailBody.addEventListener('change', handleStatusChange);
        }

        if (rentalDetailModalEl) {
            rentalDetailModalEl.addEventListener('hidden.bs.modal', function () {
                state.modalCellKey = null;
                state.reopenCellKey = null;
            });
        }
    }

    function startLiveClock() {
        renderRealtimePanels();

        if (liveClockTimerId) {
            window.clearInterval(liveClockTimerId);
        }

        liveClockTimerId = window.setInterval(function () {
            renderRealtimePanels();

            if (state.selectedDate === getTodayStr() && gridScroll && !gridScroll.classList.contains('d-none')) {
                renderTimelineGrid();
            }

            refreshOpenModal();
        }, 30000);
    }

    function showState(mode) {
        toggleHidden(stateLoading, mode !== 'loading');
        toggleHidden(stateError, mode !== 'error');
        toggleHidden(stateEmpty, mode !== 'empty');
        toggleHidden(gridScroll, mode !== 'content');
    }

    function loadRentalStatus(dateStr, reopenCellKey) {
        var nextDate = normalizeDate(dateStr) || state.selectedDate || getTodayStr();

        state.selectedDate = nextDate;
        state.reopenCellKey = reopenCellKey || null;

        if (!reopenCellKey) {
            closeDetailModal();
        }

        updateDateControls(nextDate);
        renderRealtimePanels();
        showState('loading');
        renderInventoryLoading();
        updateUrl(nextDate);

        fetch(CTX + '/api/staff/rental/status?date=' + encodeURIComponent(nextDate), {
            credentials: 'same-origin',
            headers: {
                'Accept': 'application/json'
            }
        })
            .then(handleJsonResponse)
            .then(function (body) {
                if (!body.success) {
                    throw new Error(body.message || 'Không thể tải dữ liệu.');
                }
                hydrateState(body.data || {});
                renderRealtimePanels();
                renderInventoryTable();
                renderTimelineGrid();
                reopenModalIfNeeded();
            })
            .catch(function (error) {
                console.error('Rental status load error:', error);
                if (errorMessage) {
                    errorMessage.textContent = error.message || 'Không thể tải dữ liệu.';
                }
                renderRealtimePanels();
                renderInventoryTable();
                showState('error');
                closeDetailModal();
            });
    }

    function hydrateState(data) {
        state.selectedDate = normalizeDate(data.selectedDate) || state.selectedDate || getTodayStr();
        state.courts = Array.isArray(data.courts) ? data.courts : [];
        state.slots = Array.isArray(data.slots) ? data.slots : [];
        state.cells = Array.isArray(data.cells) ? data.cells : [];
        state.inventoryItems = Array.isArray(data.inventoryItems) ? data.inventoryItems : [];
        state.cellMap = Object.create(null);

        state.cells.forEach(function (cell) {
            var normalized = cell || {};
            normalized.status = normalizeStatus(normalized.status);
            normalized.items = Array.isArray(normalized.items) ? normalized.items : [];
            state.cellMap[buildCellKey(normalized.courtId, normalized.slotId)] = normalized;
        });
    }

    function renderTimelineGrid() {
        if (!gridHeaderRow || !gridBody) {
            return;
        }

        gridHeaderRow.innerHTML = '';
        gridBody.innerHTML = '';

        var cornerCell = document.createElement('th');
        cornerCell.className = 'st-grid-corner';
        cornerCell.textContent = 'Sân / Giờ';
        gridHeaderRow.appendChild(cornerCell);

        if (!state.courts.length || !state.slots.length) {
            showState('empty');
            return;
        }

        state.slots.forEach(function (slot) {
            var th = document.createElement('th');
            th.innerHTML = '' +
                '<span class="srs-slot-label">' + esc(slot.startTime || '--') + '</span>' +
                '<span class="srs-slot-sub">' + esc(slot.endTime || '--') + '</span>';
            if (isPastSlot(slot)) {
                th.classList.add('st-th-past');
            }
            gridHeaderRow.appendChild(th);
        });

        state.courts.forEach(function (court) {
            var row = document.createElement('tr');

            var courtCell = document.createElement('td');
            courtCell.className = 'st-court-name';
            courtCell.textContent = court.courtName || ('Sân ' + court.courtId);
            row.appendChild(courtCell);

            state.slots.forEach(function (slot, slotIndex) {
                row.appendChild(buildTimelineCell(court, slot, slotIndex));
            });

            gridBody.appendChild(row);
        });

        showState('content');
    }

    function buildTimelineCell(court, slot, slotIndex) {
        var td = document.createElement('td');
        td.className = 'st-cell srs-cell';

        var inner = document.createElement('div');
        inner.className = 'st-cell-inner srs-cell-inner';

        var cellKey = buildCellKey(court.courtId, slot.slotId);
        var cell = state.cellMap[cellKey];
        var past = isPastSlot(slot);

        if (!cell) {
            inner.classList.add(past ? 'srs-cell-past' : 'srs-cell-empty');
            inner.innerHTML = past
                ? '<span class="srs-cell-placeholder">Đã qua</span>'
                : '<span class="srs-cell-placeholder">Trống</span>';
            td.appendChild(inner);
            return td;
        }

        if (past) {
            inner.classList.add('srs-cell-past');
        } else {
            inner.classList.add('srs-cell-' + normalizeStatus(cell.status).toLowerCase());
            inner.classList.add('srs-cell-clickable');
            appendGroupedClasses(inner, court.courtId, slotIndex, cell);
            inner.setAttribute('role', 'button');
            inner.setAttribute('tabindex', '0');
            inner.addEventListener('click', function () {
                openDetailModal(cellKey);
            });
            inner.addEventListener('keydown', function (event) {
                if (event.key === 'Enter' || event.key === ' ') {
                    event.preventDefault();
                    openDetailModal(cellKey);
                }
            });
        }

        inner.innerHTML = '' +
            '<span class="srs-cell-customer">' + esc(cell.customerName || 'Khách thuê') + '</span>' +
            '<span class="srs-cell-meta">' + esc(buildCellMeta(cell)) + '</span>' +
            '<span class="srs-cell-status">' + esc(statusLabel(cell.status)) + '</span>';

        td.appendChild(inner);
        return td;
    }

    function appendGroupedClasses(inner, courtId, slotIndex, cell) {
        var prevSlot = slotIndex > 0 ? state.slots[slotIndex - 1] : null;
        var nextSlot = slotIndex < state.slots.length - 1 ? state.slots[slotIndex + 1] : null;
        var prevCell = prevSlot ? state.cellMap[buildCellKey(courtId, prevSlot.slotId)] : null;
        var nextCell = nextSlot ? state.cellMap[buildCellKey(courtId, nextSlot.slotId)] : null;

        var samePrev = !!(prevCell && prevCell.customerKey && prevCell.customerKey === cell.customerKey && !isPastSlot(prevSlot));
        var sameNext = !!(nextCell && nextCell.customerKey && nextCell.customerKey === cell.customerKey && !isPastSlot(nextSlot));

        if (!samePrev && !sameNext) {
            return;
        }

        inner.classList.add('srs-cell-grouped');
        if (samePrev) {
            inner.classList.add('srs-cell-group-prev');
        }
        if (sameNext) {
            inner.classList.add('srs-cell-group-next');
        }
    }

    function renderInventoryTable() {
        if (!inventoryTableBody) {
            return;
        }

        var items = state.inventoryItems || [];
        if (!items.length) {
            inventoryTableBody.innerHTML =
                '<tr><td colspan="4" class="text-center text-muted py-4">Không có dữ liệu kho đồ.</td></tr>';
            return;
        }

        inventoryTableBody.innerHTML = items.map(function (item, index) {
            return '' +
                '<tr>' +
                '   <td>' + (index + 1) + '</td>' +
                '   <td>' + esc(item.inventoryName) + '</td>' +
                '   <td>' + formatNumber(item.totalQuantity) + '</td>' +
                '   <td class="srs-stock-available">' + formatNumber(item.availableQuantity) + '</td>' +
                '</tr>';
        }).join('');
    }

    function renderInventoryLoading() {
        if (!inventoryTableBody) {
            return;
        }

        inventoryTableBody.innerHTML =
            '<tr><td colspan="4" class="text-center text-muted py-4">Đang tải dữ liệu kho đồ...</td></tr>';
    }

    function renderRealtimePanels() {
        renderInventoryRealtimeInfo();
        refreshOpenModal();
    }

    function renderInventoryRealtimeInfo() {
        if (inventoryCurrentTime) {
            inventoryCurrentTime.textContent = getCurrentTimeLabel();
        }

        var currentSlot = getCurrentRuntimeSlot();
        if (inventoryCurrentSlot) {
            inventoryCurrentSlot.textContent = currentSlot
                ? (currentSlot.startTime + ' - ' + currentSlot.endTime)
                : 'Hiện tại không nằm trong slot nào';
        }

        if (inventoryRealtimeHint) {
            inventoryRealtimeHint.textContent = buildInventoryRealtimeHint(currentSlot);
        }
    }

    function buildInventoryRealtimeHint(currentSlot) {
        if (state.selectedDate === getTodayStr()) {
            if (currentSlot) {
                return 'Slot hiện tại ' + currentSlot.startTime + ' - ' + currentSlot.endTime +
                    ' của hôm nay có thể cập nhật trạng thái thuê đồ.';
            }
            return 'Hiện tại không nằm trong slot nào nên bạn chỉ có thể xem thông tin thuê đồ.';
        }

        if (state.selectedDate > getTodayStr()) {
            return 'Ngày đang xem nằm trong tương lai nên chỉ có thể xem thông tin thuê đồ.';
        }

        return 'Ngày đang xem đã qua nên chỉ có thể xem lại thông tin thuê đồ.';
    }

    function openDetailModal(cellKey) {
        var cell = state.cellMap[cellKey];
        if (!cell) {
            return;
        }

        var slot = findSlot(cell.slotId);
        if (!slot || isPastSlot(slot)) {
            return;
        }

        state.modalCellKey = cellKey;
        state.reopenCellKey = cellKey;

        renderDetailContext(cell, slot);
        renderDetailItems(cell, slot);

        if (detailModal) {
            detailModal.show();
            return;
        }

        if (rentalDetailModalEl) {
            rentalDetailModalEl.classList.add('show');
            rentalDetailModalEl.style.display = 'block';
            rentalDetailModalEl.removeAttribute('aria-hidden');
        }
    }

    function renderDetailContext(cell, slot) {
        if (!rentalDetailContext) {
            return;
        }

        var court = findCourt(cell.courtId);
        rentalDetailContext.innerHTML = '' +
            '<strong>' + esc(court ? court.courtName : ('Sân ' + cell.courtId)) + '</strong>' +
            '<span>' + esc(slot.startTime + ' - ' + slot.endTime) + '</span>' +
            '<span>' + esc(formatDisplayDate(state.selectedDate)) + '</span>' +
            '<span>' + esc(cell.customerName || 'Khách thuê') + '</span>';
    }

    function closeDetailModal() {
        state.modalCellKey = null;

        if (detailModal && rentalDetailModalEl && rentalDetailModalEl.classList.contains('show')) {
            detailModal.hide();
            return;
        }

        if (rentalDetailModalEl) {
            rentalDetailModalEl.classList.remove('show');
            rentalDetailModalEl.style.display = 'none';
            rentalDetailModalEl.setAttribute('aria-hidden', 'true');
        }
    }

    function renderDetailItems(cell, slot) {
        if (!rentalDetailBody || !rentalDetailEmpty) {
            return;
        }

        var items = Array.isArray(cell.items) ? cell.items : [];
        var editable = canEditSlot(slot);

        renderDetailModeHint(slot, editable);

        if (!items.length) {
            rentalDetailBody.innerHTML = '';
            rentalDetailEmpty.classList.remove('d-none');
            return;
        }

        rentalDetailEmpty.classList.add('d-none');
        rentalDetailBody.innerHTML = items.map(function (item) {
            var status = normalizeStatus(item.status);
            return '' +
                '<tr data-schedule-id="' + Number(item.scheduleId || 0) + '">' +
                '   <td>' +
                '       <div class="srs-detail-name">' + esc(item.inventoryName || '') + '</div>' +
                '   </td>' +
                '   <td>' + formatNumber(item.quantity) + '</td>' +
                '   <td>' + buildStatusControl(item.scheduleId, status, editable) + '</td>' +
                '</tr>';
        }).join('');
    }

    function renderDetailModeHint(slot, editable) {
        if (!rentalDetailModeHint) {
            return;
        }

        if (editable) {
            rentalDetailModeHint.classList.add('d-none');
            rentalDetailModeHint.textContent = '';
            return;
        }

        rentalDetailModeHint.classList.remove('d-none');
        rentalDetailModeHint.textContent = buildReadonlyMessage(slot);
    }

    function buildStatusControl(scheduleId, status, editable) {
        var normalized = normalizeStatus(status);
        if (!editable) {
            return buildReadonlyStatusBadge(normalized);
        }

        return '' +
            '<select class="form-select form-select-sm srs-status-select srs-status-select-' + normalized.toLowerCase() + '"' +
            ' data-role="detail-status"' +
            ' data-schedule-id="' + Number(scheduleId || 0) + '"' +
            ' data-prev-status="' + normalized + '">' +
            '   <option value="RENTED"' + (normalized === 'RENTED' ? ' selected' : '') + '>Đã thuê</option>' +
            '   <option value="RENTING"' + (normalized === 'RENTING' ? ' selected' : '') + '>Đang thuê</option>' +
            '   <option value="RETURNED"' + (normalized === 'RETURNED' ? ' selected' : '') + '>Đã trả</option>' +
            '</select>';
    }

    function buildReadonlyStatusBadge(status) {
        var normalized = normalizeStatus(status);
        return '' +
            '<span class="srs-status-badge srs-status-badge-' + normalized.toLowerCase() + '">' +
            esc(statusLabel(normalized)) +
            '</span>';
    }

    function handleStatusChange(event) {
        var select = event.target.closest('[data-role="detail-status"]');
        if (!select) {
            return;
        }

        var modalCell = state.modalCellKey ? state.cellMap[state.modalCellKey] : null;
        var modalSlot = modalCell ? findSlot(modalCell.slotId) : null;
        if (!canEditSlot(modalSlot)) {
            var lockedStatus = normalizeStatus(select.getAttribute('data-prev-status'));
            select.value = lockedStatus;
            refreshStatusSelectClass(select, lockedStatus);
            window.alert(buildReadonlyMessage(modalSlot));
            return;
        }

        var scheduleId = Number(select.getAttribute('data-schedule-id') || 0);
        var previousStatus = normalizeStatus(select.getAttribute('data-prev-status'));
        var nextStatus = normalizeStatus(select.value);

        if (!scheduleId) {
            select.value = previousStatus;
            refreshStatusSelectClass(select, previousStatus);
            return;
        }

        if (previousStatus === nextStatus) {
            refreshStatusSelectClass(select, nextStatus);
            return;
        }

        select.disabled = true;
        var row = select.closest('tr');
        if (row) {
            row.classList.add('srs-row-updating');
        }

        fetch(CTX + '/api/staff/rental/status', {
            method: 'POST',
            credentials: 'same-origin',
            headers: {
                'Accept': 'application/json',
                'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8'
            },
            body: new URLSearchParams({
                scheduleId: String(scheduleId),
                status: nextStatus
            }).toString()
        })
            .then(handleJsonResponse)
            .then(function (body) {
                if (!body.success) {
                    throw new Error(body.message || 'Không thể cập nhật trạng thái.');
                }

                select.setAttribute('data-prev-status', nextStatus);
                refreshStatusSelectClass(select, nextStatus);
                loadRentalStatus(state.selectedDate, state.modalCellKey);
            })
            .catch(function (error) {
                console.error('Rental status update error:', error);
                select.value = previousStatus;
                refreshStatusSelectClass(select, previousStatus);
                window.alert(error.message || 'Không thể cập nhật trạng thái.');
            })
            .finally(function () {
                select.disabled = false;
                if (row) {
                    row.classList.remove('srs-row-updating');
                }
            });
    }

    function reopenModalIfNeeded() {
        if (!state.reopenCellKey) {
            return;
        }

        var cellKey = state.reopenCellKey;
        state.reopenCellKey = null;

        var cell = state.cellMap[cellKey];
        var slot = cell ? findSlot(cell.slotId) : null;
        if (!cell || !slot || isPastSlot(slot)) {
            closeDetailModal();
            return;
        }

        openDetailModal(cellKey);
    }

    function refreshOpenModal() {
        if (!state.modalCellKey) {
            return;
        }

        var cell = state.cellMap[state.modalCellKey];
        var slot = cell ? findSlot(cell.slotId) : null;
        if (!cell || !slot || isPastSlot(slot)) {
            closeDetailModal();
            return;
        }

        renderDetailContext(cell, slot);
        renderDetailItems(cell, slot);
    }

    function refreshStatusSelectClass(select, status) {
        select.classList.remove(
            'srs-status-select-rented',
            'srs-status-select-renting',
            'srs-status-select-returned'
        );
        select.classList.add('srs-status-select-' + normalizeStatus(status).toLowerCase());
    }

    function canEditSlot(slot) {
        if (!slot || state.selectedDate !== getTodayStr()) {
            return false;
        }

        var currentSlot = getCurrentRuntimeSlot();
        return !!currentSlot && Number(currentSlot.slotId) === Number(slot.slotId);
    }

    function buildReadonlyMessage(slot) {
        if (state.selectedDate > getTodayStr()) {
            return 'Ngày được chọn nằm trong tương lai nên bạn chỉ có thể xem thông tin thuê đồ.';
        }

        if (state.selectedDate < getTodayStr()) {
            return 'Ngày được chọn đã qua nên bạn chỉ có thể xem lại thông tin thuê đồ.';
        }

        var currentSlot = getCurrentRuntimeSlot();
        if (!currentSlot) {
            return 'Hiện tại không nằm trong slot nào nên bạn chỉ có thể xem thông tin thuê đồ.';
        }

        if (!slot || Number(slot.slotId) !== Number(currentSlot.slotId)) {
            return 'Chỉ slot hiện tại ' + currentSlot.startTime + ' - ' + currentSlot.endTime +
                ' của hôm nay mới được cập nhật trạng thái.';
        }

        return '';
    }

    function getCurrentRuntimeSlot() {
        if (!Array.isArray(state.slots) || !state.slots.length) {
            return null;
        }

        var nowMinutes = getCurrentMinutes();
        for (var i = 0; i < state.slots.length; i++) {
            var slot = state.slots[i];
            var startMinutes = toMinutes(slot.startTime);
            var endMinutes = toMinutes(slot.endTime);
            if (startMinutes == null || endMinutes == null) {
                continue;
            }
            if (nowMinutes >= startMinutes && nowMinutes < endMinutes) {
                return slot;
            }
        }

        return null;
    }

    function buildCellMeta(cell) {
        var itemCount = Number(cell.itemCount || 0);
        var totalQuantity = Number(cell.totalQuantity || 0);
        return itemCount + ' đồ • SL ' + totalQuantity;
    }

    function statusLabel(status) {
        switch (normalizeStatus(status)) {
            case 'RENTING':
                return 'Đang thuê';
            case 'RETURNED':
                return 'Đã trả';
            default:
                return 'Đã thuê';
        }
    }

    function buildCellKey(courtId, slotId) {
        return String(courtId) + '-' + String(slotId);
    }

    function findCourt(courtId) {
        for (var i = 0; i < state.courts.length; i++) {
            if (Number(state.courts[i].courtId) === Number(courtId)) {
                return state.courts[i];
            }
        }
        return null;
    }

    function findSlot(slotId) {
        for (var i = 0; i < state.slots.length; i++) {
            if (Number(state.slots[i].slotId) === Number(slotId)) {
                return state.slots[i];
            }
        }
        return null;
    }

    function isPastSlot(slot) {
        if (!slot) {
            return false;
        }

        var todayStr = getTodayStr();
        if (state.selectedDate < todayStr) {
            return true;
        }
        if (state.selectedDate > todayStr) {
            return false;
        }

        var slotEndMinutes = toMinutes(slot.endTime);
        if (slotEndMinutes == null) {
            return false;
        }

        return getCurrentMinutes() >= slotEndMinutes;
    }

    function updateDateControls(dateStr) {
        if (btnToday) {
            btnToday.classList.toggle('active', dateStr === getTodayStr());
        }
        if (btnTomorrow) {
            btnTomorrow.classList.toggle('active', dateStr === getOffsetDateStr(1));
        }
        if (dateInput) {
            dateInput.value = dateStr;
        }
        if (dateDisplay) {
            dateDisplay.textContent = formatDisplayDate(dateStr);
        }
    }

    function updateUrl(dateStr) {
        var params = new URLSearchParams();
        params.set('date', dateStr);
        history.replaceState(null, '', window.location.pathname + '?' + params.toString());
    }

    function handleJsonResponse(response) {
        if (!response.ok) {
            return response.json()
                .catch(function () {
                    return null;
                })
                .then(function (body) {
                    throw new Error((body && body.message) || ('HTTP ' + response.status));
                });
        }

        return response.json();
    }

    function normalizeStatus(status) {
        var value = String(status || 'RENTED').toUpperCase();
        if (value !== 'RENTED' && value !== 'RENTING' && value !== 'RETURNED') {
            return 'RENTED';
        }
        return value;
    }

    function normalizeDate(value) {
        if (!value || !/^\d{4}-\d{2}-\d{2}$/.test(String(value))) {
            return null;
        }
        return String(value);
    }

    function getOffsetDateStr(offsetDays) {
        var date = new Date();
        date.setDate(date.getDate() + Number(offsetDays || 0));
        return formatDateInput(date);
    }

    function getTodayStr() {
        return formatDateInput(new Date());
    }

    function getCurrentMinutes() {
        var now = new Date();
        return (now.getHours() * 60) + now.getMinutes();
    }

    function getCurrentTimeLabel() {
        var now = new Date();
        return String(now.getHours()).padStart(2, '0') + ':' + String(now.getMinutes()).padStart(2, '0');
    }

    function toMinutes(timeStr) {
        var parts = String(timeStr || '').split(':');
        if (parts.length < 2) {
            return null;
        }
        return (Number(parts[0]) * 60) + Number(parts[1]);
    }

    function formatDateInput(date) {
        return date.getFullYear() + '-' +
            String(date.getMonth() + 1).padStart(2, '0') + '-' +
            String(date.getDate()).padStart(2, '0');
    }

    function formatDisplayDate(dateStr) {
        var date = new Date(dateStr + 'T00:00:00');
        var weekdays = ['Chủ nhật', 'Thứ 2', 'Thứ 3', 'Thứ 4', 'Thứ 5', 'Thứ 6', 'Thứ 7'];
        return weekdays[date.getDay()] + ', ' +
            String(date.getDate()).padStart(2, '0') + '/' +
            String(date.getMonth() + 1).padStart(2, '0') + '/' +
            date.getFullYear();
    }

    function formatNumber(value) {
        var number = Number(value || 0);
        return Number.isFinite(number) ? number.toLocaleString('vi-VN') : '0';
    }

    function toggleHidden(element, hidden) {
        if (!element) {
            return;
        }
        element.classList.toggle('d-none', !!hidden);
    }

    function esc(value) {
        var div = document.createElement('div');
        div.textContent = value == null ? '' : String(value);
        return div.innerHTML;
    }
})();
