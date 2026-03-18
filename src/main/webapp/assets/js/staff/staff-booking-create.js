(function () {
'use strict';

var CTX = window.ST_CTX || '';
var stateNoData = document.getElementById('stateNoData');
var createContent = document.getElementById('createContent');
var summaryDate = document.getElementById('summaryDate');
var sessionsContainer = document.getElementById('sessionsContainer');
var summaryTotal = document.getElementById('summaryTotal');
var tabAccount = document.getElementById('tabAccount');
var tabGuest = document.getElementById('tabGuest');
var formAccount = document.getElementById('formAccount');
var formGuest = document.getElementById('formGuest');
var customerSearch = document.getElementById('customerSearch');
var searchDropdown = document.getElementById('searchDropdown');
var selectedCustomer = document.getElementById('selectedCustomer');
var selName = document.getElementById('selName');
var selPhone = document.getElementById('selPhone');
var selEmail = document.getElementById('selEmail');
var btnRemoveCustomer = document.getElementById('btnRemoveCustomer');
var selectedAccountId = document.getElementById('selectedAccountId');
var guestNameInput = document.getElementById('guestName');
var guestPhoneInput = document.getElementById('guestPhone');
var guestEmailInput = document.getElementById('guestEmail');
var phoneHint = document.getElementById('phoneHint');
var formError = document.getElementById('formError');
var btnSubmit = document.getElementById('btnSubmit');
var rentalGroupsContainer = document.getElementById('rentalGroupsContainer');
var rentalActionNotice = document.getElementById('rentalActionNotice');
var rentalFeeSummary = document.getElementById('rentalFeeSummary');
var rentalGrandTotal = document.getElementById('rentalGrandTotal');
var rentalSearchInput = document.getElementById('rentalSearchInput');
var rentalSuggestionMenu = document.getElementById('rentalSuggestionMenu');
var rentalSortSelect = document.getElementById('rentalSortSelect');
var btnRentalSearch = document.getElementById('btnRentalSearch');
var rentalInventoryTableBody = document.getElementById('rentalInventoryTableBody');
var rentalPaginationInfo = document.getElementById('rentalPaginationInfo');
var rentalPagination = document.getElementById('rentalPagination');
var btnRentalSave = document.getElementById('btnRentalSave');
var rentalModalContext = document.getElementById('rentalModalContext');
var rentalInventoryEmpty = document.getElementById('rentalInventoryEmpty');
var rentalModalElement = document.getElementById('rentalInventoryModal');

var customerType = 'ACCOUNT';
var bookingData = null;
var searchTimer = null;
var sessions = [];
var courtTotalPrice = 0;
var rentalModal = null;
var rentalActionTimer = null;
var rentalState = {
    selectedSlot: null,
    page: 1,
    pageSize: 5,
    keyword: '',
    sortBy: 'default',
    totalItems: 0,
    totalPages: 1,
    items: [],
    suggestionItems: [],
    courts: [],
    slotPagesByCourt: {},
    draftBySlot: {},
    draftLoadedSlots: {},
    savedBySlot: {},
    clearingSlotKey: null,
    applyingAll: false,
    applyingCourtId: null
};

init();

function init() {
    var raw = sessionStorage.getItem('staffBookingSlots');
    if (!raw) {
        showNoData();
        return;
    }

    try {
        bookingData = JSON.parse(raw);
        if (!bookingData || !bookingData.slots || bookingData.slots.length === 0) {
            showNoData();
            return;
        }
    } catch (err) {
        console.error('Invalid booking data:', err);
        showNoData();
        return;
    }

    if (window.bootstrap && rentalModalElement) {
        rentalModal = new bootstrap.Modal(rentalModalElement);
    }

    createContent.classList.remove('d-none');
    normalizeBookingSlots();
    renderBookingSummary();
    renderRentalSection();
    bindEvents();
}

function showNoData() {
    if (stateNoData) stateNoData.classList.remove('d-none');
    if (createContent) createContent.classList.add('d-none');
}

function normalizeBookingSlots() {
    bookingData.slots = (bookingData.slots || []).map(function (slot, idx) {
        return {
            bookingSlotId: slot.bookingSlotId || slot.id || null,
            courtId: Number(slot.courtId || 0),
            courtName: slot.courtName || ('Sân #' + slot.courtId),
            slotId: Number(slot.slotId || 0),
            startTime: normalizeTime(slot.startTime),
            endTime: normalizeTime(slot.endTime),
            price: Number(slot.price || 0),
            __idx: idx
        };
    }).sort(function (a, b) {
        if (a.courtId === b.courtId) {
            if (toMinutes(a.startTime) === toMinutes(b.startTime)) {
                return Number(a.slotId || 0) - Number(b.slotId || 0);
            }
            return toMinutes(a.startTime) - toMinutes(b.startTime);
        }
        return String(a.courtName).localeCompare(String(b.courtName));
    });
}

function renderBookingSummary() {
    if (summaryDate) summaryDate.textContent = formatDate(bookingData.date);

    sessions = buildSessions(bookingData.slots || []);
    courtTotalPrice = 0;
    if (!sessionsContainer) {
        updateGrandSummary();
        return;
    }

    sessionsContainer.innerHTML = '';
    sessions.forEach(function (session, idx) {
        var sessionPrice = 0;
        session.forEach(function (slot) {
            sessionPrice += Number(slot.price || 0);
        });
        courtTotalPrice += sessionPrice;

        var first = session[0];
        var last = session[session.length - 1];
        var div = document.createElement('div');
        div.className = 'sbc-session';
        div.innerHTML =
            '<div class="sbc-session-idx">' + (idx + 1) + '</div>' +
            '<div class="sbc-session-info">' +
            '  <div class="sbc-session-court">' + escapeHtml(first.courtName) + '</div>' +
            '  <div class="sbc-session-meta">' +
            '    <span><i class="bi bi-clock"></i>' + escapeHtml(first.startTime) + ' - ' + escapeHtml(last.endTime) + '</span>' +
            '    <span><i class="bi bi-layers"></i>' + session.length + ' khung giờ</span>' +
            '    <span class="sbc-session-price">' + formatMoney(sessionPrice) + '</span>' +
            '  </div>' +
            '</div>';
        sessionsContainer.appendChild(div);
    });

    updateGrandSummary();
}

function updateGrandSummary() {
    if (!summaryTotal) return;
    summaryTotal.textContent = formatMoney(courtTotalPrice + getRentalGrandTotal());
}

function renderRentalSection() {
    rentalState.courts = buildRentalCourts(bookingData.slots || []);

    if (!rentalGroupsContainer) {
        renderRentalFeeSummary();
        return;
    }
    if (!rentalState.courts.length) {
        rentalGroupsContainer.innerHTML = '<div class="text-muted">Không có slot để thêm đồ thuê.</div>';
        renderRentalFeeSummary();
        return;
    }

    rentalGroupsContainer.innerHTML = buildRentalToolbar() + rentalState.courts.map(function (court) {
        var pageInfo = getCourtSlotPage(court.courtId, court.slots.length);
        var start = (pageInfo.page - 1) * 5;
        var pageSlots = court.slots.slice(start, start + 5);

        return '' +
            '<div class="sbc-rental-court">' +
            '   <div class="sbc-rental-court-header">' +
            '       <div class="sbc-rental-court-name">' + escapeHtml(court.courtName) + '</div>' +
            '       <div class="sbc-rental-court-meta">' + court.slots.length + ' slot</div>' +
            '   </div>' +
            buildCourtActionBar(court) +
            '   <div class="sbc-rental-slot-grid">' + pageSlots.map(buildRentalSlotBox).join('') + '</div>' +
            buildCourtSlotPagination(court.courtId, pageInfo.page, pageInfo.totalPages) +
            buildCourtRentalSummary(court) +
            '</div>';
    }).join('');

    renderRentalFeeSummary();
}

function buildRentalCourts(rows) {
    var map = {};
    (rows || []).forEach(function (row) {
        if (!map[row.courtId]) {
            map[row.courtId] = { courtId: row.courtId, courtName: row.courtName, slots: [] };
        }
        map[row.courtId].slots.push({
            slotKey: buildRentalSlotKey(row.courtId, row.slotId),
            bookingSlotId: row.bookingSlotId,
            courtId: row.courtId,
            courtName: row.courtName,
            slotId: row.slotId,
            startTime: row.startTime,
            endTime: row.endTime,
            price: Number(row.price || 0)
        });
    });

    return Object.keys(map).map(function (courtId) {
        var court = map[courtId];
        court.slots.sort(function (a, b) {
            if (toMinutes(a.startTime) === toMinutes(b.startTime)) {
                return Number(a.slotId || 0) - Number(b.slotId || 0);
            }
            return toMinutes(a.startTime) - toMinutes(b.startTime);
        });
        return court;
    }).sort(function (a, b) {
        return String(a.courtName).localeCompare(String(b.courtName));
    });
}

function buildRentalSlotKey(courtId, slotId) {
    return 'court_' + courtId + '__slot_' + slotId;
}

function buildRentalSlotBox(slot) {
    var items = rentalState.savedBySlot[slot.slotKey] || [];
    var classes = ['sbc-rental-slot-btn'];
    var isActive = rentalState.selectedSlot && rentalState.selectedSlot.slotKey === slot.slotKey;
    var isClearing = rentalState.clearingSlotKey === slot.slotKey;
    if (items.length) classes.push('is-configured');
    if (isActive) classes.push('is-active');

    return '' +
        '<div class="sbc-rental-slot-card">' +
        (items.length
            ? '<button type="button" class="sbc-rental-slot-clear" ' + (isClearing ? 'disabled ' : '') +
              'onclick="clearRentalSlot(\'' + jsString(slot.slotKey) + '\', event)" aria-label="Xóa đồ thuê" title="Xóa đồ thuê">' +
              '   <i class="bi ' + (isClearing ? 'bi-hourglass-split' : 'bi-x-lg') + '"></i>' +
              '</button>'
            : '') +
        '<button type="button" class="' + classes.join(' ') + '" ' + (isClearing ? 'disabled ' : '') + 'onclick="openRentalModal(\'' + jsString(slot.slotKey) + '\')">' +
        '   <span class="sbc-rental-slot-time">' + escapeHtml(slot.startTime) + ' - ' + escapeHtml(slot.endTime) + '</span>' +
        '   <span class="sbc-rental-slot-state">' + (items.length ? ('Đã lưu ' + items.length + ' món') : 'Nhấn để chọn đồ') + '</span>' +
        '   <span class="sbc-rental-slot-sub">' + (items.length ? ('SL thuê: ' + sumItemQuantities(items) + ' - ' + formatMoney(computeItemsTotal(items))) : '&nbsp;') + '</span>' +
        '</button>' +
        '</div>';
}

function buildCourtSlotPagination(courtId, currentPage, totalPages) {
    if (totalPages <= 1) return '';
    return '' +
        '<div class="sbc-rental-slot-pagination">' +
        '   <button type="button" class="btn btn-outline-secondary btn-sm" ' + (currentPage <= 1 ? 'disabled' : '') + ' onclick="changeRentalSlotPage(' + Number(courtId) + ', -1)"><i class="bi bi-chevron-left"></i></button>' +
        '   <span>Trang ' + currentPage + ' / ' + totalPages + '</span>' +
        '   <button type="button" class="btn btn-outline-secondary btn-sm" ' + (currentPage >= totalPages ? 'disabled' : '') + ' onclick="changeRentalSlotPage(' + Number(courtId) + ', 1)"><i class="bi bi-chevron-right"></i></button>' +
        '</div>';
}

function buildRentalToolbar() {
    var configuredSlots = getConfiguredRentalSlots();
    var configuredCount = configuredSlots.length;
    var isApplying = !!rentalState.applyingAll;
    var buttonLabel = isApplying ? 'Đang áp dụng...' : 'Áp dụng tất cả sân';
    var helperText = 'Hãy lưu đồ thuê cho đúng 1 slot để dùng làm mẫu.';

    if (configuredCount === 1) {
        var sourceSlot = configuredSlots[0].slot;
        helperText = 'Slot mẫu: ' + sourceSlot.courtName + ' - ' + sourceSlot.startTime + ' đến ' + sourceSlot.endTime + '.';
    } else if (configuredCount > 1) {
        helperText = 'Hiện đang có ' + configuredCount + ' slot đã lưu đồ thuê.';
    }

    return '' +
        '<div class="sbc-rental-toolbar">' +
        '   <div class="sbc-rental-toolbar-copy">' +
        '       <div class="sbc-rental-toolbar-title">Áp dụng đồ thuê cho tất cả sân</div>' +
        '       <div class="sbc-rental-toolbar-note">' + escapeHtml(helperText) + '</div>' +
        '   </div>' +
        '   <button type="button" class="btn btn-sm btn-outline-success sbc-rental-toolbar-btn" ' +
        ((configuredCount === 0 || isApplying || rentalState.applyingCourtId) ? 'disabled ' : '') +
        'onclick="applyAllRentalSlots()">' +
        '       <i class="bi bi-copy me-1"></i>' + buttonLabel +
        '   </button>' +
        '</div>';
}

function buildCourtActionBar(court) {
    var configuredSlots = getConfiguredRentalSlotsForCourt(court);
    var configuredCount = configuredSlots.length;
    var isApplying = Number(rentalState.applyingCourtId || 0) === Number(court.courtId);
    var buttonLabel = isApplying ? 'Đang áp dụng...' : 'Áp dụng sân';
    var helperText = 'Chưa có slot mẫu trong sân này.';

    if (configuredCount === 1) {
        var sourceSlot = configuredSlots[0].slot;
        helperText = 'Slot mẫu trong sân: ' + sourceSlot.startTime + ' đến ' + sourceSlot.endTime + '.';
    } else if (configuredCount > 1) {
        helperText = 'Hiện có ' + configuredCount + ' slot đã lưu đồ thuê trong sân này.';
    }

    return '' +
        '<div class="sbc-rental-court-actions">' +
        '   <div class="sbc-rental-court-actions-note">' + escapeHtml(helperText) + '</div>' +
        '   <button type="button" class="btn btn-sm btn-outline-success sbc-rental-court-btn" ' +
        ((configuredCount === 0 || isApplying || rentalState.applyingAll || rentalState.applyingCourtId) ? 'disabled ' : '') +
        'onclick="applyCourtRental(' + Number(court.courtId) + ')">' +
        '       <i class="bi bi-copy me-1"></i>' + buttonLabel +
        '   </button>' +
        '</div>';
}

function buildCourtRentalSummary(court) {
    var total = getCourtRentalTotal(court);
    return '' +
        '<div class="sbc-rental-court-summary">' +
        '   <div class="sbc-rental-court-summary-title">Tổng tiền thuê đồ</div>' +
        '   <div class="sbc-rental-court-summary-total">' + formatMoney(total) + '</div>' +
        '</div>';
}

function getConfiguredRentalSlots() {
    return getConfiguredRentalSlotsByList(getAllSlotsInApplyOrder());
}

function getConfiguredRentalSlotsForCourt(court) {
    return getConfiguredRentalSlotsByList((court && court.slots) || []);
}

function getConfiguredRentalSlotsByList(slots) {
    return (slots || []).map(function (slot) {
        return {
            slot: slot,
            items: cloneRentalItems(rentalState.savedBySlot[slot.slotKey] || [])
        };
    }).filter(function (entry) {
        return entry.items.length > 0;
    });
}

function getAllSlotsInApplyOrder() {
    var orderedSlots = [];
    (rentalState.courts || []).forEach(function (court) {
        (court.slots || []).forEach(function (slot) {
            orderedSlots.push(slot);
        });
    });
    return orderedSlots;
}

function getApplyAllTargets(sourceEntry) {
    return getApplyTargetsByList(sourceEntry, getAllSlotsInApplyOrder());
}

function getCourtApplyTargets(court, sourceEntry) {
    return getApplyTargetsByList(sourceEntry, (court && court.slots) || []);
}

function getApplyTargetsByList(sourceEntry, slots) {
    var sourceSlotKey = sourceEntry && sourceEntry.slot ? sourceEntry.slot.slotKey : '';
    var sourceItems = cloneRentalItems(sourceEntry ? sourceEntry.items : []);

    return (slots || []).filter(function (slot) {
        return slot.slotKey !== sourceSlotKey;
    }).map(function (slot) {
        return {
            slot: slot,
            items: cloneRentalItems(sourceItems)
        };
    });
}

function getCourtRentalTotal(court) {
    return (court.slots || []).reduce(function (sum, slot) {
        return sum + computeItemsTotal(rentalState.savedBySlot[slot.slotKey] || []);
    }, 0);
}

function cloneRentalItems(items) {
    return (items || []).map(function (item) {
        return {
            inventoryId: Number(item.inventoryId),
            name: item.name || '',
            quantity: Number(item.quantity || 0),
            unitPrice: Number(item.unitPrice || 0),
            brand: item.brand || '',
            description: item.description || ''
        };
    });
}

function getCourtSlotPage(courtId, slotCount) {
    var totalPages = Math.max(1, Math.ceil(slotCount / 5));
    var page = Number(rentalState.slotPagesByCourt[courtId] || 1);
    if (page < 1) page = 1;
    if (page > totalPages) page = totalPages;
    rentalState.slotPagesByCourt[courtId] = page;
    return { page: page, totalPages: totalPages };
}

function changeRentalSlotPage(courtId, delta) {
    var court = findCourtById(courtId);
    if (!court) return;
    var pageInfo = getCourtSlotPage(courtId, court.slots.length);
    var nextPage = pageInfo.page + delta;
    if (nextPage < 1 || nextPage > pageInfo.totalPages) return;
    rentalState.slotPagesByCourt[courtId] = nextPage;
    renderRentalSection();
}

function findCourtById(courtId) {
    var courts = rentalState.courts || [];
    for (var i = 0; i < courts.length; i++) {
        if (Number(courts[i].courtId) === Number(courtId)) return courts[i];
    }
    return null;
}

function findSlotByKey(slotKey) {
    var courts = rentalState.courts || [];
    for (var i = 0; i < courts.length; i++) {
        for (var j = 0; j < courts[i].slots.length; j++) {
            if (courts[i].slots[j].slotKey === slotKey) return courts[i].slots[j];
        }
    }
    return null;
}

function normalizeSavedItems(items) {
    return (items || []).map(function (item) {
        return {
            inventoryId: Number(item.inventoryId || 0),
            name: item.name || '',
            quantity: Number(item.selectedQuantity || item.quantity || 0),
            unitPrice: Number(item.rentalPrice || item.unitPrice || 0),
            brand: item.brand || '',
            description: item.description || '',
            availableItem: Number(item.availableQuantity || item.availableItem || 0)
        };
    }).filter(function (item) {
        return item.inventoryId > 0 && item.quantity > 0;
    });
}

function buildDraftMap(items) {
    var map = {};
    (items || []).forEach(function (item) {
        map[String(item.inventoryId)] = {
            inventoryId: Number(item.inventoryId),
            name: item.name || '',
            quantity: Number(item.quantity || 0),
            unitPrice: Number(item.unitPrice || 0),
            brand: item.brand || '',
            description: item.description || ''
        };
    });
    return map;
}

function getDraftMap(slotKey) {
    if (!rentalState.draftBySlot[slotKey]) {
        rentalState.draftBySlot[slotKey] = {};
    }
    return rentalState.draftBySlot[slotKey];
}

function draftMapToItems(slotKey) {
    return Object.keys(getDraftMap(slotKey)).map(function (key) {
        return rentalState.draftBySlot[slotKey][key];
    }).filter(function (item) {
        return Number(item.quantity || 0) > 0;
    }).sort(function (a, b) {
        return String(a.name || '').localeCompare(String(b.name || ''));
    });
}

function renderRentalFeeSummary() {
    if (!rentalFeeSummary) return;
    var entries = getCourtRentalEntries();
    if (!entries.length) {
        rentalFeeSummary.innerHTML = '<div class="text-muted">Chưa có phí thuê đồ.</div>';
        if (rentalGrandTotal) rentalGrandTotal.textContent = formatMoney(0);
        updateGrandSummary();
        return;
    }

    rentalFeeSummary.innerHTML = entries.map(function (entry) {
        return '<div class="d-flex justify-content-between align-items-start border-bottom py-2"><div class="me-3">' + escapeHtml(entry.courtName) + '</div><div class="fw-semibold text-nowrap">' + formatMoney(entry.total) + '</div></div>';
    }).join('');

    if (rentalGrandTotal) rentalGrandTotal.textContent = formatMoney(getRentalGrandTotal());
    updateGrandSummary();
}

function getRentalGrandTotal() {
    return getCourtRentalEntries().reduce(function (sum, entry) {
        return sum + Number(entry.total || 0);
    }, 0);
}

function getCourtRentalEntries() {
    return (rentalState.courts || []).map(function (court) {
        return {
            courtId: court.courtId,
            courtName: court.courtName,
            total: getCourtRentalTotal(court)
        };
    }).filter(function (entry) {
        return Number(entry.total || 0) > 0;
    }).sort(function (a, b) {
        return String(a.courtName).localeCompare(String(b.courtName));
    });
}

function computeItemsTotal(items) {
    return (items || []).reduce(function (sum, item) {
        return sum + (Number(item.unitPrice || 0) * Number(item.quantity || 0));
    }, 0);
}

function sumItemQuantities(items) {
    return (items || []).reduce(function (sum, item) {
        return sum + Number(item.quantity || 0);
    }, 0);
}

function loadRentalInventory() {
    if (!rentalState.selectedSlot) return;

    var slot = rentalState.selectedSlot;
    var slotKey = slot.slotKey;
    var url = CTX + '/api/staff/rental/schedule/inventory' +
        '?bookingDate=' + encodeURIComponent(bookingData.date || '') +
        '&courtId=' + encodeURIComponent(slot.courtId) +
        '&slotId=' + encodeURIComponent(slot.slotId) +
        '&page=' + encodeURIComponent(rentalState.page) +
        '&q=' + encodeURIComponent(rentalState.keyword || '') +
        '&sort=' + encodeURIComponent(rentalState.sortBy || 'default');

    fetch(url, {
        method: 'GET',
        credentials: 'same-origin',
        headers: { 'Accept': 'application/json' }
    })
        .then(function (res) { return res.json(); })
        .then(function (json) {
            if (!rentalState.selectedSlot || rentalState.selectedSlot.slotKey !== slotKey) return;
            if (!json.success || !json.data) {
                throw new Error(json.message || 'Không thể tải danh sách đồ thuê.');
            }

            rentalState.page = Number(json.data.page || rentalState.page || 1);
            rentalState.pageSize = Number(json.data.pageSize || rentalState.pageSize || 5);
            rentalState.totalItems = Number(json.data.total || 0);
            rentalState.items = json.data.items || [];
            rentalState.suggestionItems = json.data.suggestionItems || [];
            rentalState.sortBy = json.data.priceSort || rentalState.sortBy || 'default';
            rentalState.totalPages = Math.max(1, Number(json.data.totalPages || 1));
            rentalState.savedBySlot[slotKey] = normalizeSavedItems(json.data.selectedItems || []);

            if (!rentalState.draftLoadedSlots[slotKey]) {
                rentalState.draftBySlot[slotKey] = buildDraftMap(rentalState.savedBySlot[slotKey]);
                rentalState.draftLoadedSlots[slotKey] = true;
            } else if (!rentalState.draftBySlot[slotKey]) {
                rentalState.draftBySlot[slotKey] = buildDraftMap(rentalState.savedBySlot[slotKey]);
            }

            if (rentalSortSelect) {
                rentalSortSelect.value = rentalState.sortBy;
            }

            renderRentalSection();
            renderRentalInventoryTable();
            refreshRentalSuggestions();
        })
        .catch(function (err) {
            console.error('Rental inventory load error:', err);
            rentalState.totalItems = 0;
            rentalState.items = [];
            rentalState.suggestionItems = [];
            rentalState.totalPages = 1;
            renderRentalInventoryTable('Không thể tải danh sách đồ thuê.');
            hideRentalSuggestionMenu();
        });
}

function renderRentalInventoryTable(errorMessage) {
    if (!rentalInventoryTableBody) return;

    var items = rentalState.items || [];
    rentalInventoryTableBody.innerHTML = '';

    if (errorMessage) {
        rentalInventoryTableBody.innerHTML = '<tr><td colspan="7" class="text-center text-danger">' + escapeHtml(errorMessage) + '</td></tr>';
        if (rentalInventoryEmpty) rentalInventoryEmpty.classList.add('d-none');
    } else if (!items.length) {
        if (rentalInventoryEmpty) rentalInventoryEmpty.classList.remove('d-none');
    } else {
        if (rentalInventoryEmpty) rentalInventoryEmpty.classList.add('d-none');
        var slotKey = rentalState.selectedSlot ? rentalState.selectedSlot.slotKey : '';
        var draftMap = getDraftMap(slotKey);
        var startIndex = ((rentalState.page - 1) * rentalState.pageSize) + 1;
        rentalInventoryTableBody.innerHTML = items.map(function (item, idx) {
            return buildRentalRow(item, startIndex + idx, draftMap[String(item.inventoryId)]);
        }).join('');
    }

    renderRentalPagination();
}

function renderRentalPagination() {
    if (rentalPaginationInfo) {
        if (!rentalState.totalItems) {
            rentalPaginationInfo.textContent = 'Chưa có dữ liệu đồ thuê.';
        } else {
            var fromItem = ((rentalState.page - 1) * rentalState.pageSize) + 1;
            var toItem = Math.min(rentalState.totalItems, fromItem + rentalState.items.length - 1);
            rentalPaginationInfo.textContent = 'Hiển thị ' + fromItem + ' - ' + toItem + ' / ' + rentalState.totalItems + ' đồ thuê';
        }
    }

    if (!rentalPagination) return;
    if (rentalState.totalPages <= 1) {
        rentalPagination.innerHTML = '';
        return;
    }

    var current = rentalState.page;
    var total = rentalState.totalPages;
    var html = '<nav><ul class="pagination justify-content-center align-items-center gap-2 compact-pagination mb-0">';

    html += '<li class="page-item ' + (current === 1 ? 'disabled' : '') + '">';
    html += current === 1
        ? '<span class="page-link-static" aria-label="Trang trước"><i class="bi bi-chevron-left"></i></span>'
        : '<button type="button" class="page-link" data-page="' + (current - 1) + '" aria-label="Trang trước"><i class="bi bi-chevron-left"></i></button>';
    html += '</li>';

    html += '<li class="page-item active"><span class="page-link-static">' + current + '</span></li>';

    if (current + 1 < total) {
        html += '<li class="page-item disabled"><span class="page-link-static pagination-ellipsis">...</span></li>';
    }

    if (current < total) {
        html += '<li class="page-item"><button type="button" class="page-link" data-page="' + total + '">' + total + '</button></li>';
    }

    html += '<li class="page-item ' + (current === total ? 'disabled' : '') + '">';
    html += current === total
        ? '<span class="page-link-static" aria-label="Trang sau"><i class="bi bi-chevron-right"></i></span>'
        : '<button type="button" class="page-link" data-page="' + (current + 1) + '" aria-label="Trang sau"><i class="bi bi-chevron-right"></i></button>';
    html += '</li>';

    html += '</ul></nav>';
    rentalPagination.innerHTML = html;
}

function buildRentalRow(item, index, draftItem) {
    var currentQty = draftItem ? Number(draftItem.quantity || 0) : Number(item.selectedQuantity || 0);
    var maxQty = Math.max(0, Number(item.availableQuantity || 0));
    var inventoryId = Number(item.inventoryId || 0);
    if (currentQty > maxQty) currentQty = maxQty;

    return '' +
        '<tr>' +
        '   <td>' + index + '</td>' +
        '   <td>' + escapeHtml(item.name || '') + '</td>' +
        '   <td>' + escapeHtml(item.brand || '') + '</td>' +
        '   <td>' + escapeHtml(item.description || '') + '</td>' +
        '   <td><div class="fw-semibold">' + formatMoney(item.rentalPrice || 0) + '</div><div class="small text-muted">/30 phút</div></td>' +
        '   <td><div class="fw-semibold" id="rentalRemaining_' + inventoryId + '">' + Math.max(0, maxQty - currentQty) + '</div><div class="small text-muted">Tối đa: ' + maxQty + '</div></td>' +
        '   <td><input type="number" min="0" max="' + maxQty + '" value="' + currentQty + '" class="form-control form-control-sm js-rental-qty" data-inventory-id="' + inventoryId + '" data-max-qty="' + maxQty + '"><div class="small text-muted mt-1">Nhập 0 nếu không thuê món này.</div></td>' +
        '</tr>';
}

function onSlotRentalQtyChange(input) {
    if (!input || !rentalState.selectedSlot) return;

    var inventoryId = Number(input.getAttribute('data-inventory-id') || 0);
    var maxQty = Number(input.getAttribute('data-max-qty') || 0);
    var rawValue = String(input.value || '').trim();
    var quantity = rawValue ? parseInt(rawValue, 10) : 0;

    if (isNaN(quantity) || quantity < 0) quantity = 0;
    if (quantity > maxQty) quantity = maxQty;
    if (rawValue !== '' && String(quantity) !== rawValue) input.value = quantity;

    var item = findInventoryItemById(inventoryId);
    if (!item) return;

    var slotKey = rentalState.selectedSlot.slotKey;
    var draftMap = getDraftMap(slotKey);
    if (quantity > 0) {
        draftMap[String(inventoryId)] = {
            inventoryId: inventoryId,
            name: item.name || '',
            quantity: quantity,
            unitPrice: Number(item.rentalPrice || 0),
            brand: item.brand || '',
            description: item.description || ''
        };
    } else {
        delete draftMap[String(inventoryId)];
    }

    updateRemainingDisplay(inventoryId, maxQty, quantity);
}

function updateRemainingDisplay(inventoryId, maxQty, quantity) {
    var remainingEl = document.getElementById('rentalRemaining_' + inventoryId);
    if (!remainingEl) return;
    remainingEl.textContent = Math.max(0, Number(maxQty || 0) - Number(quantity || 0));
}

function findInventoryItemById(inventoryId) {
    var items = rentalState.items || [];
    for (var i = 0; i < items.length; i++) {
        if (Number(items[i].inventoryId) === Number(inventoryId)) return items[i];
    }
    return null;
}

async function requestSaveSlotRentalSchedule(slot, items) {
    var res = await fetch(CTX + '/api/staff/rental/schedule/save', {
        method: 'POST',
        credentials: 'same-origin',
        headers: { 'Content-Type': 'application/json', 'Accept': 'application/json' },
        body: JSON.stringify({
            bookingDate: bookingData.date,
            courtId: slot.courtId,
            slotId: slot.slotId,
            items: (items || []).map(function (item) {
                return {
                    inventoryId: Number(item.inventoryId),
                    quantity: Number(item.quantity)
                };
            })
        })
    });

    var body = await res.json();
    if (!body.success) {
        throw new Error(body.message || 'Không thể lưu lịch đồ thuê.');
    }

    var slotKey = slot.slotKey;
    rentalState.savedBySlot[slotKey] = normalizeSavedItems((body.data && body.data.selectedItems) || []);
    rentalState.draftBySlot[slotKey] = buildDraftMap(rentalState.savedBySlot[slotKey]);
    rentalState.draftLoadedSlots[slotKey] = true;
}

async function saveRentalSchedule() {
    if (!rentalState.selectedSlot || !btnRentalSave) return;

    var slot = rentalState.selectedSlot;
    var payloadItems = draftMapToItems(slot.slotKey);

    var originalHtml = btnRentalSave.innerHTML;
    btnRentalSave.disabled = true;
    btnRentalSave.innerHTML = '<span class="spinner-border spinner-border-sm me-1"></span>Lưu';

    try {
        await requestSaveSlotRentalSchedule(slot, payloadItems);
        renderRentalSection();
        hideError();
        hideRentalAlert();
        closeRentalModal();
    } catch (err) {
        console.error('Save rental schedule error:', err);
        showError(err.message || 'Không thể lưu lịch đồ thuê.');
    } finally {
        btnRentalSave.disabled = false;
        btnRentalSave.innerHTML = originalHtml;
    }
}

async function clearRentalSlot(slotKey, event) {
    if (event) {
        event.preventDefault();
        event.stopPropagation();
    }

    var slot = findSlotByKey(slotKey);
    if (!slot || rentalState.clearingSlotKey === slotKey) {
        return;
    }

    rentalState.clearingSlotKey = slotKey;
    renderRentalSection();

    try {
        await requestSaveSlotRentalSchedule(slot, []);
        renderRentalSection();
        hideRentalAlert();
    } catch (err) {
        console.error('Clear rental slot error:', err);
        showRentalAlert(err.message || 'Không thể xóa đồ thuê của slot này.', 10000);
    } finally {
        rentalState.clearingSlotKey = null;
        renderRentalSection();
    }
}

async function applyAllRentalSlots() {
    var configuredSlots = getConfiguredRentalSlots();
    if (!configuredSlots.length) {
        return;
    }

    if (configuredSlots.length !== 1) {
        showRentalAlert('Chỉ áp dụng cho duy nhất 1 slot', 10000);
        return;
    }

    var targets = getApplyAllTargets(configuredSlots[0]);
    if (!targets.length) {
        showRentalAlert('Không còn slot nào để áp dụng.', 10000);
        return;
    }

    rentalState.applyingAll = true;
    renderRentalSection();

    try {
        for (var i = 0; i < targets.length; i++) {
            await requestSaveSlotRentalSchedule(targets[i].slot, targets[i].items);
        }
        renderRentalSection();
        hideRentalAlert();
    } catch (err) {
        console.error('Apply rental to all courts error:', err);
        showRentalAlert(err.message || 'Không thể áp dụng cho tất cả sân.', 10000);
    } finally {
        rentalState.applyingAll = false;
        renderRentalSection();
    }
}

async function applyCourtRental(courtId) {
    var court = findCourtById(courtId);
    if (!court) return;

    var configuredSlots = getConfiguredRentalSlotsForCourt(court);
    if (!configuredSlots.length) {
        return;
    }

    if (configuredSlots.length !== 1) {
        showRentalAlert('Chỉ áp dụng cho duy nhất 1 slot', 10000);
        return;
    }

    var targets = getCourtApplyTargets(court, configuredSlots[0]);
    if (!targets.length) {
        showRentalAlert('Không còn slot nào để áp dụng trong sân này.', 10000);
        return;
    }

    rentalState.applyingCourtId = courtId;
    renderRentalSection();

    try {
        for (var i = 0; i < targets.length; i++) {
            await requestSaveSlotRentalSchedule(targets[i].slot, targets[i].items);
        }
        renderRentalSection();
        hideRentalAlert();
    } catch (err) {
        console.error('Apply rental for court error:', err);
        showRentalAlert(err.message || 'Không thể áp dụng cho sân này.', 10000);
    } finally {
        rentalState.applyingCourtId = null;
        renderRentalSection();
    }
}

function openRentalModal(slotKey) {
    var slot = findSlotByKey(slotKey);
    if (!slot) return;

    rentalState.selectedSlot = slot;
    rentalState.page = 1;
    rentalState.pageSize = 5;
    rentalState.keyword = '';
    rentalState.totalItems = 0;
    rentalState.items = [];
    rentalState.suggestionItems = [];
    rentalState.totalPages = 1;

    if (rentalSearchInput) rentalSearchInput.value = '';
    if (rentalSortSelect) rentalSortSelect.value = rentalState.sortBy || 'default';
    if (rentalModalContext) rentalModalContext.textContent = slot.courtName + ' - ' + slot.startTime + ' đến ' + slot.endTime;
    hideRentalSuggestionMenu();

    loadRentalInventory();
    if (rentalModal) {
        rentalModal.show();
    } else if (rentalModalElement) {
        rentalModalElement.classList.add('show');
        rentalModalElement.style.display = 'block';
        rentalModalElement.removeAttribute('aria-hidden');
    }
    renderRentalSection();
}

function closeRentalModal() {
    if (rentalModal) {
        rentalModal.hide();
        return;
    }
    if (!rentalModalElement) return;
    rentalModalElement.classList.remove('show');
    rentalModalElement.style.display = 'none';
    rentalModalElement.setAttribute('aria-hidden', 'true');
}

window.openRentalModal = openRentalModal;
window.clearRentalSlot = clearRentalSlot;
window.changeRentalSlotPage = changeRentalSlotPage;
window.applyAllRentalSlots = applyAllRentalSlots;
window.applyCourtRental = applyCourtRental;

function bindEvents() {
    bindCustomerModeEvents();
    bindSearchEvents();
    bindGuestPhoneEvents();
    bindRentalEvents();
    bindSubmitEvent();
}

function bindCustomerModeEvents() {
    if (tabAccount) {
        tabAccount.addEventListener('click', function () {
            customerType = 'ACCOUNT';
            tabAccount.classList.add('active');
            if (tabGuest) tabGuest.classList.remove('active');
            if (formAccount) formAccount.classList.remove('d-none');
            if (formGuest) formGuest.classList.add('d-none');
            hideError();
        });
    }

    if (tabGuest) {
        tabGuest.addEventListener('click', function () {
            customerType = 'GUEST';
            tabGuest.classList.add('active');
            if (tabAccount) tabAccount.classList.remove('active');
            if (formGuest) formGuest.classList.remove('d-none');
            if (formAccount) formAccount.classList.add('d-none');
            hideError();
        });
    }

    if (btnRemoveCustomer) {
        btnRemoveCustomer.addEventListener('click', function () {
            if (selectedAccountId) selectedAccountId.value = '';
            if (selectedCustomer) selectedCustomer.classList.add('d-none');
        });
    }
}

function bindSearchEvents() {
    if (!customerSearch || !searchDropdown) return;
    customerSearch.addEventListener('input', function () {
        var keyword = this.value.trim();
        if (keyword.length < 2) {
            searchDropdown.classList.add('d-none');
            return;
        }

        clearTimeout(searchTimer);
        searchTimer = setTimeout(function () {
            fetch(CTX + '/api/staff/customer/search?q=' + encodeURIComponent(keyword), {
                credentials: 'same-origin',
                headers: { 'Accept': 'application/json' }
            })
                .then(function (res) { return res.json(); })
                .then(function (body) {
                    if (!body.success) return;
                    renderSearchResults(body.data.customers || []);
                })
                .catch(function (err) {
                    console.error('Customer search error:', err);
                });
        }, 300);
    });

    document.addEventListener('click', function (event) {
        if (!event.target.closest('.sbc-search-wrap')) {
            searchDropdown.classList.add('d-none');
        }
    });
}

function bindGuestPhoneEvents() {
    if (!guestPhoneInput) return;
    guestPhoneInput.addEventListener('input', function () {
        var digits = this.value.replace(/[^\d]/g, '');
        if (digits.length > 10) digits = digits.substring(0, 10);
        this.value = digits;
        updatePhoneHint(digits);
    });

    guestPhoneInput.addEventListener('paste', function () {
        var self = this;
        setTimeout(function () {
            var digits = self.value.replace(/[^\d]/g, '');
            if (digits.length > 10) digits = digits.substring(0, 10);
            self.value = digits;
            updatePhoneHint(digits);
        }, 0);
    });
}

function normalizeSearchText(value) {
    return String(value || '')
        .normalize('NFD')
        .replace(/[\u0300-\u036f]/g, '')
        .toLowerCase()
        .trim();
}

function hideRentalSuggestionMenu() {
    if (!rentalSuggestionMenu) return;
    rentalSuggestionMenu.classList.remove('is-visible');
    rentalSuggestionMenu.innerHTML = '';
}

function renderRentalSuggestionMenu(items) {
    if (!rentalSuggestionMenu || !rentalSearchInput) return;
    rentalSuggestionMenu.innerHTML = '';

    if (!items.length) {
        var emptyState = document.createElement('div');
        emptyState.className = 'search-suggestion-empty';
        emptyState.textContent = 'Không có gợi ý trong 50 dữ liệu đầu tiên. Bấm Tìm kiếm để tra toàn bộ.';
        rentalSuggestionMenu.appendChild(emptyState);
        rentalSuggestionMenu.classList.add('is-visible');
        return;
    }

    items.forEach(function (item) {
        var button = document.createElement('button');
        var title = document.createElement('span');

        button.type = 'button';
        button.className = 'search-suggestion-item';
        title.className = 'search-suggestion-title';
        title.textContent = item.name || '';
        button.appendChild(title);

        if (item.brand || item.description) {
            var meta = document.createElement('span');
            meta.className = 'search-suggestion-meta';
            meta.textContent = [item.brand, item.description].filter(Boolean).join(' - ');
            button.appendChild(meta);
        }

        button.addEventListener('mousedown', function (event) {
            event.preventDefault();
            rentalSearchInput.value = item.name || '';
            hideRentalSuggestionMenu();
            rentalSearchInput.focus();
        });

        rentalSuggestionMenu.appendChild(button);
    });

    rentalSuggestionMenu.classList.add('is-visible');
}

function refreshRentalSuggestions() {
    if (!rentalSearchInput || !rentalSuggestionMenu) return;

    var keyword = normalizeSearchText(rentalSearchInput.value);
    if (!keyword) {
        hideRentalSuggestionMenu();
        return;
    }

    var matchedItems = (rentalState.suggestionItems || []).filter(function (item) {
        var joined = [item.name, item.brand, item.description].filter(Boolean).join(' ');
        return normalizeSearchText(joined).includes(keyword);
    });

    renderRentalSuggestionMenu(matchedItems);
}

function bindRentalEvents() {
    if (btnRentalSearch) {
        btnRentalSearch.addEventListener('click', function () {
            rentalState.keyword = (rentalSearchInput ? rentalSearchInput.value : '').trim();
            rentalState.page = 1;
            loadRentalInventory();
            hideRentalSuggestionMenu();
        });
    }

    if (rentalSearchInput) {
        rentalSearchInput.addEventListener('input', refreshRentalSuggestions);
        rentalSearchInput.addEventListener('focus', refreshRentalSuggestions);
        rentalSearchInput.addEventListener('keydown', function (event) {
            if (event.key !== 'Enter') return;
            event.preventDefault();
            rentalState.keyword = (rentalSearchInput.value || '').trim();
            rentalState.page = 1;
            loadRentalInventory();
            hideRentalSuggestionMenu();
        });
    }

    if (rentalSortSelect) {
        rentalSortSelect.addEventListener('change', function () {
            rentalState.sortBy = this.value || 'default';
            rentalState.page = 1;
            loadRentalInventory();
        });
    }

    if (rentalPagination) {
        rentalPagination.addEventListener('click', function (event) {
            var pageButton = event.target.closest('[data-page]');
            if (!pageButton) return;
            var targetPage = Number(pageButton.getAttribute('data-page') || 0);
            if (targetPage <= 0 || targetPage === rentalState.page) return;
            rentalState.page = targetPage;
            loadRentalInventory();
        });
    }

    if (rentalInventoryTableBody) {
        rentalInventoryTableBody.addEventListener('input', function (event) {
            var input = event.target.closest('.js-rental-qty');
            if (!input) return;
            onSlotRentalQtyChange(input);
        });
    }

    if (btnRentalSave) {
        btnRentalSave.addEventListener('click', function () {
            saveRentalSchedule();
        });
    }

    document.addEventListener('click', function (event) {
        if (!event.target.closest('.search-suggestion-wrap')) {
            hideRentalSuggestionMenu();
        }
    });
}

function bindSubmitEvent() {
    if (!btnSubmit) return;
    btnSubmit.addEventListener('click', async function () {
        hideError();

        if (customerType === 'ACCOUNT') {
            if (!selectedAccountId || !selectedAccountId.value) {
                showError('Vui lòng tìm và chọn khách hàng.');
                return;
            }
        } else {
            if (!guestNameInput || !guestNameInput.value.trim()) {
                showError('Vui lòng nhập họ tên khách.');
                if (guestNameInput) guestNameInput.focus();
                return;
            }
            if (!guestPhoneInput || !guestPhoneInput.value.trim()) {
                showError('Vui lòng nhập số điện thoại.');
                if (guestPhoneInput) guestPhoneInput.focus();
                return;
            }
            if (!isValidPhone(guestPhoneInput.value)) {
                showError('Số điện thoại phải gồm 10 chữ số và bắt đầu bằng 0.');
                guestPhoneInput.focus();
                return;
            }
        }

        var slotsPayload = (bookingData.slots || []).map(function (slot) {
            return { courtId: slot.courtId, slotId: slot.slotId };
        });

        var rentalPayload = [];
        (rentalState.courts || []).forEach(function (court) {
            (court.slots || []).forEach(function (slot) {
                var items = rentalState.savedBySlot[slot.slotKey] || [];
                items.forEach(function (item) {
                    rentalPayload.push({
                        groupKey: slot.slotKey,
                        courtId: slot.courtId,
                        courtName: slot.courtName,
                        startTime: slot.startTime,
                        endTime: slot.endTime,
                        slotIds: [slot.slotId],
                        inventoryId: item.inventoryId,
                        name: item.name,
                        quantity: item.quantity,
                        unitPrice: item.unitPrice
                    });
                });
            });
        });

        var reqBody = {
            date: bookingData.date,
            customerType: customerType,
            accountId: customerType === 'ACCOUNT' ? parseInt(selectedAccountId.value, 10) : null,
            guestName: customerType === 'GUEST' ? guestNameInput.value.trim() : null,
            guestPhone: customerType === 'GUEST' ? guestPhoneInput.value.trim() : null,
            guestEmail: customerType === 'GUEST' && guestEmailInput ? guestEmailInput.value.trim() : null,
            slots: slotsPayload,
            rentals: rentalPayload,
            rentalTotal: getRentalGrandTotal(),
            totalAmount: courtTotalPrice + getRentalGrandTotal()
        };

        btnSubmit.disabled = true;
        btnSubmit.innerHTML = '<span class="sbc-spinner"></span>Đang tạo booking...';

        try {
            var res = await fetch(CTX + '/api/staff/booking/create', {
                method: 'POST',
                credentials: 'same-origin',
                headers: { 'Content-Type': 'application/json', 'Accept': 'application/json' },
                body: JSON.stringify(reqBody)
            });

            var body = await res.json();
            if (!body.success) {
                if (body.code === 'GUEST_PHONE_MATCHED_ACCOUNT' && body.data && body.data.accountId) {
                    var confirmed = await confirmGuestPhoneMatched(body.data);
                    if (confirmed) {
                        switchToAccountMode(body.data);
                        resetSubmitButton();
                        btnSubmit.click();
                        return;
                    }
                }

                showError(body.message || 'Đặt sân thất bại.');
                resetSubmitButton();
                return;
            }

            sessionStorage.removeItem('staffBookingSlots');
            window.location.href = CTX + '/staff/booking/detail/' + body.data.bookingId;
        } catch (err) {
            console.error('Create booking error:', err);
            showError('Lỗi kết nối. Vui lòng thử lại.');
            resetSubmitButton();
        }
    });
}

function renderSearchResults(customers) {
    if (!searchDropdown) return;
    searchDropdown.innerHTML = '';

    if (!customers.length) {
        searchDropdown.innerHTML = '<div class="sbc-search-empty">Không tìm thấy</div>';
        searchDropdown.classList.remove('d-none');
        return;
    }

    customers.forEach(function (customer) {
        var item = document.createElement('div');
        item.className = 'sbc-search-item';
        item.innerHTML = '<div class="sbc-search-item-name">' + escapeHtml(customer.fullName) + '</div>' +
            '<div class="sbc-search-item-meta">' + escapeHtml(customer.phone || '') + ' - ' + escapeHtml(customer.email || '') + '</div>';
        item.addEventListener('click', function () {
            selectCustomer(customer);
        });
        searchDropdown.appendChild(item);
    });

    searchDropdown.classList.remove('d-none');
}

function selectCustomer(customer) {
    if (selectedAccountId) selectedAccountId.value = customer.accountId;
    if (selName) selName.textContent = customer.fullName || '';
    if (selPhone) selPhone.textContent = customer.phone || '--';
    if (selEmail) selEmail.textContent = customer.email || '--';
    if (selectedCustomer) selectedCustomer.classList.remove('d-none');
    if (customerSearch) customerSearch.value = '';
    if (searchDropdown) searchDropdown.classList.add('d-none');
    hideError();
}

function switchToAccountMode(matched) {
    customerType = 'ACCOUNT';
    if (tabAccount) tabAccount.classList.add('active');
    if (tabGuest) tabGuest.classList.remove('active');
    if (formAccount) formAccount.classList.remove('d-none');
    if (formGuest) formGuest.classList.add('d-none');
    if (selectedAccountId) selectedAccountId.value = matched.accountId;
    if (selName) selName.textContent = matched.fullName || '--';
    if (selPhone) selPhone.textContent = matched.phone || '--';
    if (selEmail) selEmail.textContent = matched.email || '--';
    if (selectedCustomer) selectedCustomer.classList.remove('d-none');
    hideError();
}

function confirmGuestPhoneMatched(matched) {
    var message = 'Số điện thoại này đã tồn tại trên tài khoản CUSTOMER:\n' +
        '- ' + (matched.fullName || 'Không rõ tên') + '\n' +
        '- ' + (matched.phone || '') + '\n\n' +
        'Hệ thống sẽ chuyển sang luồng khách có tài khoản. Tiếp tục?';
    return uiConfirm(message, 'Trùng số điện thoại');
}

function resetSubmitButton() {
    if (!btnSubmit) return;
    btnSubmit.disabled = false;
    btnSubmit.innerHTML = '<i class="bi bi-check-circle me-2"></i>Xác nhận đặt sân';
}

function uiConfirm(message, title) {
    if (window.StaffDialog && typeof window.StaffDialog.confirm === 'function') {
        return window.StaffDialog.confirm({ title: title || 'Xác nhận', message: message || '' });
    }
    return Promise.resolve(window.confirm(message || ''));
}

function showError(message) {
    if (!formError) return;
    formError.textContent = message || '';
    formError.classList.remove('d-none');
}

function hideError() {
    if (!formError) return;
    formError.classList.add('d-none');
}

function showRentalAlert(message, durationMs) {
    if (!rentalActionNotice) return;
    if (rentalActionTimer) {
        clearTimeout(rentalActionTimer);
        rentalActionTimer = null;
    }
    rentalActionNotice.textContent = message || '';
    rentalActionNotice.classList.remove('d-none');

    if (durationMs && durationMs > 0) {
        rentalActionTimer = setTimeout(function () {
            hideRentalAlert();
        }, durationMs);
    }
}

function hideRentalAlert() {
    if (!rentalActionNotice) return;
    if (rentalActionTimer) {
        clearTimeout(rentalActionTimer);
        rentalActionTimer = null;
    }
    rentalActionNotice.classList.add('d-none');
    rentalActionNotice.textContent = '';
}

function isValidPhone(phone) {
    var cleaned = String(phone || '').replace(/\s+/g, '');
    return /^0\d{9}$/.test(cleaned);
}

function updatePhoneHint(digits) {
    if (!phoneHint || !guestPhoneInput) return;
    if (!digits.length) {
        phoneHint.classList.add('d-none');
        guestPhoneInput.classList.remove('sbc-input-error');
        guestPhoneInput.classList.remove('sbc-input-valid');
        return;
    }

    phoneHint.classList.remove('d-none');
    if (digits.length < 10) {
        phoneHint.textContent = 'Còn thiếu ' + (10 - digits.length) + ' số.';
        phoneHint.className = 'sbc-phone-hint sbc-hint-warn';
        guestPhoneInput.classList.remove('sbc-input-valid');
        guestPhoneInput.classList.add('sbc-input-error');
        return;
    }

    if (digits.length === 10 && digits.charAt(0) === '0') {
        phoneHint.textContent = 'Số điện thoại hợp lệ.';
        phoneHint.className = 'sbc-phone-hint sbc-hint-ok';
        guestPhoneInput.classList.remove('sbc-input-error');
        guestPhoneInput.classList.add('sbc-input-valid');
        return;
    }

    phoneHint.textContent = 'Số điện thoại phải bắt đầu bằng 0.';
    phoneHint.className = 'sbc-phone-hint sbc-hint-warn';
    guestPhoneInput.classList.remove('sbc-input-valid');
    guestPhoneInput.classList.add('sbc-input-error');
}

function formatMoney(amount) {
    if (amount == null || isNaN(Number(amount))) return '0 VND';
    return Number(amount).toLocaleString('vi-VN') + ' VND';
}

function escapeHtml(str) {
    if (str == null) return '';
    return String(str).replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/"/g, '&quot;').replace(/'/g, '&#39;');
}

function jsString(str) {
    if (str == null) return '';
    return String(str).replace(/\\/g, '\\\\').replace(/'/g, '\\\'').replace(/"/g, '\\"').replace(/\r/g, '').replace(/\n/g, '\\n');
}

function normalizeTime(timeStr) {
    if (!timeStr) return '';
    var value = String(timeStr).trim();
    if (value.length >= 5) return value.substring(0, 5);
    return value;
}

function formatDate(dateStr) {
    if (!dateStr) return '--';
    var date = new Date(dateStr + 'T00:00:00');
    return String(date.getDate()).padStart(2, '0') + '/' + String(date.getMonth() + 1).padStart(2, '0') + '/' + date.getFullYear();
}

function buildSessions(slots) {
    var groupedByCourt = {};
    (slots || []).forEach(function (slot) {
        if (!groupedByCourt[slot.courtId]) groupedByCourt[slot.courtId] = [];
        groupedByCourt[slot.courtId].push(slot);
    });

    var result = [];
    Object.keys(groupedByCourt).forEach(function (courtId) {
        var courtSlots = groupedByCourt[courtId].slice().sort(function (a, b) {
            return toMinutes(a.startTime) - toMinutes(b.startTime);
        });
        if (!courtSlots.length) return;

        var currentSession = [courtSlots[0]];
        for (var i = 1; i < courtSlots.length; i++) {
            var previous = currentSession[currentSession.length - 1];
            var current = courtSlots[i];
            if (previous.endTime === current.startTime) {
                currentSession.push(current);
            } else {
                result.push(currentSession);
                currentSession = [current];
            }
        }
        result.push(currentSession);
    });

    result.sort(function (a, b) {
        var timeCompare = toMinutes(a[0].startTime) - toMinutes(b[0].startTime);
        if (timeCompare !== 0) return timeCompare;
        return String(a[0].courtName).localeCompare(String(b[0].courtName));
    });
    return result;
}

function toMinutes(timeText) {
    var parts = normalizeTime(timeText).split(':');
    if (parts.length < 2) return 0;
    return (Number(parts[0]) * 60) + Number(parts[1]);
}
})();
