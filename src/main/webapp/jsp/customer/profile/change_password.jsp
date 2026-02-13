<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<div class="flex flex-col h-full bg-white">
    <div class="p-6 border-b border-gray-100 flex items-center space-x-3">
        <a href="profile?section=settings" class="p-2 hover:bg-gray-100 rounded-full transition-colors text-gray-600">
            <i data-lucide="arrow-left" class="w-6 h-6"></i>
        </a>
        <div class="flex items-center space-x-2">
            <i data-lucide="lock" class="w-6 h-6 text-emerald-700"></i>
            <h1 class="text-xl font-bold text-gray-800">Đổi mật khẩu</h1>
        </div>
    </div>

    <form id="changePasswordForm" action="customerController" method="post" class="p-6 space-y-6">
        <input type="hidden" name="action" value="updatePassword">
        <div class="space-y-2">
            <label class="text-sm font-medium text-gray-700 flex items-center gap-2">
                <i data-lucide="key" class="w-4 h-4 text-gray-500"></i>
                Mật khẩu hiện tại
            </label>
            <div class="relative">
                <input
                        type="password"
                        id="oldPassword"
                        name="oldPassword"
                        required
                        maxlength="20"
                        class="w-full p-4 rounded-xl border border-gray-200 focus:border-emerald-500 focus:ring-emerald-500 focus:outline-none transition-all"
                        placeholder="Nhập mật khẩu hiện tại"
                />
                <button type="button" class="absolute right-4 top-1/2 transform -translate-y-1/2 text-gray-400 toggle-password">
                    <i data-lucide="eye-off" class="w-5 h-5"></i>
                </button>
            </div>
            <p class="error-message text-red-600 text-sm hidden mt-1"></p>
        </div>

        <div class="space-y-2">
            <label class="text-sm font-medium text-gray-700 flex items-center gap-2">
                <i data-lucide="lock" class="w-4 h-4 text-gray-500"></i>
                Mật khẩu mới
            </label>
            <div class="relative">
                <input
                        type="password"
                        id="newPassword"
                        name="newPassword"
                        required
                        minlength="6"
                        maxlength="20"
                        class="w-full p-4 rounded-xl border border-gray-200 focus:border-emerald-500 focus:ring-emerald-500 focus:outline-none transition-all"
                        placeholder="Nhập mật khẩu mới (6-20 ký tự)"
                />
                <button type="button" class="absolute right-4 top-1/2 transform -translate-y-1/2 text-gray-400 toggle-password">
                    <i data-lucide="eye-off" class="w-5 h-5"></i>
                </button>
            </div>
            <p class="error-message text-red-600 text-sm hidden mt-1"></p>
            <p id="passwordStrength" class="text-sm mt-1 font-medium"></p>
            <p id="passwordSuggestion" class="text-xs text-gray-500 mt-1"></p>
        </div>

        <div class="space-y-2">
            <label class="text-sm font-medium text-gray-700 flex items-center gap-2">
                <i data-lucide="lock" class="w-4 h-4 text-gray-500"></i>
                Xác nhận mật khẩu mới
            </label>
            <div class="relative">
                <input
                        type="password"
                        id="confirmPassword"
                        name="confirmPassword"
                        required
                        maxlength="20"
                        class="w-full p-4 rounded-xl border border-gray-200 focus:border-emerald-500 focus:ring-emerald-500 focus:outline-none transition-all"
                        placeholder="Nhập lại mật khẩu mới"
                />
                <button type="button" class="absolute right-4 top-1/2 transform -translate-y-1/2 text-gray-400 toggle-password">
                    <i data-lucide="eye-off" class="w-5 h-5"></i>
                </button>
            </div>
            <p class="error-message text-red-600 text-sm hidden mt-1"></p>
        </div>

        <div class="flex gap-4 pt-4">
            <button
                    type="submit"
                    class="flex-1 py-4 bg-emerald-600 text-white rounded-xl font-bold shadow-md hover:bg-emerald-700 focus:outline-none focus:ring-2 focus:ring-emerald-500 focus:ring-offset-2 transition-all"
            >
                <i data-lucide="check" class="inline w-5 h-5 mr-2"></i>
                Lưu thay đổi
            </button>

            <button
                    type="reset"
                    class="flex-1 py-4 bg-gray-200 text-gray-800 rounded-xl font-bold hover:bg-gray-300 focus:outline-none focus:ring-2 focus:ring-gray-400 transition-all"
            >
                <i data-lucide="x" class="inline w-5 h-5 mr-2"></i>
                Hủy bỏ
            </button>
        </div>
    </form>

    <!-- Thông báo server -->
    <div class="p-6 pt-4">
        <c:if test="${not empty sessionScope.updateSuccess}">
            <div class="flex items-center gap-3 p-4 mb-6 bg-emerald-50 border border-emerald-200 text-emerald-800 rounded-xl">
                <i data-lucide="check-circle" class="w-6 h-6 flex-shrink-0"></i>
                <span>${sessionScope.updateSuccess}</span>
            </div>
            <c:remove var="updateSuccess" scope="session"/>
        </c:if>

        <c:if test="${not empty sessionScope.updateError}">
            <div class="flex items-center gap-3 p-4 mb-6 bg-red-50 border border-red-200 text-red-800 rounded-xl">
                <i data-lucide="alert-circle" class="w-6 h-6 flex-shrink-0"></i>
                <span>${sessionScope.updateError}</span>
            </div>
            <c:remove var="updateError" scope="session"/>
        </c:if>
    </div>
</div>

