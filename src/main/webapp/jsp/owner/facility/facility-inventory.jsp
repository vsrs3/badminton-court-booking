<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<%@ include file="../layout/layout.jsp" %>
<%@ include file="../layout/sidebar.jsp" %>

<div class="main-content">

    <%@ include file="../layout/header.jsp" %>

    <div class="content-area">

        <%@ include file="../layout/page-header.jsp" %>

        <!-- ======================== -->
        <!-- KHO ĐỒ CỦA SÂN -->
        <!-- ======================== -->

        <div class="card mb-4 shadow-sm border-0 rounded-4">
            <div class="card-body p-4">

                <!-- TITLE + SEARCH -->
                <div class="d-flex justify-content-between align-items-center mb-4">

                    <div>
                        <h4 class="fw-bold text-emerald mb-1">
                            Kho đồ của sân
                        </h4>
                        <p class="text-muted small mb-0">
                            Quản lý các dụng cụ thuộc sân này
                        </p>
                    </div>

                    <form method="get"
                          action="${pageContext.request.contextPath}/owner/facility/inventory/${facilityId}"
                          class="d-flex gap-2">

                        <input type="text"
                               name="keyword"
                               value="${param.keyword}"
                               class="form-control rounded-3"
                               placeholder="Tìm kiếm dụng cụ...">

                        <button class="btn btn-accent rounded-3">
                            <i class="bi bi-search"></i>
                        </button>
                    </form>

                </div>

                <!-- TABLE -->
                <div class="table-responsive">
                    <table class="table align-middle table-hover">
                        <thead class="table-light">
                        <tr>
                            <th>ID</th>
                            <th>Tên</th>
                            <th>Thương hiệu</th>
                            <th>Giá thuê</th>
                            <th>Sân</th>
                            <th class="text-end">Hành động</th>
                        </tr>
                        </thead>

                        <tbody>
                        <c:choose>
                            <c:when test="${empty inventories}">
                                <tr>
                                    <td colspan="6" class="text-center text-muted py-4">
                                        <i class="bi bi-inbox fs-4 d-block mb-2"></i>
                                        Không có dụng cụ nào
                                    </td>
                                </tr>
                            </c:when>

                            <c:otherwise>
                                <c:forEach items="${inventories}" var="i">
                                    <tr>
                                        <td>${i.inventoryId}</td>
                                        <td class="fw-semibold">${i.name}</td>
                                        <td>${i.brand}</td>
                                        <td class="text-success fw-bold">
                                            ${i.rentalPrice}
                                        </td>
                                        <td>
                                            <span class="badge bg-success-subtle text-success px-3 py-2 rounded-pill">
                                                ${i.courtName}
                                            </span>
                                        </td>
                                        <td class="text-end">

                                            <form method="post" class="d-inline">
                                                <input type="hidden" name="action" value="remove"/>
                                                <input type="hidden" name="facilityId" value="${facilityId}"/>
                                                <input type="hidden" name="inventoryId" value="${i.inventoryId}"/>

                                                <button class="btn btn-sm btn-outline-danger rounded-3"
                                                        onclick="return confirmDelete('Xóa khỏi sân này?')">
                                                    <i class="bi bi-trash"></i>
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

            </div>
        </div>


        <!-- ======================== -->
        <!-- ĐỒ CHƯA CÓ SÂN -->
        <!-- ======================== -->

        <div class="card shadow-sm border-0 rounded-4">
            <div class="card-body p-4">

                <div class="d-flex justify-content-between align-items-center mb-4">

                    <div>
                        <h5 class="fw-bold mb-1">
                            Dụng cụ chưa được gán sân
                        </h5>
                        <p class="text-muted small mb-0">
                            Gán dụng cụ vào sân hiện tại
                        </p>
                    </div>

                    <form method="get"
                          action="${pageContext.request.contextPath}/owner/facility/inventory/${facilityId}"
                          class="d-flex gap-2">

                        <input type="text"
                               name="keywordUn"
                               value="${param.keywordUn}"
                               class="form-control rounded-3"
                               placeholder="Tìm kiếm...">

                        <button class="btn btn-outline-secondary rounded-3">
                            <i class="bi bi-search"></i>
                        </button>
                    </form>

                </div>

                <div class="table-responsive">
                    <table class="table align-middle table-hover">
                        <thead class="table-light">
                        <tr>
                            <th>Tên</th>
                            <th>Thương hiệu</th>
                            <th>Gán vào sân</th>
                        </tr>
                        </thead>

                        <tbody>
                        <c:choose>
                            <c:when test="${empty unassigned}">
                                <tr>
                                    <td colspan="3" class="text-center text-muted py-4">
                                        Không có dụng cụ trống
                                    </td>
                                </tr>
                            </c:when>

                            <c:otherwise>
                                <c:forEach items="${unassigned}" var="i">
                                    <tr>
                                        <td class="fw-semibold">${i.name}</td>
                                        <td>${i.brand}</td>
                                        <td>

                                            <form method="post"
                                                  class="d-flex gap-2 align-items-center">

                                                <input type="hidden" name="action" value="assign"/>
                                                <input type="hidden" name="facilityId" value="${facilityId}"/>
                                                <input type="hidden" name="inventoryId" value="${i.inventoryId}"/>

                                                <select name="courtId"
                                                        class="form-select rounded-3"
                                                        style="max-width:200px;">
                                                    <c:forEach items="${courts}" var="c">
                                                        <option value="${c.courtId}">
                                                            ${c.courtName}
                                                        </option>
                                                    </c:forEach>
                                                </select>

                                                <button class="btn btn-success btn-sm rounded-3">
                                                    <i class="bi bi-plus-circle"></i>
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

            </div>
        </div>

    </div>

    <%@ include file="../layout/footer.jsp" %>
</div>