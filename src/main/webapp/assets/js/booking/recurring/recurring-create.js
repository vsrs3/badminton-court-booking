/**
 * Recurring create page controller.
 * Builds weekly patterns and calls preview API.
 * Author: AnhTN
 */
(function () {
    'use strict';

    const CTX = window.APP_CONTEXT_PATH || '';
    const qs = new URLSearchParams(window.location.search);

    const facilityIdInput = document.getElementById('facilityId');
    const startDateInput = document.getElementById('startDate');
    const endDateInput = document.getElementById('endDate');
    const patternContainer = document.getElementById('patternContainer');
    const addPatternBtn = document.getElementById('addPatternBtn');
    const previewBtn = document.getElementById('previewBtn');
    const alertBox = document.getElementById('rcAlert');
    const facilityNameText = document.getElementById('facilityNameText');

    const prefillCourtId = qs.get('courtId');

    let courts = [];
    let timeOptions = [];
    let openTime = '06:00';
    let closeTime = '22:00';
    let facilityName = '';
    let pickerSeq = 0;

    function getSavedCreatePayload() {
        try {
            const raw = sessionStorage.getItem('recurringCreatePayload');
            if (!raw) return null;
            const payload = JSON.parse(raw);
            if (!payload || typeof payload !== 'object') return null;

            const currentFacilityId = parseInt(facilityIdInput.value, 10);
            const payloadFacilityId = parseInt(payload.facilityId, 10);
            if (!Number.isFinite(payloadFacilityId) || payloadFacilityId !== currentFacilityId) {
                return null;
            }
            return payload;
        } catch (e) {
            return null;
        }
    }

    function renderFacilityName() {
        if (!facilityNameText) return;
        const facilityId = parseInt(facilityIdInput.value, 10);
        const safe = facilityName && facilityName.trim() ? facilityName : ('Cơ sở #' + facilityId);
        facilityNameText.textContent = 'Cơ sở: ' + safe;
    }

    /** Shows error alert on create screen. */
    function showError(message) {
        alertBox.textContent = message;
        alertBox.classList.remove('d-none');
    }

    /** Hides error alert on create screen. */
    function clearError() {
        alertBox.classList.add('d-none');
        alertBox.textContent = '';
    }

    /** Returns YYYY-MM-DD for date input defaults. */
    function formatDateInput(date) {
        return date.toISOString().slice(0, 10);
    }

    /** Initializes start/end date defaults. */
    function initDates() {
        const today = new Date();
        const end = new Date();
        end.setDate(end.getDate() + 28);
        startDateInput.value = formatDateInput(today);
        endDateInput.value = formatDateInput(end);
    }

    /** Loads active courts by facility for pattern selects. */
    async function loadCourts() {
        const facilityId = parseInt(facilityIdInput.value, 10);
        const res = await fetch(CTX + '/api/recurring/courts?facilityId=' + encodeURIComponent(facilityId));
        const json = await res.json();
        if (!res.ok || !json.success) {
            throw new Error((json && json.error && json.error.message) || 'Không thể tải danh sách sân.');
        }

        if (Array.isArray(json.data)) {
            courts = json.data;
            timeOptions = buildHalfHourOptions(openTime, closeTime);
            facilityName = '';
        } else {
            courts = (json.data && json.data.courts) || [];
            openTime = (json.data && json.data.openTime) || openTime;
            closeTime = (json.data && json.data.closeTime) || closeTime;
            timeOptions = (json.data && json.data.timeOptions) || buildHalfHourOptions(openTime, closeTime);
            facilityName = (json.data && json.data.facilityName) || '';
        }

        renderFacilityName();

        if (!courts.length) {
            throw new Error('Cơ sở này chưa có sân hoạt động.');
        }
        if (!timeOptions.length) {
            throw new Error('Không tìm thấy khung giờ hợp lệ cho cơ sở này.');
        }
    }

    /** Builds fallback half-hour options from open/close times. */
    function buildHalfHourOptions(start, end) {
        const toMinutes = function (v) {
            const p = String(v || '').split(':');
            return (parseInt(p[0], 10) * 60) + parseInt(p[1] || '0', 10);
        };
        const toTime = function (m) {
            const h = String(Math.floor(m / 60)).padStart(2, '0');
            const mm = String(m % 60).padStart(2, '0');
            return h + ':' + mm;
        };
        const from = toMinutes(start);
        const to = toMinutes(end);
        if (Number.isNaN(from) || Number.isNaN(to) || from >= to) {
            return [];
        }
        const list = [];
        for (let m = from; m <= to; m += 30) {
            list.push(toTime(m));
        }
        return list;
    }

    /** Creates day-of-week options HTML for pattern row. */
    function dayOptions(selected) {
        const dayMap = [
            { value: 1, label: 'Chủ nhật' },
            { value: 2, label: 'Thứ hai' },
            { value: 3, label: 'Thứ ba' },
            { value: 4, label: 'Thứ tư' },
            { value: 5, label: 'Thứ năm' },
            { value: 6, label: 'Thứ sáu' },
            { value: 7, label: 'Thứ bảy' }
        ];
        return dayMap.map(function (d) {
            return '<option value="' + d.value + '"' + (selected === d.value ? ' selected' : '') + '>' + d.label + '</option>';
        }).join('');
    }

    /** Creates court options HTML for pattern row. */
    function courtOptions(selectedCourtId) {
        return courts.map(function (c) {
            const selected = String(c.courtId) === String(selectedCourtId) ? ' selected' : '';
            return '<option value="' + c.courtId + '"' + selected + '>' + c.courtName + '</option>';
        }).join('');
    }

    function buildTimePickerOptions() {
        return {
            required: true,
            placeholder: 'Chọn giờ',
            allowedTimes: timeOptions
        };
    }

    /** Adds one weekly pattern row into the card layout. */
    function addPatternRow(defaults) {
        const cfg = defaults || {};
        const row = document.createElement('div');
        row.className = 'pattern-row';

        const rowId = String(++pickerSeq);
        const startDisplayId = 'rcStartDisplay' + rowId;
        const startInputId = 'rcStartInput' + rowId;
        const endDisplayId = 'rcEndDisplay' + rowId;
        const endInputId = 'rcEndInput' + rowId;

        const defaultStart = cfg.startTime || timeOptions[0];
        const defaultEnd = cfg.endTime || timeOptions[Math.min(2, timeOptions.length - 1)] || timeOptions[timeOptions.length - 1];

        row.innerHTML = ''
            + '<div class="flex-grow-1">'
            + '  <select class="form-select dayOfWeek" style="height:52px">' + dayOptions(cfg.dayOfWeek || 2) + '</select>'
            + '</div>'
            + '<div class="flex-grow-1">'
            + '  <select class="form-select courtId" style="height:52px">' + courtOptions(cfg.courtId || prefillCourtId || courts[0].courtId) + '</select>'
            + '</div>'
            + '<div style="width:160px">'
            + '  <div class="time-picker-wrapper">'
            + '    <div id="' + startDisplayId + '" class="time-picker-display" tabindex="0"></div>'
            + '    <input type="hidden" class="startTime" id="' + startInputId + '" value="' + defaultStart + '" />'
            + '  </div>'
            + '</div>'
            + '<div style="width:160px">'
            + '  <div class="time-picker-wrapper">'
            + '    <div id="' + endDisplayId + '" class="time-picker-display" tabindex="0"></div>'
            + '    <input type="hidden" class="endTime" id="' + endInputId + '" value="' + defaultEnd + '" />'
            + '  </div>'
            + '</div>'
            + '<button type="button" class="btn btn-outline-danger btn-sm px-3 removePatternBtn">'
            + '  <i class="bi bi-trash3"></i>'
            + '</button>';
        patternContainer.appendChild(row);

        const pickerOptions = buildTimePickerOptions();
        initializeTimePicker(startDisplayId, startInputId, pickerOptions);
        initializeTimePicker(endDisplayId, endInputId, pickerOptions);
    }

    /** Collects all pattern rows into API payload structure. */
    function collectPatterns() {
        return Array.from(patternContainer.querySelectorAll('.pattern-row')).map(function (row) {
            return {
                dayOfWeek: parseInt(row.querySelector('.dayOfWeek').value, 10),
                courtId: parseInt(row.querySelector('.courtId').value, 10),
                startTime: row.querySelector('.startTime').value,
                endTime: row.querySelector('.endTime').value
            };
        });
    }

    /** Validates create form before calling preview API. */
    function validatePayload(payload) {
        if (!payload.startDate || !payload.endDate) {
            throw new Error('Vui lòng chọn ngày bắt đầu và kết thúc.');
        }

        const start = new Date(payload.startDate + 'T00:00:00');
        const end = new Date(payload.endDate + 'T00:00:00');
        if (Number.isNaN(start.getTime()) || Number.isNaN(end.getTime())) {
            throw new Error('Định dạng ngày không hợp lệ.');
        }

        const diffDays = Math.floor((end.getTime() - start.getTime()) / (1000 * 60 * 60 * 24));
        if (diffDays < 0) {
            throw new Error('Ngày kết thúc phải lớn hơn hoặc bằng ngày bắt đầu.');
        }
        if (diffDays < 28) {
            throw new Error('Khoảng ngày đặt lịch phải ít nhất 28 ngày mới có thể xem preview.');
        }

        if (!payload.patterns.length) {
            throw new Error('Vui lòng thêm ít nhất 1 pattern.');
        }

        const usedDays = new Set();
        payload.patterns.forEach(function (p) {
            if (!p.startTime || !p.endTime) {
                throw new Error('Vui lòng chọn đầy đủ giờ bắt đầu và kết thúc cho tất cả pattern.');
            }
            if (timeOptions.indexOf(p.startTime) === -1 || timeOptions.indexOf(p.endTime) === -1) {
                throw new Error('Giờ đã chọn phải nằm trong khung giờ mở cửa - đóng cửa của cơ sở.');
            }
            if (p.startTime >= p.endTime) {
                throw new Error('Giờ kết thúc phải lớn hơn giờ bắt đầu.');
            }
            if (usedDays.has(p.dayOfWeek)) {
                throw new Error('Mỗi thứ trong tuần chỉ được cấu hình 1 khung giờ.');
            }
            usedDays.add(p.dayOfWeek);
        });
    }

    /** Calls recurring preview API and stores response for preview screen. */
    async function previewRecurring() {
        clearError();

        const payload = {
            facilityId: parseInt(facilityIdInput.value, 10),
            startDate: startDateInput.value,
            endDate: endDateInput.value,
            patterns: collectPatterns()
        };

        try {
            validatePayload(payload);
            previewBtn.disabled = true;

            const res = await fetch(CTX + '/api/recurring/preview', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(payload)
            });
            const json = await res.json();
            if (!res.ok || !json.success) {
                const msg = (json && json.error && json.error.message) || 'Preview không thành công.';
                throw new Error(msg);
            }

            sessionStorage.setItem('recurringCreatePayload', JSON.stringify(payload));
            sessionStorage.setItem('recurringPreviewData', JSON.stringify(json.data));
            window.location.href = CTX + '/jsp/booking/recurring/preview.jsp';
        } catch (err) {
            showError(err.message || 'Không thể tạo preview recurring.');
        } finally {
            previewBtn.disabled = false;
        }
    }

    /** Binds click listeners for dynamic create page actions. */
    function bindEvents() {
        addPatternBtn.addEventListener('click', function () {
            addPatternRow();
        });

        patternContainer.addEventListener('click', function (e) {
            const removeBtn = e.target.closest('.removePatternBtn');
            if (!removeBtn) return;
            const row = removeBtn.closest('.pattern-row');
            if (row) row.remove();
        });

        previewBtn.addEventListener('click', previewRecurring);
    }

    /** Bootstraps recurring create page. */
    async function init() {
        try {
            await loadCourts();

            const savedPayload = getSavedCreatePayload();
            if (savedPayload && savedPayload.startDate && savedPayload.endDate) {
                startDateInput.value = savedPayload.startDate;
                endDateInput.value = savedPayload.endDate;

                patternContainer.innerHTML = '';
                const patterns = Array.isArray(savedPayload.patterns) ? savedPayload.patterns : [];
                if (patterns.length) {
                    patterns.forEach(function (p) {
                        addPatternRow({
                            dayOfWeek: p.dayOfWeek,
                            courtId: p.courtId,
                            startTime: p.startTime,
                            endTime: p.endTime
                        });
                    });
                } else {
                    addPatternRow();
                }
            } else {
                initDates();
                addPatternRow();
            }

            bindEvents();
        } catch (e) {
            showError(e.message || 'Không thể khởi tạo trang recurring create.');
        }
    }

    init();
})();


