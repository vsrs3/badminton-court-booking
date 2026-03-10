/**
 * pricing.js – Price Configuration UI
 */

/* ---- module state ---- */
var deleteTargetId  = null;
var _smartRowIndex  = 0;
var _smartTimePickers = {};   // { rowIdx: { start: picker, end: picker } }

document.addEventListener('DOMContentLoaded', function () {
    var priceRuleForm = document.getElementById('priceRuleForm');
    if (priceRuleForm) priceRuleForm.addEventListener('submit', handleFormSubmit);
});

/* ===================== CREATE/EDIT MODAL ===================== */

function openCreateModal() {
    if (typeof bootstrap === 'undefined') { alert('Bootstrap chua duoc tai.'); return; }
    var modalElement = document.getElementById('priceRuleModal');
    if (!modalElement) { alert('Modal khong ton tai.'); return; }

    var modal = new bootstrap.Modal(modalElement);
    var form  = document.getElementById('priceRuleForm');
    var ctx   = window.pricingContext;

    document.getElementById('priceRuleModalTitle').textContent = 'Them khoang gia moi';
    form.action = ctx.contextPath + '/owner/prices/create';
    form.reset();
    document.getElementById('modalFacilityId').value  = ctx.facilityId;
    document.getElementById('modalCourtTypeId').value = ctx.courtTypeId;
    document.getElementById('modalDayType').value     = ctx.dayType;
    document.getElementById('modalPriceId').value     = '';
    modal.show();
}

function openEditModal(priceId, startTime, endTime, price) {
    var modal = new bootstrap.Modal(document.getElementById('priceRuleModal'));
    var form  = document.getElementById('priceRuleForm');
    var ctx   = window.pricingContext;

    document.getElementById('priceRuleModalTitle').textContent = 'Chinh sua khoang gia';
    form.action = ctx.contextPath + '/owner/prices/update';
    form.reset();
    document.getElementById('modalFacilityId').value  = ctx.facilityId;
    document.getElementById('modalCourtTypeId').value = ctx.courtTypeId;
    document.getElementById('modalDayType').value     = ctx.dayType;
    document.getElementById('modalPriceId').value     = priceId;

    if (window.startTimePicker && startTime) window.startTimePicker.setValue(startTime);
    if (window.endTimePicker   && endTime)   window.endTimePicker.setValue(endTime);
    document.getElementById('modalPrice').value = price;
    modal.show();
}

function handleFormSubmit(e) {
    var startTime = document.getElementById('modalStartTime').value;
    var endTime   = document.getElementById('modalEndTime').value;
    var price     = document.getElementById('modalPrice').value;
    if (!startTime || !endTime || !price) { e.preventDefault(); alert('Vui long dien day du thong tin'); return false; }
    if (endTime <= startTime)             { e.preventDefault(); alert('Thoi gian ket thuc phai sau bat dau'); return false; }
    if (parseFloat(price) <= 0)           { e.preventDefault(); alert('Gia phai lon hon 0'); return false; }
    showLoading(true);
    return true;
}

/* ===================== DELETE ===================== */

function confirmDelete(priceId) {
    deleteTargetId = priceId;
    new bootstrap.Modal(document.getElementById('deleteConfirmModal')).show();
}

function executeDelete() {
    if (!deleteTargetId) return;
    var ctx  = window.pricingContext;
    var form = document.createElement('form');
    form.method = 'POST';
    form.action = ctx.contextPath + '/owner/prices/delete';
    [['priceId', deleteTargetId], ['facilityId', ctx.facilityId],
     ['courtTypeId', ctx.courtTypeId], ['dayType', ctx.dayType]].forEach(function(pair) {
        var inp = document.createElement('input');
        inp.type = 'hidden'; inp.name = pair[0]; inp.value = pair[1];
        form.appendChild(inp);
    });
    document.body.appendChild(form);
    showLoading(true);
    form.submit();
}

/* ===================== SMART PRICE CONFIG ===================== */

