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
var rentalFeeSummary = document.getElementById('rentalFeeSummary');
var rentalGrandTotal = document.getElementById('rentalGrandTotal');
var rentalSearchInput = document.getElementById('rentalSearchInput');
var btnRentalSearch = document.getElementById('btnRentalSearch');
var rentalInventoryTableBody = document.getElementById('rentalInventoryTableBody');
var rentalPaginationInfo = document.getElementById('rentalPaginationInfo');
var btnRentalPrev = document.getElementById('btnRentalPrev');
var btnRentalNext = document.getElementById('btnRentalNext');
var btnRentalSave = document.getElementById('btnRentalSave');
var rentalModalContext = document.getElementById('rentalModalContext');
var rentalInventoryEmpty = document.getElementById('rentalInventoryEmpty');
var rentalModalElement = document.getElementById('rentalInventoryModal');
    var CTX = window.ST_CTX || '';

    // DOM
    var stateNoData       = document.getElementById('stateNoData');
    var createContent     = document.getElementById('createContent');
    var summaryDate       = document.getElementById('summaryDate');
    var sessionsContainer = document.getElementById('sessionsContainer');
    var summaryTotal      = document.getElementById('summaryTotal');
    var tabAccount        = document.getElementById('tabAccount');
    var tabGuest          = document.getElementById('tabGuest');
    var formAccount       = document.getElementById('formAccount');
    var formGuest         = document.getElementById('formGuest');
    var customerSearch    = document.getElementById('customerSearch');
    var searchDropdown    = document.getElementById('searchDropdown');
    var selectedCustomer  = document.getElementById('selectedCustomer');
    var selName           = document.getElementById('selName');
    var selPhone          = document.getElementById('selPhone');
    var selEmail          = document.getElementById('selEmail');
    var btnRemoveCustomer = document.getElementById('btnRemoveCustomer');
    var selectedAccountId = document.getElementById('selectedAccountId');
    var guestNameInput    = document.getElementById('guestName');
    var guestPhoneInput   = document.getElementById('guestPhone');
    var guestEmailInput   = document.getElementById('guestEmail');
    var phoneHint         = document.getElementById('phoneHint');
    var formError         = document.getElementById('formError');
    var btnSubmit         = document.getElementById('btnSubmit');

var customerType = 'ACCOUNT';
var bookingData = null;
var searchTimer = null;
var sessions = [];
var courtTotalPrice = 0;
var rentalModal = null;
var rentalState = {
    selectedSlot: null,
    page: 1,
    keyword: '',
    totalPages: 1,
    items: [],
    courts: [],
    slotPagesByCourt: {},
    draftBySlot: {},
    draftLoadedSlots: {},
    savedBySlot: {},
    applyingCourtId: null
};

init();

