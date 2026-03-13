(function () {
    'use strict';

    var CTX = window.ST_CTX || '';

    // DOM
    var searchInput   = document.getElementById('searchInput');
    var searchClear   = document.getElementById('searchClear');
    var searchBtn     = document.getElementById('searchBtn');
    var resultsInfo   = document.getElementById('resultsInfo');
    var resultsText   = document.getElementById('resultsText');
    var stateLoading  = document.getElementById('stateLoading');
    var stateError    = document.getElementById('stateError');
    var stateEmpty    = document.getElementById('stateEmpty');
    var tableContent  = document.getElementById('tableContent');
    var tableBody     = document.getElementById('tableBody');
    var paginationUl  = document.getElementById('paginationUl');
    var errorMessage  = document.getElementById('errorMessage');

    var currentPage = 1;
    var pageSize = 10;

    searchInput.addEventListener('input', function () {
        searchClear.classList.toggle('d-none', this.value.length === 0);
    });
    searchClear.addEventListener('click', function () {
        searchInput.value = '';
        searchClear.classList.add('d-none');
        currentPage = 1;
        loadBookings();
    });

    searchBtn.addEventListener('click', function () {
        currentPage = 1;
        loadBookings();
    });
    searchInput.addEventListener('keydown', function (e) {
        if (e.key === 'Enter') {
            e.preventDefault();
            currentPage = 1;
            loadBookings();
        }
    });

    function showState(state) {
        stateLoading.classList.add('d-none');
        stateError.classList.add('d-none');
        stateEmpty.classList.add('d-none');
        tableContent.classList.add('d-none');

        if (state === 'loading') stateLoading.classList.remove('d-none');
        if (state === 'error') stateError.classList.remove('d-none');
        if (state === 'empty') stateEmpty.classList.remove('d-none');
        if (state === 'table') tableContent.classList.remove('d-none');
    }

    window.loadBookings = function () {
        showState('loading');
        resultsInfo.classList.add('d-none');

        var search = searchInput.value.trim();
        var url = CTX + '/api/staff/booking/list?page=' + currentPage + '&size=' + pageSize;
        if (search) url += '&search=' + encodeURIComponent(search);

        fetch(url, { credentials: 'same-origin', headers: { 'Accept': 'application/json' } })
            .then(function (res) {
                if (!res.ok) throw new Error('HTTP ' + res.status);
                return res.json();
            })
            .then(function (body) {
                if (!body.success) throw new Error(body.message || 'Lỗi');
                renderList(body.data);
            })
            .catch(function (err) {
                console.error('Booking list error:', err);
                errorMessage.textContent = err.message || 'Không thể tải dữ liệu.';
                showState('error');
            });
    };

    function renderList(data) {
        var bookings = data.bookings;

        resultsInfo.classList.remove('d-none');
        if (data.totalRows === 0) {
            resultsText.textContent = 'Không tìm thấy kết quả.';
            showState('empty');
            return;
        }

        var from = (data.page - 1) * data.size + 1;
        var to = Math.min(data.page * data.size, data.totalRows);
        resultsText.textContent = 'Hiển thị ' + from + '–' + to + ' trong ' + data.totalRows + ' booking';

        tableBody.innerHTML = '';
        bookings.forEach(function (b) {
            var tr = document.createElement('tr');
            tr.addEventListener('click', function () {
                window.location.href = CTX + '/staff/booking/detail/' + b.bookingId;
            });

            var statusHtml = '<span class="sbl-status sbl-status-' + b.bookingStatus.toLowerCase() + '">' +
                statusLabel(b.bookingStatus) + '</span>';
            if (b.bookingStatus === 'COMPLETED' && b.hasNoShow) {
                statusHtml += ' <span class="sbl-status sbl-status-noshow">Có vắng</span>';
            }

            var dateHtml = b.isRecurring
                ? '<span class="sbl-recurring-tag">??nh k?</span> ' + fmtRange(b.recurringStartDate, b.recurringEndDate)
                : fmtDate(b.bookingDate);

            tr.innerHTML =
                '<td><span class="sbl-booking-id">#' + b.bookingId + '</span></td>' +
                '<td><span class="sbl-customer-name">' + esc(b.customerName) + '</span></td>' +
                '<td>' + esc(b.phone) + '</td>' +
                '<td>' + dateHtml + '</td>' +
                '<td>' + esc(b.courtDisplay) + '</td>' +
                '<td>' + statusHtml + '</td>' +
                '<td>' + paymentBadge(b.paymentStatus) + '</td>';

            tableBody.appendChild(tr);
        });

        renderPagination(data.page, data.totalPages);
        showState('table');
    }

    function renderPagination(current, total) {
        paginationUl.innerHTML = '';
        if (total <= 1) return;

        addPageItem('?', current > 1 ? current - 1 : null, false);

        var pages = buildPageNumbers(current, total, 7);
        pages.forEach(function (p) {
            if (p === '...') {
                addPageItem('...', null, false, true);
            } else {
                addPageItem(p, p, p === current);
            }
        });

        addPageItem('?', current < total ? current + 1 : null, false);
    }

    function addPageItem(label, targetPage, isActive, isEllipsis) {
        var li = document.createElement('li');
        li.className = 'page-item';
        if (isActive) li.classList.add('active');
        if (targetPage === null && !isEllipsis) li.classList.add('disabled');

        var a = document.createElement('a');
        a.className = 'page-link';
        a.href = '#';
        a.textContent = label;

        if (targetPage !== null) {
            a.addEventListener('click', function (e) {
                e.preventDefault();
                currentPage = targetPage;
                loadBookings();
                window.scrollTo({ top: 0, behavior: 'smooth' });
            });
        } else {
            a.addEventListener('click', function (e) { e.preventDefault(); });
        }

        li.appendChild(a);
        paginationUl.appendChild(li);
    }

    function buildPageNumbers(current, total, maxVisible) {
        if (total <= maxVisible) {
            var arr = [];
            for (var i = 1; i <= total; i++) arr.push(i);
            return arr;
        }
        var pages = [];
        var half = Math.floor(maxVisible / 2);
        var start = Math.max(2, current - half);
        var end = Math.min(total - 1, current + half);

        if (end - start + 1 < maxVisible - 2) {
            if (start === 2) end = Math.min(total - 1, start + maxVisible - 3);
            else start = Math.max(2, end - maxVisible + 3);
        }

        pages.push(1);
        if (start > 2) pages.push('...');
        for (var j = start; j <= end; j++) pages.push(j);
        if (end < total - 1) pages.push('...');
        pages.push(total);
        return pages;
    }

    function esc(s) {
        if (!s) return '<span style="color:#D1D5DB;">—</span>';
        var div = document.createElement('div');
        div.textContent = s;
        return div.innerHTML;
    }

    function fmtDate(d) {
        if (!d) return '—';
        var dt = new Date(d + 'T00:00:00');
        return String(dt.getDate()).padStart(2, '0') + '/' +
            String(dt.getMonth() + 1).padStart(2, '0') + '/' +
            dt.getFullYear();
    }

    function fmtRange(start, end) {
        if (!start || !end) return '—';
        return fmtDate(start) + ' – ' + fmtDate(end);
    }

    function statusLabel(code) {
        switch (code) {
            case 'PENDING': return 'Chờ';
            case 'CONFIRMED': return 'Đã xác nhận';
            case 'EXPIRED': return 'Hết hạn';
            case 'CANCELLED': return 'Đã hủy';
            case 'COMPLETED': return 'Hoàn thành';
            default: return code || '—';
        }
    }

    function paymentBadge(status) {
        if (!status) return '<span class="sbl-pay sbl-pay-unpaid">—</span>';
        return '<span class="sbl-pay sbl-pay-' + status.toLowerCase() + '">' + status + '</span>';
    }

    loadBookings();
})();
