<%-- change_password.jsp --%>

<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/customer/customer-setting.css">

<div class="settings-page">

    <!-- HEADER -->
    <div class="settings-header">
        <div class="settings-title-group">
            <i data-lucide="lock" class="settings-title-icon"></i>
            <h1 class="settings-page-title">Đổi mật khẩu</h1>
        </div>
         <a href="profile?section=settings" class="btn-back-settings">
            <i data-lucide="arrow-left" class="icon-sm"></i>
            <span>Quay lại</span>
        </a>
    </div>

    <!-- FORM -->
    <form id="changePasswordForm" action="customerController" method="post" class="p-6 space-y-6">
        <input type="hidden" name="action" value="updatePassword">

        <!-- Mật khẩu hiện tại -->
        <div class="space-y-2">
            <label class="field-label">
                <i data-lucide="key" class="icon-sm"></i>
                <span>Mật khẩu hiện tại</span>
            </label>
            <div class="relative">
                <input type="password" id="oldPassword" name="oldPassword"
                       required maxlength="20"
                       class="pw-input"
                       placeholder="Nhập mật khẩu hiện tại"
                       oninput="validatePwLength(this)"
                       onblur="validatePwLength(this)">
                <button type="button" class="toggle-password" onclick="togglePw(this)">
                    <i data-lucide="eye-off" class="icon-sm"></i>
                </button>
            </div>
            <p class="pw-error-msg" id="oldPwError"></p>
        </div>

        <!-- Mật khẩu mới -->
        <div class="space-y-2">
            <label class="field-label">
                <i data-lucide="lock" class="icon-sm"></i>
                <span>Mật khẩu mới</span>
            </label>
            <div class="relative">
                <input type="password" id="newPassword" name="newPassword"
                       required minlength="6" maxlength="20"
                       class="pw-input"
                       placeholder="Nhập mật khẩu mới (6–20 ký tự)"
                       oninput="onNewPwInput(this)"
                       onblur="onNewPwInput(this)">
                <button type="button" class="toggle-password" onclick="togglePw(this)">
                    <i data-lucide="eye-off" class="icon-sm"></i>
                </button>
            </div>
            <p class="pw-error-msg" id="newPwError"></p>
            <p id="passwordStrength" style="font-size:0.75rem;font-weight:900;margin-top:0.25rem;"></p>
            <p id="passwordSuggestion" class="strength-suggestion"></p>
        </div>

        <!-- Xác nhận mật khẩu mới -->
        <div class="space-y-2">
            <label class="field-label">
                <i data-lucide="lock" class="icon-sm"></i>
                <span>Xác nhận mật khẩu mới</span>
            </label>
            <div class="relative">
                <input type="password" id="confirmPassword" name="confirmPassword"
                       required maxlength="20"
                       class="pw-input"
                       placeholder="Nhập lại mật khẩu mới"
                       oninput="checkConfirmInline()"
                       onblur="checkConfirmInline()">
                <button type="button" class="toggle-password" onclick="togglePw(this)">
                    <i data-lucide="eye-off" class="icon-sm"></i>
                </button>
            </div>
            <p class="pw-error-msg" id="confirmPwError"></p>
        </div>

        <!-- Buttons -->
        <div class="pw-actions">
            <button type="button" class="btn-pw-save" onclick="submitPwForm()">
                <i data-lucide="save" class="icon-sm"></i>
                <span>Lưu thay đổi</span>
            </button>
            <button type="reset" class="btn-pw-cancel">
                <i data-lucide="x" class="icon-sm"></i>
                <span>Hủy bỏ</span>
            </button>
        </div>

        <!-- Thông báo server -->
        <c:if test="${not empty sessionScope.updateSuccess}">
            <div class="notification success">
                <i data-lucide="check-circle" class="icon-md"></i>
                <span>${sessionScope.updateSuccess}</span>
            </div>
            <c:remove var="updateSuccess" scope="session"/>
        </c:if>
        <c:if test="${not empty sessionScope.updateError}">
            <div class="notification error">
                <i data-lucide="alert-circle" class="icon-md"></i>
                <span>${sessionScope.updateError}</span>
            </div>
            <c:remove var="updateError" scope="session"/>
        </c:if>
    </form>
</div>

<%-- ================================================================
     Inline validation — không dùng DOMContentLoaded vì SPA innerHTML
     ================================================================ --%>
