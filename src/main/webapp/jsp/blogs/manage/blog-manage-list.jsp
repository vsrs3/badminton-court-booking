<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Quản lý bài viết</title>

    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.0/font/bootstrap-icons.css">
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700;800;900&display=swap" rel="stylesheet">

    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/badminton-pro.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/components.css">
</head>
<body>
<div id="app" class="app-container">

    <main id="mainContent" class="main-content">
        <div class="container-fluid px-3 px-sm-5 px-lg-6 mt-4 pb-40">

            <div class="d-flex align-items-center justify-content-between flex-wrap gap-3 mb-3">
                <div>
                    <h3 class="m-0" style="font-weight: 900; letter-spacing: -0.02em; color: #065F46; text-transform: uppercase;">Quản lý bài viết</h3>
                    <div style="color: #64748B; font-weight: 700;">ADMIN / OWNER / STAFF</div>
                </div>
                <div class="d-flex gap-2">
                    <a class="btn btn-outline-secondary" href="${pageContext.request.contextPath}/blogs" style="font-weight: 900; text-transform: uppercase;">Xem public</a>
                    <a class="btn btn-success" href="${pageContext.request.contextPath}/blogs/manage/create" style="font-weight: 900; text-transform: uppercase;">Tạo bài viết</a>
                </div>
            </div>

            <c:if test="${not empty error}">
                <div class="alert alert-danger" role="alert"><c:out value="${error}" /></div>
            </c:if>

            <form class="row g-2 align-items-end mb-3" method="GET" action="${pageContext.request.contextPath}/blogs/manage/list">
                <div class="col-12 col-md-5">
                    <label class="form-label" style="font-weight: 800;">Tìm kiếm</label>
                    <input class="form-control" name="q" value="${fn:escapeXml(param.q)}" placeholder="Tìm theo tiêu đề / tóm tắt" />
                </div>
                <div class="col-6 col-md-2">
                    <label class="form-label" style="font-weight: 800;">Trạng thái</label>
                    <select class="form-select" name="status">
                        <option value="" <c:if test="${empty param.status}">selected</c:if>>Tất cả</option>
                        <option value="DRAFT" <c:if test="${param.status eq 'DRAFT'}">selected</c:if>>DRAFT</option>
                        <option value="PUBLISHED" <c:if test="${param.status eq 'PUBLISHED'}">selected</c:if>>PUBLISHED</option>
                    </select>
                </div>
                <div class="col-6 col-md-2">
                    <label class="form-label" style="font-weight: 800;">Sắp xếp</label>
                    <select class="form-select" name="sortBy">
                        <option value="created_at" <c:if test="${param.sortBy eq 'created_at' || empty param.sortBy}">selected</c:if>>Ngày tạo</option>
                        <option value="published_at" <c:if test="${param.sortBy eq 'published_at'}">selected</c:if>>Ngày đăng</option>
                        <option value="title" <c:if test="${param.sortBy eq 'title'}">selected</c:if>>Tiêu đề</option>
                    </select>
                </div>
                <div class="col-6 col-md-1">
                    <label class="form-label" style="font-weight: 800;">Dir</label>
                    <select class="form-select" name="sortDir">
                        <option value="DESC" <c:if test="${param.sortDir eq 'DESC' || empty param.sortDir}">selected</c:if>>DESC</option>
                        <option value="ASC" <c:if test="${param.sortDir eq 'ASC'}">selected</c:if>>ASC</option>
                    </select>
                </div>
                <div class="col-12 d-flex gap-2">
                    <button class="btn btn-primary" type="submit" style="font-weight: 900; text-transform: uppercase;">Lọc</button>
                    <a class="btn btn-outline-secondary" href="${pageContext.request.contextPath}/blogs/manage/list" style="font-weight: 900; text-transform: uppercase;">Reset</a>
                </div>
            </form>

            <div class="table-responsive" style="border-radius: 1rem; overflow: hidden; border: 1px solid rgba(0,0,0,.06); box-shadow: 0 8px 24px rgba(0,0,0,.06);">
                <table class="table table-hover m-0">
                    <thead class="table-light">
                    <tr>
                        <th>ID</th>
                        <th>Tiêu đề</th>
                        <th>Tác giả</th>
                        <th>Trạng thái</th>
                        <th>Ngày tạo</th>
                        <th style="width: 160px;">Thao tác</th>
                    </tr>
                    </thead>
                    <tbody>
                    <c:forEach var="p" items="${posts}">
                        <tr>
                            <td><c:out value="${p.postId}" /></td>
                            <td style="font-weight: 800;"><c:out value="${p.title}" /></td>
                            <td><c:out value="${p.authorName}" /></td>
                            <td><span class="badge bg-secondary"><c:out value="${p.status}" /></span></td>
                            <td><c:out value="${p.createdAt}" /></td>
                            <td>
                                <a class="btn btn-sm btn-success" href="${pageContext.request.contextPath}/blogs/manage/edit?id=${p.postId}">Sửa</a>
                                <a class="btn btn-sm btn-outline-danger" href="${pageContext.request.contextPath}/blogs/manage/delete?id=${p.postId}" onclick="return confirm('Xóa bài viết?');">Xóa</a>
                            </td>
                        </tr>
                    </c:forEach>
                    <c:if test="${empty posts}">
                        <tr><td colspan="6" class="text-center py-4" style="color: #64748B; font-weight: 700;">Không có dữ liệu</td></tr>
                    </c:if>
                    </tbody>
                </table>
            </div>

            <c:set var="pageSize" value="${filter.pageSize}" />
            <c:set var="page" value="${filter.page}" />
            <c:set var="totalPages" value="${(total + pageSize - 1) / pageSize}" />

            <nav class="mt-4" aria-label="pagination">
                <ul class="pagination justify-content-center">
                    <li class="page-item <c:if test='${page <= 1}'>disabled</c:if>">
                        <a class="page-link" href="${pageContext.request.contextPath}/blogs/manage/list?q=${fn:escapeXml(param.q)}&status=${fn:escapeXml(param.status)}&sortBy=${fn:escapeXml(param.sortBy)}&sortDir=${fn:escapeXml(param.sortDir)}&page=${page-1}&pageSize=${pageSize}">Trước</a>
                    </li>
                    <li class="page-item disabled"><span class="page-link">${page} / ${totalPages}</span></li>
                    <li class="page-item <c:if test='${page >= totalPages}'>disabled</c:if>">
                        <a class="page-link" href="${pageContext.request.contextPath}/blogs/manage/list?q=${fn:escapeXml(param.q)}&status=${fn:escapeXml(param.status)}&sortBy=${fn:escapeXml(param.sortBy)}&sortDir=${fn:escapeXml(param.sortDir)}&page=${page+1}&pageSize=${pageSize}">Sau</a>
                    </li>
                </ul>
            </nav>

        </div>
    </main>

</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
