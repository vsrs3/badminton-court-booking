<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Bài viết</title>

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

            <div class="d-flex align-items-center justify-content-between flex-wrap gap-2 mb-3">
                <a class="btn btn-outline-secondary" href="${pageContext.request.contextPath}/blogs" style="font-weight: 900; text-transform: uppercase;">
                    <i class="bi bi-arrow-left"></i> Quay lại
                </a>

                <c:if test="${isLoggedIn and (currentRole eq 'ADMIN' or currentRole eq 'OWNER' or currentRole eq 'STAFF')}">
                    <a class="btn btn-success" href="${pageContext.request.contextPath}/blogs/manage/list" style="font-weight: 900; text-transform: uppercase;">
                        Quản lý
                    </a>
                </c:if>
            </div>

            <c:if test="${not empty post.thumbnailPath}">
                <div class="mb-3" style="border-radius: 1rem; overflow: hidden; box-shadow: 0 10px 30px rgba(0,0,0,.08);">
                    <img src="${pageContext.request.contextPath}/${post.thumbnailPath}" alt="thumbnail" style="width: 100%; max-height: 320px; object-fit: cover;">
                </div>
            </c:if>

            <h2 style="font-weight: 900; color: #065F46; text-transform: uppercase; letter-spacing: -0.02em;">
                <c:out value="${post.title}" />
            </h2>

            <div class="d-flex align-items-center gap-3 flex-wrap" style="color: #64748B; font-weight: 700; margin-bottom: 1rem;">
                <span><i class="bi bi-calendar"></i> <c:out value="${post.publishedAt != null ? post.publishedAt : post.createdAt}" /></span>
                <span class="badge bg-secondary"><c:out value="${post.status}" /></span>
            </div>

            <div class="p-3 p-md-4" style="background: #fff; border-radius: 1rem; border: 1px solid rgba(0,0,0,.06); box-shadow: 0 8px 24px rgba(0,0,0,.06);">
                <div style="white-space: pre-wrap; font-weight: 600; color: #0F172A; line-height: 1.9;">
                    <c:out value="${post.content}" />
                </div>
            </div>

            <div class="mt-4 p-3 p-md-4" style="background: #fff; border-radius: 1rem; border: 1px solid rgba(0,0,0,.06); box-shadow: 0 8px 24px rgba(0,0,0,.06);">
                <h5 style="font-weight: 900; text-transform: uppercase; letter-spacing: 0.02em; color: #065F46;">Cảm xúc</h5>

                <c:set var="ur" value="${userReactions}" />

                <div class="d-flex flex-wrap gap-2 align-items-center">
                    <c:set var="emojis" value="LIKE,HEART,LAUGH,WOW,SAD,ANGRY" />
                    <c:forEach var="e" items="${fn:split(emojis, ',')}">
                        <c:set var="has" value="${ur != null && ur.contains(e)}" />
                        <form method="POST" action="${pageContext.request.contextPath}/blogs/interact" class="m-0">
                            <input type="hidden" name="postId" value="${post.postId}" />
                            <input type="hidden" name="emojiCode" value="${e}" />
                            <input type="hidden" name="action" value="${has ? 'reaction_remove' : 'reaction_add'}" />
                            <button type="submit" class="btn ${has ? 'btn-success' : 'btn-outline-success'} btn-sm" style="font-weight: 900; text-transform: uppercase;">
                                <span style="font-family: 'Segoe UI Emoji','Apple Color Emoji','Noto Color Emoji','Segoe UI Symbol',sans-serif; font-size: 1.05rem; line-height: 1;">
                                    <c:choose>
                                        <c:when test="${e eq 'LIKE'}">&#x1F44D;</c:when>
                                        <c:when test="${e eq 'HEART'}">&#x2764;&#xFE0F;</c:when>
                                        <c:when test="${e eq 'LAUGH'}">&#x1F602;</c:when>
                                        <c:when test="${e eq 'WOW'}">&#x1F62E;</c:when>
                                        <c:when test="${e eq 'SAD'}">&#x1F622;</c:when>
                                        <c:when test="${e eq 'ANGRY'}">&#x1F621;</c:when>
                                        <c:otherwise><c:out value="${e}" /></c:otherwise>
                                    </c:choose>
                                </span>
                                <c:set var="cnt" value="0" />
                                <c:forEach var="rc" items="${reactionCounts}">
                                    <c:if test="${rc.emojiCode eq e}"><c:set var="cnt" value="${rc.count}" /></c:if>
                                </c:forEach>
                                (${cnt})
                            </button>
                        </form>
                    </c:forEach>

                    <c:if test="${not isLoggedIn}">
                        <div class="ms-2" style="color:#64748B; font-weight: 700;">Đăng nhập để thả cảm xúc</div>
                    </c:if>
                </div>
            </div>

            <div id="comments" class="mt-4 p-3 p-md-4" style="background: #fff; border-radius: 1rem; border: 1px solid rgba(0,0,0,.06); box-shadow: 0 8px 24px rgba(0,0,0,.06);">
                <h5 style="font-weight: 900; text-transform: uppercase; letter-spacing: 0.02em; color: #065F46;">Bình luận</h5>

                <c:if test="${isLoggedIn and currentRole eq 'CUSTOMER'}">
                    <form method="POST" action="${pageContext.request.contextPath}/blogs/interact" class="mb-3">
                        <input type="hidden" name="action" value="comment_add" />
                        <input type="hidden" name="postId" value="${post.postId}" />
                        <textarea class="form-control" name="content" rows="3" maxlength="1000" placeholder="Viết bình luận..." required></textarea>
                        <div class="d-flex justify-content-end mt-2">
                            <button type="submit" class="btn btn-success" style="font-weight: 900; text-transform: uppercase;">Gửi</button>
                        </div>
                    </form>
                </c:if>

                <c:if test="${not isLoggedIn}">
                    <div class="alert alert-secondary" role="alert" style="font-weight: 700;">
                        Đăng nhập để bình luận.
                    </div>
                </c:if>

                <c:if test="${isLoggedIn and currentRole ne 'CUSTOMER' and (currentRole eq 'ADMIN' or currentRole eq 'OWNER' or currentRole eq 'STAFF')}">
                    <div class="alert alert-info" role="alert" style="font-weight: 700;">
                        Bạn đang ở chế độ kiểm duyệt bình luận.
                    </div>
                </c:if>

                <div class="d-flex flex-column gap-3">
                    <c:forEach var="cmt" items="${comments}">
                        <div style="border: 1px solid rgba(0,0,0,.06); border-radius: .75rem; padding: .75rem;">
                            <div class="d-flex align-items-center justify-content-between flex-wrap gap-2">
                                <div style="font-weight: 900; color:#0F172A;">
                                    <c:out value="${cmt.authorName}" />
                                    <span style="color:#64748B; font-weight:700; font-size: .85rem;">- <c:out value="${cmt.createdAt}" /></span>
                                </div>
                                <span class="badge bg-secondary"><c:out value="${cmt.status}" /></span>
                            </div>

                            <div style="white-space: pre-wrap; font-weight: 600; color:#0F172A; margin-top: .5rem;">
                                <c:out value="${cmt.content}" />
                            </div>

                            <div class="d-flex flex-wrap gap-2 mt-2">
                                <c:if test="${isLoggedIn and currentRole eq 'CUSTOMER' and currentUser.accountId eq cmt.authorAccountId}">
                                    <form method="POST" action="${pageContext.request.contextPath}/blogs/interact" class="d-flex gap-2 flex-wrap">
                                        <input type="hidden" name="action" value="comment_edit" />
                                        <input type="hidden" name="postId" value="${post.postId}" />
                                        <input type="hidden" name="commentId" value="${cmt.commentId}" />
                                        <input class="form-control form-control-sm" name="content" value="${fn:escapeXml(cmt.content)}" maxlength="1000" style="min-width: 240px;" />
                                        <button type="submit" class="btn btn-sm btn-outline-success" style="font-weight: 900; text-transform: uppercase;">Sửa</button>
                                    </form>

                                    <form method="POST" action="${pageContext.request.contextPath}/blogs/interact" class="m-0">
                                        <input type="hidden" name="action" value="comment_delete" />
                                        <input type="hidden" name="postId" value="${post.postId}" />
                                        <input type="hidden" name="commentId" value="${cmt.commentId}" />
                                        <button type="submit" class="btn btn-sm btn-outline-danger" style="font-weight: 900; text-transform: uppercase;" onclick="return confirm('Xóa bình luận?');">Xóa</button>
                                    </form>
                                </c:if>

                                <c:if test="${isLoggedIn and (currentRole eq 'ADMIN' or currentRole eq 'OWNER' or currentRole eq 'STAFF')}">
                                    <form method="POST" action="${pageContext.request.contextPath}/blogs/interact" class="m-0">
                                        <input type="hidden" name="action" value="comment_moderate" />
                                        <input type="hidden" name="postId" value="${post.postId}" />
                                        <input type="hidden" name="commentId" value="${cmt.commentId}" />
                                        <input type="hidden" name="modAction" value="approve" />
                                        <button type="submit" class="btn btn-sm btn-success" style="font-weight: 900; text-transform: uppercase;">Duyệt</button>
                                    </form>
                                    <form method="POST" action="${pageContext.request.contextPath}/blogs/interact" class="m-0">
                                        <input type="hidden" name="action" value="comment_moderate" />
                                        <input type="hidden" name="postId" value="${post.postId}" />
                                        <input type="hidden" name="commentId" value="${cmt.commentId}" />
                                        <input type="hidden" name="modAction" value="reject" />
                                        <button type="submit" class="btn btn-sm btn-outline-secondary" style="font-weight: 900; text-transform: uppercase;">Từ chối</button>
                                    </form>
                                    <form method="POST" action="${pageContext.request.contextPath}/blogs/interact" class="m-0">
                                        <input type="hidden" name="action" value="comment_delete_mod" />
                                        <input type="hidden" name="postId" value="${post.postId}" />
                                        <input type="hidden" name="commentId" value="${cmt.commentId}" />
                                        <button type="submit" class="btn btn-sm btn-outline-danger" style="font-weight: 900; text-transform: uppercase;" onclick="return confirm('Xóa bình luận?');">Xóa</button>
                                    </form>
                                </c:if>
                            </div>
                        </div>
                    </c:forEach>

                    <c:if test="${empty comments}">
                        <div style="color:#64748B; font-weight:700;">Chưa có bình luận.</div>
                    </c:if>
                </div>

            </div>

        </div>
    </main>

    <jsp:include page="../common/bottom-nav.jsp" />

</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
