<%-- setting-form.jsp --%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ include file="../layout/staff-layout.jsp"%>
<%@ include file="../layout/staff-sidebar.jsp"%>

<link rel="stylesheet"
    href="${pageContext.request.contextPath}/assets/css/owner/dashboard.css">
<style>
.content-area {
    padding: 0 !important;
}
</style>

<div class="main-content">
    <%@ include file="../layout/staff-header.jsp"%>

    <div class="dov-wrap">
        <%-- ============================================================
             SETTINGS CARD
        ============================================================ --%>
        <div class="dov-card dov-fadein dov-d1">

            <%-- Card header --%>
            <div class="dov-card-header">
                <div style="display:flex; align-items:center; gap:8px;">
                    <div class="dov-stat-icon dov-stat-icon--green"
                         style="width:38px; height:38px; border-radius:10px; font-size:1rem;">
                        <i class="bi bi-gear-fill"></i>
                    </div>
                    <h3 class="dov-card-title" style="font-size:1.125rem; font-weight:900;
                        text-transform:uppercase; letter-spacing:.04em; color:#064E3B;">
                        Cài Đặt
                    </h3>
                </div>
            </div>

            <%-- Settings menu list --%>
            <div class="sof-list">

                <%-- Thông tin cá nhân --%>
                <a href="${pageContext.request.contextPath}/staff/setting/profile"
                   class="sof-row">
                    <div class="sof-row-left">
                        <div class="sof-icon-wrap" style="background:#F0FDF4;">
                            <i class="bi bi-person-fill" style="color:#064E3B; font-size:1.05rem;"></i>
                        </div>
                        <div class="sof-row-body">
                            <span class="sof-row-label">Thông Tin Cá Nhân</span>
                            <span class="sof-row-sub">Cập nhật tên, email và ảnh đại diện</span>
                        </div>
                    </div>
                    <i class="bi bi-chevron-right sof-chevron"></i>
                </a>

                <%-- Đổi mật khẩu --%>
                <a href="${pageContext.request.contextPath}/staff/setting/change-password"
                   class="sof-row">
                    <div class="sof-row-left">
                        <div class="sof-icon-wrap" style="background:#F0FDF4;">
                            <i class="bi bi-key-fill" style="color:#064E3B; font-size:1.05rem;"></i>
                        </div>
                        <div class="sof-row-body">
                            <span class="sof-row-label">Đổi Mật Khẩu</span>
                            <span class="sof-row-sub">Thay đổi mật khẩu đăng nhập</span>
                        </div>
                    </div>
                    <i class="bi bi-chevron-right sof-chevron"></i>
                </a>

            </div>
            <%-- end sof-list --%>

        </div>
        <%-- end settings card --%>

    </div>
    <%-- end dov-wrap --%>
</div>
<%-- end main-content --%>

<style>
/* ── Settings list rows ─────────────────────────────────────── */
.sof-list {
    display: flex;
    flex-direction: column;
    gap: 0;
    border: 1px solid #F3F4F6;
    border-radius: 12px;
    overflow: hidden;
}

.sof-row {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 1rem 1.25rem;
    text-decoration: none;
    color: #111827;
    border-bottom: 1px solid #F3F4F6;
    transition: background-color .15s ease, box-shadow .15s ease;
}
.sof-row:last-child { border-bottom: none; }
.sof-row:hover {
    background-color: #F0FDF4;
    box-shadow: inset 3px 0 0 #A3E635;
}

.sof-row-left {
    display: flex;
    align-items: center;
    gap: .875rem;
}

.sof-icon-wrap {
    width: 2.375rem;
    height: 2.375rem;
    border-radius: 10px;
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
    border: 1px solid #E5E7EB;
}

.sof-row-body {
    display: flex;
    flex-direction: column;
    gap: 2px;
}

.sof-row-label {
    font-size: .9375rem;
    font-weight: 700;
    color: #1F2937;
    line-height: 1.2;
}

.sof-row-sub {
    font-size: .78rem;
    color: #9CA3AF;
    font-weight: 500;
}

.sof-chevron {
    color: #D1D5DB;
    font-size: .875rem;
    flex-shrink: 0;
    transition: transform .15s ease, color .15s ease;
}
.sof-row:hover .sof-chevron {
    transform: translateX(3px);
    color: #A3E635;
}
</style>

<script src="https://cdn.jsdelivr.net/npm/sweetalert2@11"></script>

<%-- SweetAlert2 — hiện lỗi/thành công qua flash attribute nếu cần --%>
<c:if test="${not empty requestScope.toastError}">
<script>
    Swal.fire({
        icon: 'error',
        title: 'Lỗi',
        text: '${requestScope.toastError}',
        confirmButtonColor: '#064E3B'
    });
</script>
</c:if>
<c:if test="${not empty requestScope.toastSuccess}">
<script>
    Swal.fire({
        icon: 'success',
        title: 'Thành công',
        text: '${requestScope.toastSuccess}',
        confirmButtonColor: '#064E3B'
    });
</script>
</c:if>