function init() {
    // ---- Init: load from sessionStorage ----
    var raw = sessionStorage.getItem('staffBookingSlots');
    if (!raw) {
        showNoData();
        return;
    }

    try {
        bookingData = JSON.parse(raw);
        if (!bookingData.slots || bookingData.slots.length === 0) {
            stateNoData.classList.remove('d-none');
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
            courtName: slot.courtName || ('San #' + slot.courtId),
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
    // ---- Render summary ----
    summaryDate.textContent = formatDate(bookingData.date);

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
            '    <span><i class="bi bi-layers"></i>' + session.length + ' slot</span>' +
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
        rentalGroupsContainer.innerHTML = '<div class="text-muted">Khong co slot de cho thue do.</div>';
        renderRentalFeeSummary();
        return;
    }

    rentalGroupsContainer.innerHTML = rentalState.courts.map(function (court) {
        var pageInfo = getCourtSlotPage(court.courtId, court.slots.length);
        var start = (pageInfo.page - 1) * 5;
        var pageSlots = court.slots.slice(start, start + 5);

        return '' +
            '<div class="sbc-rental-court">' +
            '   <div class="sbc-rental-court-header">' +
            '       <div class="sbc-rental-court-name">' + escapeHtml(court.courtName) + '</div>' +
            '       <div class="sbc-rental-court-meta">' + court.slots.length + ' o slot</div>' +
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
    if (items.length) classes.push('is-configured');
    if (isActive) classes.push('is-active');

    return '' +
        '<button type="button" class="' + classes.join(' ') + '" onclick="openRentalModal(\'' + jsString(slot.slotKey) + '\')">' +
        '   <span class="sbc-rental-slot-time">' + escapeHtml(slot.startTime) + ' - ' + escapeHtml(slot.endTime) + '</span>' +
        '   <span class="sbc-rental-slot-state">' + (items.length ? ('Da luu ' + items.length + ' mon') : 'Nhan de chon do') + '</span>' +
        '   <span class="sbc-rental-slot-sub">' + (items.length ? (sumItemQuantities(items) + ' cai - ' + formatMoney(computeItemsTotal(items))) : '&nbsp;') + '</span>' +
        '</button>';
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

function buildCourtActionBar(court) {
    var applyTargets = getCourtApplyTargets(court);
    var isApplying = Number(rentalState.applyingCourtId || 0) === Number(court.courtId);
    var buttonLabel = isApplying ? 'Dang ap dung...' : 'Ap dung toan bo';

    return '' +
        '<div class="sbc-rental-court-actions">' +
        '   <button type="button" class="btn btn-sm btn-outline-success" ' +
        (applyTargets.length === 0 || isApplying ? 'disabled ' : '') +
        'onclick="applyCourtRental(' + Number(court.courtId) + ')">' +
        '       <i class="bi bi-copy me-1"></i>' + buttonLabel +
        '   </button>' +
        '</div>';
}

function buildCourtRentalSummary(court) {
    var total = getCourtRentalTotal(court);
    return '' +
        '<div class="sbc-rental-court-summary">' +
        '   <div class="sbc-rental-court-summary-title">Tong tien thue do</div>' +
        '   <div class="sbc-rental-court-summary-total">' + formatMoney(total) + '</div>' +
        '</div>';
}

function getCourtApplyTargets(court) {
    var targets = [];
    var lastItems = null;

    (court.slots || []).forEach(function (slot) {
        var items = rentalState.savedBySlot[slot.slotKey] || [];
        if (items.length) {
            lastItems = cloneRentalItems(items);
            return;
        }

        if (lastItems && lastItems.length) {
            targets.push({
                slot: slot,
                items: cloneRentalItems(lastItems)
            });
        }
    });

    return targets;
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
        rentalFeeSummary.innerHTML = '<div class="text-muted">Chua co phi thue do.</div>';
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
        '&q=' + encodeURIComponent(rentalState.keyword || '');

    fetch(url, {
        method: 'GET',
        credentials: 'same-origin',
        headers: { 'Accept': 'application/json' }
    })
        .then(function (res) { return res.json(); })
        .then(function (json) {
            if (!rentalState.selectedSlot || rentalState.selectedSlot.slotKey !== slotKey) return;
            if (!json.success || !json.data) {
                throw new Error(json.message || 'Khong the tai danh sach do thue.');
            }

            rentalState.page = Number(json.data.page || rentalState.page || 1);
            rentalState.items = json.data.items || [];
            rentalState.totalPages = Math.max(1, Number(json.data.totalPages || 1));
            rentalState.savedBySlot[slotKey] = normalizeSavedItems(json.data.selectedItems || []);

            if (!rentalState.draftLoadedSlots[slotKey]) {
                rentalState.draftBySlot[slotKey] = buildDraftMap(rentalState.savedBySlot[slotKey]);
                rentalState.draftLoadedSlots[slotKey] = true;
            } else if (!rentalState.draftBySlot[slotKey]) {
                rentalState.draftBySlot[slotKey] = buildDraftMap(rentalState.savedBySlot[slotKey]);
            }

            renderRentalSection();
            renderRentalInventoryTable();
        })
        .catch(function (err) {
            console.error('Rental inventory load error:', err);
            rentalState.items = [];
            rentalState.totalPages = 1;
            renderRentalInventoryTable('Khong the tai danh sach do thue.');
        });
}

function renderRentalInventoryTable(errorMessage) {
    if (!rentalInventoryTableBody) return;

    // ---- Tab switching ----
    tabAccount.addEventListener('click', function () {
        customerType = 'ACCOUNT';
        tabAccount.classList.add('active');
        tabGuest.classList.remove('active');
        formAccount.classList.remove('d-none');
        formGuest.classList.add('d-none');
        hideError();
    });
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
        var startIndex = ((rentalState.page - 1) * 5) + 1;
        rentalInventoryTableBody.innerHTML = items.map(function (item, idx) {
            return buildRentalRow(item, startIndex + idx, draftMap[String(item.inventoryId)]);
        }).join('');
    }

    if (rentalPaginationInfo) rentalPaginationInfo.textContent = 'Trang ' + rentalState.page + ' / ' + rentalState.totalPages;
    if (btnRentalPrev) btnRentalPrev.disabled = rentalState.page <= 1;
    if (btnRentalNext) btnRentalNext.disabled = rentalState.page >= rentalState.totalPages;
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
        '   <td>' + formatMoney(item.rentalPrice || 0) + '</td>' +
        '   <td><div class="fw-semibold" id="rentalRemaining_' + inventoryId + '">' + Math.max(0, maxQty - currentQty) + '</div><div class="small text-muted">Toi da: ' + maxQty + '</div></td>' +
        '   <td><input type="number" min="0" max="' + maxQty + '" value="' + currentQty + '" class="form-control form-control-sm js-rental-qty" data-inventory-id="' + inventoryId + '" data-max-qty="' + maxQty + '"><div class="small text-muted mt-1">Nhap 0 neu khong thue mon nay.</div></td>' +
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
        throw new Error(body.message || 'Khong the luu lich cho thue.');
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
    btnRentalSave.innerHTML = '<span class="spinner-border spinner-border-sm me-1"></span>Luu';

    try {
        await requestSaveSlotRentalSchedule(slot, payloadItems);
        renderRentalSection();
        hideError();
        closeRentalModal();
    } catch (err) {
        console.error('Save rental schedule error:', err);
        showError(err.message || 'Khong the luu lich cho thue.');
    } finally {
        btnRentalSave.disabled = false;
        btnRentalSave.innerHTML = originalHtml;
    }
}

async function applyCourtRental(courtId) {
    var court = findCourtById(courtId);
    if (!court) return;

    var targets = getCourtApplyTargets(court);
    if (!targets.length) {
        showError('San nay chua co slot mau de ap dung cho cac slot sau.');
        return;
    }

    rentalState.applyingCourtId = courtId;
    renderRentalSection();

    try {
        for (var i = 0; i < targets.length; i++) {
            await requestSaveSlotRentalSchedule(targets[i].slot, targets[i].items);
        }
        renderRentalSection();
        hideError();
    } catch (err) {
        console.error('Apply court rental error:', err);
        showError(err.message || 'Khong the ap dung toan bo cho san nay.');
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
    rentalState.keyword = '';
    rentalState.items = [];
    rentalState.totalPages = 1;

    if (rentalSearchInput) rentalSearchInput.value = '';
    if (rentalModalContext) rentalModalContext.textContent = slot.courtName + ' - ' + slot.startTime + ' den ' + slot.endTime;

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
window.changeRentalSlotPage = changeRentalSlotPage;
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

    tabGuest.addEventListener('click', function () {
        customerType = 'GUEST';
        tabGuest.classList.add('active');
        tabAccount.classList.remove('active');
        formGuest.classList.remove('d-none');
        formAccount.classList.add('d-none');
        hideError();
    });

    function switchToAccountMode(matched) {
        customerType = 'ACCOUNT';
        tabAccount.classList.add('active');
        tabGuest.classList.remove('active');
        formAccount.classList.remove('d-none');
        formGuest.classList.add('d-none');

        selectedAccountId.value = matched.accountId;
        selName.textContent = matched.fullName || '—';
        selPhone.textContent = matched.phone || '—';
        selEmail.textContent = matched.email || '—';
        selectedCustomer.classList.remove('d-none');

        hideError();
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
    function confirmGuestPhoneMatched(matched) {
        var msg = 'Số điện thoại này đã tồn tại tài khoản CUSTOMER:\n' +
            '- ' + (matched.fullName || 'Không rõ tên') + '\n' +
            '- ' + (matched.phone || '') + '\n\n' +
            'Hệ thống sẽ chuyển sang luồng Khách có tài khoản. Tiếp tục';
        return uiConfirm(msg, 'Trùng số điện thoại');
    }
}

    // ---- Customer search (ACCOUNT) ----
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
    // ---- Phone validation helper ----
    function isValidPhone(phone) {
        // Vietnamese phone: exactly 10 digits, starts with 0
        var cleaned = phone.replace(/\s+/g, '');
        return /^0\d{9}$/.test(cleaned);
    }

    function isValidEmail(email) {
        var cleaned = (email || '').trim();
        if (!cleaned) return true;
        return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(cleaned);
    }

    // ---- Real-time phone validation on input ----
    if (guestPhoneInput) {
        // Only allow digits
        guestPhoneInput.addEventListener('input', function () {
            // Strip non-digit characters
            var raw = this.value.replace(/[^\d]/g, '');
            // Limit to 10 digits
            if (raw.length > 10) {
                raw = raw.substring(0, 10);
            }
            this.value = raw;

            // Show real-time hint
            updatePhoneHint(raw);
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

function bindRentalEvents() {
    if (btnRentalSearch) {
        btnRentalSearch.addEventListener('click', function () {
            rentalState.keyword = (rentalSearchInput ? rentalSearchInput.value : '').trim();
            rentalState.page = 1;
            loadRentalInventory();
        });
    }

    if (rentalSearchInput) {
        rentalSearchInput.addEventListener('keydown', function (event) {
            if (event.key !== 'Enter') return;
            event.preventDefault();
            rentalState.keyword = (rentalSearchInput.value || '').trim();
            rentalState.page = 1;
            loadRentalInventory();
        });
    }

    if (btnRentalPrev) {
        btnRentalPrev.addEventListener('click', function () {
            if (rentalState.page <= 1) return;
            rentalState.page--;
            loadRentalInventory();
        });
    }

    if (btnRentalNext) {
        btnRentalNext.addEventListener('click', function () {
            if (rentalState.page >= rentalState.totalPages) return;
            rentalState.page++;
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
}

function bindSubmitEvent() {
    if (!btnSubmit) return;
    // ---- Submit ----
    btnSubmit.addEventListener('click', async function () {
        hideError();

        if (customerType === 'ACCOUNT') {
            if (!selectedAccountId || !selectedAccountId.value) {
                showError('Vui long tim va chon khach hang.');
                return;
            }
        } else {
            if (!guestNameInput || !guestNameInput.value.trim()) {
                showError('Vui long nhap ho ten khach.');
                if (guestNameInput) guestNameInput.focus();
                return;
            }
            if (!guestPhoneInput || !guestPhoneInput.value.trim()) {
                showError('Vui long nhap so dien thoai.');
                if (guestPhoneInput) guestPhoneInput.focus();
                return;
            }
            if (!isValidPhone(guestPhoneInput.value)) {
                showError('So dien thoai phai gom 10 chu so va bat dau bang 0.');
                guestPhoneInput.focus();
                return;
            }
            if (guestEmailInput && guestEmailInput.value.trim() && !isValidEmail(guestEmailInput.value)) {
                showError('Email không đúng định dạng');
                guestEmailInput.focus();
                return;
            }
            if (guestEmailInput && !guestEmailInput.value.trim()) {
                var proceed = await uiConfirm(
                    'Không có email, hệ thống sẽ không gửi thông báo. Tiếp tục?',
                    'Cảnh báo'
                );
                if (!proceed) return;
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
            guestEmail: customerType === 'GUEST' && guestEmailInput ? guestEmailInput.value.trim() : null,
            slots: slotsPayload
        };

        btnSubmit.disabled = true;
        btnSubmit.innerHTML = '<span class="sbc-spinner"></span>Dang tao booking...';

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

                showError(body.message || 'Dat san that bai.');
                resetSubmitButton();
                return;
            }

            // Success -> clear sessionStorage -> redirect to detail
            if (body.data && body.data.emailWarning) {
                await uiAlert(body.data.emailWarning, 'Thông báo');
            }
            sessionStorage.removeItem('staffBookingSlots');
            window.location.href = CTX + '/staff/booking/detail/' + body.data.bookingId;
        } catch (err) {
            console.error('Create booking error:', err);
            showError('Loi ket noi. Vui long thu lai.');
            resetSubmitButton();
        }
    });
}

function renderSearchResults(customers) {
    if (!searchDropdown) return;
    searchDropdown.innerHTML = '';

    if (!customers.length) {
        searchDropdown.innerHTML = '<div class="sbc-search-empty">Khong tim thay</div>';
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
    var message = 'So dien thoai nay da ton tai tren tai khoan CUSTOMER:\n' +
        '- ' + (matched.fullName || 'Khong ro ten') + '\n' +
        '- ' + (matched.phone || '') + '\n\n' +
        'He thong se chuyen sang luong khach co tai khoan. Tiep tuc?';
    return uiConfirm(message, 'Trung so dien thoai');
}

function resetSubmitButton() {
    if (!btnSubmit) return;
    btnSubmit.disabled = false;
    btnSubmit.innerHTML = '<i class="bi bi-check-circle me-2"></i>Xac nhan dat san';
}
    // ---- Helpers ----
    function resetSubmitButton() {
        btnSubmit.disabled = false;
        btnSubmit.innerHTML = '<i class="bi bi-check-circle me-2"></i>Xác nhận đặt sân';
    }

    function uiAlert(message, title) {
        if (window.StaffDialog && typeof window.StaffDialog.alert === 'function') {
            return window.StaffDialog.alert({ title: title || 'Thông báo', message: message || '' });
        }
        window.alert(message || '');
        return Promise.resolve();
    }

    function uiConfirm(message, title) {
        if (window.StaffDialog && typeof window.StaffDialog.confirm === 'function') {
            return window.StaffDialog.confirm({ title: title || 'Xác nhận', message: message || '' });
        }
        return Promise.resolve(window.confirm(message || ''));
    }
function uiConfirm(message, title) {
    if (window.StaffDialog && typeof window.StaffDialog.confirm === 'function') {
        return window.StaffDialog.confirm({ title: title || 'Xac nhan', message: message || '' });
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
        phoneHint.textContent = 'Con thieu ' + (10 - digits.length) + ' so.';
        phoneHint.className = 'sbc-phone-hint sbc-hint-warn';
        guestPhoneInput.classList.remove('sbc-input-valid');
        guestPhoneInput.classList.add('sbc-input-error');
        return;
    }

    if (digits.length === 10 && digits.charAt(0) === '0') {
        phoneHint.textContent = 'So dien thoai hop le.';
        phoneHint.className = 'sbc-phone-hint sbc-hint-ok';
        guestPhoneInput.classList.remove('sbc-input-error');
        guestPhoneInput.classList.add('sbc-input-valid');
        return;
    }

    phoneHint.textContent = 'So dien thoai phai bat dau bang 0.';
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
