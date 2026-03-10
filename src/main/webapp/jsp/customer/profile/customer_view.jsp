<%-- <!-- customer_view.jsp -->
 
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<link rel="stylesheet"
	href="${pageContext.request.contextPath}/assets/css/customer/customer-view.css">

<div class="flex flex-col min-h-screen bg-white relative">

	<!-- ===== HEADER: Quay lại (trái) | Tiêu đề (phải) ===== -->
	<div class="p-6 border-b border-gray-100 flex items-center justify-between">

		<!-- Nút Quay Lại — BÊN TRÁI -->
		<a href="${pageContext.request.contextPath}/home"
			class="btn-back-home">
			<i data-lucide="arrow-left" class="w-4 h-4"></i>
			<span>Quay Lại Trang Chủ</span>
		</a>

		<!-- Tiêu đề — BÊN PHẢI -->
		<div class="flex items-center space-x-2">
			<h1 class="text-xl font-bold text-gray-800">Thông tin cá nhân</h1>
			<i data-lucide="user" class="w-6 h-6 text-emerald-700"></i>
		</div>
	</div>

	<form action="customerController" method="POST"
		enctype="multipart/form-data" class="p-6 space-y-6" id="profileForm">
		<input type="hidden" name="action" value="updateProfile" />

		<!-- ===== AVATAR: ảnh trên, nút + hint bên dưới ===== -->
		<div class="space-y-3">
			<label class="field-label">
				<i data-lucide="user-circle" class="w-4 h-4"></i>
				<span>Ảnh đại diện</span>
			</label>

			<div class="flex flex-col items-start gap-3">

				<!-- Ảnh tròn — id riêng tránh conflict với sidebar -->
				<div class="avatar-preview" id="profileAvatarContainer">
					<c:choose>
						<c:when test="${not empty sessionScope.account.avatarPath}">
							<img
								src="${pageContext.request.contextPath}/uploads/${sessionScope.account.avatarPath}"
								alt="Avatar" id="profileAvatarPreview"
								class="w-full h-full object-cover">
						</c:when>
						<c:otherwise>
							<i data-lucide="user-circle" id="profileAvatarIcon"
								class="w-16 h-16 text-gray-400"></i>
						</c:otherwise>
					</c:choose>
				</div>

				<!-- Nút chọn ảnh — nằm DƯỚI avatar -->
				<input type="file" id="avatar" name="avatar" accept="image/*"
					class="hidden" onchange="previewProfileAvatar(this)">
				<label for="avatar" class="btn-upload">
					<i data-lucide="camera" class="w-4 h-4"></i>
					<span>Chọn ảnh đại diện</span>
				</label>

				<!-- Hint — nằm DƯỚI nút -->
				<p class="upload-hint">
					<i data-lucide="info" class="w-3 h-3"></i>
					<span>JPG, PNG tối đa 2MB</span>
				</p>
			</div>
		</div>

		<!-- ===== HỌ VÀ TÊN ===== -->
		<div class="space-y-2">
			<label for="full_name" class="field-label">
				<i data-lucide="user" class="w-4 h-4"></i>
				<span>Họ và tên</span>
			</label>
			<input type="text" id="full_name" name="full_name"
				value="${sessionScope.account.fullName}" required maxlength="50"
				class="w-full p-4 rounded-xl border border-gray-200 focus:border-emerald-500 focus:ring-emerald-500 focus:outline-none transition-colors"
				placeholder="Ví dụ: Nguyễn Văn A">
			<div id="nameWarning" class="warning-message"></div>
		</div>

		<!-- ===== EMAIL ===== -->
		<div class="space-y-2">
			<label for="email" class="field-label">
				<i data-lucide="mail" class="w-4 h-4"></i>
				<span>Email</span>
			</label>
			<input type="email" id="email" name="email"
				value="${sessionScope.account.email}" required
				class="w-full p-4 rounded-xl border border-gray-200 focus:border-emerald-500 focus:ring-emerald-500 focus:outline-none transition-colors"
				placeholder="Ví dụ: example123@gmail.com"
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
					placeholder="Ví dụ: 0901234567, 0912345678">
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