<script src="/assets/js/alert.js"></script>
<script>
    document.addEventListener('DOMContentLoaded', () => {
        // Toggle password visibility (giữ nguyên)
        const toggles = document.querySelectorAll('.toggle-password');
        toggles.forEach(toggle => {
            toggle.addEventListener('click', () => {
                const input = toggle.previousElementSibling;
                input.type = input.type === 'password' ? 'text' : 'password';
                toggle.innerHTML = input.type === 'text'
                    ? '<i data-lucide="eye" class="w-5 h-5"></i>'
                    : '<i data-lucide="eye-off" class="w-5 h-5"></i>';
                lucide.createIcons(toggle);
            });
        });
        lucide.createIcons();

        // Form & inputs
        const form = document.getElementById('changePasswordForm');
        const oldPass = document.getElementById('oldPassword');
        const newPass = document.getElementById('newPassword');
        const confirmPass = document.getElementById('confirmPassword');

        const strengthText = document.getElementById('passwordStrength');
        const suggestionText = document.getElementById('passwordSuggestion');

        // Helper functions
        function resetError(input) {
            const errorEl = input.parentElement.nextElementSibling;
            input.classList.remove('border-red-500', 'focus:border-red-500', 'focus:ring-red-500');
            input.classList.add('border-gray-200', 'focus:border-emerald-500', 'focus:ring-emerald-500');
            if (errorEl) errorEl.classList.add('hidden');
        }

        function setError(input, message) {
            const errorEl = input.parentElement.nextElementSibling;
            input.classList.add('border-red-500', 'focus:border-red-500', 'focus:ring-red-500');
            input.classList.remove('border-gray-200', 'focus:border-emerald-500', 'focus:ring-emerald-500');
            if (errorEl) {
                errorEl.textContent = message;
                errorEl.classList.remove('hidden');
            }
        }

        function validateLength(input) {
            const val = input.value;
            const errorEl = input.parentElement.nextElementSibling;

            if (val.length === 0) {
                resetError(input);
                return true;
            }

            if (val.length < 6) {
                setError(input, 'Mật khẩu phải có ít nhất 6 ký tự');
                return false;
            }

            if (val.length > 20) {
                setError(input, 'Mật khẩu tối đa 20 ký tự');
                return false;
            }

            resetError(input);
            return true;
        }

        function checkPasswordStrength(password) {
            if (password.length === 0) {
                strengthText.textContent = '';
                suggestionText.textContent = '';
                strengthText.className = 'text-sm mt-1 font-medium';
                return;
            }

            // Giới hạn hiển thị strength chỉ khi trong khoảng 6-20
            if (password.length < 6 || password.length > 20) {
                strengthText.textContent = '';
                suggestionText.textContent = '';
                return;
            }

            let strength = 0;
            let suggestions = [];

            if (password.length >= 8) strength += 2;
            else strength += 1;

            if (/[A-Z]/.test(password)) strength += 1;
            else suggestions.push('chữ cái in hoa');

            if (/[0-9]/.test(password)) strength += 1;
            else suggestions.push('chữ số');

            if (/[^A-Za-z0-9]/.test(password)) strength += 1;
            else suggestions.push('ký tự đặc biệt (@#$%^&*)');

            if (strength >= 4) {
                strengthText.textContent = 'Mạnh';
                strengthText.className = 'text-sm mt-1 font-medium text-emerald-600';
                suggestionText.textContent = '';
            } else if (strength >= 2) {
                strengthText.textContent = 'Trung bình';
                strengthText.className = 'text-sm mt-1 font-medium text-amber-600';
                suggestionText.textContent = suggestions.length ? 'Gợi ý: thêm ' + suggestions.join(', ') : '';
            } else {
                strengthText.textContent = 'Yếu';
                strengthText.className = 'text-sm mt-1 font-medium text-red-600';
                suggestionText.textContent = suggestions.length ? 'Gợi ý: thêm ' + suggestions.join(', ') : '';
            }
        }

        function checkConfirmMatch() {
            if (confirmPass.value.length === 0) {
                resetError(confirmPass);
                return true;
            }

            if (newPass.value !== confirmPass.value) {
                setError(confirmPass, 'Mật khẩu xác nhận không khớp');
                return false;
            }
            resetError(confirmPass);
            return true;
        }

        // Real-time validation
        function validateOnInput(input) {
            if (input === newPass) {
                validateLength(input);
                checkPasswordStrength(input.value);
            } else if (input === oldPass || input === confirmPass) {
                validateLength(input);
            }

            if (input === confirmPass || input === newPass) {
                checkConfirmMatch();
            }
        }

        [oldPass, newPass, confirmPass].forEach(input => {
            input.addEventListener('focus', () => validateOnInput(input));
            input.addEventListener('input', () => validateOnInput(input));
            input.addEventListener('blur', () => {
                if (input.value.length > 0) {
                    validateOnInput(input);
                } else {
                    resetError(input);
                }
            });
        });

        form.addEventListener('submit', async (e) => {
            e.preventDefault();

            const submitBtn = form.querySelector('button[type="submit"]');

            let hasError = false;

            if (!validateLength(oldPass) || !validateLength(newPass) || !checkConfirmMatch()) {
                hasError = true;
            }

            if (hasError) {
                await showPopupWarning('Lỗi', 'Vui lòng sửa các lỗi trước khi gửi');
                return;
            }

            const confirmed = await showConfirm('Lưu mật khẩu',
                'Bạn có chắc muốn lưu mật khẩu mới không?', 'question', 'Xác nhận');

            if (confirmed) {
                // Disable button để tránh submit lại
                submitBtn.disabled = true;

                // Submit form bằng cách tạo FormData và fetch
                const formData = new FormData(form);
                form.submit();
            }
        });
    });
</script>

<style>
    .border-red-500 { border-color: #ef4444 !important; }
    .focus\:border-red-500:focus { border-color: #ef4444 !important; }
    .focus\:ring-red-500:focus { --tw-ring-color: #ef4444 !important; }
</style>