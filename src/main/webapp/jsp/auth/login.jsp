<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Đăng nhập - BadmintonPro</title>

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

    <!-- Back to Home Button -->
    <div class="back-to-home">
        <a href="${pageContext.request.contextPath}/" class="back-btn">
            <i class="bi bi-arrow-left"></i>
            <span>Trang chủ</span>
        </a>
    </div>

    <!-- Auth Card -->
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
        <div class="auth-body">
            <h2 class="auth-title">Đăng nhập</h2>
            <p class="auth-subtitle">Chào mừng bạn trở lại!</p>

            <!-- Login Form -->
            <form id="loginForm" class="auth-form" method="POST" action="${pageContext.request.contextPath}/auth/login">

                <!-- Email Field -->
                <div class="form-group">
                    <label for="email" class="form-label">
                        Email <span class="required">*</span>
                    </label>
                    <div class="form-input-wrapper">
                        <i class="bi bi-envelope-fill form-input-icon"></i>
                        <input
                                type="email"
                                id="email"
                                name="email"
                                class="form-input"
                                placeholder="you@example.com"
                                required
                                autocomplete="email"
                        />
                    </div>
                    <div class="form-error" id="emailError" style="display: none;">
                        <i class="bi bi-exclamation-circle-fill"></i>
                        <span>Email không hợp lệ</span>
                    </div>
                </div>

                <!-- Password Field -->
                <div class="form-group">
                    <label for="password" class="form-label">
                        Mật khẩu <span class="required">*</span>
                    </label>
                    <div class="form-input-wrapper">
                        <i class="bi bi-lock-fill form-input-icon"></i>
                        <input
                                type="password"
                                id="password"
                                name="password"
                                class="form-input"
                                placeholder="••••••••"
                                required
                                autocomplete="current-password"
                        />
                        <button type="button" class="password-toggle" id="togglePassword">
                            <i class="bi bi-eye-fill"></i>
                        </button>
                    </div>
                    <div class="form-error" id="passwordError" style="display: none;">
                        <i class="bi bi-exclamation-circle-fill"></i>
                        <span>Mật khẩu phải có ít nhất 6 ký tự</span>
                    </div>
                </div>

                <!-- Remember Me & Forgot Password -->
                <div class="form-options">
                    <div class="form-checkbox-group">
                        <input
                                type="checkbox"
                                id="rememberMe"
                                name="rememberMe"
                                class="form-checkbox"
                        />
                        <label for="rememberMe" class="form-checkbox-label">
                            Ghi nhớ đăng nhập
                        </label>
                    </div>
                    <a href="${pageContext.request.contextPath}/auth/forgot-password" class="form-link">
                        Quên mật khẩu?
                    </a>
                </div>

                <!-- Submit Button -->
                <button type="submit" class="auth-submit-btn" id="submitBtn">
                    <i class="bi bi-box-arrow-in-right"></i>
                    <span>Đăng nhập</span>
                </button>

            </form>
            <!-- Server Error Display -->
            <% if (request.getAttribute("error") != null) { %>
            <div style="margin-top: 1rem; padding: 0.875rem; background-color: #FEE2E2; border: 1px solid #EF4444; border-radius: 0.5rem; display: flex; align-items: center; gap: 0.5rem;">
                <i class="bi bi-exclamation-triangle-fill" style="color: #EF4444; font-size: 1.25rem;"></i>
                <span style="color: #B91C1C; font-weight: 600; font-size: 0.875rem;">
        <%= request.getAttribute("error") %>
             </span>
            </div>
            <% } %>
            <!-- Divider -->
            <div class="auth-divider">
                <span>Hoặc tiếp tục với</span>
            </div>

            <!-- Social Login (Optional) -->
            <div class="social-login">
                <button type="button" class="social-btn google">
                    <i class="bi bi-google"></i>
                    <span>Google</span>
                </button>
                <button type="button" class="social-btn facebook">
                    <i class="bi bi-facebook"></i>
                    <span>Facebook</span>
                </button>
            </div>

        </div>

        <!-- Footer -->
        <div class="auth-footer">
            <p>
                Chưa có tài khoản?
                <a href="${pageContext.request.contextPath}/auth/register">Đăng ký ngay</a>
            </p>
        </div>

    </div>

