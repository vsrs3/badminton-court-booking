<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<style>
    /* Trạng thái lỗi */
    .input-error {
        border-color: #ef4444 !important;
        background-color: #fef2f2;
    }

    .input-error:focus {
        box-shadow: 0 0 0 3px rgba(239, 68, 68, 0.2);
    }

    /* Warning text */
    .warning-message {
        color: #d97706;
        font-size: 0.75rem;
        margin-top: 0.375rem;
        display: none;
        font-weight: 500;
    }

    .warning-message.active {
        display: block;
    }

    /* Quốc gia icon nhỏ bên phải input phone */
    .phone-flag {
        position: absolute;
        right: 1rem;
        top: 50%;
        transform: translateY(-50%);
        width: 24px;
        height: 18px;
        border-radius: 2px;
        box-shadow: 0 1px 3px rgba(0,0,0,0.1);
        pointer-events: none;
    }

    .phone-input-wrapper {
        position: relative;
    }

    /* Thông báo */
    .notification {
        margin: 1rem 0;
        padding: 1rem;
        border-radius: 0.75rem;
        display: flex;
        align-items: center;
        gap: 0.75rem;
        font-size: 0.95rem;
    }

    .notification.success {
        background-color: #ecfdf5;
        border: 1px solid #a7f3d0;
        color: #065f46;
    }

    .notification.error {
        background-color: #fef2f2;
        border: 1px solid #fecaca;
        color: #991b1b;
    }

</style>

