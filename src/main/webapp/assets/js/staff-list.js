//staff-list.jsp

/* ══════════════════════════════════════════
   HIRE STAFF STATE
══════════════════════════════════════════ */
const HS = {
    selLoc:   null,
    allLocs:  window.SD_ALL_FACILITIES || [],
    contextPath: window.SD_CONTEXT_PATH || ''
};

/* ══════════════════════════════════════════
   OPEN / CLOSE
══════════════════════════════════════════ */
function openHireModal() {
    HS.selLoc = null;
    document.getElementById('hs-fullName').value = '';
    document.getElementById('hs-email').value    = '';
    document.getElementById('hs-phone').value    = '';
    document.getElementById('hs-loc-search').value = '';
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
    const q      = (query || '').toLowerCase();
    const filtered = HS.allLocs.filter(l =>
        l.name.toLowerCase().includes(q) || l.addr.toLowerCase().includes(q)
    );

    const selId = HS.selLoc ? String(HS.selLoc) : null;

    document.getElementById('hs-loc-list').innerHTML = filtered.length
        ? filtered.map(loc => {
            const sel = selId === String(loc.id);
            return '<button type="button"'
                + ' class="hs-loc-item' + (sel ? ' sel' : '') + '"'
                + ' onclick="hsSelectLoc(\'' + loc.id + '\')">'
                + '<div class="hs-loc-icon ' + (sel ? 'on' : 'off') + '">'
                + '<i class="bi bi-building"></i></div>'
                + '<div style="flex:1;min-width:0;">'
                + '<p class="hs-loc-name">' + escHtml(loc.name) + '</p>'
                + '<p class="hs-loc-addr">' + escHtml(loc.addr) + '</p>'
                + '</div>'
                + (sel ? '<i class="bi bi-check-circle-fill hs-loc-check"></i>' : '')
                + '</button>';
        }).join('')
        : '<p style="font-size:.75rem;color:var(--color-gray-400);text-align:center;padding:1rem 0;font-weight:600;">Không tìm thấy cơ sở</p>';

    document.getElementById('hs-sel-count').textContent = HS.selLoc ? '1' : '0';
}

function hsSelectLoc(id) {
    HS.selLoc = HS.selLoc === String(id) ? null : String(id);
    renderHsLocList(document.getElementById('hs-loc-search').value || '');
}

/* ══════════════════════════════════════════
   DESC COUNTER
══════════════════════════════════════════ */
/*function updateDescCount() {
    const val = document.getElementById('hs-desc').value;
    if (val.length > 500) document.getElementById('hs-desc').value = val.substring(0, 500);
    document.getElementById('hs-desc-count').textContent = 
        Math.min(val.length, 500) + '/500';
}*/

/* ══════════════════════════════════════════
   SUBMIT
══════════════════════════════════════════ */
function submitHireForm() {
    const fullName = document.getElementById('hs-fullName').value.trim();
    const email    = document.getElementById('hs-email').value.trim();
    const phone    = document.getElementById('hs-phone').value.trim();

    if (!fullName || !email || !phone) {
        alert('Vui lòng điền đầy đủ thông tin bắt buộc.');
        return;
    }
    if (!HS.selLoc) {
        alert('Vui lòng chọn cơ sở phụ trách.');
        return;
    }

    const form = document.createElement('form');
    form.method = 'POST';
    form.action = HS.contextPath + '/owner/staffs/create';

    const fields = {
        fullName:    fullName,
        email:       email,
        phone:       phone,
        facilityId:  HS.selLoc,
        description: document.getElementById('hs-desc').value.trim()
    };

    Object.entries(fields).forEach(([name, value]) => {
        const input = document.createElement('input');
        input.type  = 'hidden';
        input.name  = name;
        input.value = value;
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
        .replace(/&/g, '&amp;').replace(/</g, '&lt;')
        .replace(/>/g, '&gt;').replace(/"/g, '&quot;');
}

/* ══════════════════════════════════════════
   INIT
══════════════════════════════════════════ */
document.addEventListener('DOMContentLoaded', function () {
    renderHsLocList('');
    document.getElementById('hs-modal').addEventListener('click', function (e) {
        if (e.target === this) closeHireModal();
    });
});