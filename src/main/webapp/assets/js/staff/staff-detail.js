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

    // L?c danh s?ch c? s? theo query (t?m trong name v? addr)
    const q = (query || '').toLowerCase();
    const filtered = SD.allLocs.filter(l =>
        l.name.toLowerCase().includes(q) || l.addr.toLowerCase().includes(q)
    );

    // Render danh s?ch c? s? ?? l?c, highlight nh?ng c? s? ?ang ???c ch?n
    document.getElementById('sd-loc-list').innerHTML = filtered.length
        ? filtered.map(loc => {

            // Ki?m tra facility n?y c? ?ang ???c ch?n kh?ng ? SD.selLocs l? m?ng ch?a ID ?ang tick xanh
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
               Kh?ng t?m th?y c? s?
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

    renderLocList(document.getElementById('sd-loc-search').value || '');
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
