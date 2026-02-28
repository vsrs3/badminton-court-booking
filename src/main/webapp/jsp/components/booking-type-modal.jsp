<%-- ============================================================
     booking-type-modal.jsp  –  Tự chứa (CSS inline)
     Modal 2 lựa chọn: Đặt lịch lẻ | Đặt lịch cố định
     Author: AnhTN
     ============================================================ --%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<!-- ── INLINE CSS – không phụ thuộc file ngoài ── -->
<style>
    /* Backdrop */
    #bkBackdrop {
        display: none;
        position: fixed;
        inset: 0;
        background: rgba(0, 0, 0, .55);
        backdrop-filter: blur(3px);
        z-index: 1060;
        animation: bkFadeIn .2s ease;
    }
    #bkBackdrop.bk-open { display: block; }

    /* Modal wrapper – căn giữa màn hình */
    #bkModalWrap {
        display: none;
        position: fixed;
        inset: 0;
        z-index: 1061;
        align-items: center;
        justify-content: center;
        padding: 1rem;
    }
    #bkModalWrap.bk-open { display: flex; }

    /* Modal box */
    #bkBox {
        background: #fff;
        border-radius: 1rem;
        box-shadow: 0 20px 60px rgba(0,0,0,.18);
        width: 100%;
        max-width: 420px;
        padding: 2rem 1.75rem 1.75rem;
        position: relative;
        animation: bkSlideUp .25s cubic-bezier(.34,1.56,.64,1);
    }

    @keyframes bkFadeIn   { from { opacity: 0; }   to { opacity: 1; } }
    @keyframes bkSlideUp  { from { transform: translateY(28px) scale(.96); opacity: 0; }
                             to   { transform: translateY(0)    scale(1);   opacity: 1; } }

    /* Close button */
    #bkCloseBtn {
        position: absolute; top: .875rem; right: .875rem;
        background: #f1f5f9; border: none;
        width: 2rem; height: 2rem; border-radius: 50%;
        display: flex; align-items: center; justify-content: center;
        cursor: pointer; color: #64748b; font-size: .9rem;
        transition: background .15s;
    }
    #bkCloseBtn:hover { background: #e2e8f0; }

    /* Header */
    .bk-hdr-icon {
        width: 3.5rem; height: 3.5rem; border-radius: 50%;
        background: #d1fae5;
        display: flex; align-items: center; justify-content: center;
        font-size: 1.5rem; color: #059669;
        margin: 0 auto .875rem;
    }
    .bk-hdr-title {
        text-align: center; font-size: 1.2rem; font-weight: 800;
        color: #1e293b; margin-bottom: .25rem;
    }
    .bk-hdr-sub {
        text-align: center; font-size: .85rem; color: #64748b;
        margin-bottom: 1.25rem;
    }
    .bk-hdr-sub strong { color: #059669; }

    /* Option buttons */
    .bk-opt {
        display: flex; align-items: center; gap: 1rem;
        width: 100%; border: 2px solid #e2e8f0; border-radius: .75rem;
        background: #fff; padding: 1rem 1.125rem;
        cursor: pointer; text-align: left; margin-bottom: .625rem;
        transition: border-color .15s, background .15s, transform .1s;
    }
    .bk-opt:last-child { margin-bottom: 0; }
    .bk-opt:hover {
        border-color: #10b981; background: #f0fdf4;
        transform: translateY(-1px);
    }
    .bk-opt.bk-primary { border-color: #10b981; background: #f0fdf4; }

    .bk-opt-icon {
        width: 2.75rem; height: 2.75rem; border-radius: .625rem;
        display: flex; align-items: center; justify-content: center;
        font-size: 1.25rem; flex-shrink: 0;
    }
    .bk-opt-icon.green { background: #d1fae5; color: #059669; }
    .bk-opt-icon.blue  { background: #dbeafe; color: #2563eb; }

    .bk-opt-label { font-weight: 700; color: #1e293b; font-size: .95rem; }
    .bk-opt-desc  { font-size: .78rem; color: #64748b; margin-top: .15rem; }

    .bk-badge {
        margin-left: auto; flex-shrink: 0;
        background: #10b981; color: #fff;
        font-size: .68rem; font-weight: 800;
        padding: .2rem .55rem; border-radius: 99px;
        letter-spacing: .04em;
    }

    .bk-arrow {
        margin-left: auto; flex-shrink: 0;
        color: #94a3b8; font-size: 1rem;
    }
</style>

<!-- ── BACKDROP ── -->
<div id="bkBackdrop"></div>

<!-- ── MODAL ── -->
<div id="bkModalWrap" role="dialog" aria-modal="true" aria-labelledby="bkTitle">
    <div id="bkBox">

        <!-- Close -->
        <button id="bkCloseBtn" aria-label="Đóng">
            <i class="bi bi-x-lg"></i>
        </button>

        <!-- Header -->
        <div class="bk-hdr-icon">
            <i class="bi bi-calendar2-check"></i>
        </div>
        <div class="bk-hdr-title" id="bkTitle">Chọn loại đặt lịch</div>
        <div class="bk-hdr-sub">
            Cơ sở: <strong id="bkCourtName">—</strong>
        </div>

        <!-- Option 1: Đặt lịch lẻ -->
        <button class="bk-opt bk-primary" id="bkBtnSingle" type="button">
            <div class="bk-opt-icon green">
                <i class="bi bi-calendar3-event"></i>
            </div>
            <div>
                <div class="bk-opt-label">Đặt lịch lẻ</div>
                <div class="bk-opt-desc">Chọn ngày &amp; khung giờ tùy ý, linh hoạt</div>
            </div>
            <span class="bk-badge">PHỔ BIẾN</span>
        </button>

        <!-- Option 2: Đặt lịch cố định -->
        <button class="bk-opt" id="bkBtnRecurring" type="button">
            <div class="bk-opt-icon blue">
                <i class="bi bi-arrow-repeat"></i>
            </div>
            <div>
                <div class="bk-opt-label">Đặt lịch cố định</div>
                <div class="bk-opt-desc">Lặp lại hàng tuần theo lịch cố định</div>
            </div>
            <i class="bi bi-chevron-right bk-arrow"></i>
        </button>

    </div>
</div>

<!-- ── SCRIPT ── -->
<script>
(function () {
    'use strict';

    /* ── DOM ── */
    const backdrop   = document.getElementById('bkBackdrop');
    const wrap       = document.getElementById('bkModalWrap');
    const closeBtn   = document.getElementById('bkCloseBtn');
    const courtNameEl= document.getElementById('bkCourtName');
    const btnSingle  = document.getElementById('bkBtnSingle');
    const btnRecurring= document.getElementById('bkBtnRecurring');

    let _venueId   = null;
    let _courtName = '';

    /* ── Open / Close ── */
    function openModal(venueId, courtName) {
        _venueId   = venueId;
        _courtName = courtName || 'Sân cầu lông';
        if (courtNameEl) courtNameEl.textContent = _courtName;
        backdrop.classList.add('bk-open');
        wrap.classList.add('bk-open');
        document.body.style.overflow = 'hidden';
    }

    function closeModal() {
        backdrop.classList.remove('bk-open');
        wrap.classList.remove('bk-open');
        document.body.style.overflow = '';
    }

    /* ── Helpers ── */
    function todayStr() {
        const d = new Date();
        return d.getFullYear() + '-'
            + String(d.getMonth() + 1).padStart(2, '0') + '-'
            + String(d.getDate()).padStart(2, '0');
    }

    function getCtx() {
        if (window.APP_CONTEXT_PATH !== undefined) return window.APP_CONTEXT_PATH;
        const p = window.location.pathname.split('/');
        return (p.length > 1 && p[1]) ? '/' + p[1] : '';
    }

    /* ── Listeners ── */
    closeBtn.addEventListener('click', closeModal);
    backdrop.addEventListener('click', closeModal);
    document.addEventListener('keydown', function (e) {
        if (e.key === 'Escape') closeModal();
    });

    /* Đặt lịch lẻ */
    btnSingle.addEventListener('click', function () {
        if (!_venueId) return;
        closeModal();
        window.location.href = getCtx()
            + '/jsp/booking/singlebooking/single-booking.jsp'
            + '?facilityId=' + encodeURIComponent(_venueId)
            + '&date='       + encodeURIComponent(todayStr());
    });

    /* Đặt lịch cố định (team khác làm) */
    btnRecurring.addEventListener('click', function () {
        closeModal();
        alert('Tính năng đặt lịch cố định đang được phát triển.');
    });

    /* ── Export toàn cục ── */
    window.BookingTypeModal = {
        open  : openModal,
        close : closeModal
    };

    console.log('✅ BookingTypeModal ready');
})();
</script>
