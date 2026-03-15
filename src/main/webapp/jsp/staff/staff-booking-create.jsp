<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<%@ include file="layout/staff-layout.jsp"%>
<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/staff/staff-booking-create.css">
<%@ include file="layout/staff-sidebar.jsp"%>

<div class="main-content">
    <%@ include file="layout/staff-header.jsp"%>

    <div class="content-area">

        <%-- Back link --%>
        <a href="${pageContext.request.contextPath}/staff/timeline" class="sbc-back-link" id="backLink">
            <i class="bi bi-arrow-left"></i> Quay lại chọn slot
        </a>

        <%-- No data state --%>
        <div class="sbc-state d-none" id="stateNoData">
            <i class="bi bi-exclamation-triangle" style="font-size:2.5rem; color:#EF4444;"></i>
            <p>Không có dữ liệu slot. Vui lòng quay lại chọn slot trước.</p>
            <a href="${pageContext.request.contextPath}/staff/timeline" class="btn btn-sm rounded-3"
               style="background:var(--primary-color);color:white;">
                <i class="bi bi-arrow-left me-1"></i>Quay lại Timeline
            </a>
        </div>

        <%-- Main content: 2 columns --%>
        <div class="row g-4 d-none" id="createContent">

            <%-- LEFT: Slot summary + Rental summary --%>
            <div class="col-lg-5">

                <%-- Card: Booking summary --%>
                <div class="card sbc-card">
                    <div class="card-header sbc-card-header">
                        <i class="bi bi-receipt me-2"></i>Tóm tắt đặt sân
                    </div>
                    <div class="card-body">
                        <div class="sbc-field mb-3">
                            <span class="sbc-field-label">Ngày</span>
                            <span class="sbc-field-value fw-bold" id="summaryDate"></span>
                        </div>

                        <div id="sessionsContainer">
                            <%-- JS renders session summaries here --%>
                        </div>

                        <div class="sbc-total-row">
                            <span>TỔNG CỘNG</span>
                            <span class="sbc-total-price" id="summaryTotal"></span>
                        </div>
                    </div>
                </div>

                <%-- Card: Rental groups --%>
                <div class="card sbc-card mt-4">
                    <div class="card-header sbc-card-header">
                        <i class="bi bi-bag-check me-2"></i>Đồ thuê
                    </div>
                    <div class="card-body" id="rentalGroupsContainer">
                        <%-- JS render các court + slot group ở đây --%>
                    </div>
                </div>

                <%-- Card: Rental fee summary --%>
                <div class="card sbc-card mt-4">
                    <div class="card-header sbc-card-header">
                        <i class="bi bi-cash-stack me-2"></i>Tiền thuê đồ
                    </div>
                    <div class="card-body">
                        <div id="rentalFeeSummary">
                            <%-- JS render danh sách tiền thuê đồ ở đây --%>
                        </div>
                        <div class="mt-3 fw-bold text-end">
                            Tổng cộng: <span id="rentalGrandTotal">0đ</span>
                        </div>
                    </div>
                </div>

            </div>

            <%-- RIGHT: Customer form --%>
            <div class="col-lg-7">
                <div class="card sbc-card">
                    <div class="card-header sbc-card-header">
                        <i class="bi bi-person me-2"></i>Thông tin khách hàng
                    </div>
                    <div class="card-body">

                        <%-- Customer type tabs --%>
                        <div class="sbc-tabs mb-3">
                            <button type="button" class="sbc-tab active" id="tabAccount" data-type="ACCOUNT">
                                <i class="bi bi-person-check me-1"></i>Khách có tài khoản
                            </button>
                            <button type="button" class="sbc-tab" id="tabGuest" data-type="GUEST">
                                <i class="bi bi-person-plus me-1"></i>Khách vãng lai
                            </button>
                        </div>

                        <%-- ACCOUNT form --%>
                        <div id="formAccount">
                            <div class="sbc-search-wrap mb-3">
                                <i class="bi bi-search"></i>
                                <input type="text"
                                       class="sbc-search-input"
                                       id="customerSearch"
                                       placeholder="Tìm bằng SĐT hoặc email..."
                                       autocomplete="off">
                                <div class="sbc-search-dropdown d-none" id="searchDropdown">
                                    <%-- JS renders search results here --%>
                                </div>
                            </div>

                            <%-- Selected customer display --%>
                            <div class="sbc-selected-customer d-none" id="selectedCustomer">
                                <div class="sbc-selected-info">
                                    <div class="sbc-selected-name" id="selName"></div>
                                    <div class="sbc-selected-meta">
                                        <span>
                                            <i class="bi bi-telephone me-1"></i>
                                            <span id="selPhone"></span>
                                        </span>
                                        <span>
                                            <i class="bi bi-envelope me-1"></i>
                                            <span id="selEmail"></span>
                                        </span>
                                    </div>
                                </div>
                                <button type="button" class="sbc-btn-remove" id="btnRemoveCustomer">
                                    <i class="bi bi-x-lg"></i>
                                </button>
                            </div>

                            <input type="hidden" id="selectedAccountId" value="">
                        </div>

                        <%-- GUEST form --%>
                        <div id="formGuest" class="d-none">
                            <div class="mb-3">
                                <label class="sbc-label">Họ tên <span class="text-danger">*</span></label>
                                <input type="text" class="sbc-input" id="guestName" placeholder="Nhập họ tên">
                            </div>

                            <div class="mb-3">
                                <label class="sbc-label">Số điện thoại <span class="text-danger">*</span></label>
                                <input type="tel"
                                       class="sbc-input"
                                       id="guestPhone"
                                       placeholder="Nhập SĐT (VD: 0912345678)"
                                       maxlength="10"
                                       pattern="0[0-9]{9}"
                                       inputmode="numeric">
                                <div class="sbc-phone-hint d-none" id="phoneHint"></div>
                            </div>

                            <div class="mb-3">
                                <label class="sbc-label">Email <span style="color:#9CA3AF;">(tùy chọn)</span></label>
                                <input type="email" class="sbc-input" id="guestEmail" placeholder="Nhập email">
                            </div>
                        </div>

                        <%-- Error message --%>
                        <div class="sbc-error d-none" id="formError"></div>

                        <%-- Submit --%>
                        <button type="button" class="sbc-btn-submit" id="btnSubmit">
                            <i class="bi bi-check-circle me-2"></i>Xác nhận đặt sân
                        </button>

                    </div>
                </div>
            </div>

        </div>

    </div>
