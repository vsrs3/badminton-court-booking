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
        <c:choose>
            <c:when test="${requestScope.isEdit}">
                <c:set var="pageTitle" value="Edit Location"/>
            </c:when>
            <c:otherwise>
                <c:set var="pageTitle" value="Create New Location"/>
            </c:otherwise>
        </c:choose>
        <%@ include file="../layout/page-header.jsp" %>

        <%-- ACTIONS --%>
        <div class="d-flex justify-content-end mb-3">
            <a href="${pageContext.request.contextPath}/owner/facility/list" class="btn btn-secondary">
                <i class="bi bi-arrow-left"></i> Back to List
            </a>
        </div>

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

        <%-- FORM CONTENT --%>
        <form method="POST" enctype="multipart/form-data">
            <input type="hidden" name="action" value="${requestScope.isEdit ? 'update' : 'create'}">
            <c:if test="${requestScope.isEdit}">
                <input type="hidden" name="facilityId" value="${requestScope.facility.facilityId}">
            </c:if>

            <div class="row">
                <div class="col-lg-8">
                    <%-- Basic Information --%>
                    <div class="card mb-4">
                        <div class="card-header bg-emerald text-white">
                            <h5 class="mb-0">Basic Information</h5>
                        </div>
                        <div class="card-body">
                            <div class="mb-3">
                                <label for="name" class="form-label">Location Name <span
                                        class="text-danger">*</span></label>
                                <input type="text" class="form-control" id="name" name="name" required
                                       value="${requestScope.facility.name}">
                            </div>

                            <div class="row">
                                <div class="col-md-4">
                                    <div class="mb-3">
                                        <label for="province" class="form-label">Province</label>
                                        <input type="text" class="form-control" id="province" name="province"
                                               value="${requestScope.facility.province}">
                                    </div>
                                </div>
                                <div class="col-md-4">
                                    <div class="mb-3">
                                        <label for="district" class="form-label">District</label>
                                        <input type="text" class="form-control" id="district" name="district"
                                               value="${requestScope.facility.district}">
                                    </div>
                                </div>
                                <div class="col-md-4">
                                    <div class="mb-3">
                                        <label for="ward" class="form-label">Ward</label>
                                        <input type="text" class="form-control" id="ward" name="ward"
                                               value="${requestScope.facility.ward}">
                                    </div>
                                </div>
                            </div>

                            <div class="mb-3">
                                <label class="form-label">Address *</label>
                                <div class="input-group">
                                    <input type="text"
                                           class="form-control"
                                           id="address"
                                           name="address"
                                           required
                                           value="${requestScope.facility.address}">
                                    <button type="button" class="btn btn-outline-secondary" onclick="openMapModal()">
                                        <i class="bi bi-geo-alt"></i>
                                    </button>
                                </div>
                            </div>

                            <input type="hidden" id="latitude" name="latitude"
                                   value="${requestScope.facility.latitude}">
                            <input type="hidden" id="longitude" name="longitude"
                                   value="${requestScope.facility.longitude}">

                            <div class="row">
                                <div class="col-md-6">
                                    <div class="mb-3">
                                        <label for="openTime" class="form-label">Opening Time <span class="text-danger">*</span></label>
                                        <input type="time" class="form-control" id="openTime" name="openTime" required
                                               value="${requestScope.openTimeFormatted}">
                                    </div>
                                </div>
                                <div class="col-md-6">
                                    <div class="mb-3">
                                        <label for="closeTime" class="form-label">Closing Time <span
                                                class="text-danger">*</span></label>
                                        <input type="time" class="form-control" id="closeTime" name="closeTime" required
                                               value="${requestScope.closeTimeFormatted}">
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>

                    <%-- Description --%>
                    <div class="card mb-4">
                        <div class="card-header bg-emerald text-white">
                            <h5 class="mb-0">Description</h5>
                        </div>
                        <div class="card-body">
                            <div class="mb-0">
                                <label for="description" class="form-label">Description</label>
                                <textarea class="form-control" id="description" name="description" rows="5"
                                          placeholder="Enter location description, rules, facilities, etc.">${requestScope.facility.description}</textarea>
                                <small class="text-muted">Provide details about your facility</small>
                            </div>
                        </div>
                    </div>
                </div>

                <%-- Side Actions --%>
                <div class="col-lg-4">
                    <%-- Thumbnail Image--%>
                    <div class="card mb-4">
                        <div class="card-header">
                            <h5 class="mb-0">Thumbnail</h5>
                        </div>
                        <div class="card-body text-center">
                            <div class="mb-3">
                                <c:choose>
                                    <c:when test="${requestScope.isEdit && not empty requestScope.thumbnailImage}">
                                        <img src="${pageContext.request.contextPath}/uploads/${requestScope.thumbnailImage.imagePath}"
                                             class="img-fluid rounded border" id="thumbnailPreview"
                                             style="max-height: 200px; width: 100%; object-fit: cover;">
                                    </c:when>
                                    <c:otherwise>
                                        <div class="bg-light rounded d-flex align-items-center justify-content-center"
                                             style="height: 200px;">
                                            <i class="bi bi-image" style="font-size: 3rem; color: #d1d5db;"></i>
                                        </div>
                                    </c:otherwise>
                                </c:choose>
                            </div>
                            <input type="file" class="form-control" name="thumbnail" accept="image/*"
                                   onchange="previewThumbnail(this)">
                            <small class="text-muted d-block mt-2">JPG, PNG – max 2MB</small>
                        </div>
                    </div>

                    <%-- Gallery--%>
                    <div class="card mb-4">
                        <div class="card-header">
                            <h5 class="mb-0">Gallery</h5>
                        </div>
                        <div class="card-body">
                            <input type="hidden" name="deletedIds" id="deletedIds" value="">
                            <input type="file" class="form-control mb-3" name="gallery" accept="image/*" multiple
                                   onchange="previewGallery(this)">

                            <c:if test="${not empty requestScope.galleryImages}">
                                <div class="row g-2 mb-3" id="oldGallery">
                                    <c:forEach items="${requestScope.galleryImages}" var="img">
                                        <div class="col-4 position-relative gallery-item-container"
                                             id="img-container-${img.imageId}">
                                            <img src="${pageContext.request.contextPath}/uploads/${img.imagePath}"
                                                 class="img-fluid rounded border"
                                                 style="height: 80px; width: 100%; object-fit: cover;">
                                            <button type="button"
                                                    class="btn btn-danger btn-sm position-absolute top-0 end-0 p-1"
                                                    style="line-height: 1; border-radius: 50%;"
                                                    onclick="markForDelete('${img.imageId}')">
                                                <i class="bi bi-x"></i>
                                            </button>
                                        </div>
                                    </c:forEach>
                                </div>
                            </c:if>

                            <div class="row g-2" id="newGalleryPreview"></div>
                            <small class="text-muted d-block mt-2">Upload multiple images for your location.</small>
                        </div>
                    </div>

                    <div class="card">
                        <div class="card-body">
                            <button type="submit" class="btn btn-accent w-100 mb-2">
                                <i class="bi bi-check-circle me-1"></i>
                                <c:choose>
                                    <c:when test="${requestScope.isEdit}">Update Location</c:when>
                                    <c:otherwise>Create Location</c:otherwise>
                                </c:choose>
                            </button>
                            <a href="${pageContext.request.contextPath}/owner/facility/list"
                               class="btn btn-outline-secondary w-100">
                                <i class="bi bi-x-circle me-1"></i> Cancel
                            </a>
                        </div>
                    </div>
                </div>
            </div>
        </form>
    </div>

    <%@ include file="../layout/footer.jsp" %>

    <script src="https://unpkg.com/leaflet/dist/leaflet.js"></script>
    <script src="https://unpkg.com/leaflet-control-geocoder/dist/Control.Geocoder.js"></script>
    <script src="${pageContext.request.contextPath}/assets/js/facility-form.js"></script>
</div>

<%-- MAP MODAL --%>
<%@ include file="../../common/map-modal.jsp" %>
