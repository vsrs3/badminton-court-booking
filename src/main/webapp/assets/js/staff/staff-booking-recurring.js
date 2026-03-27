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
    var progressEl = document.getElementById('sbrProgress');
    var summaryCustomer = document.getElementById('sbrSummaryCustomer');
    var summaryDate = document.getElementById('sbrSummaryDate');
    var summaryPatterns = document.getElementById('sbrSummaryPatterns');
    var summaryPolicy = document.getElementById('sbrSummaryPolicy');
    var summaryTotal = document.getElementById('sbrSummaryTotal');
    var summarySessions = document.getElementById('sbrSummarySessions');
    var customerTypeEl = document.getElementById('sbrCustomerType');
    var customerNameEl = document.getElementById('sbrCustomerName');
    var customerPhoneEl = document.getElementById('sbrCustomerPhone');
    var customerEmailEl = document.getElementById('sbrCustomerEmail');

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
    var stepPanels = document.querySelectorAll('.sbr-step-panel');
    var btnStep1Next = document.getElementById('btnStep1Next');
    var btnStep2Back = document.getElementById('btnStep2Back');
    var btnStep2Next = document.getElementById('btnStep2Next');
    var btnStep3Back = document.getElementById('btnStep3Back');
    var btnStep3Next = document.getElementById('btnStep3Next');
    var btnStep4Back = document.getElementById('btnStep4Back');

    var customerType = 'ACCOUNT';
    var searchTimer = null;
    var courts = [];
    var slots = [];
    var previewData = null;
    var selectionMap = {};
    var previewRefreshTimer = null;
    var isRenderingPreview = false;
    var currentStep = 1;

    init();

    function init() {
        setupTabs();
        setupCustomerSearch();
        btnAddPattern.addEventListener('click', addPatternRow);
        btnPreview.addEventListener('click', function () { onPreview(false); });
        btnConfirm.addEventListener('click', onConfirm);
        patternsContainer.addEventListener('change', onScheduleInputChanged);
        startDateEl.addEventListener('change', onScheduleInputChanged);
        endDateEl.addEventListener('change', onScheduleInputChanged);
        startDateEl.addEventListener('change', updateStepProgress);
        endDateEl.addEventListener('change', updateStepProgress);
        conflictPolicyEl.addEventListener('change', function () {
            markPreviewDirty();
            updateStepProgress();
        });
        paymentMethodEl.addEventListener('change', updateStepProgress);
        guestNameInput.addEventListener('input', updateStepProgress);
        guestPhoneInput.addEventListener('input', updateStepProgress);
        guestEmailInput.addEventListener('input', updateStepProgress);
        if (previewList) previewList.addEventListener('change', function () {
            updateStepProgress();
            onPreviewSelectionChanged();
        });

        btnStep1Next.addEventListener('click', function () {
            if (!isCustomerReady()) {
                showError('Vui lòng chọn khách hàng');
                return;
            }
            showStep(2);
        });
        btnStep2Back.addEventListener('click', function () { showStep(1); });
        btnStep2Next.addEventListener('click', function () {
            if (!isScheduleReady()) {
                showError('Vui lòng hoàn tất lịch định kỳ');
                return;
            }
            if (!validateScheduleForNext()) {
                return;
            }
            btnStep2Next.disabled = true;
            onPreview(false).then(function (ok) {
                if (!ok) return;
                showStep(3);
            }).finally(function () {
                btnStep2Next.disabled = false;
            });
        });
        btnStep3Back.addEventListener('click', function () { showStep(2); });
        btnStep3Next.addEventListener('click', function () {
            if (!previewData) {
                showError('Vui lòng xem trước trước khi tiếp tục');
                return;
            }
            if (hasUnresolvedConflicts()) {
                showError('Vui lòng xử lý trùng lịch và xem trước lại để cập nhật tổng tiền');
                return;
            }
        if (conflictPolicyEl.value === 'SUGGEST') {
            var selected = collectSelectedSessions();
            if (!selected) return;
        }
        showStep(4);
    });
        btnStep4Back.addEventListener('click', function () { showStep(3); });
        loadTimelineMeta();
        addPatternRow();
        showStep(1);
        updateStepProgress();
    }

    function setupTabs() {
        tabAccount.addEventListener('click', function () {
            customerType = 'ACCOUNT';
            tabAccount.classList.add('active');
            tabGuest.classList.remove('active');
            formAccount.classList.remove('d-none');
            formGuest.classList.add('d-none');
            updateStepProgress();
        });
        tabGuest.addEventListener('click', function () {
            customerType = 'GUEST';
            tabGuest.classList.add('active');
            tabAccount.classList.remove('active');
            formGuest.classList.remove('d-none');
            formAccount.classList.add('d-none');
            updateStepProgress();
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
            updateStepProgress();
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
        updateStepProgress();
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
        updateDayHints();
        renderWeeklyView();
        updateStepProgress();
    }

    function onScheduleInputChanged() {
        markPreviewDirty();
        updateDayHints();
        renderWeeklyView();
        updateStepProgress();
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
            '<button type="button" class="sbr-btn-icon sbr-remove"><i class="bi bi-trash"></i></button>' +
            '<small class="sbr-day-hint d-none">Hôm nay đã hết khung giờ hợp lệ</small>';
        row.querySelector('.sbr-remove').addEventListener('click', function () {
            row.remove();
            markPreviewDirty();
            renderWeeklyView();
            updateStepProgress();
        });
        patternsContainer.appendChild(row);
        fillCourtOptions(row.querySelector('.sbr-court'));
        fillTimeOptions(row.querySelector('.sbr-start'), row.querySelector('.sbr-end'));
        updateDayHints();
        markPreviewDirty();
        renderWeeklyView();
        updateStepProgress();
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

    function onPreview(includeSelections) {
        hideError();
        var req = buildRequestBody();
        if (!req) return Promise.resolve(false);

        if (includeSelections) {
            updateSelectionMapFromUI();
            var selected = buildSelectedSessionsFromMap();
            if (selected == null) return Promise.resolve(false);
            if (selected.length) {
                req.selectedSessions = selected;
            }
        }

        if (btnPreview) {
            btnPreview.disabled = true;
            btnPreview.textContent = 'Đang xem trước...';
        }

        return fetch(CTX + '/api/staff/recurring-booking/preview', {
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
                    return false;
                }
                previewData = body.data;
                renderPreview(body.data);
                btnConfirm.disabled = false;
                updateStepProgress();
                return true;
            })
            .catch(function (err) {
                showError('Lỗi kết nối: ' + err.message);
                return false;
            })
            .finally(function () {
                if (btnPreview) {
                    btnPreview.disabled = false;
                    btnPreview.innerHTML = '<i class="bi bi-eye"></i>Xem trước';
                }
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
            var selectedFromMap = buildSelectedSessionsFromMap();
            if (selectedFromMap == null) return;
            req.selectedSessions = selectedFromMap;
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
        if (isEndDateBeforeStart(startDate, endDate)) {
            showError('Ngày kết thúc phải sau hoặc bằng ngày bắt đầu');
            return null;
        }
        if (!isMinFourWeeks(startDate, endDate)) {
            showError('Thời gian định kỳ phải tối thiểu 4 tuần');
            return null;
        }
        if (isStartDateInPast(startDate)) {
            showError('Không thể đặt cho ngày trong quá khứ');
            return null;
        }

        var patterns = collectPatterns();
        if (!patterns) return null;
        if (hasPastSessionToday(patterns)) {
            showError(buildPastSessionMessage());
            return null;
        }

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
        isRenderingPreview = true;
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

                applySelectionForConflict(s.date, suggestWrap);
            }

            previewList.appendChild(item);
        });
        isRenderingPreview = false;
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
        updateStepProgress();
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

    function buildSelectedSessionsFromMap() {
        var result = [];
        var dates = Object.keys(selectionMap || {});
        for (var i = 0; i < dates.length; i++) {
            var date = dates[i];
            var item = selectionMap[date];
            if (!item || !item.courtId) continue;
            if (item.mode === 'manual') {
                if (!item.slotIds || item.slotIds.length < 2) {
                    showError('Khung giờ thủ công không hợp lệ cho ngày ' + formatDate(date));
                    return null;
                }
            }
            result.push({
                date: date,
                courtId: item.courtId,
                slotIds: item.slotIds || []
            });
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

    function updateStepProgress() {
        if (!progressEl) return;
        var step1Done = isCustomerReady();
        var step2Done = isScheduleReady();
        var step3Done = !!previewData && !previewSection.classList.contains('d-none');
        var step4Done = step3Done && !!(paymentMethodEl && paymentMethodEl.value);

        var activeStep = currentStep || 1;

        setStepState(1, activeStep, step1Done);
        setStepState(2, activeStep, step2Done);
        setStepState(3, activeStep, step3Done);
        setStepState(4, activeStep, step4Done);
        updateSummary();
        updateStepButtons(step1Done, step2Done, step3Done);
    }

    function setStepState(step, activeStep, isDone) {
        var steps = progressEl.querySelectorAll('.sbr-step[data-step="' + step + '"]');
        var cards = document.querySelectorAll('.sbr-step-card[data-step="' + step + '"]');
        var isActive = activeStep === step;

        steps.forEach(function (el) {
            el.classList.toggle('is-active', isActive);
            el.classList.toggle('is-done', isDone && !isActive);
        });

        cards.forEach(function (el) {
            el.classList.toggle('is-active', isActive);
            el.classList.toggle('is-done', isDone && !isActive);
        });
    }

    function isCustomerReady() {
        if (customerType === 'ACCOUNT') {
            return parseInt(selectedAccountId.value || '0', 10) > 0;
        }
        return !!(guestNameInput.value.trim() && guestPhoneInput.value.trim());
    }

    function isScheduleReady() {
        if (!startDateEl.value || !endDateEl.value) return false;
        var rows = patternsContainer.querySelectorAll('.sbr-pattern-row');
        if (!rows.length) return false;
        for (var i = 0; i < rows.length; i++) {
            var day = rows[i].querySelector('.sbr-day').value;
            var court = rows[i].querySelector('.sbr-court').value;
            var start = rows[i].querySelector('.sbr-start').value;
            var end = rows[i].querySelector('.sbr-end').value;
            if (!day || !court || !start || !end) return false;
        }
        return true;
    }

    function showStep(step) {
        currentStep = step;
        stepPanels.forEach(function (panel) {
            panel.classList.toggle('is-active', parseInt(panel.getAttribute('data-step'), 10) === step);
        });
        updateStepProgress();
        window.scrollTo({ top: 0, behavior: 'smooth' });
    }

    function updateStepButtons(step1Done, step2Done, step3Done) {
        if (btnStep1Next) btnStep1Next.disabled = !step1Done;
        if (btnStep2Next) btnStep2Next.disabled = !step2Done;
        if (btnStep3Next) btnStep3Next.disabled = !isPreviewReady(step3Done);
    }

    function isPreviewReady(step3Done) {
        if (!step3Done) return false;
        if (hasUnresolvedConflicts()) return false;
        if (conflictPolicyEl.value !== 'SUGGEST') return true;
        var conflicts = (previewData && previewData.conflicts) ? previewData.conflicts : [];
        for (var i = 0; i < conflicts.length; i++) {
            var date = conflicts[i].date;
            var selected = document.querySelector('input[name="suggest_' + date + '"]:checked');
            if (!selected) return false;
            if (selected.value === 'manual') {
                var wrap = selected.closest('.sbr-manual');
                if (!wrap) return false;
                var startTime = wrap.querySelector('.sbr-manual-start').value;
                var endTime = wrap.querySelector('.sbr-manual-end').value;
                if (!toSlotIds(startTime, endTime)) return false;
            }
        }
        return true;
    }

    function updateSummary() {
        if (!summaryCustomer) return;
        summaryCustomer.textContent = getCustomerSummary();
        summaryDate.textContent = formatDateRange(startDateEl.value, endDateEl.value);
        summaryPatterns.textContent = String(patternsContainer.querySelectorAll('.sbr-pattern-row').length || 0);
        summaryPolicy.textContent = getPolicyLabel(conflictPolicyEl.value);
        summaryTotal.textContent = previewData ? formatMoney(previewData.totalAmount || 0) : '—';

        updateCustomerPanel();
        renderSummarySessions();
    }

    function renderSummarySessions() {
        if (!summarySessions) return;
        summarySessions.innerHTML = '';
        if (!previewData || !(previewData.sessions || []).length) {
            summarySessions.innerHTML = '<div class="sbr-summary-empty">Chưa có dữ liệu phiên chơi.</div>';
            return;
        }

        var policy = conflictPolicyEl.value || 'SKIP';
        var hasAny = false;

        (previewData.sessions || []).forEach(function (session) {
            var status = session.status;
            if (status === 'SKIPPED') {
                appendSummaryItem(buildSummaryItem({
                    date: session.date,
                    courtId: session.courtId,
                    slotIds: session.slotIds || [],
                    amount: session.amount,
                    badge: { text: 'Bỏ qua', cls: 'sbr-badge-skipped' }
                }));
                hasAny = true;
                return;
            }

            if (status === 'CONFLICT') {
                if (policy === 'SUGGEST') {
                    var chosen = selectionMap && selectionMap[session.date];
                    if (chosen) {
                        appendSummaryItem(buildSummaryItem({
                            date: session.date,
                            courtId: chosen.courtId || session.courtId,
                            slotIds: chosen.slotIds || [],
                            amount: session.amount,
                            badge: { text: 'Đã chọn', cls: 'sbr-badge-ok' }
                        }));
                        hasAny = true;
                    } else {
                        appendSummaryItem(buildSummaryItem({
                            date: session.date,
                            courtId: session.courtId,
                            slotIds: session.slotIds || [],
                            amount: session.amount,
                            badge: { text: 'Chưa chọn', cls: 'sbr-badge-conflict' }
                        }));
                        hasAny = true;
                    }
                } else {
                    appendSummaryItem(buildSummaryItem({
                        date: session.date,
                        courtId: session.courtId,
                        slotIds: session.slotIds || [],
                        amount: session.amount,
                        badge: { text: 'Trùng lịch', cls: 'sbr-badge-conflict' }
                    }));
                    hasAny = true;
                }
                return;
            }

            appendSummaryItem(buildSummaryItem({
                date: session.date,
                courtId: session.courtId,
                slotIds: session.slotIds || [],
                amount: session.amount,
                badge: { text: 'OK', cls: 'sbr-badge-ok' }
            }));
            hasAny = true;
        });

        if (!hasAny) {
            summarySessions.innerHTML = '<div class="sbr-summary-empty">Chưa có dữ liệu phiên chơi.</div>';
        }
    }

    function appendSummaryItem(node) {
        if (!summarySessions) return;
        summarySessions.appendChild(node);
    }

    function buildSummaryItem(data) {
        var dateLabel = formatDate(data.date);
        var courtLabel = courtNameById(data.courtId) || ('Sân #' + data.courtId);
        var timeRange = getTimeRangeFromSlotIds(data.slotIds || []) || {};
        var timeText = (timeRange.startTime && timeRange.endTime) ? (timeRange.startTime + ' → ' + timeRange.endTime) : '—';
        var amountText = formatMoney(data.amount || 0);

        var item = document.createElement('div');
        item.className = 'sbr-summary-item-line';
        item.innerHTML =
            '<div class="sbr-summary-main">' +
            '<div class="sbr-summary-title">' + dateLabel + ' · ' + courtLabel + '</div>' +
            '<div class="sbr-summary-sub">' + timeText + '</div>' +
            '</div>' +
            '<div class="sbr-summary-meta">' +
            '<span class="sbr-badge ' + (data.badge ? data.badge.cls : '') + '">' + (data.badge ? data.badge.text : '') + '</span>' +
            '<span class="sbr-summary-amount">' + amountText + '</span>' +
            '</div>';
        return item;
    }

    function getCustomerSummary() {
        if (customerType === 'ACCOUNT') {
            if (parseInt(selectedAccountId.value || '0', 10) > 0) {
                return selName.textContent || 'Khách hàng';
            }
            return 'Chưa chọn';
        }
        var name = guestNameInput.value.trim();
        var phone = guestPhoneInput.value.trim();
        if (!name && !phone) return 'Chưa nhập';
        if (name && phone) return name + ' · ' + phone;
        return name || phone;
    }

    function formatDateRange(start, end) {
        if (!start || !end) return 'Chưa chọn';
        return formatDate(start) + ' → ' + formatDate(end);
    }

    function getPolicyLabel(value) {
        if (value === 'SUGGEST') return 'Gợi ý & chọn thủ công';
        if (value === 'SKIP') return 'Bỏ qua ngày trùng';
        return '—';
    }

    function updateCustomerPanel() {
        if (!customerTypeEl) return;
        customerTypeEl.textContent = customerType === 'ACCOUNT' ? 'Khách có tài khoản' : 'Khách vãng lai';
        var data = getCustomerFields();
        customerNameEl.textContent = data.name || '—';
        customerPhoneEl.textContent = data.phone || '—';
        customerEmailEl.textContent = data.email || '—';
    }

    function getCustomerFields() {
        if (customerType === 'ACCOUNT') {
            if (parseInt(selectedAccountId.value || '0', 10) > 0) {
                return {
                    name: selName.textContent || '',
                    phone: selPhone.textContent || '',
                    email: selEmail.textContent || ''
                };
            }
            return { name: '', phone: '', email: '' };
        }
        return {
            name: guestNameInput.value.trim(),
            phone: guestPhoneInput.value.trim(),
            email: guestEmailInput.value.trim()
        };
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

    function isEndDateBeforeStart(start, end) {
        var s = new Date(start + 'T00:00:00');
        var e = new Date(end + 'T00:00:00');
        return e.getTime() < s.getTime();
    }

    function isValidEmail(email) {
        var cleaned = (email || '').trim();
        if (!cleaned) return true;
        return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(cleaned);
    }

    function isDateRangeIncludesToday() {
        if (!startDateEl.value || !endDateEl.value) return false;
        var today = new Date();
        var start = new Date(startDateEl.value + 'T00:00:00');
        var end = new Date(endDateEl.value + 'T23:59:59');
        return today >= start && today <= end;
    }

    function todayPatternDay() {
        var d = new Date().getDay();
        return d === 0 ? 1 : d + 1;
    }

    function updateDayHints() {
        var rows = patternsContainer.querySelectorAll('.sbr-pattern-row');
        var showHint = shouldShowTodayHint();
        rows.forEach(function (row) {
            var hint = row.querySelector('.sbr-day-hint');
            if (!hint) return;
            var dayValue = parseInt(row.querySelector('.sbr-day').value || '0', 10);
            if (showHint && dayValue === todayPatternDay()) {
                hint.classList.remove('d-none');
            } else {
                hint.classList.add('d-none');
            }
        });
    }

    function shouldShowTodayHint() {
        if (!isDateRangeIncludesToday()) return false;
        if (!slots.length) return false;
        var nowMinutes = timeToMinutes(new Date().toTimeString().slice(0, 5));
        for (var i = 0; i < slots.length; i++) {
            if (timeToMinutes(slots[i].endTime) > nowMinutes) {
                return false;
            }
        }
        return true;
    }

    function markPreviewDirty() {
        if (!previewData) return;
        previewData = null;
        selectionMap = {};
        previewSection.classList.add('d-none');
        btnConfirm.disabled = true;
        updateStepProgress();
    }

    function onPreviewSelectionChanged() {
        if (isRenderingPreview) return;
        if (!previewData) return;
        if (conflictPolicyEl.value !== 'SUGGEST') return;
        updateSelectionMapFromUI();
        schedulePreviewRefresh();
    }

    function schedulePreviewRefresh() {
        clearTimeout(previewRefreshTimer);
        if (btnStep3Next) btnStep3Next.disabled = true;
        previewRefreshTimer = setTimeout(function () {
            onPreview(true);
        }, 300);
    }

    function updateSelectionMapFromUI() {
        var conflicts = (previewData && previewData.conflicts) ? previewData.conflicts : [];
        for (var i = 0; i < conflicts.length; i++) {
            var date = conflicts[i].date;
            var selected = document.querySelector('input[name="suggest_' + date + '"]:checked');
            if (!selected) continue;

            if (selected.value === 'manual') {
                var wrap = selected.closest('.sbr-manual');
                var courtId = parseInt(wrap.querySelector('.sbr-manual-court').value, 10);
                var startTime = wrap.querySelector('.sbr-manual-start').value;
                var endTime = wrap.querySelector('.sbr-manual-end').value;
                var slotIds = toSlotIds(startTime, endTime);
                selectionMap[date] = {
                    mode: 'manual',
                    courtId: courtId,
                    slotIds: slotIds || [],
                    startTime: startTime,
                    endTime: endTime
                };
            } else {
                var line = selected.closest('label');
                var slotIdsStr = line.dataset.slotIds || '';
                var slotIds = slotIdsStr ? slotIdsStr.split(',').map(function (x) { return parseInt(x, 10); }) : [];
                selectionMap[date] = {
                    mode: 'suggestion',
                    courtId: parseInt(line.dataset.courtId, 10),
                    slotIds: slotIds
                };
            }
        }
    }

    function applySelectionForConflict(date, suggestWrap) {
        var saved = selectionMap[date];
        if (!saved) return;

        if (saved.mode === 'suggestion') {
            var options = suggestWrap.querySelectorAll('label.sbr-suggest-item');
            for (var i = 0; i < options.length; i++) {
                var courtId = parseInt(options[i].dataset.courtId || '0', 10);
                var slotIds = (options[i].dataset.slotIds || '').split(',').filter(Boolean).map(function (x) { return parseInt(x, 10); });
                if (courtId === saved.courtId && arraysEqual(slotIds, saved.slotIds || [])) {
                    var input = options[i].querySelector('input[type="radio"]');
                    if (input) input.checked = true;
                    return;
                }
            }
        }

        if (saved.mode === 'manual') {
            var manualRadio = suggestWrap.querySelector('input[value="manual"]');
            if (manualRadio) manualRadio.checked = true;
            var manualWrap = suggestWrap.querySelector('.sbr-manual');
            if (!manualWrap) return;
            var startSelect = manualWrap.querySelector('.sbr-manual-start');
            var endSelect = manualWrap.querySelector('.sbr-manual-end');
            var courtSelect = manualWrap.querySelector('.sbr-manual-court');
            if (saved.courtId && courtSelect) courtSelect.value = String(saved.courtId);
            if (saved.startTime && startSelect) startSelect.value = saved.startTime;
            if (saved.endTime && endSelect) endSelect.value = saved.endTime;
            if ((!saved.startTime || !saved.endTime) && saved.slotIds && saved.slotIds.length) {
                var range = getTimeRangeFromSlotIds(saved.slotIds);
                if (range && startSelect && endSelect) {
                    startSelect.value = range.startTime;
                    endSelect.value = range.endTime;
                }
            }
        }
    }

    function arraysEqual(a, b) {
        if (!a || !b) return false;
        if (a.length !== b.length) return false;
        for (var i = 0; i < a.length; i++) {
            if (a[i] !== b[i]) return false;
        }
        return true;
    }

    function getTimeRangeFromSlotIds(slotIds) {
        if (!slotIds || !slotIds.length) return null;
        var startTime = null;
        var endTime = null;
        for (var i = 0; i < slotIds.length; i++) {
            var slot = findSlotById(slotIds[i]);
            if (!slot) continue;
            if (!startTime || timeToMinutes(slot.startTime) < timeToMinutes(startTime)) {
                startTime = slot.startTime;
            }
            if (!endTime || timeToMinutes(slot.endTime) > timeToMinutes(endTime)) {
                endTime = slot.endTime;
            }
        }
        if (!startTime || !endTime) return null;
        return { startTime: startTime, endTime: endTime };
    }

    function getTimeRangeFromSlotIds(slotIds) {
        if (!slotIds || !slotIds.length) return null;
        var startTime = null;
        var endTime = null;
        for (var i = 0; i < slotIds.length; i++) {
            var slot = findSlotById(slotIds[i]);
            if (!slot) continue;
            if (!startTime || timeToMinutes(slot.startTime) < timeToMinutes(startTime)) {
                startTime = slot.startTime;
            }
            if (!endTime || timeToMinutes(slot.endTime) > timeToMinutes(endTime)) {
                endTime = slot.endTime;
            }
        }
        if (!startTime || !endTime) return null;
        return { startTime: startTime, endTime: endTime };
    }

    function findSlotById(slotId) {
        for (var i = 0; i < slots.length; i++) {
            if (slots[i].slotId === slotId) return slots[i];
        }
        return null;
    }

    function validateScheduleForNext() {
        hideError();
        var req = buildRequestBody();
        if (!req) return false;
        return true;
    }

    function isStartDateInPast(startDate) {
        var today = new Date();
        var start = new Date(startDate + 'T00:00:00');
        today.setHours(0, 0, 0, 0);
        return start < today;
    }

    function hasPastSessionToday(patterns) {
        if (!patterns || !patterns.length) return false;
        if (!isDateRangeIncludesToday()) return false;
        var todayDay = todayPatternDay();
        var nowMinutes = timeToMinutes(new Date().toTimeString().slice(0, 5));
        for (var i = 0; i < patterns.length; i++) {
            if (patterns[i].dayOfWeek !== todayDay) continue;
            var startTime = getSessionStartTimeFromSlotIds(patterns[i].slotIds);
            if (startTime && timeToMinutes(startTime) <= nowMinutes - 30) {
                return true;
            }
            var endTime = getSessionEndTimeFromSlotIds(patterns[i].slotIds);
            if (endTime && timeToMinutes(endTime) <= nowMinutes) {
                return true;
            }
        }
        return false;
    }

    function getSessionStartTimeFromSlotIds(slotIds) {
        var startTime = null;
        for (var i = 0; i < slotIds.length; i++) {
            var slot = findSlotById(slotIds[i]);
            if (!slot) continue;
            if (!startTime || timeToMinutes(slot.startTime) < timeToMinutes(startTime)) {
                startTime = slot.startTime;
            }
        }
        return startTime;
    }

    function getSessionEndTimeFromSlotIds(slotIds) {
        var endTime = null;
        for (var i = 0; i < slotIds.length; i++) {
            var slot = findSlotById(slotIds[i]);
            if (!slot) continue;
            if (!endTime || timeToMinutes(slot.endTime) > timeToMinutes(endTime)) {
                endTime = slot.endTime;
            }
        }
        return endTime;
    }

    function buildPastSessionMessage() {
        var nowStr = new Date(Date.now() - 30 * 60 * 1000).toTimeString().slice(0, 5);
        return 'Bạn đang đặt lịch cho hôm nay. Vui lòng chọn các khung giờ từ ' + nowStr + ' trở đi.';
    }

    function hasUnresolvedConflicts() {
        if (!previewData || !previewData.sessions) return false;
        for (var i = 0; i < previewData.sessions.length; i++) {
            if (previewData.sessions[i].status === 'CONFLICT') {
                return true;
            }
        }
        return false;
    }

    function timeToMinutes(timeStr) {
        if (!timeStr) return 0;
        var parts = timeStr.split(':');
        var h = parseInt(parts[0] || '0', 10);
        var m = parseInt(parts[1] || '0', 10);
        return h * 60 + m;
    }
})();