<script>
    function togglePw(btn) {
        var input = btn.previousElementSibling;
        if (!input) return;
        var isText = input.type === 'text';
        input.type = isText ? 'password' : 'text';
        btn.innerHTML = isText
            ? '<i data-lucide="eye-off" style="width:1rem;height:1rem;"></i>'
            : '<i data-lucide="eye"     style="width:1rem;height:1rem;"></i>';
        if (window.lucide) lucide.createIcons();
    }

    function _setPwError(input, errId, msg) {
        var err = document.getElementById(errId);
        if (input) { input.classList.add('input-error'); input.classList.remove('input-valid'); }
        if (err)   { err.textContent = msg; err.classList.add('active'); }
    }
    function _clearPwError(input, errId) {
        var err = document.getElementById(errId);
        if (input) { input.classList.remove('input-error'); }
        if (err)   { err.textContent = ''; err.classList.remove('active'); }
    }
    function _setValid(input) {
        if (input) { input.classList.add('input-valid'); input.classList.remove('input-error'); }
    }

    function validatePwLength(input) {
        var errId = input.id === 'oldPassword' ? 'oldPwError' : 'newPwError';
        var val   = input.value;
        _clearPwError(input, errId);
        if (!val) return true;
        if (val.length < 6)  { _setPwError(input, errId, 'Mật khẩu phải có ít nhất 6 ký tự'); return false; }
        if (val.length > 20) { _setPwError(input, errId, 'Mật khẩu tối đa 20 ký tự'); return false; }
        _setValid(input);
        return true;
    }

    function checkPasswordStrength(val) {
        var strengthEl    = document.getElementById('passwordStrength');
        var suggestionEl  = document.getElementById('passwordSuggestion');
        if (!strengthEl) return;
        if (!val || val.length < 6 || val.length > 20) {
            strengthEl.textContent = '';
            if (suggestionEl) suggestionEl.textContent = '';
            return;
        }
        var strength = 0, suggestions = [];
        if (val.length >= 8) strength += 2; else strength += 1;
        if (/[A-Z]/.test(val)) strength += 1; else suggestions.push('chữ hoa');
        if (/[0-9]/.test(val)) strength += 1; else suggestions.push('chữ số');
        if (/[^A-Za-z0-9]/.test(val)) strength += 1; else suggestions.push('ký tự đặc biệt');
        if (strength >= 4) {
            strengthEl.textContent = 'Mạnh';
            strengthEl.className = 'strength-strong';
            if (suggestionEl) suggestionEl.textContent = '';
        } else if (strength >= 2) {
            strengthEl.textContent = 'Trung bình';
            strengthEl.className = 'strength-medium';
            if (suggestionEl) suggestionEl.textContent = suggestions.length ? 'Gợi ý: thêm ' + suggestions.join(', ') : '';
        } else {
            strengthEl.textContent = 'Yếu';
            strengthEl.className = 'strength-weak';
            if (suggestionEl) suggestionEl.textContent = suggestions.length ? 'Gợi ý: thêm ' + suggestions.join(', ') : '';
        }
    }

    function onNewPwInput(input) {
        validatePwLength(input);
        checkPasswordStrength(input.value);
        checkConfirmInline();
    }

    function checkConfirmInline() {
        var newPw    = document.getElementById('newPassword');
        var confirmPw = document.getElementById('confirmPassword');
        if (!confirmPw || !confirmPw.value) { _clearPwError(confirmPw, 'confirmPwError'); return true; }
        if (newPw && newPw.value !== confirmPw.value) {
            _setPwError(confirmPw, 'confirmPwError', 'Mật khẩu xác nhận không khớp');
            return false;
        }
        _clearPwError(confirmPw, 'confirmPwError');
        _setValid(confirmPw);
        return true;
    }

    function submitPwForm() {
        var oldPw    = document.getElementById('oldPassword');
        var newPw    = document.getElementById('newPassword');
        var confirmPw = document.getElementById('confirmPassword');

        var ok1 = validatePwLength(oldPw);
        var ok2 = validatePwLength(newPw);
        var ok3 = checkConfirmInline();

        if (!oldPw.value) { _setPwError(oldPw, 'oldPwError', 'Vui lòng nhập mật khẩu hiện tại'); ok1 = false; }
        if (!newPw.value) { _setPwError(newPw, 'newPwError', 'Vui lòng nhập mật khẩu mới'); ok2 = false; }
        if (!confirmPw.value) { _setPwError(confirmPw, 'confirmPwError', 'Vui lòng xác nhận mật khẩu'); ok3 = false; }

        if (!ok1 || !ok2 || !ok3) return;

        var confirmed = confirm('Bạn có chắc muốn lưu mật khẩu mới không?');
        if (confirmed) document.getElementById('changePasswordForm').submit();
    }
</script>
