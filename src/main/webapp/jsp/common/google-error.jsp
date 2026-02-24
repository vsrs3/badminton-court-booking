<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%
    String error = (String) request.getAttribute("error");
    if (error == null) {
        error = "Có lỗi xảy ra khi đăng nhập bằng Google.";
    }
%>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Lỗi đăng nhập Google - BadmintonPro</title>
    <!-- Bootstrap 5.3 CSS -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <!-- Bootstrap Icons -->
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.css">
    <!-- Custom CSS -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/badminton-pro.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/auth.css">
</head>
<body>

<div class="auth-page">
    <!-- Back to Home -->
    <div class="back-to-home">
        <a href="${pageContext.request.contextPath}/" class="back-btn">
            <i class="bi bi-arrow-left"></i>
            <span>Trang chủ</span>
        </a>
    </div>

    <div class="auth-card">
        <!-- Header -->
        <div class="auth-header">
            <div class="auth-logo">
                <i class="bi bi-trophy-fill"></i>
            </div>
            <h1 class="auth-brand-name">
                CHỌN SÂN <span class="highlight">CHƠI NGAY</span>
            </h1>
            <p class="auth-tagline">Nền tảng đặt sân cầu lông số 1 Việt Nam</p>
        </div>

        <!-- Body -->
        <div class="auth-body text-center">

            <h2 class="auth-title text-danger">
                <i class="bi bi-exclamation-triangle-fill"></i>
                Đăng nhập thất bại
            </h2>

            <div class="mt-3 p-3 rounded"
                 style="background-color: #FEE2E2; border: 1px solid #EF4444;">
                <span style="color:#B91C1C; font-weight:600;">
                    <%= error %>
                </span>
            </div>

            <!-- Action Buttons -->
            <div class="d-grid gap-3 mt-4">
                <a href="<%= request.getContextPath() %>/google-login"
                   class="btn btn-outline-danger d-flex align-items-center justify-content-center gap-2">
                    <i class="bi bi-google"></i>
                    <span>Chọn lại tài khoản Google</span>
                </a>
            </div>

        </div>
    </div>
</div>

<!-- Bootstrap JS -->
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>