</div>

<!-- Bootstrap JS -->
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
<!-- Login Page JavaScript -->
<script>
    (function() {
        'use strict';

        // ============================================
        // FORM ELEMENTS
        // ============================================

        const loginForm = document.getElementById('loginForm');
        const emailInput = document.getElementById('email');
        const passwordInput = document.getElementById('password');
        const togglePasswordBtn = document.getElementById('togglePassword');
        const submitBtn = document.getElementById('submitBtn');

        const emailError = document.getElementById('emailError');
        const passwordError = document.getElementById('passwordError');

        // Show server-side error if exists
        <% if (request.getAttribute("error") != null) { %>
        alert('<%= request.getAttribute("error") %>');
        <% } %>

        // ============================================
        // PASSWORD TOGGLE
        // ============================================

        if (togglePasswordBtn) {
            togglePasswordBtn.addEventListener('click', function() {
                const type = passwordInput.type === 'password' ? 'text' : 'password';
                passwordInput.type = type;

                const icon = this.querySelector('i');
                if (type === 'password') {
                    icon.classList.remove('bi-eye-slash-fill');
                    icon.classList.add('bi-eye-fill');
                } else {
                    icon.classList.remove('bi-eye-fill');
                    icon.classList.add('bi-eye-slash-fill');
                }
            });
        }

        // ============================================
        // VALIDATION FUNCTIONS
        // ============================================

        function validateEmail(email) {
            const re = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
            return re.test(email);
        }

        function validatePassword(password) {
            return password.length >= 6;
        }

        function showError(inputElement, errorElement, message) {
            inputElement.classList.add('error');
            errorElement.querySelector('span').textContent = message;
            errorElement.style.display = 'flex';
        }

        function hideError(inputElement, errorElement) {
            inputElement.classList.remove('error');
            errorElement.style.display = 'none';
        }

        // ============================================
        // REAL-TIME VALIDATION
        // ============================================

        emailInput.addEventListener('blur', function() {
            if (this.value && !validateEmail(this.value)) {
                showError(this, emailError, 'Email không hợp lệ');
            } else {
                hideError(this, emailError);
            }
        });

        passwordInput.addEventListener('blur', function() {
            if (this.value && !validatePassword(this.value)) {
                showError(this, passwordError, 'Mật khẩu phải có ít nhất 6 ký tự');
            } else {
                hideError(this, passwordError);
            }
        });

        // Clear error on input
        emailInput.addEventListener('input', function() {
            hideError(this, emailError);
        });

        passwordInput.addEventListener('input', function() {
            hideError(this, passwordError);
        });

        // ============================================
        // FORM SUBMISSION
        // ============================================

        loginForm.addEventListener('submit', function(e) {
            // Validate before submit
            let isValid = true;

            // Validate email
            if (!emailInput.value.trim()) {
                showError(emailInput, emailError, 'Vui lòng nhập email');
                isValid = false;
            } else if (!validateEmail(emailInput.value.trim())) {
                showError(emailInput, emailError, 'Email không hợp lệ');
                isValid = false;
            }

            // Validate password
            if (!passwordInput.value) {
                showError(passwordInput, passwordError, 'Vui lòng nhập mật khẩu');
                isValid = false;
            } else if (!validatePassword(passwordInput.value)) {
                showError(passwordInput, passwordError, 'Mật khẩu phải có ít nhất 6 ký tự');
                isValid = false;
            }

            if (!isValid) {
                e.preventDefault();
                return;
            }

            // Show loading state
            submitBtn.disabled = true;
            submitBtn.classList.add('loading');
            submitBtn.innerHTML = '<i class="bi bi-hourglass-split"></i><span>Đang xử lý...</span>';

            // Form will submit normally to server
        });

    })();
</script>

</body>
</html>