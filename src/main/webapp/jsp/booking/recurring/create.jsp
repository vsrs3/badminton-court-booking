<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    String ctx = request.getContextPath();
    String facilityId = request.getParameter("facilityId");
    if (facilityId == null || facilityId.isBlank()) {
        response.sendRedirect(ctx + "/");
        return;
    }
%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>Đặt lịch cố định - BadmintonPro</title>

    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet" />
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css" />
    <link rel="stylesheet" href="<%= ctx %>/assets/css/badminton-pro.css" />
    <link rel="stylesheet" href="<%= ctx %>/assets/css/booking/recurring/recurring.css" />
</head>
<body class="recurring-page-bg">
<header class="main-header py-4">
    <div class="container recurring-container text-center">
        <h2 class="fw-bold mb-1 text-dark">Đặt Lịch Cố Định</h2>
        <p class="text-success fw-semibold mb-1" id="facilityNameText">Cơ sở: --</p>
        <p class="text-muted mb-0">Thiết lập lịch tập luyện định kỳ chỉ trong vài bước</p>
    </div>
</header>

<main class="container py-5 recurring-container">
    <div class="d-flex justify-content-between align-items-center flex-wrap gap-2 mb-3">
        <a href="<%= ctx %>/home" class="btn btn-outline-secondary btn-sm btn-lift">
            <i class="bi bi-chevron-left"></i> Quay lại
        </a>
        <span class="badge text-bg-light border">Recurring</span>
    </div>

    <div class="step-progress">
        <div class="step">
            <div class="step-circle active">1</div>
            <small class="fw-semibold text-success mt-2">Lịch trình</small>
        </div>
        <div class="step-line"></div>
        <div class="step">
            <div class="step-circle">2</div>
            <small class="fw-semibold text-muted mt-2">Xem trước</small>
        </div>
        <div class="step-line"></div>
        <div class="step">
            <div class="step-circle">3</div>
            <small class="fw-semibold text-muted mt-2">Thanh toán</small>
        </div>
    </div>

    <div class="main-card">
        <div id="rcAlert" class="alert alert-danger d-none mb-4"></div>

        <input id="facilityId" type="hidden" value="<%= facilityId %>" />

        <div class="row g-4 mb-4">
            <div class="col-md-6">
                <label for="startDate" class="form-label fw-semibold d-flex align-items-center gap-2">
                    <i class="bi bi-calendar3"></i> Ngày bắt đầu
                </label>
                <div class="recurring-date-btn">
                    <i class="bi bi-calendar3 recurring-date-icon-start"></i>
                    <input type="date" id="startDate" class="form-control date-input" />
                    <i class="bi bi-chevron-down recurring-date-icon-end"></i>
                </div>
            </div>
            <div class="col-md-6">
                <label for="endDate" class="form-label fw-semibold d-flex align-items-center gap-2">
                    <i class="bi bi-calendar3"></i> Ngày kết thúc
                </label>
                <div class="recurring-date-btn">
                    <i class="bi bi-calendar3 recurring-date-icon-start"></i>
                    <input type="date" id="endDate" class="form-control date-input" />
                    <i class="bi bi-chevron-down recurring-date-icon-end"></i>
                </div>
            </div>
        </div>

        <div class="weekly-preview-wrap mb-4">
            <div class="d-flex align-items-center gap-2 mb-2">
                <i class="bi bi-calendar-week text-success"></i>
                <h6 class="fw-bold mb-0">Lịch trình của bạn trong tuần</h6>
            </div>
            <div id="weeklySchedulePreview" class="weekly-preview-grid">
                <div class="weekly-day-card" data-day="2">
                    <div class="weekly-day-title text-center">Thứ 2</div>
                    <div class="weekly-day-content text-muted text-center">Chưa có lịch</div>
                </div>
                <div class="weekly-day-card" data-day="3">
                    <div class="weekly-day-title text-center">Thứ 3</div>
                    <div class="weekly-day-content text-muted text-center">Chưa có lịch</div>
                </div>
                <div class="weekly-day-card" data-day="4">
                    <div class="weekly-day-title text-center">Thứ 4</div>
                    <div class="weekly-day-content text-muted text-center">Chưa có lịch</div>
                </div>
                <div class="weekly-day-card" data-day="5">
                    <div class="weekly-day-title text-center">Thứ 5</div>
                    <div class="weekly-day-content text-muted text-center">Chưa có lịch</div>
                </div>
                <div class="weekly-day-card" data-day="6">
                    <div class="weekly-day-title text-center">Thứ 6</div>
                    <div class="weekly-day-content text-muted text-center">Chưa có lịch</div>
                </div>
                <div class="weekly-day-card" data-day="7">
                    <div class="weekly-day-title text-center text-center">Thứ 7</div>
                    <div class="weekly-day-content text-muted">Chưa có lịch</div>
                </div>
                <div class="weekly-day-card" data-day="1">
                    <div class="weekly-day-title text-center">Chủ nhật</div>
                    <div class="weekly-day-content text-muted text-center">Chưa có lịch</div>
                </div>
            </div>
        </div>

        <div class="d-flex justify-content-between align-items-center mb-3">
            <div>
                <h5 class="fw-bold mb-1"><i class="bi bi-repeat"></i> Lịch trình hàng tuần</h5>
                <small class="text-muted">Mỗi thứ trong tuần chỉ được đặt 1 khung giờ trong recurring booking này</small>
            </div>
            <button id="addPatternBtn" type="button" class="btn btn-outline-success btn-lift px-4 py-2">
                <i class="bi bi-plus-lg me-2"></i> Thêm lịch trình
            </button>
        </div>

        <div id="patternContainer"></div>

        <div class="d-flex justify-content-end mt-4">
            <button id="previewBtn" type="button" class="btn btn-success btn-lg px-5 py-3 btn-lift">
                <span class="preview-btn-label">Tiếp tục xem trước</span>
                <span class="preview-btn-loading d-none"><span class="spinner-border spinner-border-sm me-2" role="status" aria-hidden="true"></span>Đang tải...</span>
                <i class="bi bi-arrow-right ms-2"></i>
            </button>
        </div>
    </div>
</main>

<%@ include file="/jsp/common/time-picker.jsp" %>

<script>
    window.APP_CONTEXT_PATH = '<%= ctx %>';
</script>
<script src="<%= ctx %>/assets/js/booking/recurring/recurring-create.js"></script>
</body>
</html>


