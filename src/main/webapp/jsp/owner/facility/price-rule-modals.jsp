<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<!-- Include Custom Time Picker Component -->
<%@ include file="/jsp/common/time-picker.jsp" %>

<!-- Modal for Create/Edit Price Rule -->
<div class="modal fade" id="priceRuleModal" tabindex="-1">
    <%-- ...existing content unchanged... --%>
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

<!-- ================================================================
     SMART PRICE CONFIG MODAL
     ================================================================ -->
<div class="modal fade" id="smartPriceModal" tabindex="-1" aria-labelledby="smartPriceModalLabel" aria-hidden="true">
    <div class="modal-dialog modal-xl modal-dialog-centered">
        <div class="modal-content">
            <div class="modal-header">
                <div>
                    <h5 class="modal-title mb-0" id="smartPriceModalLabel">
                        <i class="bi bi-table me-2 text-success"></i>Cấu hình khoảng giá theo khung giờ
                    </h5>
                    <small class="text-muted">Mỗi dòng cấu hình giá cho tất cả loại sân và loại ngày</small>
                </div>
                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Đóng"></button>
            </div>

            <div class="modal-body p-0" style="overflow:visible;">
                <%-- Validation error banner --%>
                <div id="smartPriceError" class="alert alert-danger mx-3 mt-3 mb-0 d-none">
                    <i class="bi bi-exclamation-triangle-fill me-2"></i>
                    <span id="smartPriceErrorMsg"></span>
                </div>

                <%-- Config table --%>
                <div class="table-responsive" style="overflow:visible;">
                    <table class="table table-bordered align-middle mb-0" id="smartPriceTable"
                           style="overflow:visible;"
                    >
                        <thead class="table-light">
                            <tr>
                                <th class="text-center" style="min-width:150px;">Giờ bắt đầu</th>
                                <th class="text-center" style="min-width:150px;">Giờ kết thúc</th>
                                <th class="text-center text-nowrap" style="min-width:140px;">
                                    <span class="badge bg-primary me-1">NORMAL</span>Trong tuần
                                    <div class="text-muted fw-normal small">(VND/giờ)</div>
                                </th>
                                <th class="text-center text-nowrap" style="min-width:140px;">
                                    <span class="badge bg-primary me-1">NORMAL</span>Cuối tuần
                                    <div class="text-muted fw-normal small">(VND/giờ)</div>
                                </th>
                                <th class="text-center text-nowrap" style="min-width:140px;">
                                    <span class="badge bg-warning text-dark me-1">VIP</span>Trong tuần
                                    <div class="text-muted fw-normal small">(VND/giờ)</div>
                                </th>
                                <th class="text-center text-nowrap" style="min-width:140px;">
                                    <span class="badge bg-warning text-dark me-1">VIP</span>Cuối tuần
                                    <div class="text-muted fw-normal small">(VND/giờ)</div>
                                </th>
                                <th class="text-center" style="width:46px;"></th>
                            </tr>
                        </thead>
                        <tbody id="smartPriceTableBody">
                            <%-- rows injected by JS --%>
                        </tbody>
                    </table>
                </div>

                <%-- Add row button --%>
                <div class="p-3 pt-2 border-top">
                    <button type="button" class="btn btn-sm btn-outline-success smart-btn-add btn-lift"
                            onclick="smartPriceAddRow()">
                        <i class="bi bi-plus-circle me-1"></i>Thêm khung giờ
                    </button>
                </div>
            </div>

            <div class="modal-footer justify-content-between">
                <%-- Warning: overwrite notice in amber --%>
                <div class="d-flex align-items-center gap-2 px-2 py-1 rounded"
                     style="background:rgba(255,193,7,.15);border:1px solid rgba(255,193,7,.4);">
                    <i class="bi bi-exclamation-triangle-fill text-warning"></i>
                    <span class="small fw-semibold text-warning-emphasis">
                        Lưu sẽ <strong>ghi đè toàn bộ</strong> cấu hình giá hiện tại của địa điểm này.
                    </span>
                </div>
                <div class="d-flex gap-2">
                    <button type="button" class="btn btn-secondary btn-lift" data-bs-dismiss="modal">Hủy</button>
                    <button type="button" class="btn btn-accent btn-lift smart-btn-save" onclick="smartPriceSave()">
                        <i class="bi bi-check-circle me-1"></i>Lưu cấu hình
                    </button>
                </div>
            </div>
        </div>
    </div>
</div>

<!-- SMART CONFIG – Save Confirm Modal -->
<div class="modal fade" id="smartPriceConfirmModal" tabindex="-1" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered" style="max-width:440px;">
        <div class="modal-content border-0 shadow">
            <div class="modal-header border-0 pb-1"
                 style="background:rgba(255,193,7,.12);border-bottom:1px solid rgba(255,193,7,.3) !important;">
                <h5 class="modal-title d-flex align-items-center gap-2">
                    <i class="bi bi-exclamation-triangle-fill text-warning fs-4"></i>
                    Xác nhận lưu cấu hình
                </h5>
                <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
            </div>
            <div class="modal-body pt-2">
                <p class="mb-2">Lưu cấu hình sẽ <strong>ghi đè toàn bộ</strong> cấu hình giá hiện tại của địa điểm này.</p>
                <div class="alert alert-warning py-2 mb-0 small">
                    <i class="bi bi-info-circle me-1"></i>
                    Hành động này không thể hoàn tác. Bạn có chắc chắn muốn tiếp tục?
                </div>
            </div>
            <div class="modal-footer border-0 pt-0 gap-2">
                <button type="button" class="btn btn-secondary btn-lift" data-bs-dismiss="modal">Hủy</button>
                <button type="button" class="btn btn-accent btn-lift" onclick="smartPriceConfirmedSave()">
                    <i class="bi bi-check-circle me-1"></i>Tiếp tục lưu
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