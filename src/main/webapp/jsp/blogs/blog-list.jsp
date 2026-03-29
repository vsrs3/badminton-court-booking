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
    <style>
        body { background: #f0fdf4; }
        .blog-hero {
            background: linear-gradient(135deg, #065F46 0%, #047857 50%, #10B981 100%);
            border-radius: 1.25rem;
            padding: 2rem 2.5rem;
            margin-bottom: 1.5rem;
            color: #fff;
            position: relative;
            overflow: hidden;
        }
        .blog-hero::before {
            content: '';
            position: absolute;
            top: -50%;
            right: -20%;
            width: 300px;
            height: 300px;
            background: rgba(255,255,255,.06);
            border-radius: 50%;
        }
        .blog-hero h2 { font-weight: 900; letter-spacing: -0.02em; margin: 0; text-transform: uppercase; }
        .blog-hero p { opacity: .85; font-weight: 600; margin: .5rem 0 0; }
        .blog-card {
            border-radius: 1rem;
            overflow: hidden;
            border: none;
            box-shadow: 0 4px 16px rgba(0,0,0,.06);
            transition: transform .2s ease, box-shadow .2s ease;
            background: #fff;
        }
        .blog-card:hover {
            transform: translateY(-4px);
            box-shadow: 0 12px 32px rgba(0,0,0,.1);
        }
        .blog-card .card-body { padding: 1.25rem; }
        .blog-card .card-title {
            font-weight: 800;
            font-size: 1rem;
            line-height: 1.4;
            color: #0F172A;
            display: -webkit-box;
            -webkit-line-clamp: 2;
            line-clamp: 2;
            -webkit-box-orient: vertical;
            overflow: hidden;
        }
        .blog-card .card-text {
            color: #64748B;
            font-weight: 500;
            font-size: .9rem;
            display: -webkit-box;
            -webkit-line-clamp: 3;
            line-clamp: 3;
            -webkit-box-orient: vertical;
            overflow: hidden;
        }
        .blog-meta { color: #94A3B8; font-weight: 600; font-size: .8rem; }
        .blog-meta i { margin-right: 2px; }
        .filter-section {
            background: #fff;
            border-radius: 1rem;
            padding: 1.25rem;
            box-shadow: 0 2px 8px rgba(0,0,0,.04);
            margin-bottom: 1.5rem;
        }
        .back-home-btn {
            display: inline-flex;
            align-items: center;
            gap: .5rem;
            color: #fff;
            text-decoration: none;
            font-weight: 800;
            opacity: .9;
            transition: opacity .2s;
        }
        .back-home-btn:hover { opacity: 1; color: #fff; }
    </style>
</head>
<body>
<div class="container-fluid px-3 px-sm-4 px-lg-5 py-4" style="max-width: 1200px; margin: 0 auto;">

    <div class="blog-hero">
        <div class="d-flex align-items-center justify-content-between flex-wrap gap-3">
            <div>
                <a href="${pageContext.request.contextPath}/" class="back-home-btn mb-2">
                    <i class="bi bi-arrow-left"></i> Trang chủ
                </a>
                <h2><i class="bi bi-people-fill me-2"></i>Cộng đồng</h2>
                <p>Khám phá bài viết, chia sẻ kinh nghiệm chơi cầu lông</p>
            </div>
            <c:if test="${isLoggedIn and (currentRole eq 'ADMIN' or currentRole eq 'OWNER' or currentRole eq 'STAFF')}">
                <a class="btn btn-light" href="${pageContext.request.contextPath}/blogs/manage/list" style="font-weight: 800; color: #065F46;">
                    <i class="bi bi-gear me-1"></i> Quản lý bài viết
                </a>
            </c:if>
        </div>
    </div>

    <div class="filter-section">
        <form class="row g-2 align-items-end" method="GET" action="${pageContext.request.contextPath}/blogs">
            <div class="col-12 col-md-5">
                <label class="form-label" style="font-weight: 700; font-size: .85rem; color: #475569;">Tìm kiếm</label>
                <input class="form-control" name="q" value="${fn:escapeXml(param.q)}" placeholder="Tìm theo tiêu đề/tóm tắt " />
            </div>
            <div class="col-6 col-md-3">
                <label class="form-label" style="font-weight: 700; font-size: .85rem; color: #475569;">Sắp xếp</label>
                <select class="form-select" name="sortBy">
                    <option value="published_at" <c:if test="${param.sortBy eq 'published_at' || empty param.sortBy}">selected</c:if>>Mới nhất</option>
                    <option value="created_at" <c:if test="${param.sortBy eq 'created_at'}">selected</c:if>>Ngày tạo</option>
                </select>
            </div>
            <div class="col-6 col-md-2">
                <label class="form-label" style="font-weight: 700; font-size: .85rem; color: #475569;">Thứ tự</label>
                <select class="form-select" name="sortDir">
                    <option value="DESC" <c:if test="${param.sortDir eq 'DESC' || empty param.sortDir}">selected</c:if>>Giảm dần</option>
                    <option value="ASC" <c:if test="${param.sortDir eq 'ASC'}">selected</c:if>>Tăng dần</option>
                </select>
            </div>
            <div class="col-12 col-md-2 d-flex gap-2">
                <button class="btn btn-success flex-fill" type="submit" style="font-weight: 800;"><i class="bi bi-search me-1"></i> Lọc</button>
                <a class="btn btn-outline-secondary" href="${pageContext.request.contextPath}/blogs" style="font-weight: 800;">Reset</a>
            </div>
        </form>
    </div>

    <c:if test="${not empty error}">
        <div class="alert alert-danger" role="alert">
            <c:out value="${error}" />
        </div>
    </c:if>

    <c:choose>
        <c:when test="${empty posts}">
            <div class="text-center py-5">
                <div style="font-size: 3rem; color: #CBD5E1;"><i class="bi bi-journal-text"></i></div>
                <h4 style="font-weight: 800; color: #475569; margin-top: .5rem;">Chưa có bài viết</h4>
                <p style="color: #94A3B8; font-weight: 600;">Hãy quay lại sau để xem bài viết mới.</p>
            </div>
        </c:when>
        <c:otherwise>
            <div class="row g-3">
                <c:forEach var="p" items="${posts}">
                    <div class="col-12 col-md-6 col-lg-4">
                        <div class="card blog-card h-100">
                            <div class="card-body d-flex flex-column">
                                <div class="d-flex align-items-center justify-content-between mb-2 blog-meta">
                                    <span><i class="bi bi-person-circle"></i> <c:out value="${p.authorName}" /></span>
                                    <span><i class="bi bi-calendar3"></i> <c:out value="${not empty p.publishedAtFormatted ? p.publishedAtFormatted : p.createdAtFormatted}" /></span>
                                </div>
                                <h6 class="card-title mb-2">
                                    <c:out value="${p.title}" />
                                </h6>
                                <p class="card-text flex-grow-1">
                                    <c:out value="${p.summary}" />
                                </p>
                                <div class="mt-auto pt-2">
                                    <a class="btn btn-success btn-sm w-100" href="${pageContext.request.contextPath}/blogs/detail?id=${p.postId}" style="font-weight: 700; border-radius: .5rem;">
                                        Xem chi tiết <i class="bi bi-arrow-right ms-1"></i>
                                    </a>
                                </div>
                            </div>
                        </div>
                    </div>
                </c:forEach>
            </div>

            <c:set var="page" value="${filter.page}" />

            <nav class="mt-4" aria-label="pagination">
                <ul class="pagination justify-content-center">
                    <li class="page-item <c:if test='${page <= 1}'>disabled</c:if>">
                        <a class="page-link" href="${pageContext.request.contextPath}/blogs?q=${fn:escapeXml(param.q)}&sortBy=${fn:escapeXml(param.sortBy)}&sortDir=${fn:escapeXml(param.sortDir)}&page=${page-1}&pageSize=${filter.pageSize}">
                            <i class="bi bi-chevron-left"></i> Trước
                        </a>
                    </li>
                    <li class="page-item disabled"><span class="page-link" style="font-weight: 700;">Trang ${page} / ${totalPages}</span></li>
                    <li class="page-item <c:if test='${page >= totalPages}'>disabled</c:if>">
                        <a class="page-link" href="${pageContext.request.contextPath}/blogs?q=${fn:escapeXml(param.q)}&sortBy=${fn:escapeXml(param.sortBy)}&sortDir=${fn:escapeXml(param.sortDir)}&page=${page+1}&pageSize=${filter.pageSize}">
                            Sau <i class="bi bi-chevron-right"></i>
                        </a>
                    </li>
                </ul>
            </nav>
        </c:otherwise>
    </c:choose>

</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
