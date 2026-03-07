<%-- owner-voucher-list.jsp --%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ include file="../layout/layout.jsp" %>
<%@ include file="../layout/sidebar.jsp" %>

<div class="main-content">
    <%@ include file="../layout/header.jsp" %>

    <div class="content-area">
        <%@ include file="../layout/page-header.jsp" %>

        <%-- ===== ALERTS ===== --%>
        <c:if test="${not empty param.success}">
            <div class="alert alert-success alert-dismissible fade show mb-4" role="alert">
                <i class="bi bi-check-circle-fill me-2"></i>
                <c:choose>
                    <c:when test="${param.success eq 'deleted'}">Đã xóa voucher thành công.</c:when>
                    <c:otherwise>Thao tác thành công.</c:otherwise>
                </c:choose>
                <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
            </div>
        </c:if>
        <c:if test="${not empty requestScope.error}">
            <div class="alert alert-danger mb-4"><i class="bi bi-exclamation-triangle-fill me-2"></i>${requestScope.error}</div>
        </c:if>

        <%-- ===== FILTER BAR ===== --%>
        <div class="card mb-4 border-0 rounded-4" style="box-shadow:0 1px 4px rgba(0,0,0,0.06);">
            <div class="card-body p-4">
                <div class="row g-3 align-items-end">
                    <div class="col-12 col-md-4">
                        <label class="form-label fw-medium small text-muted">Tìm kiếm</label>
                        <div class="input-group">
                            <span class="input-group-text bg-white"><i class="bi bi-search text-muted"></i></span>
                            <input type="text" id="searchInput" class="form-control" placeholder="Mã hoặc tên voucher...">
                        </div>
                    </div>
                    <div class="col-6 col-md-2">
                        <label class="form-label fw-medium small text-muted">Trạng thái</label>
                        <select id="filterStatus" class="form-select">
                            <option value="">Tất cả</option>
                            <option value="ACTIVE">Đang hoạt động</option>
                            <option value="UPCOMING">Sắp diễn ra</option>
                            <option value="EXPIRED">Đã hết hạn</option>
                            <option value="DISABLED">Đã tắt</option>
                        </select>
                    </div>
                    <div class="col-6 col-md-2">
                        <label class="form-label fw-medium small text-muted">Loại giảm giá</label>
                        <select id="filterDiscountType" class="form-select">
                            <option value="">Tất cả</option>
                            <option value="PERCENTAGE">Phần trăm (%)</option>
                            <option value="FIXED_AMOUNT">Số tiền cố định</option>
                        </select>
                    </div>
                    <div class="col-6 col-md-2">
                        <label class="form-label fw-medium small text-muted">Từ ngày</label>
                        <input type="date" id="filterDateFrom" class="form-control">
                    </div>
                    <div class="col-6 col-md-2">
                        <label class="form-label fw-medium small text-muted">Đến ngày</label>
                        <input type="date" id="filterDateTo" class="form-control">
                    </div>
                    <div class="col-12 col-md-4">
                        <label class="form-label fw-medium small text-muted">Sân áp dụng</label>
                        <select id="filterFacility" class="form-select">
                            <option value="">Tất cả sân</option>
                            <c:forEach var="f" items="${facilities}">
                                <option value="${f.facilityId}">${f.name}</option>
                            </c:forEach>
                        </select>
                    </div>
                    <div class="col-12 col-md-2 d-flex gap-2">
                        <button class="btn btn-brand flex-fill" onclick="loadVouchers(1)">
                            <i class="bi bi-funnel-fill me-1"></i> Lọc
                        </button>
                        <button class="btn btn-outline-secondary" onclick="resetFilter()">
                            <i class="bi bi-x-lg"></i>
                        </button>
                    </div>
                </div>
            </div>
        </div>

        <%-- ===== TABLE CARD ===== --%>
        <div class="card border-0 rounded-4" style="box-shadow:0 1px 4px rgba(0,0,0,0.06);">
            <div class="card-header bg-white border-0 rounded-top-4 px-4 pt-4 pb-3 d-flex justify-content-between align-items-center">
                <div>
                    <h5 class="fw-bold mb-0">Danh Sách Voucher</h5>
                    <small class="text-muted" id="totalLabel">Đang tải...</small>
                </div>
                <a href="${pageContext.request.contextPath}/owner/vouchers/create"
                   class="btn btn-brand d-flex align-items-center gap-2 fw-semibold rounded-3">
                    <i class="bi bi-plus-lg"></i> Tạo voucher
                </a>
            </div>
            <div class="card-body p-0">
                <div id="tableWrapper">
                    <div class="text-center py-5 text-muted">
                        <div class="spinner-border text-success" role="status"></div>
                        <p class="mt-3">Đang tải dữ liệu...</p>
                    </div>
                </div>
            </div>
            <div class="card-footer bg-white border-0 rounded-bottom-4 px-4 pb-4" id="paginationWrapper"></div>
        </div>

    </div><%-- end .content-area --%>

    <%-- ===== DELETE MODAL – must be inside body, before footer --%>
    <div class="modal fade" id="deleteModal" tabindex="-1">
        <div class="modal-dialog modal-dialog-centered">
            <div class="modal-content rounded-4 border-0">
                <div class="modal-header border-0">
                    <h5 class="modal-title fw-bold">Xác nhận xóa</h5>
                    <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                </div>
                <div class="modal-body">
                    Bạn có chắc muốn xóa voucher <strong id="deleteVoucherCode"></strong>?
                    Voucher sẽ bị vô hiệu hóa và không thể sử dụng nữa.
                </div>
                <div class="modal-footer border-0">
                    <button class="btn btn-outline-secondary rounded-3" data-bs-dismiss="modal">Hủy</button>
                    <button class="btn btn-danger rounded-3" id="confirmDeleteBtn">
                        <i class="bi bi-trash me-1"></i> Xóa
                    </button>
                </div>
            </div>
        </div>
    </div>

    <%-- ===== JAVASCRIPT – must be inside body, before footer --%>
    <script>
    (function () {
        'use strict';

        var CTX  = '${pageContext.request.contextPath}';
        var API  = CTX + '/api/owner/vouchers';

        var currentPage = 1;
        var deleteId    = null;
        var searchTimer = null;

        function statusBadge(status) {
            var map = {
                ACTIVE:   ['success',   'Đang hoạt động'],
                UPCOMING: ['primary',   'Sắp diễn ra'],
                EXPIRED:  ['secondary', 'Đã hết hạn'],
                DISABLED: ['danger',    'Đã tắt']
            };
            var entry = map[status] || ['secondary', status];
            return '<span class="badge bg-' + entry[0] + '-subtle text-' + entry[0] + ' fw-semibold">' + entry[1] + '</span>';
        }

        function discountDisplay(type, value) {
            if (type === 'PERCENTAGE') {
                return '<span class="fw-semibold text-success">' + value + '%</span>';
            }
            return '<span class="fw-semibold text-primary">' + Number(value).toLocaleString('vi-VN') + ' ₫</span>';
        }

        function escapeHtml(str) {
            return String(str).replace(/[&<>"']/g, function(c) {
                return {'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[c];
            });
        }

        function formatDT(dt) {
            if (!dt) return '-';
            return String(dt).slice(0, 16).replace('T', ' ');
        }

        function renderTable(items) {
            if (!items || items.length === 0) {
                return '<div class="text-center py-5 text-muted">'
                     + '<i class="bi bi-inbox fs-1 d-block mb-3"></i>'
                     + '<p>Không tìm thấy voucher nào phù hợp.</p></div>';
            }
            var rows = items.map(function(v) {
                return '<tr>'
                    + '<td class="px-4"><code class="fw-bold text-success">' + escapeHtml(v.code) + '</code></td>'
                    + '<td class="px-4 fw-medium">' + escapeHtml(v.name) + '</td>'
                    + '<td class="px-4">' + (v.discountType === 'PERCENTAGE'
                        ? '<span class="badge bg-success-subtle text-success">%</span>'
                        : '<span class="badge bg-primary-subtle text-primary">VNĐ</span>') + '</td>'
                    + '<td class="px-4">' + discountDisplay(v.discountType, v.discountValue) + '</td>'
                    + '<td class="px-4 text-muted small">' + (v.minOrderAmount ? Number(v.minOrderAmount).toLocaleString('vi-VN') + ' ₫' : '-') + '</td>'
                    + '<td class="px-4 text-muted small">' + (v.maxDiscountAmount ? Number(v.maxDiscountAmount).toLocaleString('vi-VN') + ' ₫' : '-') + '</td>'
                    + '<td class="px-4 text-muted small">' + formatDT(v.validFrom) + '</td>'
                    + '<td class="px-4 text-muted small">' + formatDT(v.validTo) + '</td>'
                    + '<td class="px-4 text-center">' + (v.usageLimit != null ? v.usageLimit : '∞') + '</td>'
                    + '<td class="px-4">' + statusBadge(v.status) + '</td>'
                    + '<td class="px-4 text-end"><div class="btn-group btn-group-sm">'
                    + '<a class="btn btn-outline-secondary" href="' + CTX + '/owner/vouchers/detail?id=' + v.voucherId + '" title="Chi tiết"><i class="bi bi-eye"></i></a>'
                    + '<a class="btn btn-outline-warning" href="' + CTX + '/owner/vouchers/edit?id=' + v.voucherId + '" title="Sửa"><i class="bi bi-pencil"></i></a>'
                    + '<button class="btn btn-outline-danger" onclick="confirmDelete(' + v.voucherId + ',\'' + escapeHtml(v.code) + '\')" title="Xóa"><i class="bi bi-trash"></i></button>'
                    + '</div></td></tr>';
            }).join('');

            return '<div class="table-responsive">'
                + '<table class="table table-hover align-middle mb-0">'
                + '<thead class="table-light"><tr>'
                + '<th class="px-4">Mã voucher</th>'
                + '<th class="px-4">Tên voucher</th>'
                + '<th class="px-4">Loại</th>'
                + '<th class="px-4">Giá trị giảm</th>'
                + '<th class="px-4">Đơn tối thiểu</th>'
                + '<th class="px-4">Giảm tối đa</th>'
                + '<th class="px-4">Bắt đầu</th>'
                + '<th class="px-4">Kết thúc</th>'
                + '<th class="px-4 text-center">Giới hạn</th>'
                + '<th class="px-4">Trạng thái</th>'
                + '<th class="px-4 text-end">Thao tác</th>'
                + '</tr></thead>'
                + '<tbody>' + rows + '</tbody>'
                + '</table></div>';
        }

        function renderPagination(current, total) {
            var wrapper = document.getElementById('paginationWrapper');
            if (total <= 1) { wrapper.innerHTML = ''; return; }
            var html = '<ul class="pagination mb-0 justify-content-center">';
            html += '<li class="page-item ' + (current === 1 ? 'disabled' : '') + '">'
                  + '<a class="page-link" href="#" onclick="loadVouchers(' + (current-1) + ');return false;">«</a></li>';
            var start = Math.max(1, current - 2);
            var end   = Math.min(total, current + 2);
            for (var p = start; p <= end; p++) {
                html += '<li class="page-item ' + (p === current ? 'active' : '') + '">'
                      + '<a class="page-link" href="#" onclick="loadVouchers(' + p + ');return false;">' + p + '</a></li>';
            }
            html += '<li class="page-item ' + (current === total ? 'disabled' : '') + '">'
                  + '<a class="page-link" href="#" onclick="loadVouchers(' + (current+1) + ');return false;">»</a></li>';
            html += '</ul>';
            wrapper.innerHTML = html;
        }

        window.loadVouchers = function(page) {
            currentPage = page || 1;
            var keyword      = document.getElementById('searchInput').value.trim();
            var status       = document.getElementById('filterStatus').value;
            var discountType = document.getElementById('filterDiscountType').value;
            var dateFrom     = document.getElementById('filterDateFrom').value;
            var dateTo       = document.getElementById('filterDateTo').value;
            var facilityId   = document.getElementById('filterFacility').value;

            var params = 'keyword=' + encodeURIComponent(keyword)
                       + '&status=' + encodeURIComponent(status)
                       + '&discountType=' + encodeURIComponent(discountType)
                       + '&dateFrom=' + encodeURIComponent(dateFrom)
                       + '&dateTo=' + encodeURIComponent(dateTo)
                       + '&facilityId=' + encodeURIComponent(facilityId)
                       + '&page=' + currentPage
                       + '&pageSize=10';

            document.getElementById('tableWrapper').innerHTML =
                '<div class="text-center py-5"><div class="spinner-border text-success" role="status"></div></div>';

            fetch(API + '?' + params)
                .then(function(r) {
                    if (!r.ok) throw new Error('HTTP ' + r.status);
                    return r.json();
                })
                .then(function(data) {
                    document.getElementById('totalLabel').textContent = 'Tổng: ' + (data.totalItems || 0) + ' voucher';
                    document.getElementById('tableWrapper').innerHTML = renderTable(data.items);
                    renderPagination(data.currentPage, data.totalPages);
                })
                .catch(function(err) {
                    document.getElementById('tableWrapper').innerHTML =
                        '<div class="alert alert-danger m-4"><i class="bi bi-exclamation-triangle me-2"></i>Lỗi khi tải dữ liệu: ' + err.message + '</div>';
                });
        };

        window.resetFilter = function() {
            ['searchInput','filterStatus','filterDiscountType','filterDateFrom','filterDateTo','filterFacility']
                .forEach(function(id) { document.getElementById(id).value = ''; });
            loadVouchers(1);
        };

        window.confirmDelete = function(id, code) {
            deleteId = id;
            document.getElementById('deleteVoucherCode').textContent = code;
            new bootstrap.Modal(document.getElementById('deleteModal')).show();
        };

        document.getElementById('confirmDeleteBtn').addEventListener('click', function() {
            if (!deleteId) return;
            fetch(API + '/' + deleteId, { method: 'DELETE' })
                .then(function(r) { return r.json(); })
                .then(function() {
                    bootstrap.Modal.getInstance(document.getElementById('deleteModal')).hide();
                    loadVouchers(currentPage);
                })
                .catch(function() { alert('Xóa thất bại!'); });
        });

        document.getElementById('searchInput').addEventListener('input', function() {
            clearTimeout(searchTimer);
            searchTimer = setTimeout(function() { loadVouchers(1); }, 400);
        });

        document.addEventListener('DOMContentLoaded', function() { loadVouchers(1); });
    })();
    </script>

    <%@ include file="../layout/footer.jsp" %>
