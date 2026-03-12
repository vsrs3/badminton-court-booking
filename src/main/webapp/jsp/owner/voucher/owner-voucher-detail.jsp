<%-- owner-voucher-detail.jsp – Chi tiết voucher --%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ include file="../layout/layout.jsp" %>
<%@ include file="../layout/sidebar.jsp" %>

<div class="main-content">
    <%@ include file="../layout/header.jsp" %>

    <div class="content-area">
        <%@ include file="../layout/page-header.jsp" %>

        <%-- SUCCESS ALERT --%>
        <c:if test="${not empty param.success}">
            <div class="alert alert-success alert-dismissible fade show mb-4" role="alert">
                <i class="bi bi-check-circle-fill me-2"></i>
                <c:choose>
                    <c:when test="${param.success eq 'created'}">Tạo voucher thành công!</c:when>
                    <c:when test="${param.success eq 'updated'}">Cập nhật voucher thành công!</c:when>
                    <c:otherwise>Thao tác thành công.</c:otherwise>
                </c:choose>
                <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
            </div>
        </c:if>

        <c:set var="v" value="${voucher}" />

        <div class="row g-4">

            <%-- ===== LEFT: VOUCHER INFO ===== --%>
            <div class="col-12 col-xl-5">

                <%-- Main Info Card --%>
                <div class="card border-0 rounded-4 mb-4" style="box-shadow:0 1px 4px rgba(0,0,0,0.06);">
                    <div class="card-body p-4">
                        <div class="d-flex align-items-start justify-content-between mb-3">
                            <div>
                                <span class="badge bg-success-subtle text-success fw-bold fs-6 mb-1" style="letter-spacing:0.05em;">
                                    <i class="bi bi-ticket-perforated me-1"></i>${v.code}
                                </span>
                                <h5 class="fw-bold mb-1 mt-1">${v.name}</h5>
                                <c:if test="${not empty v.description}">
                                    <p class="text-muted mb-0 small">${v.description}</p>
                                </c:if>
                            </div>
                            <%-- Status badge --%>
                            <span id="statusBadge" class="badge rounded-pill fs-6">
                                ${v.status}
                            </span>
                        </div>
                        <hr class="my-3" style="border-color:var(--color-gray-100);">

                        <%-- Discount info highlight --%>
                        <div class="rounded-3 p-3 mb-3 text-center"
                             style="background:var(--color-green-50);border:1.5px dashed var(--color-green-300);">
                            <c:choose>
                                <c:when test="${v.discountType eq 'PERCENTAGE'}">
                                    <div class="fw-black" style="font-size:2.5rem;color:var(--color-green-700);">
                                        ${v.discountValue}%
                                    </div>
                                    <div class="text-muted small">Giảm theo phần trăm</div>
                                    <c:if test="${not empty v.maxDiscountAmount}">
                                        <div class="badge bg-success-subtle text-success mt-1">
                                            Giảm tối đa: <fmt:formatNumber value="${v.maxDiscountAmount}" type="number"/> ₫
                                        </div>
                                    </c:if>
                                </c:when>
                                <c:otherwise>
                                    <div class="fw-black" style="font-size:2rem;color:var(--color-green-700);">
                                        <fmt:formatNumber value="${v.discountValue}" type="number"/> ₫
                                    </div>
                                    <div class="text-muted small">Giảm số tiền cố định</div>
                                </c:otherwise>
                            </c:choose>
                        </div>

                        <%-- Details rows --%>
                        <table class="table table-sm table-borderless mb-0">
                            <tr>
                                <td class="text-muted small fw-semibold ps-0" style="width:45%;">Đơn tối thiểu</td>
                                <td class="fw-medium">
                                    <c:choose>
                                        <c:when test="${v.minOrderAmount gt 0}">
                                            <fmt:formatNumber value="${v.minOrderAmount}" type="number"/> ₫
                                        </c:when>
                                        <c:otherwise><span class="text-muted">Không giới hạn</span></c:otherwise>
                                    </c:choose>
                                </td>
                            </tr>
                            <tr>
                                <td class="text-muted small fw-semibold ps-0">Bắt đầu</td>
                                <td>
                                    <span class="d-inline-flex align-items-center gap-1 px-2 py-1 rounded-2 fw-semibold small"
                                          style="background:#D1FAE5;color:#065F46;">
                                        <i class="bi bi-calendar-check"></i>
                                        ${v.validFromFormatted}
                                    </span>
                                </td>
                            </tr>
                            <tr>
                                <td class="text-muted small fw-semibold ps-0">Kết thúc</td>
                                <td>
                                    <span class="d-inline-flex align-items-center gap-1 px-2 py-1 rounded-2 fw-semibold small"
                                          style="background:#FEE2E2;color:#991B1B;">
                                        <i class="bi bi-calendar-x"></i>
                                        ${v.validToFormatted}
                                    </span>
                                </td>
                            </tr>
                            <tr>
                                <td class="text-muted small fw-semibold ps-0">Giới hạn dùng</td>
                                <td class="fw-medium">
                                    <c:choose>
                                        <c:when test="${not empty v.usageLimit}">${v.usageLimit} lần</c:when>
                                        <c:otherwise><span class="text-muted">Vô hạn</span></c:otherwise>
                                    </c:choose>
                                </td>
                            </tr>
                            <tr>
                                <td class="text-muted small fw-semibold ps-0">Giới hạn/user</td>
                                <td class="fw-medium">${v.perUserLimit} lần</td>
                            </tr>
                            <tr>
                                <td class="text-muted small fw-semibold ps-0">Loại booking</td>
                                <td>
                                    <c:choose>
                                        <c:when test="${v.applicableBookingType eq 'SINGLE'}">
                                            <span class="badge bg-primary-subtle text-primary">Đặt đơn</span>
                                        </c:when>
                                        <c:when test="${v.applicableBookingType eq 'RECURRING'}">
                                            <span class="badge bg-warning-subtle text-warning">Định kỳ</span>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="badge bg-secondary-subtle text-secondary">Tất cả</span>
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                            </tr>
                            <tr>
                                <td class="text-muted small fw-semibold ps-0">Sân áp dụng</td>
                                <td>
                                    <c:choose>
                                        <c:when test="${empty v.facilityIds}">
                                            <span class="badge bg-success-subtle text-success">Tất cả sân</span>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="badge bg-info-subtle text-info">${fn:length(v.facilityIds)} sân</span>
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                            </tr>
                        </table>
                    </div>

                    <%-- Action footer --%>
                    <div class="card-footer bg-white border-0 rounded-bottom-4 px-4 pb-4 d-flex gap-2">
                        <a href="${pageContext.request.contextPath}/owner/vouchers/edit?id=${v.voucherId}"
                           class="btn btn-brand rounded-3 fw-semibold flex-fill">
                            <i class="bi bi-pencil me-1"></i> Chỉnh sửa
                        </a>
                        <a href="${pageContext.request.contextPath}/owner/vouchers/list"
                           class="btn btn-outline-secondary rounded-3 fw-semibold">
                            <i class="bi bi-arrow-left"></i> Quay lại
                        </a>
                    </div>
                </div>

                <%-- Usage Stats Card --%>
                <div class="card border-0 rounded-4" style="box-shadow:0 1px 4px rgba(0,0,0,0.06);">
                    <div class="card-body p-4">
                        <h6 class="fw-bold mb-3"><i class="bi bi-bar-chart me-2 text-success"></i>Thống kê sử dụng</h6>
                        <div class="row g-3 text-center">
                            <div class="col-6">
                                <div class="rounded-3 p-3" style="background:var(--color-green-50);">
                                    <div class="fw-black fs-4" style="color:var(--color-green-700);">
                                        ${v.usageCount != null ? v.usageCount : 0}
                                    </div>
                                    <div class="text-muted small">Lượt đã dùng</div>
                                </div>
                            </div>
                            <div class="col-6">
                                <div class="rounded-3 p-3" style="background:#EFF6FF;">
                                    <div class="fw-black fs-5" style="color:#1D4ED8;">
                                        <c:choose>
                                            <c:when test="${not empty v.totalDiscountGiven}">
                                                <fmt:formatNumber value="${v.totalDiscountGiven}" type="number"/>₫
                                            </c:when>
                                            <c:otherwise>0₫</c:otherwise>
                                        </c:choose>
                                    </div>
                                    <div class="text-muted small">Tổng tiền giảm</div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <%-- ===== RIGHT: USAGE HISTORY ===== --%>
            <div class="col-12 col-xl-7">
                <div class="card border-0 rounded-4" style="box-shadow:0 1px 4px rgba(0,0,0,0.06);">
                    <div class="card-header bg-white border-0 rounded-top-4 px-4 pt-4 pb-3 d-flex justify-content-between">
                        <h6 class="fw-bold mb-0">
                            <i class="bi bi-clock-history me-2 text-success"></i>Lịch sử sử dụng
                        </h6>
                        <small class="text-muted" id="usageTotalLabel">Đang tải...</small>
                    </div>
                    <div class="card-body p-0" id="usageTableWrapper">
                        <div class="text-center py-4">
                            <div class="spinner-border text-success spinner-border-sm"></div>
                        </div>
                    </div>
                    <div class="card-footer bg-white border-0 rounded-bottom-4 px-4 pb-4" id="usagePaginationWrapper"></div>
                </div>
            </div>

        </div><%-- end .row --%>
    </div><%-- end .content-area --%>

    <script>
    (function () {
        'use strict';

        var VOUCHER_ID = ${v.voucherId};
        var CTX        = '${pageContext.request.contextPath}';
        var USAGE_API  = CTX + '/api/owner/vouchers/' + VOUCHER_ID + '/usage';
        var currentPage = 1;

        // Status badge init
        var statusMap = {
            ACTIVE:   ['success',   'Đang hoạt động'],
            UPCOMING: ['primary',   'Sắp diễn ra'],
            EXPIRED:  ['secondary', 'Đã hết hạn'],
            DISABLED: ['danger',    'Đã tắt']
        };
        var status = '${v.status}';
        var entry  = statusMap[status] || ['secondary', status];
        var badge  = document.getElementById('statusBadge');
        if (badge) {
            badge.className   = 'badge rounded-pill bg-' + entry[0] + '-subtle text-' + entry[0] + ' fs-6';
            badge.textContent = entry[1];
        }

        function escapeHtml(str) {
            if (!str) return '';
            return String(str).replace(/[&<>"']/g, function(c) {
                return {'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[c];
            });
        }

        function formatDT(dt) {
            if (!dt) return '-';
            return String(dt).slice(0, 16).replace('T', ' ');
        }

        function renderUsageTable(items) {
            if (!items || items.length === 0) {
                return '<div class="text-center py-5 text-muted">'
                     + '<i class="bi bi-inbox fs-1 d-block mb-2"></i>'
                     + '<p>Chưa có lịch sử sử dụng.</p></div>';
            }
            var rows = items.map(function(u) {
                return '<tr>'
                    + '<td class="px-3">'
                    + '<div class="fw-medium">' + (u.accountName ? escapeHtml(u.accountName) : '<span class="text-muted">Khách</span>') + '</div>'
                    + '<div class="text-muted small">' + escapeHtml(u.accountEmail || '') + '</div>'
                    + '</td>'
                    + '<td class="px-3"><code>#' + u.bookingId + '</code></td>'
                    + '<td class="px-3"><code>#' + u.invoiceId + '</code></td>'
                    + '<td class="px-3 fw-semibold text-success">' + Number(u.discountAmount).toLocaleString('vi-VN') + ' ₫</td>'
                    + '<td class="px-3 text-muted small">' + formatDT(u.usedAt) + '</td>'
                    + '</tr>';
            }).join('');

            return '<div class="table-responsive">'
                + '<table class="table table-hover align-middle mb-0">'
                + '<thead class="table-light"><tr>'
                + '<th class="px-3">Người dùng</th>'
                + '<th class="px-3">Booking</th>'
                + '<th class="px-3">Invoice</th>'
                + '<th class="px-3">Số tiền giảm</th>'
                + '<th class="px-3">Thời gian dùng</th>'
                + '</tr></thead>'
                + '<tbody>' + rows + '</tbody>'
                + '</table></div>';
        }

        function renderUsagePagination(current, total) {
            var wrapper = document.getElementById('usagePaginationWrapper');
            if (total <= 1) { wrapper.innerHTML = ''; return; }
            var html = '<ul class="pagination mb-0 justify-content-center">';
            html += '<li class="page-item ' + (current === 1 ? 'disabled' : '') + '">'
                  + '<a class="page-link" href="#" onclick="loadUsage(' + (current-1) + ');return false;">«</a></li>';
            for (var p = Math.max(1, current-2); p <= Math.min(total, current+2); p++) {
                html += '<li class="page-item ' + (p === current ? 'active' : '') + '">'
                      + '<a class="page-link" href="#" onclick="loadUsage(' + p + ');return false;">' + p + '</a></li>';
            }
            html += '<li class="page-item ' + (current === total ? 'disabled' : '') + '">'
                  + '<a class="page-link" href="#" onclick="loadUsage(' + (current+1) + ');return false;">»</a></li>';
            html += '</ul>';
            wrapper.innerHTML = html;
        }

        window.loadUsage = function(page) {
            currentPage = page || 1;
            fetch(USAGE_API + '?page=' + currentPage + '&pageSize=10')
                .then(function(r) {
                    if (!r.ok) throw new Error('HTTP ' + r.status);
                    return r.json();
                })
                .then(function(data) {
                    document.getElementById('usageTotalLabel').textContent =
                        'Tổng: ' + (data.totalItems || 0) + ' lần sử dụng';
                    document.getElementById('usageTableWrapper').innerHTML = renderUsageTable(data.items);
                    renderUsagePagination(data.currentPage, data.totalPages);
                })
                .catch(function(err) {
                    document.getElementById('usageTableWrapper').innerHTML =
                        '<div class="alert alert-danger m-3">Lỗi tải lịch sử: ' + err.message + '</div>';
                });
        };

        document.addEventListener('DOMContentLoaded', function() { loadUsage(1); });
    })();
    </script>

    <%@ include file="../layout/footer.jsp" %>
