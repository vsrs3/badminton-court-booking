<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Đăng ký - BadmintonPro</title>

    <!-- Bootstrap -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.css">

    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/badminton-pro.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/auth.css">

    <style>
        .form-input-wrapper { position: relative; }

        /* Ẩn icon tự sinh trong auth.css nếu có */
        .form-input-wrapper i.bi-eye-fill.form-input-icon,
        .form-input-wrapper i.bi-eye-slash-fill.form-input-icon {
            display: none !important;
        }

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
                               required>
                    </div>
                    <div class="text-danger small" id="emailError"></div>
                </div>

                <!-- PASSWORD -->
                <div class="form-group mb-3">
                        <label class="form-label">Mật khẩu *</label>
                        <div class="form-input-wrapper">
                            <i class="bi bi-lock-fill form-input-icon"></i>
                            <input type="password"
                                   class="form-input"
                                   id="password"
                                   name="password"
                                   required>
                        </div>

                    <!-- RULES -->
                    <div id="pwRules" class="small text-danger mt-2" style="display:none;">
                        <div id="ruleLength">≥ 8 ký tự</div>
                        <div id="ruleUpper">Ít nhất 1 chữ in hoa</div>
                        <div id="ruleNumber">Ít nhất 1 chữ số</div>
                        <div id="ruleSpecial">Ít nhất 1 ký tự đặc biệt</div>
                    </div>
                </div>

                <!-- CONFIRM PASSWORD -->
                <div class="form-group mb-3">

                    <label class="form-label">Nhập lại mật khẩu *</label>
                    <div class="form-input-wrapper">
                        <i class="bi bi-shield-lock-fill form-input-icon"></i>
                        <input type="password"
                               class="form-input"
                               id="repassword"
                               name="repassword"
                               required>
                    </div>

                    <div class="text-danger small" id="rePwError"></div>
                </div>

                <!-- FULL NAME -->
                <div class="form-group mb-3">
                    <label class="form-label">Họ và tên *</label>
                    <input type="text" class="form-control"
                           id="fullName" name="fullName" required>
                    <div class="text-danger small" id="nameError"></div>
                </div>

                <!-- PHONE -->
                <div class="form-group mb-3">
                    <label class="form-label">Số điện thoại *</label>
                    <input type="text" class="form-control"
                           id="phone" name="phone" required>
                    <div class="text-danger small" id="phoneError"></div>
                </div>

                <button type="submit" class="auth-submit-btn w-100">
                    <i class="bi bi-person-check-fill"></i>
                    <span>Đăng ký</span>
                </button>

            </form>

        </div>
    </div>
</div>

<script>
    function togglePassword(btn) {
        const wrapper = btn.closest(".form-input-wrapper");
        const input = wrapper.querySelector("input");
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

        document.querySelectorAll(".text-danger.small").forEach(e => e.innerText = "");
        pwRules.style.display = "none";

        if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
            document.getElementById("emailError").innerText = "Email không hợp lệ";
            valid = false;
        }

        let hasPwError = false;

        if (pw.length < 8) { ruleLength.style.display = "block"; hasPwError = true; valid = false; }
        else ruleLength.style.display = "none";

        if (!/[A-Z]/.test(pw)) { ruleUpper.style.display = "block"; hasPwError = true; valid = false; }
        else ruleUpper.style.display = "none";

        if (!/[0-9]/.test(pw)) { ruleNumber.style.display = "block"; hasPwError = true; valid = false; }
        else ruleNumber.style.display = "none";

        if (!/[^A-Za-z0-9]/.test(pw)) { ruleSpecial.style.display = "block"; hasPwError = true; valid = false; }
        else ruleSpecial.style.display = "none";

        if (hasPwError) pwRules.style.display = "block";

        if (pw !== repw) {
            document.getElementById("rePwError").innerText = "Mật khẩu không khớp";
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

    document.addEventListener("DOMContentLoaded", function () {

        const passwordInputs = document.querySelectorAll('input[type="password"]');

        passwordInputs.forEach(input => {

            const wrapper = input.closest(".form-input-wrapper");

            const toggleBtn = document.createElement("button");
            toggleBtn.type = "button";
            toggleBtn.className = "password-toggle";

            toggleBtn.innerHTML = '<i class="bi bi-eye-fill"></i>';

            wrapper.appendChild(toggleBtn);

            toggleBtn.addEventListener("click", function () {
                const type = input.type === "password" ? "text" : "password";
                input.type = type;

                const icon = this.querySelector("i");
                icon.classList.toggle("bi-eye-fill");
                icon.classList.toggle("bi-eye-slash-fill");
            });
        });

    });
</script>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>

</body>
</html>