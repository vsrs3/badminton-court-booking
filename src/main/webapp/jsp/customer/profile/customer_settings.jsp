<%-- customer_settings.jsp --%>

<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/customer/customer-setting.css">

<div class="settings-page">

    <!-- HEADER -->
    <div class="settings-header">
        <div class="settings-title-group">
            <i data-lucide="settings" class="settings-title-icon"></i>
            <h1 class="settings-page-title">Cài đặt</h1>
        </div>
        <a href="${pageContext.request.contextPath}/home" class="btn-back-settings">
            <i data-lucide="arrow-left" class="icon-sm"></i>
            <span>Quay lại trang chủ</span>
        </a>
    </div>

    <!-- MENU LIST -->
    <div class="settings-list">

        <a href="profile?section=notifications" class="settings-row">
            <div class="settings-row-left">
                <div class="settings-icon-wrap" style="background:#EFF6FF;">
                    <i data-lucide="bell" class="icon-md" style="color:#3B82F6;"></i>
                </div>
                <span class="settings-row-label">Cài Đặt Thông Báo</span>
            </div>
            <i data-lucide="chevron-right" class="icon-sm settings-chevron"></i>
        </a>

        <a href="profile?section=languages" class="settings-row">
            <div class="settings-row-left">
                <div class="settings-icon-wrap" style="background:#F5F3FF;">
                    <i data-lucide="languages" class="icon-md" style="color:#7C3AED;"></i>
                </div>
                <span class="settings-row-label">Cài Đặt Ngôn Ngữ</span>
            </div>
            <i data-lucide="chevron-right" class="icon-sm settings-chevron"></i>
        </a>

        <a href="profile?section=change-password" class="settings-row">
            <div class="settings-row-left">
                <div class="settings-icon-wrap" style="background:#F0FDF4;">
                    <i data-lucide="key-round" class="icon-md" style="color:#064E3B;"></i>
                </div>
                <span class="settings-row-label">Đổi Mật Khẩu</span>
            </div>
            <i data-lucide="chevron-right" class="icon-sm settings-chevron"></i>
        </a>

    </div>
</div>

<script>
    var logoutForm = document.getElementById('logoutForm');
    if (logoutForm) {
        logoutForm.addEventListener('submit', function(e) {
            e.preventDefault();
            var ok = confirm('Bạn có chắc muốn đăng xuất không?');
            if (ok) e.target.submit();
        });
    }
</script>
