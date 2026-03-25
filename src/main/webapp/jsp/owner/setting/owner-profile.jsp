<%-- owner-profile.jsp --%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ include file="../layout/layout.jsp"%>
<%@ include file="../layout/sidebar.jsp"%>

<link rel="stylesheet"
    href="${pageContext.request.contextPath}/assets/css/owner/dashboard.css">
<style>
.content-area { padding: 0 !important; }
</style>

<div class="main-content">
    <%@ include file="../layout/header.jsp"%>

    <div class="dov-wrap">

        <%-- ============================================================
             PROFILE CARD
        ============================================================ --%>
        <div class="dov-card dov-fadein dov-d1">

            <%-- Card header --%>
            <div class="dov-card-header">
                <div style="display:flex; align-items:center; gap:8px;">
                    <div class="dov-stat-icon dov-stat-icon--green"
                         style="width:38px; height:38px; border-radius:10px; font-size:1rem;">
                        <i class="bi bi-person-fill"></i>
                    </div>
                    <h3 class="dov-card-title" style="font-size:1.125rem; font-weight:900;
                        text-transform:uppercase; letter-spacing:.04em; color:#064E3B;">
                        Thông Tin Cá Nhân
                    </h3>
                </div>
                <a href="${pageContext.request.contextPath}/owner/setting/"
                   class="sof-btn-back">
                    <i class="bi bi-arrow-left"></i>
                    <span>Quay lại</span>
                </a>
            </div>

            <%-- Form --%>
            <form action="${pageContext.request.contextPath}/owner/setting/profile"
                  method="POST"
                  enctype="multipart/form-data"
                  class="sof-form"
                  id="ownerProfileForm">
                <input type="hidden" name="action" value="updateProfile"/>

                <%-- ===== AVATAR ===== --%>
                <div class="sof-field">
                    <label class="sof-label">
                        <i class="bi bi-person-circle"></i>
                        <span>Ảnh đại diện</span>
                    </label>
                    <div class="sof-avatar-wrapper">
                        <div class="sof-avatar-preview" id="ownerAvatarContainer">
                            <c:choose>
                                <c:when test="${not empty sessionScope.account.avatarPath}">
                                    <img src="${pageContext.request.contextPath}/uploads/${sessionScope.account.avatarPath}"
                                         alt="Avatar" id="ownerAvatarPreview"
                                         style="width:100%;height:100%;object-fit:cover;">
                                </c:when>
                                <c:otherwise>
                                    <i class="bi bi-person-circle" id="ownerAvatarIcon"
                                       style="font-size:3.5rem;color:#9CA3AF;"></i>
                                </c:otherwise>
                            </c:choose>
                        </div>
                        <div style="display:flex;flex-direction:column;gap:6px;align-items:flex-start;">
                            <p class="sof-upload-hint">
                                <i class="bi bi-info-circle" style="font-size:.75rem;"></i>
                                <span>JPG, PNG tối đa 2MB</span>
                            </p>
                            <input type="file" id="ownerAvatar" name="avatar" accept="image/*"
                                   style="display:none;" onchange="ownerPreviewAvatar(this)">
                            <label for="ownerAvatar" class="sof-btn-upload">
                                <i class="bi bi-camera-fill"></i>
                                <span>Chọn ảnh đại diện</span>
                            </label>
                        </div>
                    </div>
                </div>

                <%-- ===== HỌ VÀ TÊN ===== --%>
                <div class="sof-field">
                    <label for="ownerFullName" class="sof-label">
                        <i class="bi bi-person"></i>
                        <span>Họ và tên</span>
                        <span style="color:#EF4444;">*</span>
                    </label>
                    <input type="text" id="ownerFullName" name="full_name"
                           value="${sessionScope.account.fullName}"
                           required maxlength="50"
                           class="sof-input"
                           placeholder="Ví dụ: Nguyễn Văn A"
                           oninput="ownerValidateFullName()"
                           onblur="ownerValidateFullName()">
                    <p class="sof-field-error" id="ownerNameWarning"></p>
                </div>

                <%-- ===== EMAIL ===== --%>
                <div class="sof-field">
                    <label for="ownerEmail" class="sof-label">
                        <i class="bi bi-envelope"></i>
                        <span>Email</span>
                        <span style="color:#EF4444;">*</span>
                    </label>
                    <input type="email" id="ownerEmail" name="email"
                           value="${sessionScope.account.email}"
                           required
                           class="sof-input"
                           placeholder="Ví dụ: example123@gmail.com"
                           oninput="ownerValidateEmail()"
                           onblur="ownerValidateEmail()"
                           <c:if test="${not empty sessionScope.account.googleId}">readonly</c:if>>
                    <p class="sof-field-error" id="ownerEmailWarning"></p>
                    <c:if test="${not empty sessionScope.account.googleId}">
                        <p style="font-size:.6875rem;font-weight:700;text-transform:uppercase;
                                  letter-spacing:.08em;color:#6B7280;margin:.25rem 0 0;
                                  display:flex;align-items:center;gap:.375rem;">
                            <i class="bi bi-patch-check-fill" style="color:#3B82F6;"></i>
                            <span>Đăng nhập bằng Google</span>
                        </p>
                    </c:if>
                </div>

                <%-- ===== SỐ ĐIỆN THOẠI ===== --%>
                <div class="sof-field">
                    <label for="ownerPhone" class="sof-label">
                        <i class="bi bi-telephone"></i>
                        <span>Số điện thoại</span>
                        <span style="color:#EF4444;">*</span>
                    </label>
                    <div class="sof-input-wrap">
                        <input type="tel" id="ownerPhone" name="phone"
                               value="${sessionScope.account.phone}"
                               required
                               class="sof-input"
                               placeholder="Ví dụ: 0901234567"
                               oninput="ownerValidatePhone()"
                               onblur="ownerValidatePhone()">
                        <div id="ownerPhoneFlag" class="sof-phone-flag" style="display:none;"></div>
                    </div>
                    <p class="sof-field-error" id="ownerPhoneWarning"></p>
                </div>

                <%-- ===== BUTTONS ===== --%>
                <div class="sof-actions">
                    <button type="submit" class="sof-btn-save" id="ownerBtnSave">
                        <i class="bi bi-floppy-fill"></i>
                        <span>Lưu thay đổi</span>
                    </button>
                    <button type="reset" class="sof-btn-cancel">
                        <i class="bi bi-arrow-counterclockwise"></i>
                        <span>Khôi phục</span>
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

