/**
 * booking.js — Single Booking Matrix Page Controller
 *
 * Chịu trách nhiệm:
 *  1. Load matrix data từ GET /api/single-booking/matrix-data
 *  2. Render bảng Sân × Giờ (sticky header + sticky col)
 *  3. Toggle slot selection, tính tiền/thời lượng dự kiến bằng JS
 *  4. Gọi POST /api/single-booking/preview khi nhấn "Tiếp tục"
 *  5. Chuyển sang preview.jsp (dữ liệu qua sessionStorage)
 *  6. Mini calendar để đổi ngày, không cho chọn quá khứ
 *
 * Author: AnhTN
 */
(function () {
    'use strict';

    /* ── Context path ────────────────────────────────────────── */
    const CTX = (function () {
        const parts = window.location.pathname.split('/');
        return parts.length > 1 && parts[1] ? '/' + parts[1] : '';
    })();

    /* ── Read URL params ─────────────────────────────────────── */
    const params  = new URLSearchParams(window.location.search);
    const venueId = params.get('venueId') || params.get('facilityId');

    function todayStr() {
        const d = new Date();
        return d.getFullYear() + '-'
            + String(d.getMonth() + 1).padStart(2, '0') + '-'
            + String(d.getDate()).padStart(2, '0');
    }

    let selectedDateStr = params.get('date') || todayStr();

    /* ── App state ───────────────────────────────────────────── */
    let matrixData   = null;   // { courts, timeSlots, slotMap }
    let selectedSlots = [];    // [{courtId, slotIndex, startTime, endTime, price}]
    let isSubmitting  = false;

    /* ── DOM refs ────────────────────────────────────────────── */
    const loadingOverlay = document.getElementById('sbLoadingOverlay');
    const alertBox       = document.getElementById('sbAlertBox');
    const alertMsg       = document.getElementById('sbAlertMsg');
    const tableHead      = document.getElementById('sbTableHead');
    const tableBody      = document.getElementById('sbTableBody');
    const summaryBar     = document.getElementById('sbSummaryBar');
    const statSlots      = document.getElementById('sbStatSlots');
    const statHours      = document.getElementById('sbStatHours');
    const statPrice      = document.getElementById('sbStatPrice');
    const continueBtn    = document.getElementById('sbContinueBtn');
    const dateBtn        = document.getElementById('sbDateBtn');
    const dateBtnText    = document.getElementById('sbDateBtnText');
    const calPopup       = document.getElementById('sbCalPopup');
    const calPrev        = document.getElementById('sbCalPrev');
    const calNext        = document.getElementById('sbCalNext');
    const calMonthYear   = document.getElementById('sbCalMonthYear');
    const calDaysEl      = document.getElementById('sbCalDays');
    const facilityNameEl = document.getElementById('sbFacilityName');

    /* ── Calendar state ──────────────────────────────────────── */
    let viewDate = new Date(selectedDateStr + 'T00:00:00');

    /* ══════════════════════════════════════════════════════════
       LOADING & ALERT HELPERS
       ══════════════════════════════════════════════════════════ */

    function showLoading()  { if (loadingOverlay) loadingOverlay.classList.add('active'); }
    function hideLoading()  { if (loadingOverlay) loadingOverlay.classList.remove('active'); }

    function showAlert(msg, type) {
        if (!alertBox || !alertMsg) return;
        alertBox.className = 'sb-alert sb-alert-' + (type || 'error');
        alertMsg.textContent = msg;
        alertBox.style.display = 'flex';
        alertBox.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
    }
    function hideAlert() {
        if (alertBox) alertBox.style.display = 'none';
    }

    /* ── Alert close button ── */
    const alertClose = document.getElementById('sbAlertClose');
    if (alertClose) alertClose.addEventListener('click', hideAlert);

    /* ══════════════════════════════════════════════════════════
       CALENDAR
       ══════════════════════════════════════════════════════════ */

    function formatDateDisplay(dateStr) {
        const d = new Date(dateStr + 'T00:00:00');
        return d.toLocaleDateString('vi-VN', { weekday: 'short', day: '2-digit', month: '2-digit', year: 'numeric' });
    }

    function renderCalendar() {
        if (!calMonthYear || !calDaysEl) return;
        const y = viewDate.getFullYear(), m = viewDate.getMonth();
        calMonthYear.textContent = 'Tháng ' + (m + 1) + ' / ' + y;

        const firstDow   = new Date(y, m, 1).getDay();       // 0=Sun
        const daysInMonth = new Date(y, m + 1, 0).getDate();
        const today       = new Date(); today.setHours(0, 0, 0, 0);

        calDaysEl.innerHTML = '';

        // blank cells
        for (let i = 0; i < firstDow; i++) {
            const blank = document.createElement('div');
            blank.className = 'sb-cal-day empty';
            calDaysEl.appendChild(blank);
        }

        for (let d = 1; d <= daysInMonth; d++) {
            const cell = document.createElement('div');
            cell.textContent = d;
            cell.className = 'sb-cal-day';

            const cellDate = new Date(y, m, d);
            const cellStr  = y + '-' + String(m + 1).padStart(2, '0') + '-' + String(d).padStart(2, '0');

            if (cellDate < today) {
                cell.classList.add('disabled');
            } else {
                if (cellStr === todayStr()) cell.classList.add('today');
                if (cellStr === selectedDateStr) cell.classList.add('selected');

                cell.addEventListener('click', function () {
                    selectedDateStr = cellStr;
                    if (dateBtnText) dateBtnText.textContent = formatDateDisplay(selectedDateStr);
                    calPopup.classList.remove('open');
                    selectedSlots = [];
                    updateSummaryBar();
                    loadMatrixData();
                    // Update URL without reload
                    const url = new URL(window.location.href);
                    url.searchParams.set('date', selectedDateStr);
                    window.history.replaceState({}, '', url.toString());
                });
            }
            calDaysEl.appendChild(cell);
        }
    }

    if (dateBtn) {
        dateBtn.addEventListener('click', function (e) {
            e.stopPropagation();
            renderCalendar();
            calPopup.classList.toggle('open');
        });
    }
    document.addEventListener('click', function (e) {
        if (calPopup && !calPopup.contains(e.target) && !dateBtn.contains(e.target)) {
            calPopup.classList.remove('open');
        }
    });
    if (calPrev) calPrev.addEventListener('click', function () {
        viewDate.setMonth(viewDate.getMonth() - 1);
        renderCalendar();
    });
    if (calNext) calNext.addEventListener('click', function () {
        viewDate.setMonth(viewDate.getMonth() + 1);
        renderCalendar();
    });

    /* ══════════════════════════════════════════════════════════
       LOAD MATRIX DATA
       ══════════════════════════════════════════════════════════ */

    function loadMatrixData() {
        if (!venueId) {
            showAlert('Thiếu thông tin cơ sở (venueId). Vui lòng quay lại trang chủ.', 'error');
            return;
        }

        showLoading();
        hideAlert();

        const url = CTX + '/api/single-booking/matrix-data'
            + '?venueId=' + encodeURIComponent(venueId)
            + '&date='    + encodeURIComponent(selectedDateStr);

        fetch(url, {
            method: 'GET',
            headers: { 'Accept': 'application/json' }
        })
        .then(function (res) {
            if (res.status === 401) throw { code: 'UNAUTHORIZED', status: 401 };
            if (!res.ok) return res.json().then(function (j) { throw j; });
            return res.json();
        })
        .then(function (json) {
            hideLoading();
            // Backend trả về { success: true, data: {...} } HOẶC trực tiếp
            const data = (json.success !== undefined && json.data) ? json.data : json;
            matrixData = data;
            renderMatrix();
            if (facilityNameEl && data.facility) {
                facilityNameEl.textContent = data.facility.name || '';
            }
        })
        .catch(function (err) {
            hideLoading();
            if (err && err.status === 401) {
                handleUnauthorized();
            } else {
                const msg = (err && err.error && err.error.message)
                    ? err.error.message
                    : 'Không thể tải dữ liệu sân. Vui lòng thử lại.';
                showAlert(msg, 'error');
                renderEmptyMatrix();
            }
        });
    }

    /* ══════════════════════════════════════════════════════════
       RENDER MATRIX TABLE
       Hàng = sân (court), Cột = khung giờ (timeSlot)
       ══════════════════════════════════════════════════════════ */

    function renderMatrix() {
        if (!matrixData || !tableHead || !tableBody) return;

        const { courts, timeSlots, slotMap } = matrixData;

        /* ── Build thead ── */
        let headHtml = '<tr>';
        headHtml += '<th class="sticky-col court-header">Sân / Giờ</th>';
        timeSlots.forEach(function (t) {
            headHtml += '<th>' + escHtml(t) + '</th>';
        });
        headHtml += '</tr>';
        tableHead.innerHTML = headHtml;

        /* ── Build tbody ── */
        let bodyHtml = '';
        courts.forEach(function (court) {
            bodyHtml += '<tr>';
            // Sticky court name column
            bodyHtml += '<td class="sticky-col court-name-cell">'
                + '<div>' + escHtml(court.courtName) + '</div>'
                + '<div class="court-type-cell">' + escHtml(court.courtType || '') + '</div>'
                + '</td>';

            timeSlots.forEach(function (t, idx) {
                const key  = court.courtId + '_' + idx;
                const slot = (slotMap && slotMap[key]) ? slotMap[key] : null;
                const status  = slot ? slot.status : 'AVAILABLE';
                const price   = slot ? (slot.price || 0) : 0;

                const isSelected = selectedSlots.some(function (s) {
                    return s.courtId === court.courtId && s.slotIndex === idx;
                });

                let cssClass = 'slot-cell ';
                if (status === 'BOOKED')        cssClass += 'slot-booked';
                else if (status === 'PAST')      cssClass += 'slot-past';
                else if (isSelected)             cssClass += 'slot-selected';
                else                             cssClass += 'slot-available';

                const priceLabel = status !== 'BOOKED' && status !== 'PAST'
                    ? formatVnd(price)
                    : (status === 'BOOKED' ? 'Đã đặt' : 'Quá giờ');

                const endTime = computeEndTime(t);

                bodyHtml += '<td class="' + cssClass + '"'
                    + ' data-court="'  + court.courtId    + '"'
                    + ' data-index="'  + idx               + '"'
                    + ' data-start="'  + escHtml(t)        + '"'
                    + ' data-end="'    + escHtml(endTime)  + '"'
                    + ' data-price="'  + price             + '"'
                    + ' data-status="' + status            + '"'
                    + ' data-price-label="' + escHtml(priceLabel) + '"'
                    + ' tabindex="' + (status === 'AVAILABLE' ? '0' : '-1') + '"'
                    + '></td>';
            });

            bodyHtml += '</tr>';
        });
        tableBody.innerHTML = bodyHtml;

        /* ── Attach click listeners ── */
        tableBody.querySelectorAll('.slot-cell').forEach(function (cell) {
            cell.addEventListener('click', onSlotClick);
            cell.addEventListener('keydown', function (e) {
                if (e.key === 'Enter' || e.key === ' ') { e.preventDefault(); onSlotClick.call(cell, e); }
            });
        });
    }

    function renderEmptyMatrix() {
        if (tableHead) tableHead.innerHTML = '<tr><th>Sân / Giờ</th></tr>';
        if (tableBody) tableBody.innerHTML = '<tr><td class="text-center p-4 text-muted" colspan="100">Không có dữ liệu.</td></tr>';
    }

    /* Tính endTime từ startTime (+ 30 phút) */
    function computeEndTime(start) {
        if (!start) return '';
        const [h, m] = start.split(':').map(Number);
        const total  = h * 60 + m + 30;
        return String(Math.floor(total / 60) % 24).padStart(2, '0') + ':' + String(total % 60).padStart(2, '0');
    }

    /* ══════════════════════════════════════════════════════════
       SLOT CLICK / TOGGLE
       ══════════════════════════════════════════════════════════ */

    function onSlotClick() {
        const status = this.dataset.status;
        if (status === 'BOOKED' || status === 'PAST') return;

        const courtId  = parseInt(this.dataset.court, 10);
        const slotIndex = parseInt(this.dataset.index, 10);
        const startTime = this.dataset.start;
        const endTime   = this.dataset.end;
        const price     = parseFloat(this.dataset.price) || 0;

        const existIdx = selectedSlots.findIndex(function (s) {
            return s.courtId === courtId && s.slotIndex === slotIndex;
        });

        if (existIdx > -1) {
            selectedSlots.splice(existIdx, 1);
            this.classList.remove('slot-selected');
            this.classList.add('slot-available');
        } else {
            selectedSlots.push({ courtId, slotIndex, startTime, endTime, price });
            this.classList.remove('slot-available');
            this.classList.add('slot-selected');
        }

        updateSummaryBar();
        hideAlert();
    }

    /* ══════════════════════════════════════════════════════════
       SUMMARY BAR  (tính bằng JS từ data-price trên từng cell)
       ══════════════════════════════════════════════════════════ */

    function updateSummaryBar() {
        if (!summaryBar) return;

        const count     = selectedSlots.length;
        const totalMin  = count * 30;
        const totalPrice = selectedSlots.reduce(function (acc, s) { return acc + s.price; }, 0);

        if (count === 0) {
            summaryBar.classList.remove('visible');
            return;
        }

        summaryBar.classList.add('visible');

        if (statSlots) statSlots.textContent = count + ' slot';
        if (statHours) statHours.textContent = formatDuration(totalMin);
        if (statPrice) statPrice.textContent  = formatVnd(totalPrice);
    }

    /* ══════════════════════════════════════════════════════════
       "TIẾP TỤC" → POST /api/single-booking/preview
       ══════════════════════════════════════════════════════════ */

    if (continueBtn) {
        continueBtn.addEventListener('click', function () {
            if (isSubmitting) return;
            if (selectedSlots.length === 0) {
                showAlert('Vui lòng chọn ít nhất một khung giờ.', 'warning');
                return;
            }
            doPreview();
        });
    }

    function doPreview() {
        isSubmitting = true;
        setContinueBtnLoading(true);
        hideAlert();

        const body = {
            venueId:    parseInt(venueId, 10),
            date:       selectedDateStr,
            selections: selectedSlots.map(function (s) {
                return {
                    courtId:   s.courtId,
                    slotIndex: s.slotIndex,
                    startTime: s.startTime,
                    endTime:   s.endTime
                };
            })
        };

        fetch(CTX + '/api/single-booking/preview', {
            method:  'POST',
            headers: { 'Content-Type': 'application/json', 'Accept': 'application/json' },
            body:    JSON.stringify(body)
        })
        .then(function (res) {
            if (res.status === 401) throw { code: 'UNAUTHORIZED', status: 401 };
            return res.json().then(function (j) {
                if (!res.ok) throw j;
                return j;
            });
        })
        .then(function (json) {
            // Hỗ trợ cả { success, data } lẫn flat response
            const data = (json.success !== undefined && json.data) ? json.data : json;

            if (data.valid === false) {
                // Backend validation failed
                showAlert(data.errorMessage || 'Dữ liệu không hợp lệ.', 'error');
                setContinueBtnLoading(false);
                isSubmitting = false;
                return;
            }

            // Lưu preview data + selections vào sessionStorage rồi redirect
            const previewPayload = {
                previewData: data,
                selections:  body.selections,
                venueId:     body.venueId,
                date:        body.date
            };
            sessionStorage.setItem('sbPreviewData', JSON.stringify(previewPayload));
            window.location.href = CTX + '/jsp/booking/singlebooking/preview.jsp';
        })
        .catch(function (err) {
            setContinueBtnLoading(false);
            isSubmitting = false;

            if (err && err.status === 401) {
                handleUnauthorized();
                return;
            }
            const msg = (err && err.error && err.error.message)
                ? err.error.message
                : (err && err.message) || 'Đã xảy ra lỗi. Vui lòng thử lại.';
            showAlert(msg, 'error');
        });
    }

    function setContinueBtnLoading(loading) {
        if (!continueBtn) return;
        continueBtn.disabled = loading;
        continueBtn.innerHTML = loading
            ? '<span class="spinner-border spinner-border-sm me-2" role="status"></span> Đang xử lý...'
            : 'TIẾP TỤC <i class="bi bi-chevron-right"></i>';
    }

    /* ══════════════════════════════════════════════════════════
       AUTH CHECK
       ══════════════════════════════════════════════════════════ */

    function handleUnauthorized() {
        const authBackdrop = document.getElementById('authModalBackdrop');
        const authModal    = document.getElementById('authModal');
        if (authBackdrop && authModal) {
            authBackdrop.classList.add('active');
            authModal.classList.add('active');
            document.body.style.overflow = 'hidden';
        } else {
            window.location.href = CTX + '/auth/login?redirect=' + encodeURIComponent(window.location.href);
        }
    }

    /* ══════════════════════════════════════════════════════════
       UTILS
       ══════════════════════════════════════════════════════════ */

    function escHtml(str) {
        if (str == null) return '';
        return String(str)
            .replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;')
            .replace(/"/g, '&quot;');
    }

    function formatVnd(amount) {
        return Number(amount).toLocaleString('vi-VN') + ' ₫';
    }

    function formatDuration(minutes) {
        const h = Math.floor(minutes / 60);
        const m = minutes % 60;
        if (h === 0) return m + ' phút';
        if (m === 0) return h + ' giờ';
        return h + ' giờ ' + m + ' phút';
    }

    /* ══════════════════════════════════════════════════════════
       INIT
       ══════════════════════════════════════════════════════════ */

    function init() {
        // Set date display
        if (dateBtnText) dateBtnText.textContent = formatDateDisplay(selectedDateStr);
        viewDate = new Date(selectedDateStr + 'T00:00:00');
        loadMatrixData();
    }

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', init);
    } else {
        init();
    }

})();
