(function () {
    'use strict';

    var CTX = window.ST_CTX || '';
    var PAGE_SIZE = 5;
    var state = { courts: [], pages: {} };

    var stateLoading = document.getElementById('stateLoading');
    var stateError = document.getElementById('stateError');
    var stateEmpty = document.getElementById('stateEmpty');
    var courtsContainer = document.getElementById('courtsContainer');
    var errorMessage = document.getElementById('errorMessage');

    function showState(name) {
        stateLoading.classList.add('d-none');
        stateError.classList.add('d-none');
        stateEmpty.classList.add('d-none');
        courtsContainer.classList.add('d-none');

        if (name === 'loading') stateLoading.classList.remove('d-none');
        if (name === 'error') stateError.classList.remove('d-none');
        if (name === 'empty') stateEmpty.classList.remove('d-none');
        if (name === 'content') courtsContainer.classList.remove('d-none');
    }

    window.loadRentalStatus = function () {
        showState('loading');

        fetch(CTX + '/api/staff/rental/status', {
            credentials: 'same-origin',
            headers: { 'Accept': 'application/json' }
        })
            .then(function (res) {
                if (!res.ok) throw new Error('HTTP ' + res.status);
                return res.json();
            })
            .then(function (body) {
                if (!body.success) throw new Error(body.message || 'Khong the tai du lieu');
                state.courts = Array.isArray(body.data && body.data.courts) ? body.data.courts : [];
                renderCourts();
            })
            .catch(function (err) {
                console.error('Rental status load error:', err);
                errorMessage.textContent = err.message || 'Khong the tai du lieu.';
                showState('error');
            });
    };

    function renderCourts() {
        courtsContainer.innerHTML = '';

        if (!state.courts.length) {
            showState('empty');
            return;
        }

        state.courts.forEach(function (court) {
            var card = document.createElement('section');
            card.className = 'srs-card';
            card.dataset.courtId = String(court.courtId);
            card.innerHTML =
                '<div class="srs-card-header">' +
                '  <div class="srs-card-title">' +
                '    <i class="bi bi-grid-3x3-gap-fill"></i>' +
                '    <h2>' + esc(court.courtName || 'S\u00e2n') + '</h2>' +
                '  </div>' +
                '  <span class="srs-card-count" data-role="count">0 d\u00f2ng</span>' +
                '</div>' +
                '<div class="srs-search-bar">' +
                '  <div class="srs-search-input-wrap">' +
                '    <i class="bi bi-person"></i>' +
                '    <input type="text" class="srs-search-input" data-role="customer-search" placeholder="T\u00ecm theo t\u00ean kh\u00e1ch h\u00e0ng">' +
                '  </div>' +
                '  <div class="srs-search-input-wrap">' +
                '    <i class="bi bi-search"></i>' +
                '    <input type="text" class="srs-search-input" data-role="item-search" placeholder="T\u00ecm theo t\u00ean \u0111\u1ed3 thu\u00ea">' +
                '  </div>' +
                '</div>' +
                '<div class="srs-table-wrap table-responsive">' +
                '  <table class="table srs-table">' +
                '    <thead>' +
                '      <tr>' +
                '        <th>STT</th>' +
                '        <th>T\u00ean kh\u00e1ch h\u00e0ng</th>' +
                '        <th>T\u00ean \u0111\u1ed3</th>' +
                '        <th>S\u1ed1 l\u01b0\u1ee3ng thu\u00ea</th>' +
                '        <th>T\u00ean s\u00e2n</th>' +
                '        <th>Slot</th>' +
                '        <th>Ng\u00e0y th\u00e1ng n\u0103m</th>' +
                '        <th>Tr\u1ea1ng th\u00e1i</th>' +
                '      </tr>' +
                '    </thead>' +
                '    <tbody data-role="table-body"></tbody>' +
                '  </table>' +
                '</div>' +
                '<div class="srs-pagination-wrap" data-role="pagination-wrap">' +
                '  <div class="srs-pagination" data-role="pagination"></div>' +
                '</div>';

            courtsContainer.appendChild(card);
            renderCourtRows(card, court);
        });

        showState('content');
    }

    function renderCourtRows(card, court) {
        var tbody = card.querySelector('[data-role="table-body"]');
        var pagination = card.querySelector('[data-role="pagination"]');
        var paginationWrap = card.querySelector('[data-role="pagination-wrap"]');
        var courtId = Number(card.dataset.courtId);
        var customerKeyword = normalize(card.querySelector('[data-role="customer-search"]').value);
        var itemKeyword = normalize(card.querySelector('[data-role="item-search"]').value);
        var rows = Array.isArray(court.rows) ? court.rows.filter(function (row) {
            var matchCustomer = !customerKeyword || normalize(row.customerName).indexOf(customerKeyword) >= 0;
            var matchItem = !itemKeyword || normalize(row.inventoryName).indexOf(itemKeyword) >= 0;
            return matchCustomer && matchItem;
        }) : [];
        var totalPages = Math.max(1, Math.ceil(rows.length / PAGE_SIZE));
        var currentPage = normalizePage(courtId, totalPages);
        var startIndex = (currentPage - 1) * PAGE_SIZE;
        var pageRows = rows.slice(startIndex, startIndex + PAGE_SIZE);

        card.querySelector('[data-role="count"]').textContent = rows.length + ' d\u00f2ng';
        tbody.innerHTML = '';

        if (!rows.length) {
            pagination.innerHTML = '';
            paginationWrap.classList.add('d-none');
            tbody.innerHTML =
                '<tr class="srs-empty-row">' +
                '  <td colspan="8">Kh\u00f4ng c\u00f3 d\u1eef li\u1ec7u ph\u00f9 h\u1ee3p v\u1edbi b\u1ed9 l\u1ecdc hi\u1ec7n t\u1ea1i.</td>' +
                '</tr>';
            return;
        }

        pageRows.forEach(function (row, index) {
            var rentalKey = buildRentalKey(row.rentalIds);
            var tr = document.createElement('tr');
            tr.dataset.rentalKey = rentalKey;
            tr.innerHTML =
                '<td>' + (startIndex + index + 1) + '</td>' +
                '<td>' + esc(row.customerName) + '</td>' +
                '<td>' + esc(row.inventoryName) + '</td>' +
                '<td>' + row.quantity + '</td>' +
                '<td>' + esc(row.courtName) + '</td>' +
                '<td>' + esc(row.slotLabel) + '</td>' +
                '<td>' + esc(formatDate(row.bookingDate)) + '</td>' +
                '<td>' + buildStatusSelect(row, rentalKey) + '</td>';
            tbody.appendChild(tr);
        });

        renderPagination(pagination, paginationWrap, currentPage, totalPages);
    }

    function buildStatusSelect(row, rentalKey) {
        var status = row.status === 'RETURNED' ? 'RETURNED' : 'RENTED';
        var cls = status === 'RETURNED' ? 'status-returned' : 'status-rented';
        return '' +
            '<select class="srs-status-select ' + cls + '"' +
            ' data-role="status-select"' +
            ' data-rental-key="' + rentalKey + '"' +
            ' data-rental-ids="' + escAttr(buildRentalCsv(row.rentalIds)) + '"' +
            ' data-prev-status="' + status + '">' +
            '  <option value="RENTED"' + (status === 'RENTED' ? ' selected' : '') + '>\u0110\u00e3 thu\u00ea</option>' +
            '  <option value="RETURNED"' + (status === 'RETURNED' ? ' selected' : '') + '>\u0110\u00e3 tr\u1ea3</option>' +
            '</select>';
    }

    function handleSearchInput(event) {
        var role = event.target.getAttribute('data-role');
        if (role !== 'customer-search' && role !== 'item-search') return;

        var card = event.target.closest('.srs-card');
        if (!card) return;

        var courtId = Number(card.dataset.courtId);
        state.pages[courtId] = 1;

        var court = findCourt(courtId);
        if (!court) return;
        renderCourtRows(card, court);
    }

    function handlePaginationClick(event) {
        var button = event.target.closest('[data-role="page-btn"]');
        if (!button || button.disabled) return;

        var card = button.closest('.srs-card');
        if (!card) return;

        var courtId = Number(card.dataset.courtId);
        var targetPage = Number(button.getAttribute('data-page'));
        if (!targetPage || targetPage < 1) return;

        state.pages[courtId] = targetPage;

        var court = findCourt(courtId);
        if (!court) return;
        renderCourtRows(card, court);
    }

    function handleStatusChange(event) {
        var select = event.target;
        if (select.getAttribute('data-role') !== 'status-select') return;

        var nextStatus = select.value;
        var prevStatus = select.dataset.prevStatus || 'RENTED';
        var rentalIds = select.dataset.rentalIds || '';
        var card = select.closest('.srs-card');
        if (!card) return;

        var court = findCourt(Number(card.dataset.courtId));
        if (!court) return;

        select.disabled = true;
        select.closest('tr').classList.add('srs-row-updating');

        fetch(CTX + '/api/staff/rental/status', {
            method: 'POST',
            credentials: 'same-origin',
            headers: {
                'Accept': 'application/json',
                'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8'
            },
            body: new URLSearchParams({
                rentalIds: rentalIds,
                status: nextStatus
            }).toString()
        })
            .then(function (res) {
                if (!res.ok) throw new Error('HTTP ' + res.status);
                return res.json();
            })
            .then(function (body) {
                if (!body.success) throw new Error(body.message || 'Cap nhat that bai');

                updateLocalRow(court, select.dataset.rentalKey, nextStatus);
                select.dataset.prevStatus = nextStatus;
                refreshStatusStyle(select, nextStatus);
            })
            .catch(function (err) {
                console.error('Rental status update error:', err);
                select.value = prevStatus;
                refreshStatusStyle(select, prevStatus);
                window.alert(err.message || 'Khong the cap nhat trang thai.');
            })
            .finally(function () {
                select.disabled = false;
                select.closest('tr').classList.remove('srs-row-updating');
            });
    }

    function updateLocalRow(court, rentalKey, nextStatus) {
        if (!court || !Array.isArray(court.rows)) return;
        for (var i = 0; i < court.rows.length; i++) {
            if (buildRentalKey(court.rows[i].rentalIds) === rentalKey) {
                court.rows[i].status = nextStatus;
                return;
            }
        }
    }

    function refreshStatusStyle(select, status) {
        select.classList.remove('status-rented', 'status-returned');
        select.classList.add(status === 'RETURNED' ? 'status-returned' : 'status-rented');
    }

    function findCourt(courtId) {
        for (var i = 0; i < state.courts.length; i++) {
            if (Number(state.courts[i].courtId) === courtId) {
                return state.courts[i];
            }
        }
        return null;
    }

    function buildRentalKey(rentalIds) {
        return buildRentalCsv(rentalIds).replace(/,/g, '-');
    }

    function buildRentalCsv(rentalIds) {
        return Array.isArray(rentalIds) ? rentalIds.join(',') : '';
    }

    function normalizePage(courtId, totalPages) {
        var currentPage = Number(state.pages[courtId] || 1);
        if (currentPage < 1) currentPage = 1;
        if (currentPage > totalPages) currentPage = totalPages;
        state.pages[courtId] = currentPage;
        return currentPage;
    }

    function renderPagination(container, wrap, currentPage, totalPages) {
        if (totalPages <= 1) {
            container.innerHTML = '';
            wrap.classList.add('d-none');
            return;
        }

        wrap.classList.remove('d-none');

        var pages = buildPageNumbers(currentPage, totalPages, 5);
        var html = '';

        html += buildPageButton('&lsaquo;', currentPage - 1, currentPage === 1, false);
        for (var i = 0; i < pages.length; i++) {
            if (pages[i] === '...') {
                html += '<span class="srs-page-ellipsis">...</span>';
            } else {
                html += buildPageButton(String(pages[i]), pages[i], false, pages[i] === currentPage);
            }
        }
        html += buildPageButton('&rsaquo;', currentPage + 1, currentPage === totalPages, false);
        html += '<span class="srs-page-info">Trang ' + currentPage + '/' + totalPages + '</span>';

        container.innerHTML = html;
    }

    function buildPageButton(label, page, disabled, active) {
        var classes = 'srs-page-btn';
        if (active) classes += ' active';
        return '<button type="button" class="' + classes + '" data-role="page-btn" data-page="' + page + '"' +
            (disabled ? ' disabled' : '') + '>' + label + '</button>';
    }

    function buildPageNumbers(current, total, maxVisible) {
        if (total <= maxVisible) {
            var allPages = [];
            for (var i = 1; i <= total; i++) {
                allPages.push(i);
            }
            return allPages;
        }

        var pages = [];
        var start = Math.max(1, current - 1);
        var end = Math.min(total, start + maxVisible - 1);

        if (end - start + 1 < maxVisible) {
            start = Math.max(1, end - maxVisible + 1);
        }

        if (start > 1) {
            pages.push(1);
            if (start > 2) pages.push('...');
        }

        for (var page = start; page <= end; page++) {
            pages.push(page);
        }

        if (end < total) {
            if (end < total - 1) pages.push('...');
            pages.push(total);
        }

        return pages;
    }

    function normalize(value) {
        return (value || '').toString().trim().toLowerCase();
    }

    function formatDate(value) {
        if (!value || value.indexOf('-') < 0) return value || '';
        var parts = value.split('-');
        if (parts.length !== 3) return value;
        return parts[2] + '/' + parts[1] + '/' + parts[0];
    }

    function esc(value) {
        var div = document.createElement('div');
        div.textContent = value == null ? '' : String(value);
        return div.innerHTML;
    }

    function escAttr(value) {
        return esc(value).replace(/"/g, '&quot;');
    }

    courtsContainer.addEventListener('input', handleSearchInput);
    courtsContainer.addEventListener('change', handleStatusChange);
    courtsContainer.addEventListener('click', handlePaginationClick);

    loadRentalStatus();
})();
