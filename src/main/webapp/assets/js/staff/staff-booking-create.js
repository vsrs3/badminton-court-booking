/**
 * staff-booking-create.js — Task 9c: Booking proxy create page
 */
(function () {
    'use strict';

    var CTX = window.ST_CTX || '';

    // DOM
    var stateNoData       = document.getElementById('stateNoData');
    var createContent     = document.getElementById('createContent');
    var summaryDate       = document.getElementById('summaryDate');
    var sessionsContainer = document.getElementById('sessionsContainer');
    var summaryTotal      = document.getElementById('summaryTotal');
    var tabAccount        = document.getElementById('tabAccount');
    var tabGuest          = document.getElementById('tabGuest');
    var formAccount       = document.getElementById('formAccount');
    var formGuest         = document.getElementById('formGuest');
    var customerSearch    = document.getElementById('customerSearch');
    var searchDropdown    = document.getElementById('searchDropdown');
    var selectedCustomer  = document.getElementById('selectedCustomer');
    var selName           = document.getElementById('selName');
    var selPhone          = document.getElementById('selPhone');
    var selEmail          = document.getElementById('selEmail');
    var btnRemoveCustomer = document.getElementById('btnRemoveCustomer');
    var selectedAccountId = document.getElementById('selectedAccountId');
    var guestNameInput    = document.getElementById('guestName');
    var guestPhoneInput   = document.getElementById('guestPhone');
    var formError         = document.getElementById('formError');
    var btnSubmit         = document.getElementById('btnSubmit');

    // State
    var customerType = 'ACCOUNT';
    var bookingData  = null; // from sessionStorage
    var searchTimer  = null;

    // ─── Init: load from sessionStorage ───
    var raw = sessionStorage.getItem('staffBookingSlots');
    if (!raw) {
        stateNoData.classList.remove('d-none');
        return;
    }

    try {
        bookingData = JSON.parse(raw);
        if (!bookingData.slots || bookingData.slots.length === 0) {
            stateNoData.classList.remove('d-none');
            return;
        }
    } catch (e) {
        stateNoData.classList.remove('d-none');
        return;
    }

    createContent.classList.remove('d-none');
    renderSummary();

    // ─── Render slot summary ───
    function renderSummary() {
        // Format date
        var d = new Date(bookingData.date + 'T00:00:00');
        var wd = ['Chủ nhật','Thứ 2','Thứ 3','Thứ 4','Thứ 5','Thứ 6','Thứ 7'];
        var day = String(d.getDate()).padStart(2, '0');
        var mon = String(d.getMonth() + 1).padStart(2, '0');
        summaryDate.textContent = wd[d.getDay()] + ', ' + day + '/' + mon + '/' + d.getFullYear();

        // Group slots into sessions (consecutive slots on same court)
        var sessions = groupSessions(bookingData.slots);

        sessionsContainer.innerHTML = '';
        var total = 0;

        sessions.forEach(function (session, i) {
            var sessionPrice = 0;
            session.slots.forEach(function (s) { sessionPrice += (s.price || 0); });
            total += sessionPrice;

            var div = document.createElement('div');
            div.className = 'sbc-session';
            div.innerHTML =
                '<div class="sbc-session-court">' + session.courtName + '</div>' +
                '<div class="sbc-session-meta">' +
                '<span><i class="bi bi-clock me-1"></i>' + session.startTime + ' → ' + session.endTime + '</span>' +
                '<span>' + session.slots.length + ' slot</span>' +
                '<span class="sbc-session-price">' + formatMoney(sessionPrice) + '</span>' +
                '</div>';
            sessionsContainer.appendChild(div);
        });

        summaryTotal.textContent = formatMoney(total);
    }

    function groupSessions(slots) {
        // Group by courtId
        var groups = {};
        slots.forEach(function (s) {
            var key = s.courtId;
            if (!groups[key]) groups[key] = { courtName: s.courtName, slots: [] };
            groups[key].slots.push(s);
        });

        var sessions = [];
        for (var courtId in groups) {
            var g = groups[courtId];
            // Sort by startTime
            g.slots.sort(function (a, b) { return a.startTime.localeCompare(b.startTime); });

            // Find consecutive sessions
            var current = [g.slots[0]];
            for (var i = 1; i < g.slots.length; i++) {
                var prev = current[current.length - 1];
                if (prev.endTime === g.slots[i].startTime) {
                    current.push(g.slots[i]);
                } else {
                    sessions.push({
                        courtName: g.courtName,
                        startTime: current[0].startTime,
                        endTime: current[current.length - 1].endTime,
                        slots: current
                    });
                    current = [g.slots[i]];
                }
            }
            sessions.push({
                courtName: g.courtName,
                startTime: current[0].startTime,
                endTime: current[current.length - 1].endTime,
                slots: current
            });
        }

        return sessions;
    }

    // ─── Tab switching ───
    tabAccount.addEventListener('click', function () {
        customerType = 'ACCOUNT';
        tabAccount.classList.add('active');
        tabGuest.classList.remove('active');
        formAccount.classList.remove('d-none');
        formGuest.classList.add('d-none');
        hideError();
    });

    tabGuest.addEventListener('click', function () {
        customerType = 'GUEST';
        tabGuest.classList.add('active');
        tabAccount.classList.remove('active');
        formGuest.classList.remove('d-none');
        formAccount.classList.add('d-none');
        hideError();
    });

    // ─── Customer search (debounce) ───
    customerSearch.addEventListener('input', function () {
        clearTimeout(searchTimer);
        var q = this.value.trim();
        if (q.length < 2) {
            searchDropdown.classList.add('d-none');
            return;
        }
        searchTimer = setTimeout(function () {
            searchCustomers(q);
        }, 300);
    });

    // Close dropdown on outside click
    document.addEventListener('click', function (e) {
        if (!e.target.closest('.sbc-search-wrap')) {
            searchDropdown.classList.add('d-none');
        }
    });

    function searchCustomers(q) {
        fetch(CTX + '/api/staff/customer/search?q=' + encodeURIComponent(q), {
            credentials: 'same-origin',
            headers: { 'Accept': 'application/json' }
        })
            .then(function (res) { return res.json(); })
            .then(function (body) {
                if (!body.success) return;
                renderSearchResults(body.data);
            })
            .catch(function (err) {
                console.error('Search error:', err);
            });
    }

    function renderSearchResults(results) {
        searchDropdown.innerHTML = '';

        if (results.length === 0) {
            searchDropdown.innerHTML = '<div class="sbc-search-empty">Không tìm thấy khách hàng</div>';
            searchDropdown.classList.remove('d-none');
            return;
        }

        results.forEach(function (c) {
            var item = document.createElement('div');
            item.className = 'sbc-search-item';
            item.innerHTML =
                '<div class="sbc-search-item-name">' + escapeHtml(c.fullName) + '</div>' +
                '<div class="sbc-search-item-meta">' +
                (c.phone ? c.phone : '') +
                (c.email ? ' · ' + c.email : '') +
                '</div>';
            item.addEventListener('click', function () {
                selectCustomer(c);
            });
            searchDropdown.appendChild(item);
        });

        searchDropdown.classList.remove('d-none');
    }

    function selectCustomer(c) {
        selectedAccountId.value = c.accountId;
        selName.textContent = c.fullName;
        selPhone.textContent = c.phone || '—';
        selEmail.textContent = c.email || '—';
        selectedCustomer.classList.remove('d-none');
        customerSearch.value = '';
        searchDropdown.classList.add('d-none');
        customerSearch.style.display = 'none';
        hideError();
    }

    btnRemoveCustomer.addEventListener('click', function () {
        selectedAccountId.value = '';
        selectedCustomer.classList.add('d-none');
        customerSearch.style.display = '';
        customerSearch.focus();
    });

    // ─── Submit ───
    btnSubmit.addEventListener('click', function () {
        hideError();

        // Validate customer info
        if (customerType === 'ACCOUNT') {
            if (!selectedAccountId.value) {
                showError('Vui lòng tìm và chọn khách hàng');
                return;
            }
        } else {
            if (!guestNameInput.value.trim()) {
                showError('Vui lòng nhập họ tên khách');
                guestNameInput.focus();
                return;
            }
            if (!guestPhoneInput.value.trim()) {
                showError('Vui lòng nhập số điện thoại');
                guestPhoneInput.focus();
                return;
            }
        }

        // Build request body
        var slotsPayload = bookingData.slots.map(function (s) {
            return { courtId: s.courtId, slotId: s.slotId };
        });

        var reqBody = {
            date: bookingData.date,
            customerType: customerType,
            accountId: customerType === 'ACCOUNT' ? parseInt(selectedAccountId.value) : null,
            guestName: customerType === 'GUEST' ? guestNameInput.value.trim() : null,
            guestPhone: customerType === 'GUEST' ? guestPhoneInput.value.trim() : null,
            slots: slotsPayload
        };

        // Disable button
        btnSubmit.disabled = true;
        btnSubmit.innerHTML = '<span class="sbc-spinner"></span>Đang tạo booking...';

        fetch(CTX + '/api/staff/booking/create', {
            method: 'POST',
            credentials: 'same-origin',
            headers: {
                'Content-Type': 'application/json',
                'Accept': 'application/json'
            },
            body: JSON.stringify(reqBody)
        })
            .then(function (res) { return res.json(); })
            .then(function (body) {
                if (!body.success) {
                    showError(body.message || 'Đặt sân thất bại');
                    btnSubmit.disabled = false;
                    btnSubmit.innerHTML = '<i class="bi bi-check-circle me-2"></i>Xác nhận đặt sân';
                    return;
                }

                // Success → clear sessionStorage → redirect to detail
                sessionStorage.removeItem('staffBookingSlots');
                window.location.href = CTX + '/staff/booking/detail/' + body.data.bookingId;
            })
            .catch(function (err) {
                console.error('Create error:', err);
                showError('Lỗi kết nối. Vui lòng thử lại.');
                btnSubmit.disabled = false;
                btnSubmit.innerHTML = '<i class="bi bi-check-circle me-2"></i>Xác nhận đặt sân';
            });
    });

    // ─── Helpers ───
    function showError(msg) {
        formError.textContent = msg;
        formError.classList.remove('d-none');
    }

    function hideError() {
        formError.classList.add('d-none');
    }

    function formatMoney(amount) {
        if (amount == null) return '0đ';
        return Number(amount).toLocaleString('vi-VN') + 'đ';
    }

    function escapeHtml(str) {
        if (!str) return '';
        return str.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/"/g, '&quot;');
    }

})();