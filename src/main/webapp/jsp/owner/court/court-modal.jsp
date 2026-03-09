<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<div class="modal fade" id="courtModal" tabindex="-1" aria-hidden="true" data-context-path="${pageContext.request.contextPath}">
    <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content">

            <form method="POST" action="${pageContext.request.contextPath}/owner/courts/save" id="courtForm">

                <%-- hidden để phân biệt create / edit --%>
                <input type="hidden" name="courtId" id="courtId">
                <input type="hidden" name="facilityId" value="${facility.facilityId}">
                <input type="hidden" name="isBulk" id="isBulkInput" value="false">

                <div class="modal-header bg-primary text-white">
                    <h5 class="modal-title" id="courtModalTitle">Thêm sân mới</h5>
                    <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal"></button>
                </div>

                <div class="modal-body">

                    <%-- TOGGLE TẠO NHIỀU SÂN (chỉ hiện khi tạo mới) --%>
                    <div class="mb-3 p-3 bg-light rounded" id="bulkToggleWrapper">
                        <div class="form-check form-switch d-flex align-items-center gap-2 mb-0">
                            <input class="form-check-input" type="checkbox" id="bulkToggle" role="switch" style="width:2.5em;height:1.3em;">
                            <label class="form-check-label fw-semibold" for="bulkToggle">
                                <i class="bi bi-layers me-1"></i> Tạo nhiều sân cùng lúc
                            </label>
                        </div>
                    </div>

                    <%-- HƯỚNG DẪN (chỉ hiện khi bật bulk) --%>
                    <div id="bulkHint" class="alert alert-info py-2 px-3 small" style="display:none;">
                        <i class="bi bi-info-circle-fill me-1"></i>
                        <strong>Hướng dẫn đặt tên sân:</strong> Hệ thống sẽ tự động ghép
                        <em>Tiền tố</em> + <em>số thứ tự</em> cho từng sân.<br>
                        Ví dụ: tiền tố <strong>"Sân"</strong>, số lượng <strong>3</strong>
                        → sẽ tạo <strong>Sân 3, Sân 4, Sân 5</strong>
                        (tiếp nối số sân đã có, không bị trùng tên).
                    </div>

                    <%-- BULK: TIỀN TỐ TÊN SÂN + SỐ LƯỢNG (collapse) --%>
                    <div id="bulkFields" style="display:none;">
                        <div class="mb-3">
                            <label class="form-label fw-bold">Tiền tố tên sân <span class="text-danger">*</span></label>
                            <input type="text" name="courtNamePrefix" id="courtNamePrefix" class="form-control"
                                   placeholder="Ví dụ: Sân">
                        </div>
                        <div class="mb-3">
                            <label class="form-label fw-bold">Số lượng cần tạo <span class="text-danger">*</span></label>
                            <input type="number" name="courtCount" id="courtCount" class="form-control"
                                   min="1" max="20" placeholder="Nhập số lượng (1 – 20)">
                            <div class="form-text text-muted">Tối đa 20 sân mỗi lần tạo.</div>
                        </div>
                        <hr>
                    </div>

                    <%-- SINGLE: TÊN SÂN --%>
                    <div class="mb-3" id="singleNameWrapper">
                        <label class="form-label fw-bold">Tên sân <span class="text-danger">*</span></label>
                        <input type="text" name="courtName" id="courtName" class="form-control"
                               placeholder="Ví dụ: Sân 1, Sân A">
                    </div>

                    <%-- LOẠI SÂN --%>
                    <div class="mb-3">
                        <label class="form-label fw-bold">Loại sân <span class="text-danger">*</span></label>
                        <select name="courtTypeId" id="courtTypeId" class="form-select" required>
                            <option value="">-- Chọn loại sân --</option>
                            <c:forEach var="type" items="${courtTypes}">
                                <option value="${type.courtTypeId}">${type.typeCode}</option>
                            </c:forEach>
                        </select>
                    </div>

                    <%-- MÔ TẢ --%>
                    <div class="mb-3">
                        <label for="courtDescription" class="form-label fw-bold">Mô tả</label>
                        <textarea name="description" id="courtDescription" class="form-control"
                                  rows="3" maxlength="500"
                                  placeholder="Mô tả ngắn về sân (tùy chọn)"></textarea>
                        <div class="form-text">Tối đa 500 ký tự</div>
                    </div>

                </div>

                <div class="modal-footer">
                    <button type="button" class="btn btn-outline-secondary" data-bs-dismiss="modal">Hủy</button>
                    <button type="submit" class="btn btn-accent" id="courtSubmitBtn">
                        <i class="bi bi-save me-1"></i> Lưu lại
                    </button>
                </div>

            </form>

        </div>
    </div>
</div>

<script src="${pageContext.request.contextPath}/assets/js/court-modal.js"></script>
