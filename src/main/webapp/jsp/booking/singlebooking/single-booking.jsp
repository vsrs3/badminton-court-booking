<%-- ============================================================
     single-booking.jsp  –  Ma trận chọn sân × khung giờ
     URL: /jsp/booking/singlebooking/single-booking.jsp?venueId=1&date=YYYY-MM-DD
     Author: AnhTN
     ============================================================ --%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.bcb.model.Account" %>
<%
    Account account    = (Account) session.getAttribute("account");
    boolean isLoggedIn = account != null;
    String ctx         = request.getContextPath();

    // Hỗ trợ cả facilityId lẫn venueId (alias)
    String venueId = request.getParameter("facilityId");
    if (venueId == null || venueId.isEmpty()) {
        venueId = request.getParameter("venueId");
    }
    String dateParam = request.getParameter("date");
    if (dateParam == null || dateParam.isEmpty()) {
        dateParam = java.time.LocalDate.now().toString();
    }
    if (venueId == null || venueId.isEmpty()) {
        response.sendRedirect(ctx + "/");
        return;
    }
%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
    <title>Đặt lịch lẻ – BadmintonPro</title>

    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet"/>
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css"/>
    <link rel="stylesheet" href="<%= ctx %>/assets/css/badminton-pro.css"/>
    <link rel="stylesheet" href="<%= ctx %>/assets/css/booking/booking.css"/>

    <style>
        body { background: #f8fafc; }

        /* Matrix table */
        .table-container { max-height: 62vh; overflow: auto; position: relative; }
        .matrix-table { border-collapse: separate; border-spacing: 0; width: max-content; min-width: 100%; }
        .matrix-table .sticky-left {
            position: sticky; left: 0; background: #fff; z-index: 20;
            box-shadow: 2px 0 6px -2px rgba(0,0,0,.08); min-width: 124px;
        }
        .matrix-table thead { position: sticky; top: 0; z-index: 30; }
        .matrix-table thead .sticky-left { z-index: 40; background: #f8fafc; }
        .matrix-table th {
            background: #f8fafc; font-weight: 700; font-size: .78rem;
            color: #64748b; padding: .55rem .4rem; text-align: center;
            border-bottom: 2px solid #e2e8f0; white-space: nowrap;
        }
        .matrix-table td { border: 1px solid #e2e8f0; padding: 0; height: 46px; vertical-align: middle; }
        .matrix-table tbody tr:hover .sticky-left { background: #f8fafc; }

        /* Slot cells */
        .slot-cell { width: 46px; height: 46px; cursor: pointer; transition: all .12s; position: relative; }
        .slot-cell.slot-available { background: #fff; }
        .slot-cell.slot-available:hover {
            background: #f0fdf4; outline: 2px solid #10b981; outline-offset: -2px; z-index: 5;
        }
        .slot-cell.slot-selected {
            background: #10b981 !important; outline: 2px solid #059669; outline-offset: -2px; z-index: 5;
        }
        .slot-cell.slot-booked  { background: #fee2e2; cursor: not-allowed; }
        .slot-cell.slot-past    { background: #f1f5f9; cursor: not-allowed; }

        /* Price tooltip */
        .slot-cell.slot-available::after, .slot-cell.slot-selected::after {
            content: attr(data-price-tip);
            position: absolute; bottom: calc(100% + 4px); left: 50%;
            transform: translateX(-50%);
            background: #1e293b; color: #fff; font-size: .68rem;
            padding: .2rem .45rem; border-radius: .3rem;
            white-space: nowrap; pointer-events: none; opacity: 0;
            transition: opacity .15s; z-index: 99;
        }
        .slot-cell.slot-available:hover::after,
        .slot-cell.slot-selected:hover::after { opacity: 1; }

        /* Summary footer */
        #summaryBar {
            position: fixed; bottom: 0; left: 0; right: 0;
            background: #fff; border-top: 2px solid #e2e8f0;
            box-shadow: 0 -4px 20px rgba(0,0,0,.1); z-index: 200;
            padding: .875rem 1.5rem;
            transform: translateY(100%);
            transition: transform .3s cubic-bezier(.34,1.56,.64,1);
        }
        #summaryBar.visible { transform: translateY(0); }

        /* Loading */
        #loadingOverlay {
            position: fixed; inset: 0; background: rgba(255,255,255,.85);
            display: none; align-items: center; justify-content: center; z-index: 2000;
        }
        #loadingOverlay.active { display: flex; }
        .spin {
            width: 3rem; height: 3rem; border: 4px solid #e2e8f0;
            border-top-color: #10b981; border-radius: 50%;
            animation: spin .8s linear infinite;
        }
        @keyframes spin { to { transform: rotate(360deg); } }

        /* Calendar popup */
        #calendarPopup {
            position: absolute; top: calc(100% + .5rem); right: 0;
            width: 18rem; background: #fff;
            border: 1px solid #cbd5e1; border-radius: .75rem;
            box-shadow: 0 10px 25px -5px rgba(0,0,0,.12);
            padding: 1rem; z-index: 9999;
        }
        .cal-day {
            width: 2rem; height: 2rem; display: flex; align-items: center;
            justify-content: center; font-size: .83rem; border-radius: .4rem;
            cursor: pointer; user-select: none; transition: background .1s;
        }
        .cal-day:hover:not(.past-day):not(.empty-day) { background: #f1f5f9; }
        .cal-day.selected-day { background: #10b981 !important; color: #fff; font-weight: 700; }
        .cal-day.today-day    { border: 2px solid #6ee7b7; color: #059669; font-weight: 700; }
        .cal-day.past-day     { opacity: .28; cursor: not-allowed; }
        .cal-day.empty-day    { pointer-events: none; }

        /* Alert */
        #alertBox {
            display: none; align-items: flex-start; gap: .75rem;
            padding: .875rem 1rem; border-radius: .625rem;
            margin-bottom: 1rem; font-size: .9rem;
        }
        #alertBox.ae { background:#fee2e2; color:#b91c1c; border:1px solid #fca5a5; }
        #alertBox.aw { background:#fffbeb; color:#92400e; border:1px solid #fde68a; }
        #alertBox.ai { background:#eff6ff; color:#1d4ed8; border:1px solid #bfdbfe; }

        main { padding-bottom: 5.5rem; }
    </style>
</head>
<body>

<div id="loadingOverlay"><div class="spin"></div></div>

<!-- ═══════════ HEADER ═══════════ -->
<header class="bg-white border-bottom shadow-sm sticky-top" style="z-index:100;">
    <div class="container-fluid px-3 px-md-4 py-3">
        <div class="d-flex flex-column flex-md-row justify-content-between align-items-md-center gap-2">

            <div class="d-flex align-items-center gap-3">
                <a href="<%= ctx %>/" class="btn btn-sm btn-outline-secondary d-flex align-items-center gap-1">
                    <i class="bi bi-chevron-left"></i> Quay lại
                </a>
                <div>
                    <h5 class="mb-0 fw-bold text-dark d-flex align-items-center gap-2">
                        <i class="bi bi-calendar3-event text-success"></i>
                        Đặt lịch lẻ
                        <span class="text-success fw-normal" id="sbFacilityName"></span>
                    </h5>
                    <small class="text-muted">Chọn sân và khung giờ phù hợp</small>
                </div>
            </div>

            <div class="position-relative" id="datePickerWrapper">
                <button class="btn btn-outline-success d-flex align-items-center gap-2 fw-semibold"
                        id="dateBtn" type="button">
                    <i class="bi bi-calendar3"></i>
                    <span id="selectedDateText">Đang tải...</span>
                    <i class="bi bi-chevron-down"></i>
                </button>

                <div id="calendarPopup" class="d-none">
                    <div class="d-flex justify-content-between align-items-center mb-3">
                        <button class="btn btn-sm btn-link p-1 text-dark" id="calPrev"><i class="bi bi-chevron-left"></i></button>
                        <span class="fw-bold small" id="calMonthYear"></span>
                        <button class="btn btn-sm btn-link p-1 text-dark" id="calNext"><i class="bi bi-chevron-right"></i></button>
                    </div>
                    <div class="d-flex justify-content-between mb-1">
                        <% for (String wd : new String[]{"CN","T2","T3","T4","T5","T6","T7"}) { %>
                        <div class="cal-day text-muted fw-bold" style="cursor:default;font-size:.72rem;"><%= wd %></div>
                        <% } %>
                    </div>
                    <div id="calDays" class="d-flex flex-wrap"></div>
                </div>
            </div>
        </div>

        <!-- Legend -->
        <div class="d-flex flex-wrap gap-3 mt-2 small text-muted">
            <span class="d-flex align-items-center gap-1">
                <span style="width:16px;height:16px;border-radius:4px;background:#fff;border:2px solid #cbd5e1;display:inline-block;"></span> Có thể đặt
            </span>
            <span class="d-flex align-items-center gap-1">
                <span style="width:16px;height:16px;border-radius:4px;background:#10b981;display:inline-block;"></span> Đang chọn
            </span>
            <span class="d-flex align-items-center gap-1">
                <span style="width:16px;height:16px;border-radius:4px;background:#fee2e2;border:1px solid #fca5a5;display:inline-block;"></span> Đã đặt
            </span>
            <span class="d-flex align-items-center gap-1">
                <span style="width:16px;height:16px;border-radius:4px;background:#f1f5f9;display:inline-block;"></span> Quá giờ
            </span>
        </div>
    </div>
</header>

<!-- ═══════════ MAIN ═══════════ -->
<main class="container-fluid px-3 px-md-4 py-3">

    <div id="alertBox" role="alert">
        <i class="bi bi-exclamation-triangle-fill flex-shrink-0"></i>
        <span id="alertMsg"></span>
        <button type="button" id="alertClose" class="btn-close btn-close-sm ms-auto" aria-label="Đóng"></button>
    </div>

    <div class="card shadow-sm">
        <div class="table-container">
            <table class="matrix-table table table-bordered mb-0 text-center">
                <thead id="matrixHead">
                    <tr><th class="sticky-left text-start ps-3">Sân / Giờ</th></tr>
                </thead>
                <tbody id="matrixBody">
                    <tr>
                        <td class="text-center py-5 text-muted" colspan="100">
                            <div class="spin mx-auto mb-2" style="width:2rem;height:2rem;border-width:3px;"></div>
                            Đang tải dữ liệu sân...
                        </td>
                    </tr>
                </tbody>
            </table>
        </div>
    </div>

    <div class="alert alert-info d-flex gap-3 mt-3">
        <i class="bi bi-info-circle-fill fs-5 flex-shrink-0 mt-1"></i>
        <div class="small">
            <strong>Hướng dẫn:</strong>
            <ul class="mb-0 mt-1">
                <li>Chọn ngày → click vào ô trống để chọn khung giờ.</li>
                <li>Mỗi ô = <strong>30 phút</strong>. Có thể chọn nhiều ô, nhiều sân.</li>
                <li><strong>Mỗi sân phải đặt liên tiếp ít nhất 1 giờ (2 ô).</strong></li>
                <li>Di chuột vào ô để xem giá.</li>
            </ul>
        </div>
    </div>
</main>

<!-- ═══════════ SUMMARY FOOTER ═══════════ -->
<div id="summaryBar" aria-live="polite">
    <div class="container-fluid px-3 px-md-4">
        <div class="d-flex justify-content-between align-items-center flex-wrap gap-3">
            <div class="d-flex align-items-center gap-4 flex-wrap">

                <div class="d-flex align-items-center gap-2 text-muted">
                    <div class="rounded-circle bg-light d-flex align-items-center justify-content-center"
                         style="width:2.25rem;height:2.25rem;flex-shrink:0;">
                        <i class="bi bi-grid-1x2"></i>
                    </div>
                    <div>
                        <div style="font-size:.68rem;font-weight:700;text-transform:uppercase;letter-spacing:.08em;">Số slot</div>
                        <div class="fw-bold text-dark" id="statSlots">0</div>
                    </div>
                </div>

                <div class="vr d-none d-sm-block"></div>

                <div class="d-flex align-items-center gap-2 text-muted">
                    <div class="rounded-circle bg-light d-flex align-items-center justify-content-center"
                         style="width:2.25rem;height:2.25rem;flex-shrink:0;">
                        <i class="bi bi-clock"></i>
                    </div>
                    <div>
                        <div style="font-size:.68rem;font-weight:700;text-transform:uppercase;letter-spacing:.08em;">Thời lượng</div>
                        <div class="fw-bold text-dark" id="statDuration">0 phút</div>
                    </div>
                </div>

                <div class="vr d-none d-sm-block"></div>

                <div class="d-flex align-items-center gap-2">
                    <div class="rounded-circle d-flex align-items-center justify-content-center"
                         style="width:2.25rem;height:2.25rem;background:#d1fae5;flex-shrink:0;">
                        <i class="bi bi-cash-coin text-success"></i>
                    </div>
                    <div>
                        <div style="font-size:.68rem;font-weight:700;text-transform:uppercase;letter-spacing:.08em;color:#64748b;">Dự kiến</div>
                        <div class="fw-bold fs-5 text-success" id="statPrice">0 ₫</div>
                    </div>
                </div>
            </div>

            <button id="continueBtn" type="button"
                    class="btn btn-success btn-lg px-4 d-flex align-items-center gap-2 fw-bold">
                TIẾP TỤC <i class="bi bi-chevron-right"></i>
            </button>
        </div>
    </div>
</div>

<!-- ═══════════ JS GLOBALS ═══════════ -->
<script>
    window.APP_CONTEXT_PATH = '<%= ctx %>';
    window.IS_LOGGED_IN     = <%= isLoggedIn %>;
    window.SB_VENUE_ID      = '<%= venueId %>';
    window.SB_INITIAL_DATE  = '<%= dateParam %>';
    <% if (isLoggedIn) { %>
    window.CURRENT_USER = {
        id:   <%= account.getAccountId() %>,
        name: "<%= account.getFullName().replace("\"","\\\"").replace("\n","").replace("\r","") %>",
        role: "<%= account.getRole() %>"
    };
    <% } else { %>
    window.CURRENT_USER = null;
    <% } %>
</script>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>

<!-- ═══════════ BOOKING CONTROLLER ═══════════ -->
<script>
(function () {
    'use strict';

    const CTX          = window.APP_CONTEXT_PATH || '';
    const VENUE_ID     = window.SB_VENUE_ID      || '';
    const IS_LOGGED_IN = window.IS_LOGGED_IN === true;

    /* state */
    let matrixData    = null;
    let selectedSlots = [];
    let selectedDate  = window.SB_INITIAL_DATE || todayStr();
    let viewYear, viewMonth;
    (function () {
        const d = new Date(selectedDate + 'T00:00:00');
        viewYear = d.getFullYear(); viewMonth = d.getMonth();
    })();
    let isSubmitting = false;

    /* DOM */
    const loadingOverlay  = document.getElementById('loadingOverlay');
    const alertBox        = document.getElementById('alertBox');
    const alertMsg        = document.getElementById('alertMsg');
    const alertClose      = document.getElementById('alertClose');
    const matrixHead      = document.getElementById('matrixHead');
    const matrixBody      = document.getElementById('matrixBody');
    const summaryBar      = document.getElementById('summaryBar');
    const statSlots       = document.getElementById('statSlots');
    const statDuration    = document.getElementById('statDuration');
    const statPrice       = document.getElementById('statPrice');
    const continueBtn     = document.getElementById('continueBtn');
    const dateBtn         = document.getElementById('dateBtn');
    const selDateText     = document.getElementById('selectedDateText');
    const calendarPopup   = document.getElementById('calendarPopup');
    const calPrev         = document.getElementById('calPrev');
    const calNext         = document.getElementById('calNext');
    const calMonthYear    = document.getElementById('calMonthYear');
    const calDaysEl       = document.getElementById('calDays');
    const facilityNameEl  = document.getElementById('sbFacilityName');

    /* ── helpers ── */
    function todayStr() {
        const d = new Date();
        return d.getFullYear() + '-' + pad(d.getMonth()+1) + '-' + pad(d.getDate());
    }
    function pad(n)    { return String(n).padStart(2,'0'); }
    function fmtVnd(n) { return Number(n||0).toLocaleString('vi-VN') + ' ₫'; }
    function esc(s)    {
        return s == null ? '' : String(s)
            .replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;').replace(/"/g,'&quot;');
    }
    function fmtDur(m) {
        const h = Math.floor(m/60), r = m%60;
        if (h===0) return r+' phút';
        if (r===0) return h+' giờ';
        return h+' giờ '+r+' phút';
    }
    function fmtDateDisplay(s) {
        const d = new Date(s+'T00:00:00');
        return d.toLocaleDateString('vi-VN',{weekday:'short',day:'2-digit',month:'2-digit',year:'numeric'});
    }
    function slotEnd(start) {
        const [h,m] = start.split(':').map(Number);
        const t = h*60+m+30;
        return pad(Math.floor(t/60)%24)+':'+pad(t%60);
    }

    /* ── alert ── */
    function showAlert(msg, type) {
        if (!alertBox) return;
        alertBox.className = (type==='warning'?'aw':type==='info'?'ai':'ae');
        alertBox.style.display = 'flex';
        alertMsg.textContent = msg;
        alertBox.scrollIntoView({behavior:'smooth',block:'nearest'});
    }
    function hideAlert() { if (alertBox) alertBox.style.display = 'none'; }
    if (alertClose) alertClose.addEventListener('click', hideAlert);

    /* ── loading ── */
    function showLoad() { loadingOverlay.classList.add('active'); }
    function hideLoad() { loadingOverlay.classList.remove('active'); }

    /* ── calendar ── */
    function renderCal() {
        calMonthYear.textContent = 'Tháng '+(viewMonth+1)+' / '+viewYear;
        const firstDow    = new Date(viewYear,viewMonth,1).getDay();
        const daysInMonth = new Date(viewYear,viewMonth+1,0).getDate();
        const today       = new Date(); today.setHours(0,0,0,0);

        calDaysEl.innerHTML = '';
        for (let i=0; i<firstDow; i++) {
            const b = document.createElement('div');
            b.className = 'cal-day empty-day';
            calDaysEl.appendChild(b);
        }
        for (let d=1; d<=daysInMonth; d++) {
            const cellDate = new Date(viewYear,viewMonth,d);
            const cellStr  = viewYear+'-'+pad(viewMonth+1)+'-'+pad(d);
            const el = document.createElement('div');
            el.textContent = d;
            el.className   = 'cal-day';
            if (cellDate < today) {
                el.classList.add('past-day');
            } else {
                if (cellStr === todayStr())    el.classList.add('today-day');
                if (cellStr === selectedDate)  el.classList.add('selected-day');
                el.addEventListener('click', function () {
                    selectedDate = cellStr;
                    selDateText.textContent = fmtDateDisplay(selectedDate);
                    calendarPopup.classList.add('d-none');
                    const url = new URL(window.location.href);
                    url.searchParams.set('date', selectedDate);
                    window.history.replaceState({}, '', url.toString());
                    selectedSlots = [];
                    updateSummary();
                    loadMatrix();
                });
            }
            calDaysEl.appendChild(el);
        }
    }

    dateBtn.addEventListener('click', function (e) {
        e.stopPropagation(); renderCal();
        calendarPopup.classList.toggle('d-none');
    });
    document.addEventListener('click', function (e) {
        const wrapper = document.getElementById('datePickerWrapper');
        if (wrapper && !wrapper.contains(e.target)) calendarPopup.classList.add('d-none');
    });
    calPrev.addEventListener('click', function () {
        if (--viewMonth < 0) { viewMonth=11; viewYear--; } renderCal();
    });
    calNext.addEventListener('click', function () {
        if (++viewMonth > 11) { viewMonth=0; viewYear++; } renderCal();
    });

    /* ── load matrix ── */
    function loadMatrix() {
        if (!VENUE_ID) { showAlert('Thiếu thông tin cơ sở (venueId).','error'); return; }
        showLoad(); hideAlert();

        fetch(CTX+'/api/single-booking/matrix-data'
            +'?facilityId='+encodeURIComponent(VENUE_ID)
            +'&date='+encodeURIComponent(selectedDate), { headers:{'Accept':'application/json'} })
        .then(function(res) {
            if (res.status===401) throw {_status:401};
            return res.json().then(function(j){ if(!res.ok) throw j; return j; });
        })
        .then(function(json) {
            hideLoad();
            matrixData = (json.success!==undefined && json.data) ? json.data : json;
            if (facilityNameEl && matrixData.facility && matrixData.facility.name)
                facilityNameEl.textContent = '– ' + matrixData.facility.name;
            renderMatrix();
        })
        .catch(function(err) {
            hideLoad();
            if (err && err._status===401) { handleUnauth(); return; }
            showAlert((err&&err.error&&err.error.message)||'Không thể tải dữ liệu. Vui lòng thử lại.','error');
            matrixBody.innerHTML = '<tr><td class="text-center py-5 text-muted" colspan="100">Không có dữ liệu.</td></tr>';
        });
    }

    /* ── render matrix ── */
    function renderMatrix() {
        const { courts, slots, booked, disabled, prices, facility } = matrixData || {};

        /*
         * Backend trả về:
         *  courts[]  = [{courtId, courtName, courtTypeId}]
         *  slots[]   = [{slotId, startTime, endTime}]
         *  booked{}  = { courtId -> [slotId, ...] }
         *  disabled{}= { courtId -> [slotId, ...] }
         *  prices[]  = [{courtId, slotId, price}]
         *
         * Frontend dùng index trong slots[] làm slotIndex để gửi lên preview/confirm.
         */

        // Build lookup: "courtId_slotId" -> price
        const priceMap = {};
        (prices || []).forEach(function(p) {
            if (p.price != null) priceMap[p.courtId + '_' + p.slotId] = p.price;
        });

        // Build Set: "courtId_slotId" -> status
        const bookedSet   = {};
        const disabledSet = {};
        Object.entries(booked   || {}).forEach(function([cid, sids]) { sids.forEach(function(sid){ bookedSet[cid+'_'+sid]   = true; }); });
        Object.entries(disabled || {}).forEach(function([cid, sids]) { sids.forEach(function(sid){ disabledSet[cid+'_'+sid] = true; }); });

        /* thead: first col sticky, then one col per slot (show startTime) */
        let h = '<tr><th class="sticky-left text-start ps-3" style="min-width:124px;">Sân / Giờ</th>';
        (slots || []).forEach(function(s){ h += '<th style="min-width:46px;font-size:.75rem;">'+esc(s.startTime)+'</th>'; });
        h += '</tr>';
        matrixHead.innerHTML = h;

        /* tbody: one row per court */
        let b = '';
        (courts || []).forEach(function(court) {
            b += '<tr>';
            b += '<td class="sticky-left text-start ps-3 pe-2" style="background:#fff;min-width:124px;">'
               + '<div class="fw-bold small text-dark text-nowrap">'+esc(court.courtName)+'</div>'
               + '</td>';

            (slots || []).forEach(function(slot, idx) {
                const key      = court.courtId + '_' + slot.slotId;
                const isBooked = bookedSet[key]   || false;
                const isPast   = disabledSet[key] || false;
                const price    = priceMap[key]    || 0;
                const isSel    = selectedSlots.some(function(s){
                    return s.courtId === court.courtId && s.slotIndex === idx;
                });

                let status = 'AVAILABLE';
                if      (isBooked) status = 'BOOKED';
                else if (isPast)   status = 'PAST';

                let cls = 'slot-cell ';
                if      (status === 'BOOKED') cls += 'slot-booked';
                else if (status === 'PAST')   cls += 'slot-past';
                else if (isSel)               cls += 'slot-selected';
                else                          cls += 'slot-available';

                const tip = status === 'BOOKED' ? 'Đã đặt'
                          : status === 'PAST'   ? 'Quá giờ'
                          : price > 0           ? fmtVnd(price)
                          : 'Chưa có giá';

                b += '<td class="'+cls+'"'
                  +  ' data-court="'+court.courtId+'"'
                  +  ' data-idx="'+idx+'"'
                  +  ' data-slotid="'+slot.slotId+'"'
                  +  ' data-start="'+esc(slot.startTime)+'"'
                  +  ' data-end="'+esc(slot.endTime)+'"'
                  +  ' data-price="'+price+'"'
                  +  ' data-status="'+status+'"'
                  +  ' data-price-tip="'+esc(tip)+'"'
                  +  ' tabindex="'+(status==='AVAILABLE'?'0':'-1')+'"'
                  +  '></td>';
            });
            b += '</tr>';
        });
        matrixBody.innerHTML = b;

        matrixBody.querySelectorAll('.slot-cell').forEach(function(cell){
            cell.addEventListener('click', onSlotClick);
            cell.addEventListener('keydown', function(e){
                if(e.key==='Enter'||e.key===' '){ e.preventDefault(); onSlotClick.call(cell); }
            });
        });
    }

    /* ── slot toggle ── */
    function onSlotClick() {
        const status = this.dataset.status;
        if (status==='BOOKED'||status==='PAST') return;

        const courtId  = parseInt(this.dataset.court,  10);
        const slotIndex= parseInt(this.dataset.idx,    10);
        const slotId   = parseInt(this.dataset.slotid, 10);  // real DB slot_id
        const startTime= this.dataset.start;
        const endTime  = this.dataset.end;
        const price    = parseFloat(this.dataset.price)||0;

        const pos = selectedSlots.findIndex(function(s){
            return s.courtId===courtId&&s.slotIndex===slotIndex;
        });
        if (pos>-1) {
            selectedSlots.splice(pos,1);
            this.classList.replace('slot-selected','slot-available');
        } else {
            selectedSlots.push({courtId, slotIndex, slotId, startTime, endTime, price});
            this.classList.replace('slot-available','slot-selected');
        }
        updateSummary(); hideAlert();
    }

    /* ── summary ── */
    function updateSummary() {
        const count     = selectedSlots.length;
        const totalMin  = count*30;
        const totalAmt  = selectedSlots.reduce(function(a,s){return a+s.price;},0);
        if (count===0) { summaryBar.classList.remove('visible'); return; }
        summaryBar.classList.add('visible');
        statSlots.textContent    = count+' slot';
        statDuration.textContent = fmtDur(totalMin);
        statPrice.textContent    = fmtVnd(totalAmt);
    }

    /* ── continue ── */
    continueBtn.addEventListener('click', function(){
        if (isSubmitting) return;
        if (selectedSlots.length===0){ showAlert('Vui lòng chọn ít nhất một khung giờ.','warning'); return; }
        if (!IS_LOGGED_IN)            { handleUnauth(); return; }
        doPreview();
    });

    function doPreview() {
        isSubmitting = true; setBtnLoading(true); hideAlert();
        const body = {
            facilityId:  parseInt(VENUE_ID,10),
            bookingDate: selectedDate,
            selections:  selectedSlots.map(function(s){
                return { courtId: s.courtId, slotId: s.slotId };
            })
        };
        fetch(CTX+'/api/single-booking/preview', {
            method:'POST',
            headers:{'Content-Type':'application/json','Accept':'application/json'},
            body: JSON.stringify(body)
        })
        .then(function(res){
            if(res.status===401) throw {_status:401};
            return res.json().then(function(j){if(!res.ok)throw j;return j;});
        })
        .then(function(json){
            const data = (json.success!==undefined&&json.data)?json.data:json;
            if (data.valid===false) {
                showAlert(data.errorMessage||'Lựa chọn không hợp lệ. Kiểm tra lại khung giờ.','error');
                setBtnLoading(false); isSubmitting=false; return;
            }
            sessionStorage.setItem('sbPreviewData', JSON.stringify({
                previewData: data,
                selections:  body.selections,   // [{courtId, slotId}]
                facilityId:  body.facilityId,
                bookingDate: body.bookingDate
            }));
            window.location.href = CTX+'/jsp/booking/singlebooking/preview.jsp';
        })
        .catch(function(err){
            setBtnLoading(false); isSubmitting=false;
            if(err&&err._status===401){ handleUnauth(); return; }
            showAlert((err&&err.error&&err.error.message)||(err&&err.message)||'Lỗi kết nối. Thử lại.','error');
        });
    }

    function setBtnLoading(on) {
        continueBtn.disabled = on;
        continueBtn.innerHTML = on
            ? '<span class="spinner-border spinner-border-sm me-2"></span>Đang xử lý...'
            : 'TIẾP TỤC <i class="bi bi-chevron-right"></i>';
    }

    /* ── auth ── */
    function handleUnauth() {
        sessionStorage.setItem('loginRedirect', window.location.href);
        window.location.href = CTX+'/auth/login';
    }

    /* ── init ── */
    selDateText.textContent = fmtDateDisplay(selectedDate);
    setBtnLoading(false);  // Reset button state khi load page
    isSubmitting = false;  // Reset submitting state
    loadMatrix();

    // Fix: Reset button khi quay lại từ preview (browser back/forward cache)
    window.addEventListener('pageshow', function(event) {
        if (event.persisted) {
            // Page được restore từ bfcache
            isSubmitting = false;
            setBtnLoading(false);
        }
    });
})();
</script>

</body>
</html>