function openSmartPriceModal() {
    // Destroy old picker instances
    Object.keys(_smartTimePickers).forEach(function(k) {
        var p = _smartTimePickers[k];
        if (p.start) p.start.destroy();
        if (p.end)   p.end.destroy();
    });
    _smartTimePickers = {};
    _smartRowIndex    = 0;

    document.getElementById('smartPriceTableBody').innerHTML = '';
    _hideSmartError();
    smartPriceAddRow('', '');
    new bootstrap.Modal(document.getElementById('smartPriceModal')).show();
}

function smartPriceAddRow(startVal, endVal) {
    startVal = startVal || '';
    endVal   = endVal   || '';

    // Auto-fill start = last row's end
    if (!startVal) {
        var rows = document.querySelectorAll('#smartPriceTableBody tr.sp-row');
        if (rows.length > 0) {
            var lastRow = rows[rows.length - 1];
            var lastHidden = lastRow.querySelector('.sp-end-hidden');
            startVal = lastHidden ? lastHidden.value : '';
        }
    }

    var idx = _smartRowIndex++;

    // Build row DOM manually so we can attach pickers after insert
    var tr = document.createElement('tr');
    tr.id        = 'spRow_' + idx;
    tr.className = 'sp-row';
    tr.style.opacity   = '0';
    tr.style.transform = 'translateY(-8px)';
    tr.style.transition = 'opacity 0.25s ease, transform 0.25s ease';

    // ── start-time cell ──────────────────────────────
    var tdStart = document.createElement('td');
    tdStart.className = 'p-1';
    tdStart.innerHTML =
        '<div class="time-picker-wrapper">'
        + '<div id="spStartDisplay_' + idx + '" class="time-picker-display sp-start-display" tabindex="0">'
        +   '<span class="text-muted">Chon gio</span>'
        +   '<i class="bi bi-clock time-picker-icon"></i>'
        + '</div>'
        + '<input type="hidden" id="spStart_' + idx + '" class="sp-start-hidden">'
        + '</div>';

    // ── end-time cell ────────────────────────────────
    var tdEnd = document.createElement('td');
    tdEnd.className = 'p-1';
    tdEnd.innerHTML =
        '<div class="time-picker-wrapper">'
        + '<div id="spEndDisplay_' + idx + '" class="time-picker-display sp-end-display" tabindex="0">'
        +   '<span class="text-muted">Chon gio</span>'
        +   '<i class="bi bi-clock time-picker-icon"></i>'
        + '</div>'
        + '<input type="hidden" id="spEnd_' + idx + '" class="sp-end-hidden">'
        + '</div>';

    // ── price cells ──────────────────────────────────
    var priceFields = [
        { cls: 'sp-nw' },
        { cls: 'sp-ne' },
        { cls: 'sp-vw' },
        { cls: 'sp-ve' }
    ];
    var priceTds = priceFields.map(function(f) {
        var td = document.createElement('td');
        td.className = 'p-1';
        td.innerHTML = '<input type="number" class="form-control form-control-sm ' + f.cls + '"'
            + ' placeholder="Nhap gia..." min="0" step="1000">';
        return td;
    });

    // ── delete cell ──────────────────────────────────
    var tdDel = document.createElement('td');
    tdDel.className = 'p-1 text-center';
    tdDel.innerHTML = '<button type="button" class="btn btn-sm btn-outline-danger sp-del-btn btn-lift"'
        + ' onclick="smartPriceDeleteRow(' + idx + ')" title="Xoa dong"><i class="bi bi-trash"></i></button>';

    tr.appendChild(tdStart);
    tr.appendChild(tdEnd);
    priceTds.forEach(function(td) { tr.appendChild(td); });
    tr.appendChild(tdDel);

    document.getElementById('smartPriceTableBody').appendChild(tr);

    // Animate in
    requestAnimationFrame(function() {
        requestAnimationFrame(function() {
            tr.style.opacity   = '1';
            tr.style.transform = 'translateY(0)';
        });
    });

    // Attach CustomTimePicker instances AFTER the row is in the DOM
    var startPicker = new CustomTimePicker(
        document.getElementById('spStartDisplay_' + idx),
        document.getElementById('spStart_' + idx),
        { required: false }
    );
    var endPicker = new CustomTimePicker(
        document.getElementById('spEndDisplay_' + idx),
        document.getElementById('spEnd_' + idx),
        { required: false }
    );
    _smartTimePickers[idx] = { start: startPicker, end: endPicker };

    // Pre-fill if values given
    if (startVal) startPicker.setValue(startVal);
    if (endVal)   endPicker.setValue(endVal);
}

