/**
 * staff-detail.js
 *
 * Biến global khai báo trong JSP trước khi load file này:
 *   window.SD_STAFF_NAME      — Account.full_name
 *   window.SD_ACCOUNT_ID      — Account.account_id
 *   window.SD_AVATAR_PATH     — Account.avatar_path (rỗng nếu chưa có)
 *   window.SD_ASSIGNED_IDS    — [facilityId] hiện tại của Staff
 *   window.SD_ALL_FACILITIES  — [{id, name, addr}, …] tất cả facility của owner
 *   window.SD_CONTEXT_PATH    — ${pageContext.request.contextPath}
 */

/* STATE */
const SD = {
    staffName:   window.SD_STAFF_NAME     || '',
    accountId:   window.SD_ACCOUNT_ID     || '',
    avatarPath:  window.SD_AVATAR_PATH    || '',
	isActive:    window.SD_IS_ACTIVE,
    selLocs:     [...(window.SD_ASSIGNED_IDS   || [])],
    allLocs:     window.SD_ALL_FACILITIES  || [],
    contextPath: window.SD_CONTEXT_PATH    || '',
    tempPass:    null,
    /* avatar upload state */
    av: {
        file:        null,   // File object yang dipilih
        previewUrl:  null    // Object URL sementara
    }
};

/* ══════════════════════════════════════════
   TAB SWITCH
══════════════════════════════════════════ */
function switchTab(tab) {
    const toEdit = tab === 'edit';

    document.getElementById('sd-panel-ov').classList.toggle('d-none',  toEdit);
    document.getElementById('sd-panel-ed').classList.toggle('d-none', !toEdit);

    document.getElementById('sd-tab-ov').classList.toggle('active', !toEdit);
    document.getElementById('sd-tab-ed').classList.toggle('active',  toEdit);

    document.getElementById('sd-btn-edit').classList.toggle('d-none',   toEdit);
    document.getElementById('sd-btn-cancel').classList.toggle('d-none', !toEdit);

    if (toEdit) {
        SD.selLocs = [...(window.SD_ASSIGNED_IDS || [])];
        renderLocList('');
        const srch = document.getElementById('sd-loc-search');
        if (srch) srch.value = '';
        window.scrollTo({ top: 0, behavior: 'smooth' });
    }
}

/* ══════════════════════════════════════════
   LOCATION PICKER
   Chỉ cho phép chọn 1 facility tại một thời điểm
   vì Staff.facility_id là FK đơn
══════════════════════════════════════════ */
function renderLocList(query) {
	
	// Lọc danh sách cơ sở theo query (tìm trong name và addr)
    const q = (query || '').toLowerCase();
    const filtered = SD.allLocs.filter(l =>
        l.name.toLowerCase().includes(q) || l.addr.toLowerCase().includes(q)
    );

	// Render danh sách cơ sở đã lọc, highlight những cơ sở đang được chọn
    document.getElementById('sd-loc-list').innerHTML = filtered.length  ? filtered.map( loc => {
		
			// Kiểm tra facility này có đang được chọn không — SD.selLocs là mảng chứa ID đang tick xanh
            const sel = SD.selLocs.includes(String(loc.id));

			return `<button type="button"
                        class="sd-loc-item${sel ? ' sel' : ''}"
                        onclick="selectLoc('${loc.id}')">
						
                <div class="sd-loc-icon ${sel ? 'on' : 'off'}">
                    <i class="bi bi-building"></i>
                </div>
				
                <div style="flex:1;min-width:0;">
                    <p class="sd-loc-name mb-0">${escHtml(loc.name)}</p>
                    <p class="sd-loc-addr mb-0">${escHtml(loc.addr)}</p>
                </div>
				
                ${sel ? '<i class="bi bi-check-circle-fill sd-loc-check"></i>' : ''}
            </button>`;
        }).join('')
        : `<p style="font-size:.75rem;color:var(--color-gray-400);
                     text-align:center;padding:1rem 0;font-weight:600;">
               Không tìm thấy cơ sở
           </p>`;
}

/*  xử lý khi user click vào 1 facility */
function selectLoc(id) {
    id = String(id);
	
	/* Nếu đã chọn rồi thì bỏ chọn (SD.selLocs thành rỗng), 
		Nếu chưa chọn thì chọn (SD.selLocs chỉ chứa id đó) */
    SD.selLocs = SD.selLocs.includes(id) ? [] : [id];

    /* Sync vào hidden input của Form.
		Nếu không có gì được chọn thì gửi rỗng. */
    const hidden = document.getElementById('selectedFacilityId');
    if (hidden) hidden.value = SD.selLocs[0] || '';

    renderLocList(document.getElementById('sd-loc-search')?.value || '');
}

/* TERMINATE */
function toggleStaff() {
    const msg = SD.isActive ? 'Bạn có chắc chắn muốn xóa nhân viên này?' 
                         : 'Bạn có chắc chắn muốn khôi phục nhân viên này?';
    if (confirm(msg)) {
        window.location.href = SD.contextPath + '/owner/staffs/toggle/' +  SD.accountId + '?redirect=detail';
    }
}

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

/* ══════════════════════════════════════════
   UTIL
══════════════════════════════════════════ */
function escHtml(str) {
    return String(str)
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;');
}

/* ══════════════════════════════════════════
   INIT
══════════════════════════════════════════ */

document.addEventListener('DOMContentLoaded', function () {
    renderLocList('');

    const hidden = document.getElementById('selectedFacilityId');
    if (hidden && SD.selLocs.length) hidden.value = SD.selLocs[0];

    // Lắng nghe sự kiện reset của browser
    const form = document.querySelector('#sd-panel-ed form');
    if (form) {
        form.addEventListener('reset', function () {
            // Browser đã restore hidden input về value HTML ban đầu rồi
            // Chỉ cần sync lại SD.selLocs theo đó
            SD.selLocs = [...(window.SD_ASSIGNED_IDS || [])];

            const srch = document.getElementById('sd-loc-search');
            if (srch) srch.value = '';

            renderLocList('');
        });
    }
});