<div class="flex flex-col h-full bg-white relative">
    <div class="p-6 border-b border-gray-100 flex items-center space-x-3">
        <div class="flex items-center space-x-2">
            <i data-lucide="user" class="w-6 h-6 text-emerald-700"></i>
            <h1 class="text-xl font-bold text-gray-800">Thông tin cá nhân</h1>
        </div>
    </div>

    <form action="customerController" method="post" enctype="multipart/form-data" class="p-6 space-y-6" id="profileForm">
        <!-- Avatar -->
        <div class="space-y-4">
            <label class="text-sm font-medium text-gray-700 flex items-center space-x-2">
                <i data-lucide="user-circle" class="w-4 h-4 text-emerald-700"></i>
                <span>Ảnh đại diện</span>
            </label>
            <div class="avatar-upload flex flex-col sm:flex-row items-start sm:items-center space-y-4 sm:space-y-0 sm:space-x-6">
                <div class="avatar-preview w-24 h-24 rounded-full overflow-hidden border-2 border-gray-200 bg-gray-50 flex items-center justify-center">
                    <c:choose>
                        <c:when test="${not empty sessionScope.account.avatarPath}">
                            <img src="${pageContext.request.contextPath}/${sessionScope.account.avatarPath}"
                                 alt="Avatar" id="avatarPreview" class="w-full h-full object-cover">
                        </c:when>
                        <c:otherwise>
                            <i data-lucide="user-circle" id="avatarIcon" class="w-16 h-16 text-gray-400"></i>
                        </c:otherwise>
                    </c:choose>
                </div>
                <div class="flex flex-col space-y-2">
                    <input type="file" id="avatar" name="avatar" accept="image/*" class="hidden" onchange="previewAvatar(this)">
                    <label for="avatar" class="btn-upload inline-flex items-center space-x-2 px-4 py-2 bg-emerald-600 text-white rounded-lg cursor-pointer hover:bg-emerald-700 transition-colors shadow-sm">
                        <i data-lucide="camera" class="w-4 h-4"></i>
                        <span>Chọn ảnh đại diện</span>
                    </label>
                    <p class="text-xs text-gray-500 flex items-center space-x-1">
                        <i data-lucide="info" class="w-3 h-3"></i>
                        <span>JPG, PNG tối đa 2MB</span>
                    </p>
                </div>
            </div>
        </div>

        <!-- Full name -->
        <div class="space-y-2">
            <label for="full_name" class="text-sm font-medium text-gray-700 flex items-center space-x-2">
                <i data-lucide="user" class="w-4 h-4 text-emerald-700"></i>
                <span>Họ và tên</span>
            </label>
            <input type="text" id="full_name" name="full_name"
                   value="${sessionScope.account.fullName}"
                   required maxlength="50"
                   class="w-full p-4 rounded-xl border border-gray-200 focus:border-emerald-500 focus:ring-emerald-500 focus:outline-none transition-colors"
                   placeholder="Ví dụ: Nguyễn Văn A">
            <div id="nameWarning" class="warning-message"></div>
        </div>

        <!-- Email -->
        <div class="space-y-2">
            <label for="email" class="text-sm font-medium text-gray-700 flex items-center space-x-2">
                <i data-lucide="mail" class="w-4 h-4 text-emerald-700"></i>
                <span>Email</span>
            </label>
            <input type="email" id="email" name="email"
                   value="${sessionScope.account.email}" required
                   class="w-full p-4 rounded-xl border border-gray-200 focus:border-emerald-500 focus:ring-emerald-500 focus:outline-none transition-colors"
                   placeholder="Ví dụ: example123@gmail.com"
                   <c:if test="${not empty sessionScope.account.googleId}">readonly</c:if>>
            <div id="emailWarning" class="warning-message"></div>

            <c:if test="${not empty sessionScope.account.googleId}">
                <p class="text-xs text-gray-500 flex items-center space-x-1 mt-1">
                    <i data-lucide="google" class="w-3 h-3"></i>
                    <span>Đăng nhập bằng Google</span>
                </p>
            </c:if>
        </div>

        <!-- Phone -->
        <div class="space-y-2">
            <label for="phone" class="text-sm font-medium text-gray-700 flex items-center space-x-2">
                <i data-lucide="phone" class="w-4 h-4 text-emerald-700"></i>
                <span>Số điện thoại</span>
                <span class="text-red-500">*</span>
            </label>
            <div class="phone-input-wrapper">
                <input type="tel"
                       id="phone"
                       name="phone"
                       value="${sessionScope.account.phone}"
                       required
                       class="w-full p-4 rounded-xl border border-gray-200 focus:border-emerald-500 focus:ring-emerald-500 focus:outline-none transition-colors pl-4 pr-12"
                       placeholder="Ví dụ: 0901234567, 0912345678">
                <div id="phoneFlag" class="phone-flag hidden"></div>
            </div>
            <!-- Warning Message -->
            <div id="phoneWarning" class="warning-message">
                <i data-lucide="alert-circle" class="w-3 h-3"></i>
                <span></span>
            </div>
        </div>

        <!-- Buttons -->
        <div class="flex flex-col sm:flex-row space-y-3 sm:space-y-0 sm:space-x-4 pt-4">
            <button type="submit" name="action" value="updateProfile"
                    class="w-full sm:w-auto flex-1 py-4 bg-green-600 text-white rounded-xl font-bold shadow-md hover:bg-green-700 transition-colors flex items-center justify-center space-x-2">
                <i data-lucide="save" class="w-5 h-5"></i>
                <span>Lưu thay đổi</span>
            </button>
            <button type="reset"
                    class="w-full sm:w-auto flex-1 py-4 bg-gray-200 text-gray-800 rounded-xl font-bold hover:bg-gray-300 transition-colors flex items-center justify-center space-x-2">
                <i data-lucide="refresh-cw" class="w-5 h-5"></i>
                <span>Khôi phục</span>
            </button>
        </div>
    </form>

    <!-- Thông báo thành công / lỗi -->
    <c:if test="${not empty sessionScope.updateSuccess}">
        <div class="notification success mx-6 mt-4">
            <i data-lucide="check-circle" class="w-5 h-5"></i>
            <span>${sessionScope.updateSuccess}</span>
        </div>
        <c:remove var="updateSuccess" scope="session"/>
    </c:if>

    <c:if test="${not empty sessionScope.updateError}">
        <div class="notification error mx-6 mt-4">
            <i data-lucide="alert-circle" class="w-5 h-5"></i>
            <span>${sessionScope.updateError}</span>
        </div>
        <c:remove var="updateError" scope="session"/>
    </c:if>
</div>

<script src="<c:url value='/assets/js/alertPopup.js' />"></script>
<script src="<c:url value='/assets/js/previewAvatar.js' />"></script>

