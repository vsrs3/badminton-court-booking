<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    String error = (String) request.getAttribute("error");
    String retryUrl = (String) request.getAttribute("retryUrl");
    String retryLabel = (String) request.getAttribute("retryLabel");

    if (error == null || error.isBlank()) {
        error = "Có lỗi xảy ra khi xử lý tài khoản Google.";
    }

    if (retryUrl == null || retryUrl.isBlank()) {
        retryUrl = request.getContextPath() + "/google-login";
    }

    if (retryLabel == null || retryLabel.isBlank()) {
        retryLabel = "Chọn lại tài khoản Google";
    }
%>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Loi xu ly Google - BadmintonPro</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/badminton-pro.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/auth.css">
</head>
<body>

<div class="auth-page">
    <div class="back-to-home">
        <a href="${pageContext.request.contextPath}/" class="back-btn">
            <i class="bi bi-arrow-left"></i>
            <span>Trang chu</span>
        </a>
    </div>

    <div class="auth-card">
        <div class="auth-header">
            <div class="auth-logo">
                <i class="bi bi-trophy-fill"></i>
            </div>
            <h1 class="auth-brand-name">
                CHOI SO <span class="highlight">CHOI NGAY</span>
            </h1>
            <p class="auth-tagline">Nen tang dat san cau long so 1 Viet Nam</p>
        </div>

        <div class="auth-body text-center">
            <h2 class="auth-title text-danger">
                <i class="bi bi-exclamation-triangle-fill"></i>
                Xac nhan Google that bai
            </h2>

            <div class="mt-3 p-3 rounded" style="background-color: #FEE2E2; border: 1px solid #EF4444;">
                <span style="color: #B91C1C; font-weight: 600;">
                    <%= error %>
                </span>
            </div>

            <div class="d-grid gap-3 mt-4">
                <a href="<%= retryUrl %>"
                   class="btn btn-outline-danger d-flex align-items-center justify-content-center gap-2">
                    <i class="bi bi-google"></i>
                    <span><%= retryLabel %></span>
                </a>
            </div>
        </div>
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
