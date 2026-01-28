<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<%@ include file="../layout/layout.jsp" %>
<%@ include file="../layout/sidebar.jsp" %>

<div class="main-content">

    <%-- NAVBAR CHUNG (notification + avatar) --%>
    <%@ include file="../layout/header.jsp" %>

    <div class="content-area">

        <%-- PAGE HEADER --%>
        <c:set var="pageTitle" value="My Locations"/>
        <%@ include file="../layout/page-header.jsp" %>


        <%-- ALERTS --%>
        <c:if test="${not empty requestScope.error}">
            <div class="alert alert-danger alert-dismissible fade show">
                <i class="bi bi-exclamation-circle"></i> ${requestScope.error}
                <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
            </div>
        </c:if>

        <c:if test="${not empty requestScope.success}">
            <div class="alert alert-success alert-dismissible fade show">
                <i class="bi bi-check-circle"></i> ${requestScope.success}
                <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
            </div>
        </c:if>

        <%-- SEARCH BOX --%>
        <div class="card mb-4">
            <div class="card-body">
                <div class="d-flex justify-content-between align-items-end gap-3">

                    <%-- SEARCH (LEFT) --%>
                    <form method="GET"
                          action="${pageContext.request.contextPath}/admin/facility/list"
                          class="d-flex gap-2 flex-grow-1">

                        <div class="flex-grow-1">
                            <label class="form-label">Search Location</label>
                            <input type="text"
                                   name="keyword"
                                   class="form-control"
                                   placeholder="Search by name or address"
                                   value="${requestScope.keyword}">
                        </div>

                        <div class="d-flex align-items-end gap-2">
                            <button type="submit" class="btn btn-primary">
                                <i class="bi bi-search"></i> Search
                            </button>
                            <a href="${pageContext.request.contextPath}/admin/facility/list"
                               class="btn btn-outline-secondary">
                                <i class="bi bi-arrow-counterclockwise"></i>
                            </a>
                        </div>
                    </form>

                    <%-- ADD BUTTON (RIGHT) --%>
                    <div class="d-flex align-items-end">
                        <a href="${pageContext.request.contextPath}/admin/facility/create"
                           class="btn btn-primary">
                            <i class="bi bi-plus-circle"></i> Add New Location
                        </a>
                    </div>

                </div>
            </div>
        </div>


        <%-- TABLE --%>
        <div class="card">
            <div class="card-header bg-white">
                <h5 class="mb-0">
                    Locations
                    <small class="text-muted">(${requestScope.totalRecords} total)</small>
                </h5>
            </div>

            <div class="card-body">
                <c:choose>
                    <c:when test="${empty requestScope.facilities}">
                        <div class="text-center py-5">
                            <i class="bi bi-inbox fs-1 text-muted"></i>
                            <p class="text-muted mt-3">No locations found</p>
                        </div>
                    </c:when>

                    <c:otherwise>
                        <div class="table-responsive">
                            <table class="table table-hover mb-0">
                                <thead>
                                <tr>
                                    <th>Location Name</th>
                                    <th>Address</th>
                                    <th>Operating Hours</th>
                                    <th>Actions</th>
                                </tr>
                                </thead>
                                <tbody>
                                <c:forEach items="${requestScope.facilities}" var="facility">
                                    <tr>
                                        <td><strong>${facility.name}</strong></td>
                                        <td class="text-muted small">
                                                ${facility.address}
                                            <c:if test="${not empty facility.district}">
                                                <br>${facility.district}, ${facility.province}
                                            </c:if>
                                        </td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${not empty facility.openTime}">
                                                    ${facility.openTime} – ${facility.closeTime}
                                                </c:when>
                                                <c:otherwise>-</c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td>
                                            <div class="btn-group btn-group-sm">
                                                <a class="btn btn-outline-primary"
                                                   href="${pageContext.request.contextPath}/admin/facility/view/${facility.facilityId}">
                                                    <i class="bi bi-eye"></i>
                                                </a>
                                                <a class="btn btn-outline-warning"
                                                   href="${pageContext.request.contextPath}/admin/facility/edit/${facility.facilityId}">
                                                    <i class="bi bi-pencil"></i>
                                                </a>
                                                <a class="btn btn-outline-danger"
                                                   href="${pageContext.request.contextPath}/admin/facility/delete/${facility.facilityId}"
                                                   onclick="return confirmDelete('Delete this location?')">
                                                    <i class="bi bi-trash"></i>
                                                </a>
                                            </div>
                                        </td>
                                    </tr>
                                </c:forEach>
                                </tbody>
                            </table>
                            <ul class="pagination justify-content-center">

                                <li class="page-item ${currentPage == 1 ? 'disabled' : ''}">
                                    <a class="page-link"
                                       href="${pageContext.request.contextPath}/admin/facility/list?page=${currentPage - 1}&keyword=${param.keyword}">
                                        &laquo;
                                    </a>
                                </li>

                                <c:forEach begin="1" end="${totalPages}" var="p">
                                    <li class="page-item ${p == currentPage ? 'active' : ''}">
                                        <a class="page-link"
                                           href="${pageContext.request.contextPath}/admin/facility/list?page=${p}&keyword=${param.keyword}">
                                                ${p}
                                        </a>
                                    </li>
                                </c:forEach>

                                <li class="page-item ${currentPage == totalPages ? 'disabled' : ''}">
                                    <a class="page-link"
                                       href="${pageContext.request.contextPath}/admin/facility/list?page=${currentPage + 1}&keyword=${param.keyword}">
                                        &raquo;
                                    </a>
                                </li>

                            </ul>

                        </div>
                    </c:otherwise>
                </c:choose>
            </div>
        </div>
    </div>

    <%@ include file="../layout/footer.jsp" %>
</div>
