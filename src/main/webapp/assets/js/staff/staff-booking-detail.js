/**
 * staff-booking-detail.js — Task 8 v2 + Payment confirmation before check-in/out
 */
(function () {
    'use strict';

    var CTX = window.ST_CTX || '';

    // DOM
    var stateLoading      = document.getElementById('stateLoading');
    var stateError        = document.getElementById('stateError');
    var errorMessage      = document.getElementById('errorMessage');
    var detailContent     = document.getElementById('detailContent');
    var backLink          = document.getElementById('backLink');
    var backLinkError     = document.getElementById('backLinkError');
    var sessionsContainer = document.getElementById('sessionsContainer');
    var sessionProgress   = document.getElementById('sessionProgress');

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
    var paymentModalError   = document.getElementById('paymentModalError');
    var paymentInputHint    = document.getElementById('paymentInputHint');

    // State
    var bookingData = null;
    var pendingAction = null; // {type: 'checkin'|'checkout', sessionIndex: N}

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
        showError('Booking ID không hợp lệ.');
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

    // Close modal on overlay click
    if (paymentModal) {
        paymentModal.addEventListener('click', function (e) {
            if (e.target === paymentModal) closePaymentModal();
        });
    }

    // Close modal on Escape key
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
                showError(err.message || 'Không thể tải dữ liệu.');
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
    // ═════════════════════════════════════════���═

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
        setText('dCustomerType', d.customerType === 'ACCOUNT' ? 'Tài khoản' : 'Khách vãng lai');

        // Invoice
        renderInvoice(d);

        // Sessions
        renderSessions(d);
    }

    // ─── Render Invoice ───
    function renderInvoice(d) {
        if (d.invoice) {
            var totalAmount = d.invoice.totalAmount;
            var paidAmount = d.invoice.paidAmount;
            var remaining = totalAmount - paidAmount;
            var isPaid = d.invoice.paymentStatus === 'PAID';
            var isConfirmed = (d.bookingStatus === 'CONFIRMED');

            setText('dTotalAmount', formatMoney(totalAmount));
            setText('dPaidAmount', formatMoney(paidAmount));

            // Remaining field: only show when not paid AND booking is confirmed
            if (isPaid || !isConfirmed) {
                remainingField.classList.add('d-none');
            } else {
                remainingField.classList.remove('d-none');
                setText('dRemainingAmount', formatMoney(remaining));
            }

            var payEl = document.getElementById('dPaymentStatus');
            payEl.innerHTML = '<span class="sbd-pay-' + d.invoice.paymentStatus.toLowerCase() + '">' +
                paymentLabel(d.invoice.paymentStatus) + '</span>';

            // Show confirm payment button ONLY when: not PAID + booking CONFIRMED
            if (!isPaid && isConfirmed) {
                confirmPaymentBtnWrap.classList.remove('d-none');
                // Show warning banner only when actions would be shown (today + confirmed)
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
            setText('dTotalAmount', '—');
            setText('dPaidAmount', '—');
            remainingField.classList.add('d-none');
            setText('dPaymentStatus', '—');
            confirmPaymentBtnWrap.classList.add('d-none');
            paymentWarningBanner.classList.add('d-none');
        }
    }

    // ─── Render Sessions ───
    function renderSessions(d) {
        sessionsContainer.innerHTML = '';
        var sessions = d.sessions || [];

        if (sessions.length === 0) {
            sessionsContainer.innerHTML = '<div class="sbd-no-data">Không có phiên chơi</div>';
            sessionProgress.textContent = '';
            return;
        }

        // Progress: count completed
        var completedCount = 0;
        sessions.forEach(function (s) { if (s.sessionStatus === 'COMPLETED') completedCount++; });
        sessionProgress.textContent = completedCount + '/' + sessions.length + ' hoàn thành';

        // Is booking today + CONFIRMED? → show action buttons
        var isToday = (d.bookingDate === todayStr());
        var isConfirmed = (d.bookingStatus === 'CONFIRMED');
        var showActions = isToday && isConfirmed;

        sessions.forEach(function (s, i) {
            var row = document.createElement('div');
            row.className = 'sbd-session sbd-ss-' + s.sessionStatus.toLowerCase();
            row.id = 'session-' + i;

            // ─ Index badge ─
            var idxEl = document.createElement('div');
            idxEl.className = 'sbd-session-idx';
            if (s.sessionStatus === 'COMPLETED') {
                idxEl.innerHTML = '<i class="bi bi-check-lg"></i>';
            } else if (s.sessionStatus === 'CHECKED_IN') {
                idxEl.innerHTML = '<i class="bi bi-play-fill"></i>';
            } else {
                idxEl.textContent = i + 1;
            }
            row.appendChild(idxEl);

            // ─ Info block ─
            var infoEl = document.createElement('div');
            infoEl.className = 'sbd-session-info';

            var courtEl = document.createElement('div');
            courtEl.className = 'sbd-session-court';
            courtEl.textContent = s.courtName;
            infoEl.appendChild(courtEl);

            var metaEl = document.createElement('div');
            metaEl.className = 'sbd-session-meta';
            metaEl.innerHTML =
                '<span><i class="bi bi-clock"></i>' + s.startTime + ' → ' + s.endTime + '</span>' +
                '<span><i class="bi bi-layers"></i>' + s.slotCount + ' slot</span>' +
                '<span class="sbd-session-price">' + formatMoney(s.totalPrice) + '</span>';
            infoEl.appendChild(metaEl);

            // Time info (checkin/checkout times)
            if (s.checkinTime || s.checkoutTime) {
                var timeInfoEl = document.createElement('div');
                timeInfoEl.className = 'sbd-session-time-info';
                var parts = [];
                if (s.checkinTime) parts.push('In: ' + s.checkinTime);
                if (s.checkoutTime) parts.push('Out: ' + s.checkoutTime);
                timeInfoEl.textContent = parts.join(' · ');
                infoEl.appendChild(timeInfoEl);
            }

            row.appendChild(infoEl);

            // ─ Action area ─
            var actionEl = document.createElement('div');
            actionEl.className = 'sbd-session-action';
            actionEl.id = 'session-action-' + i;

            if (showActions) {
                renderSessionAction(actionEl, s, i, d);
            } else {
                // Just show status label
                var labelEl = document.createElement('span');
                labelEl.className = 'sbd-session-status ' + sessionStatusLabelClass(s.sessionStatus);
                labelEl.textContent = sessionStatusLabel(s.sessionStatus);
                actionEl.appendChild(labelEl);
            }

            row.appendChild(actionEl);
            sessionsContainer.appendChild(row);
        });
    }

    // ─── Render action for a single session ───
    function renderSessionAction(container, session, idx, d) {
        container.innerHTML = '';
        var sessions = d.sessions;
        var isPaid = d.invoice && d.invoice.paymentStatus === 'PAID';

        if (session.sessionStatus === 'COMPLETED') {
            var label = document.createElement('span');
            label.className = 'sbd-session-status sbd-ss-label-completed';
            label.innerHTML = '<i class="bi bi-check-circle-fill me-1"></i>Hoàn thành';
            container.appendChild(label);
            return;
        }

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

        // PENDING — check if this session can be checked in
        var canCheckin = true;
        for (var i = 0; i < idx; i++) {
            if (sessions[i].sessionStatus === 'PENDING') {
                canCheckin = false;
                break;
            }
        }

        if (canCheckin) {
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
        } else {
            var waitBtn = document.createElement('span');
            waitBtn.className = 'sbd-btn sbd-btn-waiting';
            waitBtn.innerHTML = '<i class="bi bi-hourglass-split me-1"></i>Chờ phiên trước';
            container.appendChild(waitBtn);
        }
    }

    // ═══════════════════════════════════════════
    // PAYMENT MODAL
    // ═══════════════════════════════════════════

    function openPaymentModal() {
        if (!bookingData || !bookingData.invoice) return;

        var inv = bookingData.invoice;
        var remaining = inv.totalAmount - inv.paidAmount;

        setText('modalTotalAmount', formatMoney(inv.totalAmount));
        setText('modalPaidAmount', formatMoney(inv.paidAmount));
        setText('modalRemainingAmount', formatMoney(remaining));

        paymentAmountInput.value = remaining;
        paymentAmountInput.max = remaining;
        paymentInputHint.textContent = 'Nhập đúng ' + formatMoney(remaining) + ' để hoàn tất thanh toán';

        hideModalError();
        paymentModal.classList.remove('d-none');

        // Focus input after a short delay (allow CSS transition)
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
        var remaining = inv.totalAmount - inv.paidAmount;
        var inputVal = paymentAmountInput.value.trim();

        if (!inputVal || isNaN(inputVal)) {
            showModalError('Vui lòng nhập số tiền hợp lệ.');
            return;
        }

        var amount = parseFloat(inputVal);
        if (amount <= 0) {
            showModalError('Số tiền phải lớn hơn 0.');
            return;
        }
        if (amount !== remaining) {
            showModalError('Số tiền không hợp lệ. Cần thu thêm đúng ' + formatMoney(remaining) + ' để đủ tổng tiền.');
            return;
        }

        // Disable confirm button
        paymentModalConfirm.disabled = true;
        paymentModalConfirm.innerHTML = '<span class="sbd-btn-spinner"></span> Đang xử lý...';

        fetch(CTX + '/api/staff/payment/confirm', {
            method: 'POST',
            credentials: 'same-origin',
            headers: { 'Content-Type': 'application/json', 'Accept': 'application/json' },
            body: JSON.stringify({ bookingId: parseInt(bookingId), amount: amount })
        })
            .then(function (res) { return res.json(); })
            .then(function (body) {
                if (!body.success) {
                    showModalError(body.message || 'Xác nhận thanh toán thất bại.');
                    resetConfirmButton();
                    return;
                }

                // Update local state
                bookingData.invoice.paidAmount = body.data.paidAmount;
                bookingData.invoice.paymentStatus = body.data.paymentStatus;

                showToast('Xác nhận thanh toán thành công!', 'success');
                closePaymentModal();
                resetConfirmButton();

                // Re-render invoice section
                renderInvoice(bookingData);

                // Re-render sessions (buttons now enabled)
                renderSessions(bookingData);

                // Execute pending action if any
                if (pendingAction) {
                    var action = pendingAction;
                    pendingAction = null;

                    // Small delay so user can see the payment success toast
                    setTimeout(function () {
                        if (action.type === 'checkin') {
                            handleCheckin(action.sessionIndex);
                        } else if (action.type === 'checkout') {
                            handleCheckout(action.sessionIndex);
                        }
                    }, 500);
                }
            })
            .catch(function (err) {
                console.error('Payment confirm error:', err);
                showModalError('Lỗi kết nối. Vui lòng thử lại.');
                resetConfirmButton();
            });
    }

    function resetConfirmButton() {
        paymentModalConfirm.disabled = false;
        paymentModalConfirm.innerHTML = '<i class="bi bi-check-lg me-1"></i>Xác nhận';
    }

    // ═══════════════════════════════════════════
    // CHECK-IN / CHECK-OUT HANDLERS
    // ═══════════════════════════════════════════

    function handleCheckin(sessionIndex) {
        var session = bookingData.sessions[sessionIndex];
        if (!confirm('Xác nhận CHECK-IN phiên ' + (sessionIndex + 1) + '?\n' +
            session.courtName + ' (' + session.startTime + ' → ' + session.endTime + ')')) return;

        var btn = document.querySelector('#session-action-' + sessionIndex + ' .sbd-btn');
        if (btn) {
            btn.disabled = true;
            btn.innerHTML = '<span class="sbd-btn-spinner"></span> Đang xử lý...';
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

                showToast('Check-in phiên ' + (sessionIndex + 1) + ' thành công!', 'success');

                // Update local state
                bookingData.sessions[sessionIndex].sessionStatus = 'CHECKED_IN';
                bookingData.sessions[sessionIndex].checkinTime = body.data.checkinTime;

                // Re-render sessions
                renderSessions(bookingData);
            })
            .catch(function (err) {
                console.error('Checkin error:', err);
                showToast('Lỗi kết nối. Vui lòng thử lại.', 'error');
                if (btn) {
                    btn.disabled = false;
                    btn.innerHTML = '<i class="bi bi-box-arrow-in-right me-1"></i>Check-in';
                }
            });
    }

    function handleCheckout(sessionIndex) {
        var session = bookingData.sessions[sessionIndex];
        if (!confirm('Xác nhận CHECK-OUT phiên ' + (sessionIndex + 1) + '?\n' +
            session.courtName + ' (' + session.startTime + ' → ' + session.endTime + ')')) return;

        var btn = document.querySelector('#session-action-' + sessionIndex + ' .sbd-btn');
        if (btn) {
            btn.disabled = true;
            btn.innerHTML = '<span class="sbd-btn-spinner"></span> Đang xử lý...';
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

                showToast('Check-out phiên ' + (sessionIndex + 1) + ' thành công!', 'success');

                // Update local state
                bookingData.sessions[sessionIndex].sessionStatus = 'COMPLETED';
                bookingData.sessions[sessionIndex].checkoutTime = body.data.checkoutTime;

                // If all completed → update booking status
                if (body.data.bookingCompleted) {
                    bookingData.bookingStatus = 'COMPLETED';

                    // Update header badge
                    var badge = document.getElementById('dStatusBadge');
                    badge.textContent = bookingStatusLabel('COMPLETED');
                    badge.className = 'sbd-status-badge sbd-status-completed';

                    var statusEl = document.getElementById('dBookingStatus');
                    statusEl.innerHTML = '<span class="sbd-status-badge sbd-status-completed" style="font-size:0.6875rem;padding:0.2rem 0.6rem;">' +
                        bookingStatusLabel('COMPLETED') + '</span>';

                    showToast('🎉 Tất cả phiên hoàn thành — Booking đã COMPLETED!', 'success');
                }

                // Re-render sessions
                renderSessions(bookingData);
            })
            .catch(function (err) {
                console.error('Checkout error:', err);
                showToast('Lỗi kết nối. Vui lòng thử lại.', 'error');
                if (btn) {
                    btn.disabled = false;
                    btn.innerHTML = '<i class="bi bi-box-arrow-right me-1"></i>Check-out';
                }
            });
    }

    // ═══════════════════════════════════════════
    // TOAST NOTIFICATION
    // ═══════════════════════════════════════════

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
    // ═══════════════════════════════════════════

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

    function bookingStatusLabel(s) {
        var m = { PENDING: 'Chờ xác nhận', CONFIRMED: 'Đã xác nhận', COMPLETED: 'Hoàn thành', CANCELLED: 'Đã hủy', EXPIRED: 'Hết hạn' };
        return m[s] || s;
    }

    function sessionStatusLabel(s) {
        var m = { PENDING: 'Chờ check-in', CHECKED_IN: 'Đang chơi', COMPLETED: 'Hoàn thành' };
        return m[s] || s;
    }

    function sessionStatusLabelClass(s) {
        var m = { PENDING: 'sbd-ss-label-pending', CHECKED_IN: 'sbd-ss-label-checked_in', COMPLETED: 'sbd-ss-label-completed' };
        return m[s] || 'sbd-ss-label-pending';
    }

    function paymentLabel(s) {
        var m = { UNPAID: 'Chưa thanh toán', PARTIAL: 'Thanh toán một phần', PAID: 'Đã thanh toán' };
        return m[s] || s;
    }

})();