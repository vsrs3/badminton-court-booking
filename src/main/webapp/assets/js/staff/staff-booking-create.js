(function () {
    'use strict';

    var CTX = window.ST_CTX || '';

    // =========================
    // DOM
    // =========================
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

    // Rental DOM
    var rentalGroupsContainer = document.getElementById('rentalGroupsContainer');
    var rentalFeeSummary = document.getElementById('rentalFeeSummary');
    var rentalGrandTotal = document.getElementById('rentalGrandTotal');

    var rentalSearchInput = document.getElementById('rentalSearchInput');
    var btnRentalSearch = document.getElementById('btnRentalSearch');
    var rentalInventoryTableBody = document.getElementById('rentalInventoryTableBody');
    var rentalPaginationInfo = document.getElementById('rentalPaginationInfo');
    var btnRentalPrev = document.getElementById('btnRentalPrev');
    var btnRentalNext = document.getElementById('btnRentalNext');
    var rentalModalContext = document.getElementById('rentalModalContext');
    var rentalInventoryEmpty = document.getElementById('rentalInventoryEmpty');

    // =========================
    // State
    // =========================
    var customerType = 'ACCOUNT';
    var bookingData = null;
    var searchTimer = null;
    var sessions = [];
    var courtTotalPrice = 0;
    var rentalModal = null;
    var sessionKeyToBookingSlotIds = {};

    var rentalState = {
        selectedGroup: null, // { groupKey, courtId, courtName, startTime, endTime, bookingSlotIds, slotCount }
        page: 1,
        keyword: '',
        totalPages: 1,
        items: [],
        groups: [],

        // groupKey => [{ inventoryId, name, quantity, unitPrice }]
        rentalsByGroup: {}
    };

    // =========================
    // Init
    // =========================
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
        } catch (e) {
            console.error('Invalid booking data:', e);
            showNoData();
            return;
        }

        createContent.classList.remove('d-none');

        if (window.bootstrap && document.getElementById('rentalInventoryModal')) {
            rentalModal = new bootstrap.Modal(document.getElementById('rentalInventoryModal'));
        }

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
        bookingData.slots = (bookingData.slots || []).map(function (s, idx) {
            return {
                bookingSlotId: s.bookingSlotId || s.id || null,
                courtId: s.courtId,
                courtName: s.courtName || ('Sân #' + s.courtId),
                slotId: s.slotId,
                startTime: normalizeTime(s.startTime),
                endTime: normalizeTime(s.endTime),
                price: Number(s.price || 0),
                __idx: idx
            };
        });
    }

    // =========================
    // Render summary
    // =========================
    function renderBookingSummary() {
        summaryDate.textContent = formatDate(bookingData.date);

        sessions = buildSessions(bookingData.slots);
        courtTotalPrice = 0;
        sessionsContainer.innerHTML = '';

        sessions.forEach(function (session, idx) {
            var sessionPrice = 0;
            session.forEach(function (s) {
                sessionPrice += Number(s.price || 0);
            });
            courtTotalPrice += sessionPrice;

            var sessionKey = buildSessionKey(session);
            sessionKeyToBookingSlotIds[sessionKey] = session.map(function (s) {
                return s.bookingSlotId;
            }).filter(Boolean);

            var div = document.createElement('div');
            div.className = 'sbc-session';
            div.innerHTML =
                '<div class="sbc-session-idx">' + (idx + 1) + '</div>' +
                '<div class="sbc-session-info">' +
                '  <div class="sbc-session-court">' + escapeHtml(session[0].courtName) + '</div>' +
                '  <div class="sbc-session-meta">' +
                '    <span><i class="bi bi-clock"></i>' + escapeHtml(session[0].startTime) + ' → ' + escapeHtml(session[session.length - 1].endTime) + '</span>' +
                '    <span><i class="bi bi-layers"></i>' + session.length + ' slot</span>' +
                '    <span class="sbc-session-price">' + formatMoney(sessionPrice) + '</span>' +
                '  </div>' +
                '</div>';
            sessionsContainer.appendChild(div);
        });

        updateGrandSummary();
    }

    function updateGrandSummary() {
        var total = courtTotalPrice + getRentalGrandTotal();
        summaryTotal.textContent = formatMoney(total);
    }

    // =========================
    // Rental section
    // =========================
    function renderRentalSection() {
        rentalState.groups = groupConsecutiveSlots(bookingData.slots);
        renderRentalGroups(rentalState.groups);
        renderRentalFeeSummary();
    }

    function groupConsecutiveSlots(rows) {
        var groupsByCourt = {};
        (rows || []).forEach(function (s) {
            if (!groupsByCourt[s.courtId]) groupsByCourt[s.courtId] = [];
            groupsByCourt[s.courtId].push(s);
        });

        var result = [];

        Object.keys(groupsByCourt).forEach(function (courtId) {
            var list = groupsByCourt[courtId].slice().sort(function (a, b) {
                if (a.startTime === b.startTime) return Number(a.slotId || 0) - Number(b.slotId || 0);
                return a.startTime.localeCompare(b.startTime);
            });

            if (!list.length) return;

            var current = [list[0]];

            for (var i = 1; i < list.length; i++) {
                var prev = current[current.length - 1];
                var cur = list[i];

                if (prev.endTime === cur.startTime) {
                    current.push(cur);
                } else {
                    result.push(buildRentalGroup(current));
                    current = [cur];
                }
            }

            if (current.length) {
                result.push(buildRentalGroup(current));
            }
        });

        result.sort(function (a, b) {
            if (a.startTime === b.startTime) {
                return String(a.courtName).localeCompare(String(b.courtName));
            }
            return a.startTime.localeCompare(b.startTime);
        });

        return result;
    }

    function buildRentalGroup(slotGroup) {
        var first = slotGroup[0];
        var last = slotGroup[slotGroup.length - 1];
        var bookingSlotIds = slotGroup.map(function (x) { return x.bookingSlotId; }).filter(Boolean);
        var groupKey = 'court_' + first.courtId + '__' + first.startTime.replace(/:/g, '') + '__' + last.endTime.replace(/:/g, '');

        return {
            groupKey: groupKey,
            courtId: first.courtId,
            courtName: first.courtName,
            startTime: first.startTime,
            endTime: last.endTime,
            slotCount: slotGroup.length,
            bookingSlotIds: bookingSlotIds,
            slots: slotGroup
        };
    }

    function renderRentalGroups(groups) {
        if (!rentalGroupsContainer) return;
        if (!groups || groups.length === 0) {
            rentalGroupsContainer.innerHTML = '<div class="text-muted">Kh\u00f4ng c\u00f3 nh\u00f3m slot \u0111\u1ec3 thu\u00ea \u0111\u1ed3.</div>';
            return;
        }
        rentalGroupsContainer.innerHTML = groups.map(function (g) {
            var selectedHtmlId = 'rental-selected-' + safeId(g.groupKey);
            return '' +
                '<div class="border rounded-3 p-3 mb-3">' +
                '   <div class="fw-bold">' + escapeHtml(g.courtName) + '</div>' +
                '   <div class="text-muted small mb-2">' + escapeHtml(g.startTime) + ' - ' + escapeHtml(g.endTime) + ' (' + g.slotCount + ' slot)</div>' +
                buildRentalGroupActions(g) +
                '   <div class="mt-3" id="' + selectedHtmlId + '"></div>' +
                '</div>';
        }).join('');
        groups.forEach(function (g) {
            renderSelectedRentals(g.groupKey);
        });
    }
    function buildRentalGroupActions(group) {
        var currentItems = rentalState.rentalsByGroup[group.groupKey] || [];
        var previousSource = findPreviousConfiguredGroup(group.groupKey);
        var modalLabel = currentItems.length ? '\u0110\u1ed3 m\u1edbi' : 'X\u00e1c nh\u1eadn';
        var modalBtnClass = previousSource ? 'btn btn-sm btn-outline-primary' : 'btn btn-sm btn-primary';
        var buttons = [];
        if (previousSource) {
            buttons.push(
                '<button type="button" class="btn btn-sm btn-success" onclick="applyPreviousRental(\'' + jsString(group.groupKey) + '\')">' +
                '   <i class="bi bi-check2-circle me-1"></i>\u00c1p d\u1ee5ng' +
                '</button>'
            );
        }
        buttons.push(
            '<button type="button" class="' + modalBtnClass + '" onclick="openRentalModal(\'' + jsString(group.groupKey) + '\')">' +
            '   <i class="bi bi-bag-plus me-1"></i>' + modalLabel +
            '</button>'
        );
        return '' +
            '<div class="d-flex flex-wrap gap-2">' + buttons.join('') + '</div>' +
            buildRentalSourceHint(previousSource);
    }
    function buildRentalSourceHint(previousSource) {
        if (!previousSource) return '';
        return '<div class="small text-muted mt-2">\u00c1p d\u1ee5ng theo: ' +
            escapeHtml(previousSource.courtName) + ' - ' +
            escapeHtml(previousSource.startTime) + ' \u0111\u1ebfn ' +
            escapeHtml(previousSource.endTime) + '</div>';
    }

    function renderSelectedRentals(groupKey) {
        var el = document.getElementById('rental-selected-' + safeId(groupKey));
        if (!el) return;
        var items = rentalState.rentalsByGroup[groupKey] || [];
        if (!items.length) {
            el.innerHTML = '<div class="small text-muted">Ch\u01b0a c\u00f3 \u0111\u1ed3 thu\u00ea cho nh\u00f3m slot n\u00e0y.</div>';
            return;
        }
        var html = '' +
            '<div class="table-responsive">' +
            '<table class="table table-sm align-middle mb-0">' +
            '<thead>' +
            '   <tr>' +
            '       <th>T\u00ean \u0111\u1ed3</th>' +
            '       <th style="width:120px;">S\u1ed1 l\u01b0\u1ee3ng</th>' +
            '       <th style="width:140px;">\u0110\u01a1n gi\u00e1</th>' +
            '       <th style="width:140px;">Th\u00e0nh ti\u1ec1n</th>' +
            '   </tr>' +
            '</thead>' +
            '<tbody>';
        items.forEach(function (item) {
            var group = findGroupByKey(groupKey);
            var lineTotal = Number(item.unitPrice || 0) * Number(item.quantity || 0) * Number(group ? group.slotCount : 1);
            html += '' +
                '<tr>' +
                '   <td>' + escapeHtml(item.name) + '</td>' +
                '   <td>' + Number(item.quantity || 0) + '</td>' +
                '   <td>' + formatMoney(item.unitPrice || 0) + '</td>' +
                '   <td class="fw-bold">' + formatMoney(lineTotal) + '</td>' +
                '</tr>';
        });
        html += '</tbody></table></div>';
        el.innerHTML = html;
    }

    function renderRentalFeeSummary() {
        if (!rentalFeeSummary) return;
        var groups = rentalState.groups || [];
        var lines = [];
        groups.forEach(function (g) {
            var items = rentalState.rentalsByGroup[g.groupKey] || [];
            if (!items.length) return;
            var total = 0;
            items.forEach(function (item) {
                total += Number(item.unitPrice || 0) * Number(item.quantity || 0) * Number(g.slotCount || 1);
            });
            lines.push({
                label: escapeHtml(g.courtName) + ' : ' + escapeHtml(g.startTime) + ' - ' + escapeHtml(g.endTime),
                amount: total
            });
        });
        if (!lines.length) {
            rentalFeeSummary.innerHTML = '<div class="text-muted">Ch\u01b0a c\u00f3 ph\u00ed thu\u00ea \u0111\u1ed3.</div>';
            if (rentalGrandTotal) rentalGrandTotal.textContent = '0\u0111';
            updateGrandSummary();
            return;
        }
        rentalFeeSummary.innerHTML = lines.map(function (line) {
            return '' +
                '<div class="d-flex justify-content-between align-items-start border-bottom py-2">' +
                '   <div class="me-3">' + line.label + '</div>' +
                '   <div class="fw-semibold text-nowrap">' + formatMoney(line.amount) + '</div>' +
                '</div>';
        }).join('');
        if (rentalGrandTotal) rentalGrandTotal.textContent = formatMoney(getRentalGrandTotal());
        updateGrandSummary();
    }

    function getRentalGrandTotal() {
        var total = 0;
        var groups = rentalState.groups || [];

        groups.forEach(function (g) {
            var items = rentalState.rentalsByGroup[g.groupKey] || [];
            items.forEach(function (item) {
                total += Number(item.unitPrice || 0) * Number(item.quantity || 0) * Number(g.slotCount || 1);
            });
        });

        return total;
    }

    function findGroupByKey(groupKey) {
        var groups = rentalState.groups || [];
        for (var i = 0; i < groups.length; i++) {
            if (groups[i].groupKey === groupKey) return groups[i];
        }
        return null;
    }
    function findPreviousConfiguredGroup(groupKey) {
        var groups = rentalState.groups || [];
        var targetIndex = -1;
        for (var i = 0; i < groups.length; i++) {
            if (groups[i].groupKey === groupKey) {
                targetIndex = i;
                break;
            }
        }
        if (targetIndex <= 0) return null;
        for (var j = targetIndex - 1; j >= 0; j--) {
            if ((rentalState.rentalsByGroup[groups[j].groupKey] || []).length > 0) {
                return groups[j];
            }
        }
        return null;
    }
    function cloneRentalItems(items) {
        return (items || []).map(function (item) {
            return {
                inventoryId: Number(item.inventoryId),
                name: item.name,
                quantity: Number(item.quantity || 0),
                unitPrice: Number(item.unitPrice || 0)
            };
        });
    }
    function refreshRentalSection() {
        renderRentalSection();
        if (rentalState.selectedGroup && rentalState.selectedGroup.groupKey) {
            var refreshedGroup = findGroupByKey(rentalState.selectedGroup.groupKey);
            if (refreshedGroup) {
                rentalState.selectedGroup = refreshedGroup;
            }
            renderRentalInventoryTable();
        }
    }
    async function applyPreviousRental(groupKey) {
        var sourceGroup = findPreviousConfiguredGroup(groupKey);
        if (!sourceGroup) {
            showError('Chua co bo do thue truoc do de ap dung.');
            return;
        }

        var sourceItems = rentalState.rentalsByGroup[sourceGroup.groupKey] || [];
        if (!sourceItems.length) {
            showError('Nhom truoc chua co do thue de ap dung.');
            return;
        }

        var currentItems = rentalState.rentalsByGroup[groupKey] || [];
        if (currentItems.length) {
            var confirmed = await uiConfirm(
                'Ap dung se ghi de bo do thue hien tai cua nhom slot nay. Tiep tuc?',
                'Ghi de do thue'
            );
            if (!confirmed) {
                return;
            }
        }

        rentalState.rentalsByGroup[groupKey] = cloneRentalItems(sourceItems);
        refreshRentalSection();
        hideError();
    }

    // =========================
    // Rental modal logic
    // =========================
    function loadRentalInventory() {
        if (!rentalState.selectedGroup) return;

        var url = CTX + '/api/staff/rental/inventory?page=' + encodeURIComponent(rentalState.page) +
            '&q=' + encodeURIComponent(rentalState.keyword || '');

        fetch(url, {
            method: 'GET',
            credentials: 'same-origin',
            headers: { 'Accept': 'application/json' }
        })
            .then(function (res) { return res.json(); })
            .then(function (json) {
                if (!json.success || !json.data) {
                    throw new Error(json.message || 'Không tải được danh sách đồ thuê');
                }

                rentalState.items = json.data.items || [];
                rentalState.totalPages = Number(json.data.totalPages || 1);
                renderRentalInventoryTable();
            })
            .catch(function (err) {
                console.error('Rental inventory load error:', err);
                rentalInventoryTableBody.innerHTML =
                    '<tr><td colspan="7" class="text-center text-danger">Không thể tải danh sách đồ thuê.</td></tr>';
                if (rentalInventoryEmpty) rentalInventoryEmpty.classList.add('d-none');
                rentalPaginationInfo.textContent = 'Lỗi tải dữ liệu';
            });
    }

    function renderRentalInventoryTable() {
        var items = rentalState.items || [];
        rentalInventoryTableBody.innerHTML = '';

        if (!items.length) {
            if (rentalInventoryEmpty) rentalInventoryEmpty.classList.remove('d-none');
            rentalInventoryTableBody.innerHTML = '';
        } else {
            if (rentalInventoryEmpty) rentalInventoryEmpty.classList.add('d-none');

            var startIndex = ((rentalState.page - 1) * 5) + 1;
            rentalInventoryTableBody.innerHTML = items.map(function (item, idx) {
                return buildRentalRow(item, startIndex + idx);
            }).join('');
        }

        rentalPaginationInfo.textContent = 'Trang ' + rentalState.page + ' / ' + rentalState.totalPages;
        btnRentalPrev.disabled = rentalState.page <= 1;
        btnRentalNext.disabled = rentalState.page >= rentalState.totalPages;
    }

    function buildRentalRow(item, index) {
        var selectedGroup = rentalState.selectedGroup;
        var groupKey = selectedGroup ? selectedGroup.groupKey : '';
        var selectedItems = rentalState.rentalsByGroup[groupKey] || [];
        var existed = selectedItems.find(function (x) {
            return Number(x.inventoryId) === Number(item.inventoryId);
        });

        var actionHtml = '';

        if (existed) {
            actionHtml =
                '<div class="d-flex flex-column gap-2">' +
                '   <div><span class="badge bg-success">Đã thuê</span></div>' +
                '   <div class="input-group input-group-sm">' +
                '       <span class="input-group-text">SL</span>' +
                '       <input type="number" min="0" max="' + Number(item.availableQuantity || 0) + '" class="form-control" ' +
                '              id="edit_qty_' + Number(item.inventoryId) + '" value="' + Number(existed.quantity || 0) + '">' +
                '   </div>' +
                '   <div class="d-flex gap-2">' +
                '       <button class="btn btn-sm btn-warning" type="button" onclick="saveRentalEdit(' + Number(item.inventoryId) + ', ' + Number(item.rentalPrice || 0) + ', \'' + jsString(item.name || '') + '\', ' + Number(item.availableQuantity || 0) + ')">' +
                '           Lưu' +
                '       </button>' +
                '       <button class="btn btn-sm btn-danger" type="button" onclick="removeRentalItem(' + Number(item.inventoryId) + ')">' +
                '           Bỏ' +
                '       </button>' +
                '   </div>' +
                '</div>';
        } else {
            actionHtml =
                '<div id="selectBtnWrap_' + Number(item.inventoryId) + '">' +
                '   <button class="btn btn-sm btn-outline-primary" type="button" onclick="selectRentalItem(' + Number(item.inventoryId) + ')">' +
                '       Chọn' +
                '   </button>' +
                '</div>' +
                '<div class="mt-2 d-none" id="qtyWrap_' + Number(item.inventoryId) + '">' +
                '   <input type="number" min="1" max="' + Number(item.availableQuantity || 0) + '" class="form-control form-control-sm mb-2" ' +
                '          id="qty_' + Number(item.inventoryId) + '" oninput="onQtyChange(' + Number(item.inventoryId) + ', ' + Number(item.availableQuantity || 0) + ')">' +
                '   <div class="d-flex gap-2">' +
                '       <button class="btn btn-sm btn-success" disabled id="rentBtn_' + Number(item.inventoryId) + '" type="button" onclick="rentItem(' + Number(item.inventoryId) + ', ' + Number(item.rentalPrice || 0) + ', \'' + jsString(item.name || '') + '\')">' +
                '           Thuê' +
                '       </button>' +
                '       <button class="btn btn-sm btn-secondary" type="button" onclick="cancelSelectRentalItem(' + Number(item.inventoryId) + ')">' +
                '           Hủy' +
                '       </button>' +
                '   </div>' +
                '</div>';
        }

        return '' +
            '<tr>' +
            '   <td>' + index + '</td>' +
            '   <td>' + escapeHtml(item.name || '') + '</td>' +
            '   <td>' + escapeHtml(item.brand || '') + '</td>' +
            '   <td>' + escapeHtml(item.description || '') + '</td>' +
            '   <td>' + formatMoney(item.rentalPrice || 0) + '</td>' +
            '   <td>' + Number(item.availableQuantity || 0) + '</td>' +
            '   <td>' + actionHtml + '</td>' +
            '</tr>';
    }

    function selectRentalItem(inventoryId) {
        var wrap = document.getElementById('qtyWrap_' + inventoryId);
        var btnWrap = document.getElementById('selectBtnWrap_' + inventoryId);

        if (btnWrap) btnWrap.classList.add('d-none');
        if (wrap) wrap.classList.remove('d-none');
    }

    function cancelSelectRentalItem(inventoryId) {
        var wrap = document.getElementById('qtyWrap_' + inventoryId);
        var btnWrap = document.getElementById('selectBtnWrap_' + inventoryId);
        var qtyEl = document.getElementById('qty_' + inventoryId);
        var rentBtn = document.getElementById('rentBtn_' + inventoryId);

        if (wrap) wrap.classList.add('d-none');
        if (btnWrap) btnWrap.classList.remove('d-none');
        if (qtyEl) qtyEl.value = '';
        if (rentBtn) rentBtn.disabled = true;
    }

    function onQtyChange(inventoryId, maxQty) {
        var qtyEl = document.getElementById('qty_' + inventoryId);
        var btn = document.getElementById('rentBtn_' + inventoryId);
        if (!qtyEl || !btn) return;

        var qty = parseInt(qtyEl.value || '0', 10);
        var valid = !isNaN(qty) && qty > 0 && qty <= Number(maxQty || 0);

        btn.disabled = !valid;
    }

    async function rentItem(inventoryId, unitPrice, name) {
        var qtyEl = document.getElementById('qty_' + inventoryId);
        var qty = parseInt((qtyEl && qtyEl.value) || '0', 10);

        if (!rentalState.selectedGroup) return;

        var item = (rentalState.items || []).find(function (x) {
            return Number(x.inventoryId) === Number(inventoryId);
        });

        if (!item) {
            showError('Không tìm thấy đồ thuê');
            return;
        }

        if (isNaN(qty) || qty <= 0) {
            showError('Số lượng thuê phải lớn hơn 0');
            return;
        }

        if (qty > Number(item.availableQuantity || 0)) {
            showError('Số lượng thuê vượt quá số lượng khả dụng');
            return;
        }

        var groupKey = rentalState.selectedGroup.groupKey;
        if (!rentalState.rentalsByGroup[groupKey]) {
            rentalState.rentalsByGroup[groupKey] = [];
        }

        var existed = rentalState.rentalsByGroup[groupKey].find(function (x) {
            return Number(x.inventoryId) === Number(inventoryId);
        });

        if (existed) {
            existed.quantity = qty;
            existed.unitPrice = Number(unitPrice);
            existed.name = name;
        } else {
            rentalState.rentalsByGroup[groupKey].push({
                inventoryId: Number(inventoryId),
                name: name,
                quantity: qty,
                unitPrice: Number(unitPrice)
            });
        }

        refreshRentalSection();
        hideError();
    }
    async function saveRentalEdit(inventoryId, unitPrice, name, maxQty) {
        if (!rentalState.selectedGroup) return;

        var qtyEl = document.getElementById('edit_qty_' + inventoryId);
        var qty = parseInt((qtyEl && qtyEl.value) || '0', 10);

        if (isNaN(qty) || qty < 0 || qty > Number(maxQty || 0)) {
            showError('Số lượng sửa phải >= 0 và <= số lượng khả dụng');
            return;
        }

        if (qty === 0) {
            removeRentalItem(inventoryId);
            return;
        }

        var groupKey = rentalState.selectedGroup.groupKey;
        var list = rentalState.rentalsByGroup[groupKey] || [];
        var existed = list.find(function (x) {
            return Number(x.inventoryId) === Number(inventoryId);
        });

        if (!existed) {
            showError('Đồ thuê chưa tồn tại trong nhóm này');
            return;
        }

        existed.quantity = qty;
        existed.unitPrice = Number(unitPrice);
        existed.name = name;

        refreshRentalSection();
        hideError();
    }








    async function removeRentalItem(inventoryId) {
        if (!rentalState.selectedGroup) return;

        var groupKey = rentalState.selectedGroup.groupKey;
        var list = rentalState.rentalsByGroup[groupKey] || [];

        rentalState.rentalsByGroup[groupKey] = list.filter(function (x) {
            return Number(x.inventoryId) !== Number(inventoryId);
        });

        refreshRentalSection();
        hideError();
    }
    function openRentalModal(groupKey) {
        var group = findGroupByKey(groupKey);
        if (!group) return;

        rentalState.selectedGroup = group;
        rentalState.page = 1;
        rentalState.keyword = '';

        if (rentalSearchInput) rentalSearchInput.value = '';
        if (rentalModalContext) {
            rentalModalContext.textContent = group.courtName + ' - ' + group.startTime + ' đến ' + group.endTime;
        }

        loadRentalInventory();

        if (rentalModal) {
            rentalModal.show();
        } else {
            var modalEl = document.getElementById('rentalInventoryModal');
            if (modalEl) modalEl.classList.add('show');
        }
    }

    // Expose for inline onclick
    window.openRentalModal = openRentalModal;
    window.selectRentalItem = selectRentalItem;
    window.cancelSelectRentalItem = cancelSelectRentalItem;
    window.onQtyChange = onQtyChange;
    window.rentItem = rentItem;
    window.saveRentalEdit = saveRentalEdit;
    window.removeRentalItem = removeRentalItem;
    window.applyPreviousRental = applyPreviousRental;

    // =========================
    // Events
    // =========================
    function bindEvents() {
        bindCustomerModeEvents();
        bindSearchEvents();
        bindGuestPhoneEvents();
        bindRentalEvents();
        bindSubmitEvent();
    }

    function bindCustomerModeEvents() {
        tabAccount.addEventListener('click', function () {
            customerType = 'ACCOUNT';
            tabAccount.classList.add('active');
            tabGuest.classList.remove('active');
            formAccount.classList.remove('d-none');
            formGuest.classList.add('d-none');
            hideError();
        });

        tabGuest.addEventListener('click', function () {
            customerType = 'GUEST';
            tabGuest.classList.add('active');
            tabAccount.classList.remove('active');
            formGuest.classList.remove('d-none');
            formAccount.classList.add('d-none');
            hideError();
        });

        btnRemoveCustomer.addEventListener('click', function () {
            selectedAccountId.value = '';
            selectedCustomer.classList.add('d-none');
        });
    }

    function bindSearchEvents() {
        customerSearch.addEventListener('input', function () {
            var q = this.value.trim();
            if (q.length < 2) {
                searchDropdown.classList.add('d-none');
                return;
            }

            clearTimeout(searchTimer);
            searchTimer = setTimeout(function () {
                fetch(CTX + '/api/staff/customer/search?q=' + encodeURIComponent(q), {
                    credentials: 'same-origin',
                    headers: { 'Accept': 'application/json' }
                })
                    .then(function (res) { return res.json(); })
                    .then(function (body) {
                        if (!body.success) return;
                        renderSearchResults(body.data.customers || []);
                    })
                    .catch(function (err) {
                        console.error('Search error:', err);
                    });
            }, 300);
        });

        document.addEventListener('click', function (e) {
            if (!e.target.closest('.sbc-search-wrap')) {
                searchDropdown.classList.add('d-none');
            }
        });
    }

    function bindGuestPhoneEvents() {
        if (!guestPhoneInput) return;

        guestPhoneInput.addEventListener('input', function () {
            var raw = this.value.replace(/[^\d]/g, '');
            if (raw.length > 10) raw = raw.substring(0, 10);
            this.value = raw;
            updatePhoneHint(raw);
        });

        guestPhoneInput.addEventListener('paste', function () {
            var self = this;
            setTimeout(function () {
                var raw = self.value.replace(/[^\d]/g, '');
                if (raw.length > 10) raw = raw.substring(0, 10);
                self.value = raw;
                updatePhoneHint(raw);
            }, 0);
        });
    }

    function bindRentalEvents() {
        if (btnRentalSearch) {
            btnRentalSearch.addEventListener('click', function () {
                rentalState.keyword = (rentalSearchInput.value || '').trim();
                rentalState.page = 1;
                loadRentalInventory();
            });
        }

        if (rentalSearchInput) {
            rentalSearchInput.addEventListener('keydown', function (e) {
                if (e.key === 'Enter') {
                    e.preventDefault();
                    rentalState.keyword = (rentalSearchInput.value || '').trim();
                    rentalState.page = 1;
                    loadRentalInventory();
                }
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
    }

    function bindSubmitEvent() {
        btnSubmit.addEventListener('click', async function () {
            hideError();

            if (customerType === 'ACCOUNT') {
                if (!selectedAccountId.value) {
                    showError('Vui lòng tìm và chọn khách hàng');
                    return;
                }
            } else {
                if (!guestNameInput.value.trim()) {
                    showError('Vui lòng nhập họ tên khách');
                    guestNameInput.focus();
                    return;
                }
                if (!guestPhoneInput.value.trim()) {
                    showError('Vui lòng nhập số điện thoại');
                    guestPhoneInput.focus();
                    return;
                }
                if (!isValidPhone(guestPhoneInput.value)) {
                    showError('Số điện thoại phải dùng 10 chữ số và bắt đầu bằng 0');
                    guestPhoneInput.focus();
                    return;
                }
            }

            var slotsPayload = bookingData.slots.map(function (s) {
                return {
                    courtId: s.courtId,
                    slotId: s.slotId
                };
            });

            var rentalPayload = [];
            Object.keys(rentalState.rentalsByGroup).forEach(function (groupKey) {
                var group = findGroupByKey(groupKey);
                if (!group) return;

                (rentalState.rentalsByGroup[groupKey] || []).forEach(function (item) {
                    rentalPayload.push({
                        groupKey: groupKey,
                        courtId: group.courtId,
                        courtName: group.courtName,
                        startTime: group.startTime,
                        endTime: group.endTime,
                        slotIds: (group.slots || []).map(function (s) {
                            return s.slotId;
                        }),
                        inventoryId: item.inventoryId,
                        name: item.name,
                        quantity: item.quantity,
                        unitPrice: item.unitPrice
                    });
                });
            });

            var reqBody = {
                date: bookingData.date,
                customerType: customerType,
                accountId: customerType === 'ACCOUNT' ? parseInt(selectedAccountId.value, 10) : null,
                guestName: customerType === 'GUEST' ? guestNameInput.value.trim() : null,
                guestPhone: customerType === 'GUEST' ? guestPhoneInput.value.trim() : null,
                guestEmail: customerType === 'GUEST' ? (guestEmailInput ? guestEmailInput.value.trim() : null) : null,
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
                    headers: {
                        'Content-Type': 'application/json',
                        'Accept': 'application/json'
                    },
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

                    showError(body.message || 'Đặt sân thất bại');
                    resetSubmitButton();
                    return;
                }

                sessionStorage.removeItem('staffBookingSlots');
                window.location.href = CTX + '/staff/booking/detail/' + body.data.bookingId;
            } catch (err) {
                console.error('Create error:', err);
                showError('Lỗi kết nối. Vui lòng thử lại');
                resetSubmitButton();
            }
        });
    }

    // =========================
    // Customer helpers
    // =========================
    function renderSearchResults(customers) {
        searchDropdown.innerHTML = '';

        if (customers.length === 0) {
            searchDropdown.innerHTML = '<div class="sbc-search-empty">Không tìm thấy</div>';
            searchDropdown.classList.remove('d-none');
            return;
        }

        customers.forEach(function (c) {
            var item = document.createElement('div');
            item.className = 'sbc-search-item';
            item.innerHTML =
                '<div class="sbc-search-name">' + escapeHtml(c.fullName) + '</div>' +
                '<div class="sbc-search-detail">' + escapeHtml(c.phone || '') + ' · ' + escapeHtml(c.email || '') + '</div>';
            item.addEventListener('click', function () {
                selectCustomer(c);
            });
            searchDropdown.appendChild(item);
        });

        searchDropdown.classList.remove('d-none');
    }

    function selectCustomer(c) {
        selectedAccountId.value = c.accountId;
        selName.textContent = c.fullName;
        selPhone.textContent = c.phone || '—';
        selEmail.textContent = c.email || '—';
        selectedCustomer.classList.remove('d-none');
        customerSearch.value = '';
        searchDropdown.classList.add('d-none');
        hideError();
    }

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
    }

    function confirmGuestPhoneMatched(matched) {
        var msg = 'Số điện thoại này đã tồn tại tài khoản CUSTOMER:\n' +
            '- ' + (matched.fullName || 'Không rõ tên') + '\n' +
            '- ' + (matched.phone || '') + '\n\n' +
            'Hệ thống sẽ chuyển sang luồng Khách có tài khoản. Tiếp tục?';
        return uiConfirm(msg, 'Trùng số điện thoại');
    }

    // =========================
    // Generic helpers
    // =========================
    function resetSubmitButton() {
        btnSubmit.disabled = false;
        btnSubmit.innerHTML = '<i class="bi bi-check-circle me-2"></i>Xác nhận đặt sân';
    }

    function uiConfirm(message, title) {
        if (window.StaffDialog && typeof window.StaffDialog.confirm === 'function') {
            return window.StaffDialog.confirm({
                title: title || 'Xác nhận',
                message: message || ''
            });
        }
        return Promise.resolve(window.confirm(message || ''));
    }

    function showError(msg) {
        formError.textContent = msg;
        formError.classList.remove('d-none');
    }

    function hideError() {
        formError.classList.add('d-none');
    }

    function isValidPhone(phone) {
        var cleaned = String(phone || '').replace(/\s+/g, '');
        return /^0\d{9}$/.test(cleaned);
    }

    function updatePhoneHint(digits) {
        if (!phoneHint || !guestPhoneInput) return;

        if (digits.length === 0) {
            phoneHint.classList.add('d-none');
            guestPhoneInput.classList.remove('sbc-input-error');
            guestPhoneInput.classList.remove('sbc-input-valid');
            return;
        }

        phoneHint.classList.remove('d-none');

        if (digits.length < 10) {
            phoneHint.textContent = 'Còn thiếu ' + (10 - digits.length) + ' số';
            phoneHint.className = 'sbc-phone-hint sbc-hint-warn';
            guestPhoneInput.classList.remove('sbc-input-valid');
            guestPhoneInput.classList.add('sbc-input-error');
        } else if (digits.length === 10 && digits.charAt(0) === '0') {
            phoneHint.textContent = '✓ Số điện thoại hợp lệ';
            phoneHint.className = 'sbc-phone-hint sbc-hint-ok';
            guestPhoneInput.classList.remove('sbc-input-error');
            guestPhoneInput.classList.add('sbc-input-valid');
        } else {
            phoneHint.textContent = 'Số điện thoại phải bắt đầu bằng 0';
            phoneHint.className = 'sbc-phone-hint sbc-hint-warn';
            guestPhoneInput.classList.remove('sbc-input-valid');
            guestPhoneInput.classList.add('sbc-input-error');
        }
    }

    function formatMoney(amount) {
        if (amount == null || isNaN(Number(amount))) return '0đ';
        return Number(amount).toLocaleString('vi-VN') + 'đ';
    }

    function escapeHtml(str) {
        if (str == null) return '';
        return String(str)
            .replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;')
            .replace(/"/g, '&quot;')
            .replace(/'/g, '&#39;');
    }

    function jsString(str) {
        if (str == null) return '';
        return String(str)
            .replace(/\\/g, '\\\\')
            .replace(/'/g, '\\\'')
            .replace(/"/g, '\\"')
            .replace(/\r/g, '')
            .replace(/\n/g, '\\n');
    }

    function safeId(str) {
        return String(str || '').replace(/[^a-zA-Z0-9_-]/g, '_');
    }

    function normalizeTime(timeStr) {
        if (!timeStr) return '';
        var s = String(timeStr).trim();
        if (s.length >= 5) return s.substring(0, 5);
        return s;
    }

    function formatDate(dateStr) {
        if (!dateStr) return '—';
        var d = new Date(dateStr + 'T00:00:00');
        return String(d.getDate()).padStart(2, '0') + '/' +
            String(d.getMonth() + 1).padStart(2, '0') + '/' +
            d.getFullYear();
    }

    function buildSessions(slots) {
        var groups = {};
        slots.forEach(function (s) {
            if (!groups[s.courtId]) groups[s.courtId] = [];
            groups[s.courtId].push(s);
        });

        var result = [];

        for (var courtId in groups) {
            if (!Object.prototype.hasOwnProperty.call(groups, courtId)) continue;

            var courtSlots = groups[courtId];
            courtSlots.sort(function (a, b) {
                return a.startTime.localeCompare(b.startTime);
            });

            var currentSession = [courtSlots[0]];

            for (var i = 1; i < courtSlots.length; i++) {
                var prev = currentSession[currentSession.length - 1];
                var cur = courtSlots[i];

                if (prev.endTime === cur.startTime) {
                    currentSession.push(cur);
                } else {
                    result.push(currentSession);
                    currentSession = [cur];
                }
            }

            result.push(currentSession);
        }

        result.sort(function (a, b) {
            return a[0].startTime.localeCompare(b[0].startTime);
        });

        return result;
    }

    function buildSessionKey(session) {
        if (!session || !session.length) return '';
        return 'court_' + session[0].courtId + '__' +
            session[0].startTime.replace(/:/g, '') + '__' +
            session[session.length - 1].endTime.replace(/:/g, '');
    }

})();