function smartPriceDeleteRow(idx) {
    var row = document.getElementById('spRow_' + idx);
    if (!row) return;
    // Destroy pickers
    if (_smartTimePickers[idx]) {
        if (_smartTimePickers[idx].start) _smartTimePickers[idx].start.destroy();
        if (_smartTimePickers[idx].end)   _smartTimePickers[idx].end.destroy();
        delete _smartTimePickers[idx];
    }
    row.style.opacity = '0';
    row.style.transition = 'opacity 0.2s ease';
    setTimeout(function() { row.remove(); }, 200);
}

function smartPriceSave() {
    _hideSmartError();
    if (_collectSmartRows() === null) return;
    new bootstrap.Modal(document.getElementById('smartPriceConfirmModal')).show();
}

function smartPriceConfirmedSave() {
    var confirmModal = bootstrap.Modal.getInstance(document.getElementById('smartPriceConfirmModal'));
    if (confirmModal) confirmModal.hide();

    _hideSmartError();
    var rows = _collectSmartRows();
    if (rows === null) return;

    var ctx     = window.pricingContext;
    var payload = JSON.stringify({ facilityId: parseInt(ctx.facilityId), priceConfigs: rows });

    var saveBtn = document.querySelector('.smart-btn-save');
    if (saveBtn) {
        saveBtn.disabled = true;
        saveBtn.innerHTML = '<span class="spinner-border spinner-border-sm me-1"></span>Dang luu...';
    }

    fetch(ctx.contextPath + '/owner/prices/smart-config', {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json;charset=UTF-8' },
        body: payload
    })
    .then(function(res) { return res.json(); })
    .then(function(data) {
        if (saveBtn) {
            saveBtn.disabled = false;
            saveBtn.innerHTML = '<i class="bi bi-check-circle me-1"></i>Luu cau hinh';
        }
        if (data.success) {
            var mainModal = bootstrap.Modal.getInstance(document.getElementById('smartPriceModal'));
            if (mainModal) mainModal.hide();
            window.location.reload();
        } else {
            _showSmartError(data.message || 'Loi khong xac dinh');
        }
    })
    .catch(function(err) {
        if (saveBtn) {
            saveBtn.disabled = false;
            saveBtn.innerHTML = '<i class="bi bi-check-circle me-1"></i>Luu cau hinh';
        }
        _showSmartError('Loi ket noi server: ' + err.message);
    });
}

