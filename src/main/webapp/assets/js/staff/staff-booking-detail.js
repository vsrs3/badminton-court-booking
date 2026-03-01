/**
 * staff-booking-detail.js — Task 4: Fetch & render booking detail
 */
(function () {
    'use strict';

    const CTX = window.ST_CTX || '';

    // DOM
    const stateLoading  = document.getElementById('stateLoading');
    const stateError    = document.getElementById('stateError');
    const errorMessage  = document.getElementById('errorMessage');
    const detailContent = document.getElementById('detailContent');

    // ─── Extract booking ID from URL path ───
    // URL pattern: /staff/booking/detail/{id}
    const pathParts = window.location.pathname.split('/');
    const bookingId = pathParts[pathParts.length - 1];

    if (!bookingId || isNaN(bookingId)) {
        showError('Booking ID không hợp lệ.');
        return;
    }

    // ─── Fetch ───
    fetch(CTX + '/api/staff/booking/' + bookingId, {
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

    // ─── Show error ───
    function showError(msg) {
        stateLoading.classList.add('d-none');
        detailContent.classList.add('d-none');
        stateError.classList.remove('d-none');
        errorMessage.textContent = msg;
    }

    // ─── Render ───
    function render(d) {
        stateLoading.classList.add('d-none');
        stateError.classList.add('d-none');
        detailContent.classList.remove('d-none');

        // Booking info
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

        // Booking status in card
        var statusEl = document.getElementById('dBookingStatus');
        statusEl.innerHTML = '<span class="sbd-status-badge sbd-status-' +
            d.bookingStatus.toLowerCase() + '" style="font-size:0.6875rem; padding:0.2rem 0.6rem;">' +
            statusLabel(d.bookingStatus) + '</span>';

        // Customer info
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

        // Invoice / Payment
        if (d.invoice) {
            setText('dTotalAmount', formatMoney(d.invoice.totalAmount));
            setText('dPaidAmount', formatMoney(d.invoice.paidAmount));

            var payEl = document.getElementById('dPaymentStatus');
            var ps = d.invoice.paymentStatus;
            payEl.innerHTML = '<span class="sbd-pay-' + ps.toLowerCase() + '">' +
                paymentLabel(ps) + '</span>';
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
        var day = String(d.getDate()).padStart(2, '0');
        var mon = String(d.getMonth() + 1).padStart(2, '0');
        return day + '/' + mon + '/' + d.getFullYear();
    }

    function formatMoney(amount) {
        if (amount == null) return '—';
        return Number(amount).toLocaleString('vi-VN') + 'đ';
    }

    function statusLabel(s) {
        switch (s) {
            case 'PENDING':   return 'Chờ xác nhận';
            case 'CONFIRMED': return 'Đã xác nhận';
            case 'COMPLETED': return 'Hoàn thành';
            case 'CANCELLED': return 'Đã hủy';
            case 'EXPIRED':   return 'Hết hạn';
            default:          return s;
        }
    }

    function paymentLabel(s) {
        switch (s) {
            case 'UNPAID':  return 'Chưa thanh toán';
            case 'PARTIAL': return 'Thanh toán một phần';
            case 'PAID':    return 'Đã thanh toán';
            default:        return s;
        }
    }

})();