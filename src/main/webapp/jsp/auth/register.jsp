<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Đăng ký - BadmintonPro</title>

    <!-- Bootstrap -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">

    <!-- Bootstrap Icons -->
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.css">

    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/badminton-pro.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/auth.css">
</head>
<body>
<div class="auth-page">

    <!-- Back -->
    <div class="back-to-home">
        <a href="${pageContext.request.contextPath}/" class="back-btn">
            <i class="bi bi-arrow-left"></i>
            <span>Trang chủ</span>
        </a>
    </div>

    <div class="auth-card">

        <!-- Header -->
        <div class="auth-header text-center">
            <div class="auth-logo">
                <i class="bi bi-person-plus-fill"></i>
            </div>
            <h1 class="auth-brand-name">
                TẠO TÀI KHOẢN <span class="highlight">MỚI</span>
            </h1>
            <p class="auth-tagline">Tham gia hệ thống đặt sân cầu lông</p>
        </div>

        <div class="auth-body">
            <h2 class="auth-title">Đăng ký</h2>
            <p class="auth-subtitle">Điền thông tin bên dưới</p>

            <!-- Server Error -->
            <% if (request.getAttribute("error") != null) { %>
            <div class="alert alert-danger d-flex align-items-center gap-2">
                <i class="bi bi-exclamation-triangle-fill"></i>
                <span><%= request.getAttribute("error") %></span>
            </div>
            <% } %>

            <!-- FORM -->
            <form method="POST"
                  action="${pageContext.request.contextPath}/register"
                  class="auth-form"
                  onsubmit="return validateForm()">

                <!-- Email -->
                <div class="mb-3">
                    <label class="form-label">Email *</label>
                    <div class="input-group">
                        <span class="input-group-text">
                            <i class="bi bi-envelope-fill"></i>
                        </span>
                        <input type="email"
                               class="form-control"
                               name="email"
                               id="email"
                               value="${oldEmail != null ? oldEmail : ''}"
                               required>
                    </div>
                    <div class="text-danger small" id="emailError"></div>
                </div>

                <!-- Password -->
                <div class="mb-3">
                    <label class="form-label">Mật khẩu *</label>
                    <div class="input-group">
                        <span class="input-group-text">
                            <i class="bi bi-lock-fill"></i>
                        </span>
                        <input type="password"
                               class="form-control"
                               name="password"
                               id="password"
                               value="${oldPassword != null ? oldPassword : ''}"
                               required>
                        <button type="button" class="btn btn-outline-secondary" onclick="togglePassword()">
                            <i class="bi bi-eye-fill" id="pwIcon"></i>
                        </button>
                    </div>
                    <div class="text-danger small" id="pwError"></div>
                </div>

                <!-- Confirm -->
                <div class="mb-3">
                    <label class="form-label">Nhập lại mật khẩu *</label>
                    <div class="input-group">

                        <span class="input-group-text">
                            <i class="bi bi-shield-lock-fill"></i>
                        </span>

                        <input type="password"
                               class="form-control"
                               name="repassword"
                               id="repassword"
                               value="${oldPassword != null ? oldPassword : ''}"
                               required>
                    </div>
                    <div class="text-danger small" id="rePwError"></div>
                </div>
                <!-- Full Name -->
                <div class="mb-3">
                    <label class="form-label">Họ và tên *</label>
                    <input type="text"
                           class="form-control"
                           name="fullName"
                           id="fullName"
                           value="${oldFullName != null ? oldFullName : ''}"
                           required>
                    <div class="text-danger small" id="nameError"></div>
                </div>
                <!-- Phone -->
                <div class="mb-3">
                    <label class="form-label">Số điện thoại *</label>
                    <input type="text"
                           class="form-control"
                           name="phone"
                           id="phone"
                           value="${oldPhone != null ? oldPhone : ''}"
                           required>
                    <div class="text-danger small" id="phoneError"></div>
                </div>
                <button type="submit" class="btn btn-primary w-100">
                    <i class="bi bi-person-check-fill"></i>
                    Đăng ký
                </button>

            </form>

            <div class="auth-divider">
                <span>Hoặc</span>
            </div>

            <p class="text-center mt-3">
                Đã có tài khoản?
                <a href="${pageContext.request.contextPath}/jsp/auth/login.jsp">
                    Đăng nhập ngay
                </a>
            </p>

        </div>
    </div>
</div>

<script>
    function togglePassword() {
        const pw = document.getElementById("password");
        const repw = document.getElementById("repassword");
        const icon = document.getElementById("pwIcon");

        const type = pw.type === "password" ? "text" : "password";
        pw.type = type;
        repw.type = type;

        icon.classList.toggle("bi-eye-fill");
        icon.classList.toggle("bi-eye-slash-fill");
    }
</script>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>

</body>
</html>