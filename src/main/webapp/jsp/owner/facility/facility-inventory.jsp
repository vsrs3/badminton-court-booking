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

                <div class="d-flex justify-content-between align-items-center mb-3">
                    <div>
                        <h4 class="fw-bold text-emerald mb-1">Kho đồ</h4>
                        <p class="text-muted mb-0">
                            Danh sách dụng cụ hiện có trong hệ thống
                        </p>
                    </div>
                </div>

                <!-- SEARCH -->
                <form method="get"
                      action="${pageContext.request.contextPath}/owner/facility/inventory/${facilityId}"
                      class="row g-3 mb-4">

                    <div class="col-md-4">
                        <input type="text"
                               name="keyword"
                               value="${keyword}"
                               class="form-control rounded-3"
                               placeholder="Tìm dụng cụ theo tên...">
                    </div>

                    <div class="col-auto">
                        <button class="btn btn-outline-success rounded-3">
                            Tìm kiếm
                        </button>
                    </div>
                </form>

                <div class="table-responsive">
                    <table class="table table-hover align-middle">

                        <thead class="table-light">
                        <tr>
                            <th>ID</th>
                            <th>Tên</th>
                            <th>Thương hiệu</th>
                            <th>Mô tả</th>
                            <th>Giá thuê</th>
                            <th>Trạng thái</th>
                        </tr>
                        </thead>

                        <tbody>

                        <c:choose>

                            <c:when test="${empty inventories}">
                                <tr>
                                    <td colspan="6" class="text-center text-muted">
                                        Không có dụng cụ
                                    </td>
                                </tr>
                            </c:when>

                            <c:otherwise>

                                <c:forEach items="${inventories}" var="i">
                                    <tr>
                                        <td>${i.inventoryId}</td>
                                        <td>${i.name}</td>
                                        <td>${i.brand}</td>
                                        <td>${i.description}</td>
                                        <td>${i.rentalPrice}</td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${i.active}">
                                                    <span class="badge bg-success">Active</span>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="badge bg-secondary">Inactive</span>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                    </tr>
                                </c:forEach>

                            </c:otherwise>

                        </c:choose>

                        </tbody>

                    </table>
                </div>

                <!-- PAGINATION -->
                <nav class="mt-4">
                    <ul class="pagination justify-content-center">

                        <c:forEach begin="1" end="${totalPages}" var="p">
                            <li class="page-item ${p==currentPage?'active':''}">
                                <a class="page-link"
                                   href="${pageContext.request.contextPath}/owner/facility/inventory/${facilityId}?page=${p}&keyword=${keyword}">
                                        ${p}
                                </a>
                            </li>
                        </c:forEach>

                    </ul>
                </nav>

            </div>
        </div>

    </div>

    <%@ include file="../layout/footer.jsp" %>

</div>