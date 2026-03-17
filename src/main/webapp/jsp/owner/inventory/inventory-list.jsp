<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<%@ include file="../layout/layout.jsp"%>
<%@ include file="../layout/sidebar.jsp"%>

<div class="main-content">

    <%@ include file="../layout/header.jsp"%>

    <div class="content-area">

        <div class="d-flex align-items-center justify-content-between mb-4">
            <div>
                <h1 class="fw-black mb-1" style="font-size:1.75rem;color:var(--color-gray-900);">
                    Quản lý dụng cụ
                </h1>
                <p class="text-secondary mb-0">
                    Quản lý danh sách dụng cụ cho thuê
                </p>
            </div>

            <a href="${pageContext.request.contextPath}/owner/inventory?action=add"
               class="btn btn-success rounded-3 px-4">
                <i class="bi bi-plus-circle"></i>
                Thêm dụng cụ
            </a>
        </div>

        <div class="card border-0 rounded-4 mb-4" style="box-shadow:0 2px 8px rgba(0,0,0,0.06);">
            <div class="card-body">
                <form method="get"
                      action="${pageContext.request.contextPath}/owner/inventory"
                      class="row g-3">

                    <div class="col-md-4">
                        <input type="text"
                               name="keyword"
                               value="${keyword}"
                               class="form-control rounded-3"
                               placeholder="Tìm tên dụng cụ...">
                    </div>

                    <div class="col-auto">
                        <button class="btn btn-outline-success rounded-3">
                            <i class="bi bi-search"></i>
                            Tìm kiếm
                        </button>
                    </div>
                </form>
            </div>
        </div>

        <div class="card border-0 rounded-4" style="box-shadow:0 2px 8px rgba(0,0,0,0.06);">
            <div class="card-body">
                <div class="table-responsive">
                    <table class="table align-middle">
                        <thead class="table-light">
                        <tr>
                            <th>STT</th>
                            <th>Tên dụng cụ</th>
                            <th>Thương hiệu</th>
                            <th>Giá thuê</th>
                            <th>Trạng thái</th>
                            <th class="text-end">Thao tác</th>
                        </tr>
                        </thead>

                        <tbody>
                        <c:forEach items="${inventories}" var="i" varStatus="loop">
                            <tr>
                                <td>${(currentPage - 1) * 10 + loop.count}</td>
                                <td class="fw-semibold">${i.name}</td>
                                <td>${i.brand}</td>
                                <td class="text-success fw-bold">${i.rentalPrice}</td>
                                <td>
                                    <c:choose>
                                        <c:when test="${i.active}">
                                            <span class="badge bg-success">Đang hoạt động</span>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="badge bg-secondary">Ngừng hoạt động</span>
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                                <td class="text-end">
                                    <a href="${pageContext.request.contextPath}/owner/inventory?action=edit&id=${i.inventoryId}"
                                       class="btn btn-sm btn-outline-primary"
                                       aria-label="Chỉnh sửa">
                                        <i class="bi bi-pencil"></i>
                                    </a>

                                    <a href="${pageContext.request.contextPath}/owner/inventory?action=delete&id=${i.inventoryId}"
                                       onclick="return confirm('Bạn có chắc muốn xóa dụng cụ này không?')"
                                       class="btn btn-sm btn-outline-danger"
                                       aria-label="Xóa">
                                        <i class="bi bi-trash"></i>
                                    </a>
                                </td>
                            </tr>
                        </c:forEach>

                        <c:if test="${empty inventories}">
                            <tr>
                                <td colspan="6" class="text-center text-muted">
                                    Không tìm thấy dụng cụ nào
                                </td>
                            </tr>
                        </c:if>
                        </tbody>
                    </table>
                </div>

                <nav class="mt-4">
                    <ul class="pagination justify-content-center">
                        <c:forEach begin="1" end="${totalPages}" var="p">
                            <li class="page-item ${p==currentPage?'active':''}">
                                <a class="page-link"
                                   href="${pageContext.request.contextPath}/owner/inventory?page=${p}&keyword=${keyword}">
                                    ${p}
                                </a>
                            </li>
                        </c:forEach>
                    </ul>
                </nav>
            </div>
        </div>
    </div>

    <%@ include file="../layout/footer.jsp"%>

</div>
