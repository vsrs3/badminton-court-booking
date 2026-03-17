<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%
    String error = (String) request.getAttribute("error");
    String message = (String) request.getAttribute("message");
    String step = (String) request.getAttribute("step");
    String email = request.getAttribute("email") == null ? "" : request.getAttribute("email").toString();
    String token = request.getAttribute("token") == null ? "" : request.getAttribute("token").toString();
    boolean isResetStep = "reset".equals(step);
%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Quên mật khẩu - BadmintonPro</title>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">

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
            padding: 0;
            z-index: 5;
        }

        .password-toggle i {
            font-size: 1.1rem;
            color: #666;
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
        <div class="auth-header">
            <div class="auth-logo">
                <i class="bi bi-trophy-fill"></i>
            </div>
            <h1 class="auth-brand-name">
                CHỌN SÂN <span class="highlight">CHƠI NGAY</span>
            </h1>
            <p class="auth-tagline">Khôi phục mật khẩu tài khoản</p>
        </div>

        <div class="auth-body">
            <h2 class="auth-title">Quên mật khẩu</h2>
            <p class="auth-subtitle">Lấy lại quyền truy cập của bạn</p>

            <% if (error != null) { %>
            <div class="mt-3 p-3 rounded" style="background:#FEE2E2;border:1px solid #EF4444;">
                <i class="bi bi-exclamation-triangle-fill text-danger"></i>
                <span class="text-danger fw-semibold"><%= error %></span>
            </div>
            <% } %>

            <% if (message != null) { %>
            <div class="mt-3 p-3 rounded" style="background:#ECFDF5;border:1px solid #10B981;">
                <i class="bi bi-envelope-check-fill text-success"></i>
                <span class="text-success fw-semibold"><%= message %></span>
            </div>
            <% } %>

            <% if (message != null && !isResetStep) { %>
            <div class="mt-3 small" style="color:#166534;">
                Sau khi bạn nhấn link xác nhận trong email, trang này sẽ tự chuyển sang bước tạo mật khẩu mới.
            </div>
            <% } %>

            <% if (!isResetStep) { %>
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
                               value="<%= email %>"
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
            <form method="post"
                  action="${pageContext.request.contextPath}/forgot-password"
                  onsubmit="return validateForm()"
                  class="auth-form mt-4">

                <input type="hidden" name="action" value="reset"/>
                <input type="hidden" name="token" value="<%= token %>"/>

                <div class="mb-3 small" style="color:#166534;">
                    Bạn đang đổi mật khẩu cho tài khoản:
                    <strong><%= email %></strong>
                </div>

                <div class="form-group">
                    <label class="form-label">Mật khẩu mới</label>
                    <div class="form-input-wrapper">
                        <i class="bi bi-lock-fill form-input-icon"></i>
                        <input type="password"
                               id="password"
                               name="password"
                               class="form-input"
                               minlength="6"
                               required>
                        <button type="button" class="password-toggle" id="togglePassword1">
                            <i class="bi bi-eye-fill"></i>
                        </button>
                    </div>
                </div>

                <div id="lengthError" class="text-danger small mt-2" style="display:none;">
                    Mật khẩu phải có ít nhất 6 ký tự.
                </div>

                <div class="form-group mt-3">
                    <label class="form-label">Nhập lại mật khẩu</label>
                    <div class="form-input-wrapper">
                        <i class="bi bi-shield-lock-fill form-input-icon"></i>
                        <input type="password"
                               id="repassword"
                               name="repassword"
                               class="form-input"
                               minlength="6"
                               required>
                        <button type="button" class="password-toggle" id="togglePassword2">
                            <i class="bi bi-eye-fill"></i>
                        </button>
                    </div>
                </div>

                <div id="matchError" class="text-danger small mt-2" style="display:none;">
                    Mật khẩu nhập lại phải trùng với mật khẩu mới.
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
    const FORGOT_PASSWORD_SYNC_KEY = "bcb.forgot-password.verification";
    const pendingEmail = "<%= email %>";
    const waitingForEmailConfirmation = <%= (!isResetStep && message != null) ? "true" : "false" %>;
    let forgotPasswordSyncPoll = null;
    let forgotPasswordStatusRequestInFlight = false;

    function parseForgotPasswordPayload(rawValue) {
        if (!rawValue) {
            return null;
        }

        try {
            return JSON.parse(rawValue);
        } catch (error) {
            return null;
        }
    }

    function stopForgotPasswordSyncPoll() {
        if (!forgotPasswordSyncPoll) {
            return;
        }

        clearInterval(forgotPasswordSyncPoll);
        forgotPasswordSyncPoll = null;
    }

    function handleForgotPasswordVerification(payload) {
        if (!waitingForEmailConfirmation || !payload || !payload.redirectUrl) {
            return false;
        }

        if (pendingEmail && payload.email && pendingEmail.toLowerCase() !== payload.email.toLowerCase()) {
            return false;
        }

        stopForgotPasswordSyncPoll();
        localStorage.removeItem(FORGOT_PASSWORD_SYNC_KEY);
        window.location.href = payload.redirectUrl;
        return true;
    }

    function checkForgotPasswordVerification() {
        return handleForgotPasswordVerification(
            parseForgotPasswordPayload(localStorage.getItem(FORGOT_PASSWORD_SYNC_KEY))
        );
    }

    async function checkForgotPasswordVerificationFromServer() {
        if (!waitingForEmailConfirmation || forgotPasswordStatusRequestInFlight) {
            return false;
        }

        forgotPasswordStatusRequestInFlight = true;

        try {
            const response = await fetch(
                "${pageContext.request.contextPath}/email-action-status?purpose=forgot-password&email="
                + encodeURIComponent(pendingEmail ? pendingEmail : ""),
                {
                    cache: "no-store"
                }
            );

            if (!response.ok) {
                return false;
            }

            const payload = await response.json();
            if (payload.status === "confirmed" && payload.continueUrl) {
                stopForgotPasswordSyncPoll();
                window.location.href = payload.continueUrl;
                return true;
            }
        } catch (error) {
            console.log("Khong kiem tra duoc trang thai xac nhan:", error);
        } finally {
            forgotPasswordStatusRequestInFlight = false;
        }

        return false;
    }

    function syncForgotPasswordVerification() {
        if (checkForgotPasswordVerification()) {
            return;
        }

        checkForgotPasswordVerificationFromServer();
    }

    function validateForm() {
        const passwordInput = document.getElementById("password");
        const repasswordInput = document.getElementById("repassword");

        if (!passwordInput || !repasswordInput) {
            return true;
        }

        const password = passwordInput.value;
        const repassword = repasswordInput.value;
        const lengthError = document.getElementById("lengthError");
        const matchError = document.getElementById("matchError");

        let isValid = true;

        if (password.length < 6) {
            lengthError.style.display = "block";
            isValid = false;
        } else {
            lengthError.style.display = "none";
        }

        if (password !== repassword) {
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
        const password = document.getElementById("password");
        const repassword = document.getElementById("repassword");
        const toggle1 = document.getElementById("togglePassword1");
        const toggle2 = document.getElementById("togglePassword2");

        function setupToggle(toggleBtn, inputField) {
            if (!toggleBtn || !inputField) {
                return;
            }

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

        if (password) {
            password.addEventListener("input", validateForm);
        }

        if (repassword) {
            repassword.addEventListener("input", validateForm);
        }

        setupToggle(toggle1, password);
        setupToggle(toggle2, repassword);

        if (waitingForEmailConfirmation) {
            syncForgotPasswordVerification();

            window.addEventListener("storage", function(event) {
                if (event.key === FORGOT_PASSWORD_SYNC_KEY) {
                    handleForgotPasswordVerification(parseForgotPasswordPayload(event.newValue));
                }
            });

            document.addEventListener("visibilitychange", function() {
                if (!document.hidden) {
                    syncForgotPasswordVerification();
                }
            });

            window.addEventListener("focus", syncForgotPasswordVerification);

            forgotPasswordSyncPoll = setInterval(syncForgotPasswordVerification, 1000);
        }
    });
</script>
</body>
</html>
