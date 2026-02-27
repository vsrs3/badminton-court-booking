//staff-list.js

/* ══════════════════════════════════════════
   HIRE STAFF STATE
══════════════════════════════════════════ */
const HS = {
    selLoc     : null,
    allLocs    : window.SD_ALL_FACILITIES || [],
    contextPath: window.SD_CONTEXT_PATH   || ''
};

/* ══════════════════════════════════════════
   OPEN / CLOSE
══════════════════════════════════════════ */
function openHireModal() {
    // Reset state
    HS.selLoc = null;

    // Reset input values
    document.getElementById('hs-fullName').value = '';
    document.getElementById('hs-email').value    = '';
    document.getElementById('hs-phone').value    = '';
    document.getElementById('hs-loc-search').value = '';

    // Init validation lazy (chỉ bind DOM events lần đầu)
    HireFormValidation.init(
        {
            fullName : document.getElementById('hs-fullName'),
            email    : document.getElementById('hs-email'),
            phone    : document.getElementById('hs-phone'),
        },
        () => HS.selLoc
    );

    // Reset warnings
    HireFormValidation.resetAll();

    renderHsLocList('');
    document.getElementById('hs-modal').classList.add('open');
}

function closeHireModal() {
    document.getElementById('hs-modal').classList.remove('open');
}

/* ══════════════════════════════════════════
   LOCATION PICKER
══════════════════════════════════════════ */
function renderHsLocList(query) {
    const q        = (query || '').toLowerCase();
    const filtered = HS.allLocs.filter(l =>
        l.name.toLowerCase().includes(q) ||
        l.addr.toLowerCase().includes(q)
    );
    const selId = HS.selLoc ? String(HS.selLoc) : null;

    document.getElementById('hs-loc-list').innerHTML = filtered.length
        ? filtered.map(loc => {
            const sel = selId === String(loc.id);
            return `
                <div class="hs-loc-item ${sel ? 'selected' : ''}" onclick="hsSelectLoc('${loc.id}')">
                    <div class="hs-loc-info">
                        <div class="hs-loc-name">${escHtml(loc.name)}</div>
                        <div class="hs-loc-addr">${escHtml(loc.addr)}</div>
                    </div>
                    ${sel
                        ? '<span class="hs-loc-check">&#10003;</span>'
                        : '<span class="hs-loc-check hs-loc-check--empty"></span>'
                    }
                </div>`;
        }).join('')
        : '<div class="hs-loc-empty">Không tìm thấy cơ sở</div>';

    document.getElementById('hs-sel-count').textContent = HS.selLoc ? '1' : '0';
}

function hsSelectLoc(id) {
    HS.selLoc = HS.selLoc === String(id) ? null : String(id);
    renderHsLocList(document.getElementById('hs-loc-search').value || '');
}

/* ══════════════════════════════════════════
   SUBMIT
══════════════════════════════════════════ */
function submitHireForm() {
    const { valid, facilityError } = HireFormValidation.validateAll();

    // Báo lỗi facility trước (vì không có inline warning cho location)
    if (facilityError) {
        if (typeof showPopupWarning === 'function')
            showPopupWarning('Lỗi', facilityError);
        else
            alert(facilityError);
        return;
    }

    // Báo lỗi các field còn lại
    if (!valid) {
        if (typeof showPopupWarning === 'function')
            showPopupWarning('Lỗi', 'Vui lòng sửa các lỗi trước khi gửi');
        else
            alert('Vui lòng sửa các lỗi trước khi gửi.');
        return;
    }

    // Build & submit form
    const form    = document.createElement('form');
    form.method   = 'POST';
    form.action   = HS.contextPath + '/owner/staffs/create';

    const fields = {
        fullName   : document.getElementById('hs-fullName').value.trim(),
        email      : document.getElementById('hs-email').value.trim(),
        phone      : document.getElementById('hs-phone').value.trim(),
        facilityId : HS.selLoc
    };

    Object.entries(fields).forEach(([name, value]) => {
        const input  = document.createElement('input');
        input.type   = 'hidden';
        input.name   = name;
        input.value  = value;
        form.appendChild(input);
    });

    document.body.appendChild(form);
    form.submit();
}

/* ══════════════════════════════════════════
   UTIL
══════════════════════════════════════════ */
function escHtml(str) {
    return String(str)
        .replace(/&/g,  '&amp;')
        .replace(/</g,  '&lt;')
        .replace(/>/g,  '&gt;')
        .replace(/"/g,  '&quot;');
}

/* ══════════════════════════════════════════
   INIT
══════════════════════════════════════════ */
document.addEventListener('DOMContentLoaded', function () {
    renderHsLocList('');

    // Đóng modal khi click ra ngoài
    document.getElementById('hs-modal').addEventListener('click', function (e) {
        if (e.target === this) closeHireModal();
    });

    // Tìm kiếm cơ sở realtime
    document.getElementById('hs-loc-search').addEventListener('input', function () {
        renderHsLocList(this.value);
    });
});