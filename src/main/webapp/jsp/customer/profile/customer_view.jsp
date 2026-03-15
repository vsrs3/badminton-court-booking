<!-- customer_view.jsp -->

<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<link rel="stylesheet"
	href="${pageContext.request.contextPath}/assets/css/customer/customer-view.css">

<div class="flex flex-col min-h-screen bg-white relative">

    <!-- HEADER -->
    <div class="profile-header">

        <div class="profile-title-group">
            <i data-lucide="user" class="profile-title-icon"></i>
            <h1 class="profile-page-title">Thông tin cá nhân</h1>
        </div>
        
         <a href="${pageContext.request.contextPath}/home" class="btn-back-home">
            <i data-lucide="arrow-left" class="w-4 h-4"></i>
            <span>Quay Lại Trang Chủ</span>
        </a>
        
    </div>

	<form action="customerController" method="POST"
		enctype="multipart/form-data" class="p-6 space-y-6" id="profileForm"
        onsubmit="
            var nameOk   = validateFullNameInline();
            var emailOk  = validateEmailInline();
            var phoneOk  = validatePhoneInline();
            if (!nameOk || !emailOk || !phoneOk) {
                event.preventDefault();
                return false;
            }
            var ok = confirm('Bạn có chắc muốn lưu thông tin không?');
            if (!ok) { event.preventDefault(); return false; }
        ">
		<input type="hidden" name="action" value="updateProfile" />

		<!-- ===== AVATAR ===== -->
		<div class="space-y-3">
			<label class="field-label">
                <i data-lucide="user-circle" class="w-4 h-4"></i>
                <span>Ảnh đại diện</span>
            </label>
			<div class="avatar-section-wrapper">
				<div class="avatar-preview" id="profileAvatarContainer">
					<c:choose>
						<c:when test="${not empty sessionScope.account.avatarPath}">
							<img src="${pageContext.request.contextPath}/uploads/${sessionScope.account.avatarPath}"
								alt="Avatar" id="profileAvatarPreview" class="w-full h-full object-cover">
						</c:when>
						<c:otherwise>
							<i data-lucide="user-circle" id="profileAvatarIcon" class="w-16 h-16 text-gray-400"></i>
						</c:otherwise>
					</c:choose>
				</div>
				<p class="upload-hint upload-hint--center">
					<i data-lucide="info" class="w-3 h-3"></i>
                    <span>JPG, PNG tối đa 2MB</span>
				</p>
				<input type="file" id="avatar" name="avatar" accept="image/*"
					class="hidden" onchange="previewProfileAvatar(this)">
                <label for="avatar" class="btn-upload">
                    <i data-lucide="camera" class="w-4 h-4"></i>
                    <span>Chọn ảnh đại diện</span>
                </label>
			</div>
		</div>

		<!-- ===== HỌ VÀ TÊN ===== -->
		<div class="space-y-2">
			<label for="full_name" class="field-label">
                <i data-lucide="user" class="w-4 h-4"></i>
                <span>Họ và tên</span>
                <span class="text-red-500 font-bold">*</span>
            </label>
            <input type="text" id="full_name" name="full_name"
				value="${sessionScope.account.fullName}" required maxlength="50"
				class="w-full p-4 rounded-xl border border-gray-200 focus:border-emerald-500 focus:ring-emerald-500 focus:outline-none transition-colors"
				placeholder="Ví dụ: Nguyễn Văn A"
                oninput="
                    if (this.value.length > 50) this.value = this.value.substring(0, 50);
                    validateFullNameInline();
                "
                onblur="validateFullNameInline()">
			<div id="nameWarning" class="warning-message"></div>
		</div>

		<!-- ===== EMAIL ===== -->
		<div class="space-y-2">
			<label for="email" class="field-label">
                <i data-lucide="mail" class="w-4 h-4"></i>
                <span>Email</span>
                <span class="text-red-500 font-bold">*</span>
            </label>
            <input type="email" id="email" name="email"
				value="${sessionScope.account.email}" required
				class="w-full p-4 rounded-xl border border-gray-200 focus:border-emerald-500 focus:ring-emerald-500 focus:outline-none transition-colors"
				placeholder="Ví dụ: example123@gmail.com"
                oninput="validateEmailInline()"
                onblur="validateEmailInline()"
				<c:if test="${not empty sessionScope.account.googleId}">readonly</c:if>>
			<div id="emailWarning" class="warning-message"></div>
			<c:if test="${not empty sessionScope.account.googleId}">
				<p class="google-tag">
					<i data-lucide="google" class="w-3 h-3"></i>
                    <span>Đăng nhập bằng Google</span>
				</p>
			</c:if>
		</div>

		<!-- ===== SỐ ĐIỆN THOẠI ===== -->
		<div class="space-y-2">
			<label for="phone" class="field-label">
                <i data-lucide="phone" class="w-4 h-4"></i>
                <span>Số điện thoại</span>
				<span class="text-red-500 font-bold">*</span>
			</label>
			<div class="phone-input-wrapper">
				<input type="tel" id="phone" name="phone"
					value="${sessionScope.account.phone}" required
					class="w-full p-4 rounded-xl border border-gray-200 focus:border-emerald-500 focus:ring-emerald-500 focus:outline-none transition-colors"
					placeholder="Ví dụ: 0901234567, 0912345678"
                    oninput="validatePhoneInline()"
                    onblur="validatePhoneInline()">
				<div id="phoneFlag" class="phone-flag hidden"></div>
			</div>
			<div id="phoneWarning" class="warning-message">
				<i data-lucide="alert-circle" class="w-3 h-3"></i>
                <span></span>
			</div>
		</div>

		<!-- ===== BUTTONS ===== -->
		<div class="form-actions">
			<button type="submit" class="btn-save">
				<i data-lucide="save" class="w-5 h-5"></i>
                <span>Lưu thay đổi</span>
			</button>
			<button type="reset" class="btn-cancel">
				<i data-lucide="refresh-cw" class="w-4 h-4"></i>
                <span>Khôi phục</span>
			</button>
		</div>
	</form>

	<!-- ===== THÔNG BÁO SERVER ===== -->
	<c:if test="${not empty sessionScope.updateSuccess}">
		<div class="notification success mx-6 mt-4">
			<i data-lucide="check-circle" class="w-5 h-5 flex-shrink-0"></i>
            <span>${sessionScope.updateSuccess}</span>
		</div>
		<c:remove var="updateSuccess" scope="session" />
	</c:if>
	<c:if test="${not empty sessionScope.updateError}">
		<div class="notification error mx-6 mt-4">
			<i data-lucide="alert-circle" class="w-5 h-5 flex-shrink-0"></i>
            <span>${sessionScope.updateError}</span>
		</div>
		<c:remove var="updateError" scope="session" />
	</c:if>