<%-- ── Shared form styles (same as change-password) ── --%>
<style>
.sof-btn-back {
    display: inline-flex; align-items: center; gap: .5rem;
    padding: .5rem 1rem .5rem .625rem;
    background: #F0FDF4; color: #064E3B;
    border: 1.5px solid #A3E635; border-radius: 9999px;
    font-size: .8125rem; font-weight: 700;
    text-decoration: none; transition: all .2s ease; white-space: nowrap;
}
.sof-btn-back:hover {
    background: #064E3B; border-color: #064E3B; color: #fff;
    transform: translateY(-1px); box-shadow: 0 4px 10px rgba(6,78,59,.2);
}
.sof-btn-back i { transition: transform .2s ease; }
.sof-btn-back:hover i { transform: translateX(-3px); color: #fff; }

.sof-form { display: flex; flex-direction: column; gap: 1.5rem; }
.sof-field { display: flex; flex-direction: column; gap: .25rem; }

.sof-label {
    display: flex; align-items: center; gap: .375rem;
    font-size: .6875rem; font-weight: 900;
    text-transform: uppercase; letter-spacing: .1em;
    color: #064E3B; margin-bottom: .25rem;
}
.sof-label i { color: #064E3B; flex-shrink: 0; }

.sof-input-wrap { position: relative; }
.sof-input {
    width: 100%; padding: .875rem 1rem; border-radius: .75rem;
    border: 1px solid #E5E7EB;
    font-family: 'Be Vietnam Pro', 'Inter', sans-serif;
    font-size: .9375rem; font-weight: 500; color: #1F2937;
    background: #fff; outline: none;
    transition: border-color .15s, box-shadow .15s; box-sizing: border-box;
}
.sof-input:focus { border-color: #064E3B; box-shadow: 0 0 0 3px rgba(163,230,53,.2); }
.sof-input.input-error { border-color: #EF4444 !important; background: #fef2f2; }
.sof-input.input-valid { border-color: #10B981; }
.sof-input[readonly]   { background: #F9FAFB; cursor: not-allowed; color: #6B7280; }

.sof-toggle-pw {
    position: absolute; right: 1rem; top: 50%; transform: translateY(-50%);
    background: none; border: none; cursor: pointer;
    color: #9CA3AF; padding: .25rem; line-height: 1; font-size: 1rem;
}
.sof-toggle-pw:hover { color: #374151; }

.sof-phone-flag {
    position: absolute; right: 1rem; top: 50%; transform: translateY(-50%);
    width: 26px; height: 18px; border-radius: 3px; overflow: hidden;
    box-shadow: 0 1px 4px rgba(0,0,0,.12); pointer-events: none;
}

.sof-field-error {
    display: none; align-items: center; gap: .25rem;
    font-size: .6875rem; font-weight: 700;
    text-transform: uppercase; letter-spacing: .05em;
    color: #D97706; margin: 0;
}
.sof-field-error.active { display: flex; }

/* Avatar */
.sof-avatar-wrapper {
    display: flex; align-items: center; gap: 1.5rem; flex-wrap: wrap;
    padding: .5rem 0;
}
.sof-avatar-preview {
    width: 110px; height: 110px; border-radius: 50%; overflow: hidden;
    border: 3px solid #A7F3D0; background: #F9FAFB;
    display: flex; align-items: center; justify-content: center;
    box-shadow: 0 4px 12px rgba(6,78,59,.12);
    transition: border-color .2s, box-shadow .2s; flex-shrink: 0;
}
.sof-avatar-preview:hover { border-color: #A3E635; box-shadow: 0 6px 18px rgba(6,78,59,.18); }
.sof-upload-hint {
    font-size: .6875rem; font-weight: 700; text-transform: uppercase;
    letter-spacing: .08em; color: #9CA3AF;
    display: flex; align-items: center; gap: .25rem; margin: 0;
}
.sof-btn-upload {
    display: inline-flex; align-items: center; gap: .5rem;
    padding: .625rem 1.125rem; background: #064E3B; color: #fff;
    border-radius: .75rem; font-size: .8125rem; font-weight: 900;
    text-transform: uppercase; letter-spacing: .08em; cursor: pointer;
    border: none; box-shadow: 0 2px 8px rgba(6,78,59,.2);
    transition: all .2s ease;
}
.sof-btn-upload:hover {
    background: #A3E635; color: #064E3B;
    box-shadow: 0 4px 14px rgba(6,78,59,.28); transform: translateY(-1px);
}

/* Action buttons */
.sof-actions { display: flex; gap: 1.25rem; flex-wrap: wrap; padding-top: .25rem; }
.sof-btn-save {
    flex: 1 1 calc(50% - .625rem); min-width: 140px;
    display: flex; align-items: center; justify-content: center; gap: .5rem;
    padding: .875rem 1rem; background: #064E3B; color: #fff;
    border: 2px solid #064E3B; border-radius: 1rem;
    font-family: 'Be Vietnam Pro', 'Inter', sans-serif;
    font-size: .8125rem; font-weight: 900;
    text-transform: uppercase; letter-spacing: .08em; cursor: pointer;
    box-shadow: 0 2px 8px rgba(6,78,59,.2); transition: all .2s ease;
}
.sof-btn-save:hover {
    background: #A3E635; border-color: #A3E635; color: #064E3B;
    box-shadow: 0 4px 14px rgba(6,78,59,.28); transform: translateY(-1px);
}
.sof-btn-save:active { transform: scale(.97); }
.sof-btn-cancel {
    flex: 1 1 calc(50% - .625rem); min-width: 140px;
    display: flex; align-items: center; justify-content: center; gap: .5rem;
    padding: .875rem 1rem; background: #fff; color: #064E3B;
    border: 2px solid #064E3B; border-radius: 1rem;
    font-family: 'Be Vietnam Pro', 'Inter', sans-serif;
    font-size: .8125rem; font-weight: 900;
    text-transform: uppercase; letter-spacing: .08em; cursor: pointer;
    transition: all .2s ease;
}
.sof-btn-cancel:hover {
    background: #064E3B; color: #fff;
    transform: translateY(-1px); box-shadow: 0 4px 14px rgba(6,78,59,.28);
}
.sof-btn-cancel:active { transform: scale(.97); }
</style>

<script src="https://cdn.jsdelivr.net/npm/sweetalert2@11"></script>
<script>
    var _ownerPhonePatterns = {
        VN: { regex: /^(?:\+84|84|0)(?:3[2-9]|5[2689]|7[0|6-9]|8[1-9]|9[0-9])[0-9]{7}$/, flag: 'https://flagcdn.com/w40/vn.png' },
        US: { regex: /^(?:\+1|1)?[2-9][0-9]{2}[2-9][0-9]{6}$/,                             flag: 'https://flagcdn.com/w40/us.png' },
        UK: { regex: /^(?:\+44|44|0)([1-9][0-9]{9,10})$/,                                  flag: 'https://flagcdn.com/w40/gb.png' },
    };

    function ownerShowWarn(elId, msg) {
        var el = document.getElementById(elId);
        if (!el) return;
        el.textContent = msg; el.classList.add('active');
        var inp = document.getElementById(elId.replace('Warning','').replace('Warn',''));
        if (inp && inp.tagName === 'INPUT') { inp.classList.add('input-error'); inp.classList.remove('input-valid'); }
    }
    function ownerClearWarn(elId) {
        var el = document.getElementById(elId);
        if (!el) return;
        el.textContent = ''; el.classList.remove('active');
    }
    function ownerSetValid(inp) {
        if (inp) { inp.classList.add('input-valid'); inp.classList.remove('input-error'); }
    }

    function ownerValidateFullName() {
        var inp = document.getElementById('ownerFullName');
        var val = inp ? inp.value.trim() : '';
        ownerClearWarn('ownerNameWarning');
        if (!val)                               { ownerShowWarn('ownerNameWarning', 'Vui lòng nhập họ và tên'); inp.classList.add('input-error'); return false; }
        if (val.length < 2 || val.length > 50)  { ownerShowWarn('ownerNameWarning', 'Họ và tên phải từ 2 đến 50 ký tự'); inp.classList.add('input-error'); return false; }
        ownerSetValid(inp); return true;
    }
    function ownerValidateEmail() {
        var inp = document.getElementById('ownerEmail');
        var val = inp ? inp.value.trim() : '';
        ownerClearWarn('ownerEmailWarning');
        if (!val)                                                              { ownerShowWarn('ownerEmailWarning', 'Vui lòng nhập email'); inp.classList.add('input-error'); return false; }
        if (!/^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/.test(val)) { ownerShowWarn('ownerEmailWarning', 'Email không đúng định dạng'); inp.classList.add('input-error'); return false; }
        ownerSetValid(inp); return true;
    }
    function ownerValidatePhone() {
        var inp    = document.getElementById('ownerPhone');
        var flagEl = document.getElementById('ownerPhoneFlag');
        var val    = inp ? inp.value.trim().replace(/\s+/g, '') : '';
        ownerClearWarn('ownerPhoneWarning');
        if (flagEl) { flagEl.style.display = 'none'; flagEl.innerHTML = ''; }
        if (inp)    { inp.classList.remove('input-error', 'input-valid'); }
        if (!val)   { ownerShowWarn('ownerPhoneWarning', 'Vui lòng nhập số điện thoại'); inp.classList.add('input-error'); return false; }
        for (var key in _ownerPhonePatterns) {
            if (_ownerPhonePatterns[key].regex.test(val)) {
                if (flagEl) {
                    var img = document.createElement('img');
                    img.src = _ownerPhonePatterns[key].flag;
                    img.style.cssText = 'width:100%;height:100%;';
                    flagEl.appendChild(img); flagEl.style.display = 'block';
                }
                ownerSetValid(inp); return true;
            }
        }
        ownerShowWarn('ownerPhoneWarning', 'Số điện thoại không hợp lệ (VN, US, UK)');
        inp.classList.add('input-error'); return false;
    }
    function ownerPreviewAvatar(input) {
        if (!input.files || !input.files[0]) return;
        var reader = new FileReader();
        reader.onload = function(e) {
            var container = document.getElementById('ownerAvatarContainer');
            var preview   = document.getElementById('ownerAvatarPreview');
            var icon      = document.getElementById('ownerAvatarIcon');
            if (preview) {
                preview.src = e.target.result;
            } else if (icon && container) {
                var img = document.createElement('img');
                img.src = e.target.result; img.id = 'ownerAvatarPreview';
                img.style.cssText = 'width:100%;height:100%;object-fit:cover;';
                container.replaceChild(img, icon);
            }
        };
        reader.readAsDataURL(input.files[0]);
    }
    (function () {
        var form = document.getElementById('ownerProfileForm');
        if (!form) return;
        var _confirmed = false;
        form.addEventListener('submit', async function (e) {
            if (_confirmed) return;
            e.preventDefault();
            var nameOk  = ownerValidateFullName();
            var emailOk = ownerValidateEmail();
            var phoneOk = ownerValidatePhone();
            if (!nameOk || !emailOk || !phoneOk) return;
            const result = await Swal.fire({
                title: 'Xác nhận lưu', text: 'Bạn có chắc muốn lưu thông tin không?',
                icon: 'question', showCancelButton: true,
                confirmButtonColor: '#064E3B', cancelButtonColor: '#6b7280',
                confirmButtonText: 'Lưu thay đổi', cancelButtonText: 'Hủy', reverseButtons: true
            });
            if (result.isConfirmed) { _confirmed = true; form.submit(); }
        });
    })();
    ownerValidateFullName(); ownerValidateEmail(); ownerValidatePhone();
</script>
