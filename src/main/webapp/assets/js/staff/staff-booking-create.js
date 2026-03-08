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
    var phoneHint         = document.getElementById('phoneHint');
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

    // Show content
    createContent.classList.remove('d-none');

    // ─── Render summary ───
    summaryDate.textContent = formatDate(bookingData.date);

    // Group slots into sessions (same court, consecutive)
    var sessions = buildSessions(bookingData.slots);

    var totalPrice = 0;
    sessionsContainer.innerHTML = '';

    sessions.forEach(function (session, idx) {
        var sessionPrice = 0;
        session.forEach(function (s) { sessionPrice += (s.price || 0); });
        totalPrice += sessionPrice;

        var div = document.createElement('div');
        div.className = 'sbc-session';
        div.innerHTML =
            '<div class="sbc-session-idx">' + (idx + 1) + '</div>' +
            '<div class="sbc-session-info">' +
            '  <div class="sbc-session-court">' + escapeHtml(session[0].courtName) + '</div>' +
            '  <div class="sbc-session-meta">' +
            '    <span><i class="bi bi-clock"></i>' + session[0].startTime + ' → ' + session[session.length - 1].endTime + '</span>' +
            '    <span><i class="bi bi-layers"></i>' + session.length + ' slot</span>' +
            '    <span class="sbc-session-price">' + formatMoney(sessionPrice) + '</span>' +
            '  </div>' +
            '</div>';
        sessionsContainer.appendChild(div);
    });

    summaryTotal.textContent = formatMoney(totalPrice);

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

    function switchToAccountMode(matched) {
        customerType = 'ACCOUNT';
        tabAccount.classList.add('active');
        tabGuest.classList.remove('active');
        formAccount.classList.remove('d-none');
        formGuest.classList.add('d-none');

        selectedAccountId.value = matched.accountId;
        selName.textContent = matched.fullName || '�';
        selPhone.textContent = matched.phone || '�';
        selEmail.textContent = matched.email || '�';
        selectedCustomer.classList.remove('d-none');

        hideError();
    }

    function confirmGuestPhoneMatched(matched) {
        var msg = 'Số điện thoại này đã tồn tại tài khoản CUSTOMER:\n' +
            '- ' + (matched.fullName || 'Khong ro ten') + '\n' +
            '- ' + (matched.phone || '') + '\n\n' +
            'Hệ thống sẽ chuyển sang luồng Khách có tài khoản. Tiếp tục?';
        return uiConfirm(msg, 'Trùng số điện thoại');
    }

    // ─── Customer search (ACCOUNT) ───
    customerSearch.addEventListener('input', function () {
        var q = this.value.trim();
        if (q.length < 2) {
            searchDropdown.classList.add('d-none');
            return;
        }

        clearTimeout(searchTimer);
        searchTimer = setTimeout(function () {
            fetch(CTX + '/api/staff/customer/search?q=' + encodeURIComponent(q), {
                credentials: 'same-origin',
                headers: { 'Accept': 'application/json' }
            })
                .then(function (res) { return res.json(); })
                .then(function (body) {
                    if (!body.success) return;
                    renderSearchResults(body.data.customers || []);
                })
                .catch(function (err) {
                    console.error('Search error:', err);
                });
        }, 300);
    });

    function renderSearchResults(customers) {
        searchDropdown.innerHTML = '';
        if (customers.length === 0) {
            searchDropdown.innerHTML = '<div class="sbc-search-empty">Không tìm thấy</div>';
            searchDropdown.classList.remove('d-none');
            return;
        }

        customers.forEach(function (c) {
            var item = document.createElement('div');
            item.className = 'sbc-search-item';
            item.innerHTML =
                '<div class="sbc-search-name">' + escapeHtml(c.fullName) + '</div>' +
                '<div class="sbc-search-detail">' + escapeHtml(c.phone || '') + ' · ' + escapeHtml(c.email || '') + '</div>';
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
        hideError();
    }

    btnRemoveCustomer.addEventListener('click', function () {
        selectedAccountId.value = '';
        selectedCustomer.classList.add('d-none');
    });

    // Close dropdown on click outside
    document.addEventListener('click', function (e) {
        if (!e.target.closest('.sbc-search-wrap')) {
            searchDropdown.classList.add('d-none');
        }
    });

    // ─── Phone validation helper ───
    function isValidPhone(phone) {
        // Vietnamese phone: exactly 10 digits, starts with 0
        var cleaned = phone.replace(/\s+/g, '');
        return /^0\d{9}$/.test(cleaned);
    }

    // ─── Real-time phone validation on input ───
    if (guestPhoneInput) {
        // Only allow digits
        guestPhoneInput.addEventListener('input', function () {
            // Strip non-digit characters
            var raw = this.value.replace(/[^\d]/g, '');
            // Limit to 10 digits
            if (raw.length > 10) {
                raw = raw.substring(0, 10);
            }
            this.value = raw;

            // Show real-time hint
            updatePhoneHint(raw);
        });

        // Also validate on paste
        guestPhoneInput.addEventListener('paste', function () {
            var self = this;
            setTimeout(function () {
                var raw = self.value.replace(/[^\d]/g, '');
                if (raw.length > 10) raw = raw.substring(0, 10);
                self.value = raw;
                updatePhoneHint(raw);
            }, 0);
        });
    }

    function updatePhoneHint(digits) {
        if (!phoneHint) return;

        if (digits.length === 0) {
            phoneHint.classList.add('d-none');
            guestPhoneInput.classList.remove('sbc-input-error');
            guestPhoneInput.classList.remove('sbc-input-valid');
            return;
        }

        phoneHint.classList.remove('d-none');

        if (digits.length < 10) {
            phoneHint.textContent = 'Còn thiếu ' + (10 - digits.length) + ' số';
            phoneHint.className = 'sbc-phone-hint sbc-hint-warn';
            guestPhoneInput.classList.remove('sbc-input-valid');
            guestPhoneInput.classList.add('sbc-input-error');
        } else if (digits.length === 10 && digits.charAt(0) === '0') {
            phoneHint.textContent = '✓ Số điện thoại hợp lệ';
            phoneHint.className = 'sbc-phone-hint sbc-hint-ok';
            guestPhoneInput.classList.remove('sbc-input-error');
            guestPhoneInput.classList.add('sbc-input-valid');
        } else {
            phoneHint.textContent = 'Số điện thoại phải bắt đầu bằng 0';
            phoneHint.className = 'sbc-phone-hint sbc-hint-warn';
            guestPhoneInput.classList.remove('sbc-input-valid');
            guestPhoneInput.classList.add('sbc-input-error');
        }
    }

    // ─── Submit ───
    btnSubmit.addEventListener('click', async function () {
        hideError();

        // Validate customer
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
            if (!isValidPhone(guestPhoneInput.value)) {
                showError('Số điện thoại phải dùng 10 chữ số và bắt đầu bằng 0');
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

        try {
            var res = await fetch(CTX + '/api/staff/booking/create', {
                method: 'POST',
                credentials: 'same-origin',
                headers: {
                    'Content-Type': 'application/json',
                    'Accept': 'application/json'
                },
                body: JSON.stringify(reqBody)
            });
            var body = await res.json();

            if (!body.success) {
                if (body.code === 'GUEST_PHONE_MATCHED_ACCOUNT' && body.data && body.data.accountId) {
                    var confirmed = await confirmGuestPhoneMatched(body.data);
                    if (confirmed) {
                        switchToAccountMode(body.data);
                        resetSubmitButton();
                        btnSubmit.click();
                        return;
                    }
                }

                showError(body.message || 'Đặt sân thất bại');
                resetSubmitButton();
                return;
            }

            // Success -> clear sessionStorage -> redirect to detail
            sessionStorage.removeItem('staffBookingSlots');
            window.location.href = CTX + '/staff/booking/detail/' + body.data.bookingId;
        } catch (err) {
            console.error('Create error:', err);
            showError('Lỗi kết nối. Vui lòng thử lại');
            resetSubmitButton();
        }
    });


    // ─── Helpers ───
    function resetSubmitButton() {
        btnSubmit.disabled = false;
        btnSubmit.innerHTML = '<i class="bi bi-check-circle me-2"></i>Xac nhan dat san';
    }

    function uiConfirm(message, title) {
        if (window.StaffDialog && typeof window.StaffDialog.confirm === 'function') {
            return window.StaffDialog.confirm({ title: title || 'Xac nhan', message: message || '' });
        }
        return Promise.resolve(window.confirm(message || ''));
    }

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

    function formatDate(dateStr) {
        if (!dateStr) return '—';
        var d = new Date(dateStr + 'T00:00:00');
        return String(d.getDate()).padStart(2, '0') + '/' +
            String(d.getMonth() + 1).padStart(2, '0') + '/' + d.getFullYear();
    }

    function buildSessions(slots) {
        // Group by courtId
        var groups = {};
        slots.forEach(function (s) {
            if (!groups[s.courtId]) groups[s.courtId] = [];
            groups[s.courtId].push(s);
        });

        var sessions = [];

        for (var courtId in groups) {
            var courtSlots = groups[courtId];
            courtSlots.sort(function (a, b) {
                return a.startTime.localeCompare(b.startTime);
            });

            var currentSession = [courtSlots[0]];

            for (var i = 1; i < courtSlots.length; i++) {
                var prev = currentSession[currentSession.length - 1];
                if (prev.endTime === courtSlots[i].startTime) {
                    currentSession.push(courtSlots[i]);
                } else {
                    sessions.push(currentSession);
                    currentSession = [courtSlots[i]];
                }
            }
            sessions.push(currentSession);
        }

        // Sort by start time
        sessions.sort(function (a, b) {
            return a[0].startTime.localeCompare(b[0].startTime);
        });

        return sessions;
    }

})();

