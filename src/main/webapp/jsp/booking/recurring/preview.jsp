<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    String ctx = request.getContextPath();
%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>Xem trước lịch cố định - BadmintonPro</title>

    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet" />
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css" />
    <link rel="stylesheet" href="<%= ctx %>/assets/css/badminton-pro.css" />
    <link rel="stylesheet" href="<%= ctx %>/assets/css/booking/recurring/recurring.css" />
</head>
<body class="recurring-page-bg">
<header class="main-header py-4">
    <div class="container recurring-container text-center">
        <h2 class="fw-bold mb-1 text-dark">Đặt Lịch Cố Định</h2>
        <p class="text-muted mb-0">Thiết lập lịch tập luyện định kỳ của bạn chỉ trong vài bước</p>
    </div>
</header>

<main class="container py-5 recurring-container">
    <div class="d-flex justify-content-between align-items-center flex-wrap gap-2 mb-3">
        <a href="<%= ctx %>/jsp/booking/recurring/create.jsp" id="backToCreate" class="btn btn-outline-secondary btn-sm btn-lift">
            <i class="bi bi-chevron-left"></i> Quay lại
        </a>
        <span class="badge text-bg-light border">Recurring</span>
    </div>

    <div class="step-progress">
        <div class="step">
            <div class="step-circle completed"><i class="bi bi-check-lg"></i></div>
            <small class="fw-semibold text-muted mt-2">Lịch trình</small>
        </div>
        <div class="step-line step-line-active"></div>
        <div class="step">
            <div class="step-circle active">2</div>
            <small class="fw-semibold text-success mt-2">Xem trước</small>
        </div>
        <div class="step-line"></div>
        <div class="step">
            <div class="step-circle">3</div>
            <small class="fw-semibold text-muted mt-2">Thanh toán</small>
        </div>
    </div>

    <div id="rpAlert" class="alert alert-danger d-none mb-3"></div>
    <div id="rpInfo" class="alert alert-info d-none mb-3"></div>

    <div class="row g-4">
        <div class="col-lg-8">
            <div class="main-card">
                <div class="d-flex justify-content-between align-items-center mb-4">
                    <h5 class="fw-bold mb-0">Danh sách các buổi tập</h5>
                    <div class="d-flex gap-3 small">
                        <div><span class="status-dot bg-success me-1"></span> Khả dụng</div>
                        <div><span class="status-dot bg-danger me-1"></span> Xung đột</div>
                        <div><span class="status-dot bg-primary me-1"></span> Đã sửa</div>
                    </div>
                </div>

                <table id="sessionTable" class="table table-hover align-middle recurring-table">
                    <thead class="table-light">
                    <tr>
                        <th>NGÀY</th>
                        <th>SÂN</th>
                        <th>GIỜ</th>
                        <th>GIÁ</th>
                        <th>TRẠNG THÁI</th>
                        <th>HÀNH ĐỘNG</th>
                    </tr>
                    </thead>
                    <tbody id="sessionBody"></tbody>
                </table>
            </div>
        </div>

        <div class="col-lg-4">
            <div class="sidebar-card">
                <h5 class="fw-bold mb-4 text-white">Tóm tắt đặt lịch</h5>

                <div class="mb-4">
                    <div class="d-flex justify-content-between mb-2">
                        <span class="text-light">Tổng số buổi:</span>
                        <strong id="statTotal" class="text-white">0</strong>
                    </div>
                    <div class="d-flex justify-content-between mb-2">
                        <span class="text-light">Buổi khả dụng:</span>
                        <strong id="statAvailable" class="text-success">0</strong>
                    </div>
                    <div class="d-flex justify-content-between mb-2">
                        <span class="text-light">Buổi xung đột:</span>
                        <strong id="statConflict" class="text-danger">0</strong>
                    </div>
                    <div class="d-flex justify-content-between mb-2">
                        <span class="text-light">Buổi đã đổi:</span>
                        <strong id="statModified" class="text-primary">0</strong>
                    </div>
                    <div class="d-flex justify-content-between mb-0">
                        <span class="text-light">Tạm tính:</span>
                        <strong id="statTotalAmount" class="text-white">0 VND</strong>
                    </div>
                </div>

                <hr class="border-secondary" />

                <div class="mb-4">
                    <label for="voucherCode" class="form-label text-light fw-semibold mb-2">
                        <i class="bi bi-ticket-perforated me-2"></i> Voucher (tùy chọn)
                    </label>
                    <div class="input-group voucher-input-group">
                        <input id="voucherCode" class="form-control" placeholder="Nhập mã voucher recurring" />
                        <button id="applyVoucherBtn" class="btn btn-outline-success" type="button">Áp dụng</button>
                    </div>
                    <small class="text-white d-block mt-2">Chỉ áp dụng voucher loại RECURRING hoặc BOTH</small>
                    <small id="voucherStatus" class="d-block mt-1 text-muted"></small>
                </div>

                <div class="text-center mt-4">
                    <div class="small text-light mb-1">Dự kiến tổng tiền:</div>
                    <div id="moneyTotal" class="display-6 fw-bold text-success mb-2">0 VND</div>

                    <div class="d-flex justify-content-between align-items-center mb-2 discount-line">
                        <span>Giảm giá voucher:</span>
                        <strong id="moneyDiscount">0 VND</strong>
                    </div>
                    <div class="d-flex justify-content-between align-items-center final-price">
                        <span class="text-white">Thanh toán:</span>
                        <strong id="moneyFinal" class="text-white">0 VND</strong>
                    </div>
                </div>

                <button id="confirmBtn" type="button" class="btn btn-success btn-lg w-100 mt-5 py-3 btn-lift">
                    <span class="confirm-btn-label">Xác nhận và thanh toán</span>
                    <span class="confirm-btn-loading d-none"><span class="spinner-border spinner-border-sm me-2" role="status" aria-hidden="true"></span>Đang xử lý...</span>
                </button>
            </div>
        </div>
    </div>
