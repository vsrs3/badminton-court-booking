<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%
    String error = (String) request.getAttribute("error");
    String step = (String) request.getAttribute("step");
%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Quên mật khẩu - BadmintonPro</title>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">

    <!-- Bootstrap -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">

    <!-- Icons -->
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.css">

    <!-- Custom CSS -->
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
            padding: 0;
            z-index: 5;
        }

        .password-toggle i {
            font-size: 1.1rem;
            color: #666;
        }

        .form-input {
            padding-right: 45px; /* chừa chỗ cho icon */
        }
    </style>
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
        <div class="auth-header">
            <div class="auth-logo">
                <i class="bi bi-trophy-fill"></i>
            </div>
            <h1 class="auth-brand-name">
                CHỌN SÂN <span class="highlight">CHƠI NGAY</span>
            </h1>
            <p class="auth-tagline">Khôi phục mật khẩu tài khoản</p>
        </div>

        <!-- Body -->
        <div class="auth-body">

            <h2 class="auth-title">Quên mật khẩu</h2>
            <p class="auth-subtitle">Lấy lại quyền truy cập của bạn</p>
            <% if (error != null) { %>
            <div class="mt-3 p-3 rounded"
                 style="background:#FEE2E2;border:1px solid #EF4444;">
                <i class="bi bi-exclamation-triangle-fill text-danger"></i>
                <span class="text-danger fw-semibold">
                    <%= error %>
                </span>
            </div>
            <% } %>

            <% if (step == null) { %>

            <!-- ================= STEP 1 ================= -->
            <form method="post"
                  action="${pageContext.request.contextPath}/forgot-password"
                  class="auth-form mt-4">

                <input type="hidden" name="action" value="checkEmail"/>

                <div class="form-group">
                    <label class="form-label">Email <span class="required">*</span></label>
                    <div class="form-input-wrapper">
                        <i class="bi bi-envelope-fill form-input-icon"></i>
                        <input type="email"
                               name="email"
                               class="form-input"
                               placeholder="you@example.com"
                               required>
                    </div>
                </div>

                <button type="submit" class="auth-submit-btn mt-3">
                    <i class="bi bi-send-fill"></i>
                    <span>Xác nhận</span>
                </button>

            </form>

            <% } else { %>

            <!-- ================= STEP 2 ================= -->
            <form method="post"
                  action="${pageContext.request.contextPath}/forgot-password"
                  onsubmit="return validateForm()"
                  class="auth-form mt-4">

                <input type="hidden" name="action" value="reset"/>
                <input type="hidden" name="email"
                       value="<%= request.getAttribute("email") %>"/>

                <!-- PASSWORD -->
                <div class="form-group">
                    <label class="form-label">Mật khẩu mới</label>
                    <div class="form-input-wrapper">
                        <i class="bi bi-lock-fill form-input-icon"></i>
                        <input type="password"
                               id="password"
                               name="password"
                               class="form-input"
                               required>
                        <button type="button" class="password-toggle" id="togglePassword1">
                            <i class="bi bi-eye-fill"></i>
                        </button>
                    </div>
                </div>

                <!-- CONFIRM -->
                <div class="form-group">
                    <label class="form-label">Nhập lại mật khẩu</label>
                    <div class="form-input-wrapper">
                        <i class="bi bi-shield-lock-fill form-input-icon"></i>
                        <input type="password"
                               id="repassword"
                               name="repassword"
                               class="form-input"
                               required>
                        <button type="button" class="password-toggle" id="togglePassword2">
                            <i class="bi bi-eye-fill"></i>
                        </button>
                    </div>
                </div>

                <!-- RULES -->
                <div id="ruleContainer"
                     class="mt-2 small text-danger"
                     style="display:none;">
                    <div id="ruleLength">≥ 8 ký tự</div>
                    <div id="ruleUpper">Ít nhất 1 chữ in hoa</div>
                    <div id="ruleNumber">Ít nhất 1 chữ số</div>
                    <div id="ruleSpecial">Ít nhất 1 ký tự đặc biệt</div>
                </div>

                <div id="matchError"
                     class="text-danger small mt-2"
                     style="display:none;">
                    Mật khẩu không khớp
                </div>

                <button type="submit" class="auth-submit-btn mt-3">
                    <i class="bi bi-check-circle-fill"></i>
                    <span>Lưu mật khẩu</span>
                </button>

            </form>

            <% } %>

        </div>


    </div>
</div>

<script>
    function validateForm() {

        const pw = document.getElementById("password").value;
        const repw = document.getElementById("repassword").value;

        const ruleContainer = document.getElementById("ruleContainer");
        const ruleLength = document.getElementById("ruleLength");
        const ruleUpper = document.getElementById("ruleUpper");
        const ruleNumber = document.getElementById("ruleNumber");
        const ruleSpecial = document.getElementById("ruleSpecial");

        const matchError = document.getElementById("matchError");

        let isValid = true;
        let hasAnyRuleError = false;

        // ===== CHECK ≥ 8 ký tự =====
        if (pw.length < 8) {
            ruleLength.style.display = "block";
            hasAnyRuleError = true;
            isValid = false;
        } else {
            ruleLength.style.display = "none";
        }

        // ===== CHECK chữ in hoa =====
        if (!/[A-Z]/.test(pw)) {
            ruleUpper.style.display = "block";
            hasAnyRuleError = true;
            isValid = false;
        } else {
            ruleUpper.style.display = "none";
        }

        // ===== CHECK chữ số =====
        if (!/[0-9]/.test(pw)) {
            ruleNumber.style.display = "block";
            hasAnyRuleError = true;
            isValid = false;
        } else {
            ruleNumber.style.display = "none";
        }

        // ===== CHECK ký tự đặc biệt =====
        if (!/[^A-Za-z0-9]/.test(pw)) {
            ruleSpecial.style.display = "block";
            hasAnyRuleError = true;
            isValid = false;
        } else {
            ruleSpecial.style.display = "none";
        }

        // ===== Hiển thị container nếu có lỗi =====
        if (hasAnyRuleError) {
            ruleContainer.style.display = "block";
        } else {
            ruleContainer.style.display = "none";
        }

        // ===== Confirm password =====
        if (pw !== repw) {
            matchError.style.display = "block";
            isValid = false;
        } else {
            matchError.style.display = "none";
        }

        return isValid;
    }
</script>

<script>
    document.addEventListener("DOMContentLoaded", function() {

        const toggle1 = document.getElementById("togglePassword1");
        const toggle2 = document.getElementById("togglePassword2");

        const password = document.getElementById("password");
        const repassword = document.getElementById("repassword");

        function setupToggle(toggleBtn, inputField) {
            if (!toggleBtn) return;

            toggleBtn.addEventListener("click", function() {
                const type = inputField.type === "password" ? "text" : "password";
                inputField.type = type;

                const icon = this.querySelector("i");
                if (type === "password") {
                    icon.classList.remove("bi-eye-slash-fill");
                    icon.classList.add("bi-eye-fill");
                } else {
                    icon.classList.remove("bi-eye-fill");
                    icon.classList.add("bi-eye-slash-fill");
                }
            });
        }

        setupToggle(toggle1, password);
        setupToggle(toggle2, repassword);

    });
</script>
</body>
</html>