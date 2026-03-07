<%-- owner-voucher-form.jsp – Create & Edit Voucher --%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ include file="../layout/layout.jsp" %>
<%@ include file="../layout/sidebar.jsp" %>

<%-- MultiSelect CSS --%>
<link href="https://codeshack.io/web/demos/MultiSelect.css" rel="stylesheet">

<div class="main-content">
    <%@ include file="../layout/header.jsp" %>

    <div class="content-area">
        <%@ include file="../layout/page-header.jsp" %>

        <%-- ERROR --%>
        <c:if test="${not empty requestScope.formError}">
            <div class="alert alert-danger alert-dismissible fade show mb-4" role="alert">
                <i class="bi bi-exclamation-triangle-fill me-2"></i>${requestScope.formError}
                <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
            </div>
        </c:if>

        <c:set var="isEdit" value="${mode eq 'edit'}" />
        <c:set var="actionUrl" value="${pageContext.request.contextPath}/owner/vouchers/${isEdit ? 'update' : 'create'}" />

        <form method="POST" action="${actionUrl}" id="voucherForm" novalidate>
            <c:if test="${isEdit}">
                <input type="hidden" name="voucherId" value="${voucher.voucherId}">
            </c:if>

            <div class="row g-4">

                <%-- ===== LEFT COLUMN ===== --%>
                <div class="col-12 col-xl-8">

                    <%-- Basic Info Card --%>
                    <div class="card border-0 rounded-4 mb-4" style="box-shadow:0 1px 4px rgba(0,0,0,0.06);">
                        <div class="card-header bg-white border-0 rounded-top-4 px-4 pt-4 pb-0">
                            <h6 class="fw-bold mb-0"><i class="bi bi-info-circle me-2 text-success"></i>Thông tin cơ bản</h6>
                        </div>
                        <div class="card-body px-4 pb-4 pt-3">
                            <div class="row g-3">
                                <div class="col-12 col-md-6">
                                    <label class="form-label fw-semibold">Mã voucher <span class="text-danger">*</span></label>
                                    <input type="text" name="code" id="code" class="form-control text-uppercase"
                                           placeholder="VD: SUMMER2026"
                                           value="${voucher.code}"
                                           maxlength="50" required
                                           oninput="this.value=this.value.toUpperCase()">
                                    <div class="invalid-feedback">Vui lòng nhập mã voucher.</div>
                                    <div class="form-text">Mã phân biệt hoa thường. VD: SUMMER2026</div>
                                </div>
                                <div class="col-12 col-md-6">
                                    <label class="form-label fw-semibold">Tên voucher <span class="text-danger">*</span></label>
                                    <input type="text" name="name" id="name" class="form-control"
                                           placeholder="VD: Khuyến mãi mùa hè"
                                           value="${voucher.name}"
                                           maxlength="255" required>
                                    <div class="invalid-feedback">Vui lòng nhập tên voucher.</div>
                                </div>
                                <div class="col-12">
                                    <label class="form-label fw-semibold">Mô tả</label>
                                    <textarea name="description" class="form-control" rows="2"
                                              placeholder="Mô tả ngắn gọn về voucher...">${voucher.description}</textarea>
                                </div>
                            </div>
                        </div>
                    </div>

                    <%-- Discount Config Card --%>
                    <div class="card border-0 rounded-4 mb-4" style="box-shadow:0 1px 4px rgba(0,0,0,0.06);">
                        <div class="card-header bg-white border-0 rounded-top-4 px-4 pt-4 pb-0">
                            <h6 class="fw-bold mb-0"><i class="bi bi-percent me-2 text-success"></i>Cấu hình giảm giá</h6>
                        </div>
                        <div class="card-body px-4 pb-4 pt-3">
                            <div class="row g-3">
                                <div class="col-12 col-md-6">
                                    <label class="form-label fw-semibold">Loại giảm giá <span class="text-danger">*</span></label>
                                    <select name="discountType" id="discountType" class="form-select" required
                                            onchange="toggleMaxDiscount()">
                                        <option value="">-- Chọn loại --</option>
                                        <option value="PERCENTAGE" ${voucher.discountType eq 'PERCENTAGE' ? 'selected' : ''}>
                                            Phần trăm (%)
                                        </option>
                                        <option value="FIXED_AMOUNT" ${voucher.discountType eq 'FIXED_AMOUNT' ? 'selected' : ''}>
                                            Số tiền cố định (VNĐ)
                                        </option>
                                    </select>
                                    <div class="invalid-feedback">Vui lòng chọn loại giảm giá.</div>
                                </div>
                                <div class="col-12 col-md-6">
                                    <label class="form-label fw-semibold">Giá trị giảm <span class="text-danger">*</span></label>
                                    <div class="input-group">
                                        <input type="number" name="discountValue" id="discountValue" class="form-control"
                                               placeholder="0" min="0" step="0.01" required
                                               value="${voucher.discountValue}">
                                        <span class="input-group-text" id="discountUnit">%</span>
                                    </div>
                                    <div class="invalid-feedback">Vui lòng nhập giá trị giảm.</div>
                                </div>
                                <div class="col-12 col-md-6">
                                    <label class="form-label fw-semibold">Đơn hàng tối thiểu</label>
                                    <div class="input-group">
                                        <input type="number" name="minOrderAmount" class="form-control"
                                               placeholder="0" min="0" step="1000"
                                               value="${voucher.minOrderAmount}">
                                        <span class="input-group-text">₫</span>
                                    </div>
                                    <div class="form-text">Để trống = không giới hạn.</div>
                                </div>
                                <div class="col-12 col-md-6" id="maxDiscountRow" style="display:none;">
                                    <label class="form-label fw-semibold">Giảm tối đa</label>
                                    <div class="input-group">
                                        <input type="number" name="maxDiscountAmount" id="maxDiscountAmount" class="form-control"
                                               placeholder="0" min="0" step="1000"
                                               value="${voucher.maxDiscountAmount}">
                                        <span class="input-group-text">₫</span>
                                    </div>
                                    <div class="form-text">Chỉ áp dụng cho loại phần trăm.</div>
                                </div>
                            </div>
                        </div>
                    </div>

                    <%-- Time & Limits Card --%>
                    <div class="card border-0 rounded-4 mb-4" style="box-shadow:0 1px 4px rgba(0,0,0,0.06);">
                        <div class="card-header bg-white border-0 rounded-top-4 px-4 pt-4 pb-0">
                            <h6 class="fw-bold mb-0"><i class="bi bi-calendar-range me-2 text-success"></i>Thời gian & Giới hạn</h6>
                        </div>
                        <div class="card-body px-4 pb-4 pt-3">
                            <div class="row g-3">
                                <div class="col-12 col-md-6">
                                    <label class="form-label fw-semibold">Thời gian bắt đầu <span class="text-danger">*</span></label>
                                    <input type="datetime-local" name="validFrom" id="validFrom" class="form-control" required
                                           value="${not empty voucher.validFrom ? fn:substring(voucher.validFrom.toString(), 0, 16) : ''}">
                                    <div class="invalid-feedback">Vui lòng chọn thời gian bắt đầu.</div>
                                </div>
                                <div class="col-12 col-md-6">
                                    <label class="form-label fw-semibold">Thời gian kết thúc <span class="text-danger">*</span></label>
                                    <input type="datetime-local" name="validTo" id="validTo" class="form-control" required
                                           value="${not empty voucher.validTo ? fn:substring(voucher.validTo.toString(), 0, 16) : ''}">
                                    <div class="invalid-feedback">Vui lòng chọn thời gian kết thúc.</div>
                                </div>
                                <div class="col-12 col-md-6">
                                    <label class="form-label fw-semibold">Giới hạn sử dụng (toàn hệ thống)</label>
                                    <input type="number" name="usageLimit" class="form-control"
                                           placeholder="Để trống = vô hạn" min="1"
                                           value="${voucher.usageLimit}">
                                </div>
                                <div class="col-12 col-md-6">
                                    <label class="form-label fw-semibold">Giới hạn mỗi user</label>
                                    <input type="number" name="perUserLimit" class="form-control"
                                           placeholder="1" min="1" value="${not empty voucher.perUserLimit ? voucher.perUserLimit : 1}">
                                </div>
                            </div>
                        </div>
                    </div>

                </div>

                <%-- ===== RIGHT COLUMN ===== --%>
                <div class="col-12 col-xl-4">

                    <%-- Booking Type Card --%>
                    <div class="card border-0 rounded-4 mb-4" style="box-shadow:0 1px 4px rgba(0,0,0,0.06);">
                        <div class="card-header bg-white border-0 rounded-top-4 px-4 pt-4 pb-0">
                            <h6 class="fw-bold mb-0"><i class="bi bi-bookmarks me-2 text-success"></i>Loại đặt sân áp dụng</h6>
                        </div>
                        <div class="card-body px-4 pb-4 pt-3">
                            <div class="d-flex flex-column gap-2">
                                <div class="form-check">
                                    <input class="form-check-input" type="radio" name="applicableBookingType" id="typeSingle"
                                           value="SINGLE" ${(empty voucher.applicableBookingType or voucher.applicableBookingType eq 'SINGLE') ? 'checked' : ''}>
                                    <label class="form-check-label" for="typeSingle">
                                        <span class="fw-semibold">Đặt sân đơn</span>
                                        <small class="d-block text-muted">Áp dụng cho booking đơn lẻ</small>
                                    </label>
                                </div>
                                <div class="form-check">
                                    <input class="form-check-input" type="radio" name="applicableBookingType" id="typeRecurring"
                                           value="RECURRING" ${voucher.applicableBookingType eq 'RECURRING' ? 'checked' : ''}>
                                    <label class="form-check-label" for="typeRecurring">
                                        <span class="fw-semibold">Đặt sân định kỳ</span>
                                        <small class="d-block text-muted">Áp dụng cho booking định kỳ</small>
                                    </label>
                                </div>
                                <div class="form-check">
                                    <input class="form-check-input" type="radio" name="applicableBookingType" id="typeBoth"
                                           value="BOTH" ${voucher.applicableBookingType eq 'BOTH' ? 'checked' : ''}>
                                    <label class="form-check-label" for="typeBoth">
                                        <span class="fw-semibold">Cả hai</span>
                                        <small class="d-block text-muted">Áp dụng cho tất cả loại booking</small>
                                    </label>
                                </div>
                            </div>
                        </div>
                    </div>

                    <%-- Facility Selection Card --%>
                    <div class="card border-0 rounded-4 mb-4" style="box-shadow:0 1px 4px rgba(0,0,0,0.06);">
                        <div class="card-header bg-white border-0 rounded-top-4 px-4 pt-4 pb-0">
                            <h6 class="fw-bold mb-0"><i class="bi bi-building me-2 text-success"></i>Sân áp dụng</h6>
                        </div>
                        <div class="card-body px-4 pb-4 pt-3">
                            <div class="mb-3">
                                <div class="form-check mb-2">
                                    <input class="form-check-input" type="radio" name="facilityScope" id="scopeAll"
                                           value="all" onchange="toggleFacilitySelect()"
                                           ${empty linkedFacilityIds ? 'checked' : ''}>
                                    <label class="form-check-label fw-semibold" for="scopeAll">
                                        <i class="bi bi-globe me-1"></i> Tất cả sân
                                    </label>
                                </div>
                                <div class="form-check">
                                    <input class="form-check-input" type="radio" name="facilityScope" id="scopeSpecific"
                                           value="specific" onchange="toggleFacilitySelect()"
                                           ${not empty linkedFacilityIds ? 'checked' : ''}>
                                    <label class="form-check-label fw-semibold" for="scopeSpecific">
                                        <i class="bi bi-pin-map me-1"></i> Chọn sân cụ thể
                                    </label>
                                </div>
                            </div>

                            <div id="facilitySelectWrapper" style="display:${not empty linkedFacilityIds ? 'block' : 'none'};">
                                <select id="facilitySelect" name="facilityIds" multiple>
                                    <c:forEach var="f" items="${facilities}">
                                        <option value="${f.facilityId}"
                                            <c:forEach var="lid" items="${linkedFacilityIds}">
                                                <c:if test="${lid eq f.facilityId}">selected</c:if>
                                            </c:forEach>
                                        >${f.name}</option>
                                    </c:forEach>
                                </select>
                            </div>
                        </div>
                    </div>

                    <%-- Status Card --%>
                    <div class="card border-0 rounded-4 mb-4" style="box-shadow:0 1px 4px rgba(0,0,0,0.06);">
                        <div class="card-body px-4 py-3">
                            <div class="d-flex align-items-center justify-content-between">
                                <div>
                                    <p class="fw-semibold mb-0">Kích hoạt voucher</p>
                                    <small class="text-muted">Voucher có thể được sử dụng</small>
                                </div>
                                <div class="form-check form-switch mb-0">
                                    <input class="form-check-input" type="checkbox" role="switch" id="isActiveSwitch"
                                           name="isActive" value="true"
                                           ${(empty voucher or voucher.isActive) ? 'checked' : ''}>
                                </div>
                            </div>
                        </div>
                    </div>

                    <%-- Action Buttons --%>
                    <div class="d-flex gap-3">
                        <button type="submit" class="btn btn-brand flex-fill fw-semibold rounded-3 py-2">
                            <i class="bi bi-save me-2"></i>${isEdit ? 'Cập nhật' : 'Tạo voucher'}
                        </button>
                        <a href="${pageContext.request.contextPath}/owner/vouchers/list"
                           class="btn btn-outline-secondary rounded-3 py-2 fw-semibold">
                            Hủy
                        </a>
                    </div>

                </div>
            </div>
        </form>
    </div><%-- end .content-area --%>

    <%-- MultiSelect JS – must be before footer --%>
    <script src="https://codeshack.io/web/demos/MultiSelect.js"></script>
    <script>
    (function () {
        'use strict';

        var multiSelect = null;

        function initMultiSelect() {
            if (multiSelect) return;
            var el = document.getElementById('facilitySelect');
            if (!el) return;
            multiSelect = new MultiSelect('#facilitySelect', {
                placeholder: 'Chọn sân áp dụng',
                search: true,
                selectAll: true,
                listAll: false
            });
        }

        window.toggleFacilitySelect = function() {
            var specific = document.getElementById('scopeSpecific').checked;
            var wrapper  = document.getElementById('facilitySelectWrapper');
            wrapper.style.display = specific ? 'block' : 'none';
            if (specific) initMultiSelect();
        };

        window.toggleMaxDiscount = function() {
            var type = document.getElementById('discountType').value;
            var row  = document.getElementById('maxDiscountRow');
            var unit = document.getElementById('discountUnit');
            if (type === 'PERCENTAGE') {
                row.style.display = 'block';
                unit.textContent  = '%';
            } else {
                row.style.display = 'none';
                unit.textContent  = '₫';
            }
        };

        document.addEventListener('DOMContentLoaded', function() {
            toggleMaxDiscount();
            // Init multi-select if editing with specific facilities
            if (document.getElementById('scopeSpecific') && document.getElementById('scopeSpecific').checked) {
                document.getElementById('facilitySelectWrapper').style.display = 'block';
                initMultiSelect();
            }
        });

        document.getElementById('voucherForm').addEventListener('submit', function(e) {
            if (!this.checkValidity()) {
                e.preventDefault();
                e.stopPropagation();
                this.classList.add('was-validated');
                return;
            }
            var from  = document.getElementById('validFrom').value;
            var to    = document.getElementById('validTo').value;
            if (from && to && from >= to) {
                e.preventDefault();
                alert('Thời gian bắt đầu phải trước thời gian kết thúc!');
                return;
            }
            var dtype = document.getElementById('discountType').value;
            var dval  = parseFloat(document.getElementById('discountValue').value);
            if (dtype === 'PERCENTAGE' && (dval <= 0 || dval > 100)) {
                e.preventDefault();
                alert('Giá trị giảm phần trăm phải từ 1 đến 100!');
                return;
            }
            if (dtype === 'FIXED_AMOUNT' && dval <= 0) {
                e.preventDefault();
                alert('Giá trị giảm phải lớn hơn 0!');
                return;
            }
            this.classList.add('was-validated');
        });
    })();
    </script>

    <%@ include file="../layout/footer.jsp" %>