<script src="<c:url value='/assets/js/alertPopup.js' />"></script>
<script src="<c:url value='/assets/js/previewAvatar.js' />"></script>

<script>
    document.addEventListener('DOMContentLoaded', function () {
        const form          = document.getElementById('profileForm');
        const fullNameInput = document.getElementById('full_name');
        const emailInput    = document.getElementById('email');
        const phoneInput    = document.getElementById('phone');

        const nameWarning  = document.getElementById('nameWarning');
        const emailWarning = document.getElementById('emailWarning');
        const phoneWarning = document.getElementById('phoneWarning');
        const phoneFlag    = document.getElementById('phoneFlag');

        // ===== PHONE PATTERNS =====
        const phonePatterns = {
            VN: { regex: /^(?:\+84|84|0)(?:3[2-9]|5[2689]|7[0|6-9]|8[1-9]|9[0-9])[0-9]{7}$/, flag: 'https://flagcdn.com/w40/vn.png', name: 'Việt Nam' },
            US: { regex: /^(?:\+1|1)?[2-9][0-9]{2}[2-9][0-9]{6}$/,                             flag: 'https://flagcdn.com/w40/us.png', name: 'Mỹ' },
            UK: { regex: /^(?:\+44|44|0)([1-9][0-9]{9,10})$/,                                  flag: 'https://flagcdn.com/w40/gb.png', name: 'Anh' },
            JP: { regex: /^(?:\+81|81|0)([1-9][0-9]{8,9})$/,                                   flag: 'https://flagcdn.com/w40/jp.png', name: 'Nhật Bản' }
        };

        // ===== HELPERS =====
        function showWarning(el, msg) {
            el.textContent = msg;
            el.classList.add('active');
            const inp = el.previousElementSibling;
            if (inp && inp.tagName === 'INPUT') inp.classList.add('input-error');
        }
        function clearWarning(el) {
            el.textContent = '';
            el.classList.remove('active');
            const inp = el.previousElementSibling;
            if (inp && inp.tagName === 'INPUT') inp.classList.remove('input-error');
        }
        function showWarningPhone(inputEl, warningEl, msg) {
            warningEl.querySelector('span').textContent = msg;
            warningEl.classList.add('active');
            inputEl.classList.add('input-error');
            inputEl.classList.remove('input-valid');
        }
        function clearWarningPhone(inputEl, warningEl) {
            warningEl.querySelector('span').textContent = '';
            warningEl.classList.remove('active');
            inputEl.classList.remove('input-error');
        }
        function showValid(inputEl) {
            inputEl.classList.add('input-valid');
            inputEl.classList.remove('input-error');
        }

        // ===== VALIDATION =====
        function validateFullName() {
            const val = fullNameInput.value.trim();
            clearWarning(nameWarning);
            if (!val)                          { showWarning(nameWarning, 'Vui lòng nhập họ và tên'); return false; }
            if (val.length < 2 || val.length > 50) { showWarning(nameWarning, 'Họ và tên phải từ 2 đến 50 ký tự'); return false; }
            return true;
        }

        function validateEmail() {
            const val = emailInput.value.trim();
            clearWarning(emailWarning);
            if (!val)                                                                         { showWarning(emailWarning, 'Vui lòng nhập email'); return false; }
            if (!/^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/.test(val))              { showWarning(emailWarning, 'Email không đúng định dạng'); return false; }
            return true;
        }

        function validatePhone() {
            let val = phoneInput.value.trim().replace(/\s+/g, '');
            clearWarningPhone(phoneInput, phoneWarning);
            phoneFlag.classList.add('hidden');
            phoneFlag.innerHTML = '';

            if (!val) { showWarningPhone(phoneInput, phoneWarning, 'Vui lòng nhập số điện thoại'); return false; }

            for (const data of Object.values(phonePatterns)) {
                if (data.regex.test(val)) {
                    const img = document.createElement('img');
                    img.src = data.flag;
                    img.alt = data.name;
                    img.style.width = '100%';
                    img.style.height = '100%';
                    phoneFlag.appendChild(img);
                    phoneFlag.classList.remove('hidden');
                    showValid(phoneInput);
                    return true;
                }
            }
            showWarningPhone(phoneInput, phoneWarning, 'Số điện thoại không hợp lệ (VN, US, UK, JP)');
            return false;
        }

        // ===== EVENTS =====
        fullNameInput.addEventListener('input', () => {
            if (fullNameInput.value.length > 50) fullNameInput.value = fullNameInput.value.substring(0, 50);
            validateFullName();
        });
        fullNameInput.addEventListener('blur', validateFullName);
        emailInput.addEventListener('input', validateEmail);
        emailInput.addEventListener('blur', validateEmail);
        phoneInput.addEventListener('input', validatePhone);
        phoneInput.addEventListener('blur', validatePhone);

        // ===== PREVIEW AVATAR — scoped, tránh conflict với sidebar =====
        window.previewProfileAvatar = function(input) {
            if (!input.files || !input.files[0]) return;
            const file = input.files[0];
            const reader = new FileReader();
            reader.onload = function(e) {
                const container = document.getElementById('profileAvatarContainer');
                const preview   = document.getElementById('profileAvatarPreview');
                const icon      = document.getElementById('profileAvatarIcon');

                if (preview) {
                    // Đã có ảnh → chỉ đổi src
                    preview.src = e.target.result;
                } else if (icon && container) {
                    // Chưa có ảnh → tạo img mới thay icon
                    const img = document.createElement('img');
                    img.src   = e.target.result;
                    img.id    = 'profileAvatarPreview';
                    img.className = 'w-full h-full object-cover';
                    container.replaceChild(img, icon);
                    if (window.lucide) lucide.createIcons();
                }
            };
            reader.readAsDataURL(file);
        };

        form.addEventListener('submit', async (e) => {
            e.preventDefault();
            let valid = validateFullName() & validateEmail() & validatePhone();
            if (!valid) { showPopupWarning('Lỗi', 'Vui lòng sửa các lỗi trước khi gửi'); return; }
            const confirmed = await showConfirm('Lưu thông tin', 'Bạn có chắc muốn lưu thông tin không?', 'question', 'Xác nhận');
            if (confirmed) form.submit();
        });

        validateFullName();
        validateEmail();
        validatePhone();
    });
