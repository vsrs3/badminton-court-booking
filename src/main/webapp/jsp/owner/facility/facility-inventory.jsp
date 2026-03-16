<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<%@ include file="../layout/layout.jsp" %>
<%@ include file="../layout/sidebar.jsp" %>

<div class="main-content">

    <%@ include file="../layout/header.jsp" %>

    <div class="content-area">

        <%@ include file="../layout/page-header.jsp" %>

        <div class="card shadow-sm border-0 rounded-4">
            <div class="card-body p-4">

                <c:if test="${not empty sessionScope.successMessage}">
                    <div class="alert alert-success alert-dismissible fade show rounded-3" role="alert">
                            ${sessionScope.successMessage}
                        <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Đóng"></button>
                    </div>
                    <c:remove var="successMessage" scope="session"/>
                </c:if>

                <c:if test="${not empty sessionScope.errorMessage}">
                    <div class="alert alert-danger alert-dismissible fade show rounded-3" role="alert">
                            ${sessionScope.errorMessage}
                        <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Đóng"></button>
                    </div>
                    <c:remove var="errorMessage" scope="session"/>
                </c:if>

                <div class="mb-5">
                    <div class="d-flex justify-content-between align-items-center mb-3">
                        <div>
                            <h5 class="fw-bold text-dark mb-1">Đồ gán sân</h5>
                            <p class="text-muted mb-0">Danh sách sản phẩm đã được gán cho sân hiện tại.</p>
                        </div>
                    </div>

                    <form method="get"
                          action="${pageContext.request.contextPath}/owner/facility/inventory/${facilityId}"
                          class="row g-3 align-items-center mb-4">

                        <div class="col-lg-5 col-md-7">
                            <input type="text"
                                   id="assignedKeywordInput"
                                   name="assignedKeyword"
                                   value="${assignedKeyword}"
                                   class="form-control rounded-3"
                                   placeholder="Tìm theo tên sản phẩm đã gán"
                                   aria-label="Tìm theo tên sản phẩm đã gán">
                        </div>

                        <input type="hidden" name="inventoryKeyword" value="${inventoryKeyword}"/>
                        <input type="hidden" name="assignedPage" id="assignedPageField" value="${assignedCurrentPage}"/>
                        <input type="hidden" name="inventoryPage" value="${inventoryCurrentPage}"/>

                        <div class="col-auto d-flex flex-wrap gap-2">
                            <button type="submit" id="assignedSearchBtn" class="btn btn-outline-success rounded-3">
                                Tìm kiếm
                            </button>
                            <button type="button" id="toggleBulkQuantityBtn" class="btn btn-success rounded-3">
                                Thêm hàng loạt
                            </button>
                        </div>
                    </form>

                    <div id="bulkQuantityPanel" class="card border-0 bg-light rounded-4 mb-4 d-none">
                        <div class="card-body">
                            <form method="post"
                                  action="${pageContext.request.contextPath}/owner/facility/inventory/${facilityId}"
                                  class="row g-3 align-items-end">
                                <input type="hidden" name="action" value="bulkUpdateQuantity"/>
                                <input type="hidden" name="facilityId" value="${facilityId}"/>
                                <input type="hidden" name="assignedKeyword" value="${assignedKeyword}"/>
                                <input type="hidden" name="inventoryKeyword" value="${inventoryKeyword}"/>
                                <input type="hidden" name="assignedPage" value="${assignedCurrentPage}"/>
                                <input type="hidden" name="inventoryPage" value="${inventoryCurrentPage}"/>

                                <div class="col-lg-4 col-md-6">
                                    <label for="bulkQuantity" class="form-label fw-semibold">Số lượng áp dụng</label>
                                    <input type="number"
                                           id="bulkQuantity"
                                           name="bulkQuantity"
                                           min="0"
                                           step="1"
                                           class="form-control rounded-3"
                                           data-non-negative="true"
                                           data-quantity-label="Số lượng áp dụng"
                                           required>
                                </div>

                                <div class="col-12 d-flex flex-wrap gap-2">
                                    <button type="submit" class="btn btn-success rounded-3">Lưu</button>
                                    <button type="button" id="cancelBulkQuantityBtn" class="btn btn-outline-secondary rounded-3">
                                        Hủy
                                    </button>
                                </div>
                            </form>
                        </div>
                    </div>

                    <div class="table-responsive">
                        <table class="table table-hover align-middle">
                            <thead class="table-light">
                            <tr>
                                <th>STT</th>
                                <th>Tên sân</th>
                                <th>Tên sản phẩm</th>
                                <th>Số lượng sản phẩm</th>
                                <th>Số lượng khả dụng</th>
                                <th class="text-center">Thao tác</th>
                            </tr>
                            </thead>

                            <tbody>
                            <c:choose>
                                <c:when test="${empty assignedItems}">
                                    <tr>
                                        <td colspan="6" class="text-center text-muted">
                                            Chưa có sản phẩm nào được gán cho sân.
                                        </td>
                                    </tr>
                                </c:when>

                                <c:otherwise>
                                    <c:forEach items="${assignedItems}" var="item" varStatus="status">
                                        <tr>
                                            <td>${(assignedCurrentPage - 1) * assignedPageSize + status.index + 1}</td>
                                            <td>${item.facilityName}</td>
                                            <td>${item.inventoryName}</td>

                                            <td style="width: 190px;">
                                                <input type="number"
                                                       name="totalQuantity"
                                                       min="0"
                                                       step="1"
                                                       value="${item.totalQuantity}"
                                                       class="form-control form-control-sm rounded-3"
                                                       form="updateForm_${item.facilityInventoryId}"
                                                       data-non-negative="true"
                                                       data-quantity-label="Số lượng sản phẩm"
                                                       aria-label="Số lượng sản phẩm"
                                                       required>
                                            </td>

                                            <td style="width: 190px;">
                                                <input type="number"
                                                       value="${item.availableQuantity}"
                                                       class="form-control form-control-sm rounded-3"
                                                       readonly
                                                       aria-label="Số lượng khả dụng">
                                            </td>

                                            <td class="text-center" style="min-width: 220px;">
                                                <div class="d-flex justify-content-center gap-2">
                                                    <form id="updateForm_${item.facilityInventoryId}"
                                                          method="post"
                                                          action="${pageContext.request.contextPath}/owner/facility/inventory/${facilityId}">
                                                        <input type="hidden" name="action" value="updateQuantity"/>
                                                        <input type="hidden" name="facilityId" value="${facilityId}"/>
                                                        <input type="hidden" name="facilityInventoryId" value="${item.facilityInventoryId}"/>
                                                        <input type="hidden" name="assignedKeyword" value="${assignedKeyword}"/>
                                                        <input type="hidden" name="inventoryKeyword" value="${inventoryKeyword}"/>
                                                        <input type="hidden" name="assignedPage" value="${assignedCurrentPage}"/>
                                                        <input type="hidden" name="inventoryPage" value="${inventoryCurrentPage}"/>
                                                    </form>

                                                    <button type="submit"
                                                            form="updateForm_${item.facilityInventoryId}"
                                                            class="btn btn-sm btn-success rounded-3">
                                                        Lưu
                                                    </button>

                                                    <form method="post"
                                                          action="${pageContext.request.contextPath}/owner/facility/inventory/${facilityId}"
                                                          onsubmit="return confirm('Bạn có chắc muốn gỡ sản phẩm này khỏi sân không?');">
                                                        <input type="hidden" name="action" value="remove"/>
                                                        <input type="hidden" name="facilityId" value="${facilityId}"/>
                                                        <input type="hidden" name="facilityInventoryId" value="${item.facilityInventoryId}"/>
                                                        <input type="hidden" name="assignedKeyword" value="${assignedKeyword}"/>
                                                        <input type="hidden" name="inventoryKeyword" value="${inventoryKeyword}"/>
                                                        <input type="hidden" name="assignedPage" value="${assignedCurrentPage}"/>
                                                        <input type="hidden" name="inventoryPage" value="${inventoryCurrentPage}"/>

                                                        <button type="submit" class="btn btn-sm btn-outline-danger rounded-3">
                                                            Gỡ
                                                        </button>
                                                    </form>
                                                </div>
                                            </td>
                                        </tr>
                                    </c:forEach>
                                </c:otherwise>
                            </c:choose>
                            </tbody>
                        </table>
                    </div>

                    <c:if test="${assignedTotalPages > 1}">
                        <nav class="mt-4">
                            <ul class="pagination justify-content-center">
                                <c:forEach begin="1" end="${assignedTotalPages}" var="p">
                                    <li class="page-item ${p == assignedCurrentPage ? 'active' : ''}">
                                        <a class="page-link"
                                           href="${pageContext.request.contextPath}/owner/facility/inventory/${facilityId}?assignedPage=${p}&assignedKeyword=${assignedKeyword}&inventoryKeyword=${inventoryKeyword}&inventoryPage=${inventoryCurrentPage}">
                                                ${p}
                                        </a>
                                    </li>
                                </c:forEach>
                            </ul>
                        </nav>
                    </c:if>
                </div>

                <div>
                    <div class="d-flex justify-content-between align-items-center mb-3">
                        <div>
                            <h5 class="fw-bold text-dark mb-1">Kho đồ tổng</h5>
                            <p class="text-muted mb-0">Danh sách sản phẩm đang hoạt động và chưa gán cho sân hiện tại.</p>
                        </div>
                    </div>

                    <form method="get"
                          action="${pageContext.request.contextPath}/owner/facility/inventory/${facilityId}"
                          class="row g-3 align-items-center mb-4">

                        <div class="col-lg-5 col-md-7">
                            <input type="text"
                                   name="inventoryKeyword"
                                   value="${inventoryKeyword}"
                                   class="form-control rounded-3"
                                   placeholder="Tìm trong kho đồ tổng"
                                   aria-label="Tìm trong kho đồ tổng">
                        </div>

                        <input type="hidden" name="facilityId" value="${facilityId}"/>
                        <input type="hidden" name="assignedKeyword" value="${assignedKeyword}"/>
                        <input type="hidden" name="assignedPage" value="${assignedCurrentPage}"/>
                        <input type="hidden" name="inventoryPage" id="inventoryPageField" value="${inventoryCurrentPage}"/>

                        <div class="col-auto d-flex flex-wrap gap-2">
                            <button type="submit" id="inventorySearchBtn" class="btn btn-outline-success rounded-3">
                                Tìm kiếm
                            </button>
                            <button type="submit"
                                    formmethod="post"
                                    formaction="${pageContext.request.contextPath}/owner/facility/inventory/${facilityId}"
                                    name="action"
                                    value="assignAll"
                                    class="btn btn-success rounded-3">
                                Gán tất cả
                            </button>
                        </div>
                    </form>

                    <div class="table-responsive">
                        <table class="table table-hover align-middle">
                            <thead class="table-light">
                            <tr>
                                <th>STT</th>
                                <th>Tên</th>
                                <th>Thương hiệu</th>
                                <th>Mô tả</th>
                                <th>Giá thuê</th>
                                <th>Trạng thái</th>
                                <th class="text-center">Thao tác</th>
                            </tr>
                            </thead>

                            <tbody>
                            <c:choose>
                                <c:when test="${empty inventories}">
                                    <tr>
                                        <td colspan="7" class="text-center text-muted">
                                            Không còn sản phẩm khả dụng để gán cho sân.
                                        </td>
                                    </tr>
                                </c:when>

                                <c:otherwise>
                                    <c:forEach items="${inventories}" var="i" varStatus="status">
                                        <tr>
                                            <td>${(inventoryCurrentPage - 1) * inventoryPageSize + status.index + 1}</td>
                                            <td>${i.name}</td>
                                            <td>${i.brand}</td>
                                            <td>${i.description}</td>
                                            <td>${i.rentalPrice}</td>
                                            <td><span class="badge bg-success">Đang hoạt động</span></td>
                                            <td class="text-center">
                                                <form method="post"
                                                      action="${pageContext.request.contextPath}/owner/facility/inventory/${facilityId}">
                                                    <input type="hidden" name="action" value="assign"/>
                                                    <input type="hidden" name="facilityId" value="${facilityId}"/>
                                                    <input type="hidden" name="inventoryId" value="${i.inventoryId}"/>
                                                    <input type="hidden" name="assignedKeyword" value="${assignedKeyword}"/>
                                                    <input type="hidden" name="inventoryKeyword" value="${inventoryKeyword}"/>
                                                    <input type="hidden" name="assignedPage" value="${assignedCurrentPage}"/>
                                                    <input type="hidden" name="inventoryPage" value="${inventoryCurrentPage}"/>

                                                    <button type="submit" class="btn btn-sm btn-primary rounded-3">
                                                        Gán
                                                    </button>
                                                </form>
                                            </td>
                                        </tr>
                                    </c:forEach>
                                </c:otherwise>
                            </c:choose>
                            </tbody>
                        </table>
                    </div>

                    <c:if test="${inventoryTotalPages > 1}">
                        <nav class="mt-4">
                            <ul class="pagination justify-content-center">
                                <c:forEach begin="1" end="${inventoryTotalPages}" var="p">
                                    <li class="page-item ${p == inventoryCurrentPage ? 'active' : ''}">
                                        <a class="page-link"
                                           href="${pageContext.request.contextPath}/owner/facility/inventory/${facilityId}?inventoryPage=${p}&inventoryKeyword=${inventoryKeyword}&assignedKeyword=${assignedKeyword}&assignedPage=${assignedCurrentPage}">
                                                ${p}
                                        </a>
                                    </li>
                                </c:forEach>
                            </ul>
                        </nav>
                    </c:if>
                </div>

            </div>
        </div>

    </div>

    <%@ include file="../layout/footer.jsp" %>

