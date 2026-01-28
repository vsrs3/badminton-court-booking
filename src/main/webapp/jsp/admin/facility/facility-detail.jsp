<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<%-- LAYOUT INCLUDES --%>
<%@ include file="../layout/layout.jsp" %>
<%@ include file="../layout/sidebar.jsp" %>

<div class="main-content">

    <%-- GLOBAL HEADER (notification + avatar) --%>
    <%@ include file="../layout/header.jsp" %>

    <div class="content-area">

        <%-- PAGE HEADER --%>
        <c:set var="pageTitle" value="Location Details" />
        <%@ include file="../layout/page-header.jsp" %>

        <%-- ACTIONS --%>
        <div class="d-flex justify-content-end gap-2 mb-3">
            <a href="${pageContext.request.contextPath}/admin/facility/edit/${requestScope.facility.facilityId}" class="btn btn-warning">
                <i class="bi bi-pencil"></i> Edit
            </a>
            <a href="${pageContext.request.contextPath}/admin/facility/list" class="btn btn-secondary">
                <i class="bi bi-arrow-left"></i> Back
            </a>
        </div>

        <%-- ALERTS --%>
        <c:if test="${not empty requestScope.error}">
            <div class="alert alert-danger alert-dismissible fade show" role="alert">
                <i class="bi bi-exclamation-circle"></i> ${requestScope.error}
                <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
            </div>
        </c:if>

        <%-- CONTENT --%>
        <div class="row">
            <div class="col-lg-8">
                <div class="card mb-4">
                    <div class="card-header">
                        <h5 class="mb-0">Basic Information</h5>
                    </div>
                    <div class="card-body">
                        <div class="row mb-3">
                            <div class="col-md-12">
                                <label class="form-label fw-bold">Location Name</label>
                                <p class="form-control-plaintext">
                                    <strong>${requestScope.facility.name}</strong>
                                </p>
                            </div>
                        </div>

                        <div class="row mb-3">
                            <div class="col-md-6">
                                <label class="form-label fw-bold">Province</label>
                                <p class="form-control-plaintext">
                                    ${not empty requestScope.facility.province ? requestScope.facility.province : 'N/A'}
                                </p>
                            </div>
                            <div class="col-md-6">
                                <label class="form-label fw-bold">District</label>
                                <p class="form-control-plaintext">
                                    ${not empty requestScope.facility.district ? requestScope.facility.district : 'N/A'}
                                </p>
                            </div>
                        </div>

                        <div class="row mb-3">
                            <div class="col-md-12">
                                <label class="form-label fw-bold">Address</label>
                                <p class="form-control-plaintext">
                                    ${requestScope.facility.address}
                                </p>
                            </div>
                        </div>

                        <div class="row">
                            <div class="col-md-6">
                                <label class="form-label text-success">Opening Time</label>
                                <p class="form-control-plaintext">
                                    <c:if test="${not empty requestScope.facility.openTime}">
                                        ${requestScope.facility.openTime}
                                    </c:if>
                                    <c:if test="${empty requestScope.facility.openTime}">
                                        <span class="text-muted">-</span>
                                    </c:if>
                                </p>
                            </div>
                            <div class="col-md-6">
                                <label class="form-label text-danger">Closing Time</label>
                                <p class="form-control-plaintext">
                                    <c:if test="${not empty requestScope.facility.closeTime}">
                                        ${requestScope.facility.closeTime}
                                    </c:if>
                                    <c:if test="${empty requestScope.facility.closeTime}">
                                        <span class="text-muted">-</span>
                                    </c:if>
                                </p>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="card mb-4">
                    <div class="card-header">
                        <h5 class="mb-0">Description</h5>
                    </div>
                    <div class="card-body">
                        <c:if test="${not empty requestScope.facility.description}">
                            <p>${requestScope.facility.description}</p>
                        </c:if>
                        <c:if test="${empty requestScope.facility.description}">
                            <p class="text-muted">No description available</p>
                        </c:if>
                    </div>
                </div>

                <div class="card">
                    <div class="card-header d-flex justify-content-between align-items-center">
                        <h5 class="mb-0">Courts</h5>
                        <a href="${pageContext.request.contextPath}/admin/courts/list/${requestScope.facility.facilityId}"
                           class="btn btn-sm btn-primary">
                            <i class="bi bi-plus-circle"></i> Manage Courts
                        </a>
                    </div>
                    <div class="card-body">
                        <p class="text-muted">Click the "Manage Courts" button to add or edit courts for this location.</p>
                    </div>
                </div>
            </div>

            <div class="col-lg-4">
                <div class="card mb-4">
                    <div class="card-header">
                        <h5 class="mb-0">Featured Image</h5>
                    </div>
                    <div class="card-body">
                        <c:if test="${not empty requestScope.thumbnailImage}">
                            <img src="${pageContext.request.contextPath}/${requestScope.thumbnailImage.imagePath}"
                                 alt="${requestScope.facility.name}" class="img-fluid rounded">
                        </c:if>
                        <c:if test="${empty requestScope.thumbnailImage}">
                            <div class="bg-light rounded d-flex align-items-center justify-content-center" style="height: 200px;">
                                <i class="bi bi-image" style="font-size: 3rem; color: #d1d5db;"></i>
                            </div>
                        </c:if>
                    </div>
                </div>

                <div class="card mb-4">
                    <div class="card-header">
                        <h5 class="mb-0">Gallery</h5>
                    </div>
                    <div class="card-body">
                        <c:choose>
                            <c:when test="${not empty requestScope.galleryImages}">
                                <div class="row g-2">
                                    <c:forEach items="${requestScope.galleryImages}" var="image">
                                        <div class="col-6">
                                            <img src="${pageContext.request.contextPath}/${image.imagePath}"
                                                 alt="Gallery image" class="img-fluid rounded"
                                                 style="height: 120px; object-fit: cover;">
                                        </div>
                                    </c:forEach>
                                </div>
                            </c:when>
                            <c:otherwise>
                                <p class="text-muted text-center">No gallery images</p>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </div>

                <div class="card">
                    <div class="card-header">
                        <h5 class="mb-0">
                            Reviews
                            <small class="text-muted">(Read-only)</small>
                        </h5>
                    </div>
                    <div class="card-body">
                        <c:choose>
                            <c:when test="${not empty requestScope.reviews}">
                                <div class="reviews-list">
                                    <c:forEach items="${requestScope.reviews}" var="review">
                                        <div class="review-item mb-3 pb-3 border-bottom">
                                            <div class="d-flex justify-content-between align-items-start mb-2">
                                                <div>
                                                    <strong>Reviewer</strong>
                                                    <br>
                                                    <small class="text-muted">Rating:
                                                        <c:forEach var="i" begin="1" end="${review.rating}">
                                                            <i class="bi bi-star-fill" style="color: #fbbf24;"></i>
                                                        </c:forEach>
                                                    </small>
                                                </div>
                                            </div>
                                            <p class="mb-0 small">${review.comment}</p>
                                        </div>
                                    </c:forEach>
                                </div>
                            </c:when>
                            <c:otherwise>
                                <p class="text-muted text-center">No reviews yet</p>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <%@ include file="../layout/footer.jsp" %>
</div>

<style>
    .form-control-plaintext {
        padding: 0;
        border: none;
    }

    .review-item {
        padding-bottom: 1rem;
    }

    .review-item:last-child {
        border-bottom: none !important;
    }
</style>
