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
        <c:set var="pageTitle" value="Chi Tiết Địa Điểm" />
        <%@ include file="../layout/page-header.jsp" %>

        <%-- ACTIONS --%>
        <div class="d-flex justify-content-end gap-2 mb-4">
            <a href="${pageContext.request.contextPath}/owner/courts/list/${requestScope.facility.facilityId}" class="btn btn-accent">
                <i class="bi bi-grid-3x3 me-1"></i> Quản lý sân
            </a>
            <a href="${pageContext.request.contextPath}/owner/prices?facilityId=${requestScope.facility.facilityId}"
               class="btn btn-primary">
                <i class="bi bi-currency-dollar me-1"></i> Cài đặt giá
            </a>
            <a href="${pageContext.request.contextPath}/owner/facility/edit/${requestScope.facility.facilityId}" class="btn btn-warning">
                <i class="bi bi-pencil me-1"></i> Sửa
            </a>
            <a href="${pageContext.request.contextPath}/owner/facility/list" class="btn btn-secondary">
                <i class="bi bi-arrow-left me-1"></i> Quay lại
            </a>
        </div>

        <%-- ALERTS --%>
        <c:if test="${not empty requestScope.error}">
            <div class="alert alert-danger alert-dismissible fade show" role="alert">
                <i class="bi bi-exclamation-circle me-2"></i> ${requestScope.error}
                <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
            </div>
        </c:if>

        <%-- CONTENT --%>
        <div class="row">
            <div class="col-lg-8">
                <div class="card mb-4">
                    <div class="card-header bg-white">
                        <h5 class="mb-0 text-emerald">Thông tin chung</h5>
                    </div>
                    <div class="card-body">
                        <div class="row mb-4">
                            <div class="col-12">
                                <label class="form-label text-muted small uppercase fw-bold">Tên Địa Điểm</label>
                                <h4 class="fw-bold text-emerald mb-0">${requestScope.facility.name}</h4>
                            </div>
                        </div>

                        <div class="row mb-4">
                            <div class="col-md-6">
                                <label class="form-label text-muted small uppercase fw-bold">Tỉnh / Thành phố</label>
                                <p class="mb-0">${not empty requestScope.facility.province ? requestScope.facility.province : 'N/A'}</p>
                            </div>
                            <div class="col-md-6">
                                <label class="form-label text-muted small uppercase fw-bold">Quận / Huyện</label>
                                <p class="mb-0">${not empty requestScope.facility.district ? requestScope.facility.district : 'N/A'}</p>
                            </div>
                        </div>

                        <div class="row mb-4">
                            <div class="col-12">
                                <label class="form-label text-muted small uppercase fw-bold">Địa Chỉ</label>
                                <p class="mb-0"><i class="bi bi-geo-alt me-1 text-danger"></i> ${requestScope.facility.address}</p>
                            </div>
                        </div>

                        <div class="row">
                            <div class="col-md-6">
                                <div class="p-3 bg-lime-light rounded-3 border border-success border-opacity-10">
                                    <label class="form-label text-success small uppercase fw-bold">Giờ Mở Cửa</label>
                                    <h5 class="mb-0 fw-bold">
                                        <c:choose>
                                            <c:when test="${not empty requestScope.facility.openTime}">${requestScope.facility.openTime}</c:when>
                                            <c:otherwise>-</c:otherwise>
                                        </c:choose>
                                    </h5>
                                </div>
                            </div>
                            <div class="col-md-6">
                                <div class="p-3 bg-light rounded-3 border">
                                    <label class="form-label text-danger small uppercase fw-bold">Giờ Đóng Cửa</label>
                                    <h5 class="mb-0 fw-bold">
                                        <c:choose>
                                            <c:when test="${not empty requestScope.facility.closeTime}">${requestScope.facility.closeTime}</c:when>
                                            <c:otherwise>-</c:otherwise>
                                        </c:choose>
                                    </h5>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="card mb-4">
                    <div class="card-header">
                        <h5 class="mb-0">Mô Tả</h5>
                    </div>
                    <div class="card-body">
                        <c:choose>
                            <c:when test="${not empty requestScope.facility.description}">
                                <div class="text-muted" style="white-space: pre-line;">${requestScope.facility.description}</div>
                            </c:when>
                            <c:otherwise>
                                <p class="text-muted italic">Không có mô tả</p>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </div>
            </div>

            <div class="col-lg-4">
                <div class="card mb-4">
                    <div class="card-header">
                        <h5 class="mb-0">Ảnh Thu Nhỏ</h5>
                    </div>
                    <div class="card-body p-2">
                        <c:choose>
                            <c:when test="${not empty requestScope.thumbnailImage}">
                                <img src="${pageContext.request.contextPath}/uploads/${requestScope.thumbnailImage.imagePath}"
                                     alt="${requestScope.facility.name}" class="img-fluid rounded shadow-sm w-100">
                            </c:when>
                            <c:otherwise>
                                <div class="bg-light rounded d-flex align-items-center justify-content-center" style="height: 200px;">
                                    <i class="bi bi-image" style="font-size: 3rem; color: #d1d5db;"></i>
                                </div>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </div>

                <div class="card mb-4">
                    <div class="card-header">
                        <h5 class="mb-0">Ảnh Giới Thiệu</h5>
                    </div>
                    <div class="card-body">
                        <c:choose>
                            <c:when test="${not empty requestScope.galleryImages}">
                                <div class="row g-2">
                                    <c:forEach items="${requestScope.galleryImages}" var="image">
                                        <div class="col-6">
                                            <img src="${pageContext.request.contextPath}/uploads/${image.imagePath}"
                                                 alt="Gallery image" class="img-fluid rounded border shadow-sm"
                                                 style="height: 100px; width: 100%; object-fit: cover;">
                                        </div>
                                    </c:forEach>
                                </div>
                            </c:when>
                            <c:otherwise>
                                <p class="text-muted text-center italic">Không có ảnh khác</p>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </div>

                <div class="card">
                    <div class="card-header d-flex justify-content-between align-items-center">
                        <h5 class="mb-0">Đánh giá</h5>
                        <span class="badge bg-accent text-emerald">${not empty requestScope.reviews ? requestScope.reviews.size() : 0}</span>
                    </div>
                    <div class="card-body">
                        <c:choose>
                            <c:when test="${not empty requestScope.reviews}">
                                <div class="reviews-list">
                                    <c:forEach items="${requestScope.reviews}" var="review">
                                        <div class="review-item mb-3 pb-3 border-bottom">
                                            <div class="d-flex justify-content-between align-items-center mb-1">
                                                <span class="fw-bold small text-emerald">Customer</span>
                                                <div class="text-warning">
                                                    <c:forEach var="i" begin="1" end="${review.rating}">
                                                        <i class="bi bi-star-fill small"></i>
                                                    </c:forEach>
                                                </div>
                                            </div>
                                            <p class="mb-0 small text-muted italic">"${review.comment}"</p>
                                        </div>
                                    </c:forEach>
                                </div>
                            </c:when>
                            <c:otherwise>
                                <p class="text-muted text-center italic small">Chưa có đánh giá nào</p>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <%@ include file="../layout/footer.jsp" %>
</div>
