<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>${mode eq 'edit' ? 'Sửa bài viết' : 'Tạo bài viết'}</title>

    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.0/font/bootstrap-icons.css">
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700;800;900&display=swap" rel="stylesheet">

    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/badminton-pro.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/components.css">
</head>
<body>
<div id="app" class="app-container">

    <main id="mainContent" class="main-content">
        <div class="container-fluid px-3 px-sm-5 px-lg-6 mt-4 pb-40" style="max-width: 1100px;">

            <div class="d-flex align-items-center justify-content-between flex-wrap gap-3 mb-3">
                <div>
                    <h3 class="m-0" style="font-weight: 900; letter-spacing: -0.02em; color: #065F46; text-transform: uppercase;">${mode eq 'edit' ? 'Sửa bài viết' : 'Tạo bài viết'}</h3>
                    <div style="color: #64748B; font-weight: 700;">Quản trị /blogs/manage/*</div>
                </div>
                <div class="d-flex gap-2">
                    <a class="btn btn-outline-secondary" href="${pageContext.request.contextPath}/blogs/manage/list" style="font-weight: 900; text-transform: uppercase;">Danh sách</a>
                    <a class="btn btn-outline-secondary" href="${pageContext.request.contextPath}/blogs" style="font-weight: 900; text-transform: uppercase;">Xem bài viết </a>
                </div>
            </div>

            <c:if test="${not empty formError}">
                <div class="alert alert-danger" role="alert"><c:out value="${formError}" /></div>
            </c:if>
            <c:if test="${not empty error}">
                <div class="alert alert-danger" role="alert"><c:out value="${error}" /></div>
            </c:if>

            <form method="POST" action="${pageContext.request.contextPath}/blogs/manage/${mode eq 'edit' ? 'update' : 'create'}" class="p-3 p-md-4" style="background: #fff; border-radius: 1rem; border: 1px solid rgba(0,0,0,.06); box-shadow: 0 8px 24px rgba(0,0,0,.06);">

                <c:if test="${mode eq 'edit'}">
                    <input type="hidden" name="postId" value="${post.postId}" />
                </c:if>

                <div class="row g-3">
                    <div class="col-12">
                        <label class="form-label" style="font-weight: 800;">Tiêu đề *</label>
                        <input class="form-control" name="title" value="${fn:escapeXml(post.title)}" maxlength="200" required />
                    </div>

                    <div class="col-12">
                        <label class="form-label" style="font-weight: 800;">Tóm tắt</label>
                        <textarea class="form-control" name="summary" rows="2" maxlength="500">${fn:escapeXml(post.summary)}</textarea>
                    </div>

                    <div class="col-12 col-md-4">
                        <label class="form-label" style="font-weight: 800;">Trạng thái *</label>
                        <select class="form-select" name="status" required>
                            <option value="DRAFT" <c:if test="${post.status eq 'DRAFT' || empty post.status}">selected</c:if>>DRAFT</option>
                            <option value="PUBLISHED" <c:if test="${post.status eq 'PUBLISHED'}">selected</c:if>>PUBLISHED</option>
                        </select>
                    </div>

                    <div class="col-12">
                        <label class="form-label" style="font-weight: 800;">Nội dung *</label>
                        <textarea class="form-control" name="content" rows="10" required>${fn:escapeXml(post.content)}</textarea>
                    </div>

                    <div class="col-12 d-flex gap-2">
                        <button type="submit" class="btn btn-success" style="font-weight: 900; text-transform: uppercase;">Lưu</button>
                        <c:if test="${mode eq 'edit'}">
                            <a class="btn btn-outline-danger" href="${pageContext.request.contextPath}/blogs/manage/delete?id=${post.postId}" onclick="return confirm('Xóa bài viết?');" style="font-weight: 900; text-transform: uppercase;">Xóa</a>
                        </c:if>
                    </div>
                </div>

            </form>

        </div>
    </main>

</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