</main>

<div class="modal fade" id="modifySessionModal" tabindex="-1" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content recurring-modal-shell">
            <div class="modal-header">
                <h5 class="modal-title">Đổi sân / giờ cho buổi xung đột</h5>
                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Đóng"></button>
            </div>
            <div class="modal-body">
                <input type="hidden" id="mdSessionId" />
                <div class="mb-2 small text-muted" id="mdSessionDateText"></div>
                <div class="mb-3">
                    <label for="mdCourtId" class="form-label fw-semibold">Sân mới</label>
                    <select id="mdCourtId" class="form-select"></select>
                </div>
                <div class="row g-2">
                    <div class="col-6">
                        <label class="form-label fw-semibold">Bắt đầu</label>
                        <div class="time-picker-wrapper">
                            <div id="mdStartTimeDisplay" class="time-picker-display" tabindex="0"></div>
                            <input type="hidden" id="mdStartTime" />
                        </div>
                    </div>
                    <div class="col-6">
                        <label class="form-label fw-semibold">Kết thúc</label>
                        <div class="time-picker-wrapper">
                            <div id="mdEndTimeDisplay" class="time-picker-display" tabindex="0"></div>
                            <input type="hidden" id="mdEndTime" />
                        </div>
                    </div>
                </div>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-outline-secondary" data-bs-dismiss="modal">Đóng</button>
                <button type="button" class="btn btn-success btn-lift" id="saveModifyBtn">Lưu thay đổi</button>
            </div>
        </div>
    </div>
</div>

<%@ include file="/jsp/common/time-picker.jsp" %>

<script>
    window.APP_CONTEXT_PATH = '<%= ctx %>';
    document.addEventListener('DOMContentLoaded', function() {
        var btn = document.getElementById('confirmBtn');
        var label = btn.querySelector('.confirm-btn-label');
        var loading = btn.querySelector('.confirm-btn-loading');
        btn.addEventListener('click', function() {
            if (!btn.disabled) {
                btn.disabled = true;
                label.classList.add('d-none');
                loading.classList.remove('d-none');
            }
        });
        // Hàm để bật lại nút và ẩn loading (gọi sau khi xử lý xong, ví dụ sau AJAX)
        window.enableConfirmBtn = function() {
            btn.disabled = false;
            label.classList.remove('d-none');
            loading.classList.add('d-none');
        };
    });
</script>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
<script src="<%= ctx %>/assets/js/booking/recurring/recurring-preview.js"></script>
</body>
</html>