</div>

<%-- ================================================================
     Validation — dùng hàm đặt tên riêng (prefix "Profile") để tránh
     conflict, gắn qua inline oninput/onblur/onsubmit thay vì
     DOMContentLoaded (không chạy khi inject qua innerHTML trong SPA)
     ================================================================ --%>
<script>
    var _phonePatterns = {
        VN: { regex: /^(?:\+84|84|0)(?:3[2-9]|5[2689]|7[0|6-9]|8[1-9]|9[0-9])[0-9]{7}$/, flag: 'https://flagcdn.com/w40/vn.png', name: 'Việt Nam' },
        US: { regex: /^(?:\+1|1)?[2-9][0-9]{2}[2-9][0-9]{6}$/,                             flag: 'https://flagcdn.com/w40/us.png', name: 'Mỹ' },
        UK: { regex: /^(?:\+44|44|0)([1-9][0-9]{9,10})$/,                                  flag: 'https://flagcdn.com/w40/gb.png', name: 'Anh' },
    };

    function _showWarn(elId, msg) {
        var el  = document.getElementById(elId);
        var inp = el ? el.previousElementSibling : null;
        if (!el) return;
        el.textContent = msg;
        el.classList.add('active');
        if (inp && inp.tagName === 'INPUT') { inp.classList.add('input-error'); inp.classList.remove('input-valid'); }
    }
    function _clearWarn(elId) {
        var el  = document.getElementById(elId);
        var inp = el ? el.previousElementSibling : null;
        if (!el) return;
        el.textContent = '';
        el.classList.remove('active');
        if (inp && inp.tagName === 'INPUT') inp.classList.remove('input-error');
    }
    function _setValid(inp) {
        if (inp) { inp.classList.add('input-valid'); inp.classList.remove('input-error'); }
    }

    function validateFullNameInline() {
        var inp = document.getElementById('full_name');
        var val = inp ? inp.value.trim() : '';
        _clearWarn('nameWarning');
        if (!val)                              { _showWarn('nameWarning', 'Vui lòng nhập họ và tên'); return false; }
        if (val.length < 2 || val.length > 50) { _showWarn('nameWarning', 'Họ và tên phải từ 2 đến 50 ký tự'); return false; }
        _setValid(inp);
        return true;
    }

    function validateEmailInline() {
        var inp = document.getElementById('email');
        var val = inp ? inp.value.trim() : '';
        _clearWarn('emailWarning');
        if (!val)                                                               { _showWarn('emailWarning', 'Vui lòng nhập email'); return false; }
        if (!/^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/.test(val))   { _showWarn('emailWarning', 'Email không đúng định dạng'); return false; }
        _setValid(inp);
        return true;
    }

    function validatePhoneInline() {
        var inp      = document.getElementById('phone');
        var warnEl   = document.getElementById('phoneWarning');
        var flagEl   = document.getElementById('phoneFlag');
        var val      = inp ? inp.value.trim().replace(/\s+/g, '') : '';

        // Reset
        if (warnEl) { warnEl.querySelector('span').textContent = ''; warnEl.classList.remove('active'); }
        if (flagEl) { flagEl.classList.add('hidden'); flagEl.innerHTML = ''; }
        if (inp)    { inp.classList.remove('input-error', 'input-valid'); }

        if (!val) {
            if (warnEl) { warnEl.querySelector('span').textContent = 'Vui lòng nhập số điện thoại'; warnEl.classList.add('active'); }
            if (inp) inp.classList.add('input-error');
            return false;
        }
        for (var key in _phonePatterns) {
            if (_phonePatterns[key].regex.test(val)) {
                if (flagEl) {
                    var img = document.createElement('img');
                    img.src = _phonePatterns[key].flag;
                    img.alt = _phonePatterns[key].name;
                    img.style.cssText = 'width:100%;height:100%';
                    flagEl.appendChild(img);
                    flagEl.classList.remove('hidden');
                }
                _setValid(inp);
                return true;
            }
        }
        if (warnEl) { warnEl.querySelector('span').textContent = 'Số điện thoại không hợp lệ (VN, US, UK, JP)'; warnEl.classList.add('active'); }
        if (inp) inp.classList.add('input-error');
        return false;
    }

    // Preview avatar — scoped để tránh conflict với sidebar
    window.previewProfileAvatar = function(input) {
        if (!input.files || !input.files[0]) return;
        var reader = new FileReader();
        reader.onload = function(e) {
            var container = document.getElementById('profileAvatarContainer');
            var preview   = document.getElementById('profileAvatarPreview');
            var icon      = document.getElementById('profileAvatarIcon');
            if (preview) {
                preview.src = e.target.result;
            } else if (icon && container) {
                var img = document.createElement('img');
                img.src = e.target.result;
                img.id  = 'profileAvatarPreview';
                img.className = 'w-full h-full object-cover';
                container.replaceChild(img, icon);
                if (window.lucide) lucide.createIcons();
            }
        };
        reader.readAsDataURL(input.files[0]);
    };

    // Chạy validation ngay lập tức để hiển thị trạng thái ban đầu
    validateFullNameInline();
    validateEmailInline();
    validatePhoneInline();
</script>
 