</script>
  --%>

<!-- customer_view.jsp -->

<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<link rel="stylesheet"
	href="${pageContext.request.contextPath}/assets/css/customer/customer-view.css">

<div class="flex flex-col min-h-screen bg-white relative">

	<!-- ===== HEADER: Quay lại (trái) | Tiêu đề (phải) ===== -->
	<div
		class="p-6 border-b border-gray-100 flex items-center justify-between">

		<!-- Nút Quay Lại — BÊN TRÁI -->
		<a href="${pageContext.request.contextPath}/home"
			class="btn-back-home"> <i data-lucide="arrow-left"
			class="w-4 h-4"></i> <span>Quay Lại Trang Chủ</span>
		</a>

		<!-- Tiêu đề — BÊN PHẢI -->
		<div class="flex items-center space-x-2">
			<h1 class="text-xl font-bold text-gray-800">Thông tin cá nhân</h1>
			<i data-lucide="user" class="w-6 h-6 text-emerald-700"></i>
		</div>
	</div>

	<form action="customerController" method="POST"
		enctype="multipart/form-data" class="p-6 space-y-6" id="profileForm">
		<input type="hidden" name="action" value="updateProfile" />

		<!-- ===== AVATAR: căn giữa, thứ tự: ảnh → mô tả → nút ===== -->
		<div class="space-y-3">
			<label class="field-label"> <i data-lucide="user-circle"
				class="w-4 h-4"></i> <span>Ảnh đại diện</span>
			</label>

			<!-- Wrapper căn giữa theo chiều ngang -->
			<div class="avatar-section-wrapper">

				<!-- 1. Ảnh tròn -->
				<div class="avatar-preview" id="profileAvatarContainer">
					<c:choose>
						<c:when test="${not empty sessionScope.account.avatarPath}">
							<img
								src="${pageContext.request.contextPath}/uploads/${sessionScope.account.avatarPath}"
								alt="Avatar" id="profileAvatarPreview"
								class="w-full h-full object-cover">
						</c:when>
						<c:otherwise>
							<i data-lucide="user-circle" id="profileAvatarIcon"
								class="w-16 h-16 text-gray-400"></i>
						</c:otherwise>
					</c:choose>
				</div>

				<!-- 2. Text mô tả — ngay dưới avatar -->
				<p class="upload-hint upload-hint--center">
					<i data-lucide="info" class="w-3 h-3"></i> <span>JPG, PNG
						tối đa 2MB</span>
				</p>

				<!-- 3. Nút chọn ảnh — dưới dòng mô tả -->
				<input type="file" id="avatar" name="avatar" accept="image/*"
					class="hidden" onchange="previewProfileAvatar(this)"> <label
					for="avatar" class="btn-upload"> <i data-lucide="camera"
					class="w-4 h-4"></i> <span>Chọn ảnh đại diện</span>
				</label>

			</div>
		</div>

		<!-- ===== HỌ VÀ TÊN ===== -->
		<div class="space-y-2">
			<label for="full_name" class="field-label"> <i
				data-lucide="user" class="w-4 h-4"></i> <span>Họ và tên</span>
			</label> <input type="text" id="full_name" name="full_name"
				value="${sessionScope.account.fullName}" required maxlength="50"
				class="w-full p-4 rounded-xl border border-gray-200 focus:border-emerald-500 focus:ring-emerald-500 focus:outline-none transition-colors"
				placeholder="Ví dụ: Nguyễn Văn A">
			<div id="nameWarning" class="warning-message"></div>
		</div>

		<!-- ===== EMAIL ===== -->
		<div class="space-y-2">
			<label for="email" class="field-label"> <i data-lucide="mail"
				class="w-4 h-4"></i> <span>Email</span>
			</label> <input type="email" id="email" name="email"
				value="${sessionScope.account.email}" required
				class="w-full p-4 rounded-xl border border-gray-200 focus:border-emerald-500 focus:ring-emerald-500 focus:outline-none transition-colors"
				placeholder="Ví dụ: example123@gmail.com"
				<c:if test="${not empty sessionScope.account.googleId}">readonly</c:if>>
			<div id="emailWarning" class="warning-message"></div>

			<c:if test="${not empty sessionScope.account.googleId}">
				<p class="google-tag">
					<i data-lucide="google" class="w-3 h-3"></i> <span>Đăng nhập
						bằng Google</span>
				</p>
			</c:if>
		</div>

		<!-- ===== SỐ ĐIỆN THOẠI ===== -->
		<div class="space-y-2">
			<label for="phone" class="field-label"> <i
				data-lucide="phone" class="w-4 h-4"></i> <span>Số điện thoại</span>
				<span class="text-red-500 font-bold">*</span>
			</label>
			<div class="phone-input-wrapper">
				<input type="tel" id="phone" name="phone"
					value="${sessionScope.account.phone}" required
					class="w-full p-4 rounded-xl border border-gray-200 focus:border-emerald-500 focus:ring-emerald-500 focus:outline-none transition-colors"
					placeholder="Ví dụ: 0901234567, 0912345678">
				<div id="phoneFlag" class="phone-flag hidden"></div>
			</div>
			<div id="phoneWarning" class="warning-message">
				<i data-lucide="alert-circle" class="w-3 h-3"></i> <span></span>
			</div>
		</div>

		<!-- ===== BUTTONS ===== -->
		<div class="form-actions">
			<button type="submit" class="btn-save">
				<i data-lucide="save" class="w-5 h-5"></i> <span>Lưu thay đổi</span>
			</button>
			<button type="reset" class="btn-cancel">
				<i data-lucide="refresh-cw" class="w-4 h-4"></i> <span>Khôi
					phục</span>
			</button>
		</div>
	</form>

	<!-- ===== THÔNG BÁO SERVER ===== -->
	<c:if test="${not empty sessionScope.updateSuccess}">
		<div class="notification success mx-6 mt-4">
			<i data-lucide="check-circle" class="w-5 h-5 flex-shrink-0"></i> <span>${sessionScope.updateSuccess}</span>
		</div>
		<c:remove var="updateSuccess" scope="session" />
	</c:if>

	<c:if test="${not empty sessionScope.updateError}">
		<div class="notification error mx-6 mt-4">
			<i data-lucide="alert-circle" class="w-5 h-5 flex-shrink-0"></i> <span>${sessionScope.updateError}</span>
		</div>
		<c:remove var="updateError" scope="session" />
	</c:if>
