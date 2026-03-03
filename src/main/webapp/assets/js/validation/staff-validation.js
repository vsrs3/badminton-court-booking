
document.addEventListener('DOMContentLoaded', function() {
    const form = document.querySelector('form[action*="update-info"]');
    if (!form) return;

    const fullNameInput = form.querySelector('input[name="fullName"]');
    const emailInput = form.querySelector('input[name="email"]');
    const phoneInput = form.querySelector('input[name="phone"]');

    // ── Tạo warning elements ──
    function createWarning(input) {
        const el = document.createElement('span');
        el.className = 'sd-field-warning';
        el.style.cssText = 'display:none;color:#e74c3c;font-size:0.8rem;margin-top:4px;';
        input.parentNode.insertBefore(el, input.nextSibling);
        return el;
    }

    const nameWarning = createWarning(fullNameInput);
    const emailWarning = createWarning(emailInput);
    const phoneWarning = createWarning(phoneInput);

    // ── Helpers ──
    function showWarning(input, warningEl, msg) {
        warningEl.textContent = msg;
        warningEl.style.display = 'block';
        input.style.borderColor = '#e74c3c';
    }

    function clearWarning(input, warningEl) {
        warningEl.textContent = '';
        warningEl.style.display = 'none';
        input.style.borderColor = '';
    }

    // ── Phone patterns (VN, US, UK, JP) ──
    const phonePatterns = [
        /^(?:\+84|84|0)(?:3[2-9]|5[2689]|7[06-9]|8[1-9]|9[0-9])[0-9]{7}$/,
        /^(?:\+1|1)?[2-9][0-9]{2}[2-9][0-9]{6}$/,
        /^(?:\+44|44|0)[1-9][0-9]{9,10}$/,
        /^(?:\+81|81|0)[1-9][0-9]{8,9}$/
    ];

    // ── Validation functions ──
    function validateFullName() {
        const val = fullNameInput.value.trim();
        clearWarning(fullNameInput, nameWarning);
        if (!val) {
            showWarning(fullNameInput, nameWarning, 'Vui lòng nhập họ và tên');
            return false;
        }
        if (val.length < 2 || val.length > 50) {
            showWarning(fullNameInput, nameWarning, 'Họ và tên phải từ 2 đến 50 ký tự');
            return false;
        }
        return true;
    }

    function validateEmail() {
        const val = emailInput.value.trim();
        clearWarning(emailInput, emailWarning);
        if (!val) {
            showWarning(emailInput, emailWarning, 'Vui lòng nhập email');
            return false;
        }
        if (!/^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/.test(val)) {
            showWarning(emailInput, emailWarning, 'Email không đúng định dạng');
            return false;
        }
        return true;
    }

    function validatePhone() {
        const val = phoneInput.value.trim().replace(/\s+/g, '');
        clearWarning(phoneInput, phoneWarning);
        if (!val) {
            showWarning(phoneInput, phoneWarning, 'Vui lòng nhập số điện thoại');
            return false;
        }
        const valid = phonePatterns.some(regex => regex.test(val));
        if (!valid) {
            showWarning(phoneInput, phoneWarning, 'Số điện thoại không hợp lệ (VN, US, UK, JP)');
            return false;
        }
        return true;
    }

    // ── Events ──
    fullNameInput.addEventListener('input', () => {
        if (fullNameInput.value.length > 50)
            fullNameInput.value = fullNameInput.value.substring(0, 50);
        validateFullName();
    });
    fullNameInput.addEventListener('blur', validateFullName);

    emailInput.addEventListener('input', validateEmail);
    emailInput.addEventListener('blur', validateEmail);

    phoneInput.addEventListener('input', validatePhone);
    phoneInput.addEventListener('blur', validatePhone);

    form.addEventListener('submit', function(e) {
        const valid = validateFullName() & validateEmail() & validatePhone();
        if (!valid) {
            e.preventDefault();
            // Nếu có hàm showPopupWarning thì dùng, không thì alert
            if (typeof showPopupWarning === 'function') {
                showPopupWarning('Lỗi', 'Vui lòng sửa các lỗi trước khi gửi');
            } else {
                alert('Vui lòng sửa các lỗi trước khi gửi');
            }
            return;
        }
        e.preventDefault();
        // Nếu có hàm showConfirm thì dùng, không thì confirm mặc định
        if (typeof showConfirm === 'function') {
            showConfirm('Lưu thông tin', 'Bạn có chắc muốn lưu thông tin không?', 'question', 'Xác nhận')
                .then(confirmed => { if (confirmed) form.submit(); });
        } else {
            if (confirm('Bạn có chắc muốn lưu thông tin không?')) form.submit();
        }
    });
});