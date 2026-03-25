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
    var screenAlert = document.getElementById('rentalStatusScreenAlert');
    var inventoryCurrentTime = document.getElementById('inventoryCurrentTime');
    var inventoryCurrentSlot = document.getElementById('inventoryCurrentSlot');
    var inventoryToolbar = document.querySelector('.srs-inventory-toolbar');
    var inventorySearchInput = document.getElementById('inventorySearchInput');
    var inventorySearchButton = document.getElementById('inventorySearchButton');
    var inventoryTableBody = document.getElementById('inventoryTableBody');
    var inventoryPagination = document.getElementById('inventoryPagination');
    var inventorySuggestionMenu = null;
    var rentalDetailContext = document.getElementById('rentalDetailContext');
    var rentalDetailModeHint = document.getElementById('rentalDetailModeHint');
    var rentalDetailBody = document.getElementById('rentalDetailBody');
    var rentalDetailEmpty = document.getElementById('rentalDetailEmpty');
    var rentalDetailBulkWrap = document.getElementById('rentalDetailBulkWrap');
    var rentalDetailBulkSelect = document.getElementById('rentalDetailBulkSelect');
    var rentalDetailModalEl = document.getElementById('rentalDetailModal');
    var detailModal = null;
    var liveClockTimerId = null;
    var screenAlertTimerId = null;

    var state = {
        selectedDate: getTodayStr(),
        courts: [],
        slots: [],
        cells: [],
        inventoryItems: [],
        inventorySuggestionItems: [],
        inventorySuggestionLimit: 50,
        inventorySuggestionRequested: false,
        inventorySuggestionLoaded: false,
        inventorySearchTerm: '',
        inventoryCurrentPage: 1,
        inventoryPageSize: 5,
        inventoryTotal: 0,
        inventoryTotalPages: 0,
        cellMap: Object.create(null),
        modalCellKey: null,
        reopenCellKey: null,
        lastScreenNoticeKey: null
    };

    window.loadRentalStatus = loadRentalStatus;

    init();

    function init() {
        if (window.bootstrap && rentalDetailModalEl) {
            detailModal = bootstrap.Modal.getOrCreateInstance(rentalDetailModalEl);
        }

        setupInventorySearchUi();
        bindEvents();
        startLiveClock();
        loadInventorySuggestionData();

        var params = new URLSearchParams(window.location.search);
        var initialDate = normalizeDate(params.get('date')) || getTodayStr();
        loadRentalStatus(initialDate);
    }

    function setupInventorySearchUi() {
        if (!inventorySearchInput) {
            return;
        }

        inventorySearchInput.setAttribute('autocomplete', 'off');

        if (inventoryToolbar) {
            var note = inventoryToolbar.querySelector('.srs-inventory-toolbar-note');
            if (!note) {
                note = document.createElement('div');
                note.className = 'srs-inventory-toolbar-note';
                note.textContent = 'Gợi ý lấy trong ' + state.inventorySuggestionLimit +
                    ' dữ liệu đầu tiên. Bấm tìm kiếm để tra toàn bộ dữ liệu.';
                inventoryToolbar.appendChild(note);
            }
        }

        if (inventoryToolbar) {
            var legacyNote = inventoryToolbar.querySelector('.srs-inventory-toolbar-note');
            if (legacyNote) {
                legacyNote.remove();
            }
        }

        if (inventorySearchInput.parentElement) {
            inventorySuggestionMenu = document.getElementById('inventorySuggestionMenu');

            if (!inventorySuggestionMenu) {
                inventorySuggestionMenu = document.createElement('div');
                inventorySuggestionMenu.id = 'inventorySuggestionMenu';
                inventorySuggestionMenu.className = 'search-suggestion-menu';
                inventorySearchInput.parentElement.appendChild(inventorySuggestionMenu);
            }
        }

        if (inventorySearchButton) {
            inventorySearchButton.classList.add('srs-search-button');
        }
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

        if (inventorySearchInput) {
            inventorySearchInput.addEventListener('keydown', function (event) {
                if (event.key === 'Escape') {
                    hideInventorySuggestionMenu();
                    return;
                }

                if (event.key === 'Enter') {
                    event.preventDefault();
                    submitInventorySearch();
                }
            });

            inventorySearchInput.addEventListener('input', function () {
                refreshInventorySuggestionMenu();
            });

            inventorySearchInput.addEventListener('focus', function () {
                refreshInventorySuggestionMenu();
            });
        }

        if (inventorySearchButton) {
            inventorySearchButton.addEventListener('click', function () {
                submitInventorySearch();
            });
        }

        if (inventoryPagination) {
            inventoryPagination.addEventListener('click', function (event) {
                var button = event.target.closest('[data-role="inventory-page"]');
                if (!button || button.disabled) {
                    return;
                }

                var nextPage = Number(button.getAttribute('data-page') || 0);
                if (!nextPage || nextPage === state.inventoryCurrentPage) {
                    return;
                }

                state.inventoryCurrentPage = nextPage;
                loadInventoryTableData(nextPage);
            });
        }

        document.addEventListener('click', function (event) {
            if (!inventorySuggestionMenu || !inventorySearchInput || !inventorySearchInput.parentElement) {
                return;
            }

            if (inventorySearchInput.parentElement.contains(event.target)) {
                return;
            }

            hideInventorySuggestionMenu();
        });

        if (rentalDetailBody) {
            rentalDetailBody.addEventListener('change', handleStatusChange);
        }

        if (rentalDetailBulkSelect) {
            rentalDetailBulkSelect.addEventListener('change', handleBulkStatusChange);
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
                maybeShowCurrentSlotNotice();
                renderTimelineGrid();
                reopenModalIfNeeded();
                loadInventoryTableData(state.inventoryCurrentPage);
            })
            .catch(function (error) {
                console.error('Rental status load error:', error);
                if (errorMessage) {
                    errorMessage.textContent = error.message || 'Không thể tải dữ liệu.';
                }
                renderRealtimePanels();
                loadInventoryTableData(state.inventoryCurrentPage);
                showState('error');
                closeDetailModal();
            });
    }

    function hydrateState(data) {
        state.selectedDate = normalizeDate(data.selectedDate) || state.selectedDate || getTodayStr();
        state.courts = Array.isArray(data.courts) ? data.courts : [];
        state.slots = Array.isArray(data.slots) ? data.slots : [];
        state.cells = Array.isArray(data.cells) ? data.cells : [];
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

    function loadInventorySuggestionData() {
        if (state.inventorySuggestionLoaded || state.inventorySuggestionRequested) {
            return;
        }

        state.inventorySuggestionRequested = true;

        var params = new URLSearchParams();
        params.set('page', '1');
        params.set('pageSize', String(state.inventorySuggestionLimit));

        fetch(CTX + '/api/staff/rental/inventory?' + params.toString(), {
            credentials: 'same-origin',
            headers: {
                'Accept': 'application/json'
            }
        })
            .then(handleJsonResponse)
            .then(function (body) {
                if (!body.success) {
                    throw new Error(body.message || 'Không thể tải gợi ý kho đồ.');
                }

                var data = body.data || {};
                var items = Array.isArray(data.items) ? data.items.map(mapInventoryItem) : [];
                state.inventorySuggestionItems = dedupeInventorySuggestionItems(items);
                state.inventorySuggestionLoaded = true;

                if (document.activeElement === inventorySearchInput) {
                    refreshInventorySuggestionMenu();
                }
            })
            .catch(function (error) {
                console.error('Inventory suggestion load error:', error);
            })
            .finally(function () {
                state.inventorySuggestionRequested = false;
            });
    }

    function refreshInventorySuggestionMenu() {
        if (!inventorySearchInput) {
            return;
        }

        var keyword = normalizeSearchText(inventorySearchInput.value);
        if (!keyword) {
            hideInventorySuggestionMenu();
            return;
        }

        loadInventorySuggestionData();

        if (!state.inventorySuggestionLoaded) {
            hideInventorySuggestionMenu();
            return;
        }

        var matchedItems = (state.inventorySuggestionItems || []).filter(function (item) {
            var searchValue = [item.inventoryName, item.brand, item.description]
                .filter(Boolean)
                .join(' ');

            return normalizeSearchText(searchValue).includes(keyword);
        }).slice(0, 8);

        renderInventorySuggestionMenu(matchedItems);
    }

    function hideInventorySuggestionMenu() {
        if (!inventorySuggestionMenu) {
            return;
        }

        inventorySuggestionMenu.classList.remove('is-visible');
        inventorySuggestionMenu.innerHTML = '';
    }

    function renderInventorySuggestionMenu(items) {
        if (!inventorySuggestionMenu || !inventorySearchInput) {
            return;
        }

        inventorySuggestionMenu.innerHTML = '';

        if (!items.length) {
            var emptyState = document.createElement('div');
            emptyState.className = 'search-suggestion-empty';
            emptyState.textContent = 'Không có gợi ý trong 50 dữ liệu đầu tiên. Bấm Tìm kiếm để tra toàn bộ dữ liệu.';
            emptyState.textContent = 'Khong tim thay do phu hop.';
            inventorySuggestionMenu.appendChild(emptyState);
            inventorySuggestionMenu.classList.add('is-visible');
            return;
        }

        items.forEach(function (item) {
            var button = document.createElement('button');
            var title = document.createElement('span');

            button.type = 'button';
            button.className = 'search-suggestion-item';

            title.className = 'search-suggestion-title';
            title.textContent = item.inventoryName;
            button.appendChild(title);

            if (item.brand) {
                var meta = document.createElement('span');
                meta.className = 'search-suggestion-meta';
                meta.textContent = item.brand;
                button.appendChild(meta);
            }

            button.addEventListener('mousedown', function (event) {
                event.preventDefault();
                inventorySearchInput.value = item.inventoryName;
                hideInventorySuggestionMenu();
                inventorySearchInput.focus();
            });

            inventorySuggestionMenu.appendChild(button);
        });

        inventorySuggestionMenu.classList.add('is-visible');
    }

    function submitInventorySearch() {
        hideInventorySuggestionMenu();
        state.inventorySearchTerm = inventorySearchInput ? String(inventorySearchInput.value || '').trim() : '';
        state.inventoryCurrentPage = 1;
        loadInventoryTableData(1);
    }

    function loadInventoryTableData(page) {
        var nextPage = Math.max(1, Number(page || 1));
        state.inventoryCurrentPage = nextPage;
        state.inventorySearchTerm = inventorySearchInput ? String(inventorySearchInput.value || '').trim() : state.inventorySearchTerm;

        renderInventoryLoading();

        var params = new URLSearchParams();
        params.set('page', String(nextPage));
        if (state.inventorySearchTerm) {
            params.set('q', state.inventorySearchTerm);
        }

        fetch(CTX + '/api/staff/rental/inventory?' + params.toString(), {
            credentials: 'same-origin',
            headers: {
                'Accept': 'application/json'
            }
        })
            .then(handleJsonResponse)
            .then(function (body) {
                if (!body.success) {
                    throw new Error(body.message || 'Không thể tải danh sách kho đồ.');
                }

                var data = body.data || {};
                state.inventoryItems = Array.isArray(data.items) ? data.items.map(mapInventoryItem) : [];
                state.inventoryCurrentPage = Number(data.page || nextPage);
                state.inventoryPageSize = Number(data.pageSize || state.inventoryPageSize || 5);
                state.inventoryTotal = Number(data.total || 0);
                state.inventoryTotalPages = Number(data.totalPages || 0);

                renderInventoryTable();
            })
            .catch(function (error) {
                console.error('Inventory load error:', error);
                renderInventoryError(error.message || 'Không thể tải danh sách kho đồ.');
            });
    }

    function mapInventoryItem(item) {
        return {
            facilityInventoryId: Number(item.facilityInventoryId || 0),
            inventoryId: Number(item.inventoryId || 0),
            inventoryName: item.name || '',
            brand: item.brand || '',
            description: item.description || '',
            totalQuantity: Number(item.totalQuantity || 0),
            availableQuantity: Number(item.availableQuantity || 0)
        };
    }

    function renderInventoryTable() {
        if (!inventoryTableBody) {
            return;
        }

        var totalItems = Number(state.inventoryTotal || 0);
        var totalPages = Number(state.inventoryTotalPages || 0);

        if (!state.inventoryItems.length) {
            inventoryTableBody.innerHTML =
                '<tr><td colspan="4" class="text-center text-muted py-4">' +
                (state.inventorySearchTerm
                    ? 'Không tìm thấy đồ nào phù hợp với từ khóa.'
                    : 'Không có dữ liệu kho đồ.') +
                '</td></tr>';
            renderInventoryPagination(totalItems, totalPages, 0, 0);
            return;
        }

        var startIndex = (state.inventoryCurrentPage - 1) * state.inventoryPageSize;

        inventoryTableBody.innerHTML = state.inventoryItems.map(function (item, index) {
            return '' +
                '<tr>' +
                '   <td>' + (startIndex + index + 1) + '</td>' +
                '   <td>' + esc(item.inventoryName) + '</td>' +
                '   <td>' + formatNumber(item.totalQuantity) + '</td>' +
                '   <td class="srs-stock-available">' + formatNumber(item.availableQuantity) + '</td>' +
                '</tr>';
        }).join('');

        renderInventoryPagination(
            totalItems,
            totalPages,
            startIndex + 1,
            startIndex + state.inventoryItems.length
        );
    }

    function renderInventoryPagination(totalItems, totalPages, fromItem, toItem) {
        if (!inventoryPagination) {
            return;
        }

        if (!totalItems || totalPages <= 1) {
            inventoryPagination.innerHTML = '';
            return;
        }

        var current = Math.min(Math.max(state.inventoryCurrentPage, 1), totalPages);
        var html = '<nav><ul class="pagination justify-content-center align-items-center gap-2 compact-pagination mb-0">';

        html += '<li class="page-item ' + (current === 1 ? 'disabled' : '') + '">';
        html += current === 1
            ? '<span class="page-link-static" aria-label="Trang trước"><i class="bi bi-chevron-left"></i></span>'
            : '<button type="button" class="page-link" data-role="inventory-page" data-page="' + (current - 1) + '" aria-label="Trang trước"><i class="bi bi-chevron-left"></i></button>';
        html += '</li>';

        html += '<li class="page-item active"><span class="page-link-static">' + current + '</span></li>';

        if (current + 1 < totalPages) {
            html += '<li class="page-item disabled"><span class="page-link-static pagination-ellipsis">...</span></li>';
        }

        if (current < totalPages) {
            html += '<li class="page-item"><button type="button" class="page-link" data-role="inventory-page" data-page="' + totalPages + '">' + totalPages + '</button></li>';
        }

        html += '<li class="page-item ' + (current === totalPages ? 'disabled' : '') + '">';
        html += current === totalPages
            ? '<span class="page-link-static" aria-label="Trang sau"><i class="bi bi-chevron-right"></i></span>'
            : '<button type="button" class="page-link" data-role="inventory-page" data-page="' + (current + 1) + '" aria-label="Trang sau"><i class="bi bi-chevron-right"></i></button>';
        html += '</li>';

        html += '</ul></nav>';
        inventoryPagination.innerHTML = html;
        return;

        if (!totalItems) {
            inventoryPagination.innerHTML = '';
            return;
        }

        var pages = buildPageWindow(totalPages, state.inventoryCurrentPage);
        var buttons = [];

        buttons.push(buildPageButton('Trước', state.inventoryCurrentPage - 1, state.inventoryCurrentPage === 1, false));
        for (var i = 0; i < pages.length; i++) {
            buttons.push(buildPageButton(String(pages[i]), pages[i], false, pages[i] === state.inventoryCurrentPage));
        }
        buttons.push(buildPageButton('Sau', state.inventoryCurrentPage + 1, state.inventoryCurrentPage === totalPages, false));

        inventoryPagination.innerHTML = '' +
            '<div class="srs-inventory-page-info">Hiển thị ' + fromItem + ' - ' + toItem + ' trên ' + totalItems + ' đồ</div>' +
            '<div class="srs-inventory-page-list">' + buttons.join('') + '</div>';
    }

    function buildPageWindow(totalPages, currentPage) {
        var pages = [];
        if (totalPages <= 5) {
            for (var i = 1; i <= totalPages; i++) {
                pages.push(i);
            }
            return pages;
        }

        var start = Math.max(1, currentPage - 2);
        var end = Math.min(totalPages, start + 4);
        start = Math.max(1, end - 4);

        for (var page = start; page <= end; page++) {
            pages.push(page);
        }
        return pages;
    }

    function buildPageButton(label, page, disabled, active) {
        return '' +
            '<button type="button"' +
            ' class="srs-inventory-page-btn' + (active ? ' active' : '') + '"' +
            ' data-role="inventory-page"' +
            ' data-page="' + page + '"' +
            (disabled ? ' disabled' : '') + '>' +
            esc(label) +
            '</button>';
    }

    function renderInventoryLoading() {
        if (!inventoryTableBody) {
            return;
        }

        inventoryTableBody.innerHTML =
            '<tr><td colspan="4" class="text-center text-muted py-4">Đang tải dữ liệu kho đồ...</td></tr>';

        if (inventoryPagination) {
            inventoryPagination.innerHTML = '';
        }
    }

    function renderInventoryError(message) {
        if (!inventoryTableBody) {
            return;
        }

        inventoryTableBody.innerHTML =
            '<tr><td colspan="4" class="text-center text-danger py-4">' + esc(message) + '</td></tr>';

        if (inventoryPagination) {
            inventoryPagination.innerHTML = '';
        }
    }

    function renderRealtimePanels() {
        if (inventoryCurrentTime) {
            inventoryCurrentTime.textContent = getCurrentTimeLabel();
        }

        var currentSlot = getCurrentRuntimeSlot();
        if (inventoryCurrentSlot) {
            inventoryCurrentSlot.textContent = currentSlot
                ? (currentSlot.startTime + ' - ' + currentSlot.endTime)
                : 'Slot không khả dụng';
        }

        refreshOpenModal();
    }

    function maybeShowCurrentSlotNotice() {
        var currentSlot = getCurrentRuntimeSlot();
        var noticeKey = '';

        if (state.selectedDate === getTodayStr() && !currentSlot) {
            noticeKey = 'today-no-slot-' + state.selectedDate;
            if (state.lastScreenNoticeKey !== noticeKey) {
                state.lastScreenNoticeKey = noticeKey;
                showScreenAlert('Hiện tại không nằm trong slot nào nên bạn chỉ có thể xem thông tin thuê đồ.');
            }
            return;
        }

        if (state.selectedDate !== getTodayStr()) {
            state.lastScreenNoticeKey = 'view-only-' + state.selectedDate;
            return;
        }

        state.lastScreenNoticeKey = '';
    }

    function showScreenAlert(message) {
        if (!screenAlert) {
            return;
        }

        screenAlert.textContent = message;
        screenAlert.classList.remove('d-none');

        if (screenAlertTimerId) {
            window.clearTimeout(screenAlertTimerId);
        }

        screenAlertTimerId = window.setTimeout(function () {
            screenAlert.classList.add('d-none');
        }, 10000);
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
            renderBulkStatusControl([], editable);
            return;
        }

        rentalDetailEmpty.classList.add('d-none');
        rentalDetailBody.innerHTML = items.map(function (item) {
            var status = normalizeStatus(item.status);
            return '' +
                '<tr data-schedule-id="' + Number(item.scheduleId || 0) + '">' +
                '   <td><div class="srs-detail-name">' + esc(item.inventoryName || '') + '</div></td>' +
                '   <td>' + formatNumber(item.quantity) + '</td>' +
                '   <td>' + buildStatusControl(item.scheduleId, status, editable) + '</td>' +
                '</tr>';
        }).join('');
        renderBulkStatusControl(items, editable);
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

    function renderBulkStatusControl(items, editable) {
        if (!rentalDetailBulkWrap || !rentalDetailBulkSelect) {
            return;
        }

        var hasItems = Array.isArray(items) && items.length > 0;
        rentalDetailBulkWrap.classList.toggle('d-none', !hasItems);

        if (!hasItems) {
            rentalDetailBulkSelect.value = '';
            rentalDetailBulkSelect.disabled = true;
            rentalDetailBulkSelect.setAttribute('data-prev-status', '');
            refreshBulkStatusSelectClass('');
            return;
        }

        var bulkStatus = resolveBulkStatusValue(items);
        rentalDetailBulkSelect.disabled = !editable;
        rentalDetailBulkSelect.value = bulkStatus;
        rentalDetailBulkSelect.setAttribute('data-prev-status', bulkStatus);
        refreshBulkStatusSelectClass(bulkStatus);
    }

    function resolveBulkStatusValue(items) {
        if (!Array.isArray(items) || !items.length) {
            return '';
        }

        var firstStatus = normalizeStatus(items[0].status);
        for (var i = 1; i < items.length; i++) {
            if (normalizeStatus(items[i].status) !== firstStatus) {
                return '';
            }
        }
        return firstStatus;
    }

    function refreshBulkStatusSelectClass(status) {
        if (!rentalDetailBulkSelect) {
            return;
        }

        rentalDetailBulkSelect.classList.remove(
            'srs-status-select-rented',
            'srs-status-select-renting',
            'srs-status-select-returned'
        );

        if (status) {
            rentalDetailBulkSelect.classList.add('srs-status-select-' + normalizeStatus(status).toLowerCase());
        }
    }

    function setDetailRowsBusy(busy) {
        if (!rentalDetailBody) {
            return [];
        }

        var selects = Array.prototype.slice.call(
            rentalDetailBody.querySelectorAll('[data-role="detail-status"]')
        );

        selects.forEach(function (select) {
            select.disabled = !!busy;
            var row = select.closest('tr');
            if (row) {
                row.classList.toggle('srs-row-updating', !!busy);
            }
        });

        if (rentalDetailBulkSelect) {
            rentalDetailBulkSelect.disabled = !!busy;
        }

        return selects;
    }

    function sendRentalStatusUpdate(scheduleId, nextStatus) {
        return fetch(CTX + '/api/staff/rental/status', {
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
                    throw new Error(body.message || 'Khﾃｴng th盻・c蘯ｭp nh蘯ｭt tr蘯｡ng thﾃ｡i.');
                }
                return body;
            });
    }

    function handleBulkStatusChange(event) {
        var select = event.target;
        if (!select || select !== rentalDetailBulkSelect) {
            return;
        }

        var previousStatus = String(select.getAttribute('data-prev-status') || '');
        var nextStatus = String(select.value || '').toUpperCase();
        var modalCell = state.modalCellKey ? state.cellMap[state.modalCellKey] : null;
        var modalSlot = modalCell ? findSlot(modalCell.slotId) : null;

        if (!nextStatus) {
            refreshBulkStatusSelectClass(previousStatus);
            return;
        }

        if (!canEditSlot(modalSlot)) {
            select.value = previousStatus;
            refreshBulkStatusSelectClass(previousStatus);
            window.alert(buildReadonlyMessage(modalSlot));
            return;
        }

        var rowSelects = Array.prototype.slice.call(
            rentalDetailBody ? rentalDetailBody.querySelectorAll('[data-role="detail-status"]') : []
        );
        var updates = rowSelects
            .map(function (rowSelect) {
                return {
                    select: rowSelect,
                    scheduleId: Number(rowSelect.getAttribute('data-schedule-id') || 0),
                    previousStatus: normalizeStatus(rowSelect.getAttribute('data-prev-status'))
                };
            })
            .filter(function (item) {
                return item.scheduleId && item.previousStatus !== nextStatus;
            });

        refreshBulkStatusSelectClass(nextStatus);

        if (!updates.length) {
            select.setAttribute('data-prev-status', nextStatus);
            return;
        }

        setDetailRowsBusy(true);
        Promise.all(updates.map(function (item) {
            return sendRentalStatusUpdate(item.scheduleId, nextStatus);
        }))
            .then(function () {
                select.setAttribute('data-prev-status', nextStatus);
                loadRentalStatus(state.selectedDate, state.modalCellKey);
            })
            .catch(function (error) {
                console.error('Bulk rental status update error:', error);
                select.value = previousStatus;
                refreshBulkStatusSelectClass(previousStatus);
                loadRentalStatus(state.selectedDate, state.modalCellKey);
                window.alert(error.message || 'Khﾃｴng th盻・c蘯ｭp nh蘯ｭt t蘯･t c蘯｣ tr蘯｡ng thﾃ｡i.');
            })
            .finally(function () {
                setDetailRowsBusy(false);
            });
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

    function normalizeSearchText(value) {
        return String(value || '')
            .normalize('NFD')
            .replace(/[\u0300-\u036f]/g, '')
            .toLowerCase()
            .trim();
    }

    function dedupeInventorySuggestionItems(items) {
        var seen = Object.create(null);

        return (Array.isArray(items) ? items : []).filter(function (item) {
            var key = String(item.inventoryId || '') + '|' +
                normalizeSearchText((item.inventoryName || '') + '|' + (item.brand || ''));

            if (seen[key]) {
                return false;
            }

            seen[key] = true;
            return true;
        });
    }

    function setupInventorySearchUi() {
        if (!inventorySearchInput) {
            return;
        }

        inventorySearchInput.setAttribute('autocomplete', 'off');

        if (inventoryToolbar) {
            var note = inventoryToolbar.querySelector('.srs-inventory-toolbar-note');
            if (!note) {
                note = document.createElement('div');
                note.className = 'srs-inventory-toolbar-note';
                note.textContent = 'Gợi ý lấy trong ' + state.inventorySuggestionLimit +
                    ' dữ liệu đầu tiên. Bấm tìm kiếm để tra toàn bộ dữ liệu.';
                inventoryToolbar.appendChild(note);
            }
        }

        if (inventoryToolbar) {
            var legacyNote = inventoryToolbar.querySelector('.srs-inventory-toolbar-note');
            if (legacyNote) {
                legacyNote.remove();
            }
        }

        if (inventorySearchInput.parentElement) {
            inventorySuggestionMenu = document.getElementById('inventorySuggestionMenu');

            if (!inventorySuggestionMenu) {
                inventorySuggestionMenu = document.createElement('div');
                inventorySuggestionMenu.id = 'inventorySuggestionMenu';
                inventorySuggestionMenu.className = 'search-suggestion-menu';
                inventorySearchInput.parentElement.appendChild(inventorySuggestionMenu);
            }
        }

        if (inventorySearchButton) {
            inventorySearchButton.classList.add('srs-search-button');
        }
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
                maybeShowCurrentSlotNotice();
                renderTimelineGrid();
                reopenModalIfNeeded();
                loadInventoryTableData(state.inventoryCurrentPage);
            })
            .catch(function (error) {
                console.error('Rental status load error:', error);
                if (errorMessage) {
                    errorMessage.textContent = error.message || 'Không thể tải dữ liệu.';
                }
                renderRealtimePanels();
                loadInventoryTableData(state.inventoryCurrentPage);
                showState('error');
                closeDetailModal();
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

    function loadInventorySuggestionData() {
        if (state.inventorySuggestionLoaded || state.inventorySuggestionRequested) {
            return;
        }

        state.inventorySuggestionRequested = true;

        var params = new URLSearchParams();
        params.set('page', '1');
        params.set('pageSize', String(state.inventorySuggestionLimit));

        fetch(CTX + '/api/staff/rental/inventory?' + params.toString(), {
            credentials: 'same-origin',
            headers: {
                'Accept': 'application/json'
            }
        })
            .then(handleJsonResponse)
            .then(function (body) {
                if (!body.success) {
                    throw new Error(body.message || 'Không thể tải gợi ý kho đồ.');
                }

                var data = body.data || {};
                var items = Array.isArray(data.items) ? data.items.map(mapInventoryItem) : [];
                state.inventorySuggestionItems = dedupeInventorySuggestionItems(items);
                state.inventorySuggestionLoaded = true;

                if (document.activeElement === inventorySearchInput) {
                    refreshInventorySuggestionMenu();
                }
            })
            .catch(function (error) {
                console.error('Inventory suggestion load error:', error);
            })
            .finally(function () {
                state.inventorySuggestionRequested = false;
            });
    }

    function renderInventorySuggestionMenu(items) {
        if (!inventorySuggestionMenu || !inventorySearchInput) {
            return;
        }

        inventorySuggestionMenu.innerHTML = '';

        if (!items.length) {
            var emptyState = document.createElement('div');
            emptyState.className = 'search-suggestion-empty';
            emptyState.textContent = 'Không có gợi ý trong 50 dữ liệu đầu tiên. Bấm Tìm kiếm để tra toàn bộ dữ liệu.';
            emptyState.textContent = 'Khong tim thay do phu hop.';
            inventorySuggestionMenu.appendChild(emptyState);
            inventorySuggestionMenu.classList.add('is-visible');
            return;
        }

        items.forEach(function (item) {
            var button = document.createElement('button');
            var title = document.createElement('span');

            button.type = 'button';
            button.className = 'search-suggestion-item';

            title.className = 'search-suggestion-title';
            title.textContent = item.inventoryName;
            button.appendChild(title);

            if (item.brand) {
                var meta = document.createElement('span');
                meta.className = 'search-suggestion-meta';
                meta.textContent = item.brand;
                button.appendChild(meta);
            }

            button.addEventListener('mousedown', function (event) {
                event.preventDefault();
                inventorySearchInput.value = item.inventoryName;
                hideInventorySuggestionMenu();
                inventorySearchInput.focus();
            });

            inventorySuggestionMenu.appendChild(button);
        });

        inventorySuggestionMenu.classList.add('is-visible');
    }

    function loadInventoryTableData(page) {
        var nextPage = Math.max(1, Number(page || 1));
        state.inventoryCurrentPage = nextPage;
        state.inventorySearchTerm = inventorySearchInput ? String(inventorySearchInput.value || '').trim() : state.inventorySearchTerm;

        renderInventoryLoading();

        var params = new URLSearchParams();
        params.set('page', String(nextPage));
        if (state.inventorySearchTerm) {
            params.set('q', state.inventorySearchTerm);
        }

        fetch(CTX + '/api/staff/rental/inventory?' + params.toString(), {
            credentials: 'same-origin',
            headers: {
                'Accept': 'application/json'
            }
        })
            .then(handleJsonResponse)
            .then(function (body) {
                if (!body.success) {
                    throw new Error(body.message || 'Không thể tải danh sách kho đồ.');
                }

                var data = body.data || {};
                state.inventoryItems = Array.isArray(data.items) ? data.items.map(mapInventoryItem) : [];
                state.inventoryCurrentPage = Number(data.page || nextPage);
                state.inventoryPageSize = Number(data.pageSize || state.inventoryPageSize || 5);
                state.inventoryTotal = Number(data.total || 0);
                state.inventoryTotalPages = Number(data.totalPages || 0);

                renderInventoryTable();
            })
            .catch(function (error) {
                console.error('Inventory load error:', error);
                renderInventoryError(error.message || 'Không thể tải danh sách kho đồ.');
            });
    }

    function renderInventoryTable() {
        if (!inventoryTableBody) {
            return;
        }

        var totalItems = Number(state.inventoryTotal || 0);
        var totalPages = Number(state.inventoryTotalPages || 0);

        if (!state.inventoryItems.length) {
            inventoryTableBody.innerHTML =
                '<tr><td colspan="4" class="text-center text-muted py-4">' +
                (state.inventorySearchTerm
                    ? 'Không tìm thấy đồ nào phù hợp với từ khóa.'
                    : 'Không có dữ liệu kho đồ.') +
                '</td></tr>';
            renderInventoryPagination(totalItems, totalPages, 0, 0);
            return;
        }

        var startIndex = (state.inventoryCurrentPage - 1) * state.inventoryPageSize;

        inventoryTableBody.innerHTML = state.inventoryItems.map(function (item, index) {
            return '' +
                '<tr>' +
                '   <td>' + (startIndex + index + 1) + '</td>' +
                '   <td>' + esc(item.inventoryName) + '</td>' +
                '   <td>' + formatNumber(item.totalQuantity) + '</td>' +
                '   <td class="srs-stock-available">' + formatNumber(item.availableQuantity) + '</td>' +
                '</tr>';
        }).join('');

        renderInventoryPagination(
            totalItems,
            totalPages,
            startIndex + 1,
            startIndex + state.inventoryItems.length
        );
    }

    function renderInventoryPagination(totalItems, totalPages, fromItem, toItem) {
        if (!inventoryPagination) {
            return;
        }

        if (!totalItems || totalPages <= 1) {
            inventoryPagination.innerHTML = '';
            return;
        }

        var current = Math.min(Math.max(state.inventoryCurrentPage, 1), totalPages);
        var html = '<nav><ul class="pagination justify-content-center align-items-center gap-2 compact-pagination mb-0">';

        html += '<li class="page-item ' + (current === 1 ? 'disabled' : '') + '">';
        html += current === 1
            ? '<span class="page-link-static" aria-label="Trang trước"><i class="bi bi-chevron-left"></i></span>'
            : '<button type="button" class="page-link" data-role="inventory-page" data-page="' + (current - 1) + '" aria-label="Trang trước"><i class="bi bi-chevron-left"></i></button>';
        html += '</li>';

        html += '<li class="page-item active"><span class="page-link-static">' + current + '</span></li>';

        if (current + 1 < totalPages) {
            html += '<li class="page-item disabled"><span class="page-link-static pagination-ellipsis">...</span></li>';
        }

        if (current < totalPages) {
            html += '<li class="page-item"><button type="button" class="page-link" data-role="inventory-page" data-page="' + totalPages + '">' + totalPages + '</button></li>';
        }

        html += '<li class="page-item ' + (current === totalPages ? 'disabled' : '') + '">';
        html += current === totalPages
            ? '<span class="page-link-static" aria-label="Trang sau"><i class="bi bi-chevron-right"></i></span>'
            : '<button type="button" class="page-link" data-role="inventory-page" data-page="' + (current + 1) + '" aria-label="Trang sau"><i class="bi bi-chevron-right"></i></button>';
        html += '</li>';

        html += '</ul></nav>';
        inventoryPagination.innerHTML = html;
    }

    function renderInventoryLoading() {
        if (!inventoryTableBody) {
            return;
        }

        inventoryTableBody.innerHTML =
            '<tr><td colspan="4" class="text-center text-muted py-4">Đang tải dữ liệu kho đồ...</td></tr>';

        if (inventoryPagination) {
            inventoryPagination.innerHTML = '';
        }
    }

    function renderRealtimePanels() {
        if (inventoryCurrentTime) {
            inventoryCurrentTime.textContent = getCurrentTimeLabel();
        }

        var currentSlot = getCurrentRuntimeSlot();
        if (inventoryCurrentSlot) {
            inventoryCurrentSlot.textContent = currentSlot
                ? (currentSlot.startTime + ' - ' + currentSlot.endTime)
                : 'Slot không khả dụng';
        }

        refreshOpenModal();
    }

    function maybeShowCurrentSlotNotice() {
        var currentSlot = getCurrentRuntimeSlot();
        var noticeKey = '';

        if (state.selectedDate === getTodayStr() && !currentSlot) {
            noticeKey = 'today-no-slot-' + state.selectedDate;
            if (state.lastScreenNoticeKey !== noticeKey) {
                state.lastScreenNoticeKey = noticeKey;
                showScreenAlert('Hiện tại không nằm trong slot nào nên bạn chỉ có thể xem thông tin thuê đồ.');
            }
            return;
        }

        if (state.selectedDate !== getTodayStr()) {
            state.lastScreenNoticeKey = 'view-only-' + state.selectedDate;
            return;
        }

        state.lastScreenNoticeKey = '';
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

    function buildCellMeta(cell) {
        var itemCount = Number(cell.itemCount || 0);
        var totalQuantity = Number(cell.totalQuantity || 0);
        return itemCount + ' đồ - SL ' + totalQuantity;
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

    function formatDisplayDate(dateStr) {
        var date = new Date(dateStr + 'T00:00:00');
        var weekdays = ['Chủ nhật', 'Thứ 2', 'Thứ 3', 'Thứ 4', 'Thứ 5', 'Thứ 6', 'Thứ 7'];
        return weekdays[date.getDay()] + ', ' +
            String(date.getDate()).padStart(2, '0') + '/' +
            String(date.getMonth() + 1).padStart(2, '0') + '/' +
            date.getFullYear();
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
