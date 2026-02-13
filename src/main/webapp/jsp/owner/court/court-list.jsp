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
        <c:set var="pageTitle" value="Courts - ${requestScope.facility.name}"/>
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

        <%--  ADD --%>
        <div class="card mb-4">
            <div class="card-body">
                <div class="d-flex flex-column flex-md-row justify-content-between align-items-end gap-3">

                    <div class="d-flex align-items-end">
                        <button class="btn btn-accent"
                                data-bs-toggle="modal"
                                data-bs-target="#courtModal">
                            <i class="bi bi-plus-circle me-1"></i> Thêm sân mới
                        </button>
                    </div>

                </div>
            </div>
        </div>

        <%-- TABLE --%>
        <div class="card">
            <div class="card-header">
                <h5 class="mb-0 d-flex align-items-center">
                    Courts
                    <small class="text-muted ms-2">(${requestScope.totalRecords} total)</small>
                </h5>
            </div>

            <div class="card-body p-0">
                <c:choose>
                    <c:when test="${empty requestScope.courts}">
                        <div class="text-center py-5">
                            <i class="bi bi-inbox fs-1 text-muted"></i>
                            <p class="text-muted mt-3 italic">Chưa có sân nào được tạo</p>
                        </div>
                    </c:when>

                    <c:otherwise>
                        <div class="table-responsive">
                            <table class="table table-hover mb-0">
                                <thead>
                                <tr>
                                    <th class="px-4">Tên sân</th>
                                    <th class="px-4 text-center">Loại sân</th>
                                    <th class="px-4 text-end">Hành động</th>
                                </tr>
                                </thead>
                                <tbody>
                                <c:forEach items="${requestScope.courts}" var="court">
                                    <tr>
                                        <td class="px-4 fw-medium text-emerald">${court.courtName}</td>
                                        <td class="px-4 text-center">
                                            <span class="badge ${court.courtTypeCode == 'VIP' ? 'bg-warning text-dark' : 'bg-info text-white'}">
                                                ${court.courtTypeCode}
                                            </span>
                                        </td>
                                        <td class="px-4 text-end">
                                            <div class="btn-group btn-group-sm">
                                                <button class="btn btn-outline-warning"
                                                        data-bs-toggle="modal"
                                                        data-bs-target="#courtModal"
                                                        data-id="${court.courtId}"
                                                        data-name="${court.courtName}"
                                                        data-type="${court.courtTypeCode}">
                                                    <i class="bi bi-pencil"></i>
                                                </button>
                                                <a class="btn btn-outline-danger"
                                                   href="${pageContext.request.contextPath}/owner/courts/delete/${court.courtId}"
                                                   onclick="return confirmDelete('Xóa sân này?')">
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