<script>
    document.addEventListener('DOMContentLoaded', function () {
        const form = document.getElementById('profileForm');
        const fullNameInput = document.getElementById('full_name');
        const emailInput = document.getElementById('email');
        const phoneInput = document.getElementById('phone');

        const nameWarning = document.getElementById('nameWarning');
        const emailWarning = document.getElementById('emailWarning');
        const phoneWarning = document.getElementById('phoneWarning');
        const phoneFlag   = document.getElementById('phoneFlag');

        // ================= PHONE PATTERNS =================
        const phonePatterns = {
            VN: { regex:  /^(?:\+84|84|0)(?:3[2-9]|5[2689]|7[0|6-9]|8[1-9]|9[0-9])[0-9]{7}$/, flag: 'https://flagcdn.com/w40/vn.png', name: 'Việt Nam' },
            US: { regex: /^(?:\+1|1)?[2-9][0-9]{2}[2-9][0-9]{6}$/, flag: 'https://flagcdn.com/w40/us.png', name: 'Mỹ' },
            UK: { regex: /^(?:\+44|44|0)([1-9][0-9]{9,10})$/, flag: 'https://flagcdn.com/w40/gb.png', name: 'Anh' },
            JP: { regex: /^(?:\+81|81|0)([1-9][0-9]{8,9})$/, flag: 'https://flagcdn.com/w40/jp.png', name: 'Nhật Bản' }
        };

        // ================= VALIDATION HELPERS =================
        function showWarning(el, msg) {
            el.textContent = msg;
            el.classList.add('active');
            const input = el.previousElementSibling;
            if (input && input.tagName === 'INPUT') input.classList.add('input-error');
        }

        function clearWarning(el) {
            el.textContent = '';
            el.classList.remove('active');
            const input = el.previousElementSibling;
            if (input && input.tagName === 'INPUT') input.classList.remove('input-error');
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

        // ================= VALIDATION =================
        function validateFullName() {
            const val = fullNameInput.value.trim();
            clearWarning(nameWarning);

            if (!val) {
                showWarning(nameWarning, 'Vui lòng nhập họ và tên');
                return false;
            }
            if (val.length < 2 || val.length > 50) {
                showWarning(nameWarning, 'Họ và tên phải từ 2 đến 50 ký tự');
                return false;
            }
            return true;
        }

        function validateEmail() {
            const val = emailInput.value.trim();
            clearWarning(emailWarning);

            if (!val) {
                showWarning(emailWarning, 'Vui lòng nhập email');
                return false;
            }
            if (!/^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/.test(val)) {
                showWarning(emailWarning, 'Email không đúng định dạng');
                return false;
            }
            return true;
        }

        function validatePhone() {
            let val = phoneInput.value.trim().replace(/\s+/g, '');
            clearWarningPhone(phoneInput, phoneWarning);
            phoneFlag.classList.add('hidden');
            phoneFlag.innerHTML = '';

            // Kiểm tra rỗng
            if (!val) {
                showWarningPhone(phoneInput, phoneWarning, 'Vui lòng nhập số điện thoại');
                return false;
            }
            // Kiểm tra định dạng
            for (const data of Object.values(phonePatterns)) {
                if (data.regex.test(val)) {
                    // Hiển thị cờ
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

            if(!/^(?:\+84|84|0)(?:3[2-9]|5[2689]|7[0|6-9]|8[1-9]|9[0-9])[0-9]{7}$/.test(val) ||
            !/^(?:\+1|1)?[2-9][0-9]{2}[2-9][0-9]{6}$/.test(val) ||
            !/^(?:\+44|44|0)([1-9][0-9]{9,10})$/.test(val) ||
            !/^(?:\+81|81|0)([1-9][0-9]{8,9})$/.test(val)) {
                showWarningPhone(phoneInput, phoneWarning, 'Số điện thoại sai định dạng các nước (Việt Nam, Nhật Bản, Anh, Mỹ');
                return false;
            }
            return true;
        }

        // ================= EVENTS =================
        fullNameInput.addEventListener('input', () => {
            if (fullNameInput.value.length > 50) {
                fullNameInput.value = fullNameInput.value.substring(0, 50);
            }
            validateFullName();
        });
        fullNameInput.addEventListener('blur', validateFullName);

        emailInput.addEventListener('input', validateEmail);
        emailInput.addEventListener('blur', validateEmail);

        phoneInput.addEventListener('input', validatePhone);
        phoneInput.addEventListener('blur', validatePhone);

        form.addEventListener('submit', e => {
            let valid = true;
            if (!validateFullName()) valid = false;
            if (!validateEmail())    valid = false;
            if (!validatePhone())    valid = false;

            if (!valid) {
                e.preventDefault();
                if (!validateFullName()) fullNameInput.focus();
                else if (!validateEmail()) emailInput.focus();
                else if (!validatePhone()) phoneInput.focus();
            }
        });

        validateFullName();
        validateEmail();
        validatePhone();
    });
</script>