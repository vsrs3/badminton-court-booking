<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<div class="modal fade" id="courtModal" tabindex="-1" aria-hidden="true" data-context-path="${pageContext.request.contextPath}">
    <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content">

            <form method="POST" action="${pageContext.request.contextPath}/owner/courts/save">

                <%-- hidden để phân biệt create / edit --%>
                <input type="hidden" name="courtId" id="courtId">
                <input type="hidden" name="facilityId" value="${facility.facilityId}">

                <div class="modal-header bg-primary text-white">
                    <h5 class="modal-title" id="courtModalTitle">Add Court</h5>
                    <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal"></button>
                </div>

                <div class="modal-body">

                    <%-- COURT NAME --%>
                    <div class="mb-3">
                        <label class="form-label fw-bold">Tên sân <span class="text-danger">*</span></label>
                        <input type="text" name="courtName" id="courtName" class="form-control" required placeholder="Ví dụ: Sân 1, Sân A">
                    </div>

                    <%-- COURT TYPE --%>
                    <div class="mb-3">
                        <label class="form-label fw-bold">Loại sân <span class="text-danger">*</span></label>
                        <select name="courtTypeId" id="courtTypeId" class="form-select" required>
                            <option value="">-- Chọn loại sân --</option>
                            <c:forEach var="type" items="${courtTypes}">
                                <option value="${type.courtTypeId}">${type.typeCode}</option>
                            </c:forEach>
                        </select>
                    </div>

                </div>

                <div class="modal-footer">
                    <button type="button" class="btn btn-outline-secondary" data-bs-dismiss="modal">Hủy</button>
                    <button type="submit" class="btn btn-accent">
                        <i class="bi bi-save me-1"></i> Lưu lại
                    </button>
                </div>

            </form>

        </div>
    </div>
</div>

<script src="${pageContext.request.contextPath}/assets/js/court-modal.js"></script>