/** Collect + validate all rows. Returns array or null on validation error. */
function _collectSmartRows() {
    var trs = document.querySelectorAll('#smartPriceTableBody tr.sp-row');
    if (trs.length === 0) { _showSmartError('Vui long them it nhat mot khung gio'); return null; }

    var rows   = [];
    var times  = [];
    var errors = [];

    trs.forEach(function(tr, i) {
        var rowNum = i + 1;

        // Read time from hidden inputs (set by TimePicker)
        var startHidden = tr.querySelector('.sp-start-hidden');
        var endHidden   = tr.querySelector('.sp-end-hidden');
        var start = startHidden ? startHidden.value.trim() : '';
        var end   = endHidden   ? endHidden.value.trim()   : '';

        var nw = tr.querySelector('.sp-nw').value;
        var ne = tr.querySelector('.sp-ne').value;
        var vw = tr.querySelector('.sp-vw').value;
        var ve = tr.querySelector('.sp-ve').value;

        // Clear previous error state
        ['.sp-nw','.sp-ne','.sp-vw','.sp-ve'].forEach(function(sel) {
            tr.querySelector(sel).classList.remove('is-invalid');
        });
        ['.sp-start-display','.sp-end-display'].forEach(function(sel) {
            var el = tr.querySelector(sel);
            if (el) el.classList.remove('is-invalid');
        });
        tr.classList.remove('table-danger');

        var rowErrors = [];

        if (!start) {
            rowErrors.push({ field: '.sp-start-display', msg: 'Gio bat dau' });
        }
        if (!end) {
            rowErrors.push({ field: '.sp-end-display', msg: 'Gio ket thuc' });
        }
        if (start && end && start >= end) {
            rowErrors.push({ field: '.sp-start-display', msg: 'Gio bat dau phai nho hon gio ket thuc' });
        }

        if (nw === '') rowErrors.push({ field: '.sp-nw', msg: 'Gia san thuong trong tuan' });
        else if (parseFloat(nw) < 0) rowErrors.push({ field: '.sp-nw', msg: 'Gia phai >= 0' });

        if (ne === '') rowErrors.push({ field: '.sp-ne', msg: 'Gia san thuong cuoi tuan' });
        else if (parseFloat(ne) < 0) rowErrors.push({ field: '.sp-ne', msg: 'Gia phai >= 0' });

        if (vw === '') rowErrors.push({ field: '.sp-vw', msg: 'Gia san VIP trong tuan' });
        else if (parseFloat(vw) < 0) rowErrors.push({ field: '.sp-vw', msg: 'Gia phai >= 0' });

        if (ve === '') rowErrors.push({ field: '.sp-ve', msg: 'Gia san VIP cuoi tuan' });
        else if (parseFloat(ve) < 0) rowErrors.push({ field: '.sp-ve', msg: 'Gia phai >= 0' });

        if (rowErrors.length > 0) {
            tr.classList.add('table-danger');
            rowErrors.forEach(function(e) {
                var el = tr.querySelector(e.field);
                if (el) el.classList.add('is-invalid');
            });
            errors.push('Dong ' + rowNum + ': ' + rowErrors.map(function(e){ return e.msg; }).join(', '));
        } else {
            times.push({ rowNum: rowNum, start: start, end: end, tr: tr });
            rows.push({
                startTime: start, endTime: end,
                normalWeekdayPrice: parseFloat(nw),
                normalWeekendPrice: parseFloat(ne),
                vipWeekdayPrice:    parseFloat(vw),
                vipWeekendPrice:    parseFloat(ve)
            });
        }
    });

    if (errors.length > 0) { _showSmartError(errors[0]); return null; }

    // Overlap check
    var sorted = times.slice().sort(function(a, b) { return a.start < b.start ? -1 : 1; });
    for (var j = 1; j < sorted.length; j++) {
        var prev = sorted[j - 1];
        var cur  = sorted[j];
        if (cur.start < prev.end) {
            prev.tr.classList.add('table-danger');
            cur.tr.classList.add('table-danger');
            _showSmartError('Khung gio ' + prev.start + '-' + prev.end
                + ' bi trung voi ' + cur.start + '-' + cur.end);
            return null;
        }
    }

    return rows;
}

function _showSmartError(msg) {
    var el    = document.getElementById('smartPriceError');
    var msgEl = document.getElementById('smartPriceErrorMsg');
    if (el && msgEl) {
        msgEl.textContent = msg;
        el.classList.remove('d-none');
        el.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
    }
}
function _hideSmartError() {
    var el = document.getElementById('smartPriceError');
    if (el) el.classList.add('d-none');
}
function _esc(s) {
    return (s || '').replace(/&/g, '&amp;').replace(/"/g, '&quot;').replace(/</g, '&lt;');
}

/* ===================== UTILITIES ===================== */

function showLoading(show) {
    var overlay = document.getElementById('loadingOverlay');
    if (overlay) overlay.classList.toggle('d-none', !show);
}
