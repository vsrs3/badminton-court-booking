<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Đăng ký - BadmintonPro</title>

    <!-- Bootstrap -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">

    <!-- Icons -->
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.css">

    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/badminton-pro.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/auth.css">

    <style>
        .form-input-wrapper { position: relative; }
        .password-toggle {
            position: absolute;
            right: 12px;
            top: 50%;
            transform: translateY(-50%);
            background: none;
            border: none;
            cursor: pointer;
        }
        .form-input { padding-right: 45px; }
    </style>
</head>

<body>
<div class="auth-page">

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

            <% if (request.getAttribute("error") != null) { %>
            <div class="alert alert-danger d-flex align-items-center gap-2">
                <i class="bi bi-exclamation-triangle-fill"></i>
                <span><%= request.getAttribute("error") %></span>
            </div>
            <% } %>

            <form method="POST"
                  action="${pageContext.request.contextPath}/register"
                  class="auth-form"
                  onsubmit="return validateForm()">

                <!-- EMAIL -->
                <div class="form-group mb-3">
                    <label class="form-label">Email *</label>
                    <div class="form-input-wrapper">
                        <i class="bi bi-envelope-fill form-input-icon"></i>
                        <input type="email" class="form-input"
                               id="email" name="email"
                               value="${oldEmail != null ? oldEmail : ''}"
                               required>
                    </div>
                    <div class="text-danger small" id="emailError"></div>
                </div>

                <!-- PASSWORD -->
                <div class="form-group mb-3">
                    <label class="form-label">Mật khẩu *</label>
                    <div class="form-input-wrapper">
                        <i class="bi bi-lock-fill form-input-icon"></i>
                        <input type="password" class="form-input"
                               id="password" name="password"
                               required>
                        <button type="button"
                                class="password-toggle"
                                onclick="togglePassword('password', this)">
                            <i class="bi bi-eye-fill"></i>
                        </button>
                    </div>

                    <!-- RULES -->
                    <div id="pwRules" class="small text-danger mt-2" style="display:none;">
                        <div id="ruleLength">≥ 8 ký tự</div>
                        <div id="ruleUpper">Ít nhất 1 chữ in hoa</div>
                        <div id="ruleNumber">Ít nhất 1 chữ số</div>
                        <div id="ruleSpecial">Ít nhất 1 ký tự đặc biệt</div>
                    </div>
                </div>

                <!-- CONFIRM -->
                <div class="form-group mb-3">
                    <label class="form-label">Nhập lại mật khẩu *</label>
                    <div class="form-input-wrapper">
                        <i class="bi bi-shield-lock-fill form-input-icon"></i>
                        <input type="password" class="form-input"
                               id="repassword" name="repassword"
                               required>
                        <button type="button"
                                class="password-toggle"
                                onclick="togglePassword('repassword', this)">
                            <i class="bi bi-eye-fill"></i>
                        </button>
                    </div>
                    <div class="text-danger small" id="rePwError"></div>
                </div>

                <!-- FULL NAME -->
                <div class="form-group mb-3">
                    <label class="form-label">Họ và tên *</label>
                    <input type="text" class="form-control"
                           id="fullName" name="fullName"
                           value="${oldFullName != null ? oldFullName : ''}"
                           required>
                    <div class="text-danger small" id="nameError"></div>
                </div>

                <!-- PHONE -->
                <div class="form-group mb-3">
                    <label class="form-label">Số điện thoại *</label>
                    <input type="text" class="form-control"
                           id="phone" name="phone"
                           value="${oldPhone != null ? oldPhone : ''}"
                           required>
                    <div class="text-danger small" id="phoneError"></div>
                </div>

                <button type="submit" class="auth-submit-btn w-100">
                    <i class="bi bi-person-check-fill"></i>
                    <span>Đăng ký</span>
                </button>
            </form>

            <div class="auth-divider mt-4">
                <span>Đã có tài khoản?</span>
            </div>

            <p class="text-center mt-2">
                <a href="${pageContext.request.contextPath}/jsp/auth/login.jsp">
                    Đăng nhập ngay
                </a>
            </p>

        </div>
    </div>
</div>

<script>
    function togglePassword(inputId, btn) {
        const input = document.getElementById(inputId);
        const icon = btn.querySelector("i");
        const type = input.type === "password" ? "text" : "password";
        input.type = type;
        icon.classList.toggle("bi-eye-fill");
        icon.classList.toggle("bi-eye-slash-fill");
    }

    function validateForm() {

        let valid = true;

        const email = document.getElementById("email").value.trim();
        const pw = document.getElementById("password").value;
        const repw = document.getElementById("repassword").value;
        const name = document.getElementById("fullName").value.trim();
        const phone = document.getElementById("phone").value.trim();

        const pwRules = document.getElementById("pwRules");
        const ruleLength = document.getElementById("ruleLength");
        const ruleUpper = document.getElementById("ruleUpper");
        const ruleNumber = document.getElementById("ruleNumber");
        const ruleSpecial = document.getElementById("ruleSpecial");
        const rePwError = document.getElementById("rePwError");

        pwRules.style.display = "none";
        rePwError.innerText = "";

        // EMAIL
        if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
            document.getElementById("emailError").innerText = "Email không hợp lệ";
            valid = false;
        }

        let hasPwError = false;

        if (pw.length < 8) {
            ruleLength.style.display = "block";
            hasPwError = true;
            valid = false;
        } else ruleLength.style.display = "none";

        if (!/[A-Z]/.test(pw)) {
            ruleUpper.style.display = "block";
            hasPwError = true;
            valid = false;
        } else ruleUpper.style.display = "none";

        if (!/[0-9]/.test(pw)) {
            ruleNumber.style.display = "block";
            hasPwError = true;
            valid = false;
        } else ruleNumber.style.display = "none";

        if (!/[^A-Za-z0-9]/.test(pw)) {
            ruleSpecial.style.display = "block";
            hasPwError = true;
            valid = false;
        } else ruleSpecial.style.display = "none";

        if (hasPwError) pwRules.style.display = "block";

        if (pw !== repw) {
            rePwError.innerText = "Mật khẩu không khớp";
            valid = false;
        }

        if (name === "") {
            document.getElementById("nameError").innerText = "Họ tên không được để trống";
            valid = false;
        }

        if (!/^[0-9]{10}$/.test(phone)) {
            document.getElementById("phoneError").innerText = "Số điện thoại phải gồm 10 chữ số";
            valid = false;
        }

        return valid;
    }
</script>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>

</body>
</html>