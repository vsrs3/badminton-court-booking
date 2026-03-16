<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<%@ include file="layout/staff-layout.jsp"%>
<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/staff/staff-booking-recurring.css">
<%@ include file="layout/staff-sidebar.jsp"%>

<div class="main-content">
    <%@ include file="layout/staff-header.jsp"%>

    <div class="content-area sbr-page">
        <a href="${pageContext.request.contextPath}/staff/timeline" class="sbr-back-link">
            <i class="bi bi-arrow-left"></i> Quay lại Timeline
        </a>

        <div class="sbr-header">
            <h2><i class="bi bi-arrow-repeat"></i>Đặt sân định kỳ</h2>
            <p>Tạo lịch định kỳ theo thứ trong tuần, xem trước và xác nhận thanh toán.</p>
        </div>

        <div class="sbr-progress" id="sbrProgress">
            <div class="sbr-step" data-step="1">
                <div class="sbr-step-index">1</div>
                <div class="sbr-step-text">
                    <div class="sbr-step-title">Khách hàng</div>
                    <div class="sbr-step-sub">Chọn khách</div>
                </div>
            </div>
            <div class="sbr-step" data-step="2">
                <div class="sbr-step-index">2</div>
                <div class="sbr-step-text">
                    <div class="sbr-step-title">Lịch định kỳ</div>
                    <div class="sbr-step-sub">Chọn lịch</div>
                </div>
            </div>
            <div class="sbr-step" data-step="3">
                <div class="sbr-step-index">3</div>
                <div class="sbr-step-text">
                    <div class="sbr-step-title">Xem trước</div>
                    <div class="sbr-step-sub">Kiểm tra trùng</div>
                </div>
            </div>
            <div class="sbr-step" data-step="4">
                <div class="sbr-step-index">4</div>
                <div class="sbr-step-text">
                    <div class="sbr-step-title">Thanh toán</div>
                    <div class="sbr-step-sub">Xác nhận</div>
                </div>
            </div>
        </div>

        <div class="sbr-error sbr-error-floating d-none" id="formError"></div>

        <div class="sbr-wizard">
            <div class="sbr-step-panel is-active" data-step="1">
                <div class="sbr-card sbr-step-card" data-step="1">
                        <div class="sbr-card-title">
                            <span class="sbr-step-pill">Bước 1</span>
                            <i class="bi bi-person"></i>
                            <span>Khách hàng</span>
                        </div>

                        <div class="sbr-customer-grid">
                            <div class="sbr-customer-form">
                                <div class="sbr-tabs">
                                    <button type="button" class="sbr-tab active" id="tabAccount" data-type="ACCOUNT">
                                        Khách có tài khoản
                                    </button>
                                    <button type="button" class="sbr-tab" id="tabGuest" data-type="GUEST">
                                        Khách vãng lai
                                    </button>
                                </div>

                                <div id="formAccount">
                                    <div class="sbr-search-wrap">
                                        <i class="bi bi-search"></i>
                                        <input type="text" id="customerSearch" class="sbr-input" placeholder="Tìm bằng SĐT hoặc email">
                                        <div id="searchDropdown" class="sbr-search-dropdown d-none"></div>
                                    </div>

                                    <div class="sbr-selected d-none" id="selectedCustomer">
                                        <div class="sbr-selected-info">
                                            <div class="sbr-selected-name" id="selName"></div>
                                            <div class="sbr-selected-meta">
                                                <span><i class="bi bi-telephone"></i><span id="selPhone"></span></span>
                                                <span><i class="bi bi-envelope"></i><span id="selEmail"></span></span>
                                            </div>
                                        </div>
                                        <button type="button" class="sbr-btn-icon" id="btnRemoveCustomer">
                                            <i class="bi bi-x-lg"></i>
                                        </button>
                                    </div>
                                    <input type="hidden" id="selectedAccountId" value="">
                                </div>

                                <div id="formGuest" class="d-none">
                                    <div class="sbr-field">
                                        <label>Họ tên <span class="sbr-required">*</span></label>
                                        <input type="text" id="guestName" class="sbr-input" placeholder="Nhập họ tên">
                                    </div>
                                    <div class="sbr-field">
                                        <label>Số điện thoại <span class="sbr-required">*</span></label>
                                        <input type="tel" id="guestPhone" class="sbr-input" placeholder="Nhập SĐT (VD: 0912345678)">
                                    </div>
                                    <div class="sbr-field">
                                        <label>Email (tuỳ chọn)</label>
                                        <input type="email" id="guestEmail" class="sbr-input" placeholder="Nhập email để gửi thông báo">
                                    </div>
                                </div>
                            </div>

                            <div class="sbr-customer-side">
                                <div class="sbr-mini-card">
                                    <div class="sbr-mini-title">
                                        <i class="bi bi-person-badge"></i>
                                        <span>Tóm tắt khách</span>
                                    </div>
                                    <div class="sbr-mini-list">
                                        <div class="sbr-mini-row">
                                            <span class="sbr-label">Loại khách</span>
                                            <strong id="sbrCustomerType">Khách có tài khoản</strong>
                                        </div>
                                        <div class="sbr-mini-row">
                                            <span class="sbr-label">Họ tên</span>
                                            <strong id="sbrCustomerName">—</strong>
                                        </div>
                                        <div class="sbr-mini-row">
                                            <span class="sbr-label">Số điện thoại</span>
                                            <strong id="sbrCustomerPhone">—</strong>
                                        </div>
                                        <div class="sbr-mini-row">
                                            <span class="sbr-label">Email</span>
                                            <strong id="sbrCustomerEmail">—</strong>
                                        </div>
                                    </div>
                                </div>

                                <div class="sbr-mini-card sbr-tip-card">
                                    <div class="sbr-mini-title">
                                        <i class="bi bi-lightning-charge"></i>
                                        <span>Gợi ý nhanh</span>
                                    </div>
                                    <ul class="sbr-tip-list">
                                        <li>Ưu tiên tìm khách theo SĐT để giảm trùng.</li>
                                        <li>Email giúp gửi xác nhận tự động.</li>
                                        <li>Khách vãng lai cần đủ họ tên và SĐT.</li>
                                    </ul>
                                </div>
                            </div>
                        </div>
                    </div>

                <div class="sbr-step-actions">
                    <button type="button" id="btnStep1Next" class="sbr-btn sbr-btn-primary">
                        Tiếp tục
                    </button>
                </div>
            </div>

            <div class="sbr-step-panel" data-step="2">
                <div class="sbr-card sbr-step-card" data-step="2">
                        <div class="sbr-card-title">
                            <span class="sbr-step-pill">Bước 2</span>
                            <i class="bi bi-calendar-range"></i>
                            <span>Lịch định kỳ</span>
                        </div>

                        <div class="sbr-row">
                            <div class="sbr-field">
                                <label>Từ ngày</label>
                                <input type="date" id="startDate" class="sbr-input">
                            </div>
                            <div class="sbr-field">
                                <label>Đến ngày</label>
                                <input type="date" id="endDate" class="sbr-input">
                            </div>
                            <div class="sbr-field">
                                <label>Chính sách trùng lịch</label>
                                <select id="conflictPolicy" class="sbr-input">
                                    <option value="SKIP">Bỏ qua ngày trùng</option>
                                    <option value="SUGGEST">Gợi ý và chọn thủ công</option>
                                </select>
                            </div>
                        </div>

                        <div class="sbr-patterns">
                            <div class="sbr-patterns-head">
                                <span>Lịch theo thứ</span>
                                <button type="button" id="btnAddPattern" class="sbr-btn sbr-btn-light">
                                    <i class="bi bi-plus-circle"></i>Thêm lịch
                                </button>
                            </div>
                            <div id="patternsContainer" class="sbr-patterns-body"></div>
                        </div>

                        <div class="sbr-note">
                            Mỗi ngày chỉ được 1 phiên, tối thiểu 2 slot liên tiếp trên cùng sân.
                        </div>

                    </div>

                <div class="sbr-card sbr-step-card" data-step="2">
                        <div class="sbr-card-title">
                            <span class="sbr-step-pill">Bước 2</span>
                            <i class="bi bi-calendar-week"></i>
                            <span>Lịch theo tuần</span>
                        </div>
                        <div id="weeklyView" class="sbr-weekly"></div>
                    </div>

                <div class="sbr-step-actions">
                    <button type="button" id="btnStep2Back" class="sbr-btn sbr-btn-light">
                        Quay lại
                    </button>
                    <button type="button" id="btnStep2Next" class="sbr-btn sbr-btn-primary">
                        Tiếp tục
                    </button>
                </div>
            </div>

            <div class="sbr-step-panel" data-step="3">
                <div class="sbr-card sbr-step-card" data-step="3">
                    <div class="sbr-card-title">
                        <span class="sbr-step-pill">Bước 3</span>
                        <i class="bi bi-receipt"></i>
                        <span>Xem trước & xử lý trùng</span>
                    </div>
                    <div class="sbr-preview-actions">
                        <button type="button" id="btnPreview" class="sbr-btn">
                            <i class="bi bi-eye"></i>Xem trước
                        </button>
                        <span class="sbr-note">Kiểm tra danh sách và xử lý trùng trước khi tiếp tục.</span>
                    </div>
                </div>

                <div class="sbr-preview d-none" id="previewSection">
                    <div class="sbr-card sbr-step-card" data-step="3">
                        <div class="sbr-card-title">
                            <span class="sbr-step-pill">Bước 3</span>
                            <i class="bi bi-receipt"></i>
                            <span>Xem trước & xử lý trùng</span>
                        </div>
                        <div class="sbr-preview-summary">
                            <div>
                                <span class="sbr-label">Tổng tiền dự kiến</span>
                                <strong id="previewTotal">0đ</strong>
                            </div>
                            <div>
                                <span class="sbr-label">Chính sách</span>
                                <strong id="previewPolicy">SKIP</strong>
                            </div>
                        </div>
                        <div id="previewList" class="sbr-preview-list"></div>
                    </div>
                </div>

                <div class="sbr-step-actions">
                    <button type="button" id="btnStep3Back" class="sbr-btn sbr-btn-light">
                        Quay lại
                    </button>
                    <button type="button" id="btnStep3Next" class="sbr-btn sbr-btn-primary">
                        Tiếp tục
                    </button>
                </div>
            </div>

            <div class="sbr-step-panel" data-step="4">
                <div class="sbr-card sbr-step-card" data-step="4">
                    <div class="sbr-card-title">
                        <span class="sbr-step-pill">Bước 4</span>
                        <i class="bi bi-clipboard-check"></i>
                        <span>Tổng quan & thanh toán</span>
                    </div>

                    <div class="sbr-summary">
                        <div class="sbr-summary-item">
                            <span class="sbr-label">Khách hàng</span>
                            <strong id="sbrSummaryCustomer">Chưa chọn</strong>
                        </div>
                        <div class="sbr-summary-item">
                            <span class="sbr-label">Thời gian</span>
                            <strong id="sbrSummaryDate">Chưa chọn</strong>
                        </div>
                        <div class="sbr-summary-item">
                            <span class="sbr-label">Số lịch</span>
                            <strong id="sbrSummaryPatterns">0</strong>
                        </div>
                        <div class="sbr-summary-item">
                            <span class="sbr-label">Chính sách</span>
                            <strong id="sbrSummaryPolicy">—</strong>
                        </div>
                        <div class="sbr-summary-item">
                            <span class="sbr-label">Tổng tiền</span>
                            <strong id="sbrSummaryTotal">—</strong>
                        </div>
                    </div>

                    <div class="sbr-field">
                        <label>Phương thức thanh toán</label>
                        <select id="paymentMethod" class="sbr-input">
                            <option value="CASH">Tiền mặt</option>
                            <option value="BANK_TRANSFER">Chuyển khoản</option>
                            <option value="VNPAY">VNPay</option>
                        </select>
                    </div>
                </div>

                <div class="sbr-step-actions">
                    <button type="button" id="btnStep4Back" class="sbr-btn sbr-btn-light">
                        Quay lại
                    </button>
                    <button type="button" id="btnConfirm" class="sbr-btn sbr-btn-primary" disabled>
                        <i class="bi bi-check-circle"></i>Xác nhận thanh toán
                    </button>
                </div>
            </div>
        </div>
    </div>
</div>

<footer class="staff-footer">
    <p class="mb-0">&copy; 2026 Badminton Court Booking System. All rights reserved.</p>
</footer>

<script>
    window.ST_CTX = '${pageContext.request.contextPath}';
    window.ST_FACILITY_ID = '${sessionScope.facilityId}';
</script>
<script src="${pageContext.request.contextPath}/assets/js/staff/staff-dialog.js"></script>
<script src="${pageContext.request.contextPath}/assets/js/staff/staff-booking-recurring.js"></script>

</body>
</html>

