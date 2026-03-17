<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<%@ include file="../layout/layout.jsp"%>
<%@ include file="../layout/sidebar.jsp"%>

<div class="main-content">

    <%@ include file="../layout/header.jsp"%>

    <div class="content-area">
        <div class="mb-4">
            <h1 class="fw-black mb-1" style="font-size:1.75rem;color:var(--color-gray-900);">
                <c:choose>
                    <c:when test="${inventory != null}">
                        Chỉnh sửa dụng cụ
                    </c:when>
                    <c:otherwise>
                        Thêm dụng cụ mới
                    </c:otherwise>
                </c:choose>
            </h1>
        </div>

        <div class="card border-0 rounded-4" style="box-shadow:0 1px 6px rgba(0,0,0,0.08);">
            <div class="card-body p-4">
                <c:if test="${not empty error}">
                    <div class="alert alert-danger rounded-3">${error}</div>
                </c:if>

                <form method="post"
                      action="${pageContext.request.contextPath}/owner/inventory">

                    <c:if test="${inventory != null && inventory.inventoryId > 0}">
                        <input type="hidden" name="id" value="${inventory.inventoryId}"/>
                    </c:if>

                    <div class="mb-3">
                        <label class="form-label fw-semibold" for="name">Tên dụng cụ</label>
                        <input type="text"
                               id="name"
                               name="name"
                               class="form-control rounded-3"
                               value="${inventory.name}"
                               required/>
                    </div>

                    <div class="mb-3">
                        <label class="form-label fw-semibold" for="brand">Thương hiệu</label>
                        <input type="text"
                               id="brand"
                               name="brand"
                               class="form-control rounded-3"
                               value="${inventory.brand}"
                               required/>
                    </div>

                    <div class="mb-3">
                        <label class="form-label fw-semibold" for="description">Mô tả</label>
                        <textarea id="description"
                                  name="description"
                                  class="form-control rounded-3"
                                  rows="4">${inventory.description}</textarea>
                    </div>

                    <div class="mb-3">
                        <label class="form-label fw-semibold" for="price">Giá thuê</label>
                        <input type="number"
                               id="price"
                               step="0.01"
                               min="0"
                               inputmode="decimal"
                               name="price"
                               class="form-control rounded-3"
                               value="${inventory.rentalPrice}"
                               required/>
                        <div class="form-text">Giá thuê phải là số không âm.</div>
                    </div>

                    <div class="form-check mb-4">
                        <input class="form-check-input"
                               type="checkbox"
                               name="active"
                               id="activeCheck"
                               <c:if test="${inventory != null && inventory.active}">checked</c:if> />

                        <label class="form-check-label fw-semibold" for="activeCheck">
                            Đang hoạt động
                        </label>
                    </div>

                    <div class="d-flex gap-3">
                        <button type="submit" class="btn btn-success rounded-3 px-4">
                            Lưu
                        </button>

                        <a href="${pageContext.request.contextPath}/owner/inventory"
                           class="btn btn-outline-secondary rounded-3 px-4">
                            Hủy
                        </a>
                    </div>
                </form>
            </div>
        </div>
    </div>

    <%@ include file="../layout/footer.jsp"%>

</div>

<script>
    const rentalPriceInput = document.getElementById("price");

    if (rentalPriceInput) {
        rentalPriceInput.addEventListener("keydown", function (event) {
            if (event.key === "-") {
                event.preventDefault();
            }
        });

        rentalPriceInput.addEventListener("input", function () {
            rentalPriceInput.setCustomValidity("");

            if (!rentalPriceInput.value) {
                return;
            }

            const currentValue = Number(rentalPriceInput.value);
            if (Number.isFinite(currentValue) && currentValue < 0) {
                rentalPriceInput.value = "0";
            }
        });

        rentalPriceInput.addEventListener("invalid", function () {
            if (rentalPriceInput.validity.valueMissing) {
                rentalPriceInput.setCustomValidity("Vui lòng nhập giá thuê");
            } else if (rentalPriceInput.validity.rangeUnderflow) {
                rentalPriceInput.setCustomValidity("Giá thuê không được là số âm");
            } else if (rentalPriceInput.validity.badInput) {
                rentalPriceInput.setCustomValidity("Giá thuê phải là số hợp lệ");
            } else {
                rentalPriceInput.setCustomValidity("");
            }
        });
    }
</script>
