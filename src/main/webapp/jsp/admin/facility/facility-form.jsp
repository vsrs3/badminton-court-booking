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
            <a href="${pageContext.request.contextPath}/admin/facility/list" class="btn btn-secondary">
                <i class="bi bi-arrow-left"></i> Back to List
            </a>
        </div>

        <%-- ALERTS --%>
        <c:if test="${not empty requestScope.error}">
            <div class="alert alert-danger alert-dismissible fade show" role="alert">
                <i class="bi bi-exclamation-circle"></i> ${requestScope.error}
                <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
            </div>
        </c:if>

        <%-- FORM CONTENT --%>
        <form method="POST" enctype="multipart/form-data" class="form-container">
            <input type="hidden" name="action" value="${requestScope.isEdit ? 'update' : 'create'}">
            <c:if test="${requestScope.isEdit}">
                <input type="hidden" name="facilityId" value="${requestScope.facility.facilityId}">
            </c:if>

            <div class="row">
                <div class="col-lg-8">
                    <%-- Basic Information --%>
                    <div class="card mb-4">
                        <div class="card-header">
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
                                <div class="col-md-6">
                                    <div class="mb-3">
                                        <label for="province" class="form-label">Province</label>
                                        <input type="text" class="form-control" id="province" name="province"
                                               value="${requestScope.facility.province}">
                                    </div>
                                </div>
                                <div class="col-md-6">
                                    <div class="mb-3">
                                        <label for="district" class="form-label">District</label>
                                        <input type="text" class="form-control" id="district" name="district"
                                               value="${requestScope.facility.district}">
                                    </div>
                                </div>
                            </div>

                            <div class="mb-3">
                                <label for="address" class="form-label">Address <span
                                        class="text-danger">*</span></label>
                                <input type="text" class="form-control" id="address" name="address" required
                                       value="${requestScope.facility.address}">
                            </div>

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
                        <div class="card-header">
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

                            <%-- Preview --%>
                            <c:choose>
                                <c:when test="${requestScope.isEdit && not empty requestScope.thumbnailImage}">
                                    <img src="${pageContext.request.contextPath}/${requestScope.thumbnailImage.imagePath}"
                                         class="img-thumbnail"
                                         id="thumbnailPreview">
                                </c:when>
                                <c:otherwise>
                                    <img src="${pageContext.request.contextPath}/assets/images/no-image.jpg"
                                         class="img-thumbnail"
                                         id="thumbnailPreview">
                                </c:otherwise>
                            </c:choose>


                            <%-- File input --%>
                            <input type="file"
                                   class="form-control"
                                   name="thumbnail"
                                   accept="image/*"
                                   onchange="previewThumbnail(this)">

                            <small class="text-muted d-block mt-2">
                                JPG, PNG – max 2MB
                            </small>
                        </div>
                    </div>

                    <%-- Gallery--%>
                    <div class="card-body">

                        <%-- Input ẩn để chứa các ID ảnh cũ cần xóa --%>
                        <input type="hidden" name="deletedIds" id="deletedIds" value="">

                        <%-- Upload Input --%>
                        <input type="file"
                               class="form-control mb-3"
                               name="gallery"
                               accept="image/*"
                               multiple
                               onchange="previewGallery(this)">

                        <div class="row">

                            <%-- PHẦN 1: ẢNH CŨ (Load từ Server) --%>
                            <c:if test="${not empty requestScope.galleryImages}">
                                <div class="col-12">
                                    <p class="text-muted mb-1 small">Ảnh hiện có:</p>
                                    <div class="row g-2" id="oldGallery">

                                        <c:forEach items="${requestScope.galleryImages}" var="img">
                                            <div class="col-3 gallery-item" id="img-container-${img.imageId}">

                                                <img src="${pageContext.request.contextPath}/${img.imagePath}"
                                                     class="img-fluid rounded border"
                                                     style="height: 100px; width: 100%; object-fit: cover;">

                                                <button type="button"
                                                        class="btn-delete-img"
                                                        onclick="markForDelete('${img.imageId}')">
                                                    <i class="bi bi-x-lg"></i></button>
                                            </div>
                                        </c:forEach>

                                    </div>
                                </div>
                            </c:if>

                            <%-- PHẦN 2: ẢNH MỚI (Preview từ JS) --%>
                            <%-- JS sẽ chỉ render ảnh vào trong thẻ div này --%>
                            <div class="col-12 mt-3">
                                <div class="row g-2" id="newGalleryPreview">
                                    <%-- Ảnh mới sẽ hiện ở đây --%>
                                </div>
                            </div>

                        </div>

                        <small class="text-muted d-block mt-2">
                            Có thể chọn nhiều ảnh. Ảnh mới sẽ được thêm vào bộ sưu tập.
                        </small>
                    </div>


                    <div class="card">
                        <div class="card-header">
                            <h5 class="mb-0">Actions</h5>
                        </div>
                        <div class="card-body">
                            <button type="submit" class="btn btn-primary w-100 mb-2">
                                <i class="bi bi-check-circle"></i>
                                <c:choose>
                                    <c:when test="${requestScope.isEdit}">Update Location</c:when>
                                    <c:otherwise>Create Location</c:otherwise>
                                </c:choose>
                            </button>
                            <a href="${pageContext.request.contextPath}/admin/facility/list"
                               class="btn btn-outline-secondary w-100">
                                <i class="bi bi-x-circle"></i> Cancel
                            </a>
                        </div>
                    </div>
                </div>
            </div>
        </form>
    </div>

    <%@ include file="../layout/footer.jsp" %>
