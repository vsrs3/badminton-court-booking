/* reset-password.js */

// Biến cục bộ
let _rpAccountId = null;
let _rpTempPass = null;

// Mở tab xác nhận đặt lại mật khẩu, nếu xác nhận sẽ đổi mật khẩu
function openResetModal(accountId, staffName) {
    _rpAccountId = accountId;
	
    document.getElementById('sd-rp-name').textContent = staffName;
    document.getElementById('sd-rp-step1').classList.remove('d-none');
    document.getElementById('sd-rp-step2').classList.add('d-none');
	
    _rpTempPass = null;
    _resetCopyBtn();
	
    document.getElementById('sd-rp-modal').classList.add('open');
}

// Nếu click xác nhận, đổi mật khẩu ngay và hiện ra mk để copy
// Nếu click Hủy, trở về trang ban đầu
function closeResetModal() {
    document.getElementById('sd-rp-modal').classList.remove('open');
    _rpTempPass = null;
    _resetCopyBtn();
    const btn = document.getElementById('sd-rp-cfm-btn');
    if (btn) {
        btn.disabled = false;
        document.getElementById('sd-rp-cfm-txt').textContent = 'Xác nhận đặt lại';
    }
}

// Thực hiện đặt lại mật khẩu cho nhân viên
async function doResetPassword() {
	
    const btn = document.getElementById('sd-rp-cfm-btn');
    const txt = document.getElementById('sd-rp-cfm-txt');
	
    btn.disabled = true;
    txt.innerHTML = '<span class="sd-spinner"></span> Đang xử lý...';

    try {
        const contextPath = window.SD_CONTEXT_PATH || '';

        const res = await fetch(`${contextPath}/owner/staffs/reset-password` , {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: new URLSearchParams({ accountId: _rpAccountId})
        });
		if (!res.ok) throw new Error("Server error");
		
        const json = await res.json();
        if (!json.success) throw new Error(json.message);
		
        _rpTempPass = json.tempPassword;
        
        document.getElementById('sd-pass-val').textContent = _rpTempPass;
        document.getElementById('sd-rp-step1').classList.add('d-none');
        document.getElementById('sd-rp-step2').classList.remove('d-none');

    } catch (err) {
        alert('Có lỗi xảy ra. Vui lòng thử lại.');
        console.error(err);
    } finally {
        btn.disabled = false;
        txt.textContent = 'Xác nhận đặt lại';
    }
}

/*async function doResetPassword() {
    const btn = document.getElementById('sd-rp-cfm-btn');
    const txt = document.getElementById('sd-rp-cfm-txt');

    btn.disabled = true;
    txt.innerHTML = '<span class="sd-spinner"></span> Đang xử lý...';

    const contextPath = window.SD_CONTEXT_PATH || '';

    // Tạo form ẩn và submit
    const form = document.createElement('form');
    form.method = 'POST';
    form.action = `${contextPath}/owner/staffs/reset-password`;

    const inputId = document.createElement('input');
    inputId.type = 'hidden';
    inputId.name = 'accountId';
    inputId.value = _rpAccountId;
    form.appendChild(inputId);

    const inputRedirect = document.createElement('input');
    inputRedirect.type = 'hidden';
    inputRedirect.name = 'redirect';
    inputRedirect.value = _rpRedirect;
    form.appendChild(inputRedirect);

    document.body.appendChild(form);
    form.submit();
}*/

// Owner có thể Sao chép thủ công mật khẩu vào bộ nhớ tạm
function copyPassword() {
    if (!_rpTempPass) return;
    const fallback = () => {
        const el = document.createElement('textarea');
        el.value = _rpTempPass;
        document.body.appendChild(el);
        el.select();
        document.execCommand('copy');
        document.body.removeChild(el);
    };
    (navigator.clipboard
        ? navigator.clipboard.writeText(_rpTempPass).catch(fallback)
        : Promise.resolve(fallback())
    ).then(() => {
        const btn = document.getElementById('sd-copy-btn');
        btn.innerHTML = '<i class="bi bi-check-lg"></i>';
        btn.classList.add('copied');
        setTimeout(_resetCopyBtn, 2000);
    });
}

// Nút nhấn để sao chép mật khẩu vào bộ nhớ t
function _resetCopyBtn() {
    const btn = document.getElementById('sd-copy-btn');
    if (btn) {
        btn.innerHTML = '<i class="bi bi-clipboard"></i>';
        btn.classList.remove('copied');
    }
}