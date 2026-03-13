/**
 * Recurring preview page controller.
 * Supports skip dates, modified sessions, voucher apply, and confirm-and-pay.
 * Author: AnhTN
 */
(function () {
    'use strict';

    const CTX = window.APP_CONTEXT_PATH || '';

    const alertBox = document.getElementById('rpAlert');
    const infoBox = document.getElementById('rpInfo');
    const statTotal = document.getElementById('statTotal');
    const statAvailable = document.getElementById('statAvailable');
    const statConflict = document.getElementById('statConflict');
    const statTotalAmount = document.getElementById('statTotalAmount');
    const sessionBody = document.getElementById('sessionBody');

    const voucherCodeInput = document.getElementById('voucherCode');
    const applyVoucherBtn = document.getElementById('applyVoucherBtn');

    const moneyTotal = document.getElementById('moneyTotal');
    const moneyDiscount = document.getElementById('moneyDiscount');
    const moneyFinal = document.getElementById('moneyFinal');

    const confirmBtn = document.getElementById('confirmBtn');
    const backToCreate = document.getElementById('backToCreate');

    const mdSessionId = document.getElementById('mdSessionId');
    const mdSessionDateText = document.getElementById('mdSessionDateText');
    const mdCourtId = document.getElementById('mdCourtId');
    const mdStartTime = document.getElementById('mdStartTime');
    const mdEndTime = document.getElementById('mdEndTime');
    const saveModifyBtn = document.getElementById('saveModifyBtn');

    const modifyModal = new bootstrap.Modal(document.getElementById('modifySessionModal'));

    const previewData = JSON.parse(sessionStorage.getItem('recurringPreviewData') || 'null');
    const createPayload = JSON.parse(sessionStorage.getItem('recurringCreatePayload') || 'null');

    let courts = [];
    let allowedTimes = [];
    let discountAmount = 0;
    const skippedDates = new Set();
    const modifiedBySessionId = {};
    let mdStartPicker = null;
    let mdEndPicker = null;
    const defaultMinRequiredSessions = 4;

    /** Shows error alert in preview screen. */
    function showError(msg) {
        alertBox.textContent = msg;
        alertBox.classList.remove('d-none');
    }

    /** Shows informational alert in preview screen. */
    function showInfo(msg) {
        infoBox.textContent = msg;
        infoBox.classList.remove('d-none');
    }

    /** Clears all alert boxes. */
    function clearAlerts() {
        alertBox.classList.add('d-none');
        infoBox.classList.add('d-none');
        alertBox.textContent = '';
        infoBox.textContent = '';
    }

    /** Formats number to VND display string. */
    function formatMoney(value) {
        const n = Number(value || 0);
        return n.toLocaleString('vi-VN') + ' VND';
    }

    function getMinRequiredSessions() {
        const n = Number(previewData && previewData.minRequiredSessions);
        return Number.isFinite(n) && n > 0 ? n : defaultMinRequiredSessions;
    }

    /** Resolves day-of-week label from numeric value. */
    function dayLabel(day) {
        const map = {
            1: 'Chủ nhật',
            2: 'Thứ hai',
            3: 'Thứ ba',
            4: 'Thứ tư',
            5: 'Thứ năm',
            6: 'Thứ sáu',
            7: 'Thứ bảy'
        };
        return map[day] || '-';
    }

    /** Formats yyyy-mm-dd to dd/mm/yyyy with weekday label. */
    function formatSessionDate(dateIso, dayOfWeek) {
        const parts = String(dateIso || '').split('-');
        if (parts.length !== 3) {
            return dateIso || '-';
        }
        return dayLabel(dayOfWeek) + ', ' + parts[2] + '/' + parts[1] + '/' + parts[0];
    }

    /** Returns estimated payable amount based on current skip/modify choices. */
    function getCurrentEstimatedTotal() {
        let total = 0;
        (previewData.sessions || []).forEach(function (s) {
            if (skippedDates.has(s.date)) return;
            if (s.status === 'AVAILABLE' || modifiedBySessionId[s.sessionId]) {
                total += getSessionDisplayPrice(s);
            }
        });
        return total;
    }

    function getSessionDisplayPrice(session) {
        const override = modifiedBySessionId[session.sessionId];
        if (override && Number.isFinite(Number(override.newPrice))) {
            return Number(override.newPrice);
        }
        return Number(session.price || 0);
    }

    /** Re-computes and renders money summary area. */
    function refreshMoneySummary() {
        const total = getCurrentEstimatedTotal();
        const finalAmount = Math.max(total - Number(discountAmount || 0), 0);
        moneyTotal.textContent = formatMoney(total);
        moneyDiscount.textContent = formatMoney(discountAmount);
        moneyFinal.textContent = formatMoney(finalAmount);
    }

    /** Renders summary cards from preview data. */
    function renderSummary() {
        statTotal.textContent = String(previewData.totalSessions || 0);
        statAvailable.textContent = String(previewData.availableSessions || 0);
        statConflict.textContent = String(previewData.conflictSessions || 0);
        statTotalAmount.textContent = formatMoney(previewData.totalAmount || 0);
    }

    /** Creates status badge HTML for each session row. */
    function renderStatusBadge(session) {
        if (modifiedBySessionId[session.sessionId]) {
            return '<span class="recurring-badge modified">Đã chỉnh</span>';
        }
        if (session.status === 'CONFLICT') {
            return '<span class="recurring-badge conflict">Xung đột</span>';
        }
        return '<span class="recurring-badge available">Khả dụng</span>';
    }

    /** Creates action buttons HTML for each session row. */
    function renderActionCell(session) {
        const dateChecked = skippedDates.has(session.date) ? 'checked' : '';
        let html = ''
            + '<div class="d-flex flex-column gap-1">'
            + '  <label class="form-check m-0">'
            + '    <input class="form-check-input skip-date-checkbox" type="checkbox" data-date="' + session.date + '" ' + dateChecked + '>'
            + '    <span class="form-check-label small">Bỏ ngày</span>'
            + '  </label>';

        if (session.status === 'CONFLICT' || modifiedBySessionId[session.sessionId]) {
            html += renderConflictDropdown(session);
        }
        html += '</div>';
        return html;
    }

    function renderConflictDropdown(session) {
        const suggestions = Array.isArray(session.suggestions) ? session.suggestions : [];
        const items = suggestions.map(function (sg, idx) {
            const priceText = formatMoney(sg.price || 0);
            return ''
                + '<button type="button" class="dropdown-item apply-suggestion-btn" '
                + 'data-session-id="' + session.sessionId + '" '
                + 'data-court-id="' + sg.courtId + '" '
                + 'data-court-name="' + (sg.courtName || '') + '" '
                + 'data-start="' + sg.startTime + '" '
                + 'data-end="' + sg.endTime + '" '
                + 'data-price="' + Number(sg.price || 0) + '">'
                + '  <div class="small fw-semibold">#' + (idx + 1) + ' - ' + (sg.courtName || ('Sân #' + sg.courtId)) + '</div>'
                + '  <div class="small text-muted">' + sg.startTime + ' - ' + sg.endTime + '</div>'
                + '  <div class="small text-success fw-semibold">' + priceText + '</div>'
                + '</button>';
        }).join('');

        return ''
            + '<div class="dropdown">'
            + '  <button class="btn btn-outline-warning btn-sm dropdown-toggle btn-lift" type="button" data-bs-toggle="dropdown" aria-expanded="false">Xử lý</button>'
            + '  <div class="dropdown-menu p-2 recurring-suggestion-menu">'
            + (items || '<div class="dropdown-item-text small text-muted">Không có gợi ý khả dụng.</div>')
            + '    <hr class="dropdown-divider my-1">'
            + '    <button type="button" class="dropdown-item edit-session-btn" data-session-id="' + session.sessionId + '">Tự chỉnh sân/giờ...</button>'
            + '  </div>'
            + '</div>';
    }

    /** Renders session table with conflict/edit states. */
    function renderSessions() {
        const rows = (previewData.sessions || []).map(function (s) {
            const skipped = skippedDates.has(s.date) ? 'row-skipped' : '';
            const override = modifiedBySessionId[s.sessionId];
            const courtName = override ? override.newCourtName : s.courtName;
            const startTime = override ? override.newStartTime : s.startTime;
            const endTime = override ? override.newEndTime : s.endTime;
            const price = getSessionDisplayPrice(s);
            return ''
                + '<tr class="' + skipped + '">'
                + '  <td>' + formatSessionDate(s.date, s.dayOfWeek) + '</td>'
                + '  <td>' + courtName + '</td>'
                + '  <td>' + startTime + ' - ' + endTime + '</td>'
                + '  <td class="fw-semibold text-nowrap">' + formatMoney(price) + '</td>'
                + '  <td>' + renderStatusBadge(s) + '</td>'
                + '  <td>' + renderActionCell(s) + '</td>'
                + '</tr>';
        }).join('');

        sessionBody.innerHTML = rows;
        refreshMoneySummary();
    }

    /** Loads courts/setup data for edit modal by facility id. */
    async function loadSetupData() {
        if (!createPayload || !createPayload.facilityId) return;
        const res = await fetch(CTX + '/api/recurring/courts?facilityId=' + encodeURIComponent(createPayload.facilityId));
        const json = await res.json();
        if (!res.ok || !json.success) {
            throw new Error((json && json.error && json.error.message) || 'Không thể tải danh sách sân.');
        }

        if (Array.isArray(json.data)) {
            courts = json.data;
            allowedTimes = buildFallbackTimes();
        } else {
            courts = (json.data && json.data.courts) || [];
            allowedTimes = (json.data && json.data.timeOptions) || buildFallbackTimes();
        }
        initModalTimePickers();
    }

    function buildFallbackTimes() {
        const set = new Set();
        (previewData.sessions || []).forEach(function (s) {
            if (s.startTime) set.add(s.startTime);
            if (s.endTime) set.add(s.endTime);
        });
        return Array.from(set).sort();
    }

    function initModalTimePickers() {
        const opts = {
            required: true,
            placeholder: 'Chọn giờ',
            allowedTimes: allowedTimes
        };
        mdStartPicker = initializeTimePicker('mdStartTimeDisplay', 'mdStartTime', opts);
        mdEndPicker = initializeTimePicker('mdEndTimeDisplay', 'mdEndTime', opts);
    }

    /** Finds original session row by sessionId. */
    function findSession(sessionId) {
        return (previewData.sessions || []).find(function (s) {
            return String(s.sessionId) === String(sessionId);
        });
    }

    /** Opens modal and prefills modification form for one session. */
    function openModifyModal(sessionId) {
        const session = findSession(sessionId);
        if (!session) {
            showError('Không tìm thấy session để sửa.');
            return;
        }

        const applied = modifiedBySessionId[sessionId];
        mdSessionId.value = sessionId;
        mdSessionDateText.textContent = 'Ngày: ' + session.date + ' (' + dayLabel(session.dayOfWeek) + ')';

        mdCourtId.innerHTML = courts.map(function (c) {
            const selectedCourtId = applied ? applied.newCourtId : session.courtId;
            return '<option value="' + c.courtId + '"' + (String(c.courtId) === String(selectedCourtId) ? ' selected' : '') + '>' + c.courtName + '</option>';
        }).join('');

        const start = applied ? applied.newStartTime : session.startTime;
        const end = applied ? applied.newEndTime : session.endTime;
        if (mdStartPicker) {
            mdStartPicker.setValue(start);
        } else {
            mdStartTime.value = start;
        }
        if (mdEndPicker) {
            mdEndPicker.setValue(end);
        } else {
            mdEndTime.value = end;
        }

        modifyModal.show();
    }

    /** Saves one modification from modal back into local state. */
    function saveModification() {
        const sessionId = mdSessionId.value;
        const session = findSession(sessionId);
        if (!session) return;

        if (!mdCourtId.value || !mdStartTime.value || !mdEndTime.value) {
            showError('Vui lòng chọn đầy đủ sân và giờ để sửa session.');
            return;
        }
        if (allowedTimes.length
            && (allowedTimes.indexOf(mdStartTime.value) === -1 || allowedTimes.indexOf(mdEndTime.value) === -1)) {
            showError('Giờ sửa phải nằm trong khung giờ mở cửa - đóng cửa của cơ sở.');
            return;
        }
        if (mdStartTime.value >= mdEndTime.value) {
            showError('Giờ kết thúc phải lớn hơn giờ bắt đầu.');
            return;
        }

        const court = courts.find(function (c) { return String(c.courtId) === String(mdCourtId.value); });
        modifiedBySessionId[sessionId] = {
            sessionId: sessionId,
            newCourtId: parseInt(mdCourtId.value, 10),
            newStartTime: mdStartTime.value,
            newEndTime: mdEndTime.value,
            newCourtName: court ? court.courtName : ('Sân #' + mdCourtId.value),
            newPrice: null
        };
        skippedDates.delete(session.date);

        clearAlerts();
        showInfo('Đã cập nhật session xung đột. Hệ thống sẽ xác thực xung đột lại khi xác nhận.');
        renderSessions();
        modifyModal.hide();
    }

    function applySuggestion(target) {
        const sessionId = target.dataset.sessionId;
        const session = findSession(sessionId);
        if (!session) return;

        modifiedBySessionId[sessionId] = {
            sessionId: sessionId,
            newCourtId: parseInt(target.dataset.courtId, 10),
            newStartTime: target.dataset.start,
            newEndTime: target.dataset.end,
            newCourtName: target.dataset.courtName || ('Sân #' + target.dataset.courtId),
            newPrice: Number(target.dataset.price || 0)
        };
        skippedDates.delete(session.date);
        clearAlerts();
        showInfo('Đã áp dụng gợi ý cho session ' + session.date + '.');
        renderSessions();
    }

    function countSkippedNonConflictSessions() {
        let count = 0;
        (previewData.sessions || []).forEach(function (s) {
            if (!skippedDates.has(s.date)) return;
            if (s.status !== 'CONFLICT') count++;
        });
        return count;
    }

    function getCurrentRemainingSessions() {
        let count = 0;
        (previewData.sessions || []).forEach(function (s) {
            if (!skippedDates.has(s.date)) count++;
        });
        return count;
    }

    /** Applies recurring voucher based on current estimated amount. */
    async function applyVoucher() {
        clearAlerts();
        const code = (voucherCodeInput.value || '').trim();
        if (!code) {
            showError('Vui lòng nhập voucher trước khi áp dụng.');
            return;
        }

        try {
            applyVoucherBtn.disabled = true;
            const payload = {
                voucherCode: code,
                facilityId: createPayload ? createPayload.facilityId : null,
                totalAmount: getCurrentEstimatedTotal()
            };

            const res = await fetch(CTX + '/api/recurring/apply-voucher', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(payload)
            });
            const json = await res.json();
            if (!res.ok || !json.success) {
                const msg = (json && json.error && json.error.message) || 'Không thể áp dụng voucher.';
                throw new Error(msg);
            }

            discountAmount = Number(json.data.discountAmount || 0);
            refreshMoneySummary();
            showInfo('Áp dụng voucher thành công: giảm ' + formatMoney(discountAmount));
        } catch (e) {
            discountAmount = 0;
            refreshMoneySummary();
            showError(e.message || 'Áp dụng voucher thất bại.');
        } finally {
            applyVoucherBtn.disabled = false;
        }
    }

    /** Builds confirm payload from current UI state. */
    function buildConfirmPayload() {
        return {
            previewToken: previewData.previewToken,
            skipDates: Array.from(skippedDates),
            voucherCode: (voucherCodeInput.value || '').trim() || null,
            modifiedSessions: Object.values(modifiedBySessionId).map(function (m) {
                return {
                    sessionId: m.sessionId,
                    newCourtId: m.newCourtId,
                    newStartTime: m.newStartTime,
                    newEndTime: m.newEndTime
                };
            })
        };
    }

    /** Calls confirm-and-pay API for recurring booking. */
    async function confirmRecurring() {
        clearAlerts();
        try {
            confirmBtn.disabled = true;

            const minRequired = getMinRequiredSessions();
            const remaining = getCurrentRemainingSessions();
            const skippedNonConflict = countSkippedNonConflictSessions();
            const skipOnlyConflict = skippedDates.size > 0 && skippedNonConflict === 0;
            if (remaining < minRequired && !skipOnlyConflict) {
                throw new Error('Cần ít nhất ' + minRequired + ' session để đặt lịch cố định. Hiện chỉ còn ' + remaining + ' session.');
            }

            const payload = buildConfirmPayload();
            const res = await fetch(CTX + '/api/recurring/confirm-and-pay', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(payload)
            });
            const json = await res.json();
            if (!res.ok || !json.success) {
                if (json && json.error && json.error.code === 'MIN_SESSIONS_REQUIRED') {
                    throw new Error(json.error.message || 'Không đủ số session tối thiểu để đặt lịch cố định.');
                }
                const msg = (json && json.error && json.error.message) || 'Xác nhận recurring thất bại.';
                throw new Error(msg);
            }

            const data = json.data || {};
            if (data.paymentUrl) {
                window.location.href = data.paymentUrl;
                return;
            }

            showInfo('Đặt lịch thành công. Booking #' + (data.bookingId || '-') + '.');
            sessionStorage.removeItem('recurringPreviewData');
            sessionStorage.removeItem('recurringCreatePayload');
        } catch (e) {
            showError(e.message || 'Không thể xác nhận recurring booking.');
        } finally {
            confirmBtn.disabled = false;
        }
    }

    /** Binds all interactive events in preview page. */
    function bindEvents() {
        applyVoucherBtn.addEventListener('click', applyVoucher);
        confirmBtn.addEventListener('click', confirmRecurring);
        saveModifyBtn.addEventListener('click', saveModification);

        sessionBody.addEventListener('change', function (e) {
            const target = e.target;
            if (!target.classList.contains('skip-date-checkbox')) return;
            const date = target.dataset.date;
            if (target.checked) skippedDates.add(date);
            else skippedDates.delete(date);
            renderSessions();
        });

        sessionBody.addEventListener('click', function (e) {
            const suggestionBtn = e.target.closest('.apply-suggestion-btn');
            if (suggestionBtn) {
                applySuggestion(suggestionBtn);
                return;
            }

            const editBtn = e.target.closest('.edit-session-btn');
            if (!editBtn) return;
            openModifyModal(editBtn.dataset.sessionId);
        });

        if (createPayload && createPayload.facilityId) {
            backToCreate.href = CTX + '/jsp/booking/recurring/create.jsp?facilityId=' + encodeURIComponent(createPayload.facilityId);
        }
    }

    /** Initializes preview page and validates required sessionStorage data. */
    async function init() {
        if (!previewData || !previewData.previewToken) {
            showError('Không có dữ liệu preview. Vui lòng tạo lại recurring booking.');
            confirmBtn.disabled = true;
            applyVoucherBtn.disabled = true;
            return;
        }

        try {
            await loadSetupData();
            renderSummary();
            renderSessions();
            bindEvents();
        } catch (e) {
            showError(e.message || 'Không thể khởi tạo trang preview recurring.');
        }
    }

    init();
})();


