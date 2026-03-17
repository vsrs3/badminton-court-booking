<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><c:out value="${post.title}" /> - Cộng đồng</title>

    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.0/font/bootstrap-icons.css">
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700;800;900&display=swap" rel="stylesheet">

    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/badminton-pro.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/components.css">
    <style>
        body { background: #f0fdf4; }
        .detail-container { max-width: 900px; margin: 0 auto; }
        .detail-card {
            background: #fff;
            border-radius: 1rem;
            border: none;
            box-shadow: 0 4px 16px rgba(0,0,0,.06);
            padding: 1.5rem 2rem;
            margin-bottom: 1.25rem;
        }
        .detail-title {
            font-weight: 900;
            color: #065F46;
            font-size: 1.6rem;
            line-height: 1.3;
            margin-bottom: .75rem;
        }
        .detail-meta {
            color: #94A3B8;
            font-weight: 600;
            font-size: .85rem;
            margin-bottom: 1rem;
            display: flex;
            align-items: center;
            gap: 1rem;
            flex-wrap: wrap;
        }
        .detail-meta i { margin-right: 3px; }
        .detail-content {
            white-space: pre-wrap;
            font-weight: 500;
            color: #1E293B;
            line-height: 1.85;
            font-size: .95rem;
        }
        .section-title {
            font-weight: 800;
            color: #065F46;
            font-size: 1rem;
            text-transform: uppercase;
            letter-spacing: 0.02em;
            margin-bottom: .75rem;
        }
        .comment-item {
            background: #F8FAFC;
            border: 1px solid #E2E8F0;
            border-radius: .75rem;
            padding: 1rem;
        }
        .comment-author { font-weight: 800; color: #0F172A; font-size: .9rem; }
        .comment-date { color: #94A3B8; font-weight: 600; font-size: .8rem; }
        .comment-body {
            white-space: pre-wrap;
            font-weight: 500;
            color: #334155;
            margin-top: .4rem;
            font-size: .9rem;
            line-height: 1.6;
        }
        .comment-actions {
            display: flex;
            align-items: center;
            gap: .5rem;
            flex-wrap: wrap;
            margin-top: .5rem;
        }
        .back-link {
            display: inline-flex;
            align-items: center;
            gap: .4rem;
            color: #065F46;
            text-decoration: none;
            font-weight: 700;
            font-size: .9rem;
            transition: color .2s;
        }
        .back-link:hover { color: #047857; }
    </style>
</head>
<body>
<div class="container-fluid px-3 px-sm-4 px-lg-5 py-4 detail-container">

    <div class="d-flex align-items-center justify-content-between flex-wrap gap-2 mb-3">
        <a class="back-link" href="${pageContext.request.contextPath}/blogs">
            <i class="bi bi-arrow-left"></i> Quay lại cộng đồng
        </a>
        <c:if test="${isLoggedIn and (currentRole eq 'ADMIN' or currentRole eq 'OWNER' or currentRole eq 'STAFF')}">
            <a class="btn btn-sm btn-outline-success" href="${pageContext.request.contextPath}/blogs/manage/list" style="font-weight: 700;">
                <i class="bi bi-gear me-1"></i> Quản lý
            </a>
        </c:if>
    </div>

    <div class="detail-card">
        <h1 class="detail-title"><c:out value="${post.title}" /></h1>
        <div class="detail-meta">
            <span><i class="bi bi-calendar3"></i> <c:out value="${not empty post.publishedAtFormatted ? post.publishedAtFormatted : post.createdAtFormatted}" /></span>
            <span class="badge bg-success bg-opacity-25 text-success" style="font-weight: 700;"><c:out value="${post.status}" /></span>
        </div>
        <div class="detail-content">
            <c:out value="${post.content}" />
        </div>
    </div>

    <div class="detail-card">
        <h5 class="section-title"><i class="bi bi-emoji-smile me-1"></i> Cảm xúc</h5>

        <c:set var="ur" value="${userReactions}" />

        <div class="d-flex flex-wrap gap-2 align-items-center">
            <c:set var="emojis" value="LIKE,HEART,LAUGH,WOW,SAD,ANGRY" />
            <c:forEach var="e" items="${fn:split(emojis, ',')}">
                <c:set var="has" value="${ur != null && ur.contains(e)}" />
                <form method="POST" action="${pageContext.request.contextPath}/blogs/interact" class="m-0">
                    <input type="hidden" name="postId" value="${post.postId}" />
                    <input type="hidden" name="emojiCode" value="${e}" />
                    <input type="hidden" name="action" value="${has ? 'reaction_remove' : 'reaction_add'}" />
                    <button type="submit" class="btn ${has ? 'btn-success' : 'btn-outline-secondary'} btn-sm" style="font-weight: 700; border-radius: 2rem; padding: .3rem .75rem;">
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
                        <span style="font-size: .85rem;">${cnt}</span>
                    </button>
                </form>
            </c:forEach>

            <c:if test="${not isLoggedIn}">
                <div class="ms-2" style="color:#94A3B8; font-weight: 600; font-size: .85rem;">Đăng nhập để thả cảm xúc</div>
            </c:if>
        </div>
    </div>

    <div id="comments" class="detail-card">
        <h5 class="section-title"><i class="bi bi-chat-dots me-1"></i> Bình luận</h5>

        <c:if test="${isLoggedIn and currentRole eq 'CUSTOMER'}">
            <form method="POST" action="${pageContext.request.contextPath}/blogs/interact" class="mb-3">
                <input type="hidden" name="action" value="comment_add" />
                <input type="hidden" name="postId" value="${post.postId}" />
                <textarea class="form-control" name="content" rows="3" maxlength="1000" placeholder="Viết bình luận..." required style="border-radius: .75rem;"></textarea>
                <div class="d-flex justify-content-end mt-2">
                    <button type="submit" class="btn btn-success btn-sm" style="font-weight: 700; border-radius: .5rem; padding: .4rem 1.25rem;">
                        <i class="bi bi-send me-1"></i> Gửi
                    </button>
                </div>
            </form>
        </c:if>

        <c:if test="${not isLoggedIn}">
            <div class="alert alert-secondary" role="alert" style="font-weight: 600; border-radius: .75rem; font-size: .9rem;">
                <i class="bi bi-info-circle me-1"></i> Đăng nhập để bình luận.
            </div>
        </c:if>

        <c:if test="${isLoggedIn and currentRole ne 'CUSTOMER' and (currentRole eq 'ADMIN' or currentRole eq 'OWNER' or currentRole eq 'STAFF')}">
            <div class="alert alert-info" role="alert" style="font-weight: 600; border-radius: .75rem; font-size: .9rem;">
                <i class="bi bi-shield-check me-1"></i> Bạn đang ở chế độ kiểm duyệt bình luận.
            </div>
        </c:if>

        <div class="d-flex flex-column gap-3">
            <c:forEach var="cmt" items="${comments}">
                <div class="comment-item">
                    <div class="d-flex align-items-center justify-content-between flex-wrap gap-2">
                        <div>
                            <span class="comment-author"><i class="bi bi-person-circle me-1"></i><c:out value="${cmt.authorName}" /></span>
                            <span class="comment-date ms-2"><c:out value="${cmt.createdAtFormatted}" /></span>
                        </div>
                        <span class="badge bg-secondary bg-opacity-25 text-secondary" style="font-weight: 600; font-size: .75rem;"><c:out value="${cmt.status}" /></span>
                    </div>

                    <div class="comment-body">
                        <c:out value="${cmt.content}" />
                    </div>

                    <c:if test="${isLoggedIn and currentRole eq 'CUSTOMER' and currentUser.accountId eq cmt.authorAccountId}">
                        <div class="comment-actions">
                            <form method="POST" action="${pageContext.request.contextPath}/blogs/interact" class="d-flex align-items-center gap-2 flex-grow-1" style="min-width: 0;">
                                <input type="hidden" name="action" value="comment_edit" />
                                <input type="hidden" name="postId" value="${post.postId}" />
                                <input type="hidden" name="commentId" value="${cmt.commentId}" />
                                <input class="form-control form-control-sm flex-grow-1" name="content" value="${fn:escapeXml(cmt.content)}" maxlength="1000" style="border-radius: .5rem; min-width: 150px;" />
                                <button type="submit" class="btn btn-sm btn-outline-success" style="font-weight: 700; white-space: nowrap;">Sửa</button>
                            </form>
                            <form method="POST" action="${pageContext.request.contextPath}/blogs/interact" class="m-0">
                                <input type="hidden" name="action" value="comment_delete" />
                                <input type="hidden" name="postId" value="${post.postId}" />
                                <input type="hidden" name="commentId" value="${cmt.commentId}" />
                                <button type="submit" class="btn btn-sm btn-outline-danger" style="font-weight: 700; white-space: nowrap;" onclick="return confirm('Xóa bình luận?');">Xóa</button>
                            </form>
                        </div>
                    </c:if>

                    <c:if test="${isLoggedIn and (currentRole eq 'ADMIN' or currentRole eq 'OWNER' or currentRole eq 'STAFF')}">
                        <div class="comment-actions">
                            <form method="POST" action="${pageContext.request.contextPath}/blogs/interact" class="m-0">
                                <input type="hidden" name="action" value="comment_moderate" />
                                <input type="hidden" name="postId" value="${post.postId}" />
                                <input type="hidden" name="commentId" value="${cmt.commentId}" />
                                <input type="hidden" name="modAction" value="approve" />
                                <button type="submit" class="btn btn-sm btn-success" style="font-weight: 700;">Duyệt</button>
                            </form>
                            <form method="POST" action="${pageContext.request.contextPath}/blogs/interact" class="m-0">
                                <input type="hidden" name="action" value="comment_moderate" />
                                <input type="hidden" name="postId" value="${post.postId}" />
                                <input type="hidden" name="commentId" value="${cmt.commentId}" />
                                <input type="hidden" name="modAction" value="reject" />
                                <button type="submit" class="btn btn-sm btn-outline-secondary" style="font-weight: 700;">Từ chối</button>
                            </form>
                            <form method="POST" action="${pageContext.request.contextPath}/blogs/interact" class="m-0">
                                <input type="hidden" name="action" value="comment_delete_mod" />
                                <input type="hidden" name="postId" value="${post.postId}" />
                                <input type="hidden" name="commentId" value="${cmt.commentId}" />
                                <button type="submit" class="btn btn-sm btn-outline-danger" style="font-weight: 700;" onclick="return confirm('Xóa bình luận?');">Xóa</button>
                            </form>
                        </div>
                    </c:if>
                </div>
            </c:forEach>

            <c:if test="${empty comments}">
                <div class="text-center py-3" style="color:#94A3B8; font-weight:600;">
                    <i class="bi bi-chat-left-text" style="font-size: 1.5rem;"></i>
                    <div class="mt-1">Chưa có bình luận.</div>
                </div>
            </c:if>
        </div>
    </div>

</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