</div>

<%-- ═══════════════════════════════════════════════════════════════ --%>
<%-- Modal: Chọn đồ thuê --%>
<%-- ═══════════════════════════════════════════════════════════════ --%>
<div class="modal fade" id="rentalInventoryModal" tabindex="-1" aria-labelledby="rentalInventoryModalLabel" aria-hidden="true">
    <div class="modal-dialog modal-xl modal-dialog-scrollable">
        <div class="modal-content">

            <div class="modal-header">
                <h5 class="modal-title" id="rentalInventoryModalLabel">
                    <i class="bi bi-bag-check me-2"></i>Chọn đồ thuê
                </h5>
                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
            </div>

            <div class="modal-body">

                <%-- Rental context info --%>
                <div class="mb-3">
                    <div class="small text-muted">Sân / Khung giờ đang chọn</div>
                    <div class="fw-bold" id="rentalModalContext">
                        <%-- JS render thông tin sân + slot ở đây --%>
                    </div>
                </div>

                <%-- Search box --%>
                <div class="mb-3 d-flex gap-2 flex-wrap">
                    <input type="text"
                           class="form-control"
                           id="rentalSearchInput"
                           placeholder="Tìm theo tên, hãng, mô tả...">
                    <button class="btn btn-primary" type="button" id="btnRentalSearch">
                        <i class="bi bi-search me-1"></i>Tìm
                    </button>
                </div>

                <%-- Inventory table --%>
                <div class="table-responsive">
                    <table class="table table-bordered align-middle">
                        <thead class="table-light">
                        <tr>
                            <th style="width:70px;">STT</th>
                            <th>Tên đồ</th>
                            <th>Hãng</th>
                            <th>Mô tả</th>
                            <th style="width:140px;">Giá thuê</th>
                            <th style="width:130px;">SL khả dụng</th>
                            <th style="width:240px;">Thao tác</th>
                        </tr>
                        </thead>
                        <tbody id="rentalInventoryTableBody">
                        <%-- JS render danh sách đồ thuê ở đây --%>
                        </tbody>
                    </table>
                </div>

                <%-- Empty table state --%>
                <div class="text-center text-muted py-3 d-none" id="rentalInventoryEmpty">
                    Không tìm thấy đồ thuê phù hợp.
                </div>

                <%-- Pagination --%>
                <div class="d-flex justify-content-between align-items-center flex-wrap gap-2 mt-3">
                    <div id="rentalPaginationInfo" class="small text-muted">
                        <%-- VD: Trang 1 / 3 --%>
                    </div>
                    <div class="btn-group">
                        <button class="btn btn-outline-secondary btn-sm" id="btnRentalPrev" type="button">
                            <i class="bi bi-chevron-left me-1"></i>Trước
                        </button>
                        <button class="btn btn-outline-secondary btn-sm" id="btnRentalNext" type="button">
                            Sau<i class="bi bi-chevron-right ms-1"></i>
                        </button>
                    </div>
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
</script>
<script src="${pageContext.request.contextPath}/assets/js/staff/staff-dialog.js?v=<%= System.currentTimeMillis() %>"></script>
<script src="${pageContext.request.contextPath}/assets/js/staff/staff-booking-create.js?v=<%= System.currentTimeMillis() %>"></script>

</body>
</html>