</div>

<script>
    document.addEventListener("DOMContentLoaded", function () {
        const bulkPanel = document.getElementById("bulkQuantityPanel");
        const toggleBulkButton = document.getElementById("toggleBulkQuantityBtn");
        const cancelBulkButton = document.getElementById("cancelBulkQuantityBtn");
        const bulkQuantityInput = document.getElementById("bulkQuantity");
        const assignedSearchButton = document.getElementById("assignedSearchBtn");
        const assignedPageField = document.getElementById("assignedPageField");
        const inventorySearchButton = document.getElementById("inventorySearchBtn");
        const inventoryPageField = document.getElementById("inventoryPageField");

        if (toggleBulkButton && bulkPanel) {
            toggleBulkButton.addEventListener("click", function () {
                bulkPanel.classList.remove("d-none");
                if (bulkQuantityInput) {
                    bulkQuantityInput.focus();
                }
            });
        }

        if (cancelBulkButton && bulkPanel) {
            cancelBulkButton.addEventListener("click", function () {
                bulkPanel.classList.add("d-none");
                if (bulkQuantityInput) {
                    bulkQuantityInput.value = "";
                    bulkQuantityInput.setCustomValidity("");
                }
            });
        }

        if (assignedSearchButton && assignedPageField) {
            assignedSearchButton.addEventListener("click", function () {
                assignedPageField.value = "1";
            });
        }

        if (inventorySearchButton && inventoryPageField) {
            inventorySearchButton.addEventListener("click", function () {
                inventoryPageField.value = "1";
            });
        }

        const quantityInputs = document.querySelectorAll("input[data-non-negative='true']");
        const blockedKeys = ["-", "+", "e", "E"];

        quantityInputs.forEach(function (input) {
            input.addEventListener("keydown", function (event) {
                if (blockedKeys.includes(event.key)) {
                    event.preventDefault();
                }
            });

            input.addEventListener("input", function () {
                this.setCustomValidity("");
                if (this.value !== "" && Number(this.value) < 0) {
                    this.value = "0";
                }
            });

            input.addEventListener("invalid", function () {
                const label = this.dataset.quantityLabel || "Giá trị";
                if (this.validity.valueMissing) {
                    this.setCustomValidity(label + " không được để trống.");
                    return;
                }
                if (this.validity.rangeUnderflow) {
                    this.setCustomValidity(label + " phải lớn hơn hoặc bằng 0.");
                    return;
                }
                if (this.validity.badInput || this.validity.stepMismatch) {
                    this.setCustomValidity(label + " phải là số nguyên không âm.");
                    return;
                }
                this.setCustomValidity(label + " không hợp lệ.");
            });
        });
    });
</script>
