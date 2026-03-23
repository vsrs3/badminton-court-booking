<%-- staff-password.jsp --%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ include file="../layout/staff-layout.jsp"%>
<%@ include file="../layout/staff-sidebar.jsp"%>

<link rel="stylesheet"
    href="${pageContext.request.contextPath}/assets/css/owner/dashboard.css">
<style>
.content-area { padding: 0 !important; }
</style>

<div class="main-content">
    <%@ include file="../layout/staff-header.jsp"%>

    <div class="dov-wrap">
        <%-- ============================================================
             CHANGE PASSWORD CARD
        ============================================================ --%>
        <div class="dov-card dov-fadein dov-d1">

            <%-- Card header --%>
            <div class="dov-card-header">
                <div style="display:flex; align-items:center; gap:8px;">
                    <div class="dov-stat-icon dov-stat-icon--green"
                         style="width:38px; height:38px; border-radius:10px; font-size:1rem;">
                        <i class="bi bi-key-fill"></i>
                    </div>
                    <h3 class="dov-card-title" style="font-size:1.125rem; font-weight:900;
                        text-transform:uppercase; letter-spacing:.04em; color:#064E3B;">
                        Đổi Mật Khẩu
                    </h3>
                </div>
                <a href="${pageContext.request.contextPath}/staff/setting/"
                   class="sof-btn-back">
                    <i class="bi bi-arrow-left"></i>
                    <span>Quay lại</span>
                </a>
            </div>

            <%-- Form --%>
            <form id="staffChangePwForm"
                  action="${pageContext.request.contextPath}/staff/setting/change-password"
                  method="post"
                  class="sof-form">
                <input type="hidden" name="action" value="updatePassword">

                <%-- Mật khẩu hiện tại --%>
                <div class="sof-field">
                    <label class="sof-label">
                        <i class="bi bi-key"></i>
                        <span>Mật khẩu hiện tại</span>
                    </label>
                    <div class="sof-input-wrap">
                        <input type="password" id="staffOldPassword" name="oldPassword"
                               required maxlength="20"
                               class="sof-input"
                               placeholder="Nhập mật khẩu hiện tại"
                               oninput="staffValidatePwLength(this)"
                               onblur="staffValidatePwLength(this)">
                        <button type="button" class="sof-toggle-pw" onclick="staffTogglePw(this)">
                            <i class="bi bi-eye-slash"></i>
                        </button>
                    </div>
                    <p class="sof-field-error" id="staffOldPwError"></p>
                </div>

                <%-- Mật khẩu mới --%>
                <div class="sof-field">
                    <label class="sof-label">
                        <i class="bi bi-lock"></i>
                        <span>Mật khẩu mới</span>
                    </label>
                    <div class="sof-input-wrap">
                        <input type="password" id="staffNewPassword" name="newPassword"
                               required minlength="6" maxlength="20"
                               class="sof-input"
                               placeholder="Nhập mật khẩu mới (6–20 ký tự)"
                               oninput="staffOnNewPwInput(this)"
                               onblur="staffOnNewPwInput(this)">
                        <button type="button" class="sof-toggle-pw" onclick="staffTogglePw(this)">
                            <i class="bi bi-eye-slash"></i>
                        </button>
                    </div>
                    <p class="sof-field-error" id="staffNewPwError"></p>
                    <p id="staffPasswordStrength" style="margin-top:.25rem; font-size:.75rem; font-weight:900; text-transform:uppercase; letter-spacing:.05em;"></p>
                    <p id="staffPasswordSuggestion" style="font-size:.6875rem; font-weight:700; color:#9CA3AF; margin-top:.25rem;"></p>
                </div>

                <%-- Xác nhận mật khẩu mới --%>
                <div class="sof-field">
                    <label class="sof-label">
                        <i class="bi bi-lock-fill"></i>
                        <span>Xác nhận mật khẩu mới</span>
                    </label>
                    <div class="sof-input-wrap">
                        <input type="password" id="staffConfirmPassword" name="confirmPassword"
                               required maxlength="20"
                               class="sof-input"
                               placeholder="Nhập lại mật khẩu mới"
                               oninput="staffCheckConfirmInline()"
                               onblur="staffCheckConfirmInline()">
                        <button type="button" class="sof-toggle-pw" onclick="staffTogglePw(this)">
                            <i class="bi bi-eye-slash"></i>
                        </button>
                    </div>
                    <p class="sof-field-error" id="staffConfirmPwError"></p>
                </div>

                <%-- Buttons --%>
                <div class="sof-actions">
                    <button type="button" class="sof-btn-save" onclick="staffSubmitPwForm()">
                        <i class="bi bi-floppy-fill"></i>
                        <span>Lưu thay đổi</span>
                    </button>
                    <button type="reset" class="sof-btn-cancel">
                        <i class="bi bi-x-lg"></i>
                        <span>Hủy bỏ</span>
                    </button>
                </div>
            </form>

        </div>
        <%-- end card --%>
        
         <%-- ============================================================
             ERROR / SUCCESS ALERTS
        ============================================================ --%>
        <c:if test="${not empty requestScope.error}">
            <div class="alert alert-danger d-flex align-items-center gap-2 rounded-3 mb-4" role="alert">
                <i class="bi bi-exclamation-triangle-fill flex-shrink-0"></i>
                <span class="fw-semibold">${requestScope.error}</span>
            </div>
        </c:if>
        <c:if test="${not empty sessionScope.updateError}">
            <div class="alert alert-danger d-flex align-items-center gap-2 rounded-3 mb-4" role="alert">
                <i class="bi bi-exclamation-triangle-fill flex-shrink-0"></i>
                <span class="fw-semibold">${sessionScope.updateError}</span>
            </div>
            <c:remove var="updateError" scope="session"/>
        </c:if>
        <c:if test="${not empty sessionScope.updateSuccess}">
            <div class="alert alert-success d-flex align-items-center gap-2 rounded-3 mb-4" role="alert">
                <i class="bi bi-check-circle-fill flex-shrink-0"></i>
                <span class="fw-semibold">${sessionScope.updateSuccess}</span>
            </div>
            <c:remove var="updateSuccess" scope="session"/>
        </c:if>
        

    </div>
    <%-- end dov-wrap --%>
