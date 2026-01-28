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
        <c:set var="pageTitle" value="Courts - ${requestScope.facility.name}" />
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

        <%-- SEARCH + ADD --%>
        <div class="card mb-4">
            <div class="card-body">
                <div class="d-flex justify-content-between align-items-end gap-3">

                    <%-- SEARCH (LEFT) --%>
                    <form method="GET"
                          action="${pageContext.request.contextPath}/admin/court/list"
                          class="d-flex gap-2 flex-grow-1">

                        <input type="hidden" name="facilityId" value="${requestScope.facility.facilityId}" />

                        <div class="flex-grow-1">
                            <label class="form-label">Search Court</label>
                            <label>
                                <input type="text"
                                       name="keyword"
                                       class="form-control"
                                       placeholder="Search by court name"
                                       value="${param.keyword}">
                            </label>
                        </div>

                        <div class="d-flex align-items-end gap-2">
                            <button type="submit" class="btn btn-primary">
                                <i class="bi bi-search"></i>
                            </button>
                            <a href="${pageContext.request.contextPath}/admin/courts/list?facilityId=${facility.facilityId}"
                               class="btn btn-outline-secondary">
                                <i class="bi bi-arrow-counterclockwise"></i>
                            </a>
                        </div>
                    </form>

                    <%-- ADD COURT (RIGHT - MODAL) --%>
                    <div class="d-flex align-items-end">
                        <button class="btn btn-primary"
                                data-bs-toggle="modal"
                                data-bs-target="#courtModal">
                            <i class="bi bi-plus-circle"></i> Add Court
                        </button>
                    </div>

                </div>
            </div>
        </div>

        <%-- TABLE --%>
        <div class="card">
            <div class="card-header bg-white">
                <h5 class="mb-0">
                    Courts
                    <small class="text-muted">(${requestScope.totalRecords} total)</small>
                </h5>
            </div>

            <div class="card-body">
                <c:choose>
                    <c:when test="${empty requestScope.courts}">
                        <div class="text-center py-5">
                            <i class="bi bi-inbox fs-1 text-muted"></i>
                            <p class="text-muted mt-3">No courts found</p>
                        </div>
                    </c:when>

                    <c:otherwise>
                        <div class="table-responsive">
                            <table class="table table-hover mb-0">
                                <thead>
                                <tr>
                                    <th>Court Name</th>
                                    <th>Description</th>
                                    <th>Actions</th>
                                </tr>
                                </thead>
                                <tbody>
                                <c:forEach items="${requestScope.courts}" var="court">
                                    <tr>
                                        <td><strong>${court.courtName}</strong></td>

                                        <td class="text-muted small">
                                            <c:choose>
                                                <c:when test="${not empty court.description}">
                                                    ${fn:length(court.description) > 60
                                                        ? fn:substring(court.description, 0, 60).concat("...")
                                                        : court.description}
                                                </c:when>
                                                <c:otherwise>-</c:otherwise>
                                            </c:choose>
                                        </td>


                                        <td>
                                            <div class="btn-group btn-group-sm">

                                                    <%-- VIEW PRICE CONFIG --%>
                                                <a class="btn btn-outline-primary"
                                                   href="${pageContext.request.contextPath}/admin/courts/view/${court.courtId}">
                                                    <i class="bi bi-eye"></i>
                                                </a>

                                                    <%-- EDIT (MODAL) --%>
                                                        <button class="btn btn-outline-warning"
                                                                data-bs-toggle="modal"
                                                                data-bs-target="#courtModal"
                                                                data-id="${court.courtId}">
                                                            <i class="bi bi-pencil"></i>
                                                        </button>

                                                    <%-- SOFT DELETE --%>
                                                <a class="btn btn-outline-danger"
                                                   href="${pageContext.request.contextPath}/admin/courts/delete/${court.courtId}"
                                                   onclick="return confirm('Delete this court?')">
                                                    <i class="bi bi-trash"></i>
                                                </a>

                                            </div>
                                        </td>
                                    </tr>
                                </c:forEach>
                                </tbody>
                            </table>
                        </div>
                    </c:otherwise>
                </c:choose>
            </div>
        </div>

    </div>

    <%@ include file="../layout/footer.jsp" %>
</div>

<%-- CREATE / EDIT COURT MODAL --%>
<%@ include file="court-modal.jsp" %>