</div>

<script src="<c:url value='/assets/js/alertPopup.js' />"></script>
<script src="<c:url value='/assets/js/previewAvatar.js' />"></script>

<script>
    document.addEventListener('DOMContentLoaded', function () {
        const form          = document.getElementById('profileForm');
        const fullNameInput = document.getElementById('full_name');
        const emailInput    = document.getElementById('email');
        const phoneInput    = document.getElementById('phone');

        const nameWarning  = document.getElementById('nameWarning');
        const emailWarning = document.getElementById('emailWarning');
        const phoneWarning = document.getElementById('phoneWarning');
        const phoneFlag    = document.getElementById('phoneFlag');

        // ===== PHONE PATTERNS =====
        const phonePatterns = {
            VN: { regex: /^(?:\+84|84|0)(?:3[2-9]|5[2689]|7[0|6-9]|8[1-9]|9[0-9])[0-9]{7}$/, flag: 'https://flagcdn.com/w40/vn.png', name: 'Việt Nam' },
            US: { regex: /^(?:\+1|1)?[2-9][0-9]{2}[2-9][0-9]{6}$/,                             flag: 'https://flagcdn.com/w40/us.png', name: 'Mỹ' },
            UK: { regex: /^(?:\+44|44|0)([1-9][0-9]{9,10})$/,                                  flag: 'https://flagcdn.com/w40/gb.png', name: 'Anh' },
            JP: { regex: /^(?:\+81|81|0)([1-9][0-9]{8,9})$/,                                   flag: 'https://flagcdn.com/w40/jp.png', name: 'Nhật Bản' }
        };

        // ===== HELPERS =====
        function showWarning(el, msg) {
            el.textContent = msg;
            el.classList.add('active');
            const inp = el.previousElementSibling;
            if (inp && inp.tagName === 'INPUT') inp.classList.add('input-error');
        }
        function clearWarning(el) {
            el.textContent = '';
            el.classList.remove('active');
            const inp = el.previousElementSibling;
            if (inp && inp.tagName === 'INPUT') inp.classList.remove('input-error');
        }
        function showWarningPhone(inputEl, warningEl, msg) {
            warningEl.querySelector('span').textContent = msg;
            warningEl.classList.add('active');
            inputEl.classList.add('input-error');
            inputEl.classList.remove('input-valid');
        }
        function clearWarningPhone(inputEl, warningEl) {
            warningEl.querySelector('span').textContent = '';
            warningEl.classList.remove('active');
            inputEl.classList.remove('input-error');
        }
        function showValid(inputEl) {
            inputEl.classList.add('input-valid');
            inputEl.classList.remove('input-error');
        }

        // ===== VALIDATION =====
        function validateFullName() {
            const val = fullNameInput.value.trim();
            clearWarning(nameWarning);
            if (!val)                          { showWarning(nameWarning, 'Vui lòng nhập họ và tên'); return false; }
            if (val.length < 2 || val.length > 50) { showWarning(nameWarning, 'Họ và tên phải từ 2 đến 50 ký tự'); return false; }
            return true;
        }

        function validateEmail() {
            const val = emailInput.value.trim();
            clearWarning(emailWarning);
            if (!val)                                                                         { showWarning(emailWarning, 'Vui lòng nhập email'); return false; }
            if (!/^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/.test(val))              { showWarning(emailWarning, 'Email không đúng định dạng'); return false; }
            return true;
        }

        function validatePhone() {
            let val = phoneInput.value.trim().replace(/\s+/g, '');
            clearWarningPhone(phoneInput, phoneWarning);
            phoneFlag.classList.add('hidden');
            phoneFlag.innerHTML = '';

            if (!val) { showWarningPhone(phoneInput, phoneWarning, 'Vui lòng nhập số điện thoại'); return false; }

            for (const data of Object.values(phonePatterns)) {
                if (data.regex.test(val)) {
                    const img = document.createElement('img');
                    img.src = data.flag;
                    img.alt = data.name;
                    img.style.width = '100%';
                    img.style.height = '100%';
                    phoneFlag.appendChild(img);
                    phoneFlag.classList.remove('hidden');
                    showValid(phoneInput);
                    return true;
                }
            }
            showWarningPhone(phoneInput, phoneWarning, 'Số điện thoại không hợp lệ (VN, US, UK, JP)');
            return false;
        }

        // ===== EVENTS =====
        fullNameInput.addEventListener('input', () => {
            if (fullNameInput.value.length > 50) fullNameInput.value = fullNameInput.value.substring(0, 50);
            validateFullName();
        });
        fullNameInput.addEventListener('blur', validateFullName);
        emailInput.addEventListener('input', validateEmail);
        emailInput.addEventListener('blur', validateEmail);
        phoneInput.addEventListener('input', validatePhone);
        phoneInput.addEventListener('blur', validatePhone);

        // ===== PREVIEW AVATAR — scoped, tránh conflict với sidebar =====
        window.previewProfileAvatar = function(input) {
            if (!input.files || !input.files[0]) return;
            const file = input.files[0];
            const reader = new FileReader();
            reader.onload = function(e) {
                const container = document.getElementById('profileAvatarContainer');
                const preview   = document.getElementById('profileAvatarPreview');
                const icon      = document.getElementById('profileAvatarIcon');

                if (preview) {
                    // Đã có ảnh → chỉ đổi src
                    preview.src = e.target.result;
                } else if (icon && container) {
                    // Chưa có ảnh → tạo img mới thay icon
                    const img = document.createElement('img');
                    img.src   = e.target.result;
                    img.id    = 'profileAvatarPreview';
                    img.className = 'w-full h-full object-cover';
                    container.replaceChild(img, icon);
                    if (window.lucide) lucide.createIcons();
                }
            };
            reader.readAsDataURL(file);
        };

        form.addEventListener('submit', async (e) => {
            e.preventDefault();
            let valid = validateFullName() & validateEmail() & validatePhone();
            if (!valid) { showPopupWarning('Lỗi', 'Vui lòng sửa các lỗi trước khi gửi'); return; }
            const confirmed = await showConfirm('Lưu thông tin', 'Bạn có chắc muốn lưu thông tin không?', 'question', 'Xác nhận');
            if (confirmed) form.submit();
        });

        validateFullName();
        validateEmail();
        validatePhone();
    });
</script>
