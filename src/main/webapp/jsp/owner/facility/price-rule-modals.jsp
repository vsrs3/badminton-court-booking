<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<!-- Include Custom Time Picker Component -->
<%@ include file="/jsp/common/time-picker.jsp" %>

<!-- Modal for Create/Edit Price Rule -->
<div class="modal fade" id="priceRuleModal" tabindex="-1">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <h5 class="modal-title" id="priceRuleModalTitle">Thêm khoảng giá</h5>
                <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
            </div>
            <form id="priceRuleForm" method="POST">
                <div class="modal-body">
                    <input type="hidden" name="facilityId" id="modalFacilityId">
                    <input type="hidden" name="courtTypeId" id="modalCourtTypeId">
                    <input type="hidden" name="dayType" id="modalDayType">
                    <input type="hidden" name="priceId" id="modalPriceId">

                    <div class="mb-3">
                        <label for="modalStartTime" class="form-label">Thời gian bắt đầu <span class="text-danger">*</span></label>
                        <div class="time-picker-wrapper">
                            <!-- Display element (user clicks this) -->
                            <div id="startTimeDisplay" class="time-picker-display" tabindex="0">
                                <span class="text-muted">Chọn thời gian</span>
                                <i class="bi bi-clock time-picker-icon"></i>
                            </div>
                            <!-- Hidden input (submitted to backend) -->
                            <input type="hidden" id="modalStartTime" name="startTime" required>
                        </div>
                    </div>

                    <div class="mb-3">
                        <label for="modalEndTime" class="form-label">Thời gian kết thúc <span class="text-danger">*</span></label>
                        <div class="time-picker-wrapper">
                            <!-- Display element (user clicks this) -->
                            <div id="endTimeDisplay" class="time-picker-display" tabindex="0">
                                <span class="text-muted">Chọn thời gian</span>
                                <i class="bi bi-clock time-picker-icon"></i>
                            </div>
                            <!-- Hidden input (submitted to backend) -->
                            <input type="hidden" id="modalEndTime" name="endTime" required>
                        </div>
                    </div>

                    <div class="mb-3">
                        <label for="modalPrice" class="form-label">Giá / giờ (VND) <span class="text-danger">*</span></label>
                        <input type="number" class="form-control" id="modalPrice" name="price"
                               min="0" step="1000" required placeholder="VD: 100000">
                        <div class="form-text">Giá cho 1 giờ chơi</div>
                    </div>

                    <div class="alert alert-info mb-0">
                        <i class="bi bi-info-circle me-2"></i>
                        <small>Không được phép tạo khoảng thời gian chồng chéo với cấu hình hiện có</small>
                    </div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Hủy</button>
                    <button type="submit" class="btn btn-success">
                        <i class="bi bi-check-circle me-1"></i> Lưu
                    </button>
                </div>
            </form>
        </div>
    </div>
</div>

<!-- Modal for Delete Confirmation -->
<div class="modal fade" id="deleteConfirmModal" tabindex="-1">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header bg-danger text-white">
                <h5 class="modal-title">Xác nhận xóa</h5>
                <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal"></button>
            </div>
            <div class="modal-body">
                <p>Bạn có chắc chắn muốn xóa cấu hình giá này?</p>
                <p class="text-muted mb-0">Hành động này không thể hoàn tác.</p>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Hủy</button>
                <button type="button" class="btn btn-danger" onclick="executeDelete()">
                    <i class="bi bi-trash me-1"></i> Xóa
                </button>
            </div>
        </div>
    </div>
</div>

<!-- Initialize Time Pickers -->
<script>
// Initialize time pickers when document is ready
document.addEventListener('DOMContentLoaded', function() {
    initModalTimePickers();
});

function initModalTimePickers() {
    // Destroy existing instances if any
    if (window.startTimePicker) {
        window.startTimePicker.destroy();
    }
    if (window.endTimePicker) {
        window.endTimePicker.destroy();
    }

    // Create new instances and expose to window
    window.startTimePicker = initializeTimePicker('startTimeDisplay', 'modalStartTime', true);
    window.endTimePicker = initializeTimePicker('endTimeDisplay', 'modalEndTime', true);

    console.log('Time pickers initialized:', {
        startTimePicker: !!window.startTimePicker,
        endTimePicker: !!window.endTimePicker
    });
}

// Re-initialize when modal is shown (to ensure fresh state)
document.getElementById('priceRuleModal')?.addEventListener('shown.bs.modal', function() {
    // Time pickers are already initialized, just need to ensure they're ready
    if (!window.startTimePicker || !window.endTimePicker) {
        initModalTimePickers();
    }
});
</script>


