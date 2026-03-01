/**
 * staff-booking-detail.js — Task 4 + Task 6 + Task 7 cleanup
 */
(function () {
    'use strict';

    var CTX = window.ST_CTX || '';

    // DOM
    var stateLoading  = document.getElementById('stateLoading');
    var stateError    = document.getElementById('stateError');
    var errorMessage  = document.getElementById('errorMessage');
    var detailContent = document.getElementById('detailContent');
    var backLink      = document.getElementById('backLink');
    var backLinkError = document.getElementById('backLinkError');

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

    // ─── Extract booking ID from URL ───
    var pathParts = window.location.pathname.split('/');
    var bookingId = pathParts[pathParts.length - 1];

    if (!bookingId || isNaN(bookingId)) {
        showError('Booking ID không hợp lệ.');
        return;
    }

    // ─── Fetch (updated URL: /api/staff/booking/detail/{id}) ───
    fetch(CTX + '/api/staff/booking/detail/' + bookingId, {
        credentials: 'same-origin',
        headers: { 'Accept': 'application/json' }
    })
        .then(function (res) {
            if (!res.ok) {
                return res.json().then(function (err) {
                    throw new Error(err.message || 'HTTP ' + res.status);
                }).catch(function () {
                    throw new Error('HTTP ' + res.status);
                });
            }
            return res.json();
        })
        .then(function (body) {
            if (!body.success) throw new Error(body.message || 'Lỗi');
            render(body.data);
        })
        .catch(function (err) {
            console.error('Booking detail error:', err);
            showError(err.message || 'Không thể tải dữ liệu.');
        });

    function showError(msg) {
        stateLoading.classList.add('d-none');
        detailContent.classList.add('d-none');
        stateError.classList.remove('d-none');
        errorMessage.textContent = msg;
    }

    function render(d) {
        stateLoading.classList.add('d-none');
        stateError.classList.add('d-none');
        detailContent.classList.remove('d-none');

        // Update back URL with booking date for timeline context
        if (referrer.indexOf('/staff/booking/list') === -1 && d.bookingDate) {
            backUrl = CTX + '/staff/timeline?date=' + d.bookingDate;
            setBackUrl(backUrl);
        }

        setText('dBookingId', d.bookingId);
        setText('dBookingIdField', '#' + d.bookingId);
        setText('dBookingDate', formatDate(d.bookingDate));
        setText('dCreatedAt', d.createdAt || '—');
        setText('dCheckinTime', d.checkinTime || '—');
        setText('dCheckoutTime', d.checkoutTime || '—');

        // Status badge
        var badge = document.getElementById('dStatusBadge');
        badge.textContent = statusLabel(d.bookingStatus);
        badge.className = 'sbd-status-badge sbd-status-' + d.bookingStatus.toLowerCase();

        var statusEl = document.getElementById('dBookingStatus');
        statusEl.innerHTML = '<span class="sbd-status-badge sbd-status-' +
            d.bookingStatus.toLowerCase() + '" style="font-size:0.6875rem;padding:0.2rem 0.6rem;">' +
            statusLabel(d.bookingStatus) + '</span>';

        setText('dCustomerName', d.customerName || '—');
        setText('dCustomerPhone', d.customerPhone || '—');
        setText('dCustomerType', d.customerType === 'ACCOUNT' ? 'Tài khoản' : 'Khách vãng lai');

        // Slots table
        var tbody = document.getElementById('dSlotTableBody');
        tbody.innerHTML = '';
        if (d.slots && d.slots.length > 0) {
            d.slots.forEach(function (s) {
                var tr = document.createElement('tr');
                tr.innerHTML =
                    '<td><strong>' + esc(s.courtName) + '</strong></td>' +
                    '<td>' + s.startTime + '</td>' +
                    '<td>' + s.endTime + '</td>' +
                    '<td class="text-end">' + formatMoney(s.price) + '</td>';
                tbody.appendChild(tr);
            });
        } else {
            tbody.innerHTML = '<tr><td colspan="4" class="text-center sbd-no-data">Không có slot</td></tr>';
        }

        // Invoice
        if (d.invoice) {
            setText('dTotalAmount', formatMoney(d.invoice.totalAmount));
            setText('dPaidAmount', formatMoney(d.invoice.paidAmount));
            var payEl = document.getElementById('dPaymentStatus');
            var ps = d.invoice.paymentStatus;
            payEl.innerHTML = '<span class="sbd-pay-' + ps.toLowerCase() + '">' + paymentLabel(ps) + '</span>';
        } else {
            setText('dTotalAmount', '—');
            setText('dPaidAmount', '—');
            setText('dPaymentStatus', '—');
        }
    }

    // ─── Helpers ───
    function setText(id, val) {
        var el = document.getElementById(id);
        if (el) el.textContent = val;
    }

    function esc(s) {
        if (!s) return '';
        var div = document.createElement('div');
        div.textContent = s;
        return div.innerHTML;
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

    function statusLabel(s) {
        var labels = { PENDING: 'Chờ xác nhận', CONFIRMED: 'Đã xác nhận', COMPLETED: 'Hoàn thành', CANCELLED: 'Đã hủy', EXPIRED: 'Hết hạn' };
        return labels[s] || s;
    }

    function paymentLabel(s) {
        var labels = { UNPAID: 'Chưa thanh toán', PARTIAL: 'Thanh toán một phần', PAID: 'Đã thanh toán' };
        return labels[s] || s;
    }

})();