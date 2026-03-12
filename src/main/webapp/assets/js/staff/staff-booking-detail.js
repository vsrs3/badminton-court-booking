/**
 * staff-booking-detail.js — Task 8 v2 + Payment + NO_SHOW
 */
(function () {
    'use strict';

    var CTX = window.ST_CTX || '';
    var NO_SHOW_BUFFER_MINUTES = 15;


    // DOM
    var stateLoading      = document.getElementById('stateLoading');
    var stateError        = document.getElementById('stateError');
    var errorMessage      = document.getElementById('errorMessage');
    var detailContent     = document.getElementById('detailContent');
    var backLink          = document.getElementById('backLink');
    var backLinkError     = document.getElementById('backLinkError');
    var sessionsTableBody = document.getElementById('sessionsTableBody');
    var sessionProgress   = document.getElementById('sessionProgress');
    var btnEditBooking    = document.getElementById('btnEditBooking');
    var btnCancelRemaining = document.getElementById('btnCancelRemaining');


    // Payment DOM
    var confirmPaymentBtnWrap = document.getElementById('confirmPaymentBtnWrap');
    var btnConfirmPayment     = document.getElementById('btnConfirmPayment');
    var paymentWarningBanner  = document.getElementById('paymentWarningBanner');
    var remainingField        = document.getElementById('dRemainingField');

    // Modal DOM
    var paymentModal        = document.getElementById('paymentModal');
    var paymentModalClose   = document.getElementById('paymentModalClose');
    var paymentModalCancel  = document.getElementById('paymentModalCancel');
    var paymentModalConfirm = document.getElementById('paymentModalConfirm');
    var paymentAmountInput  = document.getElementById('paymentAmountInput');
    var paymentMethodSelect = document.getElementById('paymentMethodSelect');
    var paymentModalError   = document.getElementById('paymentModalError');
    var paymentInputHint    = document.getElementById('paymentInputHint');

    // State
    var bookingData = null;
    var pendingAction = null;

    // ─── Smart back URL ───
    var backUrl = CTX + '/staff/timeline';
    var referrer = document.referrer || '';
    if (referrer.indexOf('/staff/booking/list') !== -1) {
        backUrl = CTX + '/staff/booking/list';
    }
    function setBackUrl(url) {
        if (backLink)      backLink.href = url;
        if (backLinkError) backLinkError.href = url;
    }
    setBackUrl(backUrl);

    // ─── Extract booking ID ───
    var pathParts = window.location.pathname.split('/');
    var bookingId = pathParts[pathParts.length - 1];
    if (!bookingId || isNaN(bookingId)) {
        showError('Booking ID kh\u00f4ng h\u1ee3p l\u1ec7.');
        return;
    }

    // ─── Modal event listeners ───
    if (btnConfirmPayment) {
        btnConfirmPayment.addEventListener('click', function () {
            pendingAction = null;
            openPaymentModal();
        });
    }
    if (paymentModalClose)   paymentModalClose.addEventListener('click', closePaymentModal);
    if (paymentModalCancel)  paymentModalCancel.addEventListener('click', closePaymentModal);
    if (paymentModalConfirm) paymentModalConfirm.addEventListener('click', handleConfirmPayment);

    if (paymentModal) {
        paymentModal.addEventListener('click', function (e) {
            if (e.target === paymentModal) closePaymentModal();
        });
    }
    document.addEventListener('keydown', function (e) {
        if (e.key === 'Escape' && paymentModal && !paymentModal.classList.contains('d-none')) {
            closePaymentModal();
        }
    });

    // ─── Load ───
    loadDetail();

    function loadDetail() {
        stateLoading.classList.remove('d-none');
        stateError.classList.add('d-none');
        detailContent.classList.add('d-none');

        fetch(CTX + '/api/staff/booking/detail/' + bookingId, {
            credentials: 'same-origin',
            headers: { 'Accept': 'application/json' }
        })
            .then(function (res) {
                if (!res.ok) {
                    return res.json().then(function (e) { throw new Error(e.message || 'HTTP ' + res.status); })
                        .catch(function () { throw new Error('HTTP ' + res.status); });
                }
                return res.json();
            })
            .then(function (body) {
                if (!body.success) throw new Error(body.message || 'Lỗi');
                bookingData = body.data;
                render(body.data);
            })
            .catch(function (err) {
                console.error('Detail error:', err);
                showError(err.message || 'Kh\u00f4ng th\u1ec3 t\u1ea3i d\u1eef li\u1ec7u.');
            });
    }

    function showError(msg) {
        stateLoading.classList.add('d-none');
        detailContent.classList.add('d-none');
        stateError.classList.remove('d-none');
        errorMessage.textContent = msg;
    }

    // ═══════════════════════════════════════════
    // RENDER
    // ═══════════════════════════════════════════

    function render(d) {
        stateLoading.classList.add('d-none');
        stateError.classList.add('d-none');
        detailContent.classList.remove('d-none');

        if (referrer.indexOf('/staff/booking/list') === -1 && d.bookingDate) {
            setBackUrl(CTX + '/staff/timeline?date=' + d.bookingDate);
        }

        setText('dBookingId', d.bookingId);
        setText('dBookingIdField', '#' + d.bookingId);
        setText('dBookingDate', formatDate(d.bookingDate));
        setText('dCreatedAt', d.createdAt || '—');

        // Status badge
        var badge = document.getElementById('dStatusBadge');
        badge.textContent = bookingStatusLabel(d.bookingStatus);
        badge.className = 'sbd-status-badge sbd-status-' + d.bookingStatus.toLowerCase();

        var statusEl = document.getElementById('dBookingStatus');
        statusEl.innerHTML = '<span class="sbd-status-badge sbd-status-' +
            d.bookingStatus.toLowerCase() + '" style="font-size:0.6875rem;padding:0.2rem 0.6rem;">' +
            bookingStatusLabel(d.bookingStatus) + '</span>';

        setText('dCustomerName', d.customerName || '—');
        setText('dCustomerPhone', d.customerPhone || '—');
        setText('dCustomerType', d.customerType === 'ACCOUNT' ? 'T\u00e0i kho\u1ea3n' : 'Kh\u00e1ch v\u00e3ng lai');

        renderInvoice(d);
        renderRentalRows(d.rentalRows || []);
        renderSessions(d);
        renderEditActions(d);
    }

    function renderRentalRows(rentalRows) {
        if (!sessionsTableBody) return;

        if (!rentalRows || rentalRows.length === 0) {
            sessionsTableBody.innerHTML = '<tr><td colspan="5" class="text-center text-muted py-3">Không có dữ liệu phiên chơi / đồ thuê</td></tr>';
            return;
        }

        sessionsTableBody.innerHTML = rentalRows.map(function (row) {
            return '' +
                '<tr>' +
                '   <td>' + escapeHtml(row.courtName || '') + '</td>' +
                '   <td>' + escapeHtml(row.startTime || '') + ' - ' + escapeHtml(row.endTime || '') + '</td>' +
                '   <td>' + escapeHtml(row.rentalItemsText || '') + '</td>' +
                '   <td>' + formatMoney(row.rentalTotal || 0) + '</td>' +
                '   <td>-</td>' +
                '</tr>';
        }).join('');
    }

    function renderEditActions(d) {
        var canEdit = d.bookingStatus === 'CONFIRMED';

        if (btnEditBooking) {
            btnEditBooking.classList.toggle('d-none', !canEdit);
            btnEditBooking.onclick = function () {
                window.location.href = CTX + '/staff/timeline?date=' + encodeURIComponent(d.bookingDate) + '&editBookingId=' + d.bookingId;
            };
        }

        if (btnCancelRemaining) {
            btnCancelRemaining.classList.toggle('d-none', !canEdit);
            btnCancelRemaining.onclick = function () {
                handleCancelRemaining();
            };
        }
    }


    // ─── Render Invoice ───
    function renderInvoice(d) {
        if (d.invoice) {
            var courtAmount = d.invoice && d.invoice.totalAmount != null
                ? d.invoice.totalAmount
                : 0;
            var rentalAmount = d.rentalTotal || 0;
            var totalAmount = d.grandTotal || 0;
            var paidAmount = d.invoice.paidAmount || 0;
            var remaining = totalAmount - paidAmount;
            var isPaid = d.invoice.paymentStatus === 'PAID';
            var isConfirmed = (d.bookingStatus === 'CONFIRMED');

            setText('dCourtAmount', formatMoney(courtAmount));
            setText('dRentalAmount', formatMoney(rentalAmount));
            setText('dTotalAmount', formatMoney(totalAmount));
            setText('dPaidAmount', formatMoney(paidAmount));

            if (isPaid || !isConfirmed) {
                remainingField.classList.add('d-none');
            } else {
                remainingField.classList.remove('d-none');
                setText('dRemainingAmount', formatMoney(remaining));
            }

            var payEl = document.getElementById('dPaymentStatus');
            payEl.innerHTML = '<span class="sbd-pay-' + d.invoice.paymentStatus.toLowerCase() + '">' +
                paymentLabel(d.invoice.paymentStatus) + '</span>';

            if (!isPaid && isConfirmed) {
                confirmPaymentBtnWrap.classList.remove('d-none');
                var isToday = (d.bookingDate === todayStr());
                if (isToday) {
                    paymentWarningBanner.classList.remove('d-none');
                } else {
                    paymentWarningBanner.classList.add('d-none');
                }
            } else {
                confirmPaymentBtnWrap.classList.add('d-none');
                paymentWarningBanner.classList.add('d-none');
            }
        } else {
            setText('dCourtAmount', '—');
            setText('dRentalAmount', formatMoney(d.rentalTotal || 0));
            setText('dTotalAmount', formatMoney(d.grandTotal || 0));
            setText('dPaidAmount', '—');
            remainingField.classList.add('d-none');
            setText('dPaymentStatus', '—');
            confirmPaymentBtnWrap.classList.add('d-none');
            paymentWarningBanner.classList.add('d-none');
        }
    }

    // ─── Render Sessions (with NO_SHOW support) ───
    function renderSessions(d) {
        var sessions = d.sessions || [];

        if (sessions.length === 0) {
            if (sessionProgress) sessionProgress.textContent = '';
            return;
        }

        var finishedCount = 0;
        var noShowCount = 0;
        var cancelledCount = 0;

        sessions.forEach(function (s) {
            if (s.sessionStatus === 'COMPLETED' || s.sessionStatus === 'NO_SHOW') finishedCount++;
            if (s.sessionStatus === 'NO_SHOW') noShowCount++;
            if (s.sessionStatus === 'CANCELLED') cancelledCount++;
        });

        var progressText = finishedCount + '/' + sessions.length + ' hoàn thành';
        if (noShowCount > 0) {
            progressText += ' (' + noShowCount + ' vắng)';
        }
        if (cancelledCount > 0) {
            progressText += ' (' + cancelledCount + ' hủy)';
        }

        if (sessionProgress) {
            sessionProgress.textContent = progressText;
        }
    }

    function hasReleasableSlot(session) {
        var slots = session.bookingSlots || [];
        for (var i = 0; i < slots.length; i++) {
            var slotStatus = slots[i].slotStatus;
            if (!slots[i].released && slotStatus === 'NO_SHOW') {
                return true;
            }
        }
        return false;
    }

    function hasCheckinableSlot(session) {
        var slots = session.bookingSlots || [];
        for (var i = 0; i < slots.length; i++) {
            if (!slots[i].released && slots[i].slotStatus === 'PENDING') {
                return true;
            }
        }
        return false;
    }

    // Render action for a single session (with NO_SHOW)
    function renderSessionAction(container, session, idx, d, canPlayActions) {
        container.innerHTML = '';
        var sessions = d.sessions;
        var isPaid = d.invoice && d.invoice.paymentStatus === 'PAID';

        // COMPLETED
        if (session.sessionStatus === 'COMPLETED') {
            var label = document.createElement('span');
            label.className = 'sbd-session-status sbd-ss-label-completed';
            label.innerHTML = '<i class="bi bi-check-circle-fill me-1"></i>Ho\u00e0n th\u00e0nh';
            container.appendChild(label);
            return;
        }

        // NO_SHOW
        if (session.sessionStatus === 'NO_SHOW') {
            var nsLabel = document.createElement('span');
            nsLabel.className = 'sbd-session-status sbd-ss-label-no_show';
            nsLabel.innerHTML = '<i class="bi bi-person-x-fill me-1"></i>V\u1eafng m\u1eb7t';
            container.appendChild(nsLabel);
            if (hasReleasableSlot(session)) {
                appendReleaseButton(container, idx);
            }
            return;
        }

        // CANCELLED
        if (session.sessionStatus === 'CANCELLED') {
            var cLabel = document.createElement('span');
            cLabel.className = 'sbd-session-status sbd-ss-label-cancelled';
            cLabel.innerHTML = '<i class="bi bi-x-circle-fill me-1"></i>\u0110\u00e3 h\u1ee7y';
            container.appendChild(cLabel);
            return;
        }

        if (!canPlayActions) {
            var roLabel = document.createElement('span');
            roLabel.className = 'sbd-session-status ' + sessionStatusLabelClass(session.sessionStatus);
            roLabel.textContent = sessionStatusLabel(session.sessionStatus);
            container.appendChild(roLabel);
            if (hasReleasableSlot(session)) {
                appendReleaseButton(container, idx);
            }
            return;
        }

        // CHECKED_IN
        if (session.sessionStatus === 'CHECKED_IN') {
            var btnOut = document.createElement('button');
            btnOut.className = 'sbd-btn sbd-btn-checkout';
            btnOut.innerHTML = '<i class="bi bi-box-arrow-right me-1"></i>Check-out';
            btnOut.addEventListener('click', function () {
                if (!isPaid) {
                    pendingAction = { type: 'checkout', sessionIndex: idx };
                    openPaymentModal();
                } else {
                    handleCheckout(idx);
                }
            });
            container.appendChild(btnOut);
            return;
        }

        // PENDING
        var canAct = true;
        for (var i = 0; i < idx; i++) {
            if (sessions[i].sessionStatus === 'PENDING') {
                if (isSessionPastDue(sessions[i])) {
                    continue;
                }
                canAct = false;
                break;
            }
        }

        if (canAct) {
            if (!hasCheckinableSlot(session)) {
                var releasedLabel = document.createElement('span');
                releasedLabel.className = 'sbd-session-status sbd-ss-label-completed';
                releasedLabel.textContent = '\u0110\u00e3 gi\u1ea3i ph\u00f3ng';
                container.appendChild(releasedLabel);
                return;
            }

            var btnIn = document.createElement('button');
            btnIn.className = 'sbd-btn sbd-btn-checkin';
            btnIn.innerHTML = '<i class="bi bi-box-arrow-in-right me-1"></i>Check-in';
            btnIn.addEventListener('click', function () {
                if (!isPaid) {
                    pendingAction = { type: 'checkin', sessionIndex: idx };
                    openPaymentModal();
                } else {
                    handleCheckin(idx);
                }
            });
            container.appendChild(btnIn);

            if (isSessionPastDue(session)) {
                var btnNs = document.createElement('button');
                btnNs.className = 'sbd-btn sbd-btn-noshow';
                btnNs.innerHTML = '<i class="bi bi-person-x me-1"></i>\u0110\u00e1nh d\u1ea5u v\u1eafng';
                btnNs.addEventListener('click', function () {
                    handleNoShow(idx);
                });
                container.appendChild(btnNs);
            }

            if (hasReleasableSlot(session)) {
                appendReleaseButton(container, idx);
            }
        } else {
            var waitBtn = document.createElement('span');
            waitBtn.className = 'sbd-btn sbd-btn-waiting';
            waitBtn.innerHTML = '<i class="bi bi-hourglass-split me-1"></i>Ch\u1edd phi\u00ean tr\u01b0\u1edbc';
            container.appendChild(waitBtn);
            if (hasReleasableSlot(session)) {
                appendReleaseButton(container, idx);
            }
        }
    }

    function appendReleaseButton(container, sessionIndex) {
        var btnRelease = document.createElement('button');
        btnRelease.className = 'sbd-btn sbd-btn-release';
        btnRelease.innerHTML = '<i class="bi bi-unlock me-1"></i>Gi\u1ea3i ph\u00f3ng slot';
        btnRelease.addEventListener('click', function () {
            handleReleaseSession(sessionIndex);
        });
        container.appendChild(btnRelease);
    }

    /**
     * Check if a session is past due (now > startTime + 15 min buffer).
     * startTime is "HH:mm" format from API.
     */
    function isSessionPastDue(session) {
        if (!session.startTime) return false;
        var parts = session.startTime.split(':');
        var h = parseInt(parts[0], 10);
        var m = parseInt(parts[1], 10);
        if (isNaN(h) || isNaN(m)) return false;

        // Add buffer
        var deadlineMinutes = h * 60 + m + NO_SHOW_BUFFER_MINUTES;

        var now = new Date();
        var nowMinutes = now.getHours() * 60 + now.getMinutes();

        return nowMinutes > deadlineMinutes;
    }

    // ═══════════════════════════════════════════
    // PAYMENT MODAL (unchanged)
    // ═══════════════════════════════════════════

    function openPaymentModal() {
        if (!bookingData || !bookingData.invoice) return;
        var inv = bookingData.invoice;
        var totalAmount = bookingData.grandTotal || 0;
        var paidAmount = inv.paidAmount || 0;
        var remaining = totalAmount - paidAmount;

        setText('modalTotalAmount', formatMoney(totalAmount));
        setText('modalPaidAmount', formatMoney(paidAmount));
        setText('modalRemainingAmount', formatMoney(remaining));

        paymentAmountInput.value = remaining;
        paymentAmountInput.max = remaining;
        if (paymentMethodSelect) paymentMethodSelect.value = 'CASH';
        paymentInputHint.textContent = 'Nh\u1eadp \u0111\u00fang ' + formatMoney(remaining) + ' \u0111\u1ec3 ho\u00e0n t\u1ea5t thanh to\u00e1n';

        hideModalError();
        paymentModal.classList.remove('d-none');

        setTimeout(function () {
            paymentAmountInput.focus();
            paymentAmountInput.select();
        }, 100);
    }

    function closePaymentModal() {
        paymentModal.classList.add('d-none');
        pendingAction = null;
        hideModalError();
    }

    function showModalError(msg) {
        paymentModalError.textContent = msg;
        paymentModalError.classList.remove('d-none');
    }
    function hideModalError() {
        paymentModalError.classList.add('d-none');
        paymentModalError.textContent = '';
    }

    function handleConfirmPayment() {
        hideModalError();
        var inv = bookingData.invoice;
        var remaining = (bookingData.grandTotal || 0) - (inv.paidAmount || 0);
        var inputVal = paymentAmountInput.value.trim();

        if (!inputVal || isNaN(inputVal)) {
            showModalError('Vui l\u00f2ng nh\u1eadp s\u1ed1 ti\u1ec1n h\u1ee3p l\u1ec7.');
            return;
        }
        var amount = parseFloat(inputVal);
        if (amount <= 0) {
            showModalError('Số tiền phải lớn hơn 0.');
            return;
        }
        if (amount !== remaining) {
            showModalError('S\u1ed1 ti\u1ec1n kh\u00f4ng h\u1ee3p l\u1ec7. C\u1ea7n thu th\u00eam \u0111\u00fang ' + formatMoney(remaining) + ' để đủ tổng tiền.');
            return;
        }

        var method = paymentMethodSelect ? paymentMethodSelect.value : 'CASH';
        if (['CASH','BANK_TRANSFER','VNPAY'].indexOf(method) === -1) {
            showModalError('Phuong thuc thanh toan khong hop le.');
            return;
        }

        paymentModalConfirm.disabled = true;
        paymentModalConfirm.innerHTML = '<span class="sbd-btn-spinner"></span> \u0110ang x\u1eed l\u00fd...';

        fetch(CTX + '/api/staff/payment/confirm', {
            method: 'POST',
            credentials: 'same-origin',
            headers: { 'Content-Type': 'application/json', 'Accept': 'application/json' },
            body: JSON.stringify({ bookingId: parseInt(bookingId), amount: amount, method: method })
        })
            .then(function (res) { return res.json(); })
            .then(function (body) {
                if (!body.success) {
                    showModalError(body.message || 'X\u00e1c nh\u1eadn thanh to\u00e1n th\u1ea5t b\u1ea1i.');
                    resetConfirmButton();
                    return;
                }
                bookingData.invoice.paidAmount = body.data.paidAmount;
                bookingData.invoice.paymentStatus = body.data.paymentStatus;
                showToast('X\u00e1c nh\u1eadn thanh to\u00e1n th\u00e0nh c\u00f4ng!', 'success');
                closePaymentModal();
                resetConfirmButton();
                renderInvoice(bookingData);
                renderRentalRows(bookingData.rentalRows || []);
                renderSessions(bookingData);

                if (pendingAction) {
                    var action = pendingAction;
                    pendingAction = null;
                    setTimeout(function () {
                        if (action.type === 'checkin') handleCheckin(action.sessionIndex);
                        else if (action.type === 'checkout') handleCheckout(action.sessionIndex);
                    }, 500);
                }
            })
            .catch(function (err) {
                console.error('Payment confirm error:', err);
                showModalError('Lỗi kết nối. Vui l\u00f2ng th\u1eed l\u1ea1i.');
                resetConfirmButton();
            });
    }

    function resetConfirmButton() {
        paymentModalConfirm.disabled = false;
        paymentModalConfirm.innerHTML = '<i class="bi bi-check-lg me-1"></i>X\u00e1c nh\u1eadn';
    }

    // ═══════════════════════════════════════════
    // CHECK-IN / CHECK-OUT / NO-SHOW HANDLERS
    // ═══════════════════════════════════════════

    async function handleCheckin(sessionIndex) {
        var session = bookingData.sessions[sessionIndex];
        var confirmMsg = 'X\u00e1c nh\u1eadn CHECK-IN phi\u00ean ' + (sessionIndex + 1) + '?\n' +
            session.courtName + ' (' + session.startTime + ' \u2192 ' + session.endTime + ')';
        if (!(await uiConfirm(confirmMsg, 'X\u00e1c nh\u1eadn check-in'))) return;

        var btn = document.querySelector('#session-action-' + sessionIndex + ' .sbd-btn-checkin');
        if (btn) {
            btn.disabled = true;
            btn.innerHTML = '<span class="sbd-btn-spinner"></span> \u0110ang x\u1eed l\u00fd...';
        }

        fetch(CTX + '/api/staff/checkin', {
            method: 'POST',
            credentials: 'same-origin',
            headers: { 'Content-Type': 'application/json', 'Accept': 'application/json' },
            body: JSON.stringify({ bookingId: parseInt(bookingId), sessionIndex: sessionIndex })
        })
            .then(function (res) { return res.json(); })
            .then(function (body) {
                if (!body.success) {
                    showToast(body.message || 'Check-in thất bại', 'error');
                    if (btn) {
                        btn.disabled = false;
                        btn.innerHTML = '<i class="bi bi-box-arrow-in-right me-1"></i>Check-in';
                    }
                    return;
                }

                // Update local state for target session
                bookingData.sessions[sessionIndex].sessionStatus = 'CHECKED_IN';
                bookingData.sessions[sessionIndex].checkinTime = body.data.checkinTime;

                // Auto-mark previous sessions as NO_SHOW if backend did so
                var autoCount = body.data.autoNoShowCount || 0;
                if (autoCount > 0) {
                    for (var i = 0; i < sessionIndex; i++) {
                        if (bookingData.sessions[i].sessionStatus === 'PENDING') {
                            bookingData.sessions[i].sessionStatus = 'NO_SHOW';
                        }
                    }
                }

                var msg = 'Check-in phi\u00ean ' + (sessionIndex + 1) + ' th\u00e0nh c\u00f4ng!';
                if (autoCount > 0) {
                    msg += ' (' + autoCount + ' phi\u00ean tr\u01b0\u1edbc \u0111\u00e1nh d\u1ea5u v\u1eafng)';
                }
                showToast(msg, 'success');

                renderSessions(bookingData);
            })
            .catch(function (err) {
                console.error('Checkin error:', err);
                showToast('Lỗi kết nối. Vui l\u00f2ng th\u1eed l\u1ea1i.', 'error');
                if (btn) {
                    btn.disabled = false;
                    btn.innerHTML = '<i class="bi bi-box-arrow-in-right me-1"></i>Check-in';
                }
            });
    }

    async function handleCheckout(sessionIndex) {
        var session = bookingData.sessions[sessionIndex];
        var confirmMsg = 'X\u00e1c nh\u1eadn CHECK-OUT phi\u00ean ' + (sessionIndex + 1) + '?\n' +
            session.courtName + ' (' + session.startTime + ' \u2192 ' + session.endTime + ')';
        if (!(await uiConfirm(confirmMsg, 'X\u00e1c nh\u1eadn check-out'))) return;

        var btn = document.querySelector('#session-action-' + sessionIndex + ' .sbd-btn');
        if (btn) {
            btn.disabled = true;
            btn.innerHTML = '<span class="sbd-btn-spinner"></span> \u0110ang x\u1eed l\u00fd...';
        }

        fetch(CTX + '/api/staff/checkout', {
            method: 'POST',
            credentials: 'same-origin',
            headers: { 'Content-Type': 'application/json', 'Accept': 'application/json' },
            body: JSON.stringify({ bookingId: parseInt(bookingId), sessionIndex: sessionIndex })
        })
            .then(function (res) { return res.json(); })
            .then(function (body) {
                if (!body.success) {
                    showToast(body.message || 'Check-out thất bại', 'error');
                    if (btn) {
                        btn.disabled = false;
                        btn.innerHTML = '<i class="bi bi-box-arrow-right me-1"></i>Check-out';
                    }
                    return;
                }

                showToast('Check-out phi\u00ean ' + (sessionIndex + 1) + ' th\u00e0nh c\u00f4ng!', 'success');
                bookingData.sessions[sessionIndex].sessionStatus = 'COMPLETED';
                bookingData.sessions[sessionIndex].checkoutTime = body.data.checkoutTime;

                if (body.data.bookingCompleted) {
                    updateBookingCompleted();
                }

                renderSessions(bookingData);
            })
            .catch(function (err) {
                console.error('Checkout error:', err);
                showToast('Lỗi kết nối. Vui l\u00f2ng th\u1eed l\u1ea1i.', 'error');
                if (btn) {
                    btn.disabled = false;
                    btn.innerHTML = '<i class="bi bi-box-arrow-right me-1"></i>Check-out';
                }
            });
    }

    async function handleNoShow(sessionIndex) {
        var session = bookingData.sessions[sessionIndex];
        var confirmMsg = '\u0110\u00e1nh d\u1ea5u V\u1eaeNG M\u1eb6T phi\u00ean ' + (sessionIndex + 1) + '?\n' +
            session.courtName + ' (' + session.startTime + ' \u2192 ' + session.endTime + ')\n\n' +
            'H\u00e0nh \u0111\u1ed9ng n\u00e0y kh\u00f4ng th\u1ec3 ho\u00e0n t\u00e1c.';
        if (!(await uiConfirm(confirmMsg, 'X\u00e1c nh\u1eadn no-show'))) return;

        var btn = document.querySelector('#session-action-' + sessionIndex + ' .sbd-btn-noshow');
        if (btn) {
            btn.disabled = true;
            btn.innerHTML = '<span class="sbd-btn-spinner"></span> \u0110ang x\u1eed l\u00fd...';
        }

        fetch(CTX + '/api/staff/noshow', {
            method: 'POST',
            credentials: 'same-origin',
            headers: { 'Content-Type': 'application/json', 'Accept': 'application/json' },
            body: JSON.stringify({ bookingId: parseInt(bookingId), sessionIndex: sessionIndex })
        })
            .then(function (res) { return res.json(); })
            .then(function (body) {
                if (!body.success) {
                    showToast(body.message || '\u0110\u00e1nh d\u1ea5u v\u1eafng th\u1ea5t b\u1ea1i', 'error');
                    if (btn) {
                        btn.disabled = false;
                        btn.innerHTML = '<i class="bi bi-person-x me-1"></i>\u0110\u00e1nh d\u1ea5u v\u1eafng';
                    }
                    return;
                }

                showToast('Phi\u00ean ' + (sessionIndex + 1) + ' \u0111\u00e3 \u0111\u00e1nh d\u1ea5u v\u1eafng m\u1eb7t', 'warning');
                bookingData.sessions[sessionIndex].sessionStatus = 'NO_SHOW';

                if (body.data.bookingCompleted) {
                    updateBookingCompleted();
                }

                renderSessions(bookingData);
            })
            .catch(function (err) {
                console.error('No-show error:', err);
                showToast('Lỗi kết nối. Vui l\u00f2ng th\u1eed l\u1ea1i.', 'error');
                if (btn) {
                    btn.disabled = false;
                    btn.innerHTML = '<i class="bi bi-person-x me-1"></i>\u0110\u00e1nh d\u1ea5u v\u1eafng';
                }
            });
    }

    function updateBookingCompleted() {
        bookingData.bookingStatus = 'COMPLETED';

        var badge = document.getElementById('dStatusBadge');
        badge.textContent = bookingStatusLabel('COMPLETED');
        badge.className = 'sbd-status-badge sbd-status-completed';

        var statusEl = document.getElementById('dBookingStatus');
        statusEl.innerHTML = '<span class="sbd-status-badge sbd-status-completed" style="font-size:0.6875rem;padding:0.2rem 0.6rem;">' +
            bookingStatusLabel('COMPLETED') + '</span>';

        showToast('Booking \u0111\u00e3 COMPLETED!', 'success');
    }

    // ═══════════════════════════════════════════
    // TOAST
    // ═══════════════════════════════════════════

    async function handleReleaseSession(sessionIndex) {
        var session = bookingData.sessions[sessionIndex];
        var bookingSlots = (session.bookingSlots || []).filter(function (slot) {
            return !slot.released && slot.slotStatus === 'NO_SHOW';
        });

        if (!bookingSlots.length) {
            showToast('Kh\u00f4ng c\u00f2n slot NO_SHOW \u0111\u1ec3 gi\u1ea3i ph\u00f3ng', 'warning');
            return;
        }

        if (!(await uiConfirm('Gi\u1ea3i ph\u00f3ng to\u00e0n b\u1ed9 slot NO_SHOW c\u1ee7a phi\u00ean ' + (sessionIndex + 1) + '?', 'X\u00e1c nh\u1eadn gi\u1ea3i ph\u00f3ng'))) return;

        var reason = await uiPrompt('Nh\u1eadp l\u00fd do gi\u1ea3i ph\u00f3ng slot NO_SHOW (kh\u00f4ng b\u1eaft bu\u1ed9c):', '', 'L\u00fd do gi\u1ea3i ph\u00f3ng');
        if (reason == null) return;
        reason = reason || '';
        var idx = 0;

        function releaseNext() {
            if (idx >= bookingSlots.length) {
                showToast('Gi\u1ea3i ph\u00f3ng slot th\u00e0nh c\u00f4ng', 'success');
                loadDetail();
                return;
            }

            fetch(CTX + '/api/staff/booking/release-slot', {
                method: 'POST',
                credentials: 'same-origin',
                headers: { 'Content-Type': 'application/json', 'Accept': 'application/json' },
                body: JSON.stringify({
                    bookingId: parseInt(bookingId, 10),
                    etag: bookingData.etag,
                    bookingSlotId: bookingSlots[idx].bookingSlotId,
                    reason: reason
                })
            })
                .then(function (res) {
                    return res.json().then(function (body) {
                        return { ok: res.ok, body: body };
                    });
                })
                .then(function (rs) {
                    if (!rs.ok || !rs.body.success) {
                        if (rs.body && rs.body.data && rs.body.data.currentEtag) {
                            bookingData.etag = rs.body.data.currentEtag;
                        }
                        throw new Error((rs.body && rs.body.message) || 'Gi\u1ea3i ph\u00f3ng slot th\u1ea5t b\u1ea1i');
                    }
                    bookingData.etag = rs.body.data && rs.body.data.etag ? rs.body.data.etag : bookingData.etag;
                    idx++;
                    releaseNext();
                })
                .catch(function (err) {
                    console.error('Release slot error:', err);
                    showToast(err.message || 'Gi\u1ea3i ph\u00f3ng slot th\u1ea5t b\u1ea1i', 'error');
                });
        }

        releaseNext();
    }

    async function handleCancelRemaining() {
        if (!bookingData || bookingData.bookingStatus !== 'CONFIRMED') return;
        if (!(await uiConfirm('H\u1ee7y to\u00e0n b\u1ed9 c\u00e1c slot c\u00f2n l\u1ea1i c\u1ee7a booking n\u00e0y?', 'X\u00e1c nh\u1eadn h\u1ee7y ph\u1ea7n c\u00f2n l\u1ea1i'))) return;

        var reason = await uiPrompt('Nh\u1eadp l\u00fd do h\u1ee7y (b\u1eaft bu\u1ed9c):', '', 'L\u00fd do h\u1ee7y');
        if (reason == null) return;
        if (!reason || !reason.trim()) {
            showToast('Vui l\u00f2ng nh\u1eadp l\u00fd do h\u1ee7y', 'error');
            return;
        }

        fetch(CTX + '/api/staff/booking/cancel', {
            method: 'POST',
            credentials: 'same-origin',
            headers: { 'Content-Type': 'application/json', 'Accept': 'application/json' },
            body: JSON.stringify({
                bookingId: parseInt(bookingId, 10),
                etag: bookingData.etag,
                confirmAllRemaining: true,
                reason: reason.trim()
            })
        })
            .then(function (res) {
                return res.json().then(function (body) {
                    return { ok: res.ok, body: body };
                });
            })
            .then(function (rs) {
                if (!rs.ok || !rs.body.success) {
                    if (rs.body && rs.body.data && rs.body.data.currentEtag) {
                        bookingData.etag = rs.body.data.currentEtag;
                    }
                    throw new Error((rs.body && rs.body.message) || 'H\u1ee7y booking th\u1ea5t b\u1ea1i');
                }
                showToast('\u0110\u00e3 h\u1ee7y c\u00e1c slot c\u00f2n l\u1ea1i', 'warning');
                loadDetail();
            })
            .catch(function (err) {
                console.error('Cancel remaining error:', err);
                showToast(err.message || 'H\u1ee7y booking th\u1ea5t b\u1ea1i', 'error');
            });
    }



    function uiAlert(message, title) {
        if (window.StaffDialog && typeof window.StaffDialog.alert === 'function') {
            return window.StaffDialog.alert({ title: title || 'Thong bao', message: message || '' });
        }
        alert(message || '');
        return Promise.resolve();
    }

    function uiConfirm(message, title) {
        if (window.StaffDialog && typeof window.StaffDialog.confirm === 'function') {
            return window.StaffDialog.confirm({ title: title || 'Xac nhan', message: message || '' });
        }
        return Promise.resolve(confirm(message || ''));
    }

    function uiPrompt(message, defaultValue, title) {
        if (window.StaffDialog && typeof window.StaffDialog.prompt === 'function') {
            return window.StaffDialog.prompt({
                title: title || 'Nhap thong tin',
                message: message || '',
                defaultValue: defaultValue || ''
            });
        }
        return Promise.resolve(prompt(message || '', defaultValue || ''));
    }

    function showToast(msg, type) {
        var toast = document.createElement('div');
        toast.className = 'sbd-toast sbd-toast-' + type;
        toast.textContent = msg;
        document.body.appendChild(toast);
        setTimeout(function () {
            toast.style.opacity = '0';
            toast.style.transition = 'opacity 0.3s';
            setTimeout(function () { toast.remove(); }, 300);
        }, 3000);
    }

    // ═══════════════════════════════════════════
    // HELPERS
    // =======================================

    function setText(id, val) {
        var el = document.getElementById(id);
        if (el) el.textContent = val;
    }

    function todayStr() {
        var d = new Date();
        return d.getFullYear() + '-' +
            String(d.getMonth() + 1).padStart(2, '0') + '-' +
            String(d.getDate()).padStart(2, '0');
    }

    function formatDate(dateStr) {
        if (!dateStr) return '—';
        var d = new Date(dateStr + 'T00:00:00');
        return String(d.getDate()).padStart(2, '0') + '/' +
            String(d.getMonth() + 1).padStart(2, '0') + '/' + d.getFullYear();
    }

    function formatMoney(amount) {
        if (amount == null) return '—';
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

    function bookingStatusLabel(s) {
        var m = { PENDING: 'Ch\u1edd x\u00e1c nh\u1eadn', CONFIRMED: '\u0110\u00e3 x\u00e1c nh\u1eadn', COMPLETED: 'Ho\u00e0n th\u00e0nh', CANCELLED: '\u0110\u00e3 h\u1ee7y', EXPIRED: 'Hết hạn' };
        return m[s] || s;
    }

    function sessionStatusLabel(s) {
        var m = { PENDING: 'Chờ check-in', CHECKED_IN: 'Đang chơi', COMPLETED: 'Ho\u00e0n th\u00e0nh', NO_SHOW: 'Vắng mặt', CANCELLED: '\u0110\u00e3 h\u1ee7y' };
        return m[s] || s;
    }

    function sessionStatusLabelClass(s) {
        var m = {
            PENDING: 'sbd-ss-label-pending',
            CHECKED_IN: 'sbd-ss-label-checked_in',
            COMPLETED: 'sbd-ss-label-completed',
            NO_SHOW: 'sbd-ss-label-no_show',
            CANCELLED: 'sbd-ss-label-cancelled'
        };
        return m[s] || 'sbd-ss-label-pending';
    }

    function paymentLabel(s) {
        var m = { UNPAID: 'Ch\u01b0a thanh to\u00e1n', PARTIAL: 'Thanh to\u00e1n m\u1ed9t ph\u1ea7n', PAID: '\u0110\u00e3 thanh to\u00e1n' };
        return m[s] || s;
    }

})();

