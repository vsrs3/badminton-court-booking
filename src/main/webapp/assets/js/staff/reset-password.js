/* RESET PASSWORD MODAL */

function openResetModal() {
    document.getElementById('sd-rp-name').textContent = SD.staffName;
    document.getElementById('sd-rp-step1').classList.remove('d-none');
    document.getElementById('sd-rp-step2').classList.add('d-none');
    SD.tempPass = null;
    _resetCopyBtn();
    document.getElementById('sd-rp-modal').classList.add('open');
}

function closeResetModal() {
    document.getElementById('sd-rp-modal').classList.remove('open');
    SD.tempPass = null;
    _resetCopyBtn();
    const btn = document.getElementById('sd-rp-cfm-btn');
    if (btn) {
        btn.disabled = false;
        document.getElementById('sd-rp-cfm-txt').textContent = 'Xác nhận đặt lại';
    }
}

async function doResetPassword() {
    const btn = document.getElementById('sd-rp-cfm-btn');
    const txt = document.getElementById('sd-rp-cfm-txt');
    btn.disabled = true;
    txt.innerHTML = '<span class="sd-spinner"></span> Đang xử lý...';

    try {
        /*
         * TODO: Thay bằng fetch thật:
         * const res  = await fetch(`${SD.contextPath}/owner/staffs/reset-password`, {
         *     method: 'POST',
         *     headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
         *     body: new URLSearchParams({ accountId: SD.accountId })
         * });
         * const json = await res.json();  // { success: true, tempPassword: "Abc123!@" }
         * if (!json.success) throw new Error(json.message);
         * SD.tempPass = json.tempPassword;
         */

        /* Demo — tạo mật khẩu client-side */
        await new Promise(r => setTimeout(r, 1200));
        const chars  = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*';
        SD.tempPass  = Array.from({ length: 12 }, () =>
            chars[Math.floor(Math.random() * chars.length)]
        ).join('');

        document.getElementById('sd-pass-val').textContent = SD.tempPass;
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

function copyPassword() {
    if (!SD.tempPass) return;
    const fallback = () => {
        const el = document.createElement('textarea');
        el.value = SD.tempPass;
        document.body.appendChild(el);
        el.select();
        document.execCommand('copy');
        document.body.removeChild(el);
    };
    (navigator.clipboard
        ? navigator.clipboard.writeText(SD.tempPass).catch(fallback)
        : Promise.resolve(fallback())
    ).then(() => {
        const btn = document.getElementById('sd-copy-btn');
        btn.innerHTML = '<i class="bi bi-check-lg"></i>';
        btn.classList.add('copied');
        setTimeout(_resetCopyBtn, 2000);
    });
}

function _resetCopyBtn() {
    const btn = document.getElementById('sd-copy-btn');
    if (btn) { btn.innerHTML = '<i class="bi bi-clipboard"></i>'; btn.classList.remove('copied'); }
}
