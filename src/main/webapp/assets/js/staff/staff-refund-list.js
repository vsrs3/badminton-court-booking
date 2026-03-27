(function () {
    'use strict';

    var CTX = window.ST_CTX || '';

    var resultsInfo   = document.getElementById('resultsInfo');
    var resultsText   = document.getElementById('resultsText');
    var stateLoading  = document.getElementById('stateLoading');
    var stateError    = document.getElementById('stateError');
    var stateEmpty    = document.getElementById('stateEmpty');
    var tableContent  = document.getElementById('tableContent');
    var tableBody     = document.getElementById('tableBody');
    var paginationUl  = document.getElementById('paginationUl');
    var errorMessage  = document.getElementById('errorMessage');
    var searchInput   = document.getElementById('searchInput');
    var searchClear   = document.getElementById('searchClear');
    var searchBtn     = document.getElementById('searchBtn');

    var currentPage = 1;
    var pageSize = 10;
    var currentQuery = '';

    function showState(state) {
        stateLoading.classList.add('d-none');
        stateError.classList.add('d-none');
        stateEmpty.classList.add('d-none');
        tableContent.classList.add('d-none');

        switch (state) {
            case 'loading': stateLoading.classList.remove('d-none'); break;
            case 'error':   stateError.classList.remove('d-none');   break;
            case 'empty':   stateEmpty.classList.remove('d-none');   break;
            case 'table':   tableContent.classList.remove('d-none'); break;
        }
    }

    window.loadRefunds = function () {
        showState('loading');
        resultsInfo.classList.add('d-none');

        var url = CTX + '/api/staff/refund/list?page=' + currentPage + '&size=' + pageSize;
        if (currentQuery) {
            url += '&q=' + encodeURIComponent(currentQuery);
        }

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
                console.error('Refund list error:', err);
                errorMessage.textContent = err.message || 'Không thể tải dữ liệu.';
                showState('error');
            });
    };

    function renderList(data) {
        var refunds = data.refunds;

        resultsInfo.classList.remove('d-none');
        if (data.totalRows === 0) {
            resultsText.textContent = currentQuery
                ? 'Không tìm thấy yêu cầu hoàn tiền phù hợp.'
                : 'Không có yêu cầu hoàn tiền nào.';
            showState('empty');
            return;
        }

        var from = (data.page - 1) * data.size + 1;
        var to = Math.min(data.page * data.size, data.totalRows);
        var prefix = currentQuery ? 'Kết quả cho "' + currentQuery + '": ' : '';
        resultsText.textContent = prefix + 'Hiển thị ' + from + '-' + to + ' trong ' + data.totalRows + ' yêu cầu';

        tableBody.innerHTML = '';
        refunds.forEach(function (r) {
            var tr = document.createElement('tr');

            var noteHtml = esc(r.refundNote) || '<span class="srl-note">-</span>';
            var paidTotal = fmtMoney(r.paidAmount) + ' / ' + fmtMoney(r.totalAmount);

            tr.innerHTML =
                '<td><a class="srl-booking-id" href="' + CTX + '/staff/booking/detail/' + r.bookingId + '">#' + r.bookingId + '</a></td>' +
                '<td>' + esc(r.customerName) + '</td>' +
                '<td>' + esc(r.phone) + '</td>' +
                '<td>' + fmtDate(r.bookingDate) + '</td>' +
                '<td>' + paidTotal + '</td>' +
                '<td><span class="srl-money">' + fmtMoney(r.refundDue) + '</span></td>' +
                '<td>' + noteHtml + '</td>' +
                '<td>' + actionButton(r.bookingId) + '</td>';

            tableBody.appendChild(tr);
        });

        wireActions();
        renderPagination(data.page, data.totalPages);
        showState('table');
    }

    function actionButton(bookingId) {
        return '<button class="srl-action-btn" data-booking-id="' + bookingId + '">' +
               '<i class="bi bi-check2"></i>Xác nhận</button>';
    }

    function wireActions() {
        var buttons = tableBody.querySelectorAll('.srl-action-btn');
        buttons.forEach(function (btn) {
            btn.addEventListener('click', function (e) {
                e.preventDefault();
                var bookingId = parseInt(btn.getAttribute('data-booking-id'), 10);
                if (!bookingId) return;

                confirmRefundFlow(bookingId, btn);
            });
        });
    }

    function confirmRefundFlow(bookingId, btn) {
        if (!window.StaffDialog) {
            return;
        }

        StaffDialog.confirm({
            title: 'Xác nhận',
            message: 'Xác nhận hoàn tiền cho booking #' + bookingId
        }).then(function (ok) {
            if (!ok) return;
            return StaffDialog.prompt({
                title: 'Ghi chú xác nhận',
                message: 'Nhập ghi chú xác nhận (không bắt buộc):',
                defaultValue: '',
                placeholder: 'Ví dụ: Đã chuyển khoản ngân hàng'
            });
        }).then(function (note) {
            if (note === null || typeof note === 'undefined') return;
            btn.disabled = true;
            return doConfirmRefund(bookingId, note)
                .finally(function () {
                    btn.disabled = false;
                });
        });
    }

    function doConfirmRefund(bookingId, note) {
        return fetch(CTX + '/api/staff/refund/confirm', {
            method: 'POST',
            credentials: 'same-origin',
            headers: { 'Content-Type': 'application/json', 'Accept': 'application/json' },
            body: JSON.stringify({ bookingId: bookingId, note: note || '' })
        })
            .then(function (res) {
                return res.json().then(function (body) {
                    if (!res.ok || !body.success) {
                        throw new Error(body.message || 'Lỗi');
                    }
                    return body;
                });
            })
            .then(function () {
                loadRefunds();
            })
            .catch(function (err) {
                if (window.StaffDialog) {
                    return StaffDialog.alert({ title: 'Lỗi', message: err.message || 'Không thể xác nhận hoàn tiền' });
                }
            });
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
                loadRefunds();
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
        if (!s) return '';
        var div = document.createElement('div');
        div.textContent = s;
        return div.innerHTML;
    }

    function fmtDate(d) {
        if (!d) return '-';
        var dt = new Date(d + 'T00:00:00');
        return String(dt.getDate()).padStart(2, '0') + '/' +
            String(dt.getMonth() + 1).padStart(2, '0') + '/' +
            dt.getFullYear();
    }

    function fmtMoney(val) {
        if (val === null || val === undefined || val === '') return '-';
        var num = Number(val);
        if (isNaN(num)) return val;
        return num.toLocaleString('vi-VN');
    }

    function updateSearchClear() {
        if (!searchClear) return;
        searchClear.disabled = !currentQuery;
        searchClear.classList.toggle('d-none', !currentQuery);
    }

    function bindSearch() {
        if (!searchInput) return;

        var timer = null;
        function applySearch(value) {
            var next = (value || '').trim();
            if (next === currentQuery) return;
            currentQuery = next;
            currentPage = 1;
            updateSearchClear();
            loadRefunds();
        }

        searchInput.addEventListener('input', function () {
            if (timer) clearTimeout(timer);
            timer = setTimeout(function () {
                applySearch(searchInput.value);
            }, 300);
        });

        searchInput.addEventListener('keydown', function (e) {
            if (e.key === 'Enter') {
                e.preventDefault();
                applySearch(searchInput.value);
            }
        });

        if (searchClear) {
            searchClear.addEventListener('click', function () {
                if (!currentQuery) return;
                searchInput.value = '';
                applySearch('');
            });
        }

        if (searchBtn) {
            searchBtn.addEventListener('click', function () {
                applySearch(searchInput.value);
            });
        }

        updateSearchClear();
    }

    bindSearch();
    loadRefunds();

})();
