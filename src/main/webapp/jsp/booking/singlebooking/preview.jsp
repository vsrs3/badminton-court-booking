<%-- ============================================================
     preview.jsp  –  Xem trước & xác nhận đặt sân
     Dữ liệu lấy từ sessionStorage key "sbPreviewData"
     (do single-booking.jsp lưu sau khi gọi POST /api/single-booking/preview)
     Author: AnhTN
     ============================================================ --%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.bcb.model.Account" %>
<%
    Account account    = (Account) session.getAttribute("account");
    boolean isLoggedIn = account != null;
    String ctx         = request.getContextPath();
    if (!isLoggedIn) {
        response.sendRedirect(ctx + "/auth/login");
        return;
    }
%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
    <title>Xác nhận đặt sân – BadmintonPro</title>

    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet"/>
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css"/>
    <link rel="stylesheet" href="<%= ctx %>/assets/css/badminton-pro.css"/>

    <style>
        body { background: #f8fafc; }

        /* ── Header ── */
        .pv-header { background: #064E3B; padding: 1rem 1.5rem; box-shadow: 0 4px 12px rgba(0,0,0,.15); }
        .pv-header-inner { max-width: 860px; margin: 0 auto; display: flex; align-items: center; gap: 1rem; }
        .pv-title { color: #fff; font-size: 1.125rem; font-weight: 800; }

        /* ── Container ── */
        .pv-container { max-width: 860px; margin: 0 auto; padding: 1.5rem 1rem 7.5rem; }

        /* ── Cards ── */
        .pv-card { background:#fff; border-radius:.75rem; box-shadow:0 1px 4px rgba(0,0,0,.08); padding:1.25rem 1.5rem; margin-bottom:1rem; }
        .pv-section-label {
            font-size:.75rem; font-weight:800; text-transform:uppercase;
            letter-spacing:.1em; color:#64748b;
            display:flex; align-items:center; gap:.4rem; margin-bottom:.875rem;
        }

        /* ── Court detail rows ── */
        .pv-court-card { border:1px solid #e2e8f0; border-radius:.625rem; padding:1rem 1.25rem; margin-bottom:.75rem; }
        .pv-court-card:last-child { margin-bottom:0; }
        .pv-court-name { font-weight:700; color:#1e293b; margin-bottom:.5rem; font-size:.95rem; }
        .pv-range-row {
            display:flex; justify-content:space-between; align-items:center;
            padding:.375rem 0; border-bottom:1px solid #f1f5f9; font-size:.875rem;
        }
        .pv-range-row:last-child { border-bottom:none; }
        .pv-range-time { color:#334155; font-weight:600; display:flex; align-items:center; gap:.35rem; }
        .pv-range-dur  { color:#94a3b8; font-size:.78rem; }
        .pv-range-price{ font-weight:700; color:#059669; }
        .pv-court-total { display:flex; justify-content:flex-end; font-weight:700; color:#475569; font-size:.875rem; padding-top:.5rem; border-top:1px solid #f1f5f9; margin-top:.25rem; }

        /* ── Payment / Deposit radios ── */
        .pv-radio-label {
            display:flex; align-items:center; gap:.875rem;
            border:2px solid #e2e8f0; border-radius:.625rem;
            padding:.875rem 1.25rem; cursor:pointer;
            transition:all .15s; user-select:none; margin-bottom:.625rem;
        }
        .pv-radio-label:last-child { margin-bottom:0; }
        .pv-radio-label:hover      { border-color:#10b981; background:#f0fdf4; }
        .pv-radio-label.active     { border-color:#10b981; background:#f0fdf4; }
        .pv-radio-label input[type=radio]{ accent-color:#10b981; width:1.1rem; height:1.1rem; flex-shrink:0; }
        .pv-radio-title { font-weight:700; color:#1e293b; font-size:.9rem; }
        .pv-radio-desc  { font-size:.78rem; color:#64748b; margin-top:.15rem; }
        .pv-radio-amount{ margin-left:auto; font-weight:800; color:#059669; font-size:.95rem; white-space:nowrap; }

        /* ── Totals ── */
        .pv-total-row {
            display:flex; justify-content:space-between; align-items:center;
            padding:.45rem 0; font-size:.9rem; color:#475569;
            border-bottom:1px solid #f1f5f9;
        }
        .pv-total-row:last-child { border-bottom:none; }
        .pv-total-row.grand {
            font-size:1.05rem; font-weight:800; color:#1e293b;
            padding-top:.75rem; margin-top:.25rem;
            border-top:2px solid #e2e8f0; border-bottom:none;
        }
        .pv-total-row.grand .val { color:#059669; font-size:1.35rem; }

        /* ── Confirm bar ── */
        #confirmBar {
            position:fixed; bottom:0; left:0; right:0;
            background:#fff; border-top:2px solid #e2e8f0;
            box-shadow:0 -4px 20px rgba(0,0,0,.1);
            padding:1rem 1.5rem; z-index:200;
        }
        #confirmBarInner {
            max-width:860px; margin:0 auto;
            display:flex; align-items:center; justify-content:space-between; gap:1rem;
        }
        .confirm-amount-label { font-size:.72rem; font-weight:700; text-transform:uppercase; letter-spacing:.08em; color:#64748b; }
        .confirm-amount-value { font-size:1.35rem; font-weight:900; color:#059669; }
        #confirmBtn {
            background:#10b981; color:#fff; border:none;
            padding:.875rem 2.25rem; border-radius:.625rem;
            font-size:1rem; font-weight:800; cursor:pointer;
            display:flex; align-items:center; gap:.5rem;
            transition:all .15s; white-space:nowrap;
        }
        #confirmBtn:hover:not(:disabled) { background:#059669; transform:translateY(-1px); }
        #confirmBtn:disabled { opacity:.6; cursor:not-allowed; transform:none; }

        /* ── Loading ── */
        #pvLoadingOverlay {
            position:fixed; inset:0; background:rgba(255,255,255,.85);
            display:none; align-items:center; justify-content:center; z-index:2000;
        }
        #pvLoadingOverlay.active { display:flex; }
        .spin {
            width:3rem; height:3rem; border:4px solid #e2e8f0;
            border-top-color:#10b981; border-radius:50%;
            animation:spin .8s linear infinite;
        }
        @keyframes spin { to { transform:rotate(360deg); } }

        /* ── Alert ── */
        #pvAlertBox {
            display:none; align-items:flex-start; gap:.75rem;
            padding:.875rem 1rem; border-radius:.625rem;
            margin-bottom:1rem; font-size:.9rem;
        }
        #pvAlertBox.ae { background:#fee2e2; color:#b91c1c; border:1px solid #fca5a5; }
        #pvAlertBox.aw { background:#fffbeb; color:#92400e; border:1px solid #fde68a; }
        #pvAlertBox.ai { background:#eff6ff; color:#1d4ed8; border:1px solid #bfdbfe; }

        @media(max-width:576px){
            #confirmBarInner { flex-direction:column; gap:.75rem; }
            #confirmBtn { width:100%; justify-content:center; }
        }

        /* ── Free-booking state ── */
        .pv-radio-label.disabled-option {
            opacity:.45; pointer-events:none; cursor:not-allowed;
        }
        #freeBanner {
            display:none; align-items:center; gap:.75rem;
            padding:.875rem 1rem; border-radius:.625rem;
            background:#D1FAE5; border:1px solid #6EE7B7;
            font-size:.9rem; color:#065F46; font-weight:700;
            margin-top:.75rem;
        }
        #freeBanner.show { display:flex; }
    </style>
</head>
<body>

<!-- Loading -->
<div id="pvLoadingOverlay"><div class="spin"></div></div>

<!-- ═══════════ HEADER ═══════════ -->
<header class="pv-header">
    <div class="pv-header-inner">
        <button class="btn btn-sm btn-outline-light d-flex align-items-center gap-1 fw-semibold"
                id="pvBackBtn" type="button">
            <i class="bi bi-chevron-left"></i> Chọn lại
        </button>
        <div class="pv-title ms-2">
            <i class="bi bi-clipboard2-check me-2"></i>Xác nhận đặt sân
        </div>
    </div>
</header>

<!-- ═══════════ MAIN ═══════════ -->
<div class="pv-container">

    <!-- Alert -->
    <div id="pvAlertBox" role="alert">
        <i class="bi bi-exclamation-triangle-fill flex-shrink-0"></i>
        <span id="pvAlertMsg"></span>
        <button type="button" id="pvAlertClose" class="btn-close btn-close-sm ms-auto" aria-label="Đóng"></button>
    </div>

    <!-- 1. THÔNG TIN CƠ SỞ -->
    <div class="pv-card">
        <div class="pv-section-label"><i class="bi bi-building"></i> Thông tin cơ sở</div>
        <div style="font-size:1.1rem;font-weight:800;color:#1e293b;margin-bottom:.25rem;" id="pvVenueName">Đang tải...</div>
        <div class="text-muted small d-flex align-items-center gap-1 mb-2">
            <i class="bi bi-geo-alt-fill text-success"></i>
            <span id="pvVenueAddr">—</span>
        </div>
        <div class="d-flex flex-wrap gap-2 mt-1">
            <span class="badge bg-success-subtle text-success border border-success-subtle px-3 py-2">
                <i class="bi bi-calendar3 me-1"></i><span id="pvBookingDate">—</span>
            </span>
            <span class="badge bg-primary-subtle text-primary border border-primary-subtle px-3 py-2">
                <i class="bi bi-clock me-1"></i>Tổng: <span id="pvDuration">—</span>
            </span>
        </div>
    </div>

    <!-- 2. CHI TIẾT SÂN & GIỜ (backend đã gộp range) -->
    <div class="pv-card">
        <div class="pv-section-label"><i class="bi bi-grid-3x3-gap-fill"></i> Chi tiết đặt sân</div>
        <div id="pvCourtsWrap">
            <div class="text-center py-3 text-muted">
                <div class="spin mx-auto mb-2" style="width:1.75rem;height:1.75rem;border-width:3px;"></div>
                Đang tải...
            </div>
        </div>
    </div>

    <!-- 3. HÌNH THỨC ĐẶT CỌC -->
    <div class="pv-card">
        <div class="pv-section-label"><i class="bi bi-wallet2"></i> Hình thức thanh toán</div>

        <label class="pv-radio-label active" id="labelFull">
            <input type="radio" name="pvDeposit" value="100" checked id="radioFull"/>
            <div>
                <div class="pv-radio-title">Thanh toán toàn bộ</div>
                <div class="pv-radio-desc">Trả đủ 100% – xác nhận ngay lập tức</div>
            </div>
            <span class="pv-radio-amount" id="amtFull">—</span>
        </label>

        <label class="pv-radio-label" id="labelDeposit">
            <input type="radio" name="pvDeposit" value="30" id="radioDeposit"/>
            <div>
                <div class="pv-radio-title">Đặt cọc 30%</div>
                <div class="pv-radio-desc">Trả 30% trước – thanh toán phần còn lại tại sân</div>
            </div>
            <span class="pv-radio-amount" id="amtDeposit">—</span>
        </label>

        <!-- Banner hiển thị khi đơn = 0đ nhờ voucher -->
        <div id="freeBanner">
            <i class="bi bi-gift-fill fs-5"></i>
            <span>Voucher đã giảm 100% – Đơn hàng này <strong>miễn phí!</strong> Xác nhận để đặt sân ngay.</span>
        </div>

        <!-- Thông tin VNPay QR (ẩn khi đơn = 0đ) -->
        <div class="alert alert-success d-flex align-items-center gap-2 mt-3 mb-0 small" id="vnpayNotice">
            <i class="bi bi-qr-code-scan fs-5"></i>
            <div>Thanh toán qua <strong>VNPay QR</strong> – quét mã để thanh toán nhanh chóng</div>
        </div>
    </div>

    <!-- 4. VOUCHER -->
    <div class="pv-card" id="voucherCard">
        <div class="pv-section-label"><i class="bi bi-ticket-perforated-fill"></i> Mã giảm giá</div>
        <div class="input-group">
            <input type="text" id="voucherInput" class="form-control"
                   placeholder="Nhập mã voucher..." maxlength="50"
                   style="text-transform:uppercase;letter-spacing:.05em;font-weight:600;"
                   autocomplete="off"/>
            <button class="btn btn-outline-success fw-semibold px-4" id="applyVoucherBtn" type="button">
                <i class="bi bi-check-lg me-1"></i>Áp dụng
            </button>
            <button class="btn btn-outline-secondary px-3 d-none" id="removeVoucherBtn" type="button" title="Xóa voucher">
                <i class="bi bi-x-lg"></i>
            </button>
        </div>
        <!-- Result area -->
        <div id="voucherResultOk" class="d-none mt-3 p-3 rounded-3 d-flex align-items-center gap-2"
             style="background:#D1FAE5;border:1px solid #6EE7B7;">
            <i class="bi bi-patch-check-fill text-success fs-5"></i>
            <div class="flex-fill">
                <div class="fw-bold text-success small" id="voucherOkName"></div>
                <div class="text-success small" id="voucherOkDesc"></div>
            </div>
            <div class="fw-bold text-success" id="voucherOkAmt"></div>
        </div>
        <div id="voucherResultErr" class="d-none mt-3 p-3 rounded-3 d-flex align-items-center gap-2"
             style="background:#FEE2E2;border:1px solid #FCA5A5;">
            <i class="bi bi-exclamation-circle-fill text-danger fs-5"></i>
            <div class="text-danger small fw-semibold" id="voucherErrMsg"></div>
        </div>
    </div>

    <!-- 5. TÓM TẮT -->
    <div class="pv-card">
        <div class="pv-section-label"><i class="bi bi-receipt"></i> Tóm tắt đơn hàng</div>

        <div class="pv-total-row">
            <span>Tạm tính (<span id="pvSlotCount">0</span> slot)</span>
            <span id="pvSubtotal">—</span>
        </div>
        <div class="pv-total-row text-success" id="pvVoucherRow" style="display:none;">
            <span><i class="bi bi-ticket-perforated me-1"></i>Giảm giá voucher</span>
            <span class="fw-bold" id="pvVoucherDiscount">—</span>
        </div>
        <div class="pv-total-row" id="pvDepositRow" style="display:none;">
            <span>Số tiền đặt cọc (30%)</span>
            <span class="text-success fw-bold" id="pvDepositAmt">—</span>
        </div>
        <div class="pv-total-row grand">
            <span>Tổng cộng</span>
            <span class="val" id="pvTotal">—</span>
        </div>
    </div>

    <!-- 6. ĐIỀU KHOẢN -->
    <div class="alert alert-info d-flex gap-2 small">
        <i class="bi bi-shield-check-fill flex-shrink-0 mt-1"></i>
        <div>
            Bằng cách nhấn <strong>"Xác nhận &amp; Thanh toán"</strong>, bạn đồng ý với
            <a href="#" class="fw-bold">Điều khoản sử dụng</a> và
            <a href="#" class="fw-bold">Chính sách hoàn tiền</a> của BadmintonPro.
        </div>
    </div>

</div><!-- /pv-container -->

<!-- ═══════════ CONFIRM BAR ═══════════ -->
<div id="confirmBar">
    <div id="confirmBarInner">
        <div>
            <div class="confirm-amount-label">Số tiền thanh toán</div>
            <div class="confirm-amount-value" id="pvConfirmAmt">—</div>
        </div>
        <button id="confirmBtn" type="button">
            <i class="bi bi-credit-card"></i> XÁC NHẬN &amp; THANH TOÁN
        </button>
    </div>
</div>

<!-- ═══════════ GLOBALS ═══════════ -->
<script>
    window.APP_CONTEXT_PATH = '<%= ctx %>';
    window.IS_LOGGED_IN     = true;
    window.CURRENT_USER = {
        id:   <%= account.getAccountId() %>,
        name: "<%= account.getFullName().replace("\"","\\\"").replace("\n","").replace("\r","") %>",
        role: "<%= account.getRole() %>"
    };
</script>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>

<!-- ═══════════ CONTROLLER ═══════════ -->
<script>
(function(){
    'use strict';

    const CTX = window.APP_CONTEXT_PATH || '';

    /* ── Read sessionStorage ── */
    let payload = null;
    try { payload = JSON.parse(sessionStorage.getItem('sbPreviewData') || 'null'); } catch(e) {}
    if (!payload || !payload.previewData) {
        window.location.replace(CTX + '/');
        return;
    }

    const pv         = payload.previewData;  // server response (SingleBookingPreviewResponseDTO)
    const selections = payload.selections;   // [{courtId, slotId}]
    const facilityId = pv.facilityId || payload.facilityId || payload.venueId;
    const bookingDate= pv.bookingDate || payload.bookingDate || payload.date;

    /* ── State ── */
    let depositPercent  = 100;
    let isSubmitting    = false;
    // Voucher state
    let appliedVoucher  = null;  // null | { voucherId, voucherCode, voucherName, discountAmount, finalAmount }

    /* ── Helpers ── */
    function fmtVnd(n){ return Number(n||0).toLocaleString('vi-VN')+' ₫'; }
    function esc(s){ return s==null?'':String(s).replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;').replace(/"/g,'&quot;'); }
    function fmtDate(s){
        if (!s) return '';
        const [y,m,d] = s.split('-');
        return d+'/'+m+'/'+y;
    }
    function fmtDuration(mins){
        if (!mins || mins <= 0) return '—';
        const h = Math.floor(mins / 60);
        const m = mins % 60;
        if (h === 0) return m + ' phút';
        if (m === 0) return h + ' giờ';
        return h + ' giờ ' + m + ' phút';
    }

    /* ── DOM ── */
    const loadingEl    = document.getElementById('pvLoadingOverlay');
    const alertBox     = document.getElementById('pvAlertBox');
    const alertMsg     = document.getElementById('pvAlertMsg');
    const alertClose   = document.getElementById('pvAlertClose');
    const backBtn      = document.getElementById('pvBackBtn');
    const venueName    = document.getElementById('pvVenueName');
    const venueAddr    = document.getElementById('pvVenueAddr');
    const pvDate       = document.getElementById('pvBookingDate');
    const pvDuration   = document.getElementById('pvDuration');
    const courtsWrap   = document.getElementById('pvCourtsWrap');
    const pvSlotCount  = document.getElementById('pvSlotCount');
    const pvSubtotal   = document.getElementById('pvSubtotal');
    const pvDepRow     = document.getElementById('pvDepositRow');
    const pvDepAmt     = document.getElementById('pvDepositAmt');
    const pvTotal      = document.getElementById('pvTotal');
    const pvConfirmAmt = document.getElementById('pvConfirmAmt');
    const amtFull      = document.getElementById('amtFull');
    const amtDeposit   = document.getElementById('amtDeposit');
    const confirmBtn   = document.getElementById('confirmBtn');

    /* ── Alert ── */
    function showAlert(msg, type){
        alertBox.className = 'ae';
        if (type==='warning') alertBox.className = 'aw';
        if (type==='info')    alertBox.className = 'ai';
        alertBox.style.display='flex';
        alertMsg.textContent = msg;
        alertBox.scrollIntoView({behavior:'smooth',block:'nearest'});
    }
    function hideAlert(){ alertBox.style.display='none'; }
    alertClose.addEventListener('click', hideAlert);

    function showLoad(){ loadingEl.classList.add('active'); }
    function hideLoad(){ loadingEl.classList.remove('active'); }

    /* ── Back button ── */
    backBtn.addEventListener('click', function(){ window.history.back(); });

    /* ── Render venue info (from backend response) ── */
    venueName.textContent = pv.facilityName   || '—';
    venueAddr.textContent = pv.facilityAddress|| '—';
    pvDate.textContent    = fmtDate(bookingDate);
    pvDuration.textContent= fmtDuration(pv.totalMinutes);

    /* ── Render court details (backend rangesByCourt) ── */
    function renderCourts(){
        const ranges = pv.rangesByCourt || [];
        if (ranges.length === 0){
            courtsWrap.innerHTML = '<p class="text-muted text-center py-3">Không có thông tin sân.</p>';
            return;
        }

        // Group by courtId
        const courtMap = {};
        ranges.forEach(function(r){
            if (!courtMap[r.courtId]) {
                courtMap[r.courtId] = { courtName: r.courtName, ranges: [], total: 0 };
            }
            courtMap[r.courtId].ranges.push(r);
            courtMap[r.courtId].total += Number(r.subtotal || 0);
        });

        let html = '';
        Object.values(courtMap).forEach(function(court){
            html += '<div class="pv-court-card">';
            html += '<div class="pv-court-name"><i class="bi bi-grid-3x3-gap-fill text-success me-2"></i>'+esc(court.courtName||'Sân')+'</div>';
            court.ranges.forEach(function(r){
                const dur = r.minutes ? (r.minutes >= 60 ? (r.minutes/60) + ' giờ' : r.minutes + ' phút') : '';
                html += '<div class="pv-range-row">';
                html += '<span class="pv-range-time"><i class="bi bi-clock text-success me-1"></i>'+esc(r.startTime)+' – '+esc(r.endTime)+'</span>';
                html += '<span class="pv-range-dur">'+esc(dur)+'</span>';
                html += '<span class="pv-range-price">'+fmtVnd(r.subtotal)+'</span>';
                html += '</div>';
            });
            html += '<div class="pv-court-total">Tổng sân: <strong class="ms-2">'+fmtVnd(court.total)+'</strong></div>';
            html += '</div>';
        });
        courtsWrap.innerHTML = html;
    }
    renderCourts();

    /* ── Financial ── */
    const totalAmount = Number(pv.estimatedTotal || 0);

    function updateFinancials(){
        const discount   = appliedVoucher ? Number(appliedVoucher.discountAmount || 0) : 0;
        const afterDisc  = Math.max(0, totalAmount - discount);
        const isFree     = afterDisc === 0;

        // Force full-payment when order is free; restore previous choice otherwise
        if (isFree && depositPercent !== 100) {
            depositPercent = 100;
            const radioFull = document.getElementById('radioFull');
            if (radioFull) radioFull.checked = true;
            document.querySelectorAll('.pv-radio-label').forEach(function(l){ l.classList.remove('active'); });
            const lblFull = document.getElementById('labelFull');
            if (lblFull) lblFull.classList.add('active');
        }

        const payAmount  = depositPercent === 100 ? afterDisc : Math.ceil(afterDisc * depositPercent / 100);

        pvSlotCount.textContent = pv.totalSlots || 0;
        pvSubtotal.textContent  = fmtVnd(totalAmount);

        // Voucher discount row
        const voucherRow    = document.getElementById('pvVoucherRow');
        const voucherDiscEl = document.getElementById('pvVoucherDiscount');
        if (discount > 0) {
            voucherRow.style.display = '';
            voucherDiscEl.textContent = '- ' + fmtVnd(discount);
        } else {
            voucherRow.style.display = 'none';
        }

        pvTotal.textContent = isFree ? 'Miễn phí' : fmtVnd(afterDisc);

        // deposit row
        if (depositPercent < 100 && !isFree){
            pvDepRow.style.display = '';
            pvDepAmt.textContent   = fmtVnd(payAmount);
        } else {
            pvDepRow.style.display = 'none';
        }

        // radio amount labels
        if (amtFull)    amtFull.textContent    = isFree ? 'Miễn phí' : fmtVnd(afterDisc);
        if (amtDeposit) amtDeposit.textContent = isFree ? '—' : fmtVnd(Math.ceil(afterDisc * 0.3));

        // Lock / unlock deposit option
        const labelDeposit = document.getElementById('labelDeposit');
        const radioDeposit = document.getElementById('radioDeposit');
        if (labelDeposit && radioDeposit) {
            if (isFree) {
                labelDeposit.classList.add('disabled-option');
                radioDeposit.disabled = true;
            } else {
                labelDeposit.classList.remove('disabled-option');
                radioDeposit.disabled = false;
            }
        }

        // Free banner & VNPay notice
        const freeBanner  = document.getElementById('freeBanner');
        const vnpayNotice = document.getElementById('vnpayNotice');
        if (freeBanner)  freeBanner.classList.toggle('show', isFree);
        if (vnpayNotice) vnpayNotice.style.display = isFree ? 'none' : '';

        // Confirm bar amount
        pvConfirmAmt.textContent = isFree ? 'Miễn phí 🎉' : fmtVnd(payAmount);

        // Confirm button label
        if (confirmBtn) {
            confirmBtn.innerHTML = isFree
                ? '<i class="bi bi-check-circle"></i> XÁC NHẬN ĐẶT SÂN'
                : '<i class="bi bi-credit-card"></i> XÁC NHẬN &amp; THANH TOÁN';
        }
    }
    updateFinancials();

    /* ── Voucher logic ── */
    const voucherInput     = document.getElementById('voucherInput');
    const applyVoucherBtn  = document.getElementById('applyVoucherBtn');
    const removeVoucherBtn = document.getElementById('removeVoucherBtn');
    const voucherResultOk  = document.getElementById('voucherResultOk');
    const voucherResultErr = document.getElementById('voucherResultErr');
    const voucherOkName    = document.getElementById('voucherOkName');
    const voucherOkDesc    = document.getElementById('voucherOkDesc');
    const voucherOkAmt     = document.getElementById('voucherOkAmt');
    const voucherErrMsg    = document.getElementById('voucherErrMsg');

    function setVoucherApplied(data) {
        appliedVoucher = data;
        voucherInput.value     = data.voucherCode;
        voucherInput.readOnly  = true;
        applyVoucherBtn.classList.add('d-none');
        removeVoucherBtn.classList.remove('d-none');

        // Show success panel
        voucherResultOk.classList.remove('d-none');
        voucherResultErr.classList.add('d-none');
        voucherOkName.textContent = data.voucherName || data.voucherCode;
        voucherOkDesc.textContent = data.discountType === 'PERCENTAGE'
            ? 'Giảm ' + data.discountValue + '%' + (data.discountType === 'PERCENTAGE' ? '' : '')
            : 'Giảm ' + fmtVnd(data.discountValue);
        voucherOkAmt.textContent = '- ' + fmtVnd(data.discountAmount);

        updateFinancials();
    }

    function clearVoucher() {
        appliedVoucher             = null;
        voucherInput.value         = '';
        voucherInput.readOnly      = false;
        applyVoucherBtn.classList.remove('d-none');
        removeVoucherBtn.classList.add('d-none');
        voucherResultOk.classList.add('d-none');
        voucherResultErr.classList.add('d-none');
        updateFinancials();
    }

    function showVoucherError(msg) {
        voucherResultErr.classList.remove('d-none');
        voucherResultOk.classList.add('d-none');
        voucherErrMsg.textContent = msg;
    }

    applyVoucherBtn.addEventListener('click', function() {
        const code = voucherInput.value.trim().toUpperCase();
        if (!code) { showVoucherError('Vui lòng nhập mã voucher.'); return; }

        applyVoucherBtn.disabled = true;
        applyVoucherBtn.innerHTML = '<span class="spinner-border spinner-border-sm me-1"></span>Đang kiểm tra...';
        voucherResultErr.classList.add('d-none');
        voucherResultOk.classList.add('d-none');

        fetch(CTX + '/api/single-booking/apply-voucher', {
            method:  'POST',
            headers: { 'Content-Type': 'application/json', 'Accept': 'application/json' },
            body:    JSON.stringify({
                voucherCode: code,
                facilityId:  facilityId,
                totalAmount: totalAmount
            })
        })
        .then(function(res) { return res.json().then(function(j){ j._status = res.status; return j; }); })
        .then(function(json) {
            applyVoucherBtn.disabled = false;
            applyVoucherBtn.innerHTML = '<i class="bi bi-check-lg me-1"></i>Áp dụng';

            if (json._status === 401) {
                showAlert('Phiên đăng nhập hết hạn. Vui lòng đăng nhập lại.', 'warning');
                return;
            }
            const ok = json.success !== undefined ? json.success : (json._status >= 200 && json._status < 300);
            if (!ok) {
                const errMsg = (json.error && json.error.message) || json.message || 'Mã voucher không hợp lệ.';
                showVoucherError(errMsg);
                return;
            }
            const data = json.data || json;
            setVoucherApplied(data);
        })
        .catch(function() {
            applyVoucherBtn.disabled = false;
            applyVoucherBtn.innerHTML = '<i class="bi bi-check-lg me-1"></i>Áp dụng';
            showVoucherError('Lỗi kết nối. Vui lòng thử lại.');
        });
    });

    // Allow applying by pressing Enter in the input
    voucherInput.addEventListener('keydown', function(e) {
        if (e.key === 'Enter' && !voucherInput.readOnly) applyVoucherBtn.click();
    });

    removeVoucherBtn.addEventListener('click', clearVoucher);
    document.querySelectorAll('input[name="pvDeposit"]').forEach(function(r){
        r.addEventListener('change', function(){
            depositPercent = parseInt(this.value, 10);
            document.querySelectorAll('.pv-radio-label').forEach(function(l){ l.classList.remove('active'); });
            const lbl = this.closest('label');
            if (lbl) lbl.classList.add('active');
            updateFinancials();
        });
    });

    /* ── CONFIRM & PAY ── */
    confirmBtn.addEventListener('click', function(){
        if (isSubmitting) return;
        doConfirm();
    });

    function doConfirm(){
        isSubmitting = true;
        setBtnLoading(true);
        hideAlert();
        showLoad();

        // Body includes voucherCode if a voucher was applied
        const body = {
            facilityId:     facilityId,
            bookingDate:    bookingDate,
            depositPercent: depositPercent,
            selections:     selections,
            voucherCode:    appliedVoucher ? appliedVoucher.voucherCode : null
        };

        fetch(CTX+'/api/single-booking/confirm-and-pay', {
            method:'POST',
            headers:{'Content-Type':'application/json','Accept':'application/json'},
            body: JSON.stringify(body)
        })
        .then(function(res){
            hideLoad();
            if (res.status===401) throw {_status:401};
            return res.json().then(function(j){ j._httpStatus = res.status; return j; });
        })
        .then(function(json){
            const success = json.success !== undefined ? json.success
                          : (json._httpStatus >= 200 && json._httpStatus < 300);

            if (!success){
                isSubmitting = false;
                setBtnLoading(false);
                const msg = (json.error&&json.error.message)
                    || json.errorMessage
                    || 'Không thể xác nhận. Vui lòng thử lại.';

                /* 409 slot conflict → redirect matrix */
                if (json._httpStatus === 409){
                    showAlert('Slot đã được người khác đặt. Đang chuyển về trang chọn sân...','error');
                    sessionStorage.removeItem('sbPreviewData');
                    setTimeout(function(){
                        window.location.href = CTX
                            +'/jsp/booking/singlebooking/single-booking.jsp'
                            +'?facilityId='+encodeURIComponent(facilityId)
                            +'&date='+encodeURIComponent(bookingDate);
                    }, 2500);
                    return;
                }
                showAlert(msg, json._httpStatus===400?'warning':'error');
                return;
            }

            /* SUCCESS */
            sessionStorage.removeItem('sbPreviewData');
            const data   = json.data || json;
            const payUrl = data.paymentUrl;
            if (payUrl) {
                // Normal path – redirect to VNPay payment gateway
                window.location.href = payUrl;
            } else {
                // Free order (voucher covered 100%) – booking already confirmed, skip payment
                window.location.href = CTX + '/?bookingSuccess=1&bookingId=' + (data.bookingId || '');
            }
        })
        .catch(function(err){
            hideLoad();
            isSubmitting = false;
            setBtnLoading(false);
            if (err && err._status===401){
                showAlert('Phiên đăng nhập đã hết hạn. Đang chuyển về trang đăng nhập...','warning');
                setTimeout(function(){ window.location.href = CTX+'/auth/login'; }, 1500);
                return;
            }
            const msg = (err&&err.error&&err.error.message)||err.message||'Lỗi kết nối. Vui lòng thử lại.';
            showAlert(msg, 'error');
        });
    }

    function setBtnLoading(on){
        const discount  = appliedVoucher ? Number(appliedVoucher.discountAmount || 0) : 0;
        const afterDisc = Math.max(0, totalAmount - discount);
        const isFree    = afterDisc === 0;
        confirmBtn.disabled = on;
        if (on) {
            confirmBtn.innerHTML = '<span class="spinner-border spinner-border-sm me-2"></span>Đang xử lý...';
        } else {
            confirmBtn.innerHTML = isFree
                ? '<i class="bi bi-check-circle"></i> XÁC NHẬN ĐẶT SÂN'
                : '<i class="bi bi-credit-card"></i> XÁC NHẬN &amp; THANH TOÁN';
        }
    }

})();
</script>

</body>
</html>
