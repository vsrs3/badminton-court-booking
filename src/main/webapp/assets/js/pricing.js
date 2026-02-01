/**
 * pricing.js - Handles Court Price Configuration UI
 */

document.addEventListener('DOMContentLoaded', function () {
    // no init needed for bulk range anymore
});

/* ===================== NAVIGATION & FETCHING ===================== */

/**
 * Switch court type (NORMAL/VIP)
 */
function switchCourtType(id) {
    window.pricingContext.courtTypeId = id;

    document.querySelectorAll('#courtTypeTabs .nav-link').forEach(btn => {
        btn.classList.toggle('active', parseInt(btn.dataset.typeId) === id);
    });

    reloadPriceTable();
}

/**
 * Switch day type (WEEKDAY/WEEKEND)
 */
function switchDayType(type) {
    window.pricingContext.dayType = type;
    reloadPriceTable();
}

/**
 * Reload the price table via AJAX
 */
async function reloadPriceTable() {
    const { contextPath, facilityId, courtTypeId, dayType } = window.pricingContext;
    const url = `${contextPath}/owner/prices?facilityId=${facilityId}&courtTypeId=${courtTypeId}&dayType=${dayType}`;

    showLoading(true);

    try {
        const response = await fetch(url, {
            headers: { 'X-Requested-With': 'XMLHttpRequest' }
        });

        if (!response.ok) throw new Error('Failed to fetch pricing data');

        const html = await response.text();
        const doc = new DOMParser().parseFromString(html, 'text/html');

        document.getElementById('pricingTableContainer').innerHTML =
            doc.getElementById('pricingTableContainer').innerHTML;

        document.getElementById('modalSlotList').innerHTML =
            doc.getElementById('modalSlotList').innerHTML;

        const newUrl = `${contextPath}/owner/prices?facilityId=${facilityId}&courtTypeId=${courtTypeId}&dayType=${dayType}`;
        window.history.pushState({ facilityId, courtTypeId, dayType }, '', newUrl);

    } catch (e) {
        console.error(e);
        showAlert('danger', 'Error reloading pricing table');
    } finally {
        showLoading(false);
    }
}

/* ===================== INLINE EDITING ===================== */

let originalCellContent = null;

function enterEditMode(btn, slotId) {
    if (originalCellContent) cancelEdit();

    const row = btn.closest('tr');
    const priceCell = row.querySelector('.price-cell');
    const actionCell = row.querySelector('td:last-child');
    const currentPrice = row.querySelector('.price-text').dataset.value;

    originalCellContent = {
        row,
        priceCell: priceCell.innerHTML,
        actionCell: actionCell.innerHTML
    };

    priceCell.innerHTML = `
        <div class="input-group input-group-sm" style="max-width:200px">
            <span class="input-group-text">₫</span>
            <input type="number" class="form-control edit-price-input"
                   value="${currentPrice}" min="0" step="1000">
        </div>
    `;

    actionCell.innerHTML = `
        <div class="btn-group btn-group-sm">
            <button class="btn btn-success" onclick="saveSinglePrice(${slotId})">
                <i class="bi bi-check-lg"></i>
            </button>
            <button class="btn btn-secondary" onclick="cancelEdit()">
                <i class="bi bi-x-lg"></i>
            </button>
        </div>
    `;

    priceCell.querySelector('input').focus();
}

function cancelEdit() {
    if (!originalCellContent) return;

    originalCellContent.row.querySelector('.price-cell').innerHTML =
        originalCellContent.priceCell;

    originalCellContent.row.querySelector('td:last-child').innerHTML =
        originalCellContent.actionCell;

    originalCellContent = null;
}

