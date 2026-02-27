/**
 * hire-form-validation.js
 * Validation dành riêng cho Hire Staff Form
 */

const HireFormValidation = (() => {

    // ── Phone patterns (VN, US, UK, JP) ──
    const PHONE_PATTERNS = [
        /^(?:\+84|84|0)(?:3[2-9]|5[2689]|7[06-9]|8[1-9]|9[0-9])[0-9]{7}$/,
        /^(?:\+1|1)?[2-9][0-9]{2}[2-9][0-9]{6}$/,
        /^(?:\+44|44|0)[1-9][0-9]{9,10}$/,
        /^(?:\+81|81|0)[1-9][0-9]{8,9}$/
    ];

    // ── Pure validators — trả về string lỗi hoặc null nếu hợp lệ ──
    const _validators = {
        fullName(val) {
            if (!val)                          return 'Vui lòng nhập họ và tên';
            if (val.length < 2)                return 'Họ và tên phải có ít nhất 2 ký tự';
            if (val.length > 50)               return 'Họ và tên không được vượt quá 50 ký tự';
            return null;
        },
        email(val) {
            if (!val)                          return 'Vui lòng nhập email';
            if (!/^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/.test(val))
                                               return 'Email không đúng định dạng';
            return null;
        },
        phone(val) {
            const v = val.replace(/\s+/g, '');
            if (!v)                            return 'Vui lòng nhập số điện thoại';
            if (!PHONE_PATTERNS.some(r => r.test(v)))
                                               return 'Số điện thoại không hợp lệ (VN, US, UK, JP)';
            return null;
        },
        facilityId(val) {
            if (!val)                          return 'Vui lòng chọn cơ sở phụ trách';
            return null;
        }
    };

    // ── UI helpers ──
    function _getOrCreateWarning(input) {
        let el = input.parentNode.querySelector('.hf-field-warning');
        if (!el) {
            el = document.createElement('span');
            el.className = 'hf-field-warning';
            el.style.cssText = 'display:none;color:#e74c3c;font-size:0.8rem;margin-top:4px;display:block;';
            input.parentNode.insertBefore(el, input.nextSibling);
        }
        return el;
    }

    function _showWarning(input, warningEl, msg) {
        warningEl.textContent = msg;
        warningEl.style.display = 'block';
        input.style.borderColor = '#e74c3c';
    }

    function _clearWarning(input, warningEl) {
        warningEl.textContent = '';
        warningEl.style.display = 'none';
        input.style.borderColor = '';
    }

    /**
     * Bind inline validation vào một input.
     * @param {HTMLInputElement} input
     * @param {'fullName'|'email'|'phone'} type
     * @returns {{ validate: () => boolean }}
     */
    function _bindField(input, type) {
        const warningEl = _getOrCreateWarning(input);

        function validate() {
            const err = _validators[type](input.value.trim());
            if (err) { _showWarning(input, warningEl, err); return false; }
            _clearWarning(input, warningEl);
            return true;
        }

        input.addEventListener('input', () => {
            // Giới hạn độ dài realtime cho fullName
            if (type === 'fullName' && input.value.length > 50)
                input.value = input.value.substring(0, 50);
            validate();
        });
        input.addEventListener('blur', validate);

        return { validate };
    }

    // ── State nội bộ ──
    let _fields = null;   // { fullName, email, phone }
    let _getLocFn = null; // hàm trả về facilityId hiện tại

    /**
     * Khởi tạo validation cho hire form.
     * Gọi một lần duy nhất (lazy) khi modal mở lần đầu.
     *
     * @param {{ fullName, email, phone: HTMLInputElement }} inputs
     * @param {() => string|null} getSelectedFacilityId  - hàm trả về facilityId đang chọn
     */
    function init(inputs, getSelectedFacilityId) {
        if (_fields) return; // đã init rồi thì bỏ qua
        _fields = {
            fullName : _bindField(inputs.fullName, 'fullName'),
            email    : _bindField(inputs.email,    'email'),
            phone    : _bindField(inputs.phone,    'phone'),
        };
        _getLocFn = getSelectedFacilityId;
    }

    /**
     * Validate toàn bộ hire form trước khi submit.
     * @returns {{ valid: boolean, facilityError: string|null }}
     */
    function validateAll() {
        if (!_fields) throw new Error('HireFormValidation chưa được init()');

        const nameOk  = _fields.fullName.validate();
        const emailOk = _fields.email.validate();
        const phoneOk = _fields.phone.validate();

        const facilityErr = _validators.facilityId(_getLocFn ? _getLocFn() : null);

        return {
            valid: nameOk && emailOk && phoneOk && !facilityErr,
            facilityError: facilityErr
        };
    }

    /** Reset warning của tất cả các field (dùng khi đóng/reset modal) */
    function resetAll() {
        if (!_fields) return;
        ['fullName', 'email', 'phone'].forEach(key => {
            const input = document.getElementById(
                key === 'fullName' ? 'hs-fullName' :
                key === 'email'    ? 'hs-email'    : 'hs-phone'
            );
            if (input) {
                const warningEl = input.parentNode.querySelector('.hf-field-warning');
                if (warningEl) _clearWarning(input, warningEl);
            }
        });
    }

    return { init, validateAll, resetAll };

})();