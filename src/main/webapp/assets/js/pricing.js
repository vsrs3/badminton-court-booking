/**
 * pricing.js - Handles Time-based Price Configuration UI
 */

let deleteTargetId = null;

document.addEventListener('DOMContentLoaded', function () {
    // Initialize form submission handler
    const priceRuleForm = document.getElementById('priceRuleForm');
    if (priceRuleForm) {
        priceRuleForm.addEventListener('submit', handleFormSubmit);
    }

    // Log initialization
    console.log('Pricing page initialized');
    console.log('Bootstrap loaded:', typeof bootstrap !== 'undefined');
    console.log('Context:', window.pricingContext);
});

/* ===================== CREATE/EDIT MODAL ===================== */

/**
 * Open modal for creating a new price rule
 */
function openCreateModal() {
    // Check if Bootstrap is loaded
    if (typeof bootstrap === 'undefined') {
        console.error('Bootstrap is not loaded!');
        alert('Lỗi: Bootstrap chưa được tải. Vui lòng refresh trang.');
        return;
    }

    const modalElement = document.getElementById('priceRuleModal');
    if (!modalElement) {
        console.error('Modal element not found!');
        alert('Lỗi: Modal không tồn tại. Vui lòng kiểm tra JSP.');
        return;
    }

    const modal = new bootstrap.Modal(modalElement);
    const form = document.getElementById('priceRuleForm');
    const { contextPath, facilityId, courtTypeId, dayType } = window.pricingContext;

    // Set modal title
    document.getElementById('priceRuleModalTitle').textContent = 'Thêm khoảng giá mới';

    // Set form action
    form.action = `${contextPath}/owner/prices/create`;

    // Reset form
    form.reset();

    // Set hidden fields
    document.getElementById('modalFacilityId').value = facilityId;
    document.getElementById('modalCourtTypeId').value = courtTypeId;
    document.getElementById('modalDayType').value = dayType;
    document.getElementById('modalPriceId').value = '';

    modal.show();
}

/**
 * Open modal for editing an existing price rule
 */
function openEditModal(priceId, startTime, endTime, price) {
    const modal = new bootstrap.Modal(document.getElementById('priceRuleModal'));
    const form = document.getElementById('priceRuleForm');
    const { contextPath, facilityId, courtTypeId, dayType } = window.pricingContext;

    // Set modal title
    document.getElementById('priceRuleModalTitle').textContent = 'Chỉnh sửa khoảng giá';

    // Set form action
    form.action = `${contextPath}/owner/prices/update`;

    // Reset form
    form.reset();

    // Set values
    document.getElementById('modalFacilityId').value = facilityId;
    document.getElementById('modalCourtTypeId').value = courtTypeId;
    document.getElementById('modalDayType').value = dayType;
    document.getElementById('modalPriceId').value = priceId;

    // Set time picker values using custom time picker API
    if (window.startTimePicker && startTime) {
        window.startTimePicker.setValue(startTime);
    }
    if (window.endTimePicker && endTime) {
        window.endTimePicker.setValue(endTime);
    }

    document.getElementById('modalPrice').value = price;

    modal.show();
}

/**
 * Handle form submission - let it redirect naturally
 */
async function handleFormSubmit(e) {
    // Don't prevent default - let the form submit normally
    // The controller will redirect back with success/error message

    // Optional: Add client-side validation
    const startTime = document.getElementById('modalStartTime').value;
    const endTime = document.getElementById('modalEndTime').value;
    const price = document.getElementById('modalPrice').value;

    if (!startTime || !endTime || !price) {
        e.preventDefault();
        alert('Vui lòng điền đầy đủ thông tin');
        return false;
    }

    if (endTime <= startTime) {
        e.preventDefault();
        alert('Thời gian kết thúc phải sau thời gian bắt đầu');
        return false;
    }

    if (parseFloat(price) <= 0) {
        e.preventDefault();
        alert('Giá phải lớn hơn 0');
        return false;
    }

    // Show loading and let form submit naturally
    showLoading(true);
    return true;
}

/* ===================== DELETE ===================== */

/**
 * Show delete confirmation modal
 */
function confirmDelete(priceId) {
    deleteTargetId = priceId;
    const modal = new bootstrap.Modal(document.getElementById('deleteConfirmModal'));
    modal.show();
}

/**
 * Execute delete operation via form submission
 */
function executeDelete() {
    if (!deleteTargetId) return;

    const { contextPath, facilityId, courtTypeId, dayType } = window.pricingContext;

    // Create a hidden form and submit it
    const form = document.createElement('form');
    form.method = 'POST';
    form.action = `${contextPath}/owner/prices/delete`;

    // Add fields
    const fields = {
        priceId: deleteTargetId,
        facilityId: facilityId,
        courtTypeId: courtTypeId,
        dayType: dayType
    };

    for (const [key, value] of Object.entries(fields)) {
        const input = document.createElement('input');
        input.type = 'hidden';
        input.name = key;
        input.value = value;
        form.appendChild(input);
    }

    document.body.appendChild(form);
    showLoading(true);
    form.submit();
}

/* ===================== UTILITIES ===================== */

function showLoading(show) {
    const overlay = document.getElementById('loadingOverlay');
    if (overlay) {
        if (show) {
            overlay.classList.remove('d-none');
        } else {
            overlay.classList.add('d-none');
        }
    } else {
        console.warn('Loading overlay element not found');
    }
}
