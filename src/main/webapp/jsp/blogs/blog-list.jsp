<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Cộng đồng - Bài viết</title>

    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.0/font/bootstrap-icons.css">
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700;800;900&display=swap" rel="stylesheet">

    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/badminton-pro.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/components.css">
</head>
<body>
<div id="app" class="app-container">

    <jsp:include page="../common/header.jsp" />

    <main id="mainContent" class="main-content">
        <div class="container-fluid px-3 px-sm-5 px-lg-6 mt-4 pb-40">

            <div class="d-flex align-items-center justify-content-between flex-wrap gap-3 mb-3">
                <h3 class="m-0" style="font-weight: 900; letter-spacing: -0.02em; color: #065F46; text-transform: uppercase;">Cộng đồng</h3>

                <c:if test="${isLoggedIn and (currentRole eq 'ADMIN' or currentRole eq 'OWNER' or currentRole eq 'STAFF')}">
                    <a class="btn btn-success" href="${pageContext.request.contextPath}/blogs/manage/list" style="font-weight: 900; text-transform: uppercase; letter-spacing: 0.05em;">
                        Quản lý bài viết
                    </a>
                </c:if>
            </div>

            <form class="row g-2 align-items-end mb-3" method="GET" action="${pageContext.request.contextPath}/blogs">
                <div class="col-12 col-md-6">
                    <label class="form-label" style="font-weight: 800;">Tìm kiếm</label>
                    <input class="form-control" name="q" value="${fn:escapeXml(param.q)}" placeholder="Tìm theo tiêu đề / tóm tắt" />
                </div>
                <div class="col-6 col-md-3">
                    <label class="form-label" style="font-weight: 800;">Sắp xếp</label>
                    <select class="form-select" name="sortBy">
                        <option value="published_at" <c:if test="${param.sortBy eq 'published_at' || empty param.sortBy}">selected</c:if>>Mới nhất</option>
                        <option value="created_at" <c:if test="${param.sortBy eq 'created_at'}">selected</c:if>>Ngày tạo</option>
                        <option value="title" <c:if test="${param.sortBy eq 'title'}">selected</c:if>>Tiêu đề</option>
                    </select>
                </div>
                <div class="col-6 col-md-3">
                    <label class="form-label" style="font-weight: 800;">Thứ tự</label>
                    <select class="form-select" name="sortDir">
                        <option value="DESC" <c:if test="${param.sortDir eq 'DESC' || empty param.sortDir}">selected</c:if>>Giảm dần</option>
                        <option value="ASC" <c:if test="${param.sortDir eq 'ASC'}">selected</c:if>>Tăng dần</option>
                    </select>
                </div>
                <div class="col-12 d-flex gap-2">
                    <button class="btn btn-primary" type="submit" style="font-weight: 900; text-transform: uppercase;">Lọc</button>
                    <a class="btn btn-outline-secondary" href="${pageContext.request.contextPath}/blogs" style="font-weight: 900; text-transform: uppercase;">Reset</a>
                </div>

                <input type="hidden" name="page" value="${filter.page}" />
                <input type="hidden" name="pageSize" value="${filter.pageSize}" />
            </form>

            <c:choose>
                <c:when test="${empty posts}">
                    <div class="no-results" style="display: flex;">
                        <div class="no-results-icon"><i class="bi bi-journal-text"></i></div>
                        <h3 class="no-results-title">Chưa có bài viết</h3>
                        <p class="no-results-text">Hãy quay lại sau để xem bài viết mới.</p>
                    </div>
                </c:when>
                <c:otherwise>
                    <div class="row g-3">
                        <c:forEach var="p" items="${posts}">
                            <div class="col-12 col-md-6 col-lg-4">
                                <div class="card h-100" style="border-radius: 1rem; overflow: hidden; border: 1px solid rgba(0,0,0,.06); box-shadow: 0 8px 24px rgba(0,0,0,.06);">
                                    <c:if test="${not empty p.thumbnailPath}">
                                        <img src="${pageContext.request.contextPath}/${p.thumbnailPath}" class="card-img-top" alt="thumbnail" style="height: 180px; object-fit: cover;">
                                    </c:if>
                                    <div class="card-body d-flex flex-column">
                                        <div class="d-flex align-items-center justify-content-between mb-2" style="color: #64748B; font-weight: 700; font-size: .85rem;">
                                            <span><i class="bi bi-person"></i> <c:out value="${p.authorName}" /></span>
                                            <span><i class="bi bi-clock"></i> <c:out value="${p.publishedAt != null ? p.publishedAt : p.createdAt}" /></span>
                                        </div>
                                        <h5 class="card-title" style="font-weight: 900; text-transform: uppercase; font-size: .95rem; line-height: 1.3;">
                                            <c:out value="${p.title}" />
                                        </h5>
                                        <p class="card-text" style="color: #475569; font-weight: 600;">
                                            <c:out value="${p.summary}" />
                                        </p>
                                        <div class="mt-auto">
                                            <a class="btn btn-success w-100" href="${pageContext.request.contextPath}/blogs/detail?id=${p.postId}" style="font-weight: 900; text-transform: uppercase; letter-spacing: 0.05em;">
                                                Xem chi tiết
                                            </a>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </c:forEach>
                    </div>

                    <c:set var="pageSize" value="${filter.pageSize}" />
                    <c:set var="page" value="${filter.page}" />
                    <c:set var="totalPages" value="${(total + pageSize - 1) / pageSize}" />

                    <nav class="mt-4" aria-label="pagination">
                        <ul class="pagination justify-content-center">
                            <li class="page-item <c:if test='${page <= 1}'>disabled</c:if>">
                                <a class="page-link" href="${pageContext.request.contextPath}/blogs?q=${fn:escapeXml(param.q)}&sortBy=${fn:escapeXml(param.sortBy)}&sortDir=${fn:escapeXml(param.sortDir)}&page=${page-1}&pageSize=${pageSize}">Trước</a>
                            </li>
                            <li class="page-item disabled"><span class="page-link">${page} / ${totalPages}</span></li>
                            <li class="page-item <c:if test='${page >= totalPages}'>disabled</c:if>">
                                <a class="page-link" href="${pageContext.request.contextPath}/blogs?q=${fn:escapeXml(param.q)}&sortBy=${fn:escapeXml(param.sortBy)}&sortDir=${fn:escapeXml(param.sortDir)}&page=${page+1}&pageSize=${pageSize}">Sau</a>
                            </li>
                        </ul>
                    </nav>
                </c:otherwise>
            </c:choose>

        </div>
    </main>

    <jsp:include page="../common/bottom-nav.jsp" />

</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
