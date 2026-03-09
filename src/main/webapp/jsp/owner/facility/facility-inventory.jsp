<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<%@ include file="../layout/layout.jsp" %>
<%@ include file="../layout/sidebar.jsp" %>

<div class="main-content">

    <%@ include file="../layout/header.jsp" %>

    <div class="content-area">

        <%@ include file="../layout/page-header.jsp" %>

        <div class="card shadow-sm border-0 rounded-4">
            <div class="card-body p-4">

                <div class="d-flex justify-content-between align-items-center mb-4">
                    <div>
                        <h4 class="fw-bold text-emerald mb-1">Kho đồ sân</h4>
                        <p class="text-muted mb-0">
                            Quản lý đồ đã gán cho sân và kho đồ khả dụng
                        </p>
                    </div>
                </div>

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

                <!-- ====================== SECTION 1: ĐỒ GÁN SÂN ====================== -->
                <div class="mb-5">
                    <div class="d-flex justify-content-between align-items-center mb-3">
                        <div>
                            <h5 class="fw-bold text-dark mb-1">Đồ gán sân</h5>
                            <p class="text-muted mb-0">
                                Danh sách sản phẩm đã được gán cho sân hiện tại
                            </p>
                        </div>
                    </div>

                    <!-- SEARCH ASSIGNED -->
                    <form method="get"
                          action="${pageContext.request.contextPath}/owner/facility/inventory/${facilityId}"
                          class="row g-3 mb-4">

                        <div class="col-md-4">
                            <input type="text"
                                   name="assignedKeyword"
                                   value="${assignedKeyword}"
                                   class="form-control rounded-3"
                                   placeholder="Tìm đồ gán sân theo tên sản phẩm..."
                                   aria-label="Tìm đồ gán sân theo tên sản phẩm">
                        </div>

                        <input type="hidden" name="inventoryKeyword" value="${inventoryKeyword}"/>
                        <input type="hidden" name="inventoryPage" value="${inventoryCurrentPage}"/>

                        <div class="col-auto">
                            <button type="submit" class="btn btn-outline-success rounded-3">
                                Tìm kiếm
                            </button>
                        </div>
                    </form>

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
                                            Chưa có sản phẩm nào được gán cho sân
                                        </td>
                                    </tr>
                                </c:when>

                                <c:otherwise>
                                    <c:forEach items="${assignedItems}" var="item" varStatus="status">
                                        <tr>
                                            <td>${(assignedCurrentPage - 1) * assignedPageSize + status.index + 1}</td>
                                            <td>${item.facilityName}</td>
                                            <td>${item.inventoryName}</td>

                                            <td style="width: 180px;">
                                                <input type="number"
                                                       name="totalQuantity"
                                                       min="0"
                                                       value="${item.totalQuantity}"
                                                       class="form-control form-control-sm rounded-3"
                                                       form="updateForm_${item.facilityInventoryId}"
                                                       aria-label="Số lượng sản phẩm">
                                            </td>

                                            <td style="width: 180px;">
                                                <input type="number"
                                                       value="${item.availableQuantity}"
                                                       class="form-control form-control-sm rounded-3"
                                                       readonly
                                                       aria-label="Số lượng khả dụng">
                                            </td>

                                            <td class="text-center" style="min-width: 220px;">
                                                <div class="d-flex justify-content-center gap-2">

                                                    <!-- Form cập nhật -->
                                                    <form id="updateForm_${item.facilityInventoryId}"
                                                          method="post"
                                                          action="${pageContext.request.contextPath}/owner/facility/inventory/${facilityId}">
                                                        <input type="hidden" name="action" value="updateQuantity"/>
                                                        <input type="hidden" name="facilityId" value="${facilityId}"/>
                                                        <input type="hidden" name="facilityInventoryId" value="${item.facilityInventoryId}"/>
                                                    </form>

                                                    <button type="submit"
                                                            form="updateForm_${item.facilityInventoryId}"
                                                            class="btn btn-sm btn-success rounded-3">
                                                        Lưu
                                                    </button>

                                                    <!-- Form gỡ -->
                                                    <form method="post"
                                                          action="${pageContext.request.contextPath}/owner/facility/inventory/${facilityId}"
                                                          onsubmit="return confirm('Bạn có chắc muốn gỡ sản phẩm này khỏi sân không?');">
                                                        <input type="hidden" name="action" value="remove"/>
                                                        <input type="hidden" name="facilityId" value="${facilityId}"/>
                                                        <input type="hidden" name="facilityInventoryId" value="${item.facilityInventoryId}"/>

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

                    <!-- PAGINATION ASSIGNED -->
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
                </div>

                <!-- ====================== SECTION 2: KHO ĐỒ ====================== -->
                <div>
                    <div class="d-flex justify-content-between align-items-center mb-3">
                        <div>
                            <h5 class="fw-bold text-dark mb-1">Kho đồ</h5>
                            <p class="text-muted mb-0">
                                Danh sách sản phẩm đang active và chưa gán cho sân hiện tại
                            </p>
                        </div>
                    </div>

                    <!-- SEARCH INVENTORY -->
                    <form method="get"
                          action="${pageContext.request.contextPath}/owner/facility/inventory/${facilityId}"
                          class="row g-3 mb-4">

                        <div class="col-md-4">
                            <input type="text"
                                   name="inventoryKeyword"
                                   value="${inventoryKeyword}"
                                   class="form-control rounded-3"
                                   placeholder="Tìm trong kho đồ theo tên sản phẩm..."
                                   aria-label="Tìm trong kho đồ theo tên sản phẩm">
                        </div>

                        <input type="hidden" name="assignedKeyword" value="${assignedKeyword}"/>
                        <input type="hidden" name="assignedPage" value="${assignedCurrentPage}"/>

                        <div class="col-auto">
                            <button type="submit" class="btn btn-outline-success rounded-3">
                                Tìm kiếm
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
                                            Không còn sản phẩm active nào khả dụng để gán
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
                                            <td>
                                                <span class="badge bg-success">Active</span>
                                            </td>
                                            <td class="text-center">
                                                <form method="post"
                                                      action="${pageContext.request.contextPath}/owner/facility/inventory/${facilityId}">
                                                    <input type="hidden" name="action" value="assign"/>
                                                    <input type="hidden" name="facilityId" value="${facilityId}"/>
                                                    <input type="hidden" name="inventoryId" value="${i.inventoryId}"/>

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

                    <!-- PAGINATION INVENTORY -->
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
                </div>

            </div>
        </div>

    </div>

    <%@ include file="../layout/footer.jsp" %>

</div>