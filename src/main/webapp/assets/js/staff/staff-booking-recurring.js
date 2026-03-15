(function () {
    'use strict';

    var CTX = window.ST_CTX || '';

    var startDateEl = document.getElementById('startDate');
    var endDateEl = document.getElementById('endDate');
    var conflictPolicyEl = document.getElementById('conflictPolicy');
    var paymentMethodEl = document.getElementById('paymentMethod');
    var patternsContainer = document.getElementById('patternsContainer');
    var btnAddPattern = document.getElementById('btnAddPattern');
    var btnPreview = document.getElementById('btnPreview');
    var btnConfirm = document.getElementById('btnConfirm');
    var formError = document.getElementById('formError');

    var previewSection = document.getElementById('previewSection');
    var previewList = document.getElementById('previewList');
    var previewTotal = document.getElementById('previewTotal');
    var previewPolicy = document.getElementById('previewPolicy');
    var weeklyView = document.getElementById('weeklyView');

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

    var customerType = 'ACCOUNT';
    var searchTimer = null;
    var courts = [];
    var slots = [];
    var previewData = null;
    var selectionMap = {};

    init();

    function init() {
        setupTabs();
        setupCustomerSearch();
        btnAddPattern.addEventListener('click', addPatternRow);
        btnPreview.addEventListener('click', onPreview);
        btnConfirm.addEventListener('click', onConfirm);
        patternsContainer.addEventListener('change', renderWeeklyView);
        loadTimelineMeta();
        addPatternRow();
    }

    function setupTabs() {
        tabAccount.addEventListener('click', function () {
            customerType = 'ACCOUNT';
            tabAccount.classList.add('active');
            tabGuest.classList.remove('active');
            formAccount.classList.remove('d-none');
            formGuest.classList.add('d-none');
        });
        tabGuest.addEventListener('click', function () {
            customerType = 'GUEST';
            tabGuest.classList.add('active');
            tabAccount.classList.remove('active');
            formGuest.classList.remove('d-none');
            formAccount.classList.add('d-none');
        });
    }

    function setupCustomerSearch() {
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
                    .catch(function (err) { console.error('Search error:', err); });
            }, 300);
        });

        btnRemoveCustomer.addEventListener('click', function () {
            selectedAccountId.value = '';
            selectedCustomer.classList.add('d-none');
        });

        document.addEventListener('click', function (e) {
            if (!e.target.closest('.sbr-search-wrap')) {
                searchDropdown.classList.add('d-none');
            }
        });
    }

    function renderSearchResults(customers) {
        searchDropdown.innerHTML = '';
        if (customers.length === 0) {
            searchDropdown.innerHTML = '<div class="sbr-search-item">Không tìm thấy</div>';
            searchDropdown.classList.remove('d-none');
            return;
        }
        customers.forEach(function (c) {
            var item = document.createElement('div');
            item.className = 'sbr-search-item';
            item.innerHTML = '<strong>' + escapeHtml(c.fullName) + '</strong><br>' +
                '<small>' + escapeHtml(c.phone || '') + ' · ' + escapeHtml(c.email || '') + '</small>';
            item.addEventListener('click', function () { selectCustomer(c); });
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
    }

    function loadTimelineMeta() {
        var today = new Date();
        var dateStr = today.getFullYear() + '-' + String(today.getMonth() + 1).padStart(2, '0') + '-' + String(today.getDate()).padStart(2, '0');
        fetch(CTX + '/api/staff/timeline?date=' + dateStr, {
            credentials: 'same-origin',
            headers: { 'Accept': 'application/json' }
        })
            .then(function (res) { return res.json(); })
            .then(function (body) {
                if (!body.success) throw new Error(body.message || 'Lỗi tải dữ liệu');
                courts = body.data.courts || [];
                slots = body.data.slots || [];
                updatePatternOptions();
            })
            .catch(function (err) {
                showError('Không thể tải dữ liệu sân/slot: ' + err.message);
            });
    }

    function updatePatternOptions() {
        var rows = patternsContainer.querySelectorAll('.sbr-pattern-row');
        rows.forEach(function (row) {
            fillCourtOptions(row.querySelector('.sbr-court'));
            fillTimeOptions(row.querySelector('.sbr-start'), row.querySelector('.sbr-end'));
        });
        renderWeeklyView();
    }

    function addPatternRow() {
        var row = document.createElement('div');
        row.className = 'sbr-pattern-row';
        row.innerHTML =
            '<select class="sbr-input sbr-day">' +
            '<option value="2">Thứ 2</option>' +
            '<option value="3">Thứ 3</option>' +
            '<option value="4">Thứ 4</option>' +
            '<option value="5">Thứ 5</option>' +
            '<option value="6">Thứ 6</option>' +
            '<option value="7">Thứ 7</option>' +
            '<option value="1">Chủ nhật</option>' +
            '</select>' +
            '<select class="sbr-input sbr-court"></select>' +
            '<select class="sbr-input sbr-start"></select>' +
            '<select class="sbr-input sbr-end"></select>' +
            '<button type="button" class="sbr-btn-icon sbr-remove"><i class="bi bi-trash"></i></button>';
        row.querySelector('.sbr-remove').addEventListener('click', function () {
            row.remove();
            renderWeeklyView();
        });
        patternsContainer.appendChild(row);
        fillCourtOptions(row.querySelector('.sbr-court'));
        fillTimeOptions(row.querySelector('.sbr-start'), row.querySelector('.sbr-end'));
        renderWeeklyView();
    }

    function fillCourtOptions(select) {
        if (!select) return;
        select.innerHTML = '';
        courts.forEach(function (c) {
            var opt = document.createElement('option');
            opt.value = c.courtId;
            opt.textContent = c.courtName;
            select.appendChild(opt);
        });
    }

    function fillTimeOptions(startSelect, endSelect) {
        if (!startSelect || !endSelect) return;
        startSelect.innerHTML = '';
        endSelect.innerHTML = '';
        slots.forEach(function (s) {
            var optStart = document.createElement('option');
            optStart.value = s.startTime;
            optStart.textContent = s.startTime;
            startSelect.appendChild(optStart);

            var optEnd = document.createElement('option');
            optEnd.value = s.endTime;
            optEnd.textContent = s.endTime;
            endSelect.appendChild(optEnd);
        });
    }

    function onPreview() {
        hideError();
        var req = buildRequestBody();
        if (!req) return;

        btnPreview.disabled = true;
        btnPreview.textContent = 'Đang xem trước...';

        fetch(CTX + '/api/staff/recurring-booking/preview', {
            method: 'POST',
            credentials: 'same-origin',
            headers: { 'Content-Type': 'application/json', 'Accept': 'application/json' },
            body: JSON.stringify(req)
        })
            .then(function (res) { return res.json(); })
            .then(function (body) {
                if (!body.success) {
                    handleGuestPhoneMatched(body);
                    showError(body.message || 'Xem trước thất bại');
                    return;
                }
                previewData = body.data;
                selectionMap = {};
                renderPreview(body.data);
                btnConfirm.disabled = false;
            })
            .catch(function (err) {
                showError('Lỗi kết nối: ' + err.message);
            })
            .finally(function () {
                btnPreview.disabled = false;
                btnPreview.innerHTML = '<i class="bi bi-eye"></i>Xem trước';
            });
    }

    function onConfirm() {
        hideError();
        if (!previewData) {
            showError('Vui lòng xem trước trước khi xác nhận');
            return;
        }

        var req = buildRequestBody();
        if (!req) return;

        var policy = conflictPolicyEl.value;
        if (policy === 'SUGGEST') {
            var selected = collectSelectedSessions();
            if (!selected) return;
            req.selectedSessions = selected;
        }
        req.paymentMethod = paymentMethodEl.value;

        if (req.customerType === 'GUEST') {
            if (guestEmailInput && guestEmailInput.value.trim() && !isValidEmail(guestEmailInput.value)) {
                showError('Email không đúng định dạng');
                guestEmailInput.focus();
                return;
            }
        }

        btnConfirm.disabled = true;
        btnConfirm.textContent = 'Đang xác nhận...';

        var proceed = Promise.resolve(true);
        if (req.customerType === 'GUEST' && guestEmailInput && !guestEmailInput.value.trim()) {
            proceed = uiConfirm(
                'Không có email, hệ thống sẽ không gửi thông báo. Tiếp tục?',
                'Cảnh báo'
            );
        }

        proceed.then(function (ok) {
            if (!ok) {
                btnConfirm.disabled = false;
                btnConfirm.innerHTML = '<i class="bi bi-check-circle"></i>Xác nhận thanh toán';
                return;
            }

            fetch(CTX + '/api/staff/recurring-booking/confirm', {
                method: 'POST',
                credentials: 'same-origin',
                headers: { 'Content-Type': 'application/json', 'Accept': 'application/json' },
                body: JSON.stringify(req)
            })
                .then(function (res) { return res.json(); })
                .then(function (body) {
                    if (!body.success) {
                        handleGuestPhoneMatched(body);
                        showError(body.message || 'Xác nhận thất bại');
                        return;
                    }
                    if (body.data && body.data.emailWarning) {
                        return uiAlert(body.data.emailWarning, 'Thông báo').then(function () {
                            window.location.href = CTX + '/staff/booking/detail/' + body.data.bookingId;
                        });
                    }
                    window.location.href = CTX + '/staff/booking/detail/' + body.data.bookingId;
                })
                .catch(function (err) {
                    showError('Lỗi kết nối: ' + err.message);
                })
                .finally(function () {
                    btnConfirm.disabled = false;
                    btnConfirm.innerHTML = '<i class="bi bi-check-circle"></i>Xác nhận thanh toán';
                });
        });
    }

    function buildRequestBody() {
        var startDate = startDateEl.value;
        var endDate = endDateEl.value;
        if (!startDate || !endDate) {
            showError('Vui lòng chọn ngày bắt đầu và ngày kết thúc');
            return null;
        }
        if (!isMinFourWeeks(startDate, endDate)) {
            showError('Thời gian định kỳ phải tối thiểu 4 tuần');
            return null;
        }

        var patterns = collectPatterns();
        if (!patterns) return null;

        var req = {
            startDate: startDate,
            endDate: endDate,
            customerType: customerType,
            accountId: customerType === 'ACCOUNT' ? parseInt(selectedAccountId.value || '0', 10) : null,
            guestName: customerType === 'GUEST' ? guestNameInput.value.trim() : null,
            guestPhone: customerType === 'GUEST' ? guestPhoneInput.value.trim() : null,
            guestEmail: customerType === 'GUEST' && guestEmailInput ? guestEmailInput.value.trim() : null,
            conflictPolicy: conflictPolicyEl.value,
            patterns: patterns
        };

        if (req.customerType === 'ACCOUNT' && (!req.accountId || req.accountId <= 0)) {
            showError('Vui lòng chọn khách hàng');
            return null;
        }
        if (req.customerType === 'GUEST') {
            if (!req.guestName) {
                showError('Vui lòng nhập họ tên khách');
                return null;
            }
            if (!req.guestPhone) {
                showError('Vui lòng nhập số điện thoại');
                return null;
            }
        }

        return req;
    }

    function collectPatterns() {
        var rows = patternsContainer.querySelectorAll('.sbr-pattern-row');
        if (!rows.length) {
            showError('Vui lòng thêm ít nhất 1 lịch');
            return null;
        }

        var usedDays = {};
        var list = [];
        for (var i = 0; i < rows.length; i++) {
            var day = parseInt(rows[i].querySelector('.sbr-day').value, 10);
            if (usedDays[day]) {
                showError('Mỗi ngày chỉ được đặt 1 phiên');
                return null;
            }
            usedDays[day] = true;
            var courtId = parseInt(rows[i].querySelector('.sbr-court').value, 10);
            var startTime = rows[i].querySelector('.sbr-start').value;
            var endTime = rows[i].querySelector('.sbr-end').value;

            var slotIds = toSlotIds(startTime, endTime);
            if (!slotIds) {
                showError('Khung giờ không hợp lệ, cần tối thiểu 2 slot liên tiếp');
                return null;
            }

            list.push({ dayOfWeek: day, courtId: courtId, slotIds: slotIds });
        }
        return list;
    }

    function toSlotIds(startTime, endTime) {
        var startIdx = slots.findIndex(function (s) { return s.startTime === startTime; });
        var endIdx = slots.findIndex(function (s) { return s.endTime === endTime; });
        if (startIdx < 0 || endIdx < 0 || endIdx < startIdx) return null;
        var ids = [];
        for (var i = startIdx; i <= endIdx; i++) ids.push(slots[i].slotId);
        if (ids.length < 2) return null;
        return ids;
    }

    function renderPreview(data) {
        previewSection.classList.remove('d-none');
        previewTotal.textContent = formatMoney(data.totalAmount || 0);
        previewPolicy.textContent = data.policy || '';
        previewList.innerHTML = '';

        (data.sessions || []).forEach(function (s) {
            var item = document.createElement('div');
            item.className = 'sbr-preview-item';
            var badge = s.status === 'OK' ? 'sbr-badge-ok' : (s.status === 'SKIPPED' ? 'sbr-badge-skipped' : 'sbr-badge-conflict');
            var courtLabel = courtNameById(s.courtId) || ('Sân #' + s.courtId);
            item.innerHTML =
                '<h4>' + formatDate(s.date) + ' · ' + courtLabel + '</h4>' +
                '<div><span class="sbr-badge ' + badge + '">' + s.status + '</span></div>' +
                '<div>Tổng tiền: <strong>' + formatMoney(s.amount || 0) + '</strong></div>';

            if (s.status === 'CONFLICT') {
                var conflict = findConflict(data.conflicts || [], s.date);
                var suggestWrap = document.createElement('div');
                suggestWrap.className = 'sbr-suggest-list';

                if (conflict && conflict.suggestions && conflict.suggestions.length) {
                    conflict.suggestions.forEach(function (sg, idx) {
                        var id = 'sg_' + s.date + '_' + idx;
                        var line = document.createElement('label');
                        line.className = 'sbr-suggest-item';
                        line.innerHTML =
                            '<input type="radio" name="suggest_' + s.date + '" value="' + id + '">' +
                            '<span>Sân #' + sg.courtId + ' · ' + sg.startTime + ' → ' + sg.endTime + ' · ' +
                            formatMoney(sg.amount || 0) + '</span>';
                        line.dataset.mode = 'suggestion';
                        line.dataset.courtId = sg.courtId;
                        line.dataset.slotIds = (sg.slotIds || []).join(',');
                        suggestWrap.appendChild(line);
                    });
                }

                var manual = document.createElement('div');
                manual.className = 'sbr-manual';
                manual.innerHTML =
                    '<label class="sbr-suggest-item">' +
                    '<input type="radio" name="suggest_' + s.date + '" value="manual">' +
                    '<span>Chọn thủ công</span>' +
                    '</label>' +
                    '<div class="sbr-row">' +
                    '<select class="sbr-input sbr-manual-court"></select>' +
                    '<select class="sbr-input sbr-manual-start"></select>' +
                    '<select class="sbr-input sbr-manual-end"></select>' +
                    '</div>' +
                    '<small>Chưa tính giá, sẽ tính khi xác nhận</small>';
                suggestWrap.appendChild(manual);

                item.appendChild(suggestWrap);

                fillCourtOptions(manual.querySelector('.sbr-manual-court'));
                fillTimeOptions(manual.querySelector('.sbr-manual-start'), manual.querySelector('.sbr-manual-end'));
            }

            previewList.appendChild(item);
        });
    }

    function renderWeeklyView() {
        if (!weeklyView) return;
        var rows = patternsContainer.querySelectorAll('.sbr-pattern-row');
        var map = {};
        rows.forEach(function (row) {
            var day = row.querySelector('.sbr-day').value;
            var courtId = parseInt(row.querySelector('.sbr-court').value || '0', 10);
            var startTime = row.querySelector('.sbr-start').value;
            var endTime = row.querySelector('.sbr-end').value;
            var courtName = courtNameById(courtId) || ('Sân #' + courtId);
            if (day) {
                if (startTime && endTime && courtId > 0) {
                    map[day] = courtName + ' · ' + startTime + ' – ' + endTime;
                } else {
                    map[day] = 'Chưa đủ dữ liệu';
                }
            }
        });

        var days = [
            { value: '2', label: 'Thứ 2' },
            { value: '3', label: 'Thứ 3' },
            { value: '4', label: 'Thứ 4' },
            { value: '5', label: 'Thứ 5' },
            { value: '6', label: 'Thứ 6' },
            { value: '7', label: 'Thứ 7' },
            { value: '1', label: 'Chủ nhật' }
        ];

        weeklyView.innerHTML = '';
        days.forEach(function (d) {
            var col = document.createElement('div');
            col.className = 'sbr-week-col';
            var title = document.createElement('div');
            title.className = 'sbr-week-title';
            title.textContent = d.label;
            col.appendChild(title);
            if (map[d.value]) {
                var chip = document.createElement('div');
                chip.className = 'sbr-week-chip';
                chip.textContent = map[d.value];
                col.appendChild(chip);
            } else {
                var empty = document.createElement('div');
                empty.className = 'sbr-week-empty';
                empty.textContent = '—';
                col.appendChild(empty);
            }
            weeklyView.appendChild(col);
        });
    }

    function collectSelectedSessions() {
        var conflicts = (previewData && previewData.conflicts) ? previewData.conflicts : [];
        var result = [];
        for (var i = 0; i < conflicts.length; i++) {
            var date = conflicts[i].date;
            var selected = document.querySelector('input[name="suggest_' + date + '"]:checked');
            if (!selected) {
                showError('Vui lòng chọn gợi ý hoặc chọn thủ công cho ngày ' + formatDate(date));
                return null;
            }

            if (selected.value === 'manual') {
                var wrap = selected.closest('.sbr-manual');
                var courtId = parseInt(wrap.querySelector('.sbr-manual-court').value, 10);
                var startTime = wrap.querySelector('.sbr-manual-start').value;
                var endTime = wrap.querySelector('.sbr-manual-end').value;
                var slotIds = toSlotIds(startTime, endTime);
                if (!slotIds) {
                    showError('Khung giờ thủ công không hợp lệ cho ngày ' + formatDate(date));
                    return null;
                }
                result.push({ date: date, courtId: courtId, slotIds: slotIds });
            } else {
                var line = selected.closest('label');
                var slotIdsStr = line.dataset.slotIds || '';
                var slotIds = slotIdsStr ? slotIdsStr.split(',').map(function (x) { return parseInt(x, 10); }) : [];
                result.push({
                    date: date,
                    courtId: parseInt(line.dataset.courtId, 10),
                    slotIds: slotIds
                });
            }
        }
        return result;
    }

    function findConflict(list, date) {
        for (var i = 0; i < list.length; i++) {
            if (list[i].date === date) return list[i];
        }
        return null;
    }

    function courtNameById(courtId) {
        for (var i = 0; i < courts.length; i++) {
            if (courts[i].courtId === courtId) return courts[i].courtName;
        }
        return null;
    }

    function handleGuestPhoneMatched(body) {
        if (body && body.code === 'GUEST_PHONE_MATCHED_ACCOUNT' && body.data && body.data.accountId) {
            var msg = 'Số điện thoại này đã tồn tại tài khoản khách hàng:\n' +
                '- ' + (body.data.fullName || 'Không rõ tên') + '\n' +
                '- ' + (body.data.phone || '') + '\n\n' +
                'Chuyển sang khách có tài khoản?';
            uiConfirm(msg, 'Trùng số điện thoại').then(function (ok) {
                if (!ok) return;
                customerType = 'ACCOUNT';
                tabAccount.click();
                selectedAccountId.value = body.data.accountId;
                selName.textContent = body.data.fullName || '';
                selPhone.textContent = body.data.phone || '';
                selEmail.textContent = body.data.email || '';
                selectedCustomer.classList.remove('d-none');
            });
        }
    }

    function uiConfirm(message, title) {
        if (window.StaffDialog && typeof window.StaffDialog.confirm === 'function') {
            return window.StaffDialog.confirm({ title: title || 'Xác nhận', message: message || '' });
        }
        return Promise.resolve(window.confirm(message || ''));
    }

    function uiAlert(message, title) {
        if (window.StaffDialog && typeof window.StaffDialog.alert === 'function') {
            return window.StaffDialog.alert({ title: title || 'Thông báo', message: message || '' });
        }
        window.alert(message || '');
        return Promise.resolve();
    }

    function showError(msg) {
        formError.textContent = msg;
        formError.classList.remove('d-none');
    }

    function hideError() {
        formError.classList.add('d-none');
    }

    function formatMoney(amount) {
        if (amount == null) return '0đ';
        return Number(amount).toLocaleString('vi-VN') + 'đ';
    }

    function formatDate(dateStr) {
        if (!dateStr) return '';
        var d = new Date(dateStr + 'T00:00:00');
        return String(d.getDate()).padStart(2, '0') + '/' +
            String(d.getMonth() + 1).padStart(2, '0') + '/' + d.getFullYear();
    }

    function escapeHtml(str) {
        if (!str) return '';
        return str.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/"/g, '&quot;');
    }

    function isMinFourWeeks(start, end) {
        var s = new Date(start + 'T00:00:00');
        var e = new Date(end + 'T00:00:00');
        var diffMs = e.getTime() - s.getTime();
        var diffDays = Math.floor(diffMs / (1000 * 60 * 60 * 24));
        return diffDays >= 28;
    }

    function isValidEmail(email) {
        var cleaned = (email || '').trim();
        if (!cleaned) return true;
        return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(cleaned);
    }
})();

