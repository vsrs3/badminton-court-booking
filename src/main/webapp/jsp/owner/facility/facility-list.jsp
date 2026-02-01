<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<%@ include file="../layout/layout.jsp" %>
<%@ include file="../layout/sidebar.jsp" %>

<div class="main-content">

    <%-- NAVBAR CHUNG --%>
    <%@ include file="../layout/header.jsp" %>

    <div class="content-area">

        <%-- PAGE HEADER --%>
        <c:set var="pageTitle" value="My Locations"/>
        <%@ include file="../layout/page-header.jsp" %>

        <%-- ALERTS --%>
        <c:if test="${not empty requestScope.errors}">
            <div class="alert alert-danger mb-4">
                <i class="bi bi-exclamation-circle me-2"></i>
                <ul class="mb-0">
                    <c:forEach var="err" items="${requestScope.errors}">
                        <li>${err}</li>
                    </c:forEach>
                </ul>
            </div>
        </c:if>

        <c:if test="${not empty requestScope.success}">
            <div class="alert alert-success alert-dismissible fade show mb-4">
                <i class="bi bi-check-circle me-2"></i> ${requestScope.success}
                <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
            </div>
        </c:if>

        <%-- SEARCH BOX --%>
        <div class="card mb-4">
            <div class="card-body">
                <div class="d-flex flex-column flex-md-row justify-content-between align-items-end gap-3">

                    <form method="GET" action="${pageContext.request.contextPath}/owner/facility/list"
                          class="d-flex gap-3 flex-grow-1 flex-wrap">
                        <div class="flex-grow-1">
                            <label class="form-label fw-medium text-muted small">Tìm kiếm</label>
                            <input type="text" name="keyword" class="form-control"
                                   placeholder="Nhập tên địa điểm hoặc địa chỉ"
                                   value="${requestScope.keyword}">
                        </div>

                        <div class="d-flex align-items-end gap-2">
                            <button type="submit" class="btn btn-accent d-flex align-items-center gap-2">
                                <i class="bi bi-search"></i> Tìm
                            </button>

                            <a href="${pageContext.request.contextPath}/owner/facility/list"
                               class="btn btn-outline-secondary d-flex align-items-center gap-2">
                                <i class="bi bi-arrow-counterclockwise"></i>
                            </a>
                        </div>
                    </form>

                    <a href="${pageContext.request.contextPath}/owner/facility/create"
                       class="btn btn-accent d-flex align-items-center gap-2 ms-md-3">
                        <i class="bi bi-plus-circle"></i> Thêm địa điểm mới
                    </a>

                </div>
            </div>
        </div>

        <%-- TABLE --%>
        <div class="card">
            <div class="card-header">
                <h5 class="mb-0 d-flex align-items-center">
                    Locations
                    <small class="text-muted ms-2">(${requestScope.totalRecords} total)</small>
                </h5>
            </div>

            <div class="card-body p-0">
                <c:choose>
                    <c:when test="${empty requestScope.facilities}">
                        <div class="text-center py-5 text-muted">
                            <i class="bi bi-inbox fs-1"></i>
                            <p class="mt-3">Không tìm thấy địa điểm</p>
                        </div>
                    </c:when>

                    <c:otherwise>
                        <div class="table-responsive">
                            <table class="table table-hover mb-0">
                                <thead>
                                <tr>
                                    <th class="px-4">Tên địa điểm</th>
                                    <th class="px-4">Địa chỉ</th>
                                    <th class="px-4">Giờ hoạt động</th>
                                    <th class="px-4 text-end">Hành động</th>
                                </tr>
                                </thead>
                                <tbody>
                                <c:forEach items="${requestScope.facilities}" var="facility">
                                    <tr>
                                        <td class="px-4 fw-medium text-emerald">${facility.name}</td>
                                        <td class="px-4 text-muted small">
                                            <p class="mb-0">${addressMap[facility.facilityId]}</p>
                                        </td>
                                        <td class="px-4 text-muted">
                                            <c:choose>
                                                <c:when test="${not empty facility.openTime}">
                                                    ${facility.openTime} – ${facility.closeTime}
                                                </c:when>
                                                <c:otherwise>-</c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td class="px-4 text-end">
                                            <div class="btn-group btn-group-sm">
                                                <a class="btn btn-outline-secondary"
                                                   href="${pageContext.request.contextPath}/owner/facility/view/${facility.facilityId}">
                                                    <i class="bi bi-eye"></i>
                                                </a>
                                                <a class="btn btn-outline-warning"
                                                   href="${pageContext.request.contextPath}/owner/facility/edit/${facility.facilityId}">
                                                    <i class="bi bi-pencil"></i>
                                                </a>
                                                <a class="btn btn-outline-danger"
                                                   href="${pageContext.request.contextPath}/owner/facility/delete/${facility.facilityId}"
                                                   onclick="return confirmDelete('Bạn có chắc chắn muốn xóa địa điểm này?');">
                                                    <i class="bi bi-trash"></i>
                                                </a>
                                            </div>
                                        </td>
                                    </tr>
                                </c:forEach>
                                </tbody>
                            </table>

                                <%-- PAGINATION --%>
                            <div class="d-flex justify-content-center py-4">
                                <ul class="pagination mb-0">
                                    <li class="page-item ${currentPage == 1 ? 'disabled' : ''}">
                                        <a class="page-link" href="?page=${currentPage-1}&keyword=${param.keyword}">&laquo;</a>
                                    </li>

                                    <c:forEach begin="1" end="${totalPages}" var="p">
                                        <li class="page-item ${p == currentPage ? 'active' : ''}">
                                            <a class="page-link" href="?page=${p}&keyword=${param.keyword}">${p}</a>
                                        </li>
                                    </c:forEach>

                                    <li class="page-item ${currentPage == totalPages ? 'disabled' : ''}">
                                        <a class="page-link" href="?page=${currentPage+1}&keyword=${param.keyword}">&raquo;</a>
                                    </li>
                                </ul>
                            </div>
                        </div>
                    </c:otherwise>
                </c:choose>
            </div>
        </div>

    </div>

    <%@ include file="../layout/footer.jsp" %>
</div>