</div>
<%-- end main-content --%>

<%-- ── Shared form styles ── --%>
<style>
/* ── Back button ─────────────────────────────────────────────── */
.sof-btn-back {
    display: inline-flex;
    align-items: center;
    gap: .5rem;
    padding: .5rem 1rem .5rem .625rem;
    background: #F0FDF4;
    color: #064E3B;
    border: 1.5px solid #A3E635;
    border-radius: 9999px;
    font-size: .8125rem;
    font-weight: 700;
    text-decoration: none;
    transition: all .2s ease;
    white-space: nowrap;
}
.sof-btn-back:hover {
    background: #064E3B;
    border-color: #064E3B;
    color: #fff;
    transform: translateY(-1px);
    box-shadow: 0 4px 10px rgba(6,78,59,.2);
}
.sof-btn-back i { transition: transform .2s ease; }
.sof-btn-back:hover i { transform: translateX(-3px); color: #fff; }

/* ── Form layout ─────────────────────────────────────────────── */
.sof-form {
    display: flex;
    flex-direction: column;
    gap: 1.5rem;
}
.sof-field { display: flex; flex-direction: column; gap: .25rem; }

/* ── Label ───────────────────────────────────────────────────── */
.sof-label {
    display: flex;
    align-items: center;
    gap: .375rem;
    font-size: .6875rem;
    font-weight: 900;
    text-transform: uppercase;
    letter-spacing: .1em;
    color: #064E3B;
    margin-bottom: .25rem;
}
.sof-label i { color: #064E3B; flex-shrink: 0; }

/* ── Input ───────────────────────────────────────────────────── */
.sof-input-wrap { position: relative; }
.sof-input {
    width: 100%;
    padding: .875rem 3rem .875rem 1rem;
    border-radius: .75rem;
    border: 1px solid #E5E7EB;
    font-family: 'Be Vietnam Pro', 'Inter', sans-serif;
    font-size: .9375rem;
    font-weight: 500;
    color: #1F2937;
    background: #fff;
    outline: none;
    transition: border-color .15s, box-shadow .15s;
    box-sizing: border-box;
}
.sof-input:focus {
    border-color: #064E3B;
    box-shadow: 0 0 0 3px rgba(163,230,53,.2);
}
.sof-input.input-error  { border-color: #EF4444 !important; background: #fef2f2; }
.sof-input.input-valid  { border-color: #10B981; }
.sof-input[readonly]    { background: #F9FAFB; cursor: not-allowed; color: #6B7280; }

/* ── Toggle password ─────────────────────────────────────────── */
.sof-toggle-pw {
    position: absolute;
    right: 1rem;
    top: 50%;
    transform: translateY(-50%);
    background: none;
    border: none;
    cursor: pointer;
    color: #9CA3AF;
    padding: .25rem;
    line-height: 1;
    font-size: 1rem;
}
.sof-toggle-pw:hover { color: #374151; }

/* ── Inline error ────────────────────────────────────────────── */
.sof-field-error {
    display: none;
    align-items: center;
    gap: .25rem;
    font-size: .6875rem;
    font-weight: 700;
    text-transform: uppercase;
    letter-spacing: .05em;
    color: #D97706;
    margin: 0;
}
.sof-field-error.active { display: flex; }

/* ── Action buttons ──────────────────────────────────────────── */
.sof-actions {
    display: flex;
    gap: 1.25rem;
    flex-wrap: wrap;
    padding-top: .25rem;
}
.sof-btn-save {
    flex: 1 1 calc(50% - .625rem);
    min-width: 140px;
    display: flex; align-items: center; justify-content: center;
    gap: .5rem;
    padding: .875rem 1rem;
    background: #064E3B;
    color: #fff;
    border: 2px solid #064E3B;
    border-radius: 1rem;
    font-family: 'Be Vietnam Pro', 'Inter', sans-serif;
    font-size: .8125rem;
    font-weight: 900;
    text-transform: uppercase;
    letter-spacing: .08em;
    cursor: pointer;
    box-shadow: 0 2px 8px rgba(6,78,59,.2);
    transition: all .2s ease;
}
.sof-btn-save:hover {
    background: #A3E635;
    border-color: #A3E635;
    color: #064E3B;
    box-shadow: 0 4px 14px rgba(6,78,59,.28);
    transform: translateY(-1px);
}
.sof-btn-save:active { transform: scale(.97); }

.sof-btn-cancel {
    flex: 1 1 calc(50% - .625rem);
    min-width: 140px;
    display: flex; align-items: center; justify-content: center;
    gap: .5rem;
    padding: .875rem 1rem;
    background: #fff;
    color: #064E3B;
    border: 2px solid #064E3B;
    border-radius: 1rem;
    font-family: 'Be Vietnam Pro', 'Inter', sans-serif;
    font-size: .8125rem;
    font-weight: 900;
    text-transform: uppercase;
    letter-spacing: .08em;
    cursor: pointer;
    transition: all .2s ease;
}
.sof-btn-cancel:hover {
    background: #064E3B;
    color: #fff;
    transform: translateY(-1px);
    box-shadow: 0 4px 14px rgba(6,78,59,.28);
}
.sof-btn-cancel:active { transform: scale(.97); }
</style>

<script src="https://cdn.jsdelivr.net/npm/sweetalert2@11"></script>
<script>
    function staffSetPwError(input, errId, msg) {
        var err = document.getElementById(errId);
        if (input) { input.classList.add('input-error'); input.classList.remove('input-valid'); }
        if (err)   { err.textContent = msg; err.classList.add('active'); }
    }
    function staffClearPwError(input, errId) {
        var err = document.getElementById(errId);
        if (input) { input.classList.remove('input-error'); }
        if (err)   { err.textContent = ''; err.classList.remove('active'); }
    }
    function staffSetValid(input) {
        if (input) { input.classList.add('input-valid'); input.classList.remove('input-error'); }
    }
    function staffTogglePw(btn) {
        var input = btn.previousElementSibling;
        if (!input) return;
        var isText = input.type === 'text';
        input.type = isText ? 'password' : 'text';
        btn.innerHTML = isText
            ? '<i class="bi bi-eye-slash"></i>'
            : '<i class="bi bi-eye"></i>';
    }
    function staffValidatePwLength(input) {
        var errId = input.id === 'staffOldPassword' ? 'staffOldPwError' : 'staffNewPwError';
        var val   = input.value;
        staffClearPwError(input, errId);
        if (!val) return true;
        if (val.length < 6)  { staffSetPwError(input, errId, 'Mật khẩu phải có ít nhất 6 ký tự'); return false; }
        if (val.length > 20) { staffSetPwError(input, errId, 'Mật khẩu tối đa 20 ký tự'); return false; }
        staffSetValid(input);
        return true;
    }
    function staffCheckPasswordStrength(val) {
        var strengthEl   = document.getElementById('staffPasswordStrength');
        var suggestionEl = document.getElementById('staffPasswordSuggestion');
        if (!strengthEl) return;
        if (!val || val.length < 6 || val.length > 20) {
            strengthEl.textContent = ''; if (suggestionEl) suggestionEl.textContent = ''; return;
        }
        var strength = 0, suggestions = [];
        if (val.length >= 8) strength += 2; else strength += 1;
        if (/[A-Z]/.test(val))        strength += 1; else suggestions.push('chữ hoa');
        if (/[0-9]/.test(val))        strength += 1; else suggestions.push('chữ số');
        if (/[^A-Za-z0-9]/.test(val)) strength += 1; else suggestions.push('ký tự đặc biệt');
        if (strength >= 4) {
            strengthEl.textContent = 'Mạnh'; strengthEl.style.color = '#059669';
            if (suggestionEl) suggestionEl.textContent = '';
        } else if (strength >= 2) {
            strengthEl.textContent = 'Trung bình'; strengthEl.style.color = '#D97706';
            if (suggestionEl) suggestionEl.textContent = suggestions.length ? 'Gợi ý: thêm ' + suggestions.join(', ') : '';
        } else {
            strengthEl.textContent = 'Yếu'; strengthEl.style.color = '#EF4444';
            if (suggestionEl) suggestionEl.textContent = suggestions.length ? 'Gợi ý: thêm ' + suggestions.join(', ') : '';
        }
    }
    function staffOnNewPwInput(input) {
        staffValidatePwLength(input);
        staffCheckPasswordStrength(input.value);
        staffCheckConfirmInline();
    }
    function staffCheckConfirmInline() {
        var newPw     = document.getElementById('staffNewPassword');
        var confirmPw = document.getElementById('staffConfirmPassword');
        if (!confirmPw || !confirmPw.value) { staffClearPwError(confirmPw, 'staffConfirmPwError'); return true; }
        if (newPw && newPw.value !== confirmPw.value) {
            staffSetPwError(confirmPw, 'staffConfirmPwError', 'Mật khẩu xác nhận không khớp'); return false;
        }
        staffClearPwError(confirmPw, 'staffConfirmPwError');
        staffSetValid(confirmPw);
        return true;
    }
    async function staffSubmitPwForm() {
        var oldPw     = document.getElementById('staffOldPassword');
        var newPw     = document.getElementById('staffNewPassword');
        var confirmPw = document.getElementById('staffConfirmPassword');
        var ok1 = staffValidatePwLength(oldPw);
        var ok2 = staffValidatePwLength(newPw);
        var ok3 = staffCheckConfirmInline();
        if (!oldPw.value)     { staffSetPwError(oldPw,     'staffOldPwError',     'Vui lòng nhập mật khẩu hiện tại'); ok1 = false; }
        if (!newPw.value)     { staffSetPwError(newPw,     'staffNewPwError',     'Vui lòng nhập mật khẩu mới');      ok2 = false; }
        if (!confirmPw.value) { staffSetPwError(confirmPw, 'staffConfirmPwError', 'Vui lòng xác nhận mật khẩu');      ok3 = false; }
        if (!ok1 || !ok2 || !ok3) return;
        const result = await Swal.fire({
            title: 'Xác nhận đổi mật khẩu',
            text: 'Bạn có chắc muốn lưu mật khẩu mới không?',
            icon: 'question',
            showCancelButton: true,
            confirmButtonColor: '#064E3B',
            cancelButtonColor: '#6b7280',
            confirmButtonText: 'Lưu thay đổi',
            cancelButtonText: 'Hủy',
            reverseButtons: true
        });
        if (result.isConfirmed) document.getElementById('staffChangePwForm').submit();
    }
</script>
