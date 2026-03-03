/**
 * preview.js — Single Booking Preview & Confirm Page Controller
 *
 * Chịu trách nhiệm:
 *  1. Đọc previewData từ sessionStorage (do booking.js lưu)
 *  2. Render thông tin preview (venue, courts, ranges, totals)
 *  3. Xử lý chọn phương thức thanh toán (FULL | DEPOSIT 30%)
 *  4. Xử lý nhập voucher
 *  5. POST /api/single-booking/confirm-and-pay
 *  6. Xử lý mọi trường hợp lỗi: 400, 401, 409, 500, double-submit, network
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

    /* ── Read sessionStorage ──────────────────────────────────── */
    let storedRaw = null;
    try { storedRaw = sessionStorage.getItem('sbPreviewData'); } catch (e) { /* ignore */ }

    if (!storedRaw) {
        // Không có dữ liệu → redirect về trang chủ
        window.location.replace(CTX + '/');
        return;
    }

    let payload = null;
    try { payload = JSON.parse(storedRaw); } catch (e) {
        window.location.replace(CTX + '/');
        return;
    }

    const previewData  = payload.previewData;  // response từ /preview
    const selections   = payload.selections;   // [{courtId,slotIndex,startTime,endTime}]
    const venueId      = payload.venueId;
    const bookingDate  = payload.date;

    /* ── Payment state ───────────────────────────────────────── */
    let paymentMethod   = 'VNPAY';   // default
    let depositPercent  = 100;       // default full pay
    let isSubmitting    = false;

    /* ── DOM refs ────────────────────────────────────────────── */
    const pvVenueName    = document.getElementById('pvVenueName');
    const pvVenueAddr    = document.getElementById('pvVenueAddr');
    const pvBookingDate  = document.getElementById('pvBookingDate');
    const pvDuration     = document.getElementById('pvDuration');
    const pvCourtsWrap   = document.getElementById('pvCourtsWrap');
    const pvSubtotal     = document.getElementById('pvSubtotal');
    const pvDiscount     = document.getElementById('pvDiscount');
    const pvDiscountRow  = document.getElementById('pvDiscountRow');
    const pvDepositRow   = document.getElementById('pvDepositRow');
    const pvDepositAmt   = document.getElementById('pvDepositAmt');
    const pvTotal        = document.getElementById('pvTotal');
    const pvAlertBox     = document.getElementById('pvAlertBox');
    const pvAlertMsg     = document.getElementById('pvAlertMsg');
    const pvAlertClose   = document.getElementById('pvAlertClose');
    const pvConfirmBtn   = document.getElementById('pvConfirmBtn');
    const pvConfirmBarAmt= document.getElementById('pvConfirmBarAmt');
    // const pvNoteInput    = document.getElementById('pvNoteInput');
    // const pvVoucherInput = document.getElementById('pvVoucherInput');
    const pvLoadingOverlay = document.getElementById('pvLoadingOverlay');

    /* ── Alert helpers ────────────────────────────────────────── */

    function showAlert(msg, type) {
        if (!pvAlertBox || !pvAlertMsg) return;
        pvAlertBox.className = 'sb-alert sb-alert-' + (type || 'error');
        pvAlertMsg.textContent = msg;
        pvAlertBox.style.display = 'flex';
        pvAlertBox.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
    }
    function hideAlert() {
        if (pvAlertBox) pvAlertBox.style.display = 'none';
    }
    if (pvAlertClose) pvAlertClose.addEventListener('click', hideAlert);

    function showLoading()  { if (pvLoadingOverlay) pvLoadingOverlay.classList.add('active'); }
    function hideLoading()  { if (pvLoadingOverlay) pvLoadingOverlay.classList.remove('active'); }

    /* ── Formatters ──────────────────────────────────────────── */

    function formatVnd(amount) {
        return Number(amount || 0).toLocaleString('vi-VN') + ' ₫';
    }

    function formatDateVn(dateStr) {
        if (!dateStr) return '';
        const [y, m, d] = dateStr.split('-');
        return 'Ngày ' + d + '/' + m + '/' + y;
    }

    function escHtml(str) {
        if (str == null) return '';
        return String(str)
            .replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;')
            .replace(/"/g, '&quot;');
    }

    /* ══════════════════════════════════════════════════════════
       RENDER PREVIEW DATA
       Toàn bộ dữ liệu đã được backend gộp sẵn trong courtDetails
       ══════════════════════════════════════════════════════════ */

    function renderPreview() {
        // Venue info
        if (pvVenueName)   pvVenueName.textContent   = previewData.venueName   || '—';
        if (pvVenueAddr)   pvVenueAddr.textContent   = previewData.venueAddress|| '—';
        if (pvBookingDate) pvBookingDate.textContent = formatDateVn(previewData.bookingDate || bookingDate);
        if (pvDuration)    pvDuration.textContent    = previewData.totalDuration || '—';

        // Courts & time ranges
        renderCourtDetails();

        // Financial summary
        updateFinancials();
    }

    function renderCourtDetails() {
        if (!pvCourtsWrap) return;
        const courts = previewData.courtDetails || [];

        if (courts.length === 0) {
            pvCourtsWrap.innerHTML = '<p class="text-muted text-center py-3">Không có thông tin sân.</p>';
            return;
        }

        let html = '';
        courts.forEach(function (court) {
            html += '<div class="pv-court-card mb-3">';
            html += '<div class="pv-court-name"><i class="bi bi-grid-3x3-gap-fill text-success me-2"></i>'
                + escHtml(court.courtName || 'Sân') + '</div>';

            const ranges = court.timeRanges || [];
            ranges.forEach(function (r) {
                html += '<div class="pv-range-row">';
                html += '<span class="pv-range-time"><i class="bi bi-clock text-success"></i>'
                    + escHtml(r.startTime) + ' – ' + escHtml(r.endTime) + '</span>';
                html += '<span class="pv-range-duration">' + escHtml(r.duration || '') + '</span>';
                html += '<span class="pv-range-price">' + formatVnd(r.price) + '</span>';
                html += '</div>';
            });

            if (court.courtTotal != null) {
                html += '<div class="pv-court-subtotal">Tổng sân: <strong class="ms-2">'
                    + formatVnd(court.courtTotal) + '</strong></div>';
            }
            html += '</div>';
        });
        pvCourtsWrap.innerHTML = html;
    }

    /* ══════════════════════════════════════════════════════════
       FINANCIAL CALCULATION
       ══════════════════════════════════════════════════════════ */

    function updateFinancials() {
        const subtotal  = Number(previewData.subtotal   || 0);
        const discount  = Number(previewData.discount   || 0);
        const totalAmt  = Number(previewData.totalAmount || subtotal - discount);

        const payAmount = depositPercent === 100
            ? totalAmt
            : Math.ceil(totalAmt * depositPercent / 100);

        if (pvSubtotal)    pvSubtotal.textContent = formatVnd(subtotal);
        if (pvDiscount)    pvDiscount.textContent  = discount > 0 ? '- ' + formatVnd(discount) : formatVnd(0);
        if (pvDiscountRow) pvDiscountRow.style.display = discount > 0 ? '' : 'none';

        if (pvTotal)      pvTotal.textContent = formatVnd(totalAmt);

        // Deposit row
        if (pvDepositRow) {
            if (depositPercent < 100) {
                pvDepositRow.style.display = '';
                if (pvDepositAmt) pvDepositAmt.textContent = formatVnd(payAmount);
            } else {
                pvDepositRow.style.display = 'none';
            }
        }

        // Confirm bar amount
        if (pvConfirmBarAmt) pvConfirmBarAmt.textContent = formatVnd(payAmount);
    }

    /* ══════════════════════════════════════════════════════════
       PAYMENT METHOD & DEPOSIT RADIO BUTTONS
       ══════════════════════════════════════════════════════════ */

    // Payment method (VNPAY / BANK_TRANSFER / CASH)
    document.querySelectorAll('input[name="pvPaymentMethod"]').forEach(function (radio) {
        radio.addEventListener('change', function () {
            paymentMethod = this.value;
            // highlight selected label
            document.querySelectorAll('.pv-radio-label.method-opt').forEach(function (lbl) {
                lbl.classList.remove('selected');
            });
            const lbl = this.closest('.pv-radio-label');
            if (lbl) lbl.classList.add('selected');
        });
    });

    // Deposit options (100% / 30%)
    document.querySelectorAll('input[name="pvDeposit"]').forEach(function (radio) {
        radio.addEventListener('change', function () {
            depositPercent = parseInt(this.value, 10);
            // highlight
            document.querySelectorAll('.pv-radio-label.deposit-opt').forEach(function (lbl) {
                lbl.classList.remove('selected');
            });
            const lbl = this.closest('.pv-radio-label');
            if (lbl) lbl.classList.add('selected');
            updateFinancials();
        });
    });

    // Init deposit label highlight
    (function () {
        const checked = document.querySelector('input[name="pvDeposit"]:checked');
        if (checked) {
            const lbl = checked.closest('.pv-radio-label');
            if (lbl) lbl.classList.add('selected');
            depositPercent = parseInt(checked.value, 10);
        }
        const checkedMethod = document.querySelector('input[name="pvPaymentMethod"]:checked');
        if (checkedMethod) {
            const lbl = checkedMethod.closest('.pv-radio-label');
            if (lbl) lbl.classList.add('selected');
            paymentMethod = checkedMethod.value;
        }
    })();

    /* ══════════════════════════════════════════════════════════
       CONFIRM & PAY
       ══════════════════════════════════════════════════════════ */

    if (pvConfirmBtn) {
        pvConfirmBtn.addEventListener('click', function () {
            if (isSubmitting) return;   // double-submit guard
            doConfirmAndPay();
        });
    }

    function doConfirmAndPay() {
        isSubmitting = true;
        setConfirmBtnLoading(true);
        hideAlert();
        showLoading();

        const voucherCode   = pvVoucherInput ? pvVoucherInput.value.trim() : '';
        const customerNote  = pvNoteInput    ? pvNoteInput.value.trim()    : '';

        const body = {
            venueId:        venueId,
            date:           bookingDate,
            selections:     selections,
            paymentMethod:  paymentMethod,
            depositPercent: depositPercent,
            customerNote:   customerNote || undefined,
            voucherCode:    voucherCode  || undefined
        };

        fetch(CTX + '/api/single-booking/confirm-and-pay', {
            method:  'POST',
            headers: { 'Content-Type': 'application/json', 'Accept': 'application/json' },
            body:    JSON.stringify(body)
        })
        .then(function (res) {
            hideLoading();
            if (res.status === 401) throw { _status: 401, code: 'UNAUTHORIZED' };
            if (res.status === 409) return res.json().then(function (j) { j._status = 409; throw j; });
            if (res.status === 400) return res.json().then(function (j) { j._status = 400; throw j; });
            if (res.status === 500) return res.json().then(function (j) { j._status = 500; throw j; });
            if (!res.ok)            return res.json().then(function (j) { throw j; });
            return res.json();
        })
        .then(function (json) {
            // Hỗ trợ cả wrapper và flat response
            const data = (json.success !== undefined) ? json : { success: true, ...json };

            if (!data.success) {
                isSubmitting = false;
                setConfirmBtnLoading(false);
                const msg = (data.error && data.error.message)
                    ? data.error.message
                    : (data.errorMessage || 'Không thể xác nhận đặt sân.');
                showAlert(msg, 'error');
                return;
            }

            // Thành công → clear sessionStorage và redirect payment
            sessionStorage.removeItem('sbPreviewData');
            const payUrl = data.paymentUrl || data.data?.paymentUrl;
            if (payUrl) {
                window.location.href = payUrl;
            } else {
                // Không có paymentUrl (e.g. CASH/BANK_TRANSFER) → redirect booking success
                window.location.href = CTX + '/?bookingSuccess=1&bookingId=' + (data.bookingId || '');
            }
        })
        .catch(function (err) {
            hideLoading();
            isSubmitting = false;
            setConfirmBtnLoading(false);

            // 401 Unauthorized
            if (err && (err._status === 401 || err.code === 'UNAUTHORIZED')) {
                handleUnauthorized();
                return;
            }

            // 409 Slot conflict → redirect về matrix để chọn lại
            if (err && err._status === 409) {
                const msg = (err.error && err.error.message)
                    ? err.error.message
                    : 'Slot đã được người khác đặt. Vui lòng chọn lại.';
                showAlert(msg + ' Đang chuyển về trang chọn sân...', 'error');
                sessionStorage.removeItem('sbPreviewData');
                setTimeout(function () {
                    window.location.href = CTX
                        + '/jsp/booking/singlebooking/single-booking.jsp'
                        + '?venueId=' + encodeURIComponent(venueId)
                        + '&date='   + encodeURIComponent(bookingDate);
                }, 2500);
                return;
            }

            // 400 Validation
            if (err && err._status === 400) {
                const msg = (err.error && err.error.message)
                    ? err.error.message
                    : 'Dữ liệu không hợp lệ.';
                showAlert(msg, 'error');
                return;
            }

            // Network / other
            const msg = (err && err.error && err.error.message)
                ? err.error.message
                : 'Lỗi kết nối. Vui lòng kiểm tra mạng và thử lại.';
            showAlert(msg, 'error');
        });
    }

    function setConfirmBtnLoading(loading) {
        if (!pvConfirmBtn) return;
        pvConfirmBtn.disabled = loading;
        pvConfirmBtn.innerHTML = loading
            ? '<span class="spinner-border spinner-border-sm me-2" role="status"></span> Đang xử lý...'
            : '<i class="bi bi-credit-card me-2"></i> XÁC NHẬN & THANH TOÁN';
    }

    /* ══════════════════════════════════════════════════════════
       AUTH
       ══════════════════════════════════════════════════════════ */

    function handleUnauthorized() {
        showAlert('Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.', 'warning');
        setTimeout(function () {
            window.location.href = CTX + '/auth/login?redirect=' + encodeURIComponent(window.location.href);
        }, 1500);
    }

    /* ══════════════════════════════════════════════════════════
       BACK BUTTON
       ══════════════════════════════════════════════════════════ */

    const pvBackBtn = document.getElementById('pvBackBtn');
    if (pvBackBtn) {
        pvBackBtn.addEventListener('click', function () {
            window.history.back();
        });
    }

    /* ══════════════════════════════════════════════════════════
       INIT
       ══════════════════════════════════════════════════════════ */

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', renderPreview);
    } else {
        renderPreview();
    }

})();