async function saveSinglePrice(slotId) {
    const row = document.querySelector(`tr[data-slot-id="${slotId}"]`);
    const newPrice = row.querySelector('.edit-price-input').value;

    if (!newPrice || newPrice < 0) {
        showAlert('warning', 'Invalid price');
        return;
    }

    const { contextPath, facilityId, courtTypeId, dayType } = window.pricingContext;
    const formData = new URLSearchParams({
        facilityId,
        courtTypeId,
        dayType,
        slotId,
        price: newPrice
    });

    showLoading(true);

    try {
        const res = await fetch(`${contextPath}/owner/prices/update-single`, {
            method: 'POST',
            body: formData,
            headers: { 'X-Requested-With': 'XMLHttpRequest' }
        });

        if (!res.ok) throw new Error();

        const html = await res.text();
        const doc = new DOMParser().parseFromString(html, 'text/html');
        document.getElementById('pricingTableContainer').innerHTML =
            doc.getElementById('pricingTableContainer').innerHTML;

        originalCellContent = null;
        showAlert('success', 'Price updated');

    } catch {
        showAlert('danger', 'Update failed');
    } finally {
        showLoading(false);
    }
}

/* ===================== BULK UPDATE ===================== */

/**
 * Toggle slot range helper
 */
function toggleRange(range) {
    const slots = document.querySelectorAll('#modalSlotList .slot-checkbox');

    const ranges = {
        MORNING: h => h >= 5 && h < 10,
        AFTERNOON: h => h >= 10 && h < 17,
        EVENING: h => h >= 17 && h < 23
    };

    const matched = Array.from(slots).filter(chk =>
        ranges[range](parseInt(chk.dataset.hour))
    );

    if (matched.length === 0) return;

    const allChecked = matched.every(chk => chk.checked);

    matched.forEach(chk => chk.checked = !allChecked);
}

/**
 * Select / clear all slots
 */
function selectAllSlots(checked) {
    document.querySelectorAll('#modalSlotList .slot-checkbox')
        .forEach(chk => chk.checked = checked);
}

/**
 * Submit bulk update
 */
async function submitBulkUpdate() {
    const form = document.getElementById('bulkUpdateForm');
    const price = document.getElementById('bulkPrice').value;
    const selected = form.querySelectorAll('input[name="slotIds"]:checked');

    if (!price || price < 0) {
        alert('Invalid price');
        return;
    }

    if (selected.length === 0) {
        alert('Please select at least one slot');
        return;
    }

    const { contextPath, facilityId, courtTypeId, dayType } = window.pricingContext;
    const formData = new URLSearchParams(new FormData(form));
    formData.append('facilityId', facilityId);
    formData.append('courtTypeId', courtTypeId);
    formData.append('dayType', dayType);

    const btn = document.getElementById('btnApplyBulkUpdate');
    const originalText = btn.innerHTML;
    btn.disabled = true;
    btn.innerHTML = 'Applying...';

    try {
        const res = await fetch(`${contextPath}/owner/prices/bulk-update`, {
            method: 'POST',
            body: formData,
            headers: { 'X-Requested-With': 'XMLHttpRequest' }
        });

        if (!res.ok) throw new Error();

        const html = await res.text();
        const doc = new DOMParser().parseFromString(html, 'text/html');
        document.getElementById('pricingTableContainer').innerHTML =
            doc.getElementById('pricingTableContainer').innerHTML;

        bootstrap.Modal.getInstance(
            document.getElementById('bulkUpdateModal')
        ).hide();

        form.reset();
        showAlert('success', 'Bulk pricing applied');

    } catch {
        showAlert('danger', 'Bulk update failed');
    } finally {
        btn.disabled = false;
        btn.innerHTML = originalText;
    }
}

/* ===================== UI HELPERS ===================== */

function showLoading(show) {
    document.getElementById('loadingOverlay')
        .classList.toggle('d-none', !show);
}

function showAlert(type, message) {
    const container = document.getElementById('alertContainer');
    container.innerHTML = `
        <div class="alert alert-${type} alert-dismissible fade show">
            ${message}
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        </div>
    `;
}