</div>

<script>

    // thumbnail preview
    function previewThumbnail(input) {
        if (input.files && input.files[0]) {
            const reader = new FileReader();
            reader.onload = e => {
                document.getElementById('thumbnailPreview').src = e.target.result;
            };
            reader.readAsDataURL(input.files[0]);
        }
    }

    // gallery preview
    function previewGallery(input) {
        // 1. Chỉ target vào khu vực chứa ảnh MỚI
        const preview = document.getElementById('newGalleryPreview');

        // 2. Xóa các ảnh preview cũ (nếu người dùng chọn file lần 2, 3...)
        // Nhưng ảnh server (phần oldGallery) vẫn còn nguyên.
        preview.innerHTML = '';

        if (!input.files || input.files.length === 0) return;

        // Thêm tiêu đề nhỏ nếu có ảnh (tùy chọn)
        // preview.innerHTML = '<p class="text-success small mb-1 w-100">Ảnh mới chọn:</p>';

        Array.from(input.files).forEach(file => {
            if (!file.type.startsWith('image/')) return;

            const reader = new FileReader();
            reader.onload = e => {
                const col = document.createElement('div');
                col.className = 'col-3 animate__animated animate__fadeIn'; // Thêm hiệu ứng nếu muốn

                // QUAN TRỌNG: Dùng dấu cộng chuỗi để tránh lỗi JSP
                col.innerHTML = '<img src="' + e.target.result + '" ' +
                    'class="img-fluid rounded border border-success" ' +
                    'style="height:100px; width: 100%; object-fit:cover;">';

                preview.appendChild(col);
            };
            reader.readAsDataURL(file);
        });
    }


    // Mảng lưu trữ các ID cần xóa
    let deletedImageIds = [];

    function markForDelete(imageId) {
        if (confirm('Bạn có chắc muốn xóa ảnh này khi lưu không?')) {
            // 1. Thêm ID vào mảng
            deletedImageIds.push(imageId);

            // 2. Cập nhật input hidden
            // Kết quả sẽ là chuỗi string: "101,102,105"
            document.getElementById('deletedIds').value = deletedImageIds.join(',');

            // 3. Ẩn ảnh đó trên giao diện (UX)
            const element = document.getElementById('img-container-' + imageId);
            if (element) {
                // Hiệu ứng mờ dần rồi ẩn
                element.style.opacity = '0';
                setTimeout(() => {
                    element.remove(); // Xóa hẳn khỏi DOM
                }, 300);
            }

            console.log("Danh sách ID sẽ xóa:", document.getElementById('deletedIds').value);
        }
    }

</script>


<style>
    .form-container {
        max-width: 1200px;
    }

    .form-label {
        font-weight: 600;
    }

    .text-danger {
        color: #dc2626;
    }

    .form-check {
        padding: 0.5rem 0;
    }

    .form-check-label {
        cursor: pointer;
        margin-left: 0.5rem;
    }

    .form-control, .form-select {
        border: 1px solid #d1d5db;
    }

    .form-control:focus, .form-select:focus {
        border-color: #2563eb;
        box-shadow: 0 0 0 3px rgba(37, 99, 235, 0.1);
    }

    textarea.form-control {
        resize: vertical;
        font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto;
    }

    .gallery-item {
        position: relative; /* Để định vị nút xóa theo khung này */
        overflow: hidden;
    }

    .btn-delete-img {
        position: absolute;
        top: 5px;
        right: 5px;
        background: rgba(220, 38, 38, 0.9); /* Màu đỏ */
        color: white;
        border: none;
        border-radius: 50%;
        width: 24px;
        height: 24px;
        display: flex;
        align-items: center;
        justify-content: center;
        cursor: pointer;
        font-size: 12px;
        transition: all 0.2s;
        z-index: 10;
    }

    .btn-delete-img:hover {
        background: #ef4444;
        transform: scale(1.1);
    }
</style>
