<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<nav class="bottom-nav">
    <div class="bottom-nav-container">

        <!-- Home Tab -->
        <button class="nav-item active" data-tab="home">
            <i class="bi bi-house-door-fill"></i>
            <span class="nav-text">Trang chủ</span>
            <div class="nav-indicator"></div>
        </button>

        <!-- Map Tab -->
        <button class="nav-item" data-tab="map">
            <i class="bi bi-grid-3x3-gap-fill"></i>
            <span class="nav-text">Bản đồ</span>
        </button>

        <!-- Center Button: Community -->
        <div class="nav-center">
            <button class="nav-center-btn" data-tab="booking">
                <i class="bi bi-people-fill"></i>
            </button>
            <span class="nav-center-text">Cộng đồng</span>
        </div>

        <!-- Offer Tab -->
        <button class="nav-item" data-tab="offer">
            <i class="bi bi-lightning-charge-fill"></i>
            <span class="nav-text">Ưu đãi</span>
        </button>

        <!-- Profile Tab -->
        <a href="${pageContext.request.contextPath}/profile" class="nav-item" data-tab="profile" style="text-decoration: none">
            <i class="bi bi-person-fill"></i>
            <span class="nav-text">Cá nhân</span>
        </a>

    </div>
</nav>