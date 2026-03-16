<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Đăng ký - BadmintonPro</title>

    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/badminton-pro.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/auth.css">

    <style>
        .form-input-wrapper {
            position: relative;
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

        .form-input {
            padding-right: 45px;
        }
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
                  accept-charset="UTF-8"
                  class="auth-form"
                  onsubmit="return validateForm()">

                <div class="form-group mb-3">
                    <label class="form-label" for="email">Email *</label>
                    <div class="form-input-wrapper">
                        <i class="bi bi-envelope-fill form-input-icon"></i>
                        <input type="email"
                               class="form-input"
                               id="email"
                               name="email"
                               value="${oldEmail != null ? oldEmail : ''}"
                               autocomplete="email"
                               required>
                    </div>
                    <div class="text-danger small" id="emailError"></div>
                </div>

                <div class="form-group mb-3">
                    <label class="form-label" for="password">Mật khẩu *</label>
                    <div class="form-input-wrapper">
                        <i class="bi bi-lock-fill form-input-icon"></i>
                        <input type="password"
                               class="form-input"
                               id="password"
                               name="password"
                               minlength="6"
                               autocomplete="new-password"
                               required>

                        <button type="button"
                                class="password-toggle"
                                onclick="togglePassword('password', this)">
                            <i class="bi bi-eye-fill"></i>
                        </button>
                    </div>

                    <div id="pwRules" class="small text-danger mt-2" style="display:none;">
                        <div id="ruleLength">Mật khẩu phải có ít nhất 6 ký tự</div>
                    </div>
                </div>

                <div class="form-group mb-3">
                    <label class="form-label" for="repassword">Nhập lại mật khẩu *</label>
                    <div class="form-input-wrapper">
                        <i class="bi bi-shield-lock-fill form-input-icon"></i>
                        <input type="password"
                               class="form-input"
                               id="repassword"
                               name="repassword"
                               minlength="6"
                               autocomplete="new-password"
                               required>

                        <button type="button"
                                class="password-toggle"
                                onclick="togglePassword('repassword', this)">
                            <i class="bi bi-eye-fill"></i>
                        </button>
                    </div>
                    <div class="text-danger small" id="rePwError"></div>
                </div>

                <div class="form-group mb-3">
                    <label class="form-label" for="fullName">Họ và tên *</label>
                    <input type="text"
                           class="form-control"
                           id="fullName"
                           name="fullName"
                           value="${oldFullName != null ? oldFullName : ''}"
                           autocomplete="name"
                           required>
                    <div class="text-danger small" id="nameError"></div>
                </div>

                <div class="form-group mb-3">
                    <label class="form-label" for="phone">Số điện thoại *</label>
                    <input type="tel"
                           class="form-control"
                           id="phone"
                           name="phone"
                           value="${oldPhone != null ? oldPhone : ''}"
                           inputmode="numeric"
                           minlength="10"
                           maxlength="10"
                           autocomplete="tel"
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
                <a href="${pageContext.request.contextPath}/jsp/auth/login.jsp">Đăng nhập ngay</a>
            </p>
        </div>
    </div>
</div>

<script>
    const emailInput = document.getElementById("email");
    const passwordInput = document.getElementById("password");
    const rePasswordInput = document.getElementById("repassword");
    const fullNameInput = document.getElementById("fullName");
    const phoneInput = document.getElementById("phone");

    const pwRules = document.getElementById("pwRules");
    const ruleLength = document.getElementById("ruleLength");
    const rePwError = document.getElementById("rePwError");
    const emailError = document.getElementById("emailError");
    const nameError = document.getElementById("nameError");
    const phoneError = document.getElementById("phoneError");

    function togglePassword(inputId, btn) {
        const input = document.getElementById(inputId);
        const icon = btn.querySelector("i");
        const type = input.type === "password" ? "text" : "password";
        input.type = type;
        icon.classList.toggle("bi-eye-fill");
        icon.classList.toggle("bi-eye-slash-fill");
    }

    function normalizeFullName(value) {
        return (value || "")
            .normalize("NFC")
            .replace(/\s+/g, " ")
            .trim();
    }

    function isValidFullName(value) {
        return /^[\p{L}\s]+$/u.test(value);
    }

    function sanitizePhone(value) {
        return (value || "").replace(/\D/g, "").slice(0, 10);
    }

    fullNameInput.addEventListener("input", function () {
        fullNameInput.setCustomValidity("");
        nameError.innerText = "";
    });

    phoneInput.addEventListener("input", function () {
        const sanitizedPhone = sanitizePhone(phoneInput.value);
        if (phoneInput.value !== sanitizedPhone) {
            phoneInput.value = sanitizedPhone;
        }
        phoneInput.setCustomValidity("");
        phoneError.innerText = "";
    });

    passwordInput.addEventListener("input", function () {
        passwordInput.setCustomValidity("");
        pwRules.style.display = "none";
        ruleLength.style.display = "none";
    });

    rePasswordInput.addEventListener("input", function () {
        rePasswordInput.setCustomValidity("");
        rePwError.innerText = "";
    });

    emailInput.addEventListener("input", function () {
        emailInput.setCustomValidity("");
        emailError.innerText = "";
    });

    function validateForm() {
        let valid = true;

        const email = emailInput.value.trim();
        const password = passwordInput.value;
        const rePassword = rePasswordInput.value;
        const fullName = normalizeFullName(fullNameInput.value);
        const phone = sanitizePhone(phoneInput.value);

        fullNameInput.value = fullName;
        phoneInput.value = phone;

        pwRules.style.display = "none";
        ruleLength.style.display = "none";
        rePwError.innerText = "";
        emailError.innerText = "";
        nameError.innerText = "";
        phoneError.innerText = "";

        if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
            emailError.innerText = "Email không hợp lệ";
            emailInput.setCustomValidity("Email không hợp lệ");
            valid = false;
        } else {
            emailInput.setCustomValidity("");
        }

        if (password.length < 6) {
            pwRules.style.display = "block";
            ruleLength.style.display = "block";
            passwordInput.setCustomValidity("Mật khẩu phải có ít nhất 6 ký tự");
            valid = false;
        } else {
            passwordInput.setCustomValidity("");
        }

        if (password !== rePassword) {
            rePwError.innerText = "Mật khẩu không khớp";
            rePasswordInput.setCustomValidity("Mật khẩu nhập lại không khớp");
            valid = false;
        } else {
            rePasswordInput.setCustomValidity("");
        }

        if (!fullName) {
            nameError.innerText = "Vui lòng nhập họ và tên";
            fullNameInput.setCustomValidity("Vui lòng nhập họ và tên");
            valid = false;
        } else if (!isValidFullName(fullName)) {
            nameError.innerText = "Họ và tên chỉ được chứa chữ cái và khoảng trắng";
            fullNameInput.setCustomValidity("Họ và tên chỉ được chứa chữ cái và khoảng trắng");
            valid = false;
        } else {
            fullNameInput.setCustomValidity("");
        }

        if (!phone) {
            phoneError.innerText = "Vui lòng nhập số điện thoại";
            phoneInput.setCustomValidity("Vui lòng nhập số điện thoại");
            valid = false;
        } else if (!/^\d{10}$/.test(phone)) {
            phoneError.innerText = "Số điện thoại phải gồm đúng 10 chữ số";
            phoneInput.setCustomValidity("Số điện thoại phải gồm đúng 10 chữ số");
            valid = false;
        } else {
            phoneInput.setCustomValidity("");
        }

        return valid;
    }

    passwordInput.addEventListener("invalid", function () {
        if (passwordInput.validity.valueMissing) {
            passwordInput.setCustomValidity("Vui lòng nhập mật khẩu");
        } else if (passwordInput.validity.tooShort) {
            passwordInput.setCustomValidity("Mật khẩu phải có ít nhất 6 ký tự");
        } else {
            passwordInput.setCustomValidity("");
        }
    });

    emailInput.addEventListener("invalid", function () {
        if (emailInput.validity.valueMissing) {
            emailInput.setCustomValidity("Vui lòng nhập email");
        } else if (emailInput.validity.typeMismatch) {
            emailInput.setCustomValidity("Email không hợp lệ");
        } else {
            emailInput.setCustomValidity("");
        }
    });

    fullNameInput.addEventListener("invalid", function () {
        if (fullNameInput.validity.valueMissing) {
            fullNameInput.setCustomValidity("Vui lòng nhập họ và tên");
        } else {
            fullNameInput.setCustomValidity("Họ và tên chỉ được chứa chữ cái và khoảng trắng");
        }
    });

    rePasswordInput.addEventListener("invalid", function () {
        if (rePasswordInput.validity.valueMissing) {
            rePasswordInput.setCustomValidity("Vui lòng nhập lại mật khẩu");
        } else {
            rePasswordInput.setCustomValidity("");
        }
    });

    phoneInput.addEventListener("invalid", function () {
        if (phoneInput.validity.valueMissing) {
            phoneInput.setCustomValidity("Vui lòng nhập số điện thoại");
        } else if (phoneInput.validity.tooShort) {
            phoneInput.setCustomValidity("Số điện thoại phải gồm đúng 10 chữ số");
        } else {
            phoneInput.setCustomValidity("Số điện thoại phải gồm đúng 10 chữ số");
        }
    });
</script